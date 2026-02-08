package ca.corbett.extras.properties;

import ca.corbett.forms.fields.CheckBoxField;
import ca.corbett.forms.fields.FormField;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a property that can either be on or off.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2024-12-30
 */
public class BooleanProperty extends AbstractProperty {

    private static final Logger logger = Logger.getLogger(BooleanProperty.class.getName());

    protected boolean value;

    public BooleanProperty(String name, String label) {
        this(name, label, false);
    }

    public BooleanProperty(String name, String label, boolean initialValue) {
        super(name, label);
        value = initialValue;
    }

    public BooleanProperty setValue(boolean val) {
        value = val;
        return this;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public void saveToProps(Properties props) {
        props.setBoolean(fullyQualifiedName, value);
    }

    @Override
    public void loadFromProps(Properties props) {
        value = props.getBoolean(fullyQualifiedName, value);
    }

    @Override
    protected FormField generateFormFieldImpl() {
        return new CheckBoxField(propertyLabel, value);
    }

    @Override
    public void loadFromFormField(FormField field) {
        if (field.getIdentifier() == null
                || !field.getIdentifier().equals(fullyQualifiedName)
                || !(field instanceof CheckBoxField)) {
            logger.log(Level.SEVERE, "BooleanProperty.loadFromFormField: received the wrong field \"{0}\"",
                       field.getIdentifier());
            return;
        }

        // A checkbox only has two states, so it's hard to imagine a checkbox failing a validation check,
        // but it can absolutely happen if the client application has put a custom validator on it.
        // For example, you can't check this checkbox if some other condition isn't met. If the
        // field is in an invalid state, we should ignore it. (Proper form validation will prevent it
        // from getting here, but there's nothing to force client code to do that, so let's be defensive).
        if (!field.isValid()) {
            logger.log(Level.WARNING, "BooleanProperty.loadFromFormField: received an invalid field \"{0}\"",
                       field.getIdentifier());
            return;
        }

        value = ((CheckBoxField)field).isChecked();
    }

}
