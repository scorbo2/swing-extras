package ca.corbett.extras.image.animation;

import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.plaf.LayerUI;
import java.awt.AWTEvent;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * This is a LayerUI implementation that can be used to "fade" a JPanel
 * in and out by overlaying a colored translucent layer that animates
 * its opacity. This can be a neat way of providing a transition effect
 * if you need to swap out the content being shown in a JPanel, instead
 * of just abruptly changing it.
 * <p>
 * <b>USAGE:</b> Create a JLayer with an instance of this FadeLayerUI,
 * wrapping the JPanel you want to fade in/out. Call fadeOut() to start
 * the fade out animation, and provide a Runnable that will be
 * executed when the fade out completes (typically to swap the content
 * being shown). Then call fadeIn() to fade back in, again providing
 * a Runnable to be executed when the fade in completes (typically
 * to re-enable user interaction).
 * </p>
 * <pre>
 *     JPanel contentPanel = ...;
 *     FadeLayerUI fadeLayerUI = new FadeLayerUI();
 *     JLayer&lt;JPanel&gt; layeredPanel = new JLayer&lt;&gt;(contentPanel, fadeLayerUI);
 *     containerPanel.add(layeredPanel);
 *
 *     // Set your fade options, or live with the defaults:
 *     fadeLayerUI.setFadeColor(Color.BLUE);
 *     fadeLayerUI.setAnimationDuration(FadeLayerUI.AnimationDuration.VeryLong);
 *
 *     // Then, in a button handler or whatever:
 *     fadeLayerUI.fadeOut(() -&gt; {
 *         // Swap content here
 *         fadeLayerUI.fadeIn(() -&gt;
 *             // Re-enable interaction here
 *         });
 *     });
 *
 *     // Or, more simply, if you just want to fade out, swap, and fade in:
 *     fadeLayerUI.fadeOut(() -&gt; {
 *         // Swap content here
 *         fadeLayerUI.fadeIn(null); // No action needed after fade in
 *     });
 * </pre>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a> with claude.ai
 * @since swing-extras 2.7
 */
public class FadeLayerUI extends LayerUI<JPanel> {

    public static final Color DEFAULT_FADE_COLOR = Color.WHITE;
    public static final AnimationDuration DEFAULT_ANIMATION_DURATION = AnimationDuration.Medium;
    public static final AnimationSpeed DEFAULT_ANIMATION_SPEED = AnimationSpeed.Medium;

    /**
     * Controls the total duration of the fade animation.
     */
    public enum AnimationDuration {
        VeryShort(100),
        Short(200),
        Medium(400),
        Long(600),
        VeryLong(1000);

        private final int milliseconds;

        AnimationDuration(int ms) {
            this.milliseconds = ms;
        }

        public int getDurationMS() {
            return milliseconds;
        }

        @Override
        public String toString() {
            return name() + ": " + milliseconds + "ms";
        }

        public static AnimationDuration fromLabel(String label) {
            for (AnimationDuration candidate : values()) {
                if (candidate.toString().equals(label)) {
                    return candidate;
                }
            }
            return null;
        }
    }

    /**
     * Controls the speed of the animation frames, in terms of the delay between frames.
     * The toString() method provides both the delay in milliseconds and the roughly equivalent FPS.
     */
    public enum AnimationSpeed {
        VerySlow(100),
        Slow(60),
        Medium(40),
        Fast(25),
        VeryFast(16);

        private final int timerDelay;

        AnimationSpeed(int delay) {
            this.timerDelay = delay;
        }

        public int getDelayMS() {
            return timerDelay;
        }

        @Override
        public String toString() {
            return name() + ": " + timerDelay + "ms = " + (1000 / timerDelay) + "FPS";
        }

        public static AnimationSpeed fromLabel(String label) {
            for (AnimationSpeed candidate : values()) {
                if (candidate.toString().equals(label)) {
                    return candidate;
                }
            }
            return null;
        }
    }

    private float opacity = 0f;
    private Timer timer;
    private boolean animating = false;
    private Runnable onComplete;
    private JLayer<JPanel> layer;

    private Color fadeColor;
    private AnimationDuration animationDuration;
    private AnimationSpeed animationSpeed;

    // Cache for the fade overlay
    private BufferedImage fadeCache;
    private int lastWidth = -1;
    private int lastHeight = -1;

    /**
     * Creates a new FadeLayerUI with default settings. You can associate it with
     * a JLayer&lt;JPanel&gt; to use it:
     * <pre>
     *     JPanel contentPanel = ...;
     *     FadeLayerUI fadeLayerUI = new FadeLayerUI();
     *     JLayer&lt;JPanel&gt; layeredPanel = new JLayer&lt;&gt;(contentPanel, fadeLayerUI);
     * </pre>
     * <p>
     * Then you can use the fadeOut() and fadeIn() methods in this class to perform
     * the fade animations.
     * </p>
     */
    public FadeLayerUI() {
        fadeColor = DEFAULT_FADE_COLOR;
        animationDuration = DEFAULT_ANIMATION_DURATION;
        animationSpeed = DEFAULT_ANIMATION_SPEED;
    }

    /**
     * Gets the fade color that will be used during the fade animation.
     */
    public Color getFadeColor() {
        return fadeColor;
    }

