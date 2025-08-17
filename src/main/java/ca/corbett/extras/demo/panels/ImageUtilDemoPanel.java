package ca.corbett.extras.demo.panels;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.Version;
import ca.corbett.extras.gradient.Gradient;
import ca.corbett.extras.gradient.GradientType;
import ca.corbett.extras.image.ImagePanel;
import ca.corbett.extras.image.ImagePanelConfig;
import ca.corbett.extras.image.LogoFormField;
import ca.corbett.extras.image.LogoGenerator;
import ca.corbett.extras.image.LogoProperty;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.FontField;
import ca.corbett.forms.fields.LabelField;
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
    private ShortTextField textField;
    private ImagePanel imagePanel;
    private ImagePanelConfig imagePanelConfig;
    private LogoFormField logoFormField;
    private LogoProperty logo;

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
        labelField.setFont(FontField.getDefaultFont().deriveFont(Font.PLAIN, 12f));
        controlPanel.add(labelField);

        textField = new ShortTextField("Text:", 14);
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

        logo = createDefaultLogo();
        logoFormField = (LogoFormField)logo.generateFormField(controlPanel);
        logoFormField.setFieldLabelText("Image options");
        logoFormField.addValueChangedListener(e -> regenerate());
        logoFormField.setShouldExpand(false);
        controlPanel.add(logoFormField);

        leftPanel.add(controlPanel, BorderLayout.CENTER);
        panel.add(leftPanel, BorderLayout.WEST);

        imagePanelConfig = ImagePanelConfig.createSimpleReadOnlyProperties();
        imagePanelConfig.setDisplayMode(ImagePanelConfig.DisplayMode.CENTER);
        imagePanel = new ImagePanel(imagePanelConfig);
        panel.add(imagePanel, BorderLayout.CENTER);

        regenerate();

        return panel;
    }

    private LogoProperty createDefaultLogo() {
        LogoProperty logoProperty = new LogoProperty("logoPropertyDemoApp", "Image options");
        logoProperty.setAutoSize(true);
        logoProperty.setLogoWidth(400);
        logoProperty.setLogoHeight(400);
        Gradient gradient = new Gradient(GradientType.DIAGONAL2, Color.BLUE, Color.BLACK);
        logoProperty.setBgGradient(gradient);
        logoProperty.setBgColorType(LogoProperty.ColorType.GRADIENT);
        Gradient gradient2 = new Gradient(GradientType.DIAGONAL2, Color.WHITE, Color.BLUE);
        logoProperty.setTextGradient(gradient2);
        logoProperty.setTextColorType(LogoProperty.ColorType.GRADIENT);
        return logoProperty;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        regenerate();
    }

    private void regenerate() {
        String text = textField.getText().isBlank() ? Version.NAME : textField.getText();
        logo.loadFromFormField(logoFormField);
        imagePanel.applyProperties(imagePanelConfig);
        imagePanel.setImage(LogoGenerator.generateImage(text, logo));
    }
}
