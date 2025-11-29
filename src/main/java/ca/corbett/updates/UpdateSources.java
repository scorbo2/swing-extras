package ca.corbett.updates;

import ca.corbett.extras.io.FileSystemUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Your application can package a json file which represents one or more "update sources" - that is,
 * remote URLs where your application can check for new versions, and also to enable discovery and installation
 * of new extensions, or new versions of already-installed extensions. To enable this feature, package an
 * update configuration json file with your application, as shown in the following example:
 * <pre>
 *     {
 *         "applicationName": "MyAmazingApplication",
 *         "updateSources": [
 *             {
 *                 "name": "Web server at www.test.example",
 *                 "baseUrl": "http://www.test.example/MyAmazingApplication/",
 *                 "versionManifest": "version_manifest.json",
 *                 "publicKey": "public.key"
 *             }
 *         ]
 *     }
 * </pre>
 * <p>
 *     The above example shows an application with a single remote update source. The public key is optional,
 *     but recommended to allow for digital signature verification on downloaded extension jars.
 * </p>
 * <p>
 *     <b>Where's all the information detailing my extensions?</b> - that information is NOT bundled with
 *     the application, because the whole idea is that you can release new extensions AFTER your application
 *     has shipped. The UpdateManager class will read the version manifest pointed to by the update sources
 *     json, and will be able to dynamically discover new extensions and new versions of existing extensions.
 *     The version manifest can be updated after the application is released, with no changes needed
 *     on the client side.
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
public class UpdateSources {

    private static final Logger log = Logger.getLogger(UpdateSources.class.getName());
    private static final Gson gson = new GsonBuilder().create();

    private final String applicationName;
    private final List<UpdateSource> updateSources;
    private boolean isAllowSnapshots;

    public UpdateSources(String applicationName) {
        this.applicationName = applicationName;
        this.updateSources = new ArrayList<>();
        this.isAllowSnapshots = false; // Must be explicitly enabled by user
    }

    /**
     * Factory method to generate an UpdateSources instance from the given raw json string,
     * assuming that the json is well-formed and parseable.
     */
    public static UpdateSources fromJson(String json) throws JsonParseException {
        UpdateSources source = gson.fromJson(json, UpdateSources.class);
        source.pruneLocalSources();
        return source;
    }

    /**
     * Factory method to generate an UpdateSources instance from the given json file,
     * assuming that the file is readable and contains well-formed json.
     */
    public static UpdateSources fromFile(File file) throws IOException, JsonParseException {
        return fromJson(FileSystemUtil.readFileToString(file));
    }

    public String getApplicationName() {
        return applicationName;
    }

    public List<UpdateSource> getUpdateSources() {
        return new ArrayList<>(updateSources);
    }

    /**
     * Invoked internally to automatically remove any local filesystem-based update source
     * if the directory that it points to does not exist.
     */
    void pruneLocalSources() {
        // Walk backwards so we can remove as we go:
        for (int i = updateSources.size() - 1; i >= 0; i--) {
            UpdateSource source = updateSources.get(i);
            if (source.isLocalSource()) {
                try {
                    File localDir = new File(source.getBaseUrl().toURI());
                    if (!localDir.exists() || !localDir.isDirectory() || !localDir.canRead()) {
                        log.log(Level.FINE, "Pruning local update source "
                                + source.getName()
                                + " because the target directory does not exist.");
                        updateSources.remove(i);
                    }
                }
                catch (URISyntaxException e) {
                    log.log(Level.WARNING, "Unable to parse local update source "
                            + source.getName()
                            + ": "
                            + e.getMessage(), e);
                    updateSources.remove(i); // can't be loaded, nuke it
                }
            }
        }
    }

    public void addUpdateSource(UpdateSource updateSource) {
        updateSources.add(updateSource);
    }

    public boolean isAllowSnapshots() {
        return isAllowSnapshots;
    }

