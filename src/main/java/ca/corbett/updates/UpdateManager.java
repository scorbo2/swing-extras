package ca.corbett.updates;

import ca.corbett.extras.crypt.SignatureUtil;
import ca.corbett.extras.image.ImageUtil;
import ca.corbett.extras.io.DownloadAdapter;
import ca.corbett.extras.io.DownloadManager;
import ca.corbett.extras.io.DownloadThread;
import ca.corbett.extras.io.FileSystemUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

/**
 * Applications built with swing-extras have a way of detecting if they are out of date, or if any of their
 * currently-installed extensions are out of date. Further, applications have a way to discover new
 * extensions, download them, and install them. This UpdateManager class helps manage this feature.
 * <p>
 *     <b>How do I use this?</b> - Start by creating a VersionManifest for your application and all
 *     of its extensions. There is an application to help you do this! See
 *     <a href="https://github.com/scorbo2/ext-packager">ExtPackager</a> on GitHub. Once your VersionManifest
 *     is ready, you can upload it to your web server. Then, you create an UpdateSources json which
 *     points to your VersionManifest, and you then bundle this UpdateSources with your application.
 *     Then, after your application is released, you can update the published VersionManifest on your
 *     web host whenever you have a new extension, or a new version of an existing extension. Your application
 *     can dynamically discover the new extension or version, and offer the ability to download and
 *     install it!
 * </p>
 * <p>
 *     <b>Digital signing</b> - you can optionally digitally sign your extension jars so that your application
 *     can verify that they come from a known good source. The ExtPackager application can walk you through
 *     the process of generating a key pair, signing your extensions with the private key, and uploading
 *     the public key to your web host. This is all entirely optional - if you are hosting on a trusted
 *     internal server (like on a local network), you can skip this step entirely. If a signature and a public
 *     key are provided, this class will provide you with the signature file for each jar, and the public
 *     key that you can use for signature verification.
 * </p>
 * <p>
 *     <b>How do I set all this up?</b> - There's a helper application called
 *     <a href="https://github.com/scorbo2/ext-packager">ExtPackager</a> that can walk you through the process
 *     of setting up your UpdateSources json and your VersionManifest, and can also help you with things like
 *     digitally signing your extension jars, providing screenshots for each version, and uploading to your
 *     web host via FTP. You don't have to write this json by hand!
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.5
 */
public class UpdateManager {

    /**
     * The process exit code that signals to our launcher script that we want to
     * restart the application (like after a new extension is installed).
     * Don't change this value without also updating the launcher script!
     * If they're not in sync, the application will exit and then not restart.
     */
    public static final int APPLICATION_RESTART = 100;

    protected final UpdateSources updateSources;
    protected final List<UpdateManagerListener> listeners = new ArrayList<>();
    protected final List<ShutdownHook> shutdownHooks = new ArrayList<>();
    protected final DownloadManager downloadManager;

    /**
     * Creates a new UpdateManager using the given update sources json file. If the file fails to parse,
     * an exception is thrown. Otherwise, you can begin making retrieval requests with the new
     * UpdateManager instance. Be sure to register yourself as an UpdateManagerListener before you
     * invoke any of the retrieve methods!
     */
    public UpdateManager(File sourceFile) throws JsonSyntaxException, IOException {
        Gson gson = new GsonBuilder().create();
        this.updateSources = gson.fromJson(FileSystemUtil.readFileToString(sourceFile), UpdateSources.class);
        this.downloadManager = new DownloadManager();
    }

    /**
     * If you have already parsed the UpdateSources instance, you can supply it to this constructor.
     */
    public UpdateManager(UpdateSources sources) {
        this.updateSources = sources;
        this.downloadManager = new DownloadManager();
    }

    /**
     * Returns the application name as defined in the update sources json that was used to instantiate
     * this UpdateManager.
     */
    public String getApplicationName() {
        return updateSources.getApplicationName();
    }

    /**
     * Returns the UpdateSources that are available in this UpdateManager. Each one can either specify
     * a remote, web-based source, or a local filesystem-based source. The type of source is transparent
     * to the caller, but you must specify the update source to be queried when you invoke any
     * retrieval method.
     */
    public List<UpdateSources.UpdateSource> getUpdateSources() {
        return new ArrayList<>(updateSources.getUpdateSources());
    }

