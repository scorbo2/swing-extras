package ca.corbett.forms.fields;

import javax.swing.JCheckBox;

/**
 * A FormField to wrap a JCheckBox.
 * <p>
 * A note about validation: checkboxes don't generally "count" when a FormPanel validates itself.
 * That is, they won't show a little checkbox label to indicate that the selected value is "correct".
 * However, if you invoked addFieldValidator(), then the field will automatically be included
 * in any calls to formPanel.isFormValid().
 * </p>
 *
 * @author scorbo2
 * @since 2019-11-27
 */
public final class CheckBoxField extends FormField {

    public CheckBoxField(String labelText, boolean isChecked) {
        fieldLabel.setFont(DEFAULT_FONT);
        fieldComponent = new JCheckBox(labelText, isChecked);
        fieldComponent.setFont(DEFAULT_FONT);
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

    public boolean isChecked() {
        return ((JCheckBox)fieldComponent).isSelected();
    }

    public CheckBoxField setChecked(boolean checked) {
        ((JCheckBox)fieldComponent).setSelected(checked);
        return this;
    }
}
