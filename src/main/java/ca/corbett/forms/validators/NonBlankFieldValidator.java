package ca.corbett.forms.validators;

import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.TextField;

/**
 * A simple field validator that ensures that the field does not have a blank value.
 *
 * @author scorbo2
 * @since 2019-11-23
 */
public class NonBlankFieldValidator extends FieldValidator<FormField> {

    public NonBlankFieldValidator(TextField formField) {
        super(formField);
    }

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        String currentStr = ((TextField)field).getText();
        if (currentStr.trim().isEmpty()) {
            result.setResult(false, "Value cannot be blank.");
        }
        return result;
    }
}
