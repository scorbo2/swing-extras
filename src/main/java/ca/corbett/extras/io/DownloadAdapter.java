package ca.corbett.extras.io;

import java.io.File;
import java.net.URL;

/**
 * An empty implementation of DownloadListener that you can extend if you only care
 * about implementing one or two methods instead of all of them.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.5
 */
public class DownloadAdapter implements DownloadListener {

    @Override
    public void downloadBegins(DownloadThread thread, URL url) {
    }

    @Override
    public void downloadProgress(DownloadThread thread, URL url, long bytesDownloaded, long totalBytesIfKnown) {
    }

    @Override
    public void downloadFailed(DownloadThread thread, URL url, String errorMsg) {
    }

    @Override
    public void downloadComplete(DownloadThread thread, URL url, File result) {
    }
}
