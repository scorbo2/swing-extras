package ca.corbett.extras.io;

import java.io.File;
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

    void downloadComplete(DownloadThread thread, URL url, File localFile);
}
