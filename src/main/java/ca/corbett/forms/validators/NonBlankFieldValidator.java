package ca.corbett.forms.validators;

import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.LongTextField;
import ca.corbett.forms.fields.ShortTextField;

/**
 * A simple field validator for text fields that ensures that the field does not have a blank value.
 * Currently works with ShortTextField and LongTextField.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2019-11-23
 */
public class NonBlankFieldValidator implements FieldValidator<FormField> {

    public static final String MESSAGE = "Value cannot be blank";

    @Override
    public ValidationResult validate(FormField fieldToValidate) {
        // Make sure it's a field type that we recognize:
        if (!(fieldToValidate instanceof ShortTextField) &&
                !(fieldToValidate instanceof LongTextField)) {
            return ValidationResult.valid();
        }

        String currentStr = (fieldToValidate instanceof ShortTextField)
                ? ((ShortTextField)fieldToValidate).getText()
                : ((LongTextField)fieldToValidate).getText();
        if (currentStr.trim().isEmpty()) {
            return ValidationResult.invalid(MESSAGE);
        }

        return ValidationResult.valid();
    }
}
