package ca.corbett.forms.validators;

import ca.corbett.forms.fields.FileField;

import java.io.File;

/**
 * A FieldValidator that ensures that the chosen Directory exists.
 *
 * @author scorbo2
 * @since 2019-11-24
 */
public class FileMustExistValidator extends FieldValidator<FileField> {

    @Override
    public ValidationResult validate(FileField fieldToValidate) {
        // Blank values may be permissible:
        boolean allowBlank = fieldToValidate.isAllowBlankValues();
        if (fieldToValidate.getFile() == null) {
            return allowBlank ? new ValidationResult() : new ValidationResult(false, "Value cannot be blank.");
        }

        File file = fieldToValidate.getFile();
        if (!file.exists()) {
            return new ValidationResult(false, "File or directory must exist.");
        }
        if (fieldToValidate.getSelectionType() == FileField.SelectionType.ExistingDirectory && !file.isDirectory()) {
            return new ValidationResult(false, "Input must be a directory, not a file.");
        }
        if (fieldToValidate.getSelectionType() == FileField.SelectionType.ExistingFile && file.isDirectory()) {
            return new ValidationResult(false, "Input must be a file, not a directory.");
        }
        return new ValidationResult();
    }
}
