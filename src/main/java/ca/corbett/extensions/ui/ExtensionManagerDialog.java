package ca.corbett.extensions.ui;

import ca.corbett.extensions.AppExtension;
import ca.corbett.extensions.ExtensionManager;
import ca.corbett.extras.ToggleableTabbedPane;
import ca.corbett.updates.UpdateSources;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;

/**
 * Provides a standardized way of viewing and enabling extensions across applications.
 * Basically wraps an ExtensionManagerPanel and provides facilities for auto-committing
 * changes directly to the given ExtensionManager.
 * <p>
 * If you don't want a popup dialog,
 * but would rather embed the UI onto some existing screen, use ExtensionManagerPanel
 * directly, but be aware you have to commit the resulting changes (enabling or disabling
 * of extensions).
 * </p>
 * <p>
 * By default, this dialog will auto-commit any changes to extension enabled status made
 * by the user. This happens when the dialog is okayed. You can prevent this behaviour
 * via setAutoCommit(false), if you'd rather handle the enabling or disabling of
 * extensions yourself. Use isExtensionEnabled() in this class to determine if the
 * user enabled or disabled a given extension.
 * </p>
 *
 * @param <T> Any implementation of AppExtension
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2023-11-11
 */
public class ExtensionManagerDialog<T extends AppExtension> extends JDialog {

    private final ExtensionManager<T> extManager;
    private final UpdateSources updateSources;

    private ToggleableTabbedPane tabbedPane;
    private InstalledExtensionsPanel<T> installedExtensionsPanel;
    private AvailableExtensionsPanel availableExtensionsPanel;
    private boolean autoCommit;
    private boolean wasOkayed;
    private boolean wasModified;

    /**
     * Creates an ExtensionManager dialog with the given ExtensionManager and the
     * default title of "Extension Manager".
     *
     * @param manager The ExtensionManager containing our list of extensions.
     * @param owner   The owner window. This dialog will be modal.
     */
    public ExtensionManagerDialog(ExtensionManager<T> manager, Window owner) {
        this(manager, owner, null);
    }

    /**
     * Creates an ExtensionManager dialog with the given ExtensionManager
     * and the given UpdateSources instance, which may be null. If UpdateSources
     * is specified, the "available" tab will be shown with options to query
     * the remote update source for possible downloads.
     *
     * @param manager The ExtensionManager containing our list of extensions.
     * @param owner   The owner window. This dialog will be modal.
     * @param updateSources An optional UpdateSources instance for querying remote downloadable extensions.
     */
    public ExtensionManagerDialog(ExtensionManager<T> manager, Window owner, UpdateSources updateSources) {
        super(owner, "Extension Manager");
        this.extManager = manager;
        this.updateSources = updateSources;
        this.setSize(new Dimension(700, 485));
        this.setMinimumSize(new Dimension(700, 485));
        this.setResizable(true);
        this.setModalityType(JDialog.ModalityType.APPLICATION_MODAL);
        this.setLocationRelativeTo(owner);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.autoCommit = true;
        this.wasOkayed = false;
        this.wasModified = false;
        initComponents();
    }

    /**
     * Sets whether to auto-commit changes made in this dialog when the okay button is
     * clicked (the default value for this property is true). Any extension that is enabled
     * or disabled within this dialog will result in a message to ExtensionManager to
     * enable or disable that extension. This happens when the dialog is okayed.
     *
     * @param value Whether to auto-commit as described above.
     */
    public void setAutoCommit(boolean value) {
        autoCommit = value;
    }

    /**
     * Reports whether the dialog will auto-commit changes made when the okay button is clicked.
     * Any extension that is enabled or disabled within this dialog will result in a message
     * to ExtensionManager to enable or disable that extension. This will happen
     * when the dialog is okayed.
     *
     * @return Whether auto-commit is enabled as described above.
     */
    public boolean isAutoCommit() {
        return autoCommit;
    }

    /**
     * Reports whether the dialog was okayed or canceled.
     *
     * @return True if ok was clicked, false otherwise.
     */
    public boolean wasOkayed() {
        return wasOkayed;
    }

