package ca.corbett.extras.logging;

import ca.corbett.extras.properties.AbstractProperty;
import ca.corbett.extras.properties.Properties;
import ca.corbett.forms.fields.FormField;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A LogConsoleTheme represents a set of LogConsoleStyle objects that can be applied
 * within the LogConsole.
 * <h2>Styles</h2>
 * Every LogConsoleTheme contains at minimum one LogConsoleStyle -
 * the "Default" style can be customized but cannot be removed. If no other styles are
 * specified, then all log messages in the LogConsole will be displayed in that default
 * style when used with this theme.
 * <p>
 * You can specify additional styles either by selecting one of the pre-built themes
 * via the create...() factory methods in this class, or by specifying them manually
 * by creating and configuring one or more LogConsoleStyle instances and handing them
 * to this theme via the setStyle() method. There is no limit to the number of styles
 * a theme can have, but note the matching rules below.
 * </p>
 * <h2>Matching styles to log messages</h2>
 * The LogConsole will ask the current theme for a matching LogConsoleStyle to use
 * for each log message that comes in. Style matching is done either by matching
 * a log Level, or by matching a string token that appears in a log message, or both.
 * <ul>
 * <li>If a log message matches a style's log token and its log level, that style is considered
 * a strong match for that log message.</li>
 * <li>If a log message matches a style's log token, but the style doesn't specify a log level,
 * it is still considered a strong match.</li>
 * <li>If a log message matches a style's log level, but the style doesn't specify a log token,
 * then this is considered a weak match. That means that this style will match only if no
 * other style matches the log message.</li>
 * <li>If no style matches, either by log token or by log level, then the default style
 * will be used for that log message.</li>
 * </ul>
 * <h2>Persisting a custom style</h2>
 * LogConsoleTheme (and also LogConsoleStyle) extend ConfigObject, so they can be easily
 * persisted to disk using a FileBasedProperties instance. It is recommended to use a
 * prefix when doing so to differentiate different styles in the same properties file.
 * <h2>Listening for style changes</h2>
 * LogConsoleTheme accepts ChangeListeners, and will send out a change message whenever
 * any style properties are changed, or when styles are added or removed.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2023-03-17
 */
public final class LogConsoleTheme extends AbstractProperty implements ChangeListener {

    public static final String DEFAULT_STYLE_NAME = "Default";

    private static final Logger logger = Logger.getLogger(LogConsoleTheme.class.getName());
    private final List<ChangeListener> changeListeners = new ArrayList<>();
    private final Map<String, LogConsoleStyle> logStyles = new HashMap<>();
    private Color defaultBgColor;

    /**
     * Creates a plain theme with black text on a white background, and no
     * styled matchers for any particular log messages. This is equivalent
     * to calling LogConsoleTheme.createPlainTheme(), but you may also want
     * to look at the static factory methods in this class to create something
     * with some preset styling options already set.
     */
    public LogConsoleTheme(String fullyQualifiedName) {
        super(fullyQualifiedName, "");
        clear();
    }

    /**
     * Clears all log styles except for the Default style, and resets the
     * default background color to white.
     */
    public void clear() {
        logStyles.clear();
        defaultBgColor = Color.WHITE;
        LogConsoleStyle defaultStyle = new LogConsoleStyle();
        defaultStyle.addChangeListener(this);
        logStyles.put(DEFAULT_STYLE_NAME, defaultStyle);
    }

    /**
     * Creates and returns a plain theme with black text and no matchers.
     * This is equivalent to just doing a new LogConsoleTheme() but is provided
     * for consistency with other static creator methods.
     *
     * @return A new, default, rather boringly unstyled LogConsoleTheme.
     */
    public static LogConsoleTheme createPlainTheme() {
        return new LogConsoleTheme("Plain");
    }

    /**
     * Creates and returns a LogConsoleTheme with some basic log matchers.
     * Most log messages will be written in plain black text, but messages
     * of level WARNING will be written in bold orange text, and messages
     * of level SEVERE will be written in bold red text.
     *
     * @return A LogConsoleTheme with a default styling (black on white).
     */
    public static LogConsoleTheme createDefaultStyledTheme() {
        LogConsoleTheme theme = new LogConsoleTheme(DEFAULT_STYLE_NAME);

        LogConsoleStyle warningStyle = new LogConsoleStyle();
        warningStyle.addChangeListener(theme);
        warningStyle.setLogLevel(Level.WARNING);
        warningStyle.setIsBold(true);
        warningStyle.setFontColor(Color.ORANGE);
        theme.setStyle("Warnings", warningStyle);

        LogConsoleStyle errorStyle = new LogConsoleStyle();
        errorStyle.addChangeListener(theme);
        errorStyle.setLogLevel(Level.SEVERE);
        errorStyle.setIsBold(true);
        errorStyle.setFontColor(Color.RED);
        theme.setStyle("Errors", errorStyle);

        return theme;
    }

