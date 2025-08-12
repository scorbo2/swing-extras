package ca.corbett.extras.properties;

import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.LongTextField;
import ca.corbett.forms.fields.ShortTextField;

import java.util.logging.Level;
import java.util.logging.Logger;

import static ca.corbett.forms.fields.LongTextField.TextFieldType;

/**
 * Represents a property that can store a long text (multi-line) value.
 * The options for creating a LongTextProperty exactly match those for
 * containing a LongTextField, as that is the underlying FormField
 * for this property type.
 *
 * @author scorbett
 * @since sc-util 1.8
 */
public class LongTextProperty extends AbstractProperty {

    private static final Logger logger = Logger.getLogger(LongTextProperty.class.getName());

    protected String value;
    protected TextFieldType textFieldType;
    protected int width;
    protected int height;
    protected boolean allowBlank;

    /**
     * Invoked internally by the static factory methods.
     */
    protected LongTextProperty(String name, String label, TextFieldType fieldType, int x, int y) {
        super(name, label);
        this.width = x;
        this.height = y;
        this.textFieldType = fieldType;
    }

    /**
     * Creates a TextProperty with the specified number of rows and columns.
     * If rows is 1, a single-line TextField is created. Otherwise, a multi-line
     * TextField is created.
     */
    public static LongTextProperty ofFixedSizeMultiLine(String name, String label, int rows, int cols) {
        return new LongTextProperty(name, label, TextFieldType.MULTI_LINE_FIXED_ROWS_COLS, cols, rows);
    }

    /**
     * Creates a TextProperty with the specified pixel dimensions.
     * Generally, you should avoid specifying pixel dimensions for UI components, as it
     * doesn't scale well with font size changes or Look and Feel changes.
     */
    public static LongTextProperty ofFixedPixelSizeMultiLine(String name, String label, int width, int height) {
        return new LongTextProperty(name, label, TextFieldType.MULTI_LINE_FIXED_PIXELS, width, height);
    }

    /**
     * Creates a multi-line TextField which will automatically expand to fill
     * the width of whatever FormPanel it is added to.
     */
    public static LongTextProperty ofDynamicSizingMultiLine(String name, String label) {
        return new LongTextProperty(name, label, TextFieldType.MULTI_LINE_DYNAMIC, 0, 0);
    }

    public String getValue() {
        return value;
    }

    public LongTextProperty setValue(String value) {
        this.value = value;
        return this;
    }

    public LongTextProperty setAllowBlank(boolean allow) {
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
        width = props.getInteger(fullyQualifiedName + ".width", width);
        height = props.getInteger(fullyQualifiedName + ".height", height);
        value = props.getString(fullyQualifiedName + ".value", value);
        allowBlank = props.getBoolean(fullyQualifiedName + ".allowBlank", allowBlank);
    }

    @Override
    protected FormField generateFormFieldImpl() {
        LongTextField textField = switch (textFieldType) {
            case MULTI_LINE_FIXED_ROWS_COLS -> LongTextField.ofFixedSizeMultiLine(propertyLabel, height, width);
            case MULTI_LINE_FIXED_PIXELS -> LongTextField.ofFixedPixelSizeMultiLine(propertyLabel, width, height);
            case MULTI_LINE_DYNAMIC -> LongTextField.ofDynamicSizingMultiLine(propertyLabel, height);
        };
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
