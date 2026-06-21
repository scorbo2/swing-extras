package ca.corbett.forms.validators;

import ca.corbett.forms.fields.FileField;

import java.io.File;

/**
 * A FieldValidator that ensures that the chosen file or directory can be read.
 * The validator respects the field's allowBlankValues property, so blank values are validated as expected.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2019-11-24
 */
public class FileMustBeReadableValidator implements FieldValidator<FileField> {

    @Override
    public ValidationResult validate(FileField fieldToValidate) {
        // Blank values may be permissible:
        boolean allowBlank = fieldToValidate.isAllowBlankValues();
        if (fieldToValidate.getFile() == null) {
            return allowBlank
                    ? ValidationResult.valid()
                    : ValidationResult.invalid("Value cannot be blank.");
        }

        File dir = fieldToValidate.getFile();
        if (!dir.canRead()) {
            return ValidationResult.invalid("Selected location must be readable.");
        }

        return ValidationResult.valid();
    }
}