    /**
     * Creates and returns a "matrix"-style LogConsoleTheme, with a black background
     * and green text. Also includes highlighting of errors and warnings.
     *
     * @return A LogConsoleTheme with a "matrix" styling (green on black).
     */
    public static LogConsoleTheme createMatrixStyledTheme() {
        LogConsoleTheme theme = new LogConsoleTheme("Matrix");
        theme.defaultBgColor = Color.BLACK;
        theme.getStyle(DEFAULT_STYLE_NAME).setFontColor(Color.GREEN);

        LogConsoleStyle style = new LogConsoleStyle();
        style.addChangeListener(theme);
        style.setLogLevel(Level.WARNING);
        style.setIsBold(true);
        style.setFontColor(Color.YELLOW);
        theme.setStyle("Warnings", style);

        style = new LogConsoleStyle();
        style.addChangeListener(theme);
        style.setLogLevel(Level.SEVERE);
        style.setIsBold(true);
        style.setFontColor(Color.RED);
        theme.setStyle("Errors", style);

        return theme;
    }

    /**
     * Creates and returns a "paper" style LogConsoleTheme, with mostly black text
     * on a grey background. Also includes highlighting of errors and warnings.
     *
     * @return A LogConsoleTheme with a "paper" styling (black on grey).
     */
    public static LogConsoleTheme createPaperStyledTheme() {
        LogConsoleTheme theme = new LogConsoleTheme("Paper");
        theme.defaultBgColor = Color.LIGHT_GRAY; // literally only difference from default

        LogConsoleStyle warningStyle = new LogConsoleStyle();
        warningStyle.addChangeListener(theme);
        warningStyle.setLogLevel(Level.WARNING);
        warningStyle.setIsBold(true);
        warningStyle.setFontColor(Color.ORANGE);
        theme.setStyle("Warnings", warningStyle);

        LogConsoleStyle errorStyle = new LogConsoleStyle();
        errorStyle.addChangeListener(theme);
        errorStyle.setLogLevel(Level.SEVERE);
        errorStyle.setIsBold(true);
        errorStyle.setFontColor(Color.RED);
        theme.setStyle("Errors", errorStyle);

        return theme;
    }

