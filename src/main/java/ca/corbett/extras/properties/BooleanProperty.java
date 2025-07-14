package ca.corbett.extras.properties;

import ca.corbett.forms.fields.CheckBoxField;
import ca.corbett.forms.fields.FormField;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a property that can either be on or off.
 *
 * @author scorbo2
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
    public FormField generateFormField() {
        CheckBoxField field = new CheckBoxField(propertyLabel, value);
        field.setIdentifier(fullyQualifiedName);
        field.setEnabled(!isReadOnly);
        field.setHelpText(helpText);
        return field;
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

        value = ((CheckBoxField)field).isChecked();
    }

}
