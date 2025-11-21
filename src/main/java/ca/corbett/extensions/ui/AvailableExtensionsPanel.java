package ca.corbett.extensions.ui;

import ca.corbett.extensions.AppExtension;
import ca.corbett.extensions.ExtensionManager;
import ca.corbett.extras.ListPanel;
import ca.corbett.extras.MessageUtil;
import ca.corbett.extras.io.DownloadAdapter;
import ca.corbett.extras.io.DownloadManager;
import ca.corbett.extras.io.DownloadThread;
import ca.corbett.extras.progress.MultiProgressDialog;
import ca.corbett.extras.progress.SimpleProgressAdapter;
import ca.corbett.updates.ExtensionDownloadThread;
import ca.corbett.updates.UpdateManager;
import ca.corbett.updates.UpdateSources;
import ca.corbett.updates.VersionManifest;
import ca.corbett.updates.VersionStringComparator;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * This panel queries a remote UpdateSource for a list of compatible extensions
 * with this application, then allows them to be downloaded, installed, or updated.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.5
 */
public class AvailableExtensionsPanel extends JPanel {

    private static final Logger log = Logger.getLogger(AvailableExtensionsPanel.class.getName());

    protected MessageUtil messageUtil;
    protected final Window owner;
    protected final ExtensionManager<?> extensionManager;
    protected final UpdateManager updateManager;
    protected final DownloadManager downloadManager;
    protected boolean isRestartRequired;

    protected final JPanel contentPanel = new JPanel(new BorderLayout());
    protected final JPanel headerPanel = new JPanel(new BorderLayout());
    protected final JPanel detailsPanel = new JPanel(new BorderLayout());

    protected final Map<String, ExtensionDetailsPanel> detailsPanelMap;
    protected final ListPanel<ExtensionPlaceholder> extensionListPanel;
    protected ExtensionDetailsPanel emptyPanel;
    protected UpdateSources.UpdateSource currentUpdateSource;

    protected final String applicationName;
    protected final String applicationVersion;

    public AvailableExtensionsPanel(Window owner, ExtensionManager<?> extManager, UpdateManager updateManager, String appName, String appVersion) {
        this.owner = owner;
        this.updateManager = updateManager;
        this.extensionManager = extManager;
        this.applicationName = appName;
        this.applicationVersion = appVersion;
        this.downloadManager = new DownloadManager();
        this.isRestartRequired = false;
        this.currentUpdateSource = null;
        detailsPanelMap = new HashMap<>();
        extensionListPanel = new ListPanel<>(List.of(new RefreshAction()));
        extensionListPanel.setPreferredSize(new Dimension(200, 200));
        extensionListPanel.setMinimumSize(new Dimension(200, 1));
        extensionListPanel.setMaximumSize(new Dimension(200, Integer.MAX_VALUE));
        extensionListPanel.addListSelectionListener(e -> listSelectionChanged());
        initComponents();
    }

    public boolean isRestartRequired() {
        return isRestartRequired;
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
        detailsPanel.add(emptyPanel);
    }

