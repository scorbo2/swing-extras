package ca.corbett.forms.validators;

import ca.corbett.forms.fields.ShortTextField;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * A simple FieldValidator that can be attached to any ShortTextField to ensure that the
 * field's value is either blank or a valid URL. If blank values are not permitted,
 * you can use the overloaded constructor to disallow them. Blank values are
 * considered valid by default.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 3.0
 */
public class UrlValidator implements FieldValidator<ShortTextField> {

    public static final String MESSAGE = "Value must be a valid URL";

    private final boolean allowBlank;

    public UrlValidator() {
        this(true);
    }

    public UrlValidator(boolean allowBlank) {
        this.allowBlank = allowBlank;
    }

    @Override
    public ValidationResult validate(ShortTextField fieldToValidate) {
        String currentValue = fieldToValidate.getText() == null ? "" : fieldToValidate.getText().trim();
        if (currentValue.isEmpty()) {
            return allowBlank
                    ? ValidationResult.valid() // blank value is explicitly fine
                    : ValidationResult.invalid("Value cannot be blank"); // blank value is not fine
        }

        return isValidUrl(currentValue) ? ValidationResult.valid() : ValidationResult.invalid(MESSAGE);
    }

    /**
     * A public convenience method for validating URLs. We defer this validation to
     * the <code>java.net.URL</code> and <code>java.net.URI</code> classes.
     *
     * @param url A URL in string form.
     * @return true if the URL is well-formed.
     */
    public static boolean isValidUrl(String url) {
        try {
            URL ignored = new URI(url).toURL(); // this will catch both URISyntaxException and MalformedURLException
            return true;
        }
        catch (URISyntaxException | MalformedURLException ignored) {
            return false;
        }
    }
}
