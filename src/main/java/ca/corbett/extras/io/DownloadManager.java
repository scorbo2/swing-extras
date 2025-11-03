package ca.corbett.extras.io;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Manages file downloads via http and reports on their progress, completion status, or error status.
 * You could of course just manually instantiate DownloadThread instances and fire them off, but this
 * manager class creates and owns a single, reusable, thread-safe HttpClient that makes launching
 * multiple download requests much cleaner. It also keeps track of downloads in progress so that
 * we can offer methods like isDownloadInProgress() and stopAllDownloads().
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.5
 */
public class DownloadManager {

    private static final Logger log = Logger.getLogger(DownloadManager.class.getName());

    public static final int CONNECTION_TIMEOUT_SECONDS = 10;
    public static final int DOWNLOAD_TIMEOUT_SECONDS = 60;

    private final HttpClient httpClient;
    private final List<DownloadThread> downloadsInProgress = new ArrayList<>();

    public DownloadManager() {
        this.httpClient = HttpClient.newBuilder()
                                    .connectTimeout(Duration.ofSeconds(CONNECTION_TIMEOUT_SECONDS))
                                    .followRedirects(HttpClient.Redirect.NORMAL)
                                    .build();
    }

    /**
     * Downloads a file from the given URL and saves it in the system temp directory.
     */
    public void downloadFile(URL url, DownloadListener listener) {
        downloadFile(url, null, listener);
    }

    /**
     * Downloads a file from the given URL and saves it to the specified path.
     *
     * @param url The URL to download from (supported protocols: http, https, file)
     * @param destinationFile Where to save the file
     * @param listener An optional DownloadListener to receive progress/failure/completion notifications.
     */
    public void downloadFile(URL url, File destinationFile, DownloadListener listener) {
        if (destinationFile == null) {
            try {
                destinationFile = File.createTempFile("download", "." + getFileExtension(url.toString()));
                destinationFile.deleteOnExit();
            }
            catch (IOException ioe) {
                listener.downloadFailed(null, url, "Unable to create temp file: " + ioe.getMessage());
                return;
            }
        }
        new Thread(createDownloadThread(url, destinationFile, listener)).start();
    }

    /**
     * Returns true if at least one download is currently running.
     */
    public boolean isDownloadInProgress() {
        return ! downloadsInProgress.isEmpty();
    }

    /**
     * Sends a kill() request to all active download threads. Does not guarantee that they
     * will stop immediately. Any download currently in progress will report a download failure.
     */
    public void stopAllDownloads() {
        log.info("DownloadManager: stopping all downloads in progress...");

        // Make a copy of the list as its contents may change as we iterate over it:
        List<DownloadThread> inProgress = new ArrayList<>(downloadsInProgress);
        for (DownloadThread thread : inProgress) {
            thread.kill();
        }
    }

    /**
     * Creates and returns a DownloadThread suitable for executing the given download.
     * You can use the downloadFile() wrapper method instead, to both create and
     * automatically start the thread.
     */
    public DownloadThread createDownloadThread(URL url, File destinationFile, DownloadListener listener) {
        DownloadThread thread = new DownloadThread(httpClient, url, destinationFile);
        thread.addDownloadListener(new DownloadTracker());
        thread.addDownloadListener(listener);
        return thread;
    }

    /**
     * Closes the HTTP client.
     */
    public void close() {
        // HttpClient doesn't actually have a close method.
        // But if we had a custom executor, here is where we would shut it down.
    }

    public static String getFileExtension(String filename) {
        if (filename == null || filename.isBlank() || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private class DownloadTracker implements DownloadListener {

        @Override
        public void downloadBegins(DownloadThread thread, URL url) {
            downloadsInProgress.add(thread);
        }

        @Override
        public void downloadProgress(DownloadThread thread, URL url, long bytesDownloaded, long totalBytesIfKnown) {
            // Ignored - we only care about when it starts and when it stops
        }

        @Override
        public void downloadFailed(DownloadThread thread, URL url, String errorMsg) {
            downloadsInProgress.remove(thread);
        }

        @Override
        public void downloadComplete(DownloadThread thread, URL url, File result) {
            downloadsInProgress.remove(thread);
        }
    }
}