    /**
     * Reports whether SNAPSHOT versions should be shown to the user and allowed for
     * download and installation. The default value is false, unless explicitly enabled
     * in the UpdateSources json or via setAllowSnapshots() in this class.
     */
    public boolean isAllowSnapshots() {
        return updateSources.isAllowSnapshots();
    }

    /**
     * Decides whether SNAPSHOT versions should be shown to the user and allowed for
     * download and installation. The default value is false, unless explicitly enabled
     * in the UpdateSources json or via setAllowSnapshots() in this class.
     */
    public void setAllowSnapshots(boolean allow) {
        updateSources.setAllowSnapshots(allow);
    }

    /**
     * Register to receive notifications from this UpdateManager as various remote resources are downloaded.
     */
    public void addUpdateManagerListener(UpdateManagerListener listener) {
        listeners.add(listener);
    }

    /**
     * Stop listening to this UpdateManager for notifications.
     */
    public void removeUpdateManagerListener(UpdateManagerListener listener) {
        listeners.remove(listener);
    }

    /**
     * Requests the VersionManifest for the given UpdateSource (use getUpdateSources to enumerate the
     * available UpdateSources in this UpdateManager). Make sure you have added yourself as a
     * listener via addUpdateManagerListener() before invoking this request.
     */
    public void retrieveVersionManifest(UpdateSources.UpdateSource updateSource) {
        downloadManager.downloadFile(updateSource.getVersionManifestUrl(), new VersionManifestDownloadListener());
    }

    /**
     * Requests the public key for the given UpdateSource (use getUpdateSources to enumerate the
     * available UpdateSources in this UpdateManager). Make sure you have added yourself as a
     * listener via addUpdateManagerListener() before invoking this request.
     * <p>
     * <b>NOTE:</b> Not all update sources will define a public key, as jar signing is optional.
     * So, the result may be null!
     * </p>
     */
    public void retrievePublicKey(UpdateSources.UpdateSource updateSource) {
        if (!updateSource.hasPublicKey()) {
            firePublicKeyDownloaded(null, null);
        }
        downloadManager.downloadFile(updateSource.getPublicKeyUrl(), new PublicKeyDownloadListener());
    }

    /**
     * Requests the given extension jar from the given UpdateSource (use getUpdateSources to enumerate
     * the available UpdateSources in this UpdateManager). Make sure you have added yourself as a
     * listener via addUpdateManagerListener() before invoking this request.
     * <p>
     *     <b>NOTE:</b> Retrieving the signature file for the jar is a separate request. Note that
     *     jar signing is optional, so the signature file may or may not exist.
     * </p>
     */
    public void retrieveExtensionJar(UpdateSources.UpdateSource updateSource, VersionManifest.ExtensionVersion extVersion) {
        URL url = resolveUrl(updateSource.getBaseUrl(), extVersion.getDownloadPath());
        downloadManager.downloadFile(url, new DownloadAdapter() {
            @Override
            public void downloadFailed(DownloadThread thread, URL url, String errorMsg) {
                fireDownloadFailed(url, errorMsg);
            }

            @Override
            public void downloadComplete(DownloadThread thread, URL url, File file) {
                fireJarFileDownloaded(url, file);
            }
        });
    }

    /**
     * Requests the given signature file. Make sure you have added yourself as a
     * listener via addUpdateManagerListener() before invoking this request.
     */
    public void retrieveSignatureFile(URL signatureUrl) {
        downloadManager.downloadFile(signatureUrl, new DownloadAdapter() {
            @Override
            public void downloadFailed(DownloadThread thread, URL url, String errorMsg) {
                fireDownloadFailed(url, errorMsg);
            }

            @Override
            public void downloadComplete(DownloadThread thread, URL url, File file) {
                fireSignatureFileDownloaded(url, file);
            }
        });
    }

    /**
     * Requests the given screenshot. Make sure you have added yourself as a listener
     * via addUpdateManagerListener() before invoking this request.
     */
    public void retrieveScreenshot(URL screenshotUrl) {
        downloadManager.downloadFile(screenshotUrl, new ScreenshotDownloadListener());
    }

    /**
     * Register to receive notification before the application is restarted to pick up
     * changes caused by extensions being installed or uninstalled.
     */
    public void registerShutdownHook(ShutdownHook hook) {
        shutdownHooks.add(hook);
    }

