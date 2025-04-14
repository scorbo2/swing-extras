package ca.corbett.forms.validators;

import ca.corbett.forms.fields.TextField;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * A FieldValidator that enforces yyyy-mm-dd format on a given TextField.
 * This is a bit cheesy but will do until and unless I ever put in a proper calendar chooser.
 *
 * @author scorbo2
 * @since 2019-11-24
 */
public class YMDDateValidator extends FieldValidator<TextField> {

    private final boolean allowBlankValues;
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    public YMDDateValidator(TextField textField) {
        this(textField, false);
    }

    public YMDDateValidator(TextField textField, boolean allowBlankValues) {
        super(textField);
        this.allowBlankValues = allowBlankValues;
    }

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        String currentStr = field.getText().trim();
        if (currentStr.isEmpty() && allowBlankValues) {
            return result;
        }
        try {
            format.parse(currentStr);
        }
        catch (ParseException e) {
            result.setResult(false, "Value must be in format: yyyy-mm-dd");
        }
        return result;
    }

}
