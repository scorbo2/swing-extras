package ca.corbett.extras.io;

import java.net.URL;

/**
 * Can be used with DownloadManager to listen for progress, completion, or error updates
 * from downloads in progress.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.5
 */
public interface DownloadListener {

    void downloadBegins(DownloadThread thread, URL url);

    void downloadProgress(DownloadThread thread, URL url, long bytesDownloaded, long totalBytesIfKnown);

    void downloadFailed(DownloadThread thread, URL url, String errorMsg);

    /**
     * Fired when the download successfully completes. The "result" parameter will either be
     * a java.util.File instance if you requested a file download, or a String if you requested
     * the remote contents as a String.
     */
    void downloadComplete(DownloadThread thread, URL url, Object result);
}
