package ca.corbett.updates;

import java.net.URL;

/**
 * Represents a single remote update source, for use with the UpdateConfiguration class.
 * There are two key pieces of information associated with a remote update source:
 * <ul>
 *     <li><b>versionManifestUrl</b> The URL of a version manifest TODO link to appropriate class
 *     <li><b>publicKeyUrl</b> (optional) - the remote host can publish a public key to use for
 *     digital signature verification of downloaded extension jars. See SignatureUtil class
 *     for more details on signing and verifying files.
 * </ul>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.5
 */
public class UpdateSource {
    private final URL versionManifestUrl;
    private final URL publicKeyUrl;

    /**
     * Create a new UpdateSource with the given versionManifestUrl and no public key.
     * Without a public key, digital signature verification will not be possible for
     * extension jars downloaded from this remote source.
     */
    public UpdateSource(URL versionManifestUrl) {
        this.versionManifestUrl = versionManifestUrl;
        this.publicKeyUrl = null;
    }

    /**
     * Creates an UpdateSource with the specified versionManifestUrl and the specified
     * public key.
     */
    public UpdateSource(URL versionManifestUrl, URL publicKeyUrl) {
        this.versionManifestUrl = versionManifestUrl;
        this.publicKeyUrl = publicKeyUrl;
    }

    public URL getVersionManifestUrl() {
        return versionManifestUrl;
    }

    public URL getPublicKeyUrl() {
        return publicKeyUrl;
    }
}
