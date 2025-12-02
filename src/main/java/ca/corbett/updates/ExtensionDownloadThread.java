package ca.corbett.updates;

import ca.corbett.extras.io.DownloadAdapter;
import ca.corbett.extras.io.DownloadManager;
import ca.corbett.extras.io.DownloadThread;
import ca.corbett.extras.progress.SimpleProgressWorker;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * A SimpleProgressWorker implementation that can download the given Extension and
 * return it as a DownloadedExtension instance if the download succeeds. You can configure
 * which extension file(s) you wish to download using the setDownloadOptions() method,
 * but it must be invoked before the thread is started.
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

    private final static Logger log = Logger.getLogger(ExtensionDownloadThread.class.getName());

    private final Object downloadLock = new Object();

    public enum Options {
        JarOnly,
        JarAndSignature,
        ScreenshotsOnly,
        Everything;

        public boolean isJarFileDownload() {
            return this == JarOnly || this == JarAndSignature || this == Everything;
        }

        public boolean isSignatureDownload() {
            return this == JarAndSignature || this == Everything;
        }

        public boolean isScreenshotsDownload() {
            return this == ScreenshotsOnly || this == Everything;
        }
    }

    private final DownloadManager downloadManager;
    private final UpdateSources.UpdateSource updateSource;
    private final VersionManifest.ExtensionVersion extensionVersion;
    private int downloadTotal;
    private final AtomicInteger downloadRemaining = new AtomicInteger();
    private final List<String> errors = new ArrayList<>();
    private DownloadedExtension downloadedExtension;
    private Options downloadOptions;
    private long timeoutMs;

    public ExtensionDownloadThread(DownloadManager downloadManager,
                                   UpdateSources.UpdateSource updateSource,
                                   VersionManifest.ExtensionVersion extensionVersion) {
        this.updateSource = updateSource;
        this.extensionVersion = extensionVersion;
        this.downloadManager = downloadManager;

        // By default, we will download all associated extension files:
        downloadOptions = Options.Everything;

        // By default, we'll allow 10s for the download(s) to complete before we give up:
        timeoutMs = 10000;
    }

    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }

    public DownloadedExtension getDownloadedExtension() {
        return downloadedExtension;
    }

    /**
     * Before starting the worker thread, you can decide which file(s) should be downloaded
     * for the extension in question. By default, all options are set to true.
     */
    public void setDownloadOptions(Options options) {
        this.downloadOptions = options;
    }

    /**
     * Before starting the download, you can set a download timeout - if the total time to download
     * all requested files exceeds this value, the download will abort. Default is 10 seconds.
     */
    public void setDownloadTimeoutMs(long timeout) {
        timeoutMs = Math.min(0, timeout);
    }

    @Override
    public void run() {
        errors.clear();
        downloadedExtension = new DownloadedExtension();

        // Figure out what exactly we are downloading:
        URL jarUrl = null;
        URL sigUrl = null;
        List<URL> screenshotURLs = new ArrayList<>();
        downloadTotal = 0;
        if (downloadOptions.isJarFileDownload()) {
            jarUrl = UpdateManager.resolveUrl(updateSource.getBaseUrl(), extensionVersion.getDownloadPath());
            downloadTotal++;
        }
        if (downloadOptions.isSignatureDownload() && extensionVersion.getSignaturePath() != null) {
            sigUrl = UpdateManager.resolveUrl(updateSource.getBaseUrl(), extensionVersion.getSignaturePath());
            downloadTotal++;
        }
        if (downloadOptions.isScreenshotsDownload()) {
            for (String screenshotPath : extensionVersion.getScreenshots()) {
                screenshotURLs.add(UpdateManager.resolveUrl(updateSource.getBaseUrl(), screenshotPath));
                downloadTotal++;
            }
        }

        // If we have nothing to download, we're done here:
        if (downloadTotal == 0) {
            log.warning("ExtensionDownloadThread: nothing to download. Skipping.");
            fireProgressComplete();
            return;
        }

        log.info("Extension download thread will download " + downloadTotal + " associated extension files.");

        // Fire off all download requests:
        long startTime = System.currentTimeMillis();
        downloadRemaining.set(downloadTotal);
        fireProgressBegins(downloadTotal);
        if (jarUrl != null) {
            downloadManager.downloadFile(jarUrl, new JarFileListener());
        }
        if (sigUrl != null) {
            downloadManager.downloadFile(sigUrl, new SigFileListener());
        }
        for (URL url : screenshotURLs) {
            downloadManager.downloadFile(url, new ScreenshotListener());
        }

        // Wait for completion:
        while (downloadRemaining.get() > 0) {
            try {
                Thread.sleep(100); // cheesy

                // Check for stuck or very long-running downloads:
                if ((System.currentTimeMillis() - startTime) > timeoutMs) {
                    log.warning("ExtensionDownloadThread: download timeout exceeded; aborting.");
                    downloadManager.stopAllDownloads();
                    break;
                }
            }
            catch (InterruptedException ignored) {
                break;
            }
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
            synchronized(downloadLock) {
                downloadedExtension.addScreenshot(result);
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
}
