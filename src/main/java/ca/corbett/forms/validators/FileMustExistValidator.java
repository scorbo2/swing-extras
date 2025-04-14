package ca.corbett.forms.validators;

import ca.corbett.forms.fields.FileField;
import ca.corbett.forms.fields.FormField;

import java.io.File;

/**
 * A FieldValidator that ensures that the chosen Directory exists.
 *
 * @author scorbo2
 * @since 2019-11-24
 */
public class FileMustExistValidator extends FieldValidator<FormField> {

    public FileMustExistValidator(FileField field) {
        super(field);
    }

    @Override
    public ValidationResult validate() {
        FileField ourField = (FileField)field;

        // Blank values may be permissible:
        boolean allowBlank = ourField.isAllowBlankValues();
        if (ourField.getFile() == null) {
            return allowBlank ? new ValidationResult() : new ValidationResult(false, "Value cannot be blank.");
        }

        File file = ourField.getFile();
        if (!file.exists()) {
            return new ValidationResult(false, "File or directory must exist.");
        }
        if (ourField.getSelectionType() == FileField.SelectionType.ExistingDirectory && !file.isDirectory()) {
            return new ValidationResult(false, "Input must be a directory, not a file.");
        }
        if (ourField.getSelectionType() == FileField.SelectionType.ExistingFile && file.isDirectory()) {
            return new ValidationResult(false, "Input must be a file, not a directory.");
        }
        return new ValidationResult();
    }
}
