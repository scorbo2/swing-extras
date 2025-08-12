package ca.corbett.extras.properties;

import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.TextField;

import java.util.logging.Level;
import java.util.logging.Logger;

import static ca.corbett.forms.fields.TextField.TextFieldType;

/**
 * Represents a property that can store some text value.
 *
 * @author scorbett
 * @since sc-util 1.8
 */
public class TextProperty extends AbstractProperty {

    private static final Logger logger = Logger.getLogger(TextProperty.class.getName());

    protected String value;
    protected TextFieldType textFieldType;
    protected int width;
    protected int height;
    protected boolean allowBlank;

    /**
     * Invoked internally by the static factory methods.
     */
    protected TextProperty(String name, String label, TextFieldType fieldType, int x, int y) {
        super(name, label);
        this.width = x;
        this.height = y;
        this.textFieldType = fieldType;
    }

    /**
     * Creates a single-line TextProperty with 30 columns.
     */
    public static TextProperty ofSingleLine(String name, String label) {
        return new TextProperty(name, label, TextFieldType.SINGLE_LINE, 30, 0);
    }

    /**
     * Creates a single-line TextProperty with the specified column count.
     */
    public static TextProperty ofSingleLine(String name, String label, int cols) {
        return new TextProperty(name, label, TextFieldType.SINGLE_LINE, cols, 0);
    }

    /**
     * Creates a TextProperty with the specified number of rows and columns.
     * If rows is 1, a single-line TextField is created. Otherwise, a multi-line
     * TextField is created.
     */
    public static TextProperty ofFixedSizeMultiLine(String name, String label, int rows, int cols) {
        return new TextProperty(name, label, TextFieldType.MULTI_LINE_FIXED_ROWS_COLS, cols, rows);
    }

    /**
     * Creates a TextProperty with the specified pixel dimensions.
     * Generally, you should avoid specifying pixel dimensions for UI components, as it
     * doesn't scale well with font size changes or Look and Feel changes.
     */
    public static TextProperty ofFixedPixelSizeMultiLine(String name, String label, int width, int height) {
        return new TextProperty(name, label, TextFieldType.MULTI_LINE_FIXED_PIXELS, width, height);
    }

    /**
     * Creates a multi-line TextField which will automatically expand to fill
     * the width of whatever FormPanel it is added to.
     */
    public static TextProperty ofDynamicSizingMultiLine(String name, String label) {
        return new TextProperty(name, label, TextFieldType.MULTI_LINE_DYNAMIC, 0, 0);
    }

    public String getValue() {
        return value;
    }

    public TextProperty setValue(String value) {
        this.value = value;
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
        props.setString(fullyQualifiedName + ".fieldType", textFieldType.name());
        props.setInteger(fullyQualifiedName + ".width", width);
        props.setInteger(fullyQualifiedName + ".height", height);
        props.setString(fullyQualifiedName + ".value", value);
        props.setBoolean(fullyQualifiedName + ".allowBlank", allowBlank);
    }

    @Override
    public void loadFromProps(Properties props) {
        textFieldType = TextFieldType.valueOf(props.getString(fullyQualifiedName + ".fieldType", textFieldType.name()));
        width = props.getInteger(fullyQualifiedName + ".cols", width);
        height = props.getInteger(fullyQualifiedName + ".rows", height);
        value = props.getString(fullyQualifiedName + ".value", value);
        allowBlank = props.getBoolean(fullyQualifiedName + ".allowBlank", allowBlank);
    }

    @Override
    protected FormField generateFormFieldImpl() {
        TextField textField = switch (textFieldType) {
            case SINGLE_LINE -> TextField.ofSingleLine(propertyLabel, width);
            case MULTI_LINE_FIXED_ROWS_COLS -> TextField.ofFixedSizeMultiLine(propertyLabel, height, width);
            case MULTI_LINE_FIXED_PIXELS -> TextField.ofFixedPixelSizeMultiLine(propertyLabel, width, height);
            case MULTI_LINE_DYNAMIC -> TextField.ofDynamicSizingMultiLine(propertyLabel, height);
        };
        textField.setAllowBlank(allowBlank);
        textField.setText(value);
        return textField;
    }

    @Override
    public void loadFromFormField(FormField field) {
        if (field.getIdentifier() == null
                || !field.getIdentifier().equals(fullyQualifiedName)
                || !(field instanceof TextField)) {
            logger.log(Level.SEVERE, "TextProperty.loadFromFormField: received the wrong field \"{0}\"",
                       field.getIdentifier());
            return;
        }

        value = ((TextField)field).getText();
    }
}
