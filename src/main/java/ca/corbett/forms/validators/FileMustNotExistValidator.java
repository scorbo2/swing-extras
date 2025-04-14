package ca.corbett.forms.validators;

import ca.corbett.forms.fields.FileField;
import ca.corbett.forms.fields.FormField;

import java.io.File;

/**
 * The opposite of FileMustExistValidator, this one ensures that the selected file or directory
 * does not already exist (such as for a save dialog).
 *
 * @author scorbo2
 * @since 2019-11-24
 */
public class FileMustNotExistValidator extends FieldValidator<FormField> {

    public FileMustNotExistValidator(FileField field) {
        super(field);
    }

    @Override
    public ValidationResult validate() {
        FileField ourField = (FileField)field;

        // Blank values may be permissible:
        boolean allowBlank = ourField.isAllowBlankValues();
        if (ourField.getFile() == null) {
            return allowBlank ? new ValidationResult() : new ValidationResult(false, "Value cannot be empty.");
        }

        File file = ourField.getFile();
        if (file.exists()) {
            return new ValidationResult(false, "File or directory already exists.");
        }
        return new ValidationResult();
    }
}
