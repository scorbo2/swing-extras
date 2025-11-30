package ca.corbett.extras.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages a single download with progress notifications - you generally shouldn't need to interact
 * with this class directly; instances of this class are created and managed automatically
 * by the DownloadManager.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a> with help from claude.ai
 * @since swing-extras 2.5
 */
public class DownloadThread implements Runnable {

    private static final Logger log = Logger.getLogger(DownloadThread.class.getName());

    private static final int BUFFER_SIZE = 65535;

    private final HttpClient httpClient;
    private final URL url;
    private final File targetDir;
    private final List<DownloadListener> listeners = new ArrayList<>();
    private volatile boolean isKilled;
    private boolean isRunning;

    /**
     * Creates a DownloadThread for retrieving the contents of the given URL.
     * The resulting file will be saved in the system temp dir and provided via callback.
     */
    public DownloadThread(HttpClient httpClient, URL url) {
        this(httpClient, url, null);
    }

    /**
     * Creates a DownloadThread specifically for retrieving the contents of the given URL and
     * saving it to the given target directory. The file name is preserved from whatever is
     * at the end of the given URL. If targetDir is null, the system temp dir is used.
     */
    public DownloadThread(HttpClient httpClient, URL url, File targetDir) {
        this.httpClient = httpClient;
        this.url = url;
        this.targetDir = targetDir == null ? new File(System.getProperty("java.io.tmpdir")) : targetDir;
    }

    public void addDownloadListener(DownloadListener listener) {
        listeners.add(listener);
    }

    public void removeDownloadListener(DownloadListener listener) {
        listeners.remove(listener);
    }

    /**
     * Reports if the download thread is actively downloading (or waiting on network).
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Reports if the kill() method has been invoked on this thread.
     */
    public boolean isKilled() {
        return isKilled;
    }

    /**
     * Tells the thread to give up on the current download and terminate itself.
     * The download will be treated as a failure. Does nothing if the thread
     * had already completed before this method was invoked, or if the thread
     * had not yet been started before this method was invoked.
     */
    public void kill() {
        isKilled = true;
    }

