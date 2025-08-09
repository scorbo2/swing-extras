package ca.corbett.forms.validators;

import ca.corbett.forms.fields.TextField;

/**
 * A simple field validator for TextField that ensures that the field does not have a blank value.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2019-11-23
 */
public class NonBlankFieldValidator implements FieldValidator<TextField> {

    @Override
    public ValidationResult validate(TextField fieldToValidate) {
        String currentStr = fieldToValidate.getText();
        if (currentStr.trim().isEmpty()) {
            return ValidationResult.invalid("Value cannot be blank.");
        }
        return ValidationResult.valid();
    }
}
