package ca.corbett.extras.image.animation;

import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.plaf.LayerUI;
import java.awt.AWTEvent;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Arrays;

/**
 * A custom LayerUI that can apply a blur effect to a JPanel
 * and display an optional text message over it. While blurred,
 * the panel will not respond to mouse or keyboard events.
 * <p>
 * <b>USAGE:</b> Create a JLayer with an instance of this BlurLayerUI,
 * wrapping the JPanel you want to blur. Call setBlurred(true) to
 * enable the blur effect, and setBlurred(false) to disable it.
 * Alternatively, use blurOut() and blurIn() for animated transitions.
 * </p>
 * <pre>
 *     JPanel contentPanel = ...;
 *     BlurLayerUI blurLayerUI = new BlurLayerUI();
 *     JLayer&lt;JPanel&gt; layeredPanel = new JLayer&lt;&gt;(contentPanel, blurLayerUI);
 *     containerPanel.add(layeredPanel);
 *
 *     // Set your blur options, or live with the defaults:
 *     blurLayerUI.setOverlayText("Please wait...");
 *     blurLayerUI.setBlurIntensity(BlurLayerUI.BlurIntensity.STRONG);
 *
 *     // Then, in a button handler or whatever:
 *     blurLayerUI.setBlurred(true); // to enable blur instantly
 *     blurLayerUI.setBlurred(false); // to disable blur instantly
 *
 *     // Or use animated blur:
 *     blurLayerUI.blurOut(() -&gt; {
 *         // Callback after blur completes
 *     });
 *     blurLayerUI.blurIn(() -&gt; {
 *         // Callback after unblur completes
 *     });
 * </pre>
 * <p>
 * <b>Side note:</b> To give credit where credit is due, claude.ai wrote
 * the blur algorithm. I've looked at that code and I have no idea how it works :-/
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a> with claude.ai
 * @since swing-extras 2.7
 */
public class BlurLayerUI extends LayerUI<JPanel> {

    public static final BlurIntensity DEFAULT_INTENSITY = BlurIntensity.MEDIUM;
    public static final Color DEFAULT_BLUR_OVERLAY_COLOR = new Color(255, 255, 255);
    public static final int DEFAULT_TEXT_SIZE = 18;
    public static final int TEXT_MINIMUM_SIZE = 6;
    public static final int TEXT_MAXIMUM_SIZE = 72;
    public static final Color DEFAULT_TEXT_COLOR = Color.BLACK;
    public static final AnimationDuration DEFAULT_ANIMATION_DURATION = AnimationDuration.Medium;
    public static final AnimationSpeed DEFAULT_ANIMATION_SPEED = AnimationSpeed.Medium;

    private static final int BLUR_ALPHA = 100;

    /**
     * Blur intensity presets controlling the blur kernel size.
     */
    public enum BlurIntensity {
        MILD(3, "Mild Blur"),
        MEDIUM(5, "Medium Blur"),
        STRONG(9, "Strong Blur"),
        EXTREME(15, "Extreme Blur");

        private final int kernelSize;
        private final String label;

        BlurIntensity(int kernelSize, String label) {
            this.kernelSize = kernelSize;
            this.label = label;
        }

