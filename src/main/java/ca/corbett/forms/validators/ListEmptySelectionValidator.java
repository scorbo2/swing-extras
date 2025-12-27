package ca.corbett.forms.validators;

import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.ListField;
import ca.corbett.forms.fields.ListSubsetField;

/**
 * A field validator that works with both ListField and ListSubsetField to ensure that
 * at least one item is selected.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.6
 */
public class ListEmptySelectionValidator implements FieldValidator<FormField> {

    public static final String MESSAGE = "At least one item must be selected.";

    @Override
    public ValidationResult validate(FormField fieldToValidate) {
        // If the field type isn't a list field, just pass validation:
        if (!(fieldToValidate instanceof ListField) && !(fieldToValidate instanceof ListSubsetField)) {
            return ValidationResult.valid();
        }

        // Validate ListField:
        if (fieldToValidate instanceof ListField) {
            ListField<?> listField = (ListField<?>) fieldToValidate;
            if (listField.getSelectedIndexes().length == 0) {
                return ValidationResult.invalid(MESSAGE);
            }
        }

        // Validate ListSubsetField:
        else if (fieldToValidate instanceof ListSubsetField) {
            ListSubsetField<?> listSubsetField = (ListSubsetField<?>) fieldToValidate;
            if (listSubsetField.getSelectedIndexes().length == 0) {
                return ValidationResult.invalid(MESSAGE);
            }
        }

        return ValidationResult.valid();
    }
}
