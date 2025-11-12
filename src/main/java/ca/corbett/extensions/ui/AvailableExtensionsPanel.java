package ca.corbett.extensions.ui;

import ca.corbett.extras.ListPanel;
import ca.corbett.extras.MessageUtil;
import ca.corbett.updates.UpdateSources;
import ca.corbett.updates.VersionManifest;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

public class AvailableExtensionsPanel extends JPanel {

    private static final Logger log = Logger.getLogger(AvailableExtensionsPanel.class.getName());

    protected MessageUtil messageUtil;
    protected final Window owner;
    protected final UpdateSources updateSources;

    protected final JPanel contentPanel = new JPanel(new BorderLayout());
    protected final JPanel headerPanel = new JPanel(new BorderLayout());
    protected final JPanel detailsPanel = new JPanel(new BorderLayout());

    protected final Map<String, ExtensionDetailsPanel> detailsPanelMap;
    protected final ListPanel<ExtensionPlaceholder> extensionListPanel;
    protected ExtensionDetailsPanel emptyPanel;

    public AvailableExtensionsPanel(Window owner, UpdateSources updateSources) {
        this.owner = owner;
        this.updateSources = updateSources;
        detailsPanelMap = new HashMap<>();
        extensionListPanel = new ListPanel<>(List.of(new RefreshAction()));
        extensionListPanel.setPreferredSize(new Dimension(200, 200));
        extensionListPanel.setMinimumSize(new Dimension(200, 1));
        extensionListPanel.setMaximumSize(new Dimension(200, Integer.MAX_VALUE));
        extensionListPanel.addListSelectionListener(e -> listSelectionChanged());
        initComponents();
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
        contentPanel.add(emptyPanel);
    }

    protected void listSelectionChanged() {
        // TODO
    }

    protected void refreshList() {
        if (updateSources == null || updateSources.getUpdateSources().isEmpty()) {
            getMessageUtil().info("This application does not define any update sources.\n"
                                          + "Dynamic extension download/install/upgrade is not available.");
            return;
        }

        // If there's only one, just do it:
        if (updateSources.getUpdateSources().size() == 1) {

        }

        // otherwise, prompt for which one to use:
        else {
            
        }
    }

    protected class RefreshAction extends AbstractAction {

        public RefreshAction() {
            super("Refresh");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            refreshList();
        }
    }

    protected static class ExtensionPlaceholder {
        private final VersionManifest.Extension extension;
        private final boolean isInstalled;

        public ExtensionPlaceholder(VersionManifest.Extension extension, boolean isInstalled) {
            this.extension = extension;
            this.isInstalled = isInstalled;
        }

        public VersionManifest.Extension getExtension() {
            return extension;
        }

        public boolean isInstalled() {
            return isInstalled;
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof ExtensionPlaceholder that)) { return false; }
            return isInstalled == that.isInstalled && Objects.equals(extension, that.extension);
        }

        @Override
        public int hashCode() {
            return Objects.hash(extension, isInstalled);
        }

        @Override
        public String toString() {
            String string = extension.getName();
            if (isInstalled) {
                string += " (installed)";
            }
            return string;
        }
    }

    protected MessageUtil getMessageUtil() {
        if (messageUtil == null) {
            messageUtil = new MessageUtil(owner, log);
        }
        return messageUtil;
    }
}
