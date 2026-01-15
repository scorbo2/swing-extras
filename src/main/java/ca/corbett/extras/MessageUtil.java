package ca.corbett.extras;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple utility class to show messages to the user via JOptionPane while also simultaneously
 * logging them. This saves the caller from having to make two separate calls each time
 * an error comes up.
 *
 * <H2>General logging and messaging</H2>
 * <p>
 * <b>Example usage:</b></p>
 * <pre>
 *     MessageUtil messageUtil = new MessageUtil(this, myLogger);
 *     messageUtil.error("Something went horribly wrong", someException);
 * </pre>
 * This will show an error dialog to the user, and also log the message string to the
 * given Logger. It is equivalent to the following code:
 * <pre>
 *     JOptionPane.showMessageDialog(this, "Something went horribly wrong",
 *             "Error", JOptionPane.ERROR_MESSAGE);
 *     myLogger.log(Level.SEVERE, "Something went horribly wrong", someException);
 * </pre>
 * <p>
 * Both the given parent component and the given Logger are optional. If no parent
 * component is given, dialogs will have no parent. If no Logger is given, no logging
 * will occur.
 * </p>
 * <h2>Asking questions</h2>
 * <p>
 *     MessageUtil has shorthand methods for asking simple yes/no or
 *     yes/no/cancel questions via JOptionPane. These methods do not perform any logging. For example:
 * </p>
 * <pre>
 *     int result = messageUtil.askYesNo("Confirm Delete",
 *              "Are you sure you want to delete this file?");
 *     if (result == MessageUtil.YES) {
 *         // User clicked Yes
 *         // ...
 *     }
 * </pre>
 * <p>
 *     This saves a small amount of code and improves readability.
 * </p>
 *
 * <h2>Getting user input</h2>
 * <p>
 *     MessageUtil also has shorthand methods for asking the user to select from a list of options,
 *     or to enter free-form text input. These, once again, are wrappers around JOptionPane methods
 *     that save a small amount of code and improve readability. Examples:
 * </p>
 * <pre>
 *     // Get free-text entry with some default value already filled in:
 *     String color = messageUtil.askText("Select Color",
 *                                  "Enter your favorite color:",
 *                                  "Blue");
 *     if (color != null) {
 *         // User made a selection (null would mean they cancelled)
 *     }
 *
 *     // Get selection from a list of options:
 *     String[] options = {"Red", "Green", "Blue"};
 *     String color = messageUtil.askSelect("Select Color",
 *                              "Choose your favorite color:",
 *                              options,
 *                              "Green");
 *     if (color != null) {
 *         // User made a selection (null would mean they cancelled)
 *     }
 * </pre>
 * <p>
 *     Nothing is logged when asking for user input.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2022-04-11
 */
public final class MessageUtil {

    public static final int YES = JOptionPane.YES_OPTION;
    public static final int NO = JOptionPane.NO_OPTION;
    public static final int CANCEL = JOptionPane.CANCEL_OPTION;

    private final Component parent;
    private final Logger logger;
    private String defaultQuestionTitle = "Confirm";
    private String defaultErrorTitle = "Error";
    private String defaultWarningTitle = "Warning";
    private String defaultInfoTitle = "Info";
    private String defaultInputTitle = "Select";

    /**
     * Creates a MessageUtil with the given parent Component.
     *
     * @param parent The parent Component to be used with message dialogs.
     */
    public MessageUtil(Component parent) {
        this(parent, null);
    }

    /**
     * Creates a MessageUtil with the given parent Component and the given Logger.
     *
     * @param parent The parent Component to be used with message dialogs.
     * @param logger The Logger which will receive INFO and SEVERE messages.
     */
    public MessageUtil(Component parent, Logger logger) {
        this.parent = parent;
        this.logger = logger;
    }

    /**
     * Returns the configured parent Component, if one is set. May be null.
     *
     * @return The parent Component, or null.
     */
    public Component getParent() {
        return parent;
    }

