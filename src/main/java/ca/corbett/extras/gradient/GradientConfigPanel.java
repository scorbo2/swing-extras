package ca.corbett.extras.gradient;

import ca.corbett.extras.config.ConfigPanel;
import ca.corbett.extras.properties.Properties;
import ca.corbett.extras.image.ImagePanel;
import ca.corbett.extras.image.ImagePanelConfig;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.PanelField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JButton;

/**
 *
 * @author scorbo2
 */
public final class GradientConfigPanel extends ConfigPanel<GradientConfig> {

    private static final int PREVIEW_WIDTH = 500;
    private static final int PREVIEW_HEIGHT = 500;

    private ImagePanel previewPanel;
    private GradientColorField color1Field;
    private GradientColorField color2Field;
    private ComboField gradientTypeCombo;

    public GradientConfigPanel(String title) {
        this(title, new GradientConfig());
    }

    public GradientConfigPanel(String title, GradientConfig config) {
        super(title);
        this.modelObject = config == null ? new GradientConfig() : config;
        initComponents();
    }

    public GradientConfig getSelectedValue() {
        return new GradientConfig(modelObject);
    }

    /**
     * Saves settings to the specified Properties instance, using the specified
     * parameter name prefix (may be null or empty). This will clear the isModified flag.
     *
     * @param props A Properties instance to which to save all settings.
     * @param prefix An optional property name prefix to use with prop names.
     */
    @Override
    public void save(Properties props, String prefix) {
        modelObject.saveToProps(props, prefix);
        isModified = false;
    }

    /**
     * Loads settings from the specified Properties instance, using the specified
     * parameter name prefix (may be null or empty). This will set all UI fields to reflect
     * whatever is loaded from the Properties instance, clear the isModified flag, and
     * will overwrite whatever settings were contained previously.
     *
     * @param props A Properties instance from which to load all settings.
     * @param prefix An optional property name prefix to use with prop names.
     */
    @Override
    public void load(Properties props, String prefix) {
        modelObject.loadFromProps(props, prefix);
        load(modelObject);
        isModified = false;
    }

    /**
     * Loads settings from the given model object. Clears the isModified flag.
     *
     * @param obj The model object from which to load.
     */
    @Override
    public void load(GradientConfig obj) {
        modelObject = new GradientConfig(obj);
        if (color1Field != null) {
            color1Field.setColor(obj.getColor1());
            color2Field.setColor(obj.getColor2());
            gradientTypeCombo.setSelectedItem(obj.getGradientType().toString());
            updatePreview();
        }
        isModified = false;
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        FormPanel formPanel = new FormPanel(FormPanel.Alignment.TOP_CENTER);

        LabelField labelField = new LabelField("Gradient configuration");
        labelField.setFont(labelField.getFieldLabelFont().deriveFont(Font.BOLD, 14f));
        formPanel.addFormField(labelField);

        List<String> options = new ArrayList<>();
        for (GradientUtil.GradientType gradientType : GradientUtil.GradientType.values()) {
            options.add(gradientType.toString());
        }
        gradientTypeCombo = new ComboField("Type:", options, 0, false);
        gradientTypeCombo.addValueChangedAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modelObject.setGradientType(GradientUtil.GradientType.fromLabel(gradientTypeCombo.getSelectedItem()));
                updatePreview();
            }

        });
        formPanel.addFormField(gradientTypeCombo);

        color1Field = new GradientColorField("Color 1:", modelObject.getColor1());
        color1Field.addValueChangedAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modelObject.setColor1(color1Field.getColor());
                updatePreview();
            }

        });
        formPanel.addFormField(color1Field);

        color2Field = new GradientColorField("Color 2:", modelObject.getColor2());
        color2Field.addValueChangedAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modelObject.setColor2(color2Field.getColor());
                updatePreview();
            }

        });
        formPanel.addFormField(color2Field);

        PanelField panelField = new PanelField();
        panelField.getPanel().setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton button = new JButton("Swap colours");
        button.setPreferredSize(new Dimension(140, 23));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object obj1 = color1Field.getSelectedValue();
                color1Field.setSelectedValue(color2Field.getSelectedValue());
                modelObject.setColor1(color2Field.getColor());
                color2Field.setSelectedValue(obj1);
                modelObject.setColor2((Color)obj1);
                updatePreview();
            }

        });
        panelField.getPanel().add(button);
        formPanel.addFormField(panelField);

        formPanel.render();
        add(formPanel, BorderLayout.WEST);

        ImagePanelConfig iPanelConf = ImagePanelConfig.createSimpleReadOnlyProperties();
        iPanelConf.setDisplayMode(ImagePanelConfig.DisplayMode.STRETCH);
        previewPanel = new ImagePanel(GradientUtil.createGradientImage(modelObject, PREVIEW_WIDTH, PREVIEW_HEIGHT), iPanelConf);
        add(previewPanel, BorderLayout.CENTER);
    }

    private void updatePreview() {
        isModified = true;
        notifyChangeListeners();
        previewPanel.setImage(GradientUtil.createGradientImage(modelObject, PREVIEW_WIDTH, PREVIEW_HEIGHT));
    }

}
