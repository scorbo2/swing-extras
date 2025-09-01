package ca.corbett.forms.validators;

import ca.corbett.forms.fields.FileField;

import java.io.File;

/**
 * A FieldValidator that ensures that the chosen File or Directory exists.
 * Also checks that the selection makes sense given the selection type of the
 * FileField in question. For example, if the selection type is DIRECTORIES_ONLY
 * and you select a file, that selection will fail validation.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2019-11-24
 */
public class FileMustExistValidator implements FieldValidator<FileField> {

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
        if (!file.exists()) {
            return ValidationResult.invalid("File or directory must exist.");
        }
        if (fieldToValidate.getSelectionType() == FileField.SelectionType.ExistingDirectory && !file.isDirectory()) {
            return ValidationResult.invalid("Input must be a directory, not a file.");
        }
        if (fieldToValidate.getSelectionType() == FileField.SelectionType.ExistingFile && file.isDirectory()) {
            return ValidationResult.invalid("Input must be a file, not a directory.");
        }
        return ValidationResult.valid();
    }
}
