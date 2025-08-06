package ca.corbett.forms.validators;

import ca.corbett.forms.fields.FormField;

/**
 * Provides an abstract base for a class that can perform validation on some type of FormField.
 * Refer to the provided implementation classes in this package for examples of what you
 * can do with this class. Writing your own validator with custom business logic or
 * validation rules is as easy as extending this class and writing the validate() method!
 *
 * @param <T> FieldValidators are typed to a specific type of FormField.
 * @author scorbo2
 * @since 2019-11-23
 */
public abstract class FieldValidator<T extends FormField> {

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
     * @param fieldToValidate The FormField to be validated.
     * @return A ValidationResult which describes whether the current value in our field is valid.
     */
    public abstract ValidationResult validate(T fieldToValidate);
}
