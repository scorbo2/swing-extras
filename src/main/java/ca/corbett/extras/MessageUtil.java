package ca.corbett.extras;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple utility class to show messages to the user via JOptionPane while also simultaneously
 * logging them. This saves the caller from having to make two separate calls each time
 * an error comes up.
 * <p>
 * <b>Example usage:</b></p>
 * <pre>
 * MessageUtil messageUtil = new MessageUtil(this, myLogger);
 * // ...
 * messageUtil.error("Something went horribly wrong", someException);
 * </pre>
 * This will show an error dialog to the user, and also log the message string to the
 * given Logger.
 *
 * @author scorbo2
 * @since 2022-04-11
 */
public final class MessageUtil {

    private final Component parent;
    private final Logger logger;
    private String defaultErrorTitle = "Error";
    private String defaultInfoTitle = "Info";

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
     * Returns the Logger currently being used. May be null if none was set.
     *
     * @return A Logger instance, or null.
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Used to specify a default dialog title in the case where error() is invoked
     * without a given title. Does not effect logging. If unset, the default is "Error".
     *
     * @param title The new title to use for error dialogs.
     */
    public void setDefaultErrorTitle(String title) {
        defaultErrorTitle = title;
    }

    /**
     * Used to specify a default dialog title in the case where info() is invoked
     * without a given title. Does not effect logging. If unset, the default is "Info".
     *
     * @param title The new title to use for info dialogs.
     */
    public void setDefaultInfoTitle(String title) {
        defaultInfoTitle = title;
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
        logger.log(Level.SEVERE, message, ex);
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
        warning(defaultInfoTitle, message);
    }

    /**
     * Shows a warning message to the user, and also logs it if a Logger was provided.
     *
     * @param title   The title for the dialog.
     * @param message The warning message to show.
     */
    public void warning(String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);

        if (logger != null) {
            logger.warning(message);
        }
    }
}
