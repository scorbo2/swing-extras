package ca.corbett.extras.image;

import ca.corbett.extras.config.ConfigPanel;
import ca.corbett.extras.gradient.GradientColorField;
import ca.corbett.extras.gradient.GradientConfig;
import ca.corbett.extras.properties.Properties;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.FontField;
import ca.corbett.forms.fields.NumberField;

import javax.swing.AbstractAction;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides a ConfigPanel implementation for editing LogoGenerator config options.
 *
 * @author scorbo2
 * @since 2019-11-06
 */
public final class LogoConfigPanel extends ConfigPanel<LogoConfig> {

    private GradientColorField bgColorField;
    private GradientColorField borderColorField;
    private GradientColorField textColorField;
    private FontField fontField;
    private NumberField borderWidthField;
    private NumberField imageWidthField;
    private NumberField imageHeightField;
    private NumberField yTweakField;
    private ComboField fontSizeChooser;

    public LogoConfigPanel(String title) {
        this(title, new LogoConfig("Untitled"));
    }

    public LogoConfigPanel(String title, LogoConfig config) {
        super(title);
        if (config == null) {
            config = new LogoConfig("Untitled");
        }
        setModelObject(config);
        initComponents();
    }

    /**
     * We override this here to reject calling it with a null parameter. If you pass null
     * here, we create a default untitled LogoConfig and use that instead.
     *
     * @param obj The new LogoConfig to use.
     */
    @Override
    public void setModelObject(LogoConfig obj) {
        if (obj == null) {
            obj = new LogoConfig("Untitled");
        }
        super.setModelObject(obj);
    }

    @Override
    public void save(Properties props, String prefix) {
        modelObject.saveToProps(props, prefix);
    }

    @Override
    public void load(Properties props, String prefix) {
        modelObject.loadFromProps(props, prefix);
        load(modelObject);
    }

