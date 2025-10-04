package ca.corbett.extras.properties;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a wrapper around java's Properties class, with convenience methods that
 * abstract away the type conversions that callers would otherwise have to worry about.
 * Specifically, the java.util.Properties class handles everything as Strings, which is
 * great for simple name/value pairs, but when you want to start storing integers, booleans,
 * colour values, and etc, you have to worry about converting everything to and from
 * Strings. This class abstracts that for you and provides easy convenience methods.
 *
 * NOTE: All setters in this wrapper guard against null values so that we never pass a
 * null into java.util.Properties#setProperty, which would otherwise cause a
 * NullPointerException in the underlying Hashtable.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2022-05-10
 */
public class Properties {

    private static final Logger logger = Logger.getLogger(Properties.class.getName());

    protected final java.util.Properties props;

    /**
     * Creates a new, empty Properties object.
     */
    public Properties() {
        this.props = new java.util.Properties();
    }

    /**
     * Returns a sorted list of property names currently stored in this Properties object.
     * Implementation note: a copy of the list is returned to avoid client modification headaches.
     *
     * @return A list of unique property names.
     */
    public List<String> getPropertyNames() {
        List<String> list = new ArrayList<>();
        for (Object key : props.keySet()) {
            list.add((String) key);
        }
        // Use explicit lambda to avoid any method reference binding issues on some toolchains.
        list.sort((a, b) -> a.compareTo(b));
        return list;
    }

    /**
     * Removes the property with the given name, if any.
     *
     * @param name The property name.
     */
    public void remove(String name) {
        props.remove(name);
    }

    /**
     * Sets a String name/value pair. Replaces any previous value for the given name.
     * If value is null, we do not write into the underlying Properties to avoid NPE.
     *
     * @param name  The property name.
     * @param value The property value.
     */
    public void setString(String name, String value) {
        if (value == null) {
            logger.log(Level.WARNING, "Ignoring null String value for property \"{0}\"", name);
            return;
        }
        props.setProperty(name, value);
    }

    /**
     * Retrieves the String value of the named property, if any.
     *
     * @param name         The property name.
     * @param defaultValue A value to receive if no such named property exists.
     * @return The value of the named property, or defaultValue if no such property exists.
     */
    public String getString(String name, String defaultValue) {
        return props.getProperty(name, defaultValue);
    }

    /**
     * Sets an Integer property. Replaces any previous value for the given property name.
     * If value is null, ignore (do not write).
     *
     * @param name  The property name.
     * @param value The property value.
     */
    public void setInteger(String name, Integer value) {
        if (value == null) {
            logger.log(Level.WARNING, "Ignoring null Integer value for property \"{0}\"", name);
            return;
        }
        props.setProperty(name, Integer.toString(value));
    }

    /**
     * Attempts to retrieve an Integer value for the named property.
     * If no such named property exists, or if the stored value cannot be converted to
     * an Integer, then defaultValue is returned.
     *
     * @param name         The property name.
     * @param defaultValue A value to receive if no such property exists.
     * @return The value of the named property, or defaultValue if no such property or not an int.
     */
    public Integer getInteger(String name, Integer defaultValue) {
        Integer value = defaultValue;
        try {
            value = Integer.valueOf(props.getProperty(name, Integer.toString(defaultValue)));
        } catch (NumberFormatException e) {
            logger.log(Level.SEVERE, "Property \"" + name + "\" contains a non integer value.", e);
        }
        return value;
    }

    /**
     * Sets a Boolean value for the given property name. Replaces any previous value for the
     * named property. If value is null, ignore.
     *
     * @param name  The property name.
     * @param value The property value.
     */
    public void setBoolean(String name, Boolean value) {
        if (value == null) {
            logger.log(Level.WARNING, "Ignoring null Boolean value for property \"{0}\"", name);
            return;
        }
        props.setProperty(name, Boolean.toString(value));
    }

    /**
     * Attempts to retrieve a Boolean value for the named property.
     * If no such named property exists, then defaultValue is returned.
     * Any of the following values will be considered valid boolean "true" values:
     * "true", "1", "yes", "on", or "enabled".
     * Any other property value will be considered "false".
     *
     * @param name         The property name.
     * @param defaultValue A value to receive if no such property exists.
     * @return The value of the named property, or defaultValue if no such property exists.
     */
    public Boolean getBoolean(String name, Boolean defaultValue) {
        Boolean value = defaultValue;
        String propValue = props.getProperty(name);
        if (propValue != null) {
            propValue = propValue.trim().toLowerCase();
            value = (propValue.equals("true")
                    || propValue.equals("1")
                    || propValue.equals("yes")
                    || propValue.equals("on")
                    || propValue.equals("enabled"));
        }
        return value;
    }