        public int getKernelSize() {
            return kernelSize;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private boolean blurred = false;
    private BufferedImage blurredImage;
    private Color blurOverlayColor;
    private String overlayText;
    private int overlayTextSize;
    private Color overlayTextColor;
    private BlurIntensity blurIntensity;
    
    // Animation support
    private float blurOpacity = 0f;
    private Timer timer;
    private boolean animating = false;
    private Runnable onComplete;
    private JLayer<JPanel> layer;
    private AnimationDuration animationDuration;
    private AnimationSpeed animationSpeed;

    /**
     * Creates a new BlurLayerUI instance with no blur applied initially
     * and no overlay text.
     */
    public BlurLayerUI() {
        this.blurred = false;
        setBlurOverlayColor(DEFAULT_BLUR_OVERLAY_COLOR);
        this.overlayText = null;
        this.blurIntensity = DEFAULT_INTENSITY;
        this.overlayTextSize = DEFAULT_TEXT_SIZE;
        this.overlayTextColor = DEFAULT_TEXT_COLOR;
        this.animationDuration = DEFAULT_ANIMATION_DURATION;
        this.animationSpeed = DEFAULT_ANIMATION_SPEED;
    }

    /**
     * Sets whether the panel should be blurred instantly (without animation).
     * For animated blur transitions, use blurOut() and blurIn() instead.
     */
    public void setBlurred(boolean blurred) {
        boolean oldValue = this.blurred;
        this.blurred = blurred;
        this.blurOpacity = blurred ? 1f : 0f;
        if (!blurred) {
            blurredImage = null; // Clear cached image
        }

        // Request a repaint to reflect the change
        firePropertyChange("blurred", oldValue, this.blurred);
    }

    /**
     * Returns whether the panel is currently blurred.
     */
    public boolean isBlurred() {
        return blurred;
    }

    /**
     * Gets the configured animation duration.
     */
    public AnimationDuration getAnimationDuration() {
        return animationDuration;
    }

    /**
     * Sets the animation duration for blur transitions.
     *
     * @param animationDuration the animation duration to use; must not be {@code null}
     * @return this {@link BlurLayerUI} instance for chaining
     * @throws IllegalArgumentException if {@code animationDuration} is {@code null}
     */
    public BlurLayerUI setAnimationDuration(AnimationDuration animationDuration) {
        if (animationDuration == null) {
            throw new IllegalArgumentException("animationDuration must not be null");
        }
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
     * Sets the animation speed for blur transitions.
     *
     * @param animationSpeed the animation speed to use; must not be {@code null}
     * @return this {@link BlurLayerUI} instance for method chaining
     * @throws IllegalArgumentException if {@code animationSpeed} is {@code null}
     */
    public BlurLayerUI setAnimationSpeed(AnimationSpeed animationSpeed) {
        if (animationSpeed == null) {
            throw new IllegalArgumentException("animationSpeed must not be null");
        }
        this.animationSpeed = animationSpeed;
        return this;
    }

    /**
     * Returns the color used for the blur overlay.
     */
    public Color getBlurOverlayColor() {
        return blurOverlayColor;
    }

    /**
     * Sets the color used for the blur overlay. The alpha value of the supplied
     * color is ignored and a default semi-transparent alpha is used instead.
     */
    public void setBlurOverlayColor(Color blurOverlayColor) {
        // Take the r,g,b from the input and supply our own alpha:
        // (otherwise callers might supply a value which makes no sense visually)
        this.blurOverlayColor = new Color(
                blurOverlayColor.getRed(),
                blurOverlayColor.getGreen(),
                blurOverlayColor.getBlue(),
                BLUR_ALPHA);
    }

    /**
     * Returns the text that will be overlaid on the blurred panel.
     */
    public String getOverlayText() {
        return overlayText;
    }

    /**
     * Sets optional text to overlay on top of the panel when it is blurred.
     * If null, no text will be overlaid.
     */
    public BlurLayerUI setOverlayText(String overlayText) {
        this.overlayText = overlayText;
        return this;
    }

    /**
     * Returns the overlay text font point size.
     */
    public int getOverlayTextSize() {
        return overlayTextSize;
    }

    /**
     * Sets the overlay text font point size. The input size must be any
     * value between TEXT_MINIMUM_SIZE and TEXT_MAXIMUM_SIZE, otherwise
     * an IllegalArgumentException will be thrown.
     */
    public void setOverlayTextSize(int overlayTextSize) {
        // Ignore unreasonable sizes
        if (overlayTextSize < TEXT_MINIMUM_SIZE || overlayTextSize > TEXT_MAXIMUM_SIZE) {
            throw new IllegalArgumentException("Invalid text size " + overlayTextSize
                                                       + ": overlayTextSize must be between " +
                                                       TEXT_MINIMUM_SIZE + " and " + TEXT_MAXIMUM_SIZE);
        }
        this.overlayTextSize = overlayTextSize;
    }

    /**
     * Returns the overlay text color.
     */
    public Color getOverlayTextColor() {
        return overlayTextColor;
    }

    /**
     * Sets the overlay text color.
     */
    public void setOverlayTextColor(Color overlayTextColor) {
        this.overlayTextColor = overlayTextColor;
    }

    /**
     * Returns the current blur intensity setting.
     */
    public BlurIntensity getBlurIntensity() {
        return blurIntensity;
    }

    /**
     * Sets the blur intensity preset.
     */
    public BlurLayerUI setBlurIntensity(BlurIntensity intensity) {
        if (intensity == null) {
            throw new IllegalArgumentException("intensity must not be null");
        }
        BlurIntensity old = this.blurIntensity;
        if (old != intensity) {
            this.blurIntensity = intensity;
            this.blurredImage = null; // clear cached image so new intensity is applied
            firePropertyChange("blurIntensity", old, intensity);
        }
        return this;
    }

    /**
     * Performs a "blur out" animation, gradually applying the blur effect.
     * Optionally, you can provide a Runnable that will be executed when the blur
     * animation completes.
     *
     * @param onComplete An optional Runnable to invoke when the blur completes (may be null).
     */
    public void blurOut(Runnable onComplete) {
        if (animating) { return; }

        this.onComplete = onComplete;
        this.blurred = true;
        this.blurOpacity = 0f;
        this.animating = true;

        float increment = (float)animationSpeed.getDelayMS() / animationDuration.getDurationMS();

        timer = new Timer(animationSpeed.getDelayMS(), e -> {
            blurOpacity += increment;
            if (blurOpacity >= 1f) {
                blurOpacity = 1f;
                timer.stop();
                animating = false;
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
     * Performs a "blur in" animation, gradually removing the blur effect.
     * Optionally, you can provide a Runnable that will be executed when the blur
     * removal animation completes.
     *
     * @param onComplete An optional Runnable to invoke when the unblur completes (may be null).
     */
    public void blurIn(Runnable onComplete) {
        
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }

        this.onComplete = onComplete;
        this.blurOpacity = 1f;
        this.animating = true;

        float decrement = (float)animationSpeed.getDelayMS() / animationDuration.getDurationMS();

        timer = new Timer(animationSpeed.getDelayMS(), e -> {
            blurOpacity -= decrement;
            if (blurOpacity <= 0f) {
                blurOpacity = 0f;
                timer.stop();
                animating = false;
                this.blurred = false;
                blurredImage = null; // Clear cached image
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
    public void paint(Graphics g, JComponent c) {
        // Paint the component normally first
        super.paint(g, c);

        if (blurred || blurOpacity > 0f) {
            // Create blurred overlay
            if (blurredImage == null ||
                    blurredImage.getWidth() != c.getWidth() ||
                    blurredImage.getHeight() != c.getHeight()) {

                blurredImage = createBlurredImage(c);
            }

            // Draw the blurred image over the component with opacity
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, blurOpacity));
            g2.drawImage(blurredImage, 0, 0, null);
            g2.dispose();

            // Add semi-transparent overlay
            g.setColor(blurOverlayColor);
            Graphics2D g2d = (Graphics2D)g.create();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, blurOpacity));
            g2d.fillRect(0, 0, c.getWidth(), c.getHeight());
            g2d.dispose();

            if (overlayText != null && !overlayText.isBlank()) {
                g.setColor(overlayTextColor);
                g.setFont(g.getFont().deriveFont(Font.BOLD, (float)overlayTextSize));
                FontMetrics fm = g.getFontMetrics();
                int x = (c.getWidth() - fm.stringWidth(overlayText)) / 2;
                int y = c.getHeight() / 2;

                // Enable antialiasing for smoother text
                Graphics2D g2text = (Graphics2D)g.create();
                g2text.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                                     java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2text.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, blurOpacity));
                g2text.drawString(overlayText, x, y);
                g2text.dispose();
            }
        }
    }

    /**
     * Invoked internally to capture an image of the given component and then
     * overlay a blur effect on it.
     *
     * @param c any JComponent
     * @return a blurred BufferedImage of the component
     */
    private BufferedImage createBlurredImage(JComponent c) {
        int w = Math.max(1, c.getWidth());
        int h = Math.max(1, c.getHeight());

        BufferedImage source = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = source.createGraphics();
        c.paint(g2);
        g2.dispose();

        int requested = blurIntensity.getKernelSize();
        int maxAllowed = Math.min(w, h);
        int kernelSize = Math.min(requested, Math.max(1, maxAllowed));

        // Ensure odd kernel size (commonly used for convolution kernels)
        if ((kernelSize & 1) == 0) {
            kernelSize = Math.max(1, kernelSize - 1);
        }

        // Build a box blur kernel (mean filter)
        int elems = kernelSize * kernelSize;
        float[] blurKernel = new float[elems];
        Arrays.fill(blurKernel, 1.0f / elems);

        Kernel kernel = new Kernel(kernelSize, kernelSize, blurKernel);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);

        BufferedImage blurred = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        op.filter(source, blurred);

        return blurred;
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
        // Reset animation and blur state so this UI instance can be safely reused
        animating = false;
        onComplete = null;
        blurred = false;
        blurOpacity = 0.0f;
        if (c instanceof JLayer) {
            ((JLayer<?>) c).setLayerEventMask(0);
        }
        layer = null;
        blurredImage = null;
    }

    @Override
    protected void processMouseEvent(MouseEvent e, JLayer<? extends JPanel> l) {
        // Block mouse events when blurred or animating
        if (blurred || animating) {
            e.consume();
        }
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JPanel> l) {
        // Block mouse motion events when blurred or animating
        if (blurred || animating) {
            e.consume();
        }
    }

    @Override
    protected void processKeyEvent(KeyEvent e, JLayer<? extends JPanel> l) {
        // Block keyboard events when blurred or animating
        if (blurred || animating) {
            e.consume();
        }
    }
}
