package ca.corbett.extensions.ui;

import ca.corbett.extensions.AppExtension;
import ca.corbett.extensions.ExtensionManager;
import ca.corbett.extras.ListPanel;
import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.MessageUtil;
import ca.corbett.updates.UpdateManager;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * This panel shows a list of all installed extensions, and allows the user to enable/disable them,
 * or uninstall them if desired.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2023-11-11
 */
public class InstalledExtensionsPanel<T extends AppExtension> extends JPanel {

    private static final Logger log = Logger.getLogger(InstalledExtensionsPanel.class.getName());

    protected final Window owner;
    protected final ExtensionManager<T> extManager;
    protected final UpdateManager updateManager;

    protected final JPanel contentPanel = new JPanel(new BorderLayout());
    protected final JPanel headerPanel = new JPanel(new BorderLayout());
    protected final JPanel detailsPanel = new JPanel(new BorderLayout());

    protected final Map<String, ExtensionDetailsPanel> detailsPanelMap;
    protected final ListPanel<AppExtensionPlaceholder<T>> extensionListPanel;
    protected ExtensionDetailsPanel emptyPanel;
    protected MessageUtil messageUtil;
    protected boolean isRestartRequired;

    public InstalledExtensionsPanel(Window owner, ExtensionManager<T> manager) {
        this(owner, manager, null);
    }

    public InstalledExtensionsPanel(Window owner, ExtensionManager<T> manager, UpdateManager updateManager) {
        this.owner = owner;
        extManager = manager;
        this.updateManager = updateManager;
        detailsPanelMap = new HashMap<>();
        extensionListPanel = new ListPanel<>(List.of(new EnableAllAction(), new DisableAllAction()));
        extensionListPanel.setListCellRenderer(new ExtensionListRenderer());
        extensionListPanel.setPreferredSize(new Dimension(200, 200));
        extensionListPanel.setMinimumSize(new Dimension(200, 1));
        extensionListPanel.setMaximumSize(new Dimension(200, Integer.MAX_VALUE));
        extensionListPanel.addListSelectionListener(e -> listSelectionChanged());
        List<T> extensions = extManager.getAllLoadedExtensions();
        extensions.sort(Comparator.comparing(o -> o.getInfo().getName()));
        for (T extension : extensions) {
            boolean isEnabled = extManager.isExtensionEnabled(extension.getClass().getName());
            AppExtensionPlaceholder<T> placeholder = new AppExtensionPlaceholder<>(extension, isEnabled);
            extensionListPanel.addItem(placeholder);
        }
        isRestartRequired = false;
        initComponents();
    }

    /**
     * Indicates whether any change made on this panel requires an application restart.
     * (Extension uninstalled perhaps).
     */
    public boolean isRestartRequired() {
        return isRestartRequired;
    }

    /**
     * Reports whether an extension has been enabled or disabled by the user on this panel.
     *
     * @param className The fully qualified class name of the extension to query.
     * @return True if that extension is enabled on this panel, false otherwise or if not found.
     */
    public boolean isExtensionEnabled(String className) {
        for (AppExtensionPlaceholder<T> placeholder : extensionListPanel.getAll()) {
            if (placeholder.extension.getClass().getName().equals(className)) {
                return placeholder.isEnabled;
            }
        }
        return false;
    }

    /**
     * Sets all extensions to the given enabled status.
     */
    protected void setAllEnabled(boolean enabled) {
        for (AppExtensionPlaceholder<T> placeholder : extensionListPanel.getAll()) {
            placeholder.isEnabled = enabled;
            for (Component c : headerPanel.getComponents()) {
                if (c instanceof ExtensionTitleBar) {
                    ((ExtensionTitleBar)c).setExtensionEnabled(enabled);
                }
            }
        }
        rejigger(extensionListPanel);
    }

    protected void listSelectionChanged() {
        headerPanel.removeAll();
        detailsPanel.removeAll();
        final AppExtensionPlaceholder<?> placeholder = extensionListPanel.getSelected();
        if (placeholder == null) {
            headerPanel.add(new ExtensionTitleBar(null));
            detailsPanel.add(emptyPanel);
            rejigger(contentPanel);
            return; // no selection
        }

        ExtensionTitleBar titleBar = new ExtensionTitleBar(placeholder.extension.getInfo().getName())
                .setEnabledToggleAction(new ExtensionEnabledToggleAction(placeholder))
                .setUninstallAction(new ExtensionUninstallAction(placeholder));
        titleBar.setExtensionEnabled(placeholder.isEnabled());
        headerPanel.add(titleBar);
        String className = placeholder.extension.getClass().getName();
        ExtensionDetailsPanel extPanel = detailsPanelMap.get(className);
        if (extPanel == null) {
            // We haven't visited this one before, so create a new panel for it now:
            extPanel = new ExtensionDetailsPanel(owner, placeholder.extension.getInfo());
            extPanel.setIsLocallyInstalledExtension(extManager.getSourceJar(className));
            extPanel.setConfigProperties(placeholder.extension.getConfigProperties());
            extPanel.setNameFieldVisible(false); // we have our own title bar
            detailsPanelMap.put(className, extPanel);
        }
        detailsPanel.add(extPanel);
        rejigger(contentPanel);
    }

    /**
     * Swing nonsense - when stuff is added or removed from a container, you have to
     * do some silliness to get the change to become visible.
     *
     * @param component The thing to rejigger.
     */
    protected void rejigger(final JComponent component) {
        component.invalidate();
        component.revalidate();
        component.repaint();
    }

