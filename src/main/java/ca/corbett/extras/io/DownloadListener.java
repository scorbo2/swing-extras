package ca.corbett.extras.io;

import java.io.File;
import java.net.URL;

/**
 * Can be used with DownloadManager to listen for progress, completion, or error updates
 * from downloads in progress.
 * <P>
 *     <B>IMPORTANT:</B> The callbacks in this listener will be invoked from the worker thread!
 *     If you need to update a Swing UI component as a result of one of these callbacks, you need
 *     to marshal that call to the Swing Event Dispatching Thread, like this:
 * </P>
 * <pre>
 * &#64;Override
 * void downloadComplete(DownloadThread thread, URL url, File result) {
 *     // Expensive operations can be done here on the worker thread
 *     // For example, parse the downloaded file:
 *     String text = FileSystemUtil.readFileToString(result);
 *
 *     // BUT! Now we need to display it in a Swing UI component:
 *     SwingUtilities.invokeLater(() -> { // marshal to EDT
 *        myTextField.setText(text);
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
public interface DownloadListener {

    void downloadBegins(DownloadThread thread, URL url);

    void downloadProgress(DownloadThread thread, URL url, long bytesDownloaded, long totalBytesIfKnown);

    void downloadFailed(DownloadThread thread, URL url, String errorMsg);

    void downloadComplete(DownloadThread thread, URL url, File result);
}
