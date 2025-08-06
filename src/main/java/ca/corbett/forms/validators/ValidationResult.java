package ca.corbett.forms.validators;

/**
 * Used by FieldValidator to report successful or unsuccessful validation on a FormField.
 * If validation is unsuccessful, the convention is to supply some user-readable
 * message to explain what's wrong, and what the user can do to fix it.
 *
 * @author scorbo2
 * @since 2019-11-23
 */
public class ValidationResult {

    private static final ValidationResult VALID = new ValidationResult(true, "");

    private final boolean isValid;
    private final String message;

    /**
     * Creates a ValidationResult with the given isValid value and message.
     *
     * @param isValid Whether the field in question is considered valid.
     * @param message The validation message (should be blank if isValid==true).
     */
    protected ValidationResult(boolean isValid, String message) {
        this.isValid = isValid;
        this.message = message;
    }

    /**
     * Returns a valid ValidationResult with no validation message.
     */
    public static ValidationResult valid() {
        return VALID;
    }

    /**
     * Creates and returns an invalid ValidationResult with the given message.
     */
    public static ValidationResult invalid(String msg) {
        return new ValidationResult(false, msg);
    }

    /**
     * Returns whether the ValidationResult is valid.
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * Returns the message associated with this ValidationResult, or empty string if it
     * contains no message.
     */
    public String getMessage() {
        return message;
    }
}
