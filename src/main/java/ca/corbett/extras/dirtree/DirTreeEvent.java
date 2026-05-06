package ca.corbett.extras.dirtree;

import java.io.File;

/**
 * Immutable event object carrying information about a {@link DirTree} event.
 * <p>
 * Every event carries:
 * <ul>
 *   <li>The {@link Type} of the event</li>
 *   <li>The {@link DirTreeNode} that is the subject of the event</li>
 *   <li>The source {@link DirTree} that fired the event</li>
 * </ul>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public final class DirTreeEvent {

    /**
     * Enumerates all event types that a {@link DirTree} can fire.
     */
    public enum Type {

        // --- Navigation / structural events (vetoable) ---

        /** Fired before a node is expanded. Vetoable. */
        NODE_WILL_EXPAND,

        /** Fired before a node is collapsed. Vetoable. */
        NODE_WILL_COLLAPSE,

        // --- Navigation / structural events (informational) ---

        /** Fired after a node has been fully expanded (children loaded). */
        NODE_EXPANDED,

        /** Fired after a node has been collapsed. */
        NODE_COLLAPSED,

        // --- Selection / mouse events ---

        /** Fired before a node is selected. Vetoable. */
        NODE_WILL_SELECT,

        /** Fired when the user single-clicks to select a node. */
        NODE_SELECTED,

        /** Fired when the user deselects all nodes. */
        NODE_DESELECTED,

        /** Fired when the user right-clicks a node. */
        NODE_RIGHT_CLICKED,

        /** Fired when the user double-clicks a node. */
        NODE_DOUBLE_CLICKED,

        // --- Load lifecycle events ---

        /** Fired when a background load is initiated for a node. */
        LOAD_STARTED,

        /** Fired periodically as children are discovered during a background load. */
        LOAD_PROGRESS,

        /** Fired when a background load completes successfully. */
        LOAD_COMPLETE,

        /** Fired when the user cancels an in-progress background load. */
        LOAD_CANCELLED,

        /** Fired when a background load fails due to an I/O error or permission problem. */
        LOAD_FAILED,

        // --- Refresh ---

        /** Fired when the tree (or a subtree) is refreshed/reloaded. */
        TREE_REFRESHED,

        // --- Lock / unlock ---

        /** Fired after the tree has been locked to a chroot directory. */
        TREE_LOCKED,

        /** Fired after the tree has been unlocked and restored to its previous roots. */
        TREE_UNLOCKED
    }

    private final Type type;
    private final DirTree source;
    private final DirTreeNode node;

    /** For LOAD_PROGRESS: number of children discovered so far. -1 if not applicable. */
    private final int progressCount;

    /** For LOAD_FAILED: the exception that caused the failure. May be null. */
    private final Throwable cause;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Creates a basic event with no progress or error information.
     *
     * @param type   the event type
     * @param source the originating DirTree
     * @param node   the subject node
     */
    public DirTreeEvent(Type type, DirTree source, DirTreeNode node) {
        this(type, source, node, -1, null);
    }

    /**
     * Creates a LOAD_PROGRESS event.
     *
     * @param source        the originating DirTree
     * @param node          the subject node
     * @param progressCount number of children discovered so far
     */
    public DirTreeEvent(DirTree source, DirTreeNode node, int progressCount) {
        this(Type.LOAD_PROGRESS, source, node, progressCount, null);
    }

    /**
     * Creates a LOAD_FAILED event.
     *
     * @param source the originating DirTree
     * @param node   the subject node
     * @param cause  the exception that caused the failure
     */
    public DirTreeEvent(DirTree source, DirTreeNode node, Throwable cause) {
        this(Type.LOAD_FAILED, source, node, -1, cause);
    }

    private DirTreeEvent(Type type, DirTree source, DirTreeNode node, int progressCount, Throwable cause) {
        this.type = type;
        this.source = source;
        this.node = node;
        this.progressCount = progressCount;
        this.cause = cause;
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /** Returns the event type. */
    public Type getType() {
        return type;
    }

    /** Returns the {@link DirTree} that fired this event. */
    public DirTree getSource() {
        return source;
    }

    /**
     * Returns the {@link DirTreeNode} that is the subject of this event.
     * May be {@code null} for events that are not node-specific (e.g. {@link Type#NODE_DESELECTED}).
     */
    public DirTreeNode getNode() {
        return node;
    }

    /**
     * Convenience accessor: returns the {@link File} associated with the subject node,
     * or {@code null} if the node is null.
     */
    public File getFile() {
        return node == null ? null : node.getFile();
    }

    /**
     * For {@link Type#LOAD_PROGRESS} events: returns the number of children
     * discovered so far, or {@code -1} for all other event types.
     */
    public int getProgressCount() {
        return progressCount;
    }

    /**
     * For {@link Type#LOAD_FAILED} events: returns the exception that caused the failure,
     * or {@code null} for all other event types.
     */
    public Throwable getCause() {
        return cause;
    }

    @Override
    public String toString() {
        return "DirTreeEvent{type=" + type
                + ", file=" + getFile()
                + (progressCount >= 0 ? ", progress=" + progressCount : "")
                + (cause != null ? ", cause=" + cause.getMessage() : "")
                + "}";
    }
}

