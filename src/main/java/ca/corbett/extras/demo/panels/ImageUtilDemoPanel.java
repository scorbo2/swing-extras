package ca.corbett.extras.demo.panels;

import ca.corbett.extras.Version;
import ca.corbett.extras.gradient.Gradient;
import ca.corbett.extras.gradient.GradientType;
import ca.corbett.extras.image.ImagePanel;
import ca.corbett.extras.image.ImagePanelConfig;
import ca.corbett.extras.image.LogoFormField;
import ca.corbett.extras.image.LogoGenerator;
import ca.corbett.extras.image.LogoProperty;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.FontField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.ShortTextField;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/**
 * ImageUtil is a utility class that offers a lot of handy functionality around loading, saving, and
 * manipulating images. It's tough to demo ALL of its capabilities, but this demo panel will show
 * off at least some of what it can do.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class ImageUtilDemoPanel extends PanelBuilder {
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
        FormPanel controlPanel = buildFormPanel("ImageUtil", 12);

        LabelField labelField = new LabelField("<html>ImageUtil and the associated color<br>"
                                            + "gradient classes can generate images with<br>"
                                            + "a variety of options. Here are just a few!</html>");
        labelField.setFont(FontField.getDefaultFont().deriveFont(Font.PLAIN, 12f));
        controlPanel.add(labelField);

        // Let's have a short text field for entering example text:
        textField = new ShortTextField("Text:", 14);
        textField.setText(Version.NAME);
        textField.addValueChangedListener(field -> regenerate());
        controlPanel.add(textField);

        // Let's add a combo field for picking the display mode for our image:
        controlPanel.add(buildDisplayModeCombo());

        // The text config options are fairly complex. Fortunately, we don't
        // have to build out the UI for it here, because the LogoFormField
        // class has a method to build and return the FormField for us:
        logo = createDefaultLogo();
        logoFormField = (LogoFormField)logo.generateFormField(controlPanel);
        logoFormField.setFieldLabelText("Image options");
        logoFormField.addValueChangedListener(e -> regenerate());
        logoFormField.setShouldExpand(false);
        controlPanel.add(logoFormField);

        leftPanel.add(controlPanel, BorderLayout.CENTER);
        panel.add(leftPanel, BorderLayout.WEST);

        imagePanelConfig = ImagePanelConfig.createSimpleReadOnlyProperties();
        imagePanelConfig.setDisplayMode(ImagePanelConfig.DisplayMode.BEST_FIT);
        imagePanel = new ImagePanel(imagePanelConfig);
        panel.add(imagePanel, BorderLayout.CENTER);

        regenerate();

        return panel;
    }

    /**
     * Invoked internally to build and return a ComboField suitable for picking
     * a custom display mode for our generated image.
     */
    private ComboField<String> buildDisplayModeCombo() {
        List<String> options = new ArrayList<>();
        options.add("Center");
        options.add("Best fit");
        options.add("Stretch");
        ComboField<String> displayModeCombo = new ComboField<>("Display mode:", options, 1, false);
        displayModeCombo.getMargins().setBottom(24);
        displayModeCombo.addValueChangedListener(field -> {
            ImagePanelConfig.DisplayMode displayMode = switch (displayModeCombo.getSelectedIndex()) {
                case 2 -> ImagePanelConfig.DisplayMode.STRETCH;
                case 1 -> ImagePanelConfig.DisplayMode.BEST_FIT;
                default -> ImagePanelConfig.DisplayMode.CENTER;
            };
            imagePanelConfig.setDisplayMode(displayMode);
            regenerate();
        });
        return displayModeCombo;
    }

    /**
     * Creates an arbitrary default LogoProperty for initial display.
     * The user can tinker with the settings to modify it.
     */
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

    /**
     * Invoked internally to regenerate the image when the text changes,
     * or when any of our display properties change.
     */
    private void regenerate() {
        String text = textField.getText().isBlank() ? Version.NAME : textField.getText();
        logo.loadFromFormField(logoFormField);
        imagePanel.applyProperties(imagePanelConfig);
        imagePanel.setImage(LogoGenerator.generateImage(text, logo));
    }
}
