package ca.corbett.extras.logging;

import ca.corbett.extras.config.ConfigObject;
import ca.corbett.extras.properties.Properties;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents configuration options for a specific LogConsole message style.
 * A "Style" here refers to the font and colour options for specific types
 * of log messages. LogConsoleStyles are grouped together into a
 * LogConsoleTheme, which contains one or more styles.
 * <p>
 * A LogConsoleStyle consists of either a log Level or a log string token, or both.
 * Styles associated with a specific log Level will be applied to any message
 * that is logged at that level (example: all INFO messages or all WARNING messages).
 * Styles associated with a string token will be applied to any log message that
 * contains that string token (example: "error" or "failure"). If both a Level and
 * a string token are supplied, then they must both match in order to apply this style.
 * </p>
 * <p>
 * If a log message doesn't meet the criteria of any given Style within a Theme, then
 * it is styled using the "Default" style (which can be customized but not removed
 * from the LogConsoleTheme).
 * </p>
 * <p>
 * If more than one style in a given theme matches a given log message, a matching
 * algorithm is applied as described in LogConsoleTheme.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2023-03-17
 */
public final class LogConsoleStyle implements ConfigObject {

    private static final Logger logger = Logger.getLogger(LogConsoleStyle.class.getName());

    private final List<ChangeListener> changeListeners = new ArrayList<>();
    private Level logLevel;
    private String logToken;
    private boolean logTokenIsCaseSensitive;
    private String fontFamilyName;
    private Color fontColor;
    private Color fontBgColor;
    private boolean isBold;
    private boolean isItalic;
    private boolean isUnderline;
    private int fontPointSize;

    /**
     * Creates a style with all default properties and no matchers.
     */
    public LogConsoleStyle() {
        setDefaults();
    }

    /**
     * Returns the Level matcher - that is, the log level to which this style will be applied.
     * A value of Level.ALL means the style will be applied to all log messages. A value of
     * null (the default) means this style will not be applied based on log Level.
     *
     * @return A Level value, or null if not Level matcher is to be applied.
     */
    public Level getLogLevel() {
        return logLevel;
    }

    /**
     * Sets the Level matcher to use - that is, the log level to which this style will be applied.
     * A value of Level.ALL means the style will be applied to all log messages. A value of
     * null means this style will not be applied based on log Level.
     *
     * @param logLevel A Level value as described above.
     */
    public void setLogLevel(Level logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * Returns the log token matcher - that is, a string to search for in log messages to
     * see if this style should be applied. A value of null (the default) means that
     * this style will not be applied based on log message contents.
     *
     * @return A String as described above.
     */
    public String getLogToken() {
        return logToken;
    }

    /**
     * Sets the log token matcher - that is, a string to search for in log messages to
     * see if this style should be applied. A value of null means that
     * this style will not be applied based on log message contents.
     *
     * @param logToken        A String as described above, or null.
     * @param isCaseSensitive whether or not to treat logToken in a case sensitive manner.
     */
    public void setLogToken(String logToken, boolean isCaseSensitive) {
        this.logToken = logToken == null ? "" : logToken;
        this.logTokenIsCaseSensitive = isCaseSensitive;
    }

    public boolean isLogTokenCaseSensitive() {
        return logTokenIsCaseSensitive;
    }

    /**
     * Returns the font colour associated with this style.
     *
     * @return A Color object, will never be null.
     */
    public Color getFontColor() {
        return fontColor;
    }

    /**
     * Sets the font colour associated with this style.
     *
     * @param fontColor A Color object. Null values are ignored.
     */
    public void setFontColor(Color fontColor) {
        if (fontColor != null) {
            this.fontColor = fontColor;
        }
    }

    /**
     * Returns the font background colour for this style, or null if one is not set.
     * If the value is null, the background colour of the theme itself will be applied.
     *
     * @return A Color object, or null.
     */
    public Color getFontBgColor() {
        return fontBgColor;
    }

    /**
     * Sets a background colour for this style. If null is specified, the background
     * colour of the theme itself will be used.
     *
     * @param fontBgColor A Color object, or null to fall back to the theme background color.
     */
    public void setFontBgColor(Color fontBgColor) {
        this.fontBgColor = fontBgColor;
    }

    /**
     * Due to limitations in JTextPane, we can only accept a font "family" name instead
     * of a specific font name. Allowable values are "MonoSpaced", "Serif", or
     * "SansSerif" (case sensitive).
     *
     * @param family A font family name as described above.
     */
    public void setFontFamilyName(String family) {
        switch (family) {
            case "MonoSpaced":
            case "Serif":
            case "SansSerif":
                fontFamilyName = family;
                break;

            default:
                fontFamilyName = "MonoSpaced"; // safe default
        }
    }

    /**
     * Returns the font family name currently in effect for this style.
     *
     * @return One of "MonoSpaced", "Serif", or "SansSerif"
     */
    public String getFontFamilyName() {
        return fontFamilyName;
    }

    public boolean isBold() {
        return isBold;
    }

    public void setIsBold(boolean isBold) {
        this.isBold = isBold;
    }

    public boolean isItalic() {
        return isItalic;
    }

    public void setIsItalic(boolean isItalic) {
        this.isItalic = isItalic;
    }

    public boolean isUnderline() {
        return isUnderline;
    }

    public void setIsUnderline(boolean isUnderline) {
        this.isUnderline = isUnderline;
    }

    public int getFontPointSize() {
        return fontPointSize;
    }

    public void setFontPointSize(int fontPointSize) {
        this.fontPointSize = fontPointSize;
    }

    /**
     * Register to receive notifications when any style properties are changed.
     *
     * @param listener The listener to add.
     */
    public void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }

