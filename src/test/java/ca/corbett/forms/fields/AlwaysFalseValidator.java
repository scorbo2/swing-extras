package ca.corbett.forms.fields;

import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.ValidationResult;

/**
 * A very simple test validator that just always returns false.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class AlwaysFalseValidator implements FieldValidator<FormField> {

    public static String MESSAGE = "invalid";

    @Override
    public ValidationResult validate(FormField fieldToValidate) {
        return ValidationResult.invalid(MESSAGE);
    }
}