    /**
     * When an extension is selected in the left menu, we display its details,
     * and provide controls for installing it (if not already installed),
     * updating it (if already installed but a newer version is available),
     * or uninstalling it (if already installed).
     */
    protected void listSelectionChanged() {
        headerPanel.removeAll();
        detailsPanel.removeAll();
        final ExtensionPlaceholder placeholder = extensionListPanel.getSelected();
        final VersionManifest.ExtensionVersion latestVersion =
                placeholder == null ? null : findLatestExtVersion(placeholder.getExtension());
        if (placeholder == null || latestVersion == null) {
            headerPanel.add(new ExtensionTitleBar(null));
            detailsPanel.add(emptyPanel);
            contentPanel.revalidate();
            contentPanel.repaint();
            return; // no selection
        }

        // Create a title bar for this extension:
        ExtensionTitleBar titleBar = new ExtensionTitleBar(latestVersion.getExtInfo().getName());
        AppExtension installedExtension = extensionManager.findExtensionByName(placeholder.getExtension().getName());

        // If this extension is installed, we can add an "uninstall" button:
        if (installedExtension != null) {
            titleBar.setUninstallAction(new UninstallAction(placeholder));

            // If this extension has a newer version available, we can add an "update button":
            if (VersionStringComparator.isOlderThan(installedExtension.getInfo().getVersion(),
                                                    latestVersion.getExtInfo().getVersion())) {
                titleBar.setUpdateAction(new UpdateAction(placeholder));
            }
        }

        // Otherwise, if this extension is not installed, we can add an "install" button:
        else {
            titleBar.setInstallAction(new InstallAction(placeholder));
        }
        headerPanel.add(titleBar);

        // Now we can find or create a details panel for this extension:
        ExtensionDetailsPanel extPanel = detailsPanelMap.get(placeholder.extension.getName());
        if (extPanel == null) {
            // We haven't visited this one before, so create a new panel for it now:
            extPanel = new ExtensionDetailsPanel(owner, latestVersion.getExtInfo());
            extPanel.setNameFieldVisible(false); // we have our own title bar
            detailsPanelMap.put(placeholder.extension.getName(), extPanel);

            // TODO download screenshots and display
        }
        detailsPanel.add(extPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    protected VersionManifest.ExtensionVersion findLatestExtVersion(VersionManifest.Extension extension) {
        if (extension == null || extension.getVersions() == null || extension.getVersions().isEmpty()) {
            return null;
        }

        // Stream all versions of this extension, sort them by version and find the highest one:
        return extension
                .getVersions()
                .stream()
                .filter(ev -> ev.getExtInfo() != null)
                .max(Comparator.comparing(ev -> ev.getExtInfo().getVersion(), new VersionStringComparator()))
                .orElse(null);
    }

    protected void refreshList() {
        if (updateManager == null || updateManager.getUpdateSources().isEmpty()) {
            getMessageUtil().info("This application does not define any update sources.\n"
                                          + "Dynamic extension download/install/upgrade is not available.");
            return;
        }

        promptForUpdateSource();
        if (currentUpdateSource == null) {
            // user hit cancel on update source chooser
            return;
        }
        downloadManager.downloadFile(currentUpdateSource.getVersionManifestUrl(),
                                     new VersionManifestDownloadListener());
    }

    /**
     * Assuming there is at least one update source, this method will return one, else null.
     * If there are more than one update sources available, this method will prompt the user
     * in a dialog to pick which one to use, and then return that one (or null if the user cancels).
     * The result is stored in currentUpdateSource (may be null);
     */
    protected void promptForUpdateSource() {
        // If there are none, return null:
        if (updateManager == null || updateManager.getUpdateSources().isEmpty()) {
            currentUpdateSource = null;
            return;
        }

        // If there's exactly one, then the choice is easy:
        if (updateManager.getUpdateSources().size() == 1) {
            currentUpdateSource = updateManager.getUpdateSources().get(0);
            return;
        }

        // If we get here, there are more than one. Prompt for user input:
        Object[] choices = updateManager.getUpdateSources().toArray();
        currentUpdateSource = (UpdateSources.UpdateSource)JOptionPane.showInputDialog(owner,
                                                                       "Select which update source to query:",
                                                                       "Select update source",
                                                                       JOptionPane.QUESTION_MESSAGE,
                                                                       null,
                                                                       choices,
                                                                       choices[0]);
    }

    protected void setVersionManifest(VersionManifest manifest) {
        extensionListPanel.clear();
        detailsPanelMap.clear();

        // Find all extensions for our version of the application, sort them, and add them to the list:
        manifest.getApplicationVersions().stream()
                .filter(version -> applicationVersion.equals(version.getVersion()))
                .flatMap(version -> version.getExtensions().stream())
                .sorted(Comparator.comparing(VersionManifest.Extension::getName))
                .map(extension -> new ExtensionPlaceholder(extension, isInstalled(extension)))
                .forEach(extensionListPanel::addItem);

        if (!extensionListPanel.isEmpty()) {
            extensionListPanel.selectItem(0); // select 1st
        }
        else {
            getMessageUtil().info("No extensions found",
                                  "The given update source does not publish any extensions for this version of the application.");
        }
        extensionListPanel.revalidate();
        extensionListPanel.repaint();
    }

    protected boolean isInstalled(VersionManifest.Extension extension) {
        if (extension == null || extension.getName() == null) {
            return false;
        }
        return extensionManager.findExtensionByName(extension.getName()) != null;
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

    /**
     * Listens to our DownloadManager for a VersionManifest file to be downloaded, then
     * parses it and displays it in our UI.
     *
     * @author <a href="https://github.com/scorbo2">scorbo2</a>
     */
    protected class VersionManifestDownloadListener extends DownloadAdapter {

        @Override
        public void downloadFailed(DownloadThread thread, URL url, String errorMsg) {
            getMessageUtil().error("Error", "Unable to retrieve version manifest: " + errorMsg);
        }

        @Override
        public void downloadComplete(DownloadThread thread, URL url, File result) {
            if (result == null || !result.exists() || !result.isFile() || result.length() == 0) {
                downloadFailed(thread, url, "Locally downloaded file is empty.");
                return;
            }

            try {
                VersionManifest manifest = VersionManifest.fromFile(result);
                setVersionManifest(manifest);
            }
            catch (IOException ioe) {
                downloadFailed(thread, url, "Problem parsing manifest: " + ioe.getMessage());
            }
        }
    }

    protected class InstallAction extends AbstractAction {

        private final ExtensionPlaceholder extension;

        public InstallAction(ExtensionPlaceholder placeholder) {
            super("Install");
            this.extension = placeholder;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (currentUpdateSource == null) {
                getMessageUtil().info("There is no update source selected.");
                return;
            }
            MultiProgressDialog progressDialog = new MultiProgressDialog(owner, "Downloading...");
            progressDialog.setInitialShowDelayMS(500);
            final ExtensionDownloadThread workerThread = new ExtensionDownloadThread(downloadManager,
                                                                                     currentUpdateSource,
                                                                                     findLatestExtVersion(
                                                                                             extension.getExtension()));
            workerThread.addProgressListener(new SimpleProgressAdapter() {
                @Override
                public void progressComplete() {
                    // TODO remove this debug code, just testing
                    for (String error : workerThread.getErrors()) {
                        System.out.println("ERROR: " + error);
                    }
                    System.out.println("Jar: " + workerThread.getDownloadedExtension().getJarFile().getAbsolutePath());
                    File sigFile = workerThread.getDownloadedExtension().getSignatureFile();
                    if (sigFile == null) {
                        System.out.println("No signature.");
                    }
                    else {
                        System.out.println("Signature: " + sigFile.getAbsolutePath());
                    }
                    System.out.println(
                            "Found " + workerThread.getDownloadedExtension().getScreenshots().size() + " screenshots.");

                    if (!workerThread.getErrors().isEmpty()) {
                        // TODO move extension jar to app extensions dir... wait, where is that? Do we know?
                        //      note we can leave the signature file and screenshots behind as we don't need them
                        //      wait... why did we download screenshots for an install if we don't need them...
                    }
                    else {
                        isRestartRequired = true;
                        updateManager.showApplicationRestartPrompt(owner);
                    }
                }
            });
            progressDialog.runWorker(workerThread, true);
        }
    }

    protected class UpdateAction extends AbstractAction {

        private final ExtensionPlaceholder extension;

        public UpdateAction(ExtensionPlaceholder placeholder) {
            super("Update");
            this.extension = placeholder;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Existing jar is out of date, need to pull new one
            // TODO uninstall existing one
            // TODO download jar, sig file, public key, do signature match, prompt user, copy into place, prompt restart
        }
    }

    protected class UninstallAction extends AbstractAction {

        private final ExtensionPlaceholder extension;

        public UninstallAction(ExtensionPlaceholder placeholder) {
            super("Uninstall");
            this.extension = placeholder;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // TODO nuke existing one, prompt restart
        }
    }


    protected MessageUtil getMessageUtil() {
        if (messageUtil == null) {
            messageUtil = new MessageUtil(owner, log);
        }
        return messageUtil;
    }
}
