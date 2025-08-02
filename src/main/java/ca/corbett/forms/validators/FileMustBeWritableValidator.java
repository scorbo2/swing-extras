package ca.corbett.forms.validators;

import ca.corbett.forms.fields.FileField;

import java.io.File;

/**
 * A FieldValidator that ensures that the chosen File can be written.
 *
 * @author scorbo2
 * @since 2019-11-24
 */
public class FileMustBeWritableValidator extends FieldValidator<FileField> {

    @Override
    public ValidationResult validate(FileField fieldToValidate) {
        // Blank values may be permissible:
        boolean allowBlank = fieldToValidate.isAllowBlankValues();
        if (fieldToValidate.getFile() == null) {
            return allowBlank ? new ValidationResult() : new ValidationResult(false,
                                                                              "Selected location must be writable.");
        }

        File file = fieldToValidate.getFile();
        if (file == null || !file.canWrite()) {
            return new ValidationResult(false, "Selected location must be writable.");
        }

        return new ValidationResult();
    }
}
