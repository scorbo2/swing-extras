package ca.corbett.forms.validators;

import ca.corbett.forms.fields.FileField;

import java.io.File;

/**
 * A FieldValidator that ensures that the chosen Directory can be read.
 *
 * @author scorbo2
 * @since 2019-11-24
 */
public class FileMustBeReadableValidator extends FieldValidator<FileField> {

    @Override
    public ValidationResult validate(FileField fieldToValidate) {
        // Blank values may be permissible:
        boolean allowBlank = fieldToValidate.isAllowBlankValues();
        if (fieldToValidate.getFile() == null) {
            return allowBlank ? new ValidationResult() : new ValidationResult(false,
                                                                              "Selected location must be readable.");
        }

        File dir = fieldToValidate.getFile();
        if (!dir.canRead()) {
            return new ValidationResult(false, "Selected location must be readable.");
        }

        return new ValidationResult();
    }
}
