package ca.corbett.extras.io;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for hyperlink launching in JREs that support it.
 * Use isBrowsingSupported() to check if the current JRE allows
 * launching hyperlinks. Use the openHyperlink(...) methods to
 * easily launch a given hyperlink in the default browser, or
 * use the many BrowseAction.of() overloads to create an Action
 * that can be used with buttons, menu items, etc.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.6
 */
public class HyperlinkUtil {

    private static final Logger log = Logger.getLogger(HyperlinkUtil.class.getName());
    private static DesktopBrowser desktopBrowser = new SystemDesktopBrowser();

    private HyperlinkUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Some JREs don't allow launching a hyperlink to open the browser.
     */
    public static boolean isBrowsingSupported() {
        return desktopBrowser.isBrowsingSupported();
    }

    /**
     * Validates that the given String evaluates to a valid URL.
     */
    public static boolean isValidUrl(String urlString) {
        try {
            new URL(urlString);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
     * If hyperlink launching is supported, will open the user's default browser to the
     * given link (assuming the link is valid). If the JRE doesn't allow such things,
     * then the given link will be copied to the clipboard instead (better than nothing).
     * No message dialog will be shown on failure. If you wish to show a message dialog
     * on failure, use the overload that accepts an owner Component.
     */
    public static void openHyperlink(String link) {
        openHyperlink(link, null);
    }

    /**
     * If hyperlink launching is supported, will open the user's default browser to the
     * given link (assuming the link is valid). If the JRE doesn't allow such things,
     * then the given link will be copied to the clipboard instead (better than nothing).
     * No message dialog will be shown on failure. If you wish to show a message dialog
     * on failure, use the overload that accepts an owner Component.
     */
    public static void openHyperlink(URL url) {
        if (url == null) {
            report("Error", "Cannot open null URL.", Level.WARNING, null, null);
            return;
        }
        openHyperlink(url.toString(), null);
    }

    /**
     * If hyperlink launching is supported, will open the user's default browser to the
     * given link (assuming the link is valid). If the JRE doesn't allow such things,
     * then the given link will be copied to the clipboard instead (better than nothing).
     * No message dialog will be shown on failure. If you wish to show a message dialog
     * on failure, use the overload that accepts an owner Component.
     */
    public static void openHyperlink(URI uri) {
        openHyperlink(uri, null);
    }

    /**
     * If hyperlink launching is supported, will open the user's default browser to the
     * given link (assuming the link is valid). If the JRE doesn't allow such things,
     * then the given link will be copied to the clipboard instead (better than nothing).
     * If an owner Component is provided, a message dialog will be shown to inform
     * the user that the link was copied to the clipboard.
     */
    public static void openHyperlink(String link, Component owner) {
        if (link == null) {
            report("Error", "Cannot open null link.", Level.WARNING, null, owner);
            return;
        }
        try {
            openHyperlink(new URL(link).toURI(), owner);
        }
        catch (Exception e) {
            report("Malformed URL", "Unable to browse URL: "+link, Level.WARNING, e, owner);
        }
    }

    /**
     * If hyperlink launching is supported, will open the user's default browser to the
     * given link (assuming the link is valid). If the JRE doesn't allow such things,
     * then the given link will be copied to the clipboard instead (better than nothing).
     * If an owner Component is provided, a message dialog will be shown to inform
     * the user that the link was copied to the clipboard.
     */
    public static void openHyperlink(URL url, Component owner) {
        if (url == null) {
            report("Error", "Cannot open null URL.", Level.WARNING, null, owner);
            return;
        }
        try {
            openHyperlink(url.toURI(), owner);
        }
        catch (Exception e) {
            report("Malformed URL", "Unable to browse URL: " + url.toString(), Level.WARNING, e, owner);
        }
    }

    /**
     * If hyperlink launching is supported, will open the user's default browser to the
     * given link (assuming the link is valid). If the JRE doesn't allow such things,
     * then the given link will be copied to the clipboard instead (better than nothing).
     * If an owner Component is provided, a message dialog will be shown to inform
     * the user that the link was copied to the clipboard.
     */
    public static void openHyperlink(URI uri, Component owner) {
        if (uri == null) {
            report("Error", "Cannot open null URI.", Level.WARNING, null, owner);
            return;
        }

        if (isBrowsingSupported()) {
            try {
                desktopBrowser.browse(uri);
            }
            catch (Exception e) {
                report("Error", "Unable to browse URI: " + uri, Level.WARNING, e, owner);
            }
        }
        else {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(uri.toString()), null);
            report("Copied to clipboard",
                   "Hyperlinks are not enabled in your JRE. Link copied to clipboard instead.",
                   Level.INFO,
                   null,
                   owner);
        }
    }

    /**
     * If the current JRE supports browsing, this action will open the given URI
     * in the user's default browser. This is equivalent to invoking
     * one of the openHyperlink methods, but in a convenient Action form for use
     * with buttons, menu items, etc.
     * <p>
     *     By default, the name of this action is set to the String representation
     *     of the given URI. You can change the name later by calling setName().
     *     For example:
     * </p>
     * <pre>
     * // Creates an action with a name equal to the URI string:
     * HyperlinkUtil.BrowseAction browseAction =
     *       HyperlinkUtil.BrowseAction.of(new URI("http://www.example.invalid"));
     *
     * // Update the name to something more user-friendly:
     * browseAction.setName("Visit Example.invalid");
     * </pre>
     */
    public static class BrowseAction extends AbstractAction {
        final URI uri;
        final URL url;
        final String urlString;
        final Component owner;

        private BrowseAction(URI uri, URL url, String urlString, Component owner) {
            super(uri != null ? uri.toString() : (url != null ? url.toString() : urlString));
            this.uri = uri;
            this.url = url;
            this.urlString = urlString;
            this.owner = owner;
        }

        /**
         * Allows modifying the name of this action after creation.
         */
        public BrowseAction setName(String name) {
            putValue(NAME, name);
            return this;
        }

        /**
         * Returns the name of this action.
         */
        public String getName() {
            Object obj = getValue(NAME);
            if (obj == null) {
                return "(null)";
            }
            return obj instanceof String ? (String)getValue(NAME) : obj.toString();
        }

        /**
         * Creates a new BrowseAction for the given URI and with no popup dialog.
         */
        public static BrowseAction of(URI uri) {
            return new BrowseAction(uri, null, null, null);
        }

        /**
         * Creates a new BrowseAction for the given URL and with no popup dialog.
         */
        public static BrowseAction of(URL url) {
            return new BrowseAction(null, url, null, null);
        }

        /**
         * Creates a new BrowseAction for the given URL string and with no popup dialog.
         */
        public static BrowseAction of(String urlString) {
            return new BrowseAction(null, null, urlString, null);
        }

        /**
         * Creates a new BrowseAction for the given URI and the given owner
         * Component for showing popup dialogs.
         */
        public static BrowseAction of(URI uri, Component owner) {
            return new BrowseAction(uri, null, null, owner);
        }

        /**
         * Creates a new BrowseAction for the given URL and the given owner
         * Component for showing popup dialogs.
         */
        public static BrowseAction of(URL url, Component owner) {
            return new BrowseAction(null, url, null, owner);
        }

        /**
         * Creates a new BrowseAction for the given URL string and the given owner
         * Component for showing popup dialogs.
         */
        public static BrowseAction of(String urlString, Component owner) {
            return new BrowseAction(null, null, urlString, owner);
        }

        /**
         * Opens our configured hyperlink when invoked.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            if (uri != null) {
                HyperlinkUtil.openHyperlink(uri, owner);
                return;
            }
            else if (url != null) {
                HyperlinkUtil.openHyperlink(url, owner);
                return;
            }
            else if (urlString != null) {
                HyperlinkUtil.openHyperlink(urlString, owner);
                return;
            }

            // If we get to this point, we were horribly misconfigured:
            report("Error", "No hyperlink configured for this action.", Level.SEVERE, null, owner);
        }
    }

    /**
     * Invoked internally to report errors and log messages.
     *
     * @param title A title for the dialog, if a dialog is to be shown.
     * @param message The message text to log and/or show.
     * @param level The logging level to use for the log message.
     * @param e An optional Throwable whose message will be appended to the log and dialog message.
     * @param owner An optional owner Component for the dialog. If null, no dialog will be shown.
     */
    private static void report(String title, String message, Level level, Throwable e, Component owner) {
        if (e != null) {
            log.log(level, message + ": " + e.getMessage());
        }
        else {
            log.log(level, message);
        }
        if (owner != null) {
            if (e != null) {
                message += "\n\nError details: " + e.getMessage();
            }
            int dialogEventType = switch (level.getName()) {
                case "SEVERE" -> JOptionPane.ERROR_MESSAGE;
                case "WARNING" -> JOptionPane.WARNING_MESSAGE;
                default -> JOptionPane.INFORMATION_MESSAGE;
            };
            JOptionPane.showMessageDialog(owner, message, title, dialogEventType);
        }
    }

    /**
     * Interface for abstracting Desktop operations to allow for testing.
     */
    interface DesktopBrowser {
        /**
         * Returns true if browsing is supported in the current environment.
         */
        boolean isBrowsingSupported();

        /**
         * Opens the given URI in the default browser.
         *
         * @throws IOException if the browser launch fails
         */
        void browse(URI uri) throws IOException;
    }

    /**
     * Default implementation that delegates to the system Desktop.
     */
    static class SystemDesktopBrowser implements DesktopBrowser {
        private final Desktop desktop;

        SystemDesktopBrowser() {
            this.desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        }

        @Override
        public boolean isBrowsingSupported() {
            return desktop != null && desktop.isSupported(Desktop.Action.BROWSE);
        }

        @Override
        public void browse(URI uri) throws IOException {
            if (desktop != null) {
                desktop.browse(uri);
            }
            else {
                throw new IOException("Desktop browsing not supported on this JRE.");
            }
        }
    }

    /**
     * Sets a custom DesktopBrowser implementation. This is exclusively intended for testing.
     *
     * @param browser the DesktopBrowser implementation to use (mock or otherwise)
     */
    static void setDesktopBrowser(DesktopBrowser browser) {
        desktopBrowser = browser;
    }

    /**
     * Resets the DesktopBrowser to the default system implementation.
     */
    static void resetDesktopBrowser() {
        desktopBrowser = new SystemDesktopBrowser();
    }
}
