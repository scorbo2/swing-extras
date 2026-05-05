package ca.corbett.extras.dirtree;

/**
 * Listener interface for selection and mouse events on a {@link DirTree}.
 * <p>
 * All methods have default no-op implementations so callers only need to
 * override the events they care about.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public interface DirTreeSelectionListener {

    /**
     * Called when a node is single-clicked / selected.
     *
     * @param event the event carrying the subject node
     */
    default void nodeSelected(DirTreeEvent event) {
    }

    /**
     * Called when the selection is cleared (no node selected).
     *
     * @param event the event; {@link DirTreeEvent#getNode()} will be {@code null}
     */
    default void nodeDeselected(DirTreeEvent event) {
    }

    /**
     * Called when a node is right-clicked.
     *
     * @param event the event carrying the subject node
     */
    default void nodeRightClicked(DirTreeEvent event) {
    }

    /**
     * Called when a node is double-clicked.
     *
     * @param event the event carrying the subject node
     */
    default void nodeDoubleClicked(DirTreeEvent event) {
    }
}

