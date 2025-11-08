package ca.corbett.extras.demo.panels;

import ca.corbett.extras.image.ImagePanel;
import ca.corbett.extras.image.ImagePanelConfig;
import ca.corbett.extras.image.ImageUtil;
import ca.corbett.extras.image.animation.ImageScroller;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.PanelField;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A demo panel to show off the ImageScroller class.
 * <p>
 * I'm hesitant to include this in the swing-extras demo app, as this really
 * has nothing to do with Swing... and also because the ImageScroller class
 * was more intended for fullscreen animations, and not for use in a
 * Swing container. But, it's kind of neat, so here we go.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class AnimationScrollDemoPanel extends PanelBuilder {
    private final BufferedImage canvas;
    private final BufferedImage scrollImage;
    private AnimationThread worker;
    private static final int IMG_WIDTH = 360;
    private static final int IMG_HEIGHT = 120;
    private ComboField<String> speedField;
    private ComboField<String> bounceTypeField;
    private ComboField<String> bounceMarginField;
    private ImagePanel imagePanel;

    public AnimationScrollDemoPanel() {
        canvas = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_INT_RGB);
        BufferedImage img;
        try {
            img = ImageUtil.loadFromResource(AnimationScrollDemoPanel.class, "/swing-extras/images/image-scroll-demo.jpg");
        }
        catch (IOException e) {
            img = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_INT_RGB);
        }
        scrollImage = img;
    }

    @Override
    public String getTitle() {
        return "Animation: scroll";
    }

    @Override
    public JPanel build() {
        FormPanel formPanel = buildFormPanel("ImageScroller demo");

        // A ComboField for selecting animation speed:
        List<String> options = new ArrayList<>(List.of("Very slow", "Slow", "Medium", "Fast", "Very fast"));
        speedField = new ComboField<>("Scroll speed:", options, 1, false);
        speedField.addValueChangedListener(field -> updateSpeed());
        formPanel.add(speedField);

        // A ComboField for selecting the "bounce" type (linear is boring, the other options are more slow-then-reverse)
        options = List.of("None", "Linear", "Quadratic", "Cubic");
        bounceTypeField = new ComboField<>("Bounce type:", options, 1, false);
        bounceTypeField.addValueChangedListener(field -> updateBounceType());
        formPanel.add(bounceTypeField);

        // A ComboField for selecting "bounce margin" (percentage of the image to use as the deceleration zone):
        options = List.of("Small", "Medium", "Large");
        bounceMarginField = new ComboField<>("Bounce margin:", options, 1, false);
        bounceMarginField.addValueChangedListener(field -> updateBounceMargin());
        formPanel.add(bounceMarginField);

        // We can use PanelField to wrap the ImagePanel that we'll use for displaying the animation:
        PanelField panelField = new PanelField(new FlowLayout(FlowLayout.LEFT));
        imagePanel = new ImagePanel(ImagePanelConfig.createSimpleReadOnlyProperties());
        imagePanel.setPreferredSize(new Dimension(IMG_WIDTH, IMG_HEIGHT));
        imagePanel.setImage(canvas);
        panelField.getPanel().add(imagePanel);
        formPanel.add(panelField);

        // And a separate PanelField can be used to house the start/stop buttons:
        panelField = new PanelField(new FlowLayout(FlowLayout.LEFT));
        JButton button = new JButton("Scroll!");
        button.addActionListener(e -> go());
        panelField.getPanel().add(button);

        button = new JButton("Stop");
        button.addActionListener(e -> stop());
        panelField.getPanel().add(button);
        formPanel.add(panelField);

        // We can add a cheesy disclaimer and some informational text:
        formPanel.add(new LabelField("ImageScroller is better suited for fullscreen applications!"));
        formPanel.add(new LabelField("It can scroll an oversized image with configurable 'bounce' parameters."));

        return formPanel;
    }

    /**
     * Invoked internally to stop the current animation (if in progress) and start a new one
     * using current parameters.
     */
    private void go() {
        if (worker != null) {
            worker.stop();
        }
        worker = new AnimationThread(canvas, scrollImage, IMG_WIDTH, IMG_HEIGHT, imagePanel);
        updateSpeed();
        updateBounceType();
        updateBounceMargin();
        new Thread(worker).start();
    }

    /**
     * Invoked internally to update the animation speed selection.
     */
    private void updateSpeed() {
        if (worker != null) {
            ImageScroller.ScrollSpeed speed = switch (speedField.getSelectedIndex()) {
                case 0 -> ImageScroller.ScrollSpeed.VERY_SLOW;
                case 1 -> ImageScroller.ScrollSpeed.SLOW;
                case 3 -> ImageScroller.ScrollSpeed.FAST;
                case 4 -> ImageScroller.ScrollSpeed.VERY_FAST;
                default -> ImageScroller.ScrollSpeed.MEDIUM; // safe default
            };
            worker.getImageScroller().setScrollSpeed(speed);
        }
    }

    /**
     * Invoked internally to update the bounce type that we will use.
     * This affects whether the image will slow as it approaches a scroll limit, then reverse,
     * then accelerate as it moves away from the scroll limit. You can turn this off for
     * a simple full-speed bounce where there is no deceleration or acceleration.
     */
    private void updateBounceType() {
        if (worker != null) {
            int selectedIndex = bounceTypeField.getSelectedIndex();
            switch (selectedIndex) {
                case 0: {
                    worker.getImageScroller().setMinSpeedRatio(1.0f);
                } break;
                case 1: {
                    worker.getImageScroller().setMinSpeedRatio(0.1f);
                    worker.getImageScroller().setEasingStrength(ImageScroller.EasingStrength.LINEAR);
                } break;
                case 2: {
                    worker.getImageScroller().setMinSpeedRatio(0.1f);
                    worker.getImageScroller().setEasingStrength(ImageScroller.EasingStrength.QUADRATIC);
                } break;
                case 3: {
                    worker.getImageScroller().setMinSpeedRatio(0.1f);
                    worker.getImageScroller().setEasingStrength(ImageScroller.EasingStrength.CUBIC);
                } break;
            }
        }
    }

    /**
     * Invoked internally to set the bounce margin. This is the percentage of the image that will
     * be used for deceleration and acceleration for certain bounce types.
     */
    private void updateBounceMargin() {
        if (worker != null) {
            switch (bounceMarginField.getSelectedIndex()) {
                case 0: {
                    worker.getImageScroller().setBounceZoneRatio(0.05f);
                } break;

                case 1: {
                    worker.getImageScroller().setBounceZoneRatio(0.12f);
                } break;

                case 2: {
                    worker.getImageScroller().setBounceZoneRatio(0.24f);
                } break;
            }
        }
    }

    /**
     * Invoked by the user via the "stop" button. Will kill the current worker thread, if any.
     */
    private void stop() {
        if (worker != null && worker.isRunning()) {
            worker.stop();
        }
    }

    private static class AnimationThread implements Runnable {

        private volatile boolean isRunning = false;
        private final int width;
        private final int height;
        private final BufferedImage canvas;
        private final ImageScroller imageScroller;
        private final ImagePanel imagePanel;

        public AnimationThread(BufferedImage canvas, BufferedImage scrollImage, int w, int h, ImagePanel imagePanel) {
            this.width = w;
            this.height = h;
            this.canvas = canvas;
            imageScroller = new ImageScroller(scrollImage, w, h);
            this.imagePanel = imagePanel;
        }

        public ImageScroller getImageScroller() {
            return imageScroller;
        }

        public void stop() {
            isRunning = false;
        }

        public boolean isRunning() {
            return isRunning;
        }

        @Override
        public void run() {
            isRunning = true;

            while (isRunning) {
                // Render the next frame of the animation:
                Graphics2D g = canvas.createGraphics();
                imageScroller.renderFrame(g);
                g.dispose();

                // Marshal the UI update to the Swing Event Dispatch Thread - important!
                //   Don't update Swing UI components from a worker thread directly!
                SwingUtilities.invokeLater(() -> imagePanel.setImage(canvas));

                try {
                    // We'll aim for 20 frames per second of animation.
                    // Any faster will strain the cpu because we're not in a fullscreen
                    // application, but rather working within a Swing app with all
                    // of its repaint overhead. This is not really what the ImageScroller
                    // was intended for, but it'll do for this little demo.
                    Thread.sleep(50);
                }
                catch (InterruptedException ignored) {
                }
            }

            // When the animation loop ends (user has clicked "stop"), let's blank
            // out the animation display:
            Graphics2D g = canvas.createGraphics();
            g.setColor(Color.BLACK);
            g.fillRect(0,0,width,height);
            g.dispose();
            imagePanel.setImage(canvas);
        }
    }
}
