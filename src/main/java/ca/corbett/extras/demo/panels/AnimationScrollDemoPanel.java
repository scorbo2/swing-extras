package ca.corbett.extras.demo.panels;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.image.ImagePanel;
import ca.corbett.extras.image.ImagePanelConfig;
import ca.corbett.extras.image.ImageUtil;
import ca.corbett.extras.image.animation.ImageScroller;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.PanelField;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AnimationScrollDemoPanel extends PanelBuilder {
    private FormPanel formPanel;
    private final BufferedImage canvas;
    private final BufferedImage scrollImage;
    private AnimationThread worker;
    private static final int IMG_WIDTH = 360;
    private static final int IMG_HEIGHT = 120;
    private ComboField speedField;
    private ComboField bounceTypeField;
    private ComboField bounceMarginField;
    private ImagePanel imagePanel;

    public AnimationScrollDemoPanel() {
        formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.setBorderMargin(24);
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
        LabelField headerLabel = LabelField.createBoldHeaderLabel("ImageScroller demo", 20);
        headerLabel.getMargins().setBottom(24);
        headerLabel.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE));
        LookAndFeelManager.addChangeListener(
                e -> headerLabel.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE)));
        formPanel.add(headerLabel);

        List<String> options = new ArrayList<>(List.of("Very slow", "Slow", "Medium", "Fast", "Very fast"));
        speedField = new ComboField<>("Scroll speed:", options, 1, false);
        speedField.addValueChangedListener(field -> {
            updateSpeed();
        });
        formPanel.add(speedField);

        options = List.of("None", "Linear", "Quadratic", "Cubic");
        bounceTypeField = new ComboField<>("Bounce type:", options, 1, false);
        bounceTypeField.addValueChangedListener(field -> {
            updateBounceType();
        });
        formPanel.add(bounceTypeField);

        options = List.of("Small", "Medium", "Large");
        bounceMarginField = new ComboField<>("Bounce margin:", options, 1, false);
        bounceMarginField.addValueChangedListener(field -> {
            updateBounceMargin();
        });
        formPanel.add(bounceMarginField);

        PanelField panelField = new PanelField();
        JPanel panel = panelField.getPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));

        imagePanel = new ImagePanel(ImagePanelConfig.createSimpleReadOnlyProperties());
        imagePanel.setPreferredSize(new Dimension(IMG_WIDTH, IMG_HEIGHT));
        imagePanel.setImage(canvas);
        panel.add(imagePanel);
        formPanel.add(panelField);

        panelField = new PanelField();
        panel = panelField.getPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));

        JButton button = new JButton("Scroll!");
        button.addActionListener(e -> go());
        panel.add(button);

        button = new JButton("Stop");
        button.addActionListener(e -> stop());
        panel.add(button);
        formPanel.add(panelField);

        formPanel.add(new LabelField("ImageScroller is better suited for fullscreen applications!"));
        formPanel.add(new LabelField("It can scroll an oversized image with configurable 'bounce' parameters."));

        formPanel.render();
        return formPanel;
    }

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

    private void updateSpeed() {
        if (worker != null) {
            ImageScroller.ScrollSpeed speed = ImageScroller.ScrollSpeed.MEDIUM;
            switch (speedField.getSelectedIndex()) {
                case 0: speed = ImageScroller.ScrollSpeed.VERY_SLOW; break;
                case 1: speed = ImageScroller.ScrollSpeed.SLOW; break;
                case 2: speed = ImageScroller.ScrollSpeed.MEDIUM; break;
                case 3: speed = ImageScroller.ScrollSpeed.FAST; break;
                case 4: speed = ImageScroller.ScrollSpeed.VERY_FAST; break;
            }
            worker.getImageScroller().setScrollSpeed(speed);
        }
    }

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

    private void stop() {
        if (worker != null) {
            worker.stop();
        }
    }

    private static class AnimationThread implements Runnable {

        private volatile boolean isRunning = false;
        private final int width;
        private final int height;
        private final BufferedImage canvas;
        private final BufferedImage scrollImage;
        private final ImageScroller imageScroller;
        private final ImagePanel imagePanel;

        public AnimationThread(BufferedImage canvas, BufferedImage scrollImage, int w, int h, ImagePanel imagePanel) {
            this.width = w;
            this.height = h;
            this.canvas = canvas;
            this.scrollImage = scrollImage;
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
                Graphics2D g = canvas.createGraphics();
                imageScroller.renderFrame(g);
                g.dispose();
                imagePanel.setImage(canvas);
                try {
                    Thread.sleep(33);
                }
                catch (InterruptedException ignored) {
                }
            }

            Graphics2D g = canvas.createGraphics();
            g.setColor(Color.BLACK);
            g.fillRect(0,0,width,height);
            g.dispose();
            imagePanel.setImage(canvas);
        }
    }
}
