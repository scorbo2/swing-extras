package ca.corbett.updates;

import ca.corbett.extras.image.ImageUtil;
import ca.corbett.extras.io.DownloadAdapter;
import ca.corbett.extras.io.DownloadManager;
import ca.corbett.extras.io.DownloadThread;
import ca.corbett.extras.progress.SimpleProgressWorker;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A SimpleProgressWorker implementation that can download the given Extension and
 * return it as a DownloadedExtension instance if the download succeeds.
 * <p>
 * <b>NOTE:</b> This worker thread will not fire a progressError event!
 * It will invoke progressComplete when all associated files have either
 * succeeded or failed. Callers can check getErrors() to see if anything failed.
 * </p>
 * <P>
 * <B>IMPORTANT:</B> The callbacks in this listener will be invoked from the worker thread!
 * If you need to update a Swing UI component as a result of one of these callbacks, you need
 * to marshal that call to the Swing Event Dispatching Thread, like this:
 * </P>
 * <pre>
 * &#64;Override
 * boolean progressComplete() {
 *     // Some operations are fine to do here on the worker thread.
 *     // For example, verifying the signature on the downloaded jar:
 *     DownloadedExtension ext = workerThread.getDownloadedExtension();
 *     boolean isValid = false;
 *     if (ext.getSignatureFile() != null) {
 *         isValid = SignatureUtil.verifyFile(ext.getJarFile(),
 *                                            ext.getSigFile(),
 *                                            ext.getPublicKey());
 *     }
 *
 *     // BUT! Now we need to display it in a Swing UI component:
 *     SwingUtilities.invokeLater(() -> { // marshal to EDT
 *        myStatusLabel.setText("Signature is valid: " + isValid);
 *     });
 * }
 * </pre>
 * <p>
 * Failure to do this may result in deadlocks or other threading issues, as Swing
 * itself is not thread-safe.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.5
 */
public class ExtensionDownloadThread extends SimpleProgressWorker {

    private final DownloadManager downloadManager;
    private final UpdateSources.UpdateSource updateSource;
    private final VersionManifest.ExtensionVersion extensionVersion;
    private int downloadTotal;
    private final AtomicInteger downloadRemaining = new AtomicInteger();
    private final List<String> errors = new ArrayList<>();
    private DownloadedExtension downloadedExtension;

    public ExtensionDownloadThread(DownloadManager downloadManager,
                                   UpdateSources.UpdateSource updateSource,
                                   VersionManifest.ExtensionVersion extensionVersion) {
        this.updateSource = updateSource;
        this.extensionVersion = extensionVersion;
        this.downloadManager = downloadManager;
    }

    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }

    public DownloadedExtension getDownloadedExtension() {
        return downloadedExtension;
    }

    @Override
    public void run() {
        errors.clear();
        downloadedExtension = new DownloadedExtension();

        // Resolve all of our URLs to download (should be at least one - the jar URL):
        URL jarUrl = UpdateManager.resolveUrl(updateSource.getBaseUrl(), extensionVersion.getDownloadPath());
        URL sigUrl = null;
        if (extensionVersion.getSignaturePath() != null) {
            sigUrl = UpdateManager.resolveUrl(updateSource.getBaseUrl(), extensionVersion.getSignaturePath());
        }
        List<URL> screenshotURLs = new ArrayList<>();
        for (String screenshotPath : extensionVersion.getScreenshots()) {
            screenshotURLs.add(UpdateManager.resolveUrl(updateSource.getBaseUrl(), screenshotPath));
        }
        downloadTotal = 1 + screenshotURLs.size();
        if (sigUrl != null) {
            downloadTotal++;
        }
        downloadRemaining.set(downloadTotal);

        // Fire off all download requests:
        fireProgressBegins(downloadTotal);
        downloadManager.downloadFile(jarUrl, new JarFileListener());
        if (sigUrl != null) {
            downloadManager.downloadFile(sigUrl, new SigFileListener());
        }
        for (URL url : screenshotURLs) {
            downloadManager.downloadFile(url, new ScreenshotListener());
        }
    }

    private class JarFileListener extends DownloadAdapter {
        @Override
        public void downloadFailed(DownloadThread thread, URL url, String errorMsg) {
            errors.add(errorMsg);
            if (downloadRemaining.decrementAndGet() == 0) {
                fireProgressComplete();
            }
        }

        @Override
        public void downloadComplete(DownloadThread thread, URL url, File result) {
            downloadedExtension.setJarFile(result);
            int remaining = downloadRemaining.decrementAndGet();
            if (remaining == 0) {
                fireProgressComplete();
            }
            else {
                fireProgressUpdate(downloadTotal - remaining, "Downloaded extension jar...");
            }
        }
    }

    private class SigFileListener extends DownloadAdapter {
        @Override
        public void downloadFailed(DownloadThread thread, URL url, String errorMsg) {
            errors.add(errorMsg);
            if (downloadRemaining.decrementAndGet() == 0) {
                fireProgressComplete();
            }
        }

        @Override
        public void downloadComplete(DownloadThread thread, URL url, File result) {
            downloadedExtension.setSignatureFile(result);
            int remaining = downloadRemaining.decrementAndGet();
            if (remaining == 0) {
                fireProgressComplete();
            }
            else {
                fireProgressUpdate(downloadTotal - remaining, "Downloaded signature file...");
            }
        }
    }

    private class ScreenshotListener extends DownloadAdapter {
        @Override
        public void downloadFailed(DownloadThread thread, URL url, String errorMsg) {
            errors.add(errorMsg);
            if (downloadRemaining.decrementAndGet() == 0) {
                fireProgressComplete();
            }
        }

        @Override
        public void downloadComplete(DownloadThread thread, URL url, File result) {
            try {
                downloadedExtension.addScreenshot(ImageUtil.loadImage(result));
            }
            catch (IOException ioe) {
                errors.add("Failed to parse downloaded screenshot: " + ioe.getMessage());
            }
            int remaining = downloadRemaining.decrementAndGet();
            if (remaining == 0) {
                fireProgressComplete();
            }
            else {
                fireProgressUpdate(downloadTotal - remaining, "Downloaded screenshot...");
            }
        }
    }
}
