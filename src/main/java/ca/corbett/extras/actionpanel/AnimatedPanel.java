package ca.corbett.extras.actionpanel;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * An internal panel that wraps the actions panel and provides animated expand/collapse functionality.
 * The panel gradually changes its preferred height to create a smooth sliding effect.
 *
 * @author claude.ai
 * @since swing-extras 2.8
 */
class AnimatedPanel extends JPanel {
    private final JPanel contentPanel;
    private int currentHeight;
    private int targetHeight;
    private Timer animationTimer;

    public AnimatedPanel(JPanel contentPanel) {
        this.contentPanel = contentPanel;
        setLayout(new BorderLayout());
        add(contentPanel, BorderLayout.CENTER);

        // Initialize heights
        this.targetHeight = contentPanel.getPreferredSize().height;
        this.currentHeight = targetHeight;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension contentSize = contentPanel.getPreferredSize();
        return new Dimension(contentSize.width, currentHeight);
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension contentSize = contentPanel.getMaximumSize();
        return new Dimension(contentSize.width, currentHeight);
    }

    /**
     * Sets the panel to fully expanded state (instantaneous).
     */
    public void setFullyExpanded() {
        stopAnimation();
        targetHeight = contentPanel.getPreferredSize().height;
        currentHeight = targetHeight;
        revalidate();
        repaint();
    }

    /**
     * Sets the panel to fully collapsed state (instantaneous).
     */
    public void setFullyCollapsed() {
        stopAnimation();
        targetHeight = 0;
        currentHeight = 0;
        revalidate();
        repaint();
    }

    /**
     * Animates the panel expanding to its full height.
     *
     * @param durationMs The duration of the animation in milliseconds.
     */
    public void animateExpand(int durationMs) {
        stopAnimation();
        targetHeight = contentPanel.getPreferredSize().height;

        if (currentHeight >= targetHeight) {
            // Already expanded
            currentHeight = targetHeight;
            revalidate();
            repaint();
            return;
        }

        startAnimation(durationMs);
    }

    /**
     * Animates the panel collapsing to zero height.
     *
     * @param durationMs The duration of the animation in milliseconds.
     */
    public void animateCollapse(int durationMs) {
        stopAnimation();
        targetHeight = 0;

        if (currentHeight <= 0) {
            // Already collapsed
            currentHeight = 0;
            revalidate();
            repaint();
            return;
        }

        startAnimation(durationMs);
    }

    /**
     * Starts the animation timer.
     */
    private void startAnimation(int durationMs) {
        final int startHeight = currentHeight;
        final int heightDifference = targetHeight - startHeight;
        final long startTime = System.currentTimeMillis();

        animationTimer = new Timer(ActionPanel.ANIMATION_FRAME_DELAY_MS, e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            double progress = Math.min(1.0, (double)elapsed / durationMs);

            // Use ease-in-out function for smoother animation
            double easedProgress = easeInOutCubic(progress);

            currentHeight = startHeight + (int)(heightDifference * easedProgress);

            revalidate();
            repaint();

            if (progress >= 1.0) {
                currentHeight = targetHeight;
                stopAnimation();
                revalidate();
                repaint();
            }
        });
        animationTimer.start();
    }

    /**
     * Stops any running animation.
     */
    private void stopAnimation() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
    }

    /**
     * Easing function for smoother animation (ease-in-out cubic).
     */
    private double easeInOutCubic(double t) {
        if (t < 0.5) {
            return 4 * t * t * t;
        }
        else {
            double f = 2 * t - 2;
            return 1 + f * f * f / 2;
        }
    }
}
