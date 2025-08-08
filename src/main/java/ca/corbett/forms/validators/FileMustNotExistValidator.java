package ca.corbett.forms.validators;

import ca.corbett.forms.fields.FileField;

import java.io.File;

/**
 * The opposite of FileMustExistValidator, this one ensures that the selected file or directory
 * does not already exist (such as for a save dialog).
 *
 * @author scorbo2
 * @since 2019-11-24
 */
public class FileMustNotExistValidator implements FieldValidator<FileField> {

    @Override
    public ValidationResult validate(FileField fieldToValidate) {
        // Blank values may be permissible:
        boolean allowBlank = fieldToValidate.isAllowBlankValues();
        if (fieldToValidate.getFile() == null) {
            return allowBlank
                    ? ValidationResult.valid()
                    : ValidationResult.invalid("Value cannot be empty.");
        }

        File file = fieldToValidate.getFile();
        if (file.exists()) {
            return ValidationResult.invalid("File or directory already exists.");
        }
        return ValidationResult.valid();
    }
}