    /**
     * Sets the fade color that will be used during the fade animation.
     * The default is white.
     */
    public FadeLayerUI setFadeColor(Color fadeColor) {
        this.fadeColor = fadeColor;
        return this;
    }

    /**
     * Gets the configured animation duration.
     */
    public AnimationDuration getAnimationDuration() {
        return animationDuration;
    }

    /**
     * Sets the animation duration.
     */
    public FadeLayerUI setAnimationDuration(AnimationDuration animationDuration) {
        this.animationDuration = animationDuration;
        return this;
    }

    /**
     * Gets the configured animation speed.
     */
    public AnimationSpeed getAnimationSpeed() {
        return animationSpeed;
    }

    /**
     * Sets the animation speed.
     */
    public FadeLayerUI setAnimationSpeed(AnimationSpeed animationSpeed) {
        this.animationSpeed = animationSpeed;
        return this;
    }

    /**
     * Performs a "fade out" animation, fading the content to the configured fade color.
     * Optionally, you can provide a Runnable that will be executed when the fade out
     * completes. This is typically where you would swap the content being shown
     * in the underlying JPanel.
     *
     * @param onComplete An optional Runnable to invoke when the fade out completes (may be null).
     */
    public void fadeOut(Runnable onComplete) {
        if (animating) { return; }

        this.onComplete = onComplete;
        this.opacity = 0f;
        this.animating = true;

        float increment = (float)animationSpeed.getDelayMS() / animationDuration.getDurationMS();

        timer = new Timer(animationSpeed.getDelayMS(), e -> {
            opacity += increment;
            if (opacity >= 1f) {
                opacity = 1f;
                timer.stop();
                animating = false;
                fadeCache = null; // Clear cache
                // Force a final repaint at full opacity before executing callback
                if (layer != null) {
                    layer.repaint();
                    // Use invokeLater to ensure repaint completes before callback
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        if (this.onComplete != null) {
                            this.onComplete.run();
                        }
                    });
                }
                else if (this.onComplete != null) {
                    this.onComplete.run();
                }
            }
            else if (layer != null) {
                layer.repaint();
            }
        });
        timer.start();
    }

    /**
     * Performs a "fade in" animation, fading the content back from the fade color
     * to fully visible. Optionally, you can provide a Runnable that will be executed
     * when the fade in completes. This is typically where you would re-enable user
     * interaction with the underlying JPanel.
     *
     * @param onComplete An optional Runnable to invoke when the fade in completes (may be null).
     */
    public void fadeIn(Runnable onComplete) {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }

        this.onComplete = onComplete;
        this.opacity = 1f;
        this.animating = true;

        float decrement = (float)animationSpeed.getDelayMS() / animationDuration.getDurationMS();

        timer = new Timer(animationSpeed.getDelayMS(), e -> {
            opacity -= decrement;
            if (opacity <= 0f) {
                opacity = 0f;
                timer.stop();
                animating = false;
                fadeCache = null; // Clear cache
                // Force a final repaint at zero opacity before executing callback
                if (layer != null) {
                    layer.repaint();
                    // Use invokeLater to ensure repaint completes before callback
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        if (this.onComplete != null) {
                            this.onComplete.run();
                        }
                    });
                }
                else if (this.onComplete != null) {
                    this.onComplete.run();
                }
            }
            else if (layer != null) {
                layer.repaint();
            }
        });
        timer.start();
    }


    @Override
    @SuppressWarnings("unchecked")
    public void installUI(JComponent c) {
        super.installUI(c);
        if (c instanceof JLayer) {
            layer = (JLayer<JPanel>)c;
            // Ensure the layer receives all mouse events
            ((JLayer<JPanel>)c).setLayerEventMask(
                    AWTEvent.MOUSE_EVENT_MASK |
                            AWTEvent.MOUSE_MOTION_EVENT_MASK |
                            AWTEvent.KEY_EVENT_MASK
            );
        }
    }

    @Override
    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
        layer = null;
        fadeCache = null;
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        super.paint(g, c);

        if (opacity > 0f) {
            int width = c.getWidth();
            int height = c.getHeight();

            // Create or recreate cache if dimensions changed
            if (fadeCache == null || lastWidth != width || lastHeight != height) {
                fadeCache = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D cacheG = fadeCache.createGraphics();
                cacheG.setColor(fadeColor);
                cacheG.fillRect(0, 0, width, height);
                cacheG.dispose();
                lastWidth = width;
                lastHeight = height;
            }

            Graphics2D g2 = (Graphics2D)g.create();
            // Disable anti-aliasing for better performance on large fills
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            g2.drawImage(fadeCache, 0, 0, null);
            g2.dispose();
        }
    }

    // Block mouse events during animation
    @Override
    protected void processMouseEvent(MouseEvent e, JLayer<? extends JPanel> l) {
        if (animating) {
            e.consume();
        }
        else {
            super.processMouseEvent(e, l);
        }
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JPanel> l) {
        if (animating) {
            e.consume();
        }
        else {
            super.processMouseMotionEvent(e, l);
        }
    }

    // Block keyboard events during animation
    @Override
    protected void processKeyEvent(java.awt.event.KeyEvent e, JLayer<? extends JPanel> l) {
        if (animating) {
            e.consume();
        }
        else {
            super.processKeyEvent(e, l);
        }
    }
}
