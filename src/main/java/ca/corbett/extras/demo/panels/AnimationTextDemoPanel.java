package ca.corbett.extras.demo.panels;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.image.ImagePanel;
import ca.corbett.extras.image.ImagePanelConfig;
import ca.corbett.extras.image.animation.AnimatedTextRenderer;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.FontField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.NumberField;
import ca.corbett.forms.fields.PanelField;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class AnimationTextDemoPanel extends PanelBuilder {
    private FormPanel formPanel;
    private final BufferedImage image;
    private AnimationThread worker;
    private static final int IMG_WIDTH = 360;
    private static final int IMG_HEIGHT = 180;
    private FontField fontField;
    private NumberField speedField;
    private ImagePanel imagePanel;

    public AnimationTextDemoPanel() {
        formPanel = new FormPanel(FormPanel.Alignment.TOP_LEFT);
        formPanel.setStandardLeftMargin(24);
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
        LabelField headerLabel = LabelField.createBoldHeaderLabel("AnimatedTextRenderer demo", 20);
        headerLabel.setBottomMargin(24);
        headerLabel.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE));
        LookAndFeelManager.addChangeListener(
                e -> headerLabel.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE)));
        formPanel.addFormField(headerLabel);

        fontField = new FontField("Text style:", new Font(Font.MONOSPACED, Font.PLAIN, 16), Color.GREEN, Color.BLACK);
        formPanel.addFormField(fontField);

        speedField = new NumberField("Chars/second:", 8, 1, 15, 1);
        formPanel.addFormField(speedField);

        PanelField panelField = new PanelField();
        JPanel panel = panelField.getPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));

        imagePanel = new ImagePanel(ImagePanelConfig.createSimpleReadOnlyProperties());
        imagePanel.setPreferredSize(new Dimension(IMG_WIDTH, IMG_HEIGHT));
        imagePanel.setImage(image);
        panel.add(imagePanel);
        formPanel.addFormField(panelField);

        panelField = new PanelField();
        panel = panelField.getPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));

        JButton button = new JButton("Restart animation");
        button.addActionListener(e -> go());
        panel.add(button);
        formPanel.addFormField(panelField);

        formPanel.render();
        return formPanel;
    }

    private void go() {
        if (worker != null) {
            worker.stop();
        }
        worker = new AnimationThread(IMG_WIDTH, IMG_HEIGHT, fontField.getSelectedFont(), fontField.getBgColor(),
                                     fontField.getTextColor(), (int)speedField.getCurrentValue(), imagePanel);
        new Thread(worker).start();
    }

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
                imagePanel.setImage(textRenderer.getBuffer());
                try {
                    Thread.sleep(100);
                }
                catch (InterruptedException ignored) {
                }
                if (textRenderer.isAnimationComplete()) {
                    isRunning = false;
                }
            }
        }
    }
}
