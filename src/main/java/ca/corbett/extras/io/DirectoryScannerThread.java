package ca.corbett.extras.io;

import ca.corbett.extras.progress.SimpleProgressWorker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * A handy utility class that can wrap the findSubdirectories methods in FileSystemUtil
 * in the form of a worker thread that can be easily wired up to a MultiProgressDialog.
 * This saves a modest amount of work for client applications that just want to find
 * subdirectories in a thread-safe way (that is, without blocking the UI thread).
 * <p>
 * This class uses the SimpleProgressListener interface, which provides discrete events
 * for begins, progress, completed, and canceled. Callers can cancel the search by
 * returning false from the progress callback.
 * </p>
 * <p>
 * Instead of (or in addition to) using the progress listener callbacks, callers can also register
 * CompletionListener and CancelListener callbacks, which will be invoked when the search completes
 * or is canceled, respectively. This allows callers to be notified of these events without having to
 * implement the full SimpleProgressListener interface. Note that these callbacks are invoked
 * from the worker thread! If you need to do UI updates as a result of these callbacks, you must
 * marshal those calls to the Swing Event Dispatching Thread using SwingUtilities.invokeLater() or similar.
 * </p>
 * <p>
 * <b>EXAMPLE USAGE</b>
 * </p>
 * <pre>
 * // Find all subdirectories under a given path (recursion is true by default):
 * DirectoryScannerThread scanner = new DirectoryScannerThread(new File("/path/to/search"))
 *     .addCompletionListener(results -> handleResults(results))
 *     .addCancelListener(() -> handleCancellation());
 * MultiProgressDialog progressDialog = new MultiProgressDialog(parentFrame, "Scanning for directories...");
 * progressDialog.runWorker(scanner, true); // run the scan and dispose when finished.
 * </pre>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 3.0
 */
public class DirectoryScannerThread extends SimpleProgressWorker {

    private static final Logger log = Logger.getLogger(DirectoryScannerThread.class.getName());

    @FunctionalInterface
    public interface CompletionListener {
        void searchComplete(List<File> results);
    }

    @FunctionalInterface
    public interface CancelListener {
        void searchCanceled();
    }

    private final File rootDir;
    private boolean recursive;
    private final List<File> results;
    private final List<CompletionListener> completionListeners = new CopyOnWriteArrayList<>();
    private final List<CancelListener> cancelListeners = new CopyOnWriteArrayList<>();

    public DirectoryScannerThread(File rootDir) {
        this.rootDir = rootDir;
        recursive = true;
        results = new ArrayList<>();
    }

    public boolean isRecursive() {
        return recursive;
    }

    /**
     * Decides whether the search should include subdirectories recursively, or just the
     * immediate children of rootDir. By default, all searches are recursive.
     */
    public DirectoryScannerThread setRecursive(boolean recursive) {
        this.recursive = recursive;
        return this;
    }

    public List<File> getResults() {
        return new ArrayList<>(results);
    }

    /**
     * Register to receive notification when the search completes. This will not be invoked
     * if the search is canceled. This hook might be easier to use than implementing
     * an entire SimpleProgressListener in your code.
     * <p>
     * <b>IMPORTANT NOTE:</b> This callback is invoked from the worker thread!
     * If you need to do UI updates in response to this, you must marshal them
     * to the Swing Event Dispatching Thread using SwingUtilities.invokeLater() or similar.
     * </p>
     */
    public DirectoryScannerThread addCompletionListener(CompletionListener listener) {
        completionListeners.add(listener);
        return this;
    }

    /**
     * Register to receive notification if the search is canceled by any listener.
     * This will not be invoked if the search completes normally. This hook might be easier to use than implementing
     * an entire SimpleProgressListener in your code.
     * <p>
     * <b>IMPORTANT NOTE:</b> This callback is invoked from the worker thread!
     * If you need to do UI updates in response to this, you must marshal them
     * to the Swing Event Dispatching Thread using SwingUtilities.invokeLater() or similar.
     * </p>
     */
    public DirectoryScannerThread addCancelListener(CancelListener listener) {
        cancelListeners.add(listener);
        return this;
    }

    public DirectoryScannerThread removeCompletionListener(CompletionListener listener) {
        completionListeners.remove(listener);
        return this;
    }

    public DirectoryScannerThread removeCancelListener(CancelListener listener) {
        cancelListeners.remove(listener);
        return this;
    }

    @Override
    public void run() {
        // We don't know how many directories there are to scan, and finding out
        // is a costly operation. So, we fire a progressBegins event just
        // to get the progress dialog to show up. We'll correct it with
        // a second progressBegins event once we have an idea of what we're up against.
        fireProgressBegins(1);

        // Special-case stupid input:
        if (rootDir == null || !rootDir.isDirectory() || !rootDir.canRead()) {
            log.warning("DirectoryScannerThread was given a null or unreadable rootDir. Aborting.");
            fireProgressError("rootDir", "rootDir is not a readable directory. Aborting.");
            fireProgressComplete();
            return;
        }

        results.clear();
        boolean wasCanceled = false;
        try {
            // Find all subdirectories:
            List<File> dirs = findSubdirectoriesInternal(rootDir, recursive);
            FileSystemUtil.sortFiles(dirs);
            fireProgressBegins(dirs.size());

            // Now just enumerate and return them:
            int currentStep = 0;
            for (File dir : dirs) {
                results.add(dir);

                if (!fireProgressUpdate(currentStep++, dir.getName())) {
                    wasCanceled = true;
                    break;
                }
            }
        }

        finally {
            // We have to fire one of our terminal events, otherwise
            // the progress dialog will never close:
            if (wasCanceled) {
                fireCancellationInternal();
            }
            else {
                fireCompletionInternal();
            }
        }
    }

    /**
     * Basically duplicating the logic in FileSystemUtil, but without the FileSearchListener nonsense.
     */
    private List<File> findSubdirectoriesInternal(final File rootDir,
                                                  final boolean recursive) {
        File[] children = rootDir.listFiles();
        List<File> fileList = new ArrayList<>();
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory()) {
                    if (recursive) {
                        fileList.addAll(findSubdirectoriesInternal(child, true));
                    }
                    fileList.add(child);
                }
            }
        }

        return fileList;
    }


    private void fireCompletionInternal() {
        fireProgressComplete();
        for (CompletionListener listener : completionListeners) {
            listener.searchComplete(getResults());
        }
    }

    private void fireCancellationInternal() {
        fireProgressCanceled();
        for (CancelListener listener : cancelListeners) {
            listener.searchCanceled();
        }
    }
}
