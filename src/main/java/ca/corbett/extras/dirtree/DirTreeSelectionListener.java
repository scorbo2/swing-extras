package ca.corbett.extras.dirtree;

/**
 * Listener interface for selection and mouse events on a {@link DirTree}.
 * <p>
 * The <em>will</em> method is <strong>vetoable</strong>: returning {@code false}
 * cancels the pending selection. The remaining methods are purely informational.
 * <p>
 * All methods have default no-op / no-veto implementations so callers only need to
 * override the events they care about.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public interface DirTreeSelectionListener {

    /**
     * Called before a node is selected. Return {@code false} to prevent the selection change.
     * <p>
     * This can be used, for example, to keep the tree on the currently selected node
     * until unsaved changes have been committed.
     *
     * @param event the event carrying the node that is about to be selected
     * @return {@code true} to allow the selection, {@code false} to veto it
     */
    default boolean nodeWillSelect(DirTreeEvent event) {
        return true;
    }

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