    public void setAllowSnapshots(boolean allowSnapshots) {
        this.isAllowSnapshots = allowSnapshots;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof UpdateSources that)) { return false; }
        return Objects.equals(applicationName, that.applicationName)
                && Objects.equals(updateSources, that.updateSources)
                && Objects.equals(isAllowSnapshots, that.isAllowSnapshots);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationName, updateSources, isAllowSnapshots);
    }

    /**
     * Represents a single remote update source, for use with the UpdateConfiguration class.
     * There are two key pieces of information associated with a remote update source:
     * <ul>
     *     <li><b>name</b>: Some human-readable name for this data source.
     *     <li><b>baseUrl</b>: The base url for this UpdateSource. All paths are relative to this url.
     *     <li><b>versionManifest</b>: The relative path to the VersionManifest json file.
     *     <li><b>publicKey</b> (optional): - the remote host can publish a public key to use for
     *     digital signature verification of downloaded extension jars. See SignatureUtil class
     *     for more details on signing and verifying files. This field is optional but highly
     *     recommended. Without it, jars will be downloaded and installed with no verification.
     *     This field represents the relative path to the public key.
     * </ul>
     * <p>
     *     This class has convenience methods that allow you to retrieve the versionManifest and the
     *     publicKey either as fully-qualified URLs (built using the value in baseUrl), or as
     *     relative paths (relative to the baseUrl).
     * </p>
     *
     * @author <a href="https://github.com/scorbo2">scorbo2</a>
     * @since swing-extras 2.5
     */
    public static class UpdateSource {
        private static final Gson gson;

        private final String name;
        private final URL baseUrl;
        private final String versionManifest;
        private final String publicKey;

        static {
            gson = new GsonBuilder()
                    .registerTypeAdapter(URL.class, new UrlDeserializer())
                    .create();
        }

        /**
         * Create a new UpdateSource with the given baseUrl, versionManifest and no public key.
         * Without a public key, digital signature verification will not be possible for
         * extension jars downloaded from this remote source.
         */
        public UpdateSource(String name, URL baseUrl, String versionManifest) {
            this(name, baseUrl, versionManifest, null);
        }

        /**
         * Creates an UpdateSource with the specified baseUrl, versionManifest, and public key.
         */
        public UpdateSource(String name, URL baseUrl, String versionManifest, String publicKey) {
            this.name = name;
            this.baseUrl = baseUrl;
            this.versionManifest = versionManifest;
            this.publicKey = publicKey;
        }

        /**
         * Factory method to generate an UpdateSource instance from the given raw json string,
         * assuming that the json is well-formed and parseable.
         */
        public static UpdateSource fromJson(String json) throws JsonParseException {
            return gson.fromJson(json, UpdateSource.class);
        }

        /**
         * Factory method to generate an UpdateSource instance from the given json file,
         * assuming that the file is readable and contains well-formed json.
         */
        public static UpdateSource fromFile(File file) throws IOException, JsonParseException {
            return fromJson(FileSystemUtil.readFileToString(file));
        }

        public String getName() {
            return name;
        }

        public URL getBaseUrl() {
            return baseUrl;
        }

        public boolean isLocalSource() {
            return baseUrl != null && baseUrl.getProtocol().equalsIgnoreCase("file");
        }

        public String getVersionManifestRelativePath() {
            return versionManifest;
        }

        public URL getVersionManifestUrl() {
            return UpdateManager.resolveUrl(baseUrl, versionManifest);
        }

        public boolean hasPublicKey() {
            return publicKey != null && !publicKey.isBlank();
        }

        public String getPublicKeyRelativePath() {
            return publicKey;
        }

        public URL getPublicKeyUrl() {
            return UpdateManager.resolveUrl(baseUrl, publicKey);
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof UpdateSource that)) { return false; }
            return Objects.equals(name, that.name)
                    && Objects.equals(baseUrl, that.baseUrl)
                    && Objects.equals(versionManifest, that.versionManifest)
                    && Objects.equals(publicKey, that.publicKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, baseUrl, versionManifest, publicKey);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * A custom JsonDeserializer that can handle variable substitution on the URLs in string format
     * before parsing them into URL instances. For example:
     * <pre>
     *     {
     *         "someUrl": "file:${user.home}/some/path"
     *     }
     * </pre>
     * <p>
     * When this custom UrlDeserializer is used during the parse, the variable is substituted
     * and you end up with a URL like "file:/home/scorbett/some/path".
     * </p>
     */
    static class UrlDeserializer implements JsonDeserializer<URL> {

        @Override
        public URL deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            String urlString = json.getAsString();
            String substituted = handleVarSubstitution(urlString);

            try {
                return new URL(substituted);
            }
            catch (MalformedURLException e) {
                throw new JsonParseException("Invalid URL: " + substituted, e);
            }
        }

        /**
         * The following substitutions are supported:
         * <ul>
         *     <li><b>${user.home}</b> - will be replaced with the full path of the user's home directory.
         * </ul>
         */
        private String handleVarSubstitution(String input) {
            if (input == null || input.isBlank()) {
                return input;
            }
            return input.replaceAll("\\$\\{user.home}", System.getProperty("user.home"));
        }
    }
}
