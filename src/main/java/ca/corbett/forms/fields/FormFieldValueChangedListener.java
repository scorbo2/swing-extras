package ca.corbett.forms.fields;

/**
 * Can be added to a FormField to receive notification when the FormField's value
 * has changed.
 *
 * @author scorbo2
 * @since swing-extras 2.4
 */
public interface FormFieldValueChangedListener {

    /**
     * The value in the given FormField has been modified. The field can be interrogated for its new value.
     */
    void formFieldValueChanged(FormField field);
}