    @Override
    public void load(LogoConfig obj) {
        modelObject.setBgColor(obj.getBgColor());
        modelObject.setBgGradient(obj.getBgGradient());
        modelObject.setBgColorType(obj.getBgColorType());
        modelObject.setBorderColor(obj.getBorderColor());
        modelObject.setBorderGradient(obj.getBorderGradient());
        modelObject.setBorderColorType(obj.getBorderColorType());
        modelObject.setBorderWidth(obj.getBorderWidth());
        modelObject.setHasBorder(obj.hasBorder());
        modelObject.setFontPointSize(obj.getFont().getSize()); // set this BEFORE setAutoSize()
        modelObject.setAutoSize(obj.isAutoSize());
        modelObject.setFont(obj.getFont());
        modelObject.setTextColor(obj.getTextColor());
        modelObject.setTextGradient(obj.getTextGradient());
        modelObject.setTextColorType(obj.getTextColorType());
        modelObject.setLogoWidth(obj.getLogoWidth());
        modelObject.setLogoHeight(obj.getLogoHeight());
        modelObject.setYTweak(obj.getYTweak());
        modelObject.setName(obj.getName());

        // Don't try to update UI if we haven't initialized yet:
        if (bgColorField != null) {
            if (obj.getBgColorType() == LogoConfig.ColorType.SOLID) {
                bgColorField.setColor(obj.getBgColor());
            } else {
                bgColorField.setGradient(obj.getBgGradient());
            }
            if (obj.getBorderColorType() == LogoConfig.ColorType.SOLID) {
                borderColorField.setColor(obj.getBorderColor());
            } else {
                borderColorField.setGradient(obj.getBorderGradient());
            }
            if (obj.getTextColorType() == LogoConfig.ColorType.SOLID) {
                textColorField.setColor(obj.getTextColor());
            } else {
                textColorField.setGradient(obj.getTextGradient());
            }
            borderWidthField.setCurrentValue(obj.getBorderWidth());
            fontSizeChooser.setSelectedIndex(obj.isAutoSize() ? 0 : 1);
            fontField.setSelectedFont(modelObject.getFont());
            imageWidthField.setCurrentValue(obj.getLogoWidth());
            imageHeightField.setCurrentValue(obj.getLogoHeight());
            yTweakField.setCurrentValue(obj.getYTweak());
            notifyChangeListeners();
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        FormPanel formPanel = new FormPanel(FormPanel.Alignment.TOP_LEFT);

        bgColorField = new GradientColorField("Background:", modelObject.getBgColor(), modelObject.getBgGradient(), modelObject.getBgColorType() == LogoConfig.ColorType.SOLID);
        bgColorField.addValueChangedAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object selectedValue = bgColorField.getSelectedValue();
                if (selectedValue instanceof Color) {
                    modelObject.setBgColorType(LogoConfig.ColorType.SOLID);
                    modelObject.setBgColor((Color) selectedValue);
                } else {
                    modelObject.setBgColorType(LogoConfig.ColorType.GRADIENT);
                    modelObject.setBgGradient((GradientConfig) selectedValue);
                }
                notifyChangeListeners();
            }

        });
        formPanel.addFormField(bgColorField);

        borderColorField = new GradientColorField("Border color:", modelObject.getBorderColor(), modelObject.getBorderGradient(), modelObject.getBorderColorType() == LogoConfig.ColorType.SOLID);
        borderColorField.addValueChangedAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object selectedValue = borderColorField.getSelectedValue();
                if (selectedValue instanceof Color) {
                    modelObject.setBorderColorType(LogoConfig.ColorType.SOLID);
                    modelObject.setBorderColor((Color) selectedValue);
                } else {
                    modelObject.setBorderColorType(LogoConfig.ColorType.GRADIENT);
                    modelObject.setBorderGradient((GradientConfig) selectedValue);
                }
                notifyChangeListeners();
            }

        });
        formPanel.addFormField(borderColorField);

        textColorField = new GradientColorField("Text color:", modelObject.getTextColor(), modelObject.getTextGradient(), modelObject.getTextColorType() == LogoConfig.ColorType.SOLID);
        textColorField.addValueChangedAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object selectedValue = textColorField.getSelectedValue();
                if (selectedValue instanceof Color) {
                    modelObject.setTextColorType(LogoConfig.ColorType.SOLID);
                    modelObject.setTextColor((Color) selectedValue);
                } else {
                    modelObject.setTextColorType(LogoConfig.ColorType.GRADIENT);
                    modelObject.setTextGradient((GradientConfig) selectedValue);
                }
                notifyChangeListeners();
            }

        });
        formPanel.addFormField(textColorField);

        fontField = new FontField("Font:", modelObject.getFont());
        fontField.addValueChangedAction(fontFieldChangedAction);
        formPanel.addFormField(fontField);

        List<String> options = new ArrayList<>();
        options.add("Auto-scale to image");
        options.add("Use size from font chooser");
        fontSizeChooser = new ComboField("Font size:", options, 0, false);
        fontSizeChooser.setSelectedIndex(modelObject.isAutoSize() ? 0 : 1);
        fontSizeChooser.addValueChangedAction(fontFieldChangedAction);
        formPanel.addFormField(fontSizeChooser);

        borderWidthField = new NumberField("Border width:", 1, 0, 20, 1);
        borderWidthField.addValueChangedAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modelObject.setBorderWidth(borderWidthField.getCurrentValue().intValue());
                notifyChangeListeners();
            }

        });
        formPanel.addFormField(borderWidthField);

        imageWidthField = new NumberField("Image width:", modelObject.getLogoWidth(), 10, 10000, 10);
        imageWidthField.addValueChangedAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modelObject.setLogoWidth(imageWidthField.getCurrentValue().intValue());
                notifyChangeListeners();
            }

        });
        formPanel.addFormField(imageWidthField);

        imageHeightField = new NumberField("Image height:", modelObject.getLogoHeight(), 10, 10000, 10);
        imageHeightField.addValueChangedAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modelObject.setLogoHeight(imageHeightField.getCurrentValue().intValue());
                notifyChangeListeners();
            }

        });
        formPanel.addFormField(imageHeightField);

        yTweakField = new NumberField("Y Tweak:", 0, -500, 500, 1);
        yTweakField.addValueChangedAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modelObject.setYTweak(yTweakField.getCurrentValue().intValue());
                notifyChangeListeners();
            }

        });
        formPanel.addFormField(yTweakField);

        formPanel.render();
        add(formPanel, BorderLayout.CENTER);
    }

    private final AbstractAction fontFieldChangedAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Font font = fontField.getSelectedFont();
            int size = font.getSize();
            modelObject.setFont(font);
            modelObject.setFontPointSize(size);
            modelObject.setAutoSize(fontSizeChooser.getSelectedIndex() == 0);
            notifyChangeListeners();
        }
    };
}
