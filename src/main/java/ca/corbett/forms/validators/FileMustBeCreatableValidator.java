package ca.corbett.forms.validators;

import ca.corbett.forms.fields.FileField;
import ca.corbett.forms.fields.FormField;

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
public class FileMustBeCreatableValidator extends FieldValidator<FormField> {

    public FileMustBeCreatableValidator(FileField field) {
        super(field);
    }

    @Override
    public ValidationResult validate() {
        FileField ourField = (FileField)field;

        // Blank values may be permissible:
        boolean allowBlank = ourField.isAllowBlankValues();
        if (ourField.getFile() == null) {
            return allowBlank ? new ValidationResult() : new ValidationResult(false,
                                                                              "Selected location must be writable.");
        }

        File file = ourField.getFile().getParentFile();
        if (file == null) {
            file = ourField.getFile(); // wonky case where someone selected the root directory
        }

        if (!file.canWrite()) {
            return new ValidationResult(false, "Selected location must be writable.");
        }

        return new ValidationResult();
    }
}
