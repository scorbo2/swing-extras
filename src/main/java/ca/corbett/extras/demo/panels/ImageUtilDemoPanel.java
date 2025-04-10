package ca.corbett.extras.demo.panels;

import ca.corbett.extras.Version;
import ca.corbett.extras.gradient.GradientConfig;
import ca.corbett.extras.gradient.GradientUtil;
import ca.corbett.extras.image.ImagePanel;
import ca.corbett.extras.image.ImagePanelConfig;
import ca.corbett.extras.image.LogoConfig;
import ca.corbett.extras.image.LogoConfigPanel;
import ca.corbett.extras.image.LogoGenerator;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.PanelField;
import ca.corbett.forms.fields.TextField;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class ImageUtilDemoPanel extends PanelBuilder implements ChangeListener {
    private LogoConfig logoConfig;
    private TextField textField;
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
        FormPanel controlPanel = new FormPanel(FormPanel.Alignment.TOP_LEFT);

        LabelField labelField = new LabelField("ImageUtil");
        labelField.setFont(labelField.getFieldLabelFont().deriveFont(Font.BOLD, 18f));
        labelField.setTopMargin(18);
        controlPanel.addFormField(labelField);

        labelField = new LabelField("<html>ImageUtil and the associated color<br>"
                + "gradient classes can generate images with<br>"
                + "a variety of options. Here are just a few!</html>");
        labelField.setFont(labelField.getFieldLabelFont().deriveFont(Font.PLAIN, 12f));
        controlPanel.addFormField(labelField);

        textField = new TextField("Text:", 20, 1, true);
        textField.setText(Version.NAME);
        textField.addValueChangedAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                regenerate();
            }
        });
        controlPanel.addFormField(textField);

        List<String> options = new ArrayList<>();
        options.add("Center");
        options.add("Best fit");
        options.add("Stretch");
        ComboField displayModeChooser = new ComboField("Display mode:", options, 0, false);
        displayModeChooser.setBottomMargin(24);
        displayModeChooser.addValueChangedAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
            }
        });
        controlPanel.addFormField(displayModeChooser);

        PanelField panelField = new PanelField();
        JPanel containerPanel = panelField.getPanel();
        containerPanel.setLayout(new BorderLayout());
        logoConfig = createDefaultLogoConfig();
        LogoConfigPanel configPanel = new LogoConfigPanel("Image options", logoConfig);
        configPanel.addChangeListener(this);
        containerPanel.add(configPanel, BorderLayout.NORTH);
        controlPanel.addFormField(panelField);

        controlPanel.render();
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
        imagePanel.setImage(LogoGenerator.generateImage(textField.getText().isBlank() ? Version.NAME : textField.getText(), logoConfig));
    }
}