    /**
     * Sets a Float property. Replaces any previous value for the given property name.
     * If value is null, ignore.
     *
     * @param name  The property name.
     * @param value The property value.
     */
    public void setFloat(String name, Float value) {
        if (value == null) {
            logger.log(Level.WARNING, "Ignoring null Float value for property \"{0}\"", name);
            return;
        }
        props.setProperty(name, Float.toString(value));
    }

    /**
     * Attempts to retrieve a Float value for the named property.
     * If no such named property exists, or if the stored value cannot be converted to
     * a Float, then defaultValue is returned.
     *
     * @param name         The property name.
     * @param defaultValue A value to receive if no such property exists.
     * @return The value of the named property, or defaultValue if no such property or not a float.
     */
    public Float getFloat(String name, Float defaultValue) {
        Float value = defaultValue;
        try {
            value = Float.valueOf(props.getProperty(name, Float.toString(defaultValue)));
        } catch (NumberFormatException e) {
            logger.log(Level.SEVERE, "Property \"" + name + "\" contains a non-float value.", e);
        }
        return value;
    }

    /**
     * Sets a Double property. Replaces any previous value for the given property name.
     * If value is null, ignore.
     *
     * @param name  The property name.
     * @param value The property value.
     */
    public void setDouble(String name, Double value) {
        if (value == null) {
            logger.log(Level.WARNING, "Ignoring null Double value for property \"{0}\"", name);
            return;
        }
        props.setProperty(name, Double.toString(value));
    }

    /**
     * Attempts to retrieve a Double value for the named property.
     * If no such named property exists, or if the stored value cannot be converted to
     * a Double, then defaultValue is returned.
     *
     * @param name         The property name.
     * @param defaultValue A value to receive if no such property exists.
     * @return The value of the named property, or defaultValue if no such property or not a double.
     */
    public Double getDouble(String name, Double defaultValue) {
        Double value = defaultValue;
        try {
            value = Double.valueOf(props.getProperty(name, Double.toString(defaultValue)));
        } catch (NumberFormatException e) {
            logger.log(Level.SEVERE, "Property \"" + name + "\" contains a non-double value.", e);
        }
        return value;
    }

    /**
     * Sets a Color property. Replaces any previous value for the given property name.
     * Color values are written as Strings in the form "0xAARRGGBB".
     * If value is null, ignore.
     *
     * @param name  The property name.
     * @param value The property value.
     */
    public void setColor(String name, Color value) {
        if (value == null) {
            logger.log(Level.WARNING, "Ignoring null Color value for property \"{0}\"", name);
            return;
        }
        props.setProperty(name, encodeColor(value));
    }

    /**
     * Attempts to retrieve a Color value for the named property.
     * If no such named property exists, then defaultValue is returned.
     * Color values may be stored as Strings in the form "0xAARRGGBB", or
     * "0xRRGGBB" with no alpha value, or in the legacy format of
     * a simple integer representation of the rgb value (deprecated
     * but will still be read here).
     *
     * @param name         The property name
     * @param defaultValue The value to receive if no such property exists.
     * @return The value of the named property, or defaultValue if no such property exists.
     */
    public Color getColor(String name, Color defaultValue) {
        Color value = defaultValue;
        try {
            String propValue = props.getProperty(name);
            if (propValue != null && !propValue.isEmpty()) {
                value = decodeColor(propValue);
            }
        } catch (NumberFormatException e) {
            logger.log(Level.SEVERE, "Property \"" + name + "\" contains a non-colour value.", e);
        }
        return value;
    }

    /**
     * Sets a Font value, replacing any previous value with that name.
     * If value is null, ignore.
     * Internally, Font objects are split into five properties:
     * <ul>
     *     <li>font family name</li>
     *     <li>font face name</li>
     *     <li>is bold</li>
     *     <li>is italic</li>
     *     <li>point size</li>
     * </ul>
     * These are transparently read back into a single Font instance by the getFont() method.
     *
     * @param name  The name of the property to set.
     * @param value The Font object to set.
     */
    public void setFont(String name, Font value) {
        if (value == null) {
            logger.log(Level.WARNING, "Ignoring null Font value for property \"{0}\"", name);
            return;
        }
        props.setProperty(name + "_familyName", value.getFamily());
        props.setProperty(name + "_faceName", value.getFontName());
        props.setProperty(name + "_isBold", Boolean.toString(value.isBold()));
        props.setProperty(name + "_isItalic", Boolean.toString(value.isItalic()));
        props.setProperty(name + "_pointSize", Integer.toString(value.getSize()));
    }

