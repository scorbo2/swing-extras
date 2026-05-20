package ca.corbett.forms.validators;

import ca.corbett.forms.fields.FileField;

import java.io.File;

/**
 * A simple validator that can be attached to any FileField to ensure that the selected file is not a directory.
 * The validator respects the field's allowBlankValues property, so blank values are validated as expected.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class FileMustNotBeDirectoryValidator implements FieldValidator<FileField> {
    @Override
    public ValidationResult validate(FileField fieldToValidate) {
        // Blank values may be permissible:
        boolean allowBlank = fieldToValidate.isAllowBlankValues();
        if (fieldToValidate.getFile() == null) {
            return allowBlank
                    ? ValidationResult.valid()
                    : ValidationResult.invalid("Value cannot be blank.");
        }

        File file = fieldToValidate.getFile();
        if (file.exists() && file.isDirectory()) {
            return ValidationResult.invalid("Must not select a directory.");
        }
        return ValidationResult.valid();
    }
}
