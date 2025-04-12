package ca.corbett.forms.validators;

import ca.corbett.forms.fields.FormField;

/**
 * Provides an interface that can be implemented by any class that wants to validate
 * the contents of a particular FormField. You must supply a reference to the FormField
 * that this FieldValidator needs to know about in the constructor, so that it is
 * available when we go to validate the field.
 *
 * @param <T> FieldValidators are typed to a specific type of FormField.
 * @author scorbo2
 * @since 2019-11-23
 */
public abstract class FieldValidator<T extends FormField> {

    protected T field;

    public FieldValidator(T field) {
        this.field = field;
    }

    /**
     * Performs validation on the field in question, and returns a ValidationResult as appropriate.
     * Here you can do whatever checks you need to do, either on the FormField in question, or
     * on other FormFields on the same FormPanel if you have references to them, or to whatever
     * other state your application maintains. If you wish to signal a validation error, it's
     * highly recommended to make sure you give some clue as to what the user can do to fix
     * the problem.
     * <p>An example of bad validation:</p>
     * <BLOCKQUOTE><PRE>return new ValidationResult(false, "Something bad happened.");</PRE></BLOCKQUOTE>
     * <p>An example of good validation:</p>
     * <BLOCKQUOTE><PRE>return new ValidationResult(false, "Value must be less than 10.");</PRE></BLOCKQUOTE>
     *
     * @return A ValidationResult which describes whether or not the current value in our field is valid.
     */
    public abstract ValidationResult validate();
}
