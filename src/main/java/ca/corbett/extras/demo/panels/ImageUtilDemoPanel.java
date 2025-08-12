package ca.corbett.extras.demo.panels;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.Version;
import ca.corbett.extras.gradient.GradientConfig;
import ca.corbett.extras.gradient.GradientUtil;
import ca.corbett.extras.image.ImagePanel;
import ca.corbett.extras.image.ImagePanelConfig;
import ca.corbett.extras.image.LogoConfig;
import ca.corbett.extras.image.LogoConfigPanel;
import ca.corbett.extras.image.LogoGenerator;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.FontField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.PanelField;
import ca.corbett.forms.fields.ShortTextField;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

public class ImageUtilDemoPanel extends PanelBuilder implements ChangeListener {
    private LogoConfig logoConfig;
    private ShortTextField textField;
    private ImagePanel imagePanel;
    private ImagePanelConfig imagePanelConfig;

    @Override
    public String getTitle() {
        return "ImageUtil";
    }

    @Override
    public JPanel build() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        FormPanel controlPanel = new FormPanel(Alignment.TOP_LEFT);
        controlPanel.setBorderMargin(12);

        final LabelField label = LabelField.createBoldHeaderLabel("ImageUtil", 20);
        label.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE));
        LookAndFeelManager.addChangeListener(
                e -> label.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE)));
        controlPanel.add(label);

        LabelField labelField = new LabelField("<html>ImageUtil and the associated color<br>"
                                            + "gradient classes can generate images with<br>"
                                            + "a variety of options. Here are just a few!</html>");
        labelField.setFont(FontField.DEFAULT_FONT.deriveFont(Font.PLAIN, 12f));
        controlPanel.add(labelField);

        textField = new ShortTextField("Text:", 20);
        textField.setText(Version.NAME);
        textField.addValueChangedListener(field -> {
            regenerate();
        });
        controlPanel.add(textField);

        List<String> options = new ArrayList<>();
        options.add("Center");
        options.add("Best fit");
        options.add("Stretch");
        ComboField<String> displayModeChooser = new ComboField<>("Display mode:", options, 0, false);
        displayModeChooser.getMargins().setBottom(24);
        displayModeChooser.addValueChangedListener(field -> {
            ImagePanelConfig.DisplayMode displayMode;
            switch (displayModeChooser.getSelectedIndex()) {
                case 2:
                    displayMode = ImagePanelConfig.DisplayMode.STRETCH;
                    break;
                case 1:
                    displayMode = ImagePanelConfig.DisplayMode.BEST_FIT;
                    break;
                default:
                    displayMode = ImagePanelConfig.DisplayMode.CENTER;
            }
            imagePanelConfig.setDisplayMode(displayMode);
            regenerate();
        });
        controlPanel.add(displayModeChooser);

        PanelField panelField = new PanelField();
        JPanel containerPanel = panelField.getPanel();
        containerPanel.setLayout(new BorderLayout());
        logoConfig = createDefaultLogoConfig();
        LogoConfigPanel configPanel = new LogoConfigPanel("Image options", logoConfig);
        configPanel.addChangeListener(this);
        containerPanel.add(configPanel, BorderLayout.NORTH);
        controlPanel.add(panelField);

        leftPanel.add(controlPanel, BorderLayout.CENTER);
        panel.add(leftPanel, BorderLayout.WEST);

        imagePanelConfig = ImagePanelConfig.createSimpleReadOnlyProperties();
        imagePanelConfig.setDisplayMode(ImagePanelConfig.DisplayMode.CENTER);
        imagePanel = new ImagePanel(imagePanelConfig);
        panel.add(imagePanel, BorderLayout.CENTER);

        regenerate();

        return panel;
    }

    private LogoConfig createDefaultLogoConfig() {
        LogoConfig logoConfig = new LogoConfig("Image options");
        logoConfig.setAutoSize(true);
        logoConfig.setLogoWidth(400);
        logoConfig.setLogoHeight(400);
        GradientConfig gradient = new GradientConfig();
        gradient.setColor1(Color.BLUE);
        gradient.setColor2(Color.BLACK);
        gradient.setGradientType(GradientUtil.GradientType.DIAGONAL2);
        logoConfig.setBgGradient(gradient);
        logoConfig.setBgColorType(LogoConfig.ColorType.GRADIENT);
        GradientConfig gradient2 = new GradientConfig(gradient);
        gradient2.setColor1(Color.WHITE);
        gradient2.setColor2(Color.BLUE);
        logoConfig.setTextGradient(gradient2);
        logoConfig.setTextColorType(LogoConfig.ColorType.GRADIENT);
        return logoConfig;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        regenerate();
    }

    private void regenerate() {
        imagePanel.applyProperties(imagePanelConfig);
        imagePanel.setImage(
                LogoGenerator.generateImage(textField.getText().isBlank() ? Version.NAME : textField.getText(),
                                            logoConfig));
    }
}
