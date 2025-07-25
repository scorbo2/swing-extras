package ca.corbett.extras.properties;

import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.TextField;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a property that can store some text value.
 *
 * @author scorbett
 * @since sc-util 1.8
 */
public class TextProperty extends AbstractProperty {

    private static final Logger logger = Logger.getLogger(TextProperty.class.getName());

    protected String value;
    protected int columns;
    protected int rows;
    protected boolean allowBlank;

    public TextProperty(String name, String label) {
        this(name, label, "", 30, 1);
    }

    public TextProperty(String name, String label, String value) {
        this(name, label, value, 30, 1);
    }

    public TextProperty(String name, String label, String value, int cols, int rows) {
        super(name, label);
        this.value = value;
        this.columns = cols;
        this.rows = rows;
    }

    public String getValue() {
        return value;
    }

    public TextProperty setValue(String value) {
        this.value = value;
        return this;
    }

    public int getColumns() {
        return columns;
    }

    public TextProperty setColumns(int columns) {
        this.columns = columns;
        return this;
    }

    public int getRows() {
        return rows;
    }

    public TextProperty setRows(int rows) {
        this.rows = rows;
        return this;
    }

    public TextProperty setAllowBlank(boolean allow) {
        allowBlank = allow;
        return this;
    }

    public boolean isAllowBlank() {
        return allowBlank;
    }

    @Override
    public void saveToProps(Properties props) {
        props.setInteger(fullyQualifiedName + ".cols", columns);
        props.setInteger(fullyQualifiedName + ".rows", rows);
        props.setString(fullyQualifiedName + ".value", value);
        props.setBoolean(fullyQualifiedName + ".allowBlank", allowBlank);
    }

    @Override
    public void loadFromProps(Properties props) {
        columns = props.getInteger(fullyQualifiedName + ".cols", columns);
        rows = props.getInteger(fullyQualifiedName + ".rows", rows);
        value = props.getString(fullyQualifiedName + ".value", value);
        allowBlank = props.getBoolean(fullyQualifiedName + ".allowBlank", allowBlank);
    }

    @Override
    protected FormField generateFormFieldImpl() {
        TextField textField = new TextField(propertyLabel, columns, rows, allowBlank);
        textField.setText(value);
        return textField;
    }

    @Override
    public void loadFromFormField(FormField field) {
        if (field.getIdentifier() == null
                || !field.getIdentifier().equals(fullyQualifiedName)
                || !(field instanceof TextField)) {
            logger.log(Level.SEVERE, "BooleanProperty.loadFromFormField: received the wrong field \"{0}\"",
                       field.getIdentifier());
            return;
        }

        value = ((TextField)field).getText();
    }

}