    /**
     * You can unregister a previously registered shutdown hook.
     */
    public void unregisterShutdownHook(ShutdownHook hook) {
        shutdownHooks.remove(hook);
    }

    /**
     * This is invoked as needed by ExtensionManager when extensions have been installed
     * or uninstalled, and the application needs to restart to pick up the changes.
     * If you have cleanup that needs to be done before a restart (closing open
     * db connections, saving unsaved changes, etc), you should use registerShutdownHook
     * so that your cleanup code can be executed before a restart!
     */
    public void restartApplication() {
        // Run all registered shutdown hooks:
        for (ShutdownHook hook : new ArrayList<>(shutdownHooks)) {
            hook.applicationWillRestart();
        }

        // Do it:
        System.exit(APPLICATION_RESTART);
    }

    protected void fireVersionManifestDownloaded(URL sourceUrl, VersionManifest manifest) {
        List<UpdateManagerListener> copy = new ArrayList<>(listeners);
        for (UpdateManagerListener listener : copy) {
            listener.versionManifestDownloaded(this, sourceUrl, manifest);
        }
    }

    protected void firePublicKeyDownloaded(URL sourceUrl, PublicKey publicKey) {
        List<UpdateManagerListener> copy = new ArrayList<>(listeners);
        for (UpdateManagerListener listener : copy) {
            listener.publicKeyDownloaded(this, sourceUrl, publicKey);
        }
    }

    protected void fireJarFileDownloaded(URL sourceUrl, File jarFile) {
        List<UpdateManagerListener> copy = new ArrayList<>(listeners);
        for (UpdateManagerListener listener : copy) {
            listener.jarFileDownloaded(this, sourceUrl, jarFile);
        }
    }

    protected void fireSignatureFileDownloaded(URL sourceUrl, File signatureFile) {
        List<UpdateManagerListener> copy = new ArrayList<>(listeners);
        for (UpdateManagerListener listener : copy) {
            listener.signatureFileDownloaded(this, sourceUrl, signatureFile);
        }
    }

    protected void fireScreenshotFileDownloaded(URL sourceUrl, BufferedImage screenshot) {
        List<UpdateManagerListener> copy = new ArrayList<>(listeners);
        for (UpdateManagerListener listener : copy) {
            listener.screenshotDownloaded(this, sourceUrl, screenshot);
        }
    }

    protected void fireDownloadFailed(URL requestedUrl, String errorMessage) {
        List<UpdateManagerListener> copy = new ArrayList<>(listeners);
        for (UpdateManagerListener listener : copy) {
            listener.downloadFailed(this, requestedUrl, errorMessage);
        }
    }

    /**
     * Given a hopefully sane base URL and some string path component, make an honest
     * attempt to put them together into one URL. Examples:
     * <ul>
     *     <li>resolveUrl(http://test.example, "hello"); // returns http://test.example/hello
     *     <li>resolveUrl(http://test.example/a/, "b/c.txt"); // returns http://test/example/a/b/c.txt
     * </ul>
     */
    public static URL resolveUrl(URL base, String path) {
        if (base == null) {
            return null;
        }
        if (path == null || path.isBlank()) {
            return base;
        }
        try {
            // Windows wonkiness! Ensure input path is correct:
            path = path.replaceAll("\\\\", "/");

            String baseStr = base.toString();
            // Ensure base ends with / for proper resolution
            if (!baseStr.endsWith("/")) {
                baseStr += "/";
            }
            return new URI(baseStr).resolve(path).toURL();
        }
        catch (URISyntaxException | MalformedURLException ignored) {
            return null;
        }
    }

    /**
     * Provides the reverse of the resolveUrl method - that is, given a full URL and the base URl that
     * it was created from, will return the path and file that was appended to the base URL. Examples:
     * <ul>
     *     <li>unresolveUrl(http://test.example, http://test.example/hello); // returns "hello"
     *     <li>unresolveUrl(http://test.example/a/, http://test.example/a/b/c.txt); // returns "b/c.txt"
     * </ul>
     * <p>
     * Will return null if the fullUrl was not derived from the given base, or if either input is null.
     * </p>
     */
    public static String unresolveUrl(URL base, URL fullUrl) {
        if (base == null || fullUrl == null) {
            return null;
        }

        String baseStr = base.toString();
        String fullStr = fullUrl.toString();

        // Ensure base ends with / for consistent comparison
        if (!baseStr.endsWith("/")) {
            baseStr += "/";
        }

        if (!fullStr.startsWith(baseStr)) {
            // fullUrl is not derived from baseUrl
            return null;
        }

        return fullStr.substring(baseStr.length());
    }

