package ca.corbett.forms.validators;

import ca.corbett.forms.fields.ListField;

/**
 * A simple Validator that can be attached to a ListField to ensure
 * that the list is not empty. This is intended for dynamically populated
 * lists, where list contents can be added or removed while the form is open.
 * For ListFields that display a static list of options, this validator
 * will always pass (unless the supplied static list was empty).
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class ListMustNotBeEmptyValidator implements FieldValidator<ListField<?>> {
    private static final String MESSAGE = "The list cannot be empty.";

    @Override
    public ValidationResult validate(ListField<?> fieldToValidate) {
        return fieldToValidate.getListModel().isEmpty()
                ? ValidationResult.invalid(MESSAGE)
                : ValidationResult.valid();
    }
}