    /**
     * Reports whether any extension was enabled or disabled while the dialog was open.
     * If autoCommit is true, these changes are pushed to ExtensionManager as soon as the
     * dialog is okayed.
     *
     * @return True if at least one extension was enabled or disabled in this dialog.
     */
    public boolean wasModified() {
        return wasModified;
    }

    /**
     * Reports whether the extension with the given class name was toggled to enabled or
     * disabled while this dialog was open. If autoCommit is true, these changes are pushed
     * to ExtensionManager as soon as the dialog is okayed, so you don't have to worry about
     * this. If autoCommit is false, you can use this to manually determine which extensions
     * have been enabled or disabled, by comparing this value to the equivalent value
     * from ExtensionManager.isExtensionEnabled().
     *
     * @param className The fully qualified name of the extension in question.
     * @return Whether or not the extension is marked as enabled in this dialog.
     */
    public boolean isExtensionEnabled(String className) {
        return installedExtensionsPanel.isExtensionEnabled(className);
    }

    /**
     * Invoked internally when the dialog is okayed. Will determine whether any change
     * was made and store the result in wasModified. Will notify ExtensionManager of any
     * changes if autoCommit is true.
     * TODO this is old code from when we only dealt with installed extensions... update for the new case
     */
    private void okay() {
        wasOkayed = true;
        wasModified = false;

        // First determine if any changes were made:
        for (AppExtension extension : extManager.getAllLoadedExtensions()) {
            String className = extension.getClass().getName();
            if (extManager.isExtensionEnabled(className) != installedExtensionsPanel.isExtensionEnabled(className)) {
                wasModified = true;
            }
        }

        // We can auto-commit those changes here if requested:
        if (wasModified && autoCommit) {
            for (AppExtension extension : extManager.getAllLoadedExtensions()) {
                String className = extension.getClass().getName();

                // This call does nothing if the new enabled state matches the old one,
                // so we can just fire it off here blindly and let ExtensionManager deal with it:
                extManager.setExtensionEnabled(className, installedExtensionsPanel.isExtensionEnabled(className));
            }
        }

        dispose();
    }

    /**
     * Utility method to trim the given String to a max length of 50 chars, with
     * a "..." added to it if we had to cut it down. See trimString(String,int) for
     * a more flexible version of this method.
     */
    protected static String trimString(String input) {
        return trimString(input, 50);
    }

    /**
     * Utility method to trim the given String to the given length, with a "..." added
     * to it if it was too long.
     *
     * @param input The String to be trimmed.
     * @param LIMIT The maximum length to allow.
     * @return The input string if it was shorter than LIMIT, or the chopped string plus "..." otherwise.
     */
    public static String trimString(String input, final int LIMIT) {
        if (input == null) {
            return null;
        }
        if (input.length() >= LIMIT) {
            input = input.substring(0, LIMIT) + "...";
        }
        return input;
    }


    private void initComponents() {
        setLayout(new BorderLayout());
        tabbedPane = new ToggleableTabbedPane();
        installedExtensionsPanel = new InstalledExtensionsPanel<>(this, extManager);
        tabbedPane.addTab("Installed", installedExtensionsPanel);

        if (updateSources != null) {
            String appName = extManager.getApplicationName();
            String appVersion = extManager.getApplicationVersion();
            availableExtensionsPanel = new AvailableExtensionsPanel(this, updateSources, appName, appVersion);
            tabbedPane.addTab("Available", availableExtensionsPanel);
        }

        if (!extManager.getStartupErrors().isEmpty()) {
            tabbedPane.addTab("Errors", new ExtensionErrorsTab(this, extManager));
        }

        // No point showing the tab header bar if there's only one tab:
        if (tabbedPane.getTabCount() == 1) {
            tabbedPane.setTabHeaderVisible(false);
        }

        add(tabbedPane, BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton button = new JButton("OK");
        button.setPreferredSize(new Dimension(90, 24));
        button.addActionListener(e -> okay());
        panel.add(button);
        button = new JButton("Cancel");
        button.setPreferredSize(new Dimension(90, 24));
        button.addActionListener(e -> dispose());
        panel.add(button);
        panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        return panel;
    }

}
