package ca.corbett.extras.properties;

import ca.corbett.forms.fields.FontField;
import ca.corbett.forms.fields.FormField;

import java.awt.Color;
import java.awt.Font;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a property field that allows storing a Font,
 * along with associated style and optional color attributes.
 *
 * @author scorbo2
 */
public class FontProperty extends AbstractProperty {

    private static final Logger logger = Logger.getLogger(FontProperty.class.getName());
    public static final Font DEFAULT_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

    protected Font font;
    protected Color textColor;
    protected Color bgColor;
    protected boolean allowSizeSelection = true;

    /**
     * Creates a new FontProperty with all default values.
     * This will use FontProperty.DEFAULT_FONT as the font, and will
     * set null for both foreground and background colors.
     *
     * @param name  The fully qualified field name.
     * @param label The field label.
     */
    public FontProperty(String name, String label) {
        this(name, label, DEFAULT_FONT, null, null);
    }

    /**
     * Creates a new FontProperty with the given font and
     * null for both foreground and background colors.
     *
     * @param name  The fully qualified field name.
     * @param label The field label.
     * @param font  The initial font.
     */
    public FontProperty(String name, String label, Font font) {
        this(name, label, font, null, null);
    }

    /**
     * Creates a new FontProperty with the given font and
     * the given foreground color, and null for the background color.
     *
     * @param name      The fully qualified field name.
     * @param label     The field label.
     * @param font      The initial font.
     * @param textColor The foreground color.
     */
    public FontProperty(String name, String label, Font font, Color textColor) {
        this(name, label, font, textColor, null);
    }

    /**
     * Creates a new FontProperty with the default font and
     * the given foreground color and a null background color.
     * FontProperty.DEFAULT_FONT will be used for the font.
     *
     * @param name      The fully qualified field name.
     * @param label     The field label.
     * @param textColor The foreground color.
     */
    public FontProperty(String name, String label, Color textColor) {
        this(name, label, DEFAULT_FONT, textColor, null);
    }

    /**
     * Creates a new FontProperty with the default font and
     * the given foreground color and the given background color.
     * FontProperty.DEFAULT_FONT will be used for the font.
     *
     * @param name      The fully qualified field name.
     * @param label     The field label.
     * @param textColor The foreground color.
     * @param bgColor   The background color.
     */
    public FontProperty(String name, String label, Color textColor, Color bgColor) {
        this(name, label, DEFAULT_FONT, textColor, bgColor);
    }

    /**
     * Creates a new FontProperty with the given font and
     * the given foreground color and the given background color.
     *
     * @param name      The fully qualified field name.
     * @param label     The field label.
     * @param font      The initial font.
     * @param textColor The foreground color.
     * @param bgColor   The background color.
     */
    public FontProperty(String name, String label, Font font, Color textColor, Color bgColor) {
        super(name, label);
        this.font = font;
        this.textColor = textColor;
        this.bgColor = bgColor;
    }

    public FontProperty setFont(Font font) {
        this.font = font;
        return this;
    }

    public Color getTextColor() {
        return textColor;
    }

    public Color getBgColor() {
        return bgColor;
    }

    public FontProperty setTextColor(Color color) {
        textColor = color;
        return this;
    }

    public void setBgColor(Color color) {
        bgColor = color;
    }

    public Font getFont() {
        return font;
    }

    public boolean isAllowSizeSelection() {
        return allowSizeSelection;
    }

    public FontProperty setAllowSizeSelection(boolean allow) {
        allowSizeSelection = allow;
        return this;
    }

    @Override
    public void saveToProps(Properties props) {
        props.setString(fullyQualifiedName + ".name", font.getFamily());
        props.setBoolean(fullyQualifiedName + ".isBold", font.isBold());
        props.setBoolean(fullyQualifiedName + ".isItalic", font.isItalic());
        props.setInteger(fullyQualifiedName + ".pointSize", font.getSize());
        if (textColor != null) {
            props.setColor(fullyQualifiedName + ".textColor", textColor);
        }
        else {
            props.remove(fullyQualifiedName + ".textColor");
        }
        if (bgColor != null) {
            props.setColor(fullyQualifiedName + ".bgColor", bgColor);
        }
        else {
            props.remove(fullyQualifiedName + ".bgColor");
        }
        props.setBoolean(fullyQualifiedName + ".allowSizeSelection", allowSizeSelection);
    }

    @Override
    public void loadFromProps(Properties props) {
        String fontName = props.getString(fullyQualifiedName + ".name", font.getFamily());
        boolean isBold = props.getBoolean(fullyQualifiedName + ".isBold", font.isBold());
        boolean isItalic = props.getBoolean(fullyQualifiedName + ".isItalic", font.isItalic());
        int pointSize = props.getInteger(fullyQualifiedName + ".pointSize", font.getSize());
        font = Properties.createFontFromAttributes(fontName, isBold, isItalic, pointSize);
        textColor = props.getColor(fullyQualifiedName + ".textColor", textColor);
        bgColor = props.getColor(fullyQualifiedName + ".bgColor", bgColor);
        allowSizeSelection = props.getBoolean(fullyQualifiedName + ".allowSizeSelection", allowSizeSelection);
    }

    @Override
    protected FormField generateFormFieldImpl() {
        FontField field = new FontField(propertyLabel, getFont(), textColor, bgColor);
        field.setShowSizeField(allowSizeSelection);
        return field;
    }

    @Override
    public void loadFromFormField(FormField field) {
        if (field.getIdentifier() == null
                || !field.getIdentifier().equals(fullyQualifiedName)
                || !(field instanceof FontField)) {
            logger.log(Level.SEVERE, "FontProperty.loadFromFormField: received the wrong field \"{0}\"",
                       field.getIdentifier());
            return;
        }

        FontField fontField = (FontField)field;
        font = ((FontField)field).getSelectedFont();
        textColor = fontField.getTextColor();
        bgColor = fontField.getBgColor();
    }

}
