package ca.corbett.forms.validators;

import ca.corbett.forms.fields.FileField;
import ca.corbett.forms.fields.FormField;

import java.io.File;

/**
 * A FieldValidator that ensures that the chosen Directory can be read.
 *
 * @author scorbo2
 * @since 2019-11-24
 */
public class FileMustBeReadableValidator extends FieldValidator<FormField> {

    public FileMustBeReadableValidator(FileField field) {
        super(field);
    }

    @Override
    public ValidationResult validate() {
        FileField ourField = (FileField)field;

        // Blank values may be permissable:
        boolean allowBlank = ourField.isAllowBlankValues();
        if (ourField.getFile() == null) {
            return allowBlank ? new ValidationResult() : new ValidationResult(false,
                                                                              "Selected location must be readable.");
        }

        File dir = ourField.getFile();
        if (!dir.canRead()) {
            return new ValidationResult(false, "Selected location must be readable.");
        }

        return new ValidationResult();
    }
}
