package ca.corbett.updates;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.security.PublicKey;

/**
 * Callers can subscribe to receive notification events from an UpdateManager.
 * <P>
 *     <B>IMPORTANT:</B> The callbacks in this listener will be invoked from the worker thread!
 *     If you need to update a Swing UI component as a result of one of these callbacks, you need
 *     to marshal that call to the Swing Event Dispatching Thread, like this:
 * </P>
 * <pre>
 * &#64;Override
 * void screenshotDownloaded(UpdateManager manager, URL sourceUrl, BufferedImage screenshot) {
 *     // Expensive operations can be done here on the worker thread
 *     // For example, scale/resize the image or whatever
 *     screenshot = ImageUtil.scaleImageToFitSquareBounds(screenshot, 500);
 *
 *     // BUT! Now we need to display it in a Swing UI component:
 *     SwingUtilities.invokeLater(() -> { // marshal to EDT
 *        myImageListField.addImage(screenshot);
 *     });
 * }
 * </pre>
 * <p>
 *     Failure to do this may result in deadlocks or other threading issues, as Swing
 *     itself is not thread-safe.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.5
 */
public interface UpdateManagerListener {

    void versionManifestDownloaded(UpdateManager manager, URL sourceUrl, VersionManifest versionManifest);

    void publicKeyDownloaded(UpdateManager manager, URL sourceUrl, PublicKey publicKey);

    void screenshotDownloaded(UpdateManager manager, URL sourceUrl, BufferedImage screenshot);

    void jarFileDownloaded(UpdateManager manager, URL sourceUrl, File jarFile);

    void signatureFileDownloaded(UpdateManager manager, URL sourceUrl, File signatureFile);

    void downloadFailed(UpdateManager manager, URL requestedUrl, String errorMessage);
}
