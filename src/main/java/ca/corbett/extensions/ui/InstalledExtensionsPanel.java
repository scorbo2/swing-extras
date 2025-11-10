package ca.corbett.extensions.ui;

import ca.corbett.extensions.AppExtension;
import ca.corbett.extensions.ExtensionManager;
import ca.corbett.extras.ListPanel;
import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.updates.VersionManifest;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This panel combines a configurable ListPanel on the left side, a configurable ExtensionTitleBar
 * in the top right, and an ExtensionDetailsPanel in the main right position. The combination of these
 * three allows the user to pick an extension on the left and view/edit its details on the right.
 * <p>
 *
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2023-11-11
 */
public class ExtensionManagerPanel<T extends AppExtension> extends JPanel {

    protected final Window owner;
    protected final ExtensionManager<T> extManager;
    protected final JPanel contentPanel = new JPanel(new BorderLayout());
    protected final Map<String, ExtensionDetailsPanel> detailsPanelMap;
    protected final ListPanel<AppExtensionPlaceholder<T>> extensionListPanel;
    protected ExtensionDetailsPanel emptyPanel;

    public ExtensionManagerPanel(Window owner, ExtensionManager<T> manager) {
        this.owner = owner;
        extManager = manager;
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
            AppExtensionPlaceholder<T> placeholder = new AppExtensionPlaceholder<>(extension);
            placeholder.setEnabled(extManager.isExtensionEnabled(extension.getClass().getName()));
            extensionListPanel.addItem(placeholder);
        }
        initComponents();
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
            ExtensionDetailsPanel detailsPanel = detailsPanelMap.get(placeholder.extension.getClass().getName());
            if (detailsPanel != null) {
                detailsPanel.setExtensionEnabled(enabled);
            }
        }
        rejigger(extensionListPanel);
    }

    protected void listSelectionChanged() {
        contentPanel.removeAll();
        final AppExtensionPlaceholder<?> placeholder = extensionListPanel.getSelected();
        if (placeholder == null) {
            contentPanel.add(emptyPanel);
            rejigger(contentPanel);
            return; // no selection
        }
        ExtensionDetailsPanel detailsPanel = detailsPanelMap.get(placeholder.extension.getClass().getName());
        if (detailsPanel == null) {
            detailsPanel = new ExtensionDetailsPanel(owner, extManager, placeholder.extension,
                                                     placeholder.isEnabled);
            detailsPanel.addExtensionDetailsPanelListener(new ExtensionDetailsPanelListener() {
                @Override
                public void extensionEnabled(ExtensionDetailsPanel source, String className) {
                    placeholder.isEnabled = true;
                    rejigger(extensionListPanel);
                }

                @Override
                public void extensionDisabled(ExtensionDetailsPanel source, String className) {
                    placeholder.isEnabled = false;
                    rejigger(extensionListPanel);
                }

            });
            detailsPanelMap.put(placeholder.extension.getClass().getName(), detailsPanel);
        }
        contentPanel.add(detailsPanel, BorderLayout.CENTER);
        rejigger(contentPanel);
    }

    /**
     * Swing nonsense - when stuff is added or removed from a container, you have to
     * do some silliness to get the change to become visible.
     *
     * @param component The thing to rejigger.
     */
    protected void rejigger(final JComponent component) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                component.invalidate();
                component.revalidate();
                component.repaint();
            }

        });
    }

    protected void initComponents() {
        setLayout(new BorderLayout());

        // The empty panel will be shown by default, or whenever the list selection is cleared.
        emptyPanel = new ExtensionDetailsPanel(owner, extManager, null, false);

        // Add the extension list on the left:
        add(extensionListPanel, BorderLayout.WEST);

        // And we have our layout:
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

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
        protected boolean isInstalled;
        protected VersionManifest.ExtensionVersion extensionVersion;

        public AppExtensionPlaceholder(T extension) {
            this.extension = extension;
            this.name = extension.getInfo().getName();
            this.isEnabled = false;
            this.isInstalled = false;
            this.extensionVersion = null;
        }

        public AppExtensionPlaceholder<T> setEnabled(boolean enabled) {
            isEnabled = enabled;
            return this;
        }

        public AppExtensionPlaceholder<T> setInstalled(boolean installed) {
            isInstalled = installed;
            return this;
        }

        public AppExtensionPlaceholder<T> setExtensionVersion(VersionManifest.ExtensionVersion version) {
            extensionVersion = version;

        }

        public boolean isEnabled() {
            return isEnabled;
        }

        public boolean isInstalled() {
            return isInstalled;
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
}
