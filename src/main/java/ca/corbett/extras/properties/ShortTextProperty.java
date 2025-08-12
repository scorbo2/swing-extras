package ca.corbett.extras.properties;

import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.ShortTextField;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ShortTextProperty extends AbstractProperty {

    private static final Logger logger = Logger.getLogger(ShortTextProperty.class.getName());

    protected String value;
    protected int cols;
    protected boolean allowBlank;

    public ShortTextProperty(String name, String label, String value) {
        this(name, label, value, 30);
    }

    public ShortTextProperty(String name, String label, String value, int cols) {
        super(name, label);
        this.value = value;
        this.cols = cols;
    }

    public String getValue() {
        return value;
    }

    public ShortTextProperty setValue(String value) {
        this.value = value;
        return this;
    }

    public ShortTextProperty setAllowBlank(boolean allow) {
        allowBlank = allow;
        return this;
    }

    public boolean isAllowBlank() {
        return allowBlank;
    }

    @Override
    public void saveToProps(Properties props) {
        props.setInteger(fullyQualifiedName + ".cols", cols);
        props.setString(fullyQualifiedName + ".value", value);
        props.setBoolean(fullyQualifiedName + ".allowBlank", allowBlank);
    }

    @Override
    public void loadFromProps(Properties props) {
        cols = props.getInteger(fullyQualifiedName + ".cols", cols);
        value = props.getString(fullyQualifiedName + ".value", value);
        allowBlank = props.getBoolean(fullyQualifiedName + ".allowBlank", allowBlank);
    }

    @Override
    protected FormField generateFormFieldImpl() {
        ShortTextField textField = new ShortTextField(propertyLabel, cols);
        textField.setAllowBlank(allowBlank);
        textField.setText(value);
        return textField;
    }

    @Override
    public void loadFromFormField(FormField field) {
        if (field.getIdentifier() == null
                || !field.getIdentifier().equals(fullyQualifiedName)
                || !(field instanceof ShortTextField)) {
            logger.log(Level.SEVERE, "TextProperty.loadFromFormField: received the wrong field \"{0}\"",
                       field.getIdentifier());
            return;
        }

        value = ((ShortTextField)field).getText();
    }
}
