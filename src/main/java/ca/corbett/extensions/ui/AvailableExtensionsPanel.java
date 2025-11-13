package ca.corbett.extensions.ui;

import ca.corbett.extras.ListPanel;
import ca.corbett.extras.MessageUtil;
import ca.corbett.updates.UpdateManager;
import ca.corbett.updates.UpdateSources;
import ca.corbett.updates.VersionManifest;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
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

    protected final String applicationName;
    protected final String applicationVersion;

    public AvailableExtensionsPanel(Window owner, UpdateSources updateSources, String appName, String appVersion) {
        this.owner = owner;
        this.updateSources = updateSources;
        this.applicationName = appName;
        this.applicationVersion = appVersion;
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

        // TODO let's do it:
        UpdateSources.UpdateSource updateSource = getUpdateSource();
        UpdateManager manager = new UpdateManager(updateSources);
        //manager.retrieveVersionManifest(updateSource); // I want this to be synchronous...
        // Like, I don't want to have to set up callback listeners here and wait for the file to come in
        // I want UpdateManager to block until it has the file (or it errors out), and then return it to me
    }

    /**
     * Assuming there is at least one update source, this method will return one, else null.
     * If there are more than one update sources available, this method will prompt the user
     * in a dialog to pick which one to use, and then return that one (or null if the user cancels).
     */
    protected UpdateSources.UpdateSource getUpdateSource() {
        // If there are none, return null:
        if (updateSources == null || updateSources.getUpdateSources().isEmpty()) {
            return null;
        }

        // If there's exactly one, then the choice is easy:
        if (updateSources.getUpdateSources().size() == 1) {
            return updateSources.getUpdateSources().get(0);
        }

        // If we get here, there are more than one. Prompt for user input:
        Object[] choices = updateSources.getUpdateSources().toArray();
        return (UpdateSources.UpdateSource)JOptionPane.showInputDialog(owner,
                                                                       "Select which update source to query:",
                                                                       "Select update source",
                                                                       JOptionPane.QUESTION_MESSAGE,
                                                                       null,
                                                                       choices,
                                                                       choices[0]);
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
