package ca.corbett.forms.validators;

import ca.corbett.forms.fields.FileField;
import ca.corbett.forms.fields.FormField;

import java.io.File;

/**
 * A FieldValidator that ensures that the chosen File can be written.
 *
 * @author scorbo2
 * @since 2019-11-24
 */
public class FileMustBeWritableValidator extends FieldValidator<FormField> {

    public FileMustBeWritableValidator(FileField field) {
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

        File file = ((FileField)field).getFile();
        if (file == null || !file.canWrite()) {
            return new ValidationResult(false, "Selected location must be writable.");
        }

        return new ValidationResult();
    }
}