    /**
     * Unregister from receiving notifications when style properties are changed.
     *
     * @param listener The listener to add.
     */
    public void removeChangeListener(ChangeListener listener) {
        changeListeners.remove(listener);
    }

    @Override
    public void loadFromProps(Properties props, String prefix) {
        setDefaults();
        String pfx = (prefix == null) ? "" : prefix;
        String levelName = props.getString(pfx + "logLevel", "");
        try {
            logLevel = levelName.isEmpty() ? null : Level.parse(levelName);
        }
        catch (IllegalArgumentException iae) {
            logLevel = null;
            logger.log(Level.SEVERE, "Unknown log level name found in properties: {0}", levelName);
        }
        logToken = props.getString(pfx + "logToken", logToken);
        logTokenIsCaseSensitive = props.getBoolean(pfx + "logTokenIsCaseSensitive", logTokenIsCaseSensitive);
        fontColor = props.getColor(pfx + "fontColor", fontColor);
        boolean hasFontBgColor = props.getBoolean(pfx + "fontBgColorIsSet", (fontBgColor != null));
        fontBgColor = hasFontBgColor ? props.getColor(pfx + "fontBgColor", fontBgColor) : null;
        fontFamilyName = props.getString(pfx + "fontFamilyName", fontFamilyName);
        isBold = props.getBoolean(pfx + "isBold", isBold);
        isItalic = props.getBoolean(pfx + "isItalic", isItalic);
        isUnderline = props.getBoolean(pfx + "isUnderline", isUnderline);
        fontPointSize = props.getInteger(pfx + "fontPointSize", fontPointSize);
    }

    @Override
    public void saveToProps(Properties props, String prefix) {
        String pfx = (prefix == null) ? "" : prefix;
        props.setString(pfx + "logLevel", logLevel != null ? logLevel.getName() : "");
        props.setString(pfx + "logToken", logToken);
        props.setBoolean(pfx + "logTokenIsCaseSensitive", logTokenIsCaseSensitive);
        props.setColor(pfx + "fontColor", fontColor);
        props.setBoolean(pfx + "fontBgColorIsSet", (fontBgColor != null));
        if (fontBgColor != null) {
            props.setColor(pfx + "fontBgColor", fontBgColor);
        }
        props.setString(pfx + "fontFamilyName", fontFamilyName);
        props.setBoolean(pfx + "isBold", isBold);
        props.setBoolean(pfx + "isItalic", isItalic);
        props.setBoolean(pfx + "isUnderline", isUnderline);
        props.setInteger(pfx + "fontPointSize", fontPointSize);
    }

    private void setDefaults() {
        logLevel = null;
        logToken = "";
        logTokenIsCaseSensitive = false;
        fontColor = Color.BLACK;
        fontFamilyName = "MonoSpaced";
        fontBgColor = null;
        isBold = false;
        isItalic = false;
        isUnderline = false;
        fontPointSize = 12;
    }

    private void fireChangeEvent() {
        for (ChangeListener listener : changeListeners) {
            ChangeEvent event = new ChangeEvent(this);
            listener.stateChanged(event);
        }
    }

}
