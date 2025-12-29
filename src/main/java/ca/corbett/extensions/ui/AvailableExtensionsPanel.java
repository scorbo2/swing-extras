package ca.corbett.extensions.ui;

import ca.corbett.extensions.AppExtension;
import ca.corbett.extensions.AppExtensionInfo;
import ca.corbett.extensions.ExtensionManager;
import ca.corbett.extras.ListPanel;
import ca.corbett.extras.MessageUtil;
import ca.corbett.extras.crypt.SignatureUtil;
import ca.corbett.extras.image.ImageUtil;
import ca.corbett.extras.io.DownloadAdapter;
import ca.corbett.extras.io.DownloadManager;
import ca.corbett.extras.io.DownloadThread;
import ca.corbett.extras.progress.MultiProgressDialog;
import ca.corbett.extras.progress.SimpleProgressAdapter;
import ca.corbett.updates.DownloadedExtension;
import ca.corbett.updates.ExtensionDownloadThread;
import ca.corbett.updates.UpdateManager;
import ca.corbett.updates.UpdateSources;
import ca.corbett.updates.VersionManifest;
import ca.corbett.updates.VersionStringComparator;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
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
    protected PublicKey currentPublicKey;

    protected final String applicationName;
    protected final String applicationVersion;

    // Screenshot cache infrastructure
    protected final File screenshotCacheDir;
    protected final ConcurrentHashMap<String, File> screenshotCache;

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
        screenshotCache = new ConcurrentHashMap<>();
        screenshotCacheDir = createScreenshotCacheDirectory();
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

    /**
     * Invoked from ExtensionManagerDialog when our tab becomes active - if we have
     * no currently-selected UpdateSource, we'll prompt for one.
     * There is also a manual "refresh" button on this tab, but it may not be
     * immediately intuitive for the user when first visiting this tab to click it,
     * so we can make it happen automatically on first visit.
     */
    public void tabActivated() {
        if (currentUpdateSource == null) {
            promptForUpdateSource();
        }
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
        final VersionManifest.ExtensionVersion latestVersion = placeholder == null
                ? null
                : placeholder.getExtension().getHighestVersion().orElse(null);
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

            // If there are screenshots for this version, load them async:
            if (!latestVersion.getScreenshots().isEmpty()) {
                new ScreenshotDownloader(latestVersion, extPanel).start();
            }
        }
        detailsPanel.add(extPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    protected void refreshList() {
        if (updateManager == null || updateManager.getUpdateSources().isEmpty()) {
            getMessageUtil().info("This application does not define any update sources.\n"
                                          + "Dynamic extension download/install/upgrade is not available.");
            return;
        }

        promptForUpdateSource();
    }

    /**
     * Tries to select an UpdateSource intelligently and display it.
     * If there are no update sources, the view is cleared.
     * If there's only one update source, it is selected automatically.
     * If there are more than one, the user is prompted to pick one.
     * If an update source is chosen as a result of the above, its
     * version manifest and public key are downloaded and set as current.
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
        }

        // Otherwise, prompt the use to pick one:
        else {
            Object[] choices = updateManager.getUpdateSources().toArray();
            currentUpdateSource = (UpdateSources.UpdateSource)JOptionPane.showInputDialog(owner,
                                                                                          "Select which update source to query:",
                                                                                          "Select update source",
                                                                                          JOptionPane.QUESTION_MESSAGE,
                                                                                          null,
                                                                                          choices,
                                                                                          choices[0]);
        }

        if (currentUpdateSource == null) {
            // user hit cancel on update source chooser
            return;
        }
        downloadManager.downloadFile(currentUpdateSource.getVersionManifestUrl(),
                                     new VersionManifestDownloadListener());


        // Also grab this update source's public key if it has one:
        currentPublicKey = null;
        if (currentUpdateSource.hasPublicKey()) {
            downloadManager.downloadFile(currentUpdateSource.getPublicKeyUrl(), new DownloadAdapter() {
                @Override
                public void downloadComplete(DownloadThread thread, URL url, File result) {
                    if (result != null && result.exists()) {
                        try {
                            currentPublicKey = SignatureUtil.loadPublicKey(result);
                        }
                        catch (Exception e) {
                            log.log(Level.SEVERE, "Unable to download public key: " + e.getMessage(), e);
                        }
                    }
                }
            });
        }
        else {
            log.warning("Remote host does not specify a public key - extension verification will not be possible.");
        }

    }

    protected void setVersionManifest(VersionManifest manifest) {
        extensionListPanel.clear();
        detailsPanelMap.clear();

        // Starting in swing-extras 2.6, we now only consider the application's major version when
        // determining if an extension is compatible. The versioning convention that applications must
        // follow is to release a minor version if there are no extension-breaking changes, and a major version
        // if there are extension-breaking changes. So, an extension will be considered compatible if
        // its target application major version matches the application's major version. So, we will grab the
        // highest version of each extension that matches this application's major version.
        int appMajorVersion = AppExtensionInfo.extractMajorVersion(applicationVersion);
        if (appMajorVersion != 0) {
            // Get a list of the highest version of each extension that matches our application's major version:
            // (this list will be returned to us sorted by extension name in ascending order)
            for (VersionManifest.ExtensionVersion highestVersion :
                    manifest.getHighestExtensionVersionsForMajorAppVersion(appMajorVersion)) {

                // Grab its containing Extension and add it to our list:
                manifest.findExtensionForExtensionVersion(highestVersion)
                        .ifPresent(e -> extensionListPanel.addItem(new ExtensionPlaceholder(e, isInstalled(e))));
            }
        }

        // If we can't extract the major version from the application version, then fall back to exact matching:
        // TODO this code path will almost certainly never be hit... I'm tempted to just remove it entirely.
        //      But it's nice to have some kind of fallback for wonky error conditions (malformed version strings).
        else {
            // Scold the user about their poor versioning practices:
            log.warning("Unable to extract major version from application version: "
                                + applicationVersion
                                + " - falling back to exact version matching for extensions."
                                + " Application versions should be in X.Y format!");

            // Now let's hope there are extensions that target this exact application version:
            manifest.getApplicationVersions().stream()
                    .filter(version -> applicationVersion.equals(version.getVersion()))
                    .flatMap(version -> version.getExtensions().stream())
                    .sorted(Comparator.comparing(VersionManifest.Extension::getName))
                    .map(extension -> new ExtensionPlaceholder(extension, isInstalled(extension)))
                    .forEach(extensionListPanel::addItem);
        }

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

    /**
     * Invoked internally from the install and update actions, after the extension is downloaded,
     * to complete the process and then prompt to restart the application.
     */
    protected boolean extensionInstallCallback(DownloadedExtension downloadedExtension) {
        if (downloadedExtension == null
                || downloadedExtension.getJarFile() == null
                || !downloadedExtension.getJarFile().exists()) {
            getMessageUtil().info("Unable to complete extension install: no jar file downloaded.");
            return false;
        }

        // This is a very wonky case, but if our ExtensionManager is for some reason not configured
        // with an extensions directory, we can't proceed:
        File extensionsDir = extensionManager.getExtensionsDirectory();
        if (extensionsDir == null) {
            getMessageUtil().info("Unable to install new extensions: application extension dir is not set.");
            return false;
        }
        if (!extensionsDir.exists() || !extensionsDir.canWrite()) {
            getMessageUtil().info("Unable to install new extensions: extension dir does not exist or is not writable.\n"
                                          + extensionsDir.getAbsolutePath());
            return false;
        }

        // If we don't have a public key, warn:
        if (currentPublicKey == null) {
            if (JOptionPane.showConfirmDialog(owner,
                                              "The selected update source does not specify a public key.\n"
                                                      + "It is not possible to verify extensions downloaded from this source.\n"
                                                      + "Only download extensions fromm sources that you trust!\n\n"
                                                      + "Proceed without verification?",
                                              "No public key",
                                              JOptionPane.YES_NO_OPTION,
                                              JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
                return false;
            }
        }

        else {
            // If we didn't get a signature, prompt with a huge warning:
            if (downloadedExtension.getSignatureFile() == null || !downloadedExtension.getSignatureFile().exists()) {
                if (JOptionPane.showConfirmDialog(owner,
                                                  "This extension is not digitally signed.\n"
                                                          + "It is not possible to verify its integrity.\n\n"
                                                          + "Are you sure you wish to proceed with the installation?",
                                                  "Extension not signed",
                                                  JOptionPane.YES_NO_OPTION,
                                                  JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
                    return false;
                }
            }

            // Otherwise, do the verification:
            else {
                try {
                    if (!SignatureUtil.verifyFile(downloadedExtension.getJarFile(),
                                                  downloadedExtension.getSignatureFile(),
                                                  currentPublicKey)) {
                        if (JOptionPane.showConfirmDialog(owner,
                                                          "This extension's digital signature does not match!\n"
                                                                  + "The extension may not have downloaded correctly,\n"
                                                                  + "or the extension may have been tampered with.\n"
                                                                  + "It is STRONGLY RECOMMENDED not to proceed.\n\n"
                                                                  + "Proceed anyway?",
                                                          "Verification failed!",
                                                          JOptionPane.YES_NO_OPTION,
                                                          JOptionPane.ERROR_MESSAGE) == JOptionPane.NO_OPTION) {
                            return false;
                        }
                    }
                    else {
                        log.info("Signature matches! The downloaded extension has a valid signature.");
                    }
                }
                catch (Exception e) {
                    log.log(Level.SEVERE, "Problem verifying signature: " + e.getMessage(), e);
                    if (JOptionPane.showConfirmDialog(owner,
                                                      "There was a problem verifying this extension:\n"
                                                              + e.getMessage()
                                                              + "\n\nProceed without verification?",
                                                      "Unable to verify extension",
                                                      JOptionPane.YES_NO_OPTION,
                                                      JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
                        return false;
                    }
                }
            }
        }

        // If we make it to this point, the extension has either been verified, or the user has
        // made a deliberate decision to proceed without verification.
        try {
            Files.move(downloadedExtension.getJarFile().toPath(),
                       new File(extensionsDir, downloadedExtension.getJarFile().getName()).toPath(),
                       StandardCopyOption.REPLACE_EXISTING);
            return true;
        }
        catch (IOException ioe) {
            getMessageUtil().error("Problem saving extension jar: " + ioe.getMessage(), ioe);
            return false;
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
                SwingUtilities.invokeLater(() -> setVersionManifest(manifest));
            }
            catch (IOException ioe) {
                downloadFailed(thread, url, "Problem parsing manifest: " + ioe.getMessage());
            }
        }
    }

    protected class InstallAction extends AbstractAction {

        private final ExtensionPlaceholder extension;
        private final List<ActionListener> completionListeners = new ArrayList<>();

        public InstallAction(ExtensionPlaceholder placeholder) {
            super("Install");
            this.extension = placeholder;
        }

        public void addCompletionListener(ActionListener listener) {
            completionListeners.add(listener);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (currentUpdateSource == null) {
                getMessageUtil().info("There is no update source selected.");
                return;
            }
            VersionManifest.ExtensionVersion latestVersion = extension.getExtension().getHighestVersion().orElse(null);
            if (latestVersion == null) {
                getMessageUtil().info("Unable to determine latest version of extension "
                                              + extension.extension.getName()
                                              + " - cannot proceed with install.");
                return;
            }
            MultiProgressDialog progressDialog = new MultiProgressDialog(owner, "Downloading...");
            progressDialog.setInitialShowDelayMS(500);
            final ExtensionDownloadThread workerThread = new ExtensionDownloadThread(downloadManager,
                                                                                     currentUpdateSource,
                                                                                     latestVersion);
            workerThread.setDownloadOptions(ExtensionDownloadThread.Options.JarAndSignature);
            workerThread.addProgressListener(new SimpleProgressAdapter() {
                @Override
                public void progressComplete() {
                    if (!workerThread.getErrors().isEmpty()) {
                        String errorText = String.join("\n", workerThread.getErrors());
                        getMessageUtil().error("Problem downloading extension:\n" + errorText);
                        return;
                    }
                    SwingUtilities.invokeLater(() -> {
                        if (extensionInstallCallback(workerThread.getDownloadedExtension())) {
                            String version = latestVersion.getExtInfo().getVersion();
                            log.info("Successfully installed extension "
                                             + extension.extension.getName()
                                             + " version "
                                             + version);

                            // Execute any additional completion listeners we have before we prompt for restart:
                            for (ActionListener listener : completionListeners) {
                                listener.actionPerformed(null);
                            }

                            isRestartRequired = true;
                            if (updateManager != null) {
                                updateManager.showApplicationRestartPrompt(owner);
                            }
                            else {
                                getMessageUtil().info("Changes will take effect when the application is restarted.");
                            }
                        }
                    });
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
            if (currentUpdateSource == null) {
                getMessageUtil().info("There is no update source selected.");
                return;
            }
            final File jarFile = extensionManager.findExtensionJarByExtensionName(extension.extension.getName());
            VersionManifest.ExtensionVersion latestVersion = extension.getExtension().getHighestVersion().orElse(null);
            if (latestVersion == null) {
                getMessageUtil().info("Unable to determine latest version of extension "
                                              + extension.extension.getName()
                                              + " - cannot proceed with update.");
                return;
            }
            String latestVersionStr = latestVersion.getExtInfo().getVersion();
            if (jarFile == null) {
                getMessageUtil().info("The extension \"" + extension.getExtension().getName()
                                              + "\" is a built-in extension.\n"
                                              + "It cannot be updated.");
                return;
            }

            // Give user a chance to back out:
            if (JOptionPane.showConfirmDialog(owner,
                                              "Extension "
                                                      + extension.extension.getName()
                                                      + " will be updated to version "
                                                      + latestVersionStr
                                                      + "\n\nProceed?",
                                              "Confirm",
                                              JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                return;
            }

            InstallAction installAction = new InstallAction(extension);

            // Don't mark the old extension for deletion unless the install of the new version succeeds:
            installAction.addCompletionListener(e1 -> {
                log.info("Uninstalling older version of extension "
                                 + extension.extension.getName()
                                 + ": "
                                 + jarFile.getName());
                jarFile.deleteOnExit();
            });
            installAction.actionPerformed(null);

            // No need to prompt for restart as the InstallAction will handle it.
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
            File jarFile = extensionManager.findExtensionJarByExtensionName(extension.extension.getName());
            if (jarFile == null) {
                getMessageUtil().info("The extension \"" + extension.getExtension().getName()
                                              + "\" is a built-in extension.\n"
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

            log.info("Uninstalling extension " + extension.extension.getName() + ": " + jarFile.getName());
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
     * Creates a temporary directory for caching screenshot images during this panel's lifetime.
     * Returns null if creation fails.
     */
    protected File createScreenshotCacheDirectory() {
        try {
            File tempDir = new File(System.getProperty("java.io.tmpdir"));
            File cacheDir = new File(tempDir, "ext-screenshots-" + System.currentTimeMillis());
            if (cacheDir.mkdir()) {
                log.info("Created screenshot cache directory: " + cacheDir.getAbsolutePath());
                return cacheDir;
            }
            else {
                log.warning("Failed to create screenshot cache directory");
                return null;
            }
        }
        catch (Exception e) {
            log.log(Level.WARNING, "Error creating screenshot cache directory: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Cleans up the temporary screenshot cache directory. We rely on the caller to invoke
     * this, as we don't know when the panel is no longer needed. This is typically invoked
     * from the ExtensionManagerDialog when it is closed.
     */
    public void cleanupScreenshotCache() {
        if (screenshotCacheDir == null || !screenshotCacheDir.exists()) {
            return;
        }

        try {
            File[] files = screenshotCacheDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!file.delete()) {
                        log.warning("Failed to delete cached screenshot: " + file.getName());
                    }
                }
            }
            if (!screenshotCacheDir.delete()) {
                log.warning("Failed to delete screenshot cache directory");
            }
            else {
                log.info("Cleaned up screenshot cache directory");
            }
        }
        catch (Exception e) {
            log.log(Level.WARNING, "Error cleaning up screenshot cache: " + e.getMessage(), e);
        }
        finally {
            if (screenshotCache != null) {
                screenshotCache.clear();
            }
        }
    }

    /**
     * Generates a cache key from a URL to use as filename.
     */
    protected String getCacheKeyForUrl(URL url) {
        if (url == null) {
            return null;
        }
        // Use the hash of the entire URL as part of the cache key to avoid collisions:
        return Integer.toHexString(url.toString().hashCode());
    }

    /**
     * Internal helper class to grab all the screenshots for the given extension version
     * and load them into the given ExtensionDetailsPanel.
     * <p>
     * Screenshots are cached in a temporary directory to avoid re-downloading them
     * when the same extension is selected multiple times.
     * </p>
     */
    private class ScreenshotDownloader {
        private final VersionManifest.ExtensionVersion extVersion;
        private final ExtensionDetailsPanel extDetailsPanel;

        public ScreenshotDownloader(VersionManifest.ExtensionVersion version,
                                    ExtensionDetailsPanel panel) {
            this.extVersion = version;
            this.extDetailsPanel = panel;
        }

        public void start() {
            // Check if we already have all screenshots cached
            List<String> screenshotUrls = extVersion.getScreenshots();
            List<File> cachedFiles = new ArrayList<>();
            boolean allCached = true;

            for (String screenshotPath : screenshotUrls) {
                URL screenshotUrl = UpdateManager.resolveUrl(currentUpdateSource.getBaseUrl(), screenshotPath);
                if (screenshotUrl == null) {
                    allCached = false;
                    break;
                }

                String cacheKey = getCacheKeyForUrl(screenshotUrl);
                if (cacheKey == null) {
                    allCached = false;
                    break;
                }
                File cachedFile = screenshotCache.get(cacheKey);

                if (cachedFile != null && cachedFile.exists()) {
                    cachedFiles.add(cachedFile);
                }
                else {
                    allCached = false;
                    break;
                }
            }

            // If all screenshots are cached, load them directly without downloading
            if (allCached && !cachedFiles.isEmpty()) {
                log.info("Loading " + cachedFiles.size() + " cached screenshots for extension: " + extVersion
                        .getExtInfo().getName());
                loadCachedScreenshots(cachedFiles);
                return;
            }

            // Otherwise, proceed with downloading
            MultiProgressDialog progressDialog = new MultiProgressDialog(owner, "Downloading...");
            ExtensionDownloadThread worker = new ExtensionDownloadThread(downloadManager,
                                                                         currentUpdateSource,
                                                                         extVersion);
            worker.setDownloadOptions(ExtensionDownloadThread.Options.ScreenshotsOnly);
            worker.addProgressListener(new SimpleProgressAdapter() {
                @Override
                public void progressComplete() {
                    if (!worker.getErrors().isEmpty()) {
                        for (String error : worker.getErrors()) {
                            // Log it but proceed as it may have downloaded at least some of them
                            log.log(Level.SEVERE, "Error downloading extension screenshots: " + error);
                        }
                    }
                    processResults(worker.getDownloadedExtension());
                }
            });
            progressDialog.runWorker(worker, true);
        }

        private void loadCachedScreenshots(List<File> cachedFiles) {
            for (File cachedFile : cachedFiles) {
                try {
                    BufferedImage image = ImageUtil.loadImage(cachedFile);
                    SwingUtilities.invokeLater(() -> extDetailsPanel.addScreenshot(image, false));
                }
                catch (IOException ioe) {
                    log.log(Level.SEVERE, "Problem loading cached screenshot: " + ioe.getMessage(), ioe);
                }
            }
        }

        public void processResults(DownloadedExtension extensionFiles) {
            List<String> screenshotUrls = extVersion.getScreenshots();
            List<File> downloadedFiles = extensionFiles.getScreenshots();

            for (int i = 0; i < downloadedFiles.size() && i < screenshotUrls.size(); i++) {
                File screenshotFile = downloadedFiles.get(i);
                String screenshotPath = screenshotUrls.get(i);
                URL screenshotUrl = UpdateManager.resolveUrl(currentUpdateSource.getBaseUrl(), screenshotPath);

                try {
                    // Cache the downloaded file if caching is enabled
                    if (screenshotCacheDir != null && screenshotUrl != null) {
                        String cacheKey = getCacheKeyForUrl(screenshotUrl);
                        if (cacheKey != null) {
                            String extension = DownloadManager.getFileExtension(screenshotFile.getName());
                            File cachedFile = new File(screenshotCacheDir, cacheKey + extension);
                            Files.copy(screenshotFile.toPath(), cachedFile.toPath(),
                                       StandardCopyOption.REPLACE_EXISTING);
                            screenshotCache.put(cacheKey, cachedFile);
                            log.fine("Cached screenshot: " + cacheKey);
                        }
                    }

                    // Do I/O on the worker thread:
                    BufferedImage image = ImageUtil.loadImage(screenshotFile);

                    // Do UI updates on the EDT:
                    SwingUtilities.invokeLater(() -> extDetailsPanel.addScreenshot(image, false));
                }
                catch (IOException ioe) {
                    log.log(Level.SEVERE, "Problem parsing screenshot: " + ioe.getMessage(), ioe);
                }
            }
        }
    }

    protected MessageUtil getMessageUtil() {
        if (messageUtil == null) {
            messageUtil = new MessageUtil(owner, log);
        }
        return messageUtil;
    }
}