    /**
     * Listens to our DownloadManager for a VersionManifest file to be downloaded, then
     * parses it and hands it to our own listeners. Basically, we're translating between
     * DownloadListener callbacks and our own UpdateManager callbacks, to make them more
     * friendly to users of this class. It allows us to enhance the results of the callback.
     * For example, instead of just returning the raw version manifest file that was downloaded,
     * we can parse that file and return the actual VersionManifest object to our listeners.
     *
     * @author <a href="https://github.com/scorbo2">scorbo2</a>
     */
    protected class VersionManifestDownloadListener extends DownloadAdapter {

        @Override
        public void downloadFailed(DownloadThread thread, URL url, String errorMsg) {
            fireDownloadFailed(url, "Unable to retrieve version manifest: " + errorMsg);
        }

        @Override
        public void downloadComplete(DownloadThread thread, URL url, File result) {
            if (result == null || !result.exists() || !result.isFile() || result.length() == 0) {
                downloadFailed(thread, url, "Locally downloaded file is empty.");
                return;
            }

            try {
                VersionManifest manifest = VersionManifest.fromFile(result);
                fireVersionManifestDownloaded(url, manifest);
            }
            catch (IOException ioe) {
                downloadFailed(thread, url, "Problem parsing manifest: " + ioe.getMessage());
            }
        }
    }

    /**
     * Listens to our DownloadManager for a PublicKey to be downloaded, then
     * parses it and hands it to our own listeners. Basically, we're translating between
     * DownloadListener callbacks and our own UpdateManager callbacks, to make them more
     * friendly to users of this class. It allows us to enhance the results of the callback.
     * For example, instead of just returning the raw public key file that was downloaded,
     * we can parse that file and return the actual PublicKey object to our listeners.
     *
     * @author <a href="https://github.com/scorbo2">scorbo2</a>
     */
    protected class PublicKeyDownloadListener extends DownloadAdapter {
        @Override
        public void downloadFailed(DownloadThread thread, URL url, String errorMsg) {
            fireDownloadFailed(url, "Unable to retrieve public key: " + errorMsg);
        }

        @Override
        public void downloadComplete(DownloadThread thread, URL url, File result) {
            if (result == null || !result.exists() || !result.isFile() || result.length() == 0) {
                downloadFailed(thread, url, "Locally downloaded file is empty.");
                return;
            }

            try {
                PublicKey publicKey = SignatureUtil.loadPublicKey(result);
                firePublicKeyDownloaded(url, publicKey);
            }
            catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
                downloadFailed(thread, url, "Problem parsing public key: " + e.getMessage());
            }
        }
    }

    /**
     * Listens to our DownloadManager for a screenshot to be downloaded, then
     * parses it and hands it to our own listeners. Basically, we're translating between
     * DownloadListener callbacks and our own UpdateManager callbacks, to make them more
     * friendly to users of this class. It allows us to enhance the results of the callback.
     * For example, instead of just returning the raw screenshot image file that was downloaded,
     * we can parse that file and return the actual BufferedImage object to our listeners.
     *
     * @author <a href="https://github.com/scorbo2">scorbo2</a>
     */
    protected class ScreenshotDownloadListener extends DownloadAdapter {
        @Override
        public void downloadFailed(DownloadThread thread, URL url, String errorMsg) {
            fireDownloadFailed(url, "Unable to retrieve screenshot: " + errorMsg);
        }

        @Override
        public void downloadComplete(DownloadThread thread, URL url, File result) {
            if (result == null || !result.exists() || !result.isFile() || result.length() == 0) {
                downloadFailed(thread, url, "Locally downloaded file is empty.");
                return;
            }

            try {
                fireScreenshotFileDownloaded(url, ImageUtil.loadImage(result));
            }
            catch (IOException e) {
                downloadFailed(thread, url, "Problem loading screenshot: " + e.getMessage());
            }
        }

    }
}
