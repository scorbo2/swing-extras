package ca.corbett.extras.image;

import ca.corbett.extras.gradient.Gradient;
import ca.corbett.extras.gradient.GradientType;
import ca.corbett.extras.properties.AbstractProperty;
import ca.corbett.extras.properties.Properties;
import ca.corbett.forms.fields.FormField;

import java.awt.Color;
import java.awt.Font;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a configuration preset for logo images, to be used with LogoGenerator.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public final class LogoProperty extends AbstractProperty {

    private static final Logger logger = Logger.getLogger(LogoProperty.class.getName());

    public enum ColorType {
        SOLID("Solid colour"),
        GRADIENT("Gradient fill");

        private final String label;

        private ColorType(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }

        public static ColorType fromLabel(String l) {
            for (ColorType candidate : values()) {
                if (candidate.label.equals(l)) {
                    return candidate;
                }
            }
            return null;
        }

    }

    private ColorType bgColorType;
    private Color bgColor;
    private Gradient bgGradient;

    private ColorType borderColorType;
    private Color borderColor;
    private Gradient borderGradient;

    private ColorType textColorType;
    private Color textColor;
    private Gradient textGradient;

    private int borderWidth;
    private boolean hasBorder;
    private boolean autoSize;
    private Font font;
    private int logoWidth;
    private int logoHeight;
    private int yTweak;

    public LogoProperty(String fullyQualifiedName) {
        this(fullyQualifiedName, "Logo image");
    }

    public LogoProperty(String fullyQualifiedName, String label) {
        super(fullyQualifiedName, label);
        resetToDefaults();
    }

    /**
     * Returns the width of the logo image that will be generated from this config.
     *
     * @return A pixel width of the logo image to be generated.
     */
    public int getLogoWidth() {
        return logoWidth;
    }

    /**
     * Sets the desired pixel width of the logo image which will be generated from this config.
     *
     * @param width Desired logo image width in pixels.
     */
    public void setLogoWidth(int width) {
        this.logoWidth = width;
    }

    /**
     * Returns the height of the logo image that will be generated from this config.
     *
     * @return A pixel height of the logo image to be generated.
     */
    public int getLogoHeight() {
        return logoHeight;
    }

    /**
     * Sets the desired pixel height of the logo image which will be generated from this config.
     *
     * @param height Desired logo image height in pixels.
     */
    public void setLogoHeight(int height) {
        this.logoHeight = height;
    }

    /**
     * Kludge alert: the vertical centering of text is sometimes wonky, so this property
     * can be used to adjust up or down by supplying a negative or positive pixel value here.
     * Through trial and error, you can property vertically center the image, if Graphics2D
     * gives us incorrect text height values for your text.
     *
     * @param tweak A positive or negative pixel value to adjust logo text vertically. Default 0.
     */
    public void setYTweak(int tweak) {
        yTweak = tweak;
    }

    /**
     * Returns the positive or negative pixel offset that's applied to the text to try to
     * better center it vertically, or 0 if this value has not been set.
     *
     * @return See setYTweak.
     */
    public int getYTweak() {
        return yTweak;
    }

    /**
     * Returns the desired background fill, which is either a solid colour or a gradient fill.
     *
     * @return The ColorType for the background of this logo image.
     */
    public ColorType getBgColorType() {
        return bgColorType;
    }

    /**
     * Returns the GradientConfig for this logo image, if one is in use. See setBgType.
     *
     * @return A GradientConfig object for the background fill of this logo image.
     */
    public Gradient getBgGradient() {
        return bgGradient;
    }

    /**
     * Sets the background fill type for this logo image, which is either a solid colour or
     * a gradient fill.
     *
     * @param bgColorType A ColorType to use for the background of this logo image.
     */
    public void setBgColorType(ColorType bgColorType) {
        this.bgColorType = bgColorType;
    }

    /**
     * Sets the gradient fill type for the image background. This property is ignored
     * unless the BgType is set to GRADIENT.
     *
     * @param bgGradient The GradientConfig to use with this object.
     */
    public void setBgGradient(Gradient bgGradient) {
        this.bgGradient = bgGradient;
    }

    /**
     * Gets the logo image background color. This property is only used if the BgType is
     * set to SOLID.
     *
     * @return The background color of the logo image.
     */
    public Color getBgColor() {
        return bgColor;
    }

    /**
     * Sets the logo image background color.
     *
     * @param bgColor The background color of the logo image.
     */
    public void setBgColor(Color bgColor) {
        this.bgColor = bgColor;
    }

    /**
     * Gets the border color type for this logo image, which is either a solid colour or
     * a gradient fill.
     *
     * @return The ColorType of the border color.
     */
    public ColorType getBorderColorType() {
        return borderColorType;
    }

    /**
     * Returns the GradientConfig for the border of this logo image, if one is in use.
     * See setBorderColorType.
     *
     * @return A GradientConfig object for the border of this logo image.
     */
    public Gradient getBorderGradient() {
        return borderGradient;
    }

    /**
     * Sets the border color type for this logo image, which is either a solid colour or
     * a gradient fill.
     *
     * @param borderColorType A ColorType to use with this logo image.
     */
    public void setBorderColorType(ColorType borderColorType) {
        this.borderColorType = borderColorType;
    }

    /**
     * Sets the gradient fill type for the image border. This property is ignored
     * unless the BorderColorType is set to GRADIENT.
     *
     * @param borderGradient The GradientConfig to use for the border of this object.
     */
    public void setBorderGradient(Gradient borderGradient) {
        this.borderGradient = borderGradient;
    }

    /**
     * Returns the border color of the logo image.
     *
     * @return The border color of the logo image.
     */
    public Color getBorderColor() {
        return borderColor;
    }

    /**
     * Sets the border color of the logo image. This property is ignored if hasBorder() is false.
     *
     * @param borderColor The border color for the generated logo image, if it has a border.
     */
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    /**
     * Returns the pixel width of the border of the logo image.
     *
     * @return The width, in pixels, of the border of the image.
     */
    public int getBorderWidth() {
        return borderWidth;
    }

    /**
     * Sets the width, in pixels, of the border of the generated logo image.
     * Calling setBorderWidth(0) is equivalent to calling setHasBorder(false).
     *
     * @param borderWidth The desired pixel width of the image border, or 0 for no border.
     */
    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
        this.hasBorder = (borderWidth != 0);
    }

    /**
     * Indicates whether or not the generated logo image will have a border applied to it.
     * The border can be explicitly disabled by calling setHasBorder(false), or by
     * calling setBorderWidth(0).
     *
     * @return Whether or not the generated image should have a border.
     */
    public boolean hasBorder() {
        return hasBorder && borderWidth > 0;
    }

    /**
     * Enables or disables the border of the generated image.
     *
     * @param hasBorder Whether the generated image should have a border or not.
     */
    public void setHasBorder(boolean hasBorder) {
        this.hasBorder = hasBorder;
    }

    /**
     * Returns the Font used in the generated image.
     *
     * @return A Font object.
     */
    public Font getFont() {
        return font;
    }

    /**
     * Sets the Font to use in the generated image. Font style information is also
     * taken from here, but the font point size in the given object will be ignored.
     * Use setFontPointSize() to control text size, or use setAutoSize() to allow the size
     * to be calculated automatically.
     *
     * @param font Any Font object.
     */
    public void setFont(Font font) {
        this.font = font;
    }

    /**
     * Allows setting the font by specifying just the family name.
     * The resulting font will be styled as Font.PLAIN.
     *
     * @param fontName A font family name, such as "Serif" or "Sans-Serif".
     */
    public void setFontByFamilyName(String fontName) {
        this.font = new Font(fontName, Font.PLAIN, 12);
    }

    /**
     * Gets the text color.
     *
     * @return The font color for the logo image.
     */
    public Color getTextColor() {
        return textColor;
    }

    /**
     * Sets the text color to use with the generated logo image.
     *
     * @param textColor The font color to use.
     */
    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    /**
     * Gets the text color type for this logo image, which is either a solid colour or
     * a gradient fill.
     *
     * @return The ColorType of the text color.
     */
    public ColorType getTextColorType() {
        return textColorType;
    }

    /**
     * Returns the GradientConfig for the text of this logo image, if one is in use.
     * See setTextColorType.
     *
     * @return A GradientConfig object for the text of this logo image.
     */
    public Gradient getTextGradient() {
        return textGradient;
    }

    /**
     * Sets the text color type for this logo image, which is either a solid colour or
     * a gradient fill.
     *
     * @param textColorType A ColorType to use with for the text color of this logo image.
     */
    public void setTextColorType(ColorType textColorType) {
        this.textColorType = textColorType;
    }

    /**
     * Sets the gradient type for text color of the image. This property is ignored
     * unless the TextColorType is set to GRADIENT.
     *
     * @param textGradient The GradientConfig to use with for the text of this object.
     */
    public void setTextGradient(Gradient textGradient) {
        this.textGradient = textGradient;
    }

    /**
     * Sets the desired font point size to use with the generated image. If this method
     * is invoked to manually set the font point size, then auto-sizing of the text
     * is automatically turned off. You can re-enable auto-sizing by invoking
     * setAutoSize(true), in which case this font point size will be ignored
     * (but will be stored for later use in case you do setAutoSize(false)).
     *
     * @param fontPointSize The desired font point size. If 0, we set autoSize to true.
     */
    public void setFontPointSize(int fontPointSize) {
        if (fontPointSize <= 0) {
            autoSize = true;
        }
        else {
            font = font.deriveFont((float)fontPointSize);
        }
    }

    /**
     * Indicates whether auto font sizing is enabled. If true, fontPointSize is ignored, and instead,
     * the text will be rendered in a size appropriate to the dimensions of the image.
     *
     * @return Whether or not font auto-sizing is in use.
     */
    public boolean isAutoSize() {
        return autoSize;
    }

    /**
     * Enables or disables font auto-sizing on the generated image. If disabled, then
     * the size in our Font property will be used to determine the font size.
     *
     * @param auto Whether or not to auto-size the text to fit the image.
     */
    public void setAutoSize(boolean auto) {
        autoSize = auto;
    }

    /**
     * Resets this configuration object to reasonable default values.
     */
    public void resetToDefaults() {
        this.bgColorType = ColorType.SOLID;
        this.bgColor = Color.BLACK;
        this.bgGradient = Gradient.createDefault();
        this.borderColorType = ColorType.SOLID;
        this.borderColor = Color.GRAY;
        this.borderGradient = Gradient.createDefault();
        this.textColorType = ColorType.SOLID;
        this.textColor = Color.WHITE;
        this.textGradient = Gradient.createDefault();
        this.borderWidth = 1;
        this.hasBorder = true;
        this.autoSize = false;
        this.font = new Font(Font.SERIF, Font.PLAIN, 42);
        this.logoWidth = 200;
        this.logoHeight = 150;
        this.yTweak = 0;
    }

    @Override
    public void saveToProps(Properties props) {
        String pfx = fullyQualifiedName + ".";

        props.setString(pfx + "bgColorType", bgColorType.name());
        saveGradientToProps(props, pfx + "bgGradient", bgGradient);
        props.setColor(pfx + "bgColor", bgColor);
        props.setString(pfx + "borderColorType", borderColorType.name());
        props.setColor(pfx + "borderColor", borderColor);
        saveGradientToProps(props, pfx + "borderGradient", borderGradient);
        props.setString(pfx + "textColorType", textColorType.name());
        props.setColor(pfx + "textColor", textColor);
        saveGradientToProps(props, pfx + "textGradient", textGradient);
        props.setInteger(pfx + "borderWidth", borderWidth);
        props.setBoolean(pfx + "hasBorder", hasBorder);
        props.setFont(pfx + "font", font);
        props.setInteger(pfx + "width", logoWidth);
        props.setInteger(pfx + "height", logoHeight);
        props.setInteger(pfx + "yTweak", yTweak);
        props.setBoolean(pfx + "autoSize", autoSize);
    }

    private void saveGradientToProps(Properties props, String name, Gradient gradient) {
        props.setString(name + ".gradientType", gradient.type().name());
        props.setColor(name + ".color1", gradient.color1());
        props.setColor(name + ".color2", gradient.color2());
    }

    @Override
    public void loadFromProps(Properties props) {
        String pfx = fullyQualifiedName + ".";
        resetToDefaults(); // to provide sensible defaults for any value not specified in props

        bgColorType = ColorType.valueOf(props.getString(pfx + "bgColorType", bgColorType.name()));
        bgColor = props.getColor(pfx + "bgColor", bgColor);
        bgGradient = loadGradientFromProps(props, pfx + "bgGradient");
        borderColorType = ColorType.valueOf(props.getString(pfx + "borderColorType", borderColorType.name()));
        borderColor = props.getColor(pfx + "borderColor", borderColor);
        borderGradient = loadGradientFromProps(props, pfx + "borderGradient");
        textColorType = ColorType.valueOf(props.getString(pfx + "textColorType", textColorType.name()));
        textColor = props.getColor(pfx + "textColor", textColor);
        textGradient = loadGradientFromProps(props, pfx + "textGradient");
        borderWidth = props.getInteger(pfx + "borderWidth", borderWidth);
        hasBorder = props.getBoolean(pfx + "hasBorder", hasBorder);
        font = props.getFont(pfx + "font", font);
        logoWidth = props.getInteger(pfx + "width", logoWidth);
        logoHeight = props.getInteger(pfx + "height", logoHeight);
        yTweak = props.getInteger(pfx + "yTweak", yTweak);
        autoSize = props.getBoolean(pfx + "autoSize", autoSize);
    }

    private Gradient loadGradientFromProps(Properties props, String pfx) {
        GradientType gradientType = GradientType.valueOf(
                props.getString(pfx + ".gradientType", bgGradient.type().name()));
        Color gradientColor1 = props.getColor(pfx + ".color1", bgGradient.color1());
        Color gradientColor2 = props.getColor(pfx + ".color2", bgGradient.color2());
        return new Gradient(gradientType, gradientColor1, gradientColor2);
    }

    @Override
    protected FormField generateFormFieldImpl() {
        LogoFormField formField = new LogoFormField(propertyLabel);
        formField.setIdentifier(fullyQualifiedName);
        formField.setBackgroundColor(bgColorType == ColorType.SOLID ? bgColor : bgGradient);
        formField.setBorderColor(borderColorType == ColorType.SOLID ? borderColor : borderGradient);
        formField.setTextColor(textColorType == ColorType.SOLID ? textColor : textGradient);
        formField.setBorderWidth(hasBorder ? borderWidth : 0);
        formField.setFontAutoScale(autoSize);
        formField.setImageWidth(logoWidth);
        formField.setImageHeight(logoHeight);
        formField.setYTweak(yTweak);
        formField.setSelectedFont(font);

        return formField;
    }

    @Override
    public void loadFromFormField(FormField field) {
        if (field.getIdentifier() == null
                || !field.getIdentifier().equals(fullyQualifiedName)
                || !(field instanceof LogoFormField logoField)) {
            logger.log(Level.SEVERE, "LogoProperty.loadFromFormField: received the wrong field \"{0}\"",
                       field.getIdentifier());
            return;
        }

        if (!logoField.isValid()) {
            logger.log(Level.WARNING, "LogoProperty.loadFromFormField: received invalid form field \"{0}\"",
                       field.getIdentifier());
            return;
        }

        bgColorType = logoField.getBackgroundColor() instanceof Color ? ColorType.SOLID : ColorType.GRADIENT;
        if (bgColorType == ColorType.SOLID) {
            bgColor = (Color)logoField.getBackgroundColor();
        }
        else {
            bgGradient = (Gradient)logoField.getBackgroundColor();
        }

        borderColorType = logoField.getBorderColor() instanceof Color ? ColorType.SOLID : ColorType.GRADIENT;
        if (borderColorType == ColorType.SOLID) {
            borderColor = (Color)logoField.getBorderColor();
        }
        else {
            borderGradient = (Gradient)logoField.getBorderColor();
        }

        textColorType = logoField.getTextColor() instanceof Color ? ColorType.SOLID : ColorType.GRADIENT;
        if (textColorType == ColorType.SOLID) {
            textColor = (Color)logoField.getTextColor();
        }
        else {
            textGradient = (Gradient)logoField.getTextColor();
        }

        borderWidth = logoField.getBorderWidth();
        hasBorder = borderWidth > 0;

        autoSize = logoField.isFontAutoScale();
        font = logoField.getSelectedFont();
        logoWidth = logoField.getImageWidth();
        logoHeight = logoField.getImageHeight();
        yTweak = logoField.getYTweak();
    }
}
