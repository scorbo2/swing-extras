package ca.corbett.forms.validators;

import ca.corbett.forms.fields.FileField;

/**
 * Similar to NonBlankFieldValidator for TextFields, this FieldValidator implementation
 * insists that a FileField cannot contain a blank value. Note that this validation is
 * in addition to the SelectionType for the FileField in question (e.g. if you specify
 * ExistingFile and also add this FieldValidator, then a file must be specified AND exist.
 * If you specify ExistingFile but don't add this FieldValidator, then the given file
 * must only exist if one is specified... blank value will return null in that case).
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2020-10-13
 */
public class FileMustBeSpecifiedValidator implements FieldValidator<FileField> {

    @Override
    public ValidationResult validate(FileField fieldToValidate) {
        if (fieldToValidate.getTextField().getText().trim().isEmpty()) {
            return ValidationResult.invalid("Value cannot be blank.");
        }
        return ValidationResult.valid();
    }
}