    /**
     * Register to receive notifications when any style properties are changed, added,
     * or removed from this theme.
     *
     * @param listener The listener to add.
     */
    public void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }

    /**
     * Unregister from receiving notifications when style properties are changed, added,
     * or removed from this theme.
     *
     * @param listener The listener to add.
     */
    public void removeChangeListener(ChangeListener listener) {
        changeListeners.remove(listener);
    }

    /**
     * Sets the default background color - this is the background that will be used
     * unless specifically overridden by some style matcher.
     *
     * @param color The new default background color. Ignored if null.
     */
    public void setDefaultBgColor(Color color) {
        if (color != null) {
            defaultBgColor = color;
            fireChangeEvent();
        }
    }

    /**
     * Returns the default background color - this is the background that will be used
     * unless specifically overridden by some style matcher.
     *
     * @return The default background color.
     */
    public Color getDefaultBgColor() {
        return defaultBgColor;
    }

    /**
     * As a shortcut, it's possible to set the font point size of all style matchers
     * with one call, instead of one by one.
     *
     * @param size The new font point size to apply to all styles in this theme.
     */
    public void setFontPointSize(int size) {
        for (LogConsoleStyle style : logStyles.values()) {
            style.setFontPointSize(size);
        }
        fireChangeEvent();
    }

    /**
     * Sets or replaces the style with the given name. If the given style is null,
     * then this method will defer to removeStyle(name).
     *
     * @param style The style object to set. If null, will invoke removeStyle(name).
     */
    public void setStyle(String name, LogConsoleStyle style) {
        if (style == null) {
            removeStyle(name);
        }
        else {
            logStyles.put(name, style);
            style.addChangeListener(this);
        }
        fireChangeEvent();
    }

    /**
     * Returns the LogConsoleStyle of the given name, if it exists.
     *
     * @param name The name of the style in question.
     * @return A LogConsolStyle object, or null if name not found.
     */
    public LogConsoleStyle getStyle(String name) {
        return logStyles.get(name);
    }

    /**
     * Returns a list of all style names contained in this theme. This list is guaranteed
     * to always contain at least one name ("Default"). The "Default" entry is always
     * returned in position 0 in the list. The remainder of the list is sorted alphabetically.
     *
     * @return A list of all style names contained in this theme.
     */
    public List<String> getStyleNames() {
        List<String> list = new ArrayList<>();
        list.add(DEFAULT_STYLE_NAME);

        SortedSet<String> set = new TreeSet<>();
        for (String name : logStyles.keySet()) {
            if (!DEFAULT_STYLE_NAME.equals(name)) {
                set.add(name);
            }
        }
        list.addAll(set);

        return list;
    }

    /**
     * Returns the name of the given style, if it exists within this theme.
     *
     * @param style A LogConsoleStyle instance.
     * @return The name of the given LogConsoleStyle if it is present, else null.
     */
    public String getStyleName(LogConsoleStyle style) {
        if (style == null) {
            return null;
        }

        String foundName = null;
        for (String name : getStyleNames()) {
            if (style == logStyles.get(name)) {
                foundName = name;
                break;
            }
        }

        return foundName;
    }

    /**
     * Removes the style with the given name, if it exists. If the given name is null,
     * or if you try to remove the "Default" style, an IllegalArgumentException will be thrown.
     *
     * @param name The name of the style to remove.
     * @throws IllegalArgumentException If name is null or "Default"
     */
    public void removeStyle(String name) throws IllegalArgumentException {
        if (name == null) {
            throw new IllegalArgumentException("Attempted to remove null style.");
        }
        if (name.equals(DEFAULT_STYLE_NAME)) {
            throw new IllegalArgumentException("Attempted to remove default style.");
        }
        LogConsoleStyle style = logStyles.get(name);
        if (style != null) {
            style.removeChangeListener(this);
            logStyles.remove(name);
            fireChangeEvent();
        }
    }

    /**
     * Returns the appropriate LogConsoleStyle within this theme for the given log
     * message and log level. If no style within this theme is a match, then the default
     * theme is returned.
     *
     * @param logMsg   The log message in question.
     * @param logLevel The Level at which this message was logged.
     * @return A LogConsoleStyle from this theme that matches the given parameters.
     */
    public LogConsoleStyle getMatchingStyle(String logMsg, Level logLevel) {
        LogConsoleStyle matchingStyle = null;

        List<String> styleNames = getStyleNames();
        for (String styleName : styleNames) {
            LogConsoleStyle style = logStyles.get(styleName);

            // Skip default style... this will be matched by default if nothing else hits:
            if (style == matchingStyle) {
                continue;
            }

            // If this style has no matchers, ignore it (this is what the default style is for):
            String styleLogToken = style.getLogToken() == null ? "" : style.getLogToken();
            Level styleLogLevel = style.getLogLevel();
            if (styleLogToken.isEmpty() && styleLogLevel == null) {
                continue;
            }

            // Adjust for case sensitivity if needed:
            String message = logMsg == null ? "" : logMsg;
            if (!style.isLogTokenCaseSensitive()) {
                message = message.toLowerCase();
                styleLogToken = styleLogToken.toLowerCase();
            }

            // If both are set, then both must match:
            if (!styleLogToken.isEmpty() && styleLogLevel != null) {
                if (styleLogLevel.equals(logLevel) && message.contains(styleLogToken)) {
                    matchingStyle = style; // unconditional overwrite of any previous match
                    break;
                }
            }

            // Check log token only:
            if (!styleLogToken.isEmpty()) {
                if (message.contains(styleLogToken)) {
                    matchingStyle = style; // unconditional overwrite of any previous match
                    break;
                }
            }

            // Check log level only:
            if (styleLogLevel != null) {
                if (styleLogLevel.equals(logLevel)) {
                    // This is a "weaker" match, so don't overwrite a previous match if one exists:
                    if (matchingStyle == null) {
                        matchingStyle = style;
                    }
                }
            }
        }

        return matchingStyle == null ? getStyle(DEFAULT_STYLE_NAME) : matchingStyle;
    }

    /**
     * Invoked internally when style properties are changed. We notify listeners
     * so they can update as needed.
     */
    private void fireChangeEvent() {
        for (ChangeListener listener : new ArrayList<>(changeListeners)) {
            ChangeEvent event = new ChangeEvent(this);
            listener.stateChanged(event);
        }
    }

    /**
     * Triggered when one of our style objects is changed. We simply pass this on
     * to whoever is listening to this theme.
     *
     * @param e A ChangeEvent
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        fireChangeEvent();
    }

    @Override
    public void saveToProps(Properties props) {
        String pfx = fullyQualifiedName + ".";
        props.setColor(pfx + "defaultBgColor", defaultBgColor);
        List<String> styleNames = getStyleNames();
        String nameList = String.join(",", styleNames);
        props.setString(pfx + "styleNames", nameList);
        for (String name : styleNames) {
            LogConsoleStyle style = logStyles.get(name);
            style.saveToProps(props, pfx + name + ".");
        }
    }

    @Override
    public void loadFromProps(Properties props) {
        clear();
        String pfx = fullyQualifiedName + ".";
        defaultBgColor = props.getColor(pfx + "defaultBgColor", defaultBgColor);
        String[] styleNames = props.getString(pfx + "styleNames", DEFAULT_STYLE_NAME).split(",");
        for (String name : styleNames) {
            LogConsoleStyle style = new LogConsoleStyle();
            style.loadFromProps(props, pfx + name + ".");
            logStyles.put(name, style);
        }
    }

    @Override
    protected FormField generateFormFieldImpl() {
        throw new UnsupportedOperationException("LogConsoleTheme does not support FormField generation.");
    }

    @Override
    public void loadFromFormField(FormField field) {
        throw new UnsupportedOperationException("LogConsoleTheme does not support FormField generation.");
    }
}
