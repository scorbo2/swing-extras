package ca.corbett.updates;

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
 *                 "versionManifestUrl": "http://www.test.example/versions.json",
 *                 "publicKeyUrl": "http://www.test.example/public.key"
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
 *     <a href="https://github.com/scorbo2/ext-package">ExtPackager</a> that can walk you through the process
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
     *     <li><b>versionManifestUrl</b>: The URL of a VersionManifest json file.
     *     <li><b>publicKeyUrl</b> (optional): - the remote host can publish a public key to use for
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
        private final URL versionManifestUrl;
        private final URL publicKeyUrl;

        /**
         * Create a new UpdateSource with the given versionManifestUrl and no public key.
         * Without a public key, digital signature verification will not be possible for
         * extension jars downloaded from this remote source.
         */
        public UpdateSource(String name, URL versionManifestUrl) {
            this.name = name;
            this.versionManifestUrl = versionManifestUrl;
            this.publicKeyUrl = null;
        }

        /**
         * Creates an UpdateSource with the specified versionManifestUrl and the specified
         * public key.
         */
        public UpdateSource(String name, URL versionManifestUrl, URL publicKeyUrl) {
            this.name = name;
            this.versionManifestUrl = versionManifestUrl;
            this.publicKeyUrl = publicKeyUrl;
        }

        public String getName() {
            return name;
        }

        public URL getVersionManifestUrl() {
            return versionManifestUrl;
        }

        public URL getPublicKeyUrl() {
            return publicKeyUrl;
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof UpdateSource that)) { return false; }
            return Objects.equals(name, that.name)
                    && Objects.equals(versionManifestUrl, that.versionManifestUrl)
                    && Objects.equals(publicKeyUrl, that.publicKeyUrl);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, versionManifestUrl, publicKeyUrl);
        }
    }
}
