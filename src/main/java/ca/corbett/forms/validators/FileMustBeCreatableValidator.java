package ca.corbett.forms.validators;

import ca.corbett.forms.fields.FileField;

import java.io.File;

/**
 * A validator for use with FileField to ensure that the selected File
 * is in a location that can be written. Specifically, if you're browsing
 * for a new File which does NOT exist, it should be in a location where
 * we have permission to create a new file.
 *
 * @author scorbo2
 * @since 2019-11-27
 */
public class FileMustBeCreatableValidator extends FieldValidator<FileField> {

    @Override
    public ValidationResult validate(FileField fieldToValidate) {

        // Blank values may be permissible:
        boolean allowBlank = fieldToValidate.isAllowBlankValues();
        if (fieldToValidate.getFile() == null) {
            return allowBlank ? new ValidationResult() : new ValidationResult(false,
                                                                              "Selected location must be writable.");
        }

        File file = fieldToValidate.getFile().getParentFile();
        if (file == null) {
            file = fieldToValidate.getFile(); // wonky case where someone selected the root directory
        }

        if (!file.canWrite()) {
            return new ValidationResult(false, "Selected location must be writable.");
        }

        return new ValidationResult();
    }
}