    /**
     * Returns the Logger currently being used. May be null if none was set.
     *
     * @return A Logger instance, or null.
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Used to specify a default dialog title in the case where error() is invoked
     * without a given title. Does not affect logging. If unset, the default is "Error".
     *
     * @param title The new title to use for error dialogs.
     */
    public void setDefaultErrorTitle(String title) {
        defaultErrorTitle = title;
    }

    /**
     * Used to specify a default dialog title in the case where warning() is invoked
     * without a given title. Does not affect logging. If unset, the default is "Warning".
     *
     * @param title The new title to use for warning dialogs.
     */
    public void setDefaultWarningTitle(String title) {
        defaultWarningTitle = title;
    }

    /**
     * Used to specify a default dialog title in the case where info() is invoked
     * without a given title. Does not affect logging. If unset, the default is "Info".
     *
     * @param title The new title to use for info dialogs.
     */
    public void setDefaultInfoTitle(String title) {
        defaultInfoTitle = title;
    }

    /**
     * Used to specify a default dialog title in the case where askYesNo() or
     * askYesNoCancel() is invoked without a given title. If unset, the default is "Confirm".
     *
     * @param title The new title to use for question dialogs.
     */
    public void setDefaultQuestionTitle(String title) {
        defaultQuestionTitle = title;
    }

    /**
     * Used to specify a default dialog title in the case where input dialogs are invoked
     * without a given title. If unset, the default is "Select".
     *
     * @param title The new title to use for input dialogs.
     */
    public void setDefaultInputTitle(String title) {
        defaultInputTitle = title;
    }

    /**
     * Shows an error message to the user, and also logs it if a Logger was provided.
     *
     * @param message The error message to show.
     */
    public void error(String message) {
        error(defaultErrorTitle, message);
    }

    /**
     * Shows an error message to the user, and also logs it if a Logger was provided.
     *
     * @param title   The title for the dialog.
     * @param message The error message to show.
     */
    public void error(String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);

