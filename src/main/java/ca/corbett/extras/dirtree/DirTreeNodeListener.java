package ca.corbett.extras.dirtree;

/**
 * Listener interface for structural navigation events on a {@link DirTree}.
 * <p>
 * The <em>will</em> methods are <strong>vetoable</strong>: returning {@code false}
 * cancels the pending action. The remaining methods are purely informational.
 * <p>
 * Implementations only need to override the methods they care about; default
 * no-op / no-veto implementations are provided for every method.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public interface DirTreeNodeListener {

    /**
     * Called before a node is expanded. Return {@code false} to prevent expansion.
     *
     * @param event the event carrying the subject node
     * @return {@code true} to allow expansion, {@code false} to veto it
     */
    default boolean nodeWillExpand(DirTreeEvent event) {
        return true;
    }

    /**
     * Called before a node is collapsed. Return {@code false} to prevent collapse.
     *
     * @param event the event carrying the subject node
     * @return {@code true} to allow collapse, {@code false} to veto it
     */
    default boolean nodeWillCollapse(DirTreeEvent event) {
        return true;
    }

    /**
     * Called after a node has been expanded and its children are fully loaded.
     *
     * @param event the event carrying the subject node
     */
    default void nodeExpanded(DirTreeEvent event) {
    }

    /**
     * Called after a node has been collapsed.
     *
     * @param event the event carrying the subject node
     */
    default void nodeCollapsed(DirTreeEvent event) {
    }
}

