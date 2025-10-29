package ca.corbett.updates;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private final String applicationName;
    private final List<UpdateSource> updateSources;

    public UpdateSources(String applicationName) {
        this.applicationName = applicationName;
        this.updateSources = new ArrayList<>();
    }

    public String getApplicationName() {
        return applicationName;
    }

    public List<UpdateSource> getUpdateSources() {
        return new ArrayList<>(updateSources);
    }

    public void addUpdateSource(UpdateSource updateSource) {
        updateSources.add(updateSource);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof UpdateSources that)) { return false; }
        return Objects.equals(applicationName, that.applicationName)
                && Objects.equals(updateSources, that.updateSources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationName, updateSources);
    }

    /**
     * Represents a single remote update source, for use with the UpdateConfiguration class.
     * There are two key pieces of information associated with a remote update source:
     * <ul>
     *     <li><b>name</b>: Some human-readable name for this data source.
     *     <li><b>baseUrl</b>: The base url for this UpdateSource. All paths are relative to this url.
     *     <li><b>versionManifest</b>: The path to the VersionManifest json file.
     *     <li><b>publicKey</b> (optional): - the remote host can publish a public key to use for
     *     digital signature verification of downloaded extension jars. See SignatureUtil class
     *     for more details on signing and verifying files. This field is optional but highly
     *     recommended. Without it, jars will be downloaded and installed with no verification.
     * </ul>
     *
     * @author <a href="https://github.com/scorbo2">scorbo2</a>
     * @since swing-extras 2.5
     */
    public static class UpdateSource {
        private final String name;
        private final URL baseUrl;
        private final String versionManifest;
        private final String publicKey;

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

        public String getName() {
            return name;
        }

        public URL getBaseUrl() {
            return baseUrl;
        }

        public URL getVersionManifestUrl() {
            return resolveUrl(baseUrl, versionManifest);
        }

        public URL getPublicKeyUrl() {
            return resolveUrl(baseUrl, publicKey);
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

        /**
         * Given a hopefully sane base URL and some string path component, make an honest
         * attempt to put them together into one URL. Examples:
         * <ul>
         *     <li>resolveUrl(http://test.example, "hello"); // returns http://test.example/hello
         *     <li>resolveUrl(http://test.example/a/, "b/c.txt); // returns http://test/example/a/b/c.txt
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
    }
}