    /**
     * Reads the various Font properties associated with the given name and returns a Font
     * object that encapsulates them. For example, getFont("myFont" ...) will actually
     * look for five different properties:
     * <ul>
     *     <li>myFont_familyName</li>
     *     <li>myFont_faceName</li>
     *     <li>myFont_isBold</li>
     *     <li>myFont_isItalic</li>
     *     <li>myFont_pointSize</li>
     * </ul>
     * This is transparent to the caller, as we simply return a Font object here.
     *
     * @param name         The property name of this font.
     * @param defaultValue A Font to use if the named property does not exist.
     * @return A Font object loaded from props, or defaultValue if something went wrong.
     */
    public Font getFont(String name, Font defaultValue) {

        // Wonky case: if defaultValue is null and our properties are not set,
        // then just return null:
        if (defaultValue == null && (
                props.getProperty(name + "_familyName", null) == null ||
                props.getProperty(name + "_faceName", null) == null ||
                props.getProperty(name + "_isBold", null) == null ||
                props.getProperty(name + "_isItalic", null) == null ||
                props.getProperty(name + "_pointSize", null) == null)) {
            return null;
        }

        Font font = defaultValue == null ? new Font("Serif", Font.PLAIN, 12) : defaultValue;
        try {
            String familyName = props.getProperty(name + "_familyName", font.getFamily());
            String faceName = props.getProperty(name + "_faceName", font.getFontName());
            boolean isBold = getBoolean(name + "_isBold", font.isBold());
            boolean isItalic = getBoolean(name + "_isItalic", font.isItalic());
            int fontStyle = Font.PLAIN;
            if (isBold) {
                fontStyle |= Font.BOLD;
            }
            if (isItalic) {
                fontStyle |= Font.ITALIC;
            }
            int pointSize = Integer.parseInt(props.getProperty(name + "_pointSize", Integer.toString(font.getSize())));
            font = new Font(faceName, fontStyle, pointSize);
        } catch (NumberFormatException nfe) {
            logger.log(Level.SEVERE, "Property \"" + name + "\" contains a non-font value.", nfe);
        }
        return font;
    }

    /**
     * Encodes the given Color to a String in the format 0xAARRGGBB, where
     * AA is the alpha value from 00 to FF, RR is the red value, GG is the
     * green value, and BB is the blue value.
     */
    public static String encodeColor(Color color) {
        return "0x" + Integer.toHexString(color.getRGB());
    }

    /**
     * Attempts to decode a Color value from the given String. The formats accepted are:
     * <ul>
     *     <li>"0xAARRGGBB" (alpha, red, green blue)</li>
     *     <LI>"0xRRGGBB" (red, green, blue with implied full opacity)</LI>
     *     <LI>an integer rgb value as received from Color.getRGB()</LI>
     * </ul>
     * If the input is null or empty, you'll get null. You may also get a NumberFormatException
     * if the given string makes no sense.
     */
    public static Color decodeColor(String input) throws NumberFormatException {
        if (input == null || input.isBlank()) {
            return null;
        }

        if (input.toLowerCase().startsWith("0x")) {
            // alpha values: 0xAARRGGBB
            if (input.length() == 10) {
                return new Color(Long.decode(input).intValue(), true);
            }
            // regular values: 0xRRGGBB with no alpha value (fully opaque)
            else {
                return new Color(Long.decode(input).intValue());
            }
        }
        else {
            // backwards compatibility... we used to just take color.getRGB() as an int value
            return new Color(Integer.parseInt(input));
        }
    }

    /**
     * Translate between discrete font style properties and the weird way
     * that java.awt.Font represents them.
     */
    public static Font createFontFromAttributes(String fontFamilyName, boolean isBold, boolean isItalic, int pointSize) {
        int fontStyle = Font.PLAIN;
        if (isBold) {
            fontStyle |= Font.BOLD;
        }
        if (isItalic) {
            fontStyle |= Font.ITALIC;
        }
        return new Font(fontFamilyName, fontStyle, pointSize);
    }
}
