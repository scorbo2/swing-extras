package ca.corbett.extras.testutils;

import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.ValidationResult;

/**
 * A very simple validator for testing purposes that just always returns false.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class AlwaysFalseValidator implements FieldValidator<FormField> {
    public static final String MESSAGE = "AlwaysFalseValidator always returns false.";

    @Override
    public ValidationResult validate(FormField fieldToValidate) {
        return ValidationResult.invalid(MESSAGE);
    }
}
