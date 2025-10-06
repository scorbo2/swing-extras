package ca.corbett.forms.validators;

import ca.corbett.forms.fields.FileField;

import java.io.File;

public class FileMustNotBeDirectoryValidator implements FieldValidator<FileField> {
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
        if (file.exists() && file.isDirectory()) {
            return ValidationResult.invalid("Must not select a directory.");
        }
        return ValidationResult.valid();
    }
}
