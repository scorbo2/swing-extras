package ca.corbett.extras.image;

import ca.corbett.extras.gradient.GradientColorField;
import ca.corbett.extras.gradient.GradientConfig;
import ca.corbett.extras.gradient.GradientUtil;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.CheckBoxField;
import ca.corbett.forms.fields.FontField;
import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.NumberField;
import ca.corbett.forms.fields.ValueChangedListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Color;

/**
 * A compound (multi-component) FormField for grouping all of the fields required
 * to configure a LogoGenerator request.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.4 (replacement for LogoConfigPanel)
 */
public class LogoFormField extends FormField {

    private final JPanel wrapperPanel;
    private final FormPanel formPanel;
    private String labelText;
    private GradientColorField bgColorField;
    private GradientColorField borderColorField;
    private GradientColorField textColorField;
    private FontField fontField;
    private NumberField borderWidthField;
    private NumberField imageWidthField;
    private NumberField imageHeightField;
    private NumberField yTweakField;
    private CheckBoxField fontSizeOverrideField;
    private boolean useTitleBorder;
    private boolean shouldExpand = true;

    public LogoFormField(String label) {
        labelText = label;
        wrapperPanel = new JPanel(new BorderLayout());
        formPanel = createFormPanel();
        wrapperPanel.add(formPanel, BorderLayout.CENTER);
        wrapperPanel.setBorder(BorderFactory.createTitledBorder(label));
        useTitleBorder = true;
        fieldComponent = wrapperPanel;
    }

    @Override
    public boolean isMultiLine() {
        return true;
    }

    public LogoFormField setShouldExpand(boolean should) {
        shouldExpand = should;
        return this;
    }

    @Override
    public boolean shouldExpand() {
        return shouldExpand;
    }

    @Override
    public boolean hasValidationLabel() {
        return false;
    }

    @Override
    public boolean validate() {
        return formPanel.isFormValid();
    }

    /**
     * Decides whether to use a titled border around this field component (the default), or
     * to use a traditional FormField field label instead.
     */
    public LogoFormField setUseTitleBorder(boolean use) {
        if (use == useTitleBorder) {
            return this; // ignore no-op requests
        }
        if (use) {
            wrapperPanel.setBorder(BorderFactory.createTitledBorder(labelText));
            fieldLabel.setText("");
        }
        else {
            wrapperPanel.setBorder(null);
            fieldLabel.setText(labelText);
        }
        useTitleBorder = use;
        return this;
    }

    public boolean isUseTitleBorder() {
        return useTitleBorder;
    }

    /**
     * Returns the text that is either showing in the field label OR in the
     * field's title border, depending on the value of useTitleBorder.
     */
    public String getFieldLabelText() {
        return labelText;
    }

    public LogoFormField setFieldLabelText(String text) {
        labelText = text;
        if (useTitleBorder && (wrapperPanel.getBorder() instanceof TitledBorder)) {
            ((TitledBorder)wrapperPanel.getBorder()).setTitle(labelText);
        }
        else if (! useTitleBorder) {
            fieldLabel.setText(labelText);
        }
        return this;
    }

    //TODO all property getters and setters

    private FormPanel createFormPanel() {
        FormPanel formPanel = new FormPanel(Alignment.TOP_LEFT);

        // Arbitrary defaults:
        final Color DEFAULT_BG = Color.BLACK;
        final Color DEFAULT_FG = Color.WHITE;
        final GradientConfig DEFAULT_GRADIENT = new GradientConfig();
        DEFAULT_GRADIENT.setColor1(Color.BLACK);
        DEFAULT_GRADIENT.setColor2(Color.WHITE);
        DEFAULT_GRADIENT.setGradientType(GradientUtil.GradientType.VERTICAL_STRIPE);

        final ValueChangedListener listener = field -> fireValueChangedEvent();

        bgColorField = new GradientColorField("Background:", DEFAULT_BG, DEFAULT_GRADIENT, true);
        bgColorField.addValueChangedListener(listener);
        formPanel.add(bgColorField);

        borderColorField = new GradientColorField("Border color:", DEFAULT_FG, DEFAULT_GRADIENT, true);
        borderColorField.addValueChangedListener(listener);
        formPanel.add(borderColorField);

        textColorField = new GradientColorField("Text color:", DEFAULT_FG, DEFAULT_GRADIENT, true);
        textColorField.addValueChangedListener(listener);
        formPanel.add(textColorField);

        fontField = new FontField("Font:", getDefaultFont().deriveFont(24f));
        fontField.addValueChangedListener(listener);
        formPanel.add(fontField);

        fontSizeOverrideField = new CheckBoxField("Auto-scale font to fit image", true);
        fontSizeOverrideField.addValueChangedListener(listener);
        formPanel.add(fontSizeOverrideField);

        borderWidthField = new NumberField("Border width:", 1, 0, 20, 1);
        borderWidthField.addValueChangedListener(listener);
        formPanel.add(borderWidthField);

        imageWidthField = new NumberField("Image width:", 400, 10, 10000, 10);
        imageWidthField.addValueChangedListener(listener);
        formPanel.add(imageWidthField);

        imageHeightField = new NumberField("Image height:", 200, 10, 10000, 10);
        imageHeightField.addValueChangedListener(listener);
        formPanel.add(imageHeightField);

        yTweakField = new NumberField("Y Tweak:", 0, -500, 500, 1);
        yTweakField.addValueChangedListener(listener);
        formPanel.add(yTweakField);

        return formPanel;
    }
}
