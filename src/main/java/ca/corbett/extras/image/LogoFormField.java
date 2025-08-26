package ca.corbett.extras.image;

import ca.corbett.extras.gradient.ColorSelectionType;
import ca.corbett.extras.gradient.Gradient;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.CheckBoxField;
import ca.corbett.forms.fields.ColorField;
import ca.corbett.forms.fields.FontField;
import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.NumberField;
import ca.corbett.forms.fields.ValueChangedListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

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
    private ColorField bgColorField;
    private ColorField borderColorField;
    private ColorField textColorField;
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

    /**
     * This field itself typically will not show a validation label, as we delegate field validation
     * to our embedded form fields. But, in keeping with the swing-forms general contract, you can
     * still assign FieldValidators to instances of this field if you wish.
     */
    @Override
    public boolean hasValidationLabel() {
        return !fieldValidators.isEmpty();
    }

    @Override
    public boolean validate() {
        // ask the parent to validate also, in case we have any FieldValidators assigned here
        return super.validate() && formPanel.isFormValid();
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

    public Object getBackgroundColor() {
        return bgColorField.getSelectedValue();
    }

    public LogoFormField setBackgroundColor(Object object) {
        if (bgColorField.getSelectedValue().equals(object)) {
            return this; // reject no-op requests
        }
        if (object == null) {
            return this; // reject null
        }
        bgColorField.setSelectedValue(object);
        return this;
    }

    public Object getBorderColor() {
        return borderColorField.getSelectedValue();
    }

    public LogoFormField setBorderColor(Object object) {
        if (borderColorField.getSelectedValue().equals(object)) {
            return this; // reject no-op requests
        }
        if (object == null) {
            return this; // reject null
        }
        borderColorField.setSelectedValue(object);
        return this;
    }

    public Font getSelectedFont() {
        return fontField.getSelectedFont();
    }

    public LogoFormField setSelectedFont(Font font) {
        if (getSelectedFont().equals(font)) {
            return this; // reject no-op requests
        }
        if (font == null) {
            return this; // reject null
        }
        fontField.setSelectedFont(font);
        return this;
    }

    public Object getTextColor() {
        return textColorField.getSelectedValue();
    }

    public LogoFormField setTextColor(Object object) {
        if (textColorField.getSelectedValue().equals(object)) {
            return this; // reject no-op requests
        }
        if (object == null) {
            return this; // reject null
        }
        textColorField.setSelectedValue(object);
        return this;
    }

    public int getBorderWidth() {
        return (Integer)borderWidthField.getCurrentValue();
    }

    public LogoFormField setBorderWidth(int value) {
        if (getBorderWidth() == value) {
            return this; // reject no-op requests
        }
        borderWidthField.setCurrentValue(value);
        return this;
    }

    public int getImageWidth() {
        return (Integer)imageWidthField.getCurrentValue();
    }

    public LogoFormField setImageWidth(int value) {
        if (getImageWidth() == value) {
            return this; // reject no-op requests
        }
        imageWidthField.setCurrentValue(value);
        return this;
    }

    public int getImageHeight() {
        return (Integer)imageHeightField.getCurrentValue();
    }

    public LogoFormField setImageHeight(int value) {
        if (getImageHeight() == value) {
            return this; // reject no-op requests
        }
        imageHeightField.setCurrentValue(value);
        return this;
    }

    public int getYTweak() {
        return (Integer)yTweakField.getCurrentValue();
    }

    public LogoFormField setYTweak(int value) {
        if (getYTweak() == value) {
            return this; // reject no-op requests
        }
        yTweakField.setCurrentValue(value);
        return this;
    }

    public boolean isFontAutoScale() {
        return fontSizeOverrideField.isChecked();
    }

    public LogoFormField setFontAutoScale(boolean autoScale) {
        if (isFontAutoScale() == autoScale) {
            return this; // reject no-op requests
        }
        fontSizeOverrideField.setChecked(autoScale);
        return this;
    }

    private FormPanel createFormPanel() {
        FormPanel formPanel = new FormPanel(Alignment.TOP_LEFT);

        // Arbitrary defaults:
        final Color DEFAULT_BG = Color.BLACK;
        final Color DEFAULT_FG = Color.WHITE;
        final Gradient DEFAULT_GRADIENT = Gradient.createDefault();

        final ValueChangedListener listener = field -> fireValueChangedEvent();

        bgColorField = new ColorField("Background:", ColorSelectionType.EITHER).setGradient(DEFAULT_GRADIENT)
                                                                               .setColor(DEFAULT_BG);
        bgColorField.addValueChangedListener(listener);
        formPanel.add(bgColorField);

        borderColorField = new ColorField("Border color:", ColorSelectionType.EITHER).setGradient(DEFAULT_GRADIENT)
                                                                                     .setColor(DEFAULT_FG);
        borderColorField.addValueChangedListener(listener);
        formPanel.add(borderColorField);

        textColorField = new ColorField("Text color:", ColorSelectionType.EITHER).setGradient(DEFAULT_GRADIENT)
                                                                                 .setColor(DEFAULT_FG);
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
