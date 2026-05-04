package ca.corbett.extras.io;

import ca.corbett.extras.progress.SimpleProgressWorker;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * A handy utility class that can wrap the various find methods in FileSystemUtil
 * in the form of a worker thread that can be easily wired up to a MultiProgressDialog.
 * This saves a modest amount of work for client applications that just want to find
 * files in a thread-safe way (that is, without blocking the UI thread).
 * <p>
 * This class also improved progress reporting, compared to the FileSearchListener
 * approach. This class goes through the SimpleProgressListener interface, which provides
 * discrete events for begins, progress, completed, and canceled. Callers still have the
 * option of cancelling the search by returning false from the progress callback,
 * but now they have better visibility into the current step and total step counts.
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
 * // Find all text and Markdown files in a directory (recursion is true by default):
 * FileScannerThread scanner = new FileScannerThread(new File("/path/to/search"))
 *     .setExtensionsToMatch(List.of(".txt", ".md"))
 *     .addCompletionListener(results -> handleResults(results))
 *     .addCancelListener(() -> handleCancellation());
 * MultiProgressDialog progressDialog = new MultiProgressDialog(parentFrame, "Scanning for files...");
 * progressDialog.runWorker(scanner, true); // run the scan and dispose when finished.
 * </pre>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 3.0
 */
public class FileScannerThread extends SimpleProgressWorker {

    private static final Logger log = Logger.getLogger(FileScannerThread.class.getName());

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
    private boolean invertSearch;
    private final Set<String> extensionsToMatch;
    private final List<File> results;
    private final List<CompletionListener> completionListeners = new CopyOnWriteArrayList<>();
    private final List<CancelListener> cancelListeners = new CopyOnWriteArrayList<>();

    public FileScannerThread(File rootDir) {
        this.rootDir = rootDir;
        recursive = true;
        invertSearch = false;
        extensionsToMatch = new HashSet<>();
        results = new ArrayList<>();
    }

    public boolean isRecursive() {
        return recursive;
    }

    /**
     * Decides whether the search should include subdirectories, or just rootDir itself.
     * By default, all searches are recursive.
     */
    public FileScannerThread setRecursive(boolean recursive) {
        this.recursive = recursive;
        return this;
    }

    public boolean isSearchInverted() {
        return invertSearch;
    }

    /**
     * Changes how the extensionsToMatch list is interpreted. By default, searches are non-inverted,
     * and files must match at least one of the given extensions in order to be returned.
     * An inverted search will only return files that do NOT match any of the given extensions.
     */
    public FileScannerThread setInvertSearch(boolean invertSearch) {
        this.invertSearch = invertSearch;
        return this;
    }

    public List<String> getExtensionsToMatch() {
        return List.copyOf(extensionsToMatch);
    }

    /**
     * Adds to the list of extensions that should be matched during the search,
     * while also considering whether our search is inverted (see setInvertSearch()).
     * <p>
     * Extensions can include the dot or not: ".txt" and "txt" are treated the same.
     * Extensions are normalized to lowercase and trimmed for matching purposes.
     * </p>
     * <p>
     * An empty or null list is acceptable. For regular (non-inverted) searches,
     * this means "match everything". For inverted searches, this means "exclude nothing".
     * This means you'll get a list of ALL files in either case.
     * </p>
     */
    public FileScannerThread addExtensionsToMatch(List<String> extensions) {
        extensionsToMatch.addAll(FileSystemUtil.normalizeExtensionsToSet(extensions));
        return this;
    }

    public FileScannerThread clearExtensionsToMatch() {
        extensionsToMatch.clear();
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
    public FileScannerThread addCompletionListener(CompletionListener listener) {
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
    public FileScannerThread addCancelListener(CancelListener listener) {
        cancelListeners.add(listener);
        return this;
    }

    public FileScannerThread removeCompletionListener(CompletionListener listener) {
        completionListeners.remove(listener);
        return this;
    }

    public FileScannerThread removeCancelListener(CancelListener listener) {
        cancelListeners.remove(listener);
        return this;
    }

    @Override
    public void run() {
        // We don't know how many files there are to scan, and finding out
        // is a costly operation. So, we fire a progressBegins event just
        // to get the progress dialog to show up. We'll correct it with
        // a second progressBegins event once we have an idea of what we're up against.
        fireProgressBegins(1);

        // Special-case stupid input:
        if (rootDir == null || !rootDir.isDirectory() || !rootDir.canRead()) {
            log.warning("FileScannerThread was given a null or unreadable rootDir. Aborting.");
            fireProgressError("rootDir", "rootDir is not a readable directory. Aborting.");
            fireProgressComplete();
            return;
        }

        results.clear();
        boolean wasCanceled = false;
        try {
            // Find ALL files, unfiltered. We'll filter them ourselves.
            List<File> files = FileSystemUtil.findFiles(rootDir, recursive);
            fireProgressBegins(files.size());

            int currentStep = 0;
            for (File file : files) {
                if (fileMatches(file)) {
                    results.add(file);
                }

                if (!fireProgressUpdate(currentStep++, file.getName())) {
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

    /**
     * Basically duplicating the logic from FileSystemUtil's findFilesRecurse() method,
     * but without the FileSearchListener nonsense. Our main search loop handles firing
     * progress events and checking for user cancellation.
     *
     * @param file The file to consider.
     * @return True if the file should be added to the results list, false to ignore it.
     */
    private boolean fileMatches(File file) {
        String filename = file.getName().toLowerCase();

        // if any extensions match, it's a hit:
        boolean fileMatched = false;
        for (String ext : extensionsToMatch) {
            if (filename.endsWith(ext)) {
                fileMatched = true;
                break; // Found match, no need to check other extensions
            }
        }

        // Special handling for empty extension list: if it's empty, we consider it a match for all files:
        if (extensionsToMatch.isEmpty()) {
            // If it's a regular, non-inverted search, empty list means "match everything".
            // If it's an inverted search, empty list means "exclude nothing".
            // "fileMatched" will be interpreted correctly in either case by the logic below.
            fileMatched = !invertSearch;
        }

        // If the file matched an extension and our search is not inverted, it's a hit:
        // OR if the file did NOT match any extension and our search IS inverted, it's a hit:
        return ((fileMatched && !invertSearch) || (!fileMatched && invertSearch));
    }
}
