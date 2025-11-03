package ca.corbett.updates;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.security.PublicKey;

/**
 * Callers can subscribe to receive notification events from an UpdateManager.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.5
 */
public interface UpdateManagerListener {

    void versionManifestDownloaded(UpdateManager manager, URL sourceUrl, VersionManifest versionManifest);

    void publicKeyDownloaded(UpdateManager manager, URL sourceUrl, PublicKey publicKey);

    void screenshotDownloaded(UpdateManager manager, URL sourceUrl, BufferedImage screenshot);

    void extensionDownloaded(UpdateManager manager, File jarFile, File signatureFile);

    void downloadFailed(UpdateManager manager, URL requestedUrl, String errorMessage);
}