    @Override
    public void run() {
        if (httpClient == null || url == null) {
            fireDownloadFailed("Internal error: DownloadThread given null input, cannot proceed.");
            isRunning = false;
            isKilled = false;
            return;
        }
        if (!"file".equalsIgnoreCase(url.getProtocol())
                && !"http".equalsIgnoreCase(url.getProtocol())
                && !"https".equalsIgnoreCase(url.getProtocol())) {
            fireDownloadFailed("Unsupported file download protocol: " + url.getProtocol());
            isRunning = false;
            isKilled = false;
            return;
        }

        isRunning = true;
        isKilled = false;
        String filename = DownloadManager.getFilenameComponent(url.toString());
        if (filename.isBlank()) {
            filename = "unnamed";
        }
        File targetFile = new File(targetDir, filename);
        fireDownloadBegins();

        try {
            // If we were given a file url, just do a local file copy and we're done:
            if ("file".equalsIgnoreCase(url.getProtocol())) {
                File sourceFile = new File(url.toURI());
                log.fine("DownloadThread: copying local file "
                                 + sourceFile.getAbsolutePath()
                                 + " to "
                                 + targetDir.getAbsolutePath());
                Files.copy(Paths.get(url.toURI()), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                isRunning = false;
                fireDownloadComplete(targetFile);
                return;
            }

            // Otherwise, we'll download it:
            HttpRequest request = HttpRequest.newBuilder()
                                             .uri(url.toURI())
                                             .timeout(Duration.ofSeconds(DownloadManager.DOWNLOAD_TIMEOUT_SECONDS))
                                             .GET()
                                             .build();
            HttpResponse<InputStream> response = httpClient.send(request,
                                                                 HttpResponse.BodyHandlers.ofInputStream());

            int statusCode = response.statusCode();
            long lastUpdateTime = System.currentTimeMillis();
            if (statusCode == 200) {
                long contentLength = response.headers()
                                             .firstValueAsLong("Content-Length")
                                             .orElse(-1L);

                try (InputStream in = response.body();
                     FileOutputStream out = new FileOutputStream(targetFile)) {

                    byte[] buffer = new byte[BUFFER_SIZE];
                    long downloaded = 0;
                    int bytesRead;

                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                        downloaded += bytesRead;

                        // Avoid spamming progress messages, send at most four per second:
                        long currentTime = System.currentTimeMillis();
                        if ((currentTime - lastUpdateTime) > 250) {
                            fireDownloadProgress(downloaded, contentLength);
                            lastUpdateTime = currentTime;
                        }

                        // Check for kill requests:
                        if (isKilled) {
                            fireDownloadFailed("Download was killed by requestor.");
                            break;
                        }
                    }

                    isRunning = false;
                    fireDownloadComplete(targetFile);
                }
                finally {
                    isRunning = false;
                }
            }
            else if (statusCode == 404) {
                fireDownloadFailed("404 File not found: " + url);
            }
            else if (statusCode >= 400 && statusCode < 500) {
                fireDownloadFailed(statusCode + " Client error: " + url);
            }
            else if (statusCode >= 500) {
                fireDownloadFailed(statusCode + " Server error: " + url);
            }
            else {
                fireDownloadFailed("Unexpected status code " + statusCode + ": " + url);
            }
        } catch (URISyntaxException e) {
            fireDownloadFailed("Invalid URL format: " + url + " - " + e.getMessage());

        } catch (HttpTimeoutException e) {
            fireDownloadFailed("Request timed out: " + url + " - " + e.getMessage());

        } catch (ConnectException e) {
            fireDownloadFailed("Connection failed: " + url + " - " + e.getMessage());

        } catch (IOException e) {
            // This catches various network issues and file I/O problems
            fireDownloadFailed("I/O error downloading " + url + ": " + e.getMessage(), e);

        } catch (InterruptedException e) {
            // Restore interrupt status
            Thread.currentThread().interrupt();
            fireDownloadFailed("Download interrupted: " + url);

        } catch (SecurityException e) {
            fireDownloadFailed("Security error (file permissions?): " + targetFile.getAbsolutePath() +
                                       " - " + e.getMessage(), e);

        } catch (Exception e) {
            // Catch any other unexpected exceptions
            fireDownloadFailed("Unexpected error downloading " + url + ": " +
                                       e.getClass().getSimpleName() + " - " + e.getMessage(), e);
        }
    }

    private void fireDownloadBegins() {
        log.fine("DownloadThread: beginning download: " + url.toString());
        for (DownloadListener listener : new ArrayList<>(listeners)) {
            listener.downloadBegins(this, url);
        }
    }

    private void fireDownloadProgress(long bytesDownloaded, long totalBytesIfKnown) {
        log.fine("DownloadThread: read "
                         + bytesDownloaded
                         + " bytes of "
                         + ((totalBytesIfKnown == -1) ? "(unknown)" : totalBytesIfKnown));
        for (DownloadListener listener : new ArrayList<>(listeners)) {
            listener.downloadProgress(this, url, bytesDownloaded, totalBytesIfKnown);
        }
    }

    private void fireDownloadFailed(String errorMsg) {
        fireDownloadFailed(errorMsg, null);
    }

    private void fireDownloadFailed(String errorMsg, Exception e) {
        if (e != null) {
            log.log(Level.SEVERE,
                    "DownloadThread: download failed with exception: " + e.getMessage() + " and message: " + errorMsg,
                    e);
        }
        else {
            log.log(Level.SEVERE, "DownloadThread: download failed with message: " + errorMsg);
        }
        for (DownloadListener listener : new ArrayList<>(listeners)) {
            listener.downloadFailed(this, url, errorMsg);
        }
    }

    private void fireDownloadComplete(File targetFile) {
        log.fine("DownloadThread: downloaded "
                         + url.toString()
                         + " to local file "
                         + targetFile.getAbsolutePath());
        for (DownloadListener listener : new ArrayList<>(listeners)) {
            listener.downloadComplete(this, url, targetFile);
        }
    }
}
