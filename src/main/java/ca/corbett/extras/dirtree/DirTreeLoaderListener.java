package ca.corbett.extras.dirtree;

/**
 * Listener interface for background-load lifecycle events on a {@link DirTree}.
 * <p>
 * All methods have default no-op implementations so callers only need to
 * override the events they care about.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public interface DirTreeLoaderListener {

    /**
     * Called on the EDT when a background directory load is about to start.
     *
     * @param event the event carrying the subject node
     */
    default void loadStarted(DirTreeEvent event) {
    }

    /**
     * Called on the EDT periodically as children are discovered during a background load.
     * {@link DirTreeEvent#getProgressCount()} returns the number discovered so far.
     *
     * @param event the event carrying progress information
     */
    default void loadProgress(DirTreeEvent event) {
    }

    /**
     * Called on the EDT when a background load completes successfully and the
     * tree model has been updated.
     *
     * @param event the event carrying the subject node
     */
    default void loadComplete(DirTreeEvent event) {
    }

    /**
     * Called on the EDT when an in-progress background load is cancelled
     * (e.g. the user presses the Cancel button in the loading indicator).
     *
     * @param event the event carrying the subject node
     */
    default void loadCancelled(DirTreeEvent event) {
    }

    /**
     * Called on the EDT when a background load fails due to an I/O error
     * or permission problem. {@link DirTreeEvent#getCause()} carries the exception.
     *
     * @param event the event carrying the subject node and cause
     */
    default void loadFailed(DirTreeEvent event) {
    }
}