        if (logger != null) {
            logger.severe(message);
        }
    }

    /**
     * Shows an error message to the user, and also logs it if a Logger was provided.
     *
     * @param message The error message to show.
     * @param ex      An Exception which will be handed to the Logger.
     */
    public void error(String message, Throwable ex) {
        error(defaultErrorTitle, message, ex);
    }

    /**
     * Shows an error message to the user, and also logs it if a Logger was provided.
     *
     * @param title   The title for the dialog.
     * @param message The error message to show.
     * @param ex      An Exception which will be handed to the Logger.
     */
    public void error(String title, String message, Throwable ex) {
        error(title, message);

        if (logger != null) {
            logger.log(Level.SEVERE, message, ex);
        }
    }

    /**
     * Shows an info message to the user, and also logs it if a Logger was provided.
     *
     * @param message The info message to show.
     */
    public void info(String message) {
        info(defaultInfoTitle, message);
    }

    /**
     * Shows an info message to the user, and also logs it if a Logger was provided.
     *
     * @param title   The title for the dialog.
     * @param message The info message to show.
     */
    public void info(String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);

        if (logger != null) {
            logger.info(message);
        }
    }

    /**
     * Shows a warning message to the user, and also logs it if a Logger was provided.
     *
     * @param message The warning message to show.
     */
    public void warning(String message) {
        warning(defaultWarningTitle, message);
    }

    /**
     * Shows a warning message to the user, and also logs it if a Logger was provided.
     *
     * @param title   The title for the dialog.
     * @param message The warning message to show.
     */
    public void warning(String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);

        if (logger != null) {
            logger.warning(message);
        }
    }

    /**
     * Shorthand for using JOptionPane to ask the user a simple yes or no question.
     * Nothing is logged. A default dialog title of "Confirm" is used.
     *
     * @param message The question message.
     * @return One of JOptionPane.YES_OPTION or JOptionPane.NO_OPTION (shorthand: MessageUtil.YES or MessageUtil.NO).
     */
    public int askYesNo(String message) {
        return askYesNo(defaultQuestionTitle, message);
    }

    /**
     * Shorthand for using JOptionPane to ask the user a yes, no, or cancel question.
     * Nothing is logged. A default dialog title of "Confirm" is used.
     *
     * @param message The question message.
     * @return One of JOptionPane.YES_OPTION, JOptionPane.NO_OPTION, or JOptionPane.CANCEL_OPTION
     * (shorthand: MessageUtil.YES, MessageUtil.NO, or MessageUtil.CANCEL).
     */
    public int askYesNoCancel(String message) {
        return askYesNoCancel(defaultQuestionTitle, message);
    }

    /**
     * Shorthand for using JOptionPane to ask the user a simple yes or no question.
     * Nothing is logged.
     *
     * @param title   The title for the dialog.
     * @param message The question message.
     * @return One of JOptionPane.YES_OPTION or JOptionPane.NO_OPTION (shorthand: MessageUtil.YES or MessageUtil.NO).
     */
    public int askYesNo(String title, String message) {
        return JOptionPane.showConfirmDialog(parent, message, title,
                                             JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    }

    /**
     * Shorthand for using JOptionPane to ask the user a yes, no, or cancel question.
     * Nothing is logged.
     *
     * @param title   The title for the dialog.
     * @param message The question message.
     * @return One of JOptionPane.YES_OPTION, JOptionPane.NO_OPTION, or JOptionPane.CANCEL_OPTION
     * (shorthand: MessageUtil.YES, MessageUtil.NO, or MessageUtil.CANCEL).
     */
    public int askYesNoCancel(String title, String message) {
        return JOptionPane.showConfirmDialog(parent, message, title,
                                             JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
    }

    /**
     * Shorthand for using JOptionPane to ask the user to select from a list of options.
     * A default dialog title of "Select" is used. Nothing is logged.
     *
     * @param message               The prompt message.
     * @param options               An array of possible options in String form.
     * @param initialSelectionValue The option that should be initially selected when the dialog appears.
     * @return The selected option as a String, or null if the user cancelled.
     */
    public String askSelect(String message, String[] options, String initialSelectionValue) {
        return askSelect(defaultInputTitle, message, options, initialSelectionValue);
    }

    /**
     * Shorthand for using JOptionPane to ask the user to select from a list of options.
     * Nothing is logged.
     *
     * @param title                 The title for the dialog.
     * @param message               The prompt message.
     * @param options               An array of possible options in String form.
     * @param initialSelectionValue The option that should be initially selected when the dialog appears.
     * @return The selected option as a String, or null if the user cancelled.
     */
    public String askSelect(String title, String message, String[] options, String initialSelectionValue) {
        Object result = JOptionPane.showInputDialog(
                parent,
                message,
                title,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                initialSelectionValue);
        if (result != null) {
            return result.toString();
        }
        return null;
    }

    /**
     * Shorthand for using JOptionPane to ask the user for free-form text input.
     * A default dialog title of "Select" is used. Nothing is logged.
     *
     * @param message      The prompt message.
     * @param initialValue The initial value to pre-fill in the input field.
     * @return The entered text as a String, or null if the user cancelled.
     */
    public String askText(String message, String initialValue) {
        return askText(defaultInputTitle, message, initialValue);
    }

    /**
     * Shorthand for using JOptionPane to ask the user for free-form text input.
     * Nothing is logged.
     *
     * @param title        The title for the dialog.
     * @param message      The prompt message.
     * @param initialValue The initial value to pre-fill in the input field.
     * @return The entered text as a String, or null if the user cancelled.
     */
    public String askText(String title, String message, String initialValue) {
        return (String)JOptionPane.showInputDialog(
                parent,
                message,
                title,
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                initialValue);
    }
}