    protected void initComponents() {
        setLayout(new BorderLayout());

        // The empty panel will be shown by default, or whenever the list selection is cleared.
        emptyPanel = new ExtensionDetailsPanel(owner, null);

        // Add the extension list on the left:
        add(extensionListPanel, BorderLayout.WEST);

        // And we have our layout:
        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(detailsPanel, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);

        // If we have at least one extension, select it:
        if (!extensionListPanel.isEmpty()) {
            SwingUtilities.invokeLater(() -> extensionListPanel.selectItem(0));
        }

        // Otherwise, show the empty panel:
        else {
            contentPanel.add(emptyPanel);
        }
    }

    protected class EnableAllAction extends AbstractAction {

        public EnableAllAction() {
            super("Enable all");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setAllEnabled(true);
        }
    }

    protected class DisableAllAction extends AbstractAction {

        public DisableAllAction() {
            super("Disable all");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setAllEnabled(false);
        }
    }

    /**
     * Used internally to represent an extension, either installed or uninstalled, either enabled or disabled.
     *
     * @param <T> The specific type of AppExtension that we're dealing with.
     * @author <a href="https://github.com/scorbo2">scorbo2</a>
     */
    protected static class AppExtensionPlaceholder<T extends AppExtension> {

        protected final T extension;
        protected String name;
        protected boolean isEnabled;

        public AppExtensionPlaceholder(T extension, boolean isEnabled) {
            this.extension = extension;
            this.name = extension.getInfo().getName();
            this.isEnabled = isEnabled;
        }

        public AppExtensionPlaceholder<T> setEnabled(boolean enabled) {
            isEnabled = enabled;
            return this;
        }

        public boolean isEnabled() {
            return isEnabled;
        }

        @Override
        public boolean equals(Object object) {
            if (object == null || getClass() != object.getClass()) { return false; }
            AppExtensionPlaceholder<?> that = (AppExtensionPlaceholder<?>)object;
            return Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(name);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(name);
            if (!isEnabled) {
                sb.append(" (disabled)");
            }
            return sb.toString();
        }

    }

    /**
     * An action that can respond to an extension being enabled or disabled. We present
     * a checkbox for this in the ExtensionTitleBar, and this action can be invoked
     * when the checkbox is toggled. We update the extension status and change
     * the label for it in the extension list on the left.
     *
     * @author <a href="https://github.com/scorbo2">scorbo2</a>
     */
    protected class ExtensionEnabledToggleAction extends AbstractAction {

        private final AppExtensionPlaceholder<?> placeholder;

        public ExtensionEnabledToggleAction(AppExtensionPlaceholder<?> placeholder) {
            super("Enabled");
            this.placeholder = placeholder;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof ExtensionTitleBar) {
                placeholder.setEnabled(((ExtensionTitleBar)e.getSource()).isExtensionEnabled());
                extensionListPanel.revalidate();
                extensionListPanel.repaint();
            }
        }
    }

    protected class ExtensionUninstallAction extends AbstractAction {

        private final AppExtensionPlaceholder<?> placeholder;

        public ExtensionUninstallAction(AppExtensionPlaceholder<?> placeholder) {
            super("Uninstall");
            this.placeholder = placeholder;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            File jarFile = extManager.getSourceJar(placeholder.extension.getClass().getName());
            if (jarFile == null) {
                getMessageUtil().info("The extension \"" + placeholder.name + "\" is a built-in extension.\n"
                                              + "It cannot be uninstalled.");
                return;
            }

            // Give user a chance to back out:
            if (JOptionPane.showConfirmDialog(owner,
                                              "Really uninstall this extension?",
                                              "Confirm",
                                              JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                return;
            }

            jarFile.deleteOnExit();
            isRestartRequired = true;

            // If we have no update manager, just do an info prompt:
            if (updateManager == null) {
                getMessageUtil().info("Changes will take effect when the application is restarted.");
            }

            // Otherwise, we can do it now:
            else {
                updateManager.showApplicationRestartPrompt(owner);
            }

            // We could do stuff here like remove it from the menu on the left, update our UI to reflect
            // that that extension is no longer with us, and etc, but eh... I feel like if you're uninstalling
            // stuff, and you know you need a restart to reflect the change, then it's probably a
            // reasonable expectation that the restart will happen soon enough after the uninstall that
            // updating the UI is probably not worth the effort.
        }
    }

    /**
     * Custom renderer so that we can show disabled extensions in a different font style to
     * make it a little more clear that they are disabled.
     */
    protected class ExtensionListRenderer extends JLabel
            implements ListCellRenderer<AppExtensionPlaceholder<T>> {

        @Override
        public Component getListCellRendererComponent(JList<? extends AppExtensionPlaceholder<T>> list,
                                                      AppExtensionPlaceholder<T> value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            setText(value.toString());
            setOpaque(true);
            Color selectedFg = LookAndFeelManager.getLafColor("List.selectionForeground", Color.WHITE);
            Color selectedBg = LookAndFeelManager.getLafColor("List.selectionBackground", Color.BLUE);
            Color normalFg = LookAndFeelManager.getLafColor("List.foreground", Color.BLACK);
            Color normalBg = LookAndFeelManager.getLafColor("List.background", Color.WHITE);
            setForeground(isSelected ? selectedFg : normalFg);
            setBackground(isSelected ? selectedBg : normalBg);
            if (value.isEnabled) {
                setFont(list.getFont().deriveFont(Font.PLAIN));
            }
            else {
                setFont(list.getFont().deriveFont(Font.ITALIC));
            }
            return this;
        }
    }

    protected MessageUtil getMessageUtil() {
        if (messageUtil == null) {
            messageUtil = new MessageUtil(owner, log);
        }
        return messageUtil;
    }
}
