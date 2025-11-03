package ca.corbett.updates;

import ca.corbett.extras.crypt.SignatureUtil;
import ca.corbett.extras.io.DownloadAdapter;
import ca.corbett.extras.io.DownloadManager;
import ca.corbett.extras.io.DownloadThread;
import ca.corbett.extras.io.FileSystemUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

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
 *     key are provided, this class will automatically verify the digital signature after each download.
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

    protected final Gson gson;
    protected final File sourceFile;
    protected final UpdateSources updateSources;
    protected final List<UpdateManagerListener> listeners = new ArrayList<>();
    protected final DownloadManager downloadManager;

    public UpdateManager(File sourceFile) throws JsonSyntaxException, IOException {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.sourceFile = sourceFile;
        this.updateSources = gson.fromJson(FileSystemUtil.readFileToString(sourceFile), UpdateSources.class);
        this.downloadManager = new DownloadManager();
    }

    public String getApplicationName() {
        return updateSources.getApplicationName();
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
        downloadManager.downloadFile(updateSource.getVersionManifestUrl(), new PublicKeyDownloadListener());
    }

    /**
     * Returns the UpdateSources that are available in this UpdateManager.
     */
    public List<UpdateSources.UpdateSource> getUpdateSources() {
        return new ArrayList<>(updateSources.getUpdateSources());
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
     * parses it and hands it to our own listeners.
     *
     * @author <a href="https://github.com/scorbo2">scorbo2</a>
     */
    private class VersionManifestDownloadListener extends DownloadAdapter {

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

    private class PublicKeyDownloadListener extends DownloadAdapter {
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
}
