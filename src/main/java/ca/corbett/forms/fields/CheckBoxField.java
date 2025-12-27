package ca.corbett.forms.fields;

import javax.swing.JCheckBox;

/**
 * A FormField to wrap a JCheckBox.
 * <p>
 * A note about validation: checkboxes don't generally "count" when a FormPanel validates itself.
 * That is, they won't show a validation label to indicate that the selected value is "correct".
 * You can change this behavior by using addFieldValidator() - if any FieldValidators are present
 * on this field, then it will be included when the FormPanel is validated.
 * <p>
 * <b>Getting access to the underlying JCheckBox</b> - if you need access to the underlying
 * JCheckBox (for example, for styling purposes, changing the font, etc), you can
 * use getFieldComponent() and cast the return to JCheckBox.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2019-11-27
 */
public final class CheckBoxField extends FormField {

    public CheckBoxField(String labelText, boolean isChecked) {
        fieldComponent = new JCheckBox(labelText, isChecked);
        fieldComponent.setFont(defaultFont);
        ((JCheckBox)fieldComponent).addItemListener(e -> fireValueChangedEvent());
    }

    /**
     * Overridden here as we generally don't want to show a validation label on a checkbox.
     * Will return true only if one or more FieldValidators have been explicitly assigned.
     */
    @Override
    public boolean hasValidationLabel() {
        return !fieldValidators.isEmpty();
    }

    /**
     * Exposes the text from the underlying JCheckBox.
     */
    public String getCheckBoxText() {
        return ((JCheckBox)fieldComponent).getText();
    }

    /**
     * Allows updating the label text in the underlying JCheckBox.
     */
    public CheckBoxField setCheckBoxText(String text) {
        ((JCheckBox)fieldComponent).setText(text);
        return this;
    }

    public boolean isChecked() {
        return ((JCheckBox)fieldComponent).isSelected();
    }

    public CheckBoxField setChecked(boolean checked) {
        ((JCheckBox)fieldComponent).setSelected(checked);
        return this;
    }
}
