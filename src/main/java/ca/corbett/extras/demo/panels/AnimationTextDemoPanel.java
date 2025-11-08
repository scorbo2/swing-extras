package ca.corbett.extras.demo.panels;

import ca.corbett.extras.image.ImagePanel;
import ca.corbett.extras.image.ImagePanelConfig;
import ca.corbett.extras.image.animation.AnimatedTextRenderer;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.FontField;
import ca.corbett.forms.fields.NumberField;
import ca.corbett.forms.fields.PanelField;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class AnimationTextDemoPanel extends PanelBuilder {
    private final BufferedImage image;
    private AnimationThread worker;
    private static final int IMG_WIDTH = 360;
    private static final int IMG_HEIGHT = 180;
    private FontField fontField;
    private NumberField speedField;
    private ImagePanel imagePanel;

    public AnimationTextDemoPanel() {
        image = new BufferedImage(IMG_WIDTH,IMG_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0,0,IMG_WIDTH,IMG_HEIGHT);
    }

    @Override
    public String getTitle() {
        return "Animation: text";
    }

    @Override
    public JPanel build() {
        FormPanel formPanel = buildFormPanel("AnimatedTextRenderer demo");

        // Let's add a simple FontField for picking font (with color/bg options as well):
        fontField = new FontField("Text style:", new Font(Font.MONOSPACED, Font.PLAIN, 16), Color.GREEN, Color.BLACK);
        formPanel.add(fontField);

        // A simple NumberField will allow choosing the animation speed:
        speedField = new NumberField("Chars/second:", 8, 1, 15, 1);
        formPanel.add(speedField);

        // We can use PanelField to wrap an ImagePanel for displaying the animation:
        PanelField panelField = new PanelField(new FlowLayout(FlowLayout.LEFT));
        imagePanel = new ImagePanel(ImagePanelConfig.createSimpleReadOnlyProperties());
        imagePanel.setPreferredSize(new Dimension(IMG_WIDTH, IMG_HEIGHT));
        imagePanel.setImage(image);
        panelField.getPanel().add(imagePanel);
        formPanel.add(panelField);

        // And another PanelField can hold the button for restarting the animation:
        panelField = new PanelField(new FlowLayout(FlowLayout.LEFT));
        JButton button = new JButton("Restart animation");
        button.addActionListener(e -> go());
        panelField.getPanel().add(button);
        formPanel.add(panelField);

        return formPanel;
    }

    /**
     * Invoked when the user clicks the animation button. Will start the AnimationThread (and stop
     * the old one if one was running already).
     */
    private void go() {
        if (worker != null) {
            worker.stop();
        }
        worker = new AnimationThread(IMG_WIDTH, IMG_HEIGHT, fontField.getSelectedFont(), fontField.getBgColor(),
                                     fontField.getTextColor(), (int)speedField.getCurrentValue(), imagePanel);
        new Thread(worker).start();
    }

    /**
     * A very simple worker thread to drive our AnimatedTextRenderer.
     * We can simply invoke updateTextAnimation() at regular intervals, and the
     * AnimatedTextRenderer class is smart enough to figure out how many characters
     * to draw based on elapsed time since it was started (this maps to our
     * characters per second input parameter).
     *
     * @author <a href="https://github.com/scorbo2">scorbo2</a>
     */
    private static class AnimationThread implements Runnable {

        private static final String TEXT = "This is a demo of the animated text renderer. "
                + "It types out text at a configurable speed!"
                + " It even handles line wrap automatically.";
        private volatile boolean isRunning = false;
        private final AnimatedTextRenderer textRenderer;
        private final ImagePanel imagePanel;

        public AnimationThread(int w, int h, Font font, Color bgColor, Color fgColor, int charsPerSecond, ImagePanel imagePanel) {
            textRenderer = new AnimatedTextRenderer(w,h,TEXT,charsPerSecond,font,fgColor,bgColor);
            this.imagePanel = imagePanel;
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
                textRenderer.updateTextAnimation();

                // Marshal the UI update to the Swing Event Dispatch Thread - important!
                //   Don't try to update Swing UI components from a worker thread directly!
                SwingUtilities.invokeLater(() -> imagePanel.setImage(textRenderer.getBuffer()));

                try {
                    // 10fps of animation won't overload the cpu
                    // Note that AnimatedTextRenderer is smart enough to figure out characters per second
                    // regardless of how frequently we invoke it! We don't have to do the math here
                    // and run a traditional animation loop with an fps counter. We can just update
                    // at whatever interval, and the text will output at the selected speed automagically.
                    Thread.sleep(100);
                }
                catch (InterruptedException ignored) {
                }

                // Check for animation completion so we can terminate this worker thread.
                if (textRenderer.isAnimationComplete()) {
                    isRunning = false;
                }
            }
        }
    }
}
