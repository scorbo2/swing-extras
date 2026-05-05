package ca.corbett.extras.dirtree;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;

/**
 * A tree node that wraps a {@link File} (directory or regular file) for use
 * inside a {@link DirTree}.
 * <p>
 * Each node starts in {@link LoadState#UNLOADED} and carries a single
 * <em>placeholder</em> child so that {@code JTree} renders the expand arrow
 * before the first real load. The placeholder is replaced with real children
 * once the background loader finishes.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class DirTreeNode extends DefaultMutableTreeNode {

    /**
     * Tracks the loading lifecycle of a node's children.
     */
    public enum LoadState {
        /** Children have never been loaded. A placeholder child is present. */
        UNLOADED,
        /** A background load is in progress. */
        LOADING,
        /** Children have been loaded successfully. */
        LOADED,
        /** The last load attempt failed (I/O error, permission denied, etc.). */
        ERROR
    }

    // Sentinel node used as a placeholder child before the first real load.
    // All DirTreeNode instances that are directories share a reference to this.
    static final DirTreeNode LOADING_PLACEHOLDER = new DirTreeNode(null, false);

    private final File file;
    private final boolean isDirectory;
    private LoadState loadState;

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    /**
     * Creates a node for the given file.
     *
     * @param file        the file or directory this node represents; may be {@code null}
     *                    only for the invisible synthetic root node
     * @param isDirectory whether this node should behave as a directory (expandable)
     */
    public DirTreeNode(File file, boolean isDirectory) {
        super(file);
        this.file = file;
        this.isDirectory = isDirectory;

        if (isDirectory) {
            loadState = LoadState.UNLOADED;
            setAllowsChildren(true);
            // Add a placeholder so JTree shows the expand arrow immediately.
            super.add(LOADING_PLACEHOLDER);
        }
        else {
            // Files are always leaves — no placeholder needed, no expand arrow.
            loadState = LoadState.LOADED;
            setAllowsChildren(false);
        }
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /** Returns the {@link File} this node represents, or {@code null} for the synthetic root. */
    public File getFile() {
        return file;
    }

    /** Returns {@code true} if this node represents a directory. */
    public boolean isDirectory() {
        return isDirectory;
    }

    /** Returns the current {@link LoadState} of this node. */
    public LoadState getLoadState() {
        return loadState;
    }

    /** Updates the load state. Should be called on the EDT. */
    public void setLoadState(LoadState loadState) {
        this.loadState = loadState;
    }

    /**
     * Returns {@code true} if this node is the shared loading placeholder sentinel.
     */
    public boolean isPlaceholder() {
        return this == LOADING_PLACEHOLDER;
    }

    /**
     * Removes the placeholder child (if present) without affecting real children.
     * Should be called on the EDT before inserting real children.
     */
    public void removePlaceholder() {
        if (getChildCount() > 0) {
            DirTreeNode first = (DirTreeNode)getChildAt(0);
            if (first.isPlaceholder()) {
                super.remove(0);
            }
        }
    }

    // -------------------------------------------------------------------------
    // JTree / TreeNode overrides
    // -------------------------------------------------------------------------

    /**
     * A directory node is always reported as a non-leaf so that JTree shows the
     * expand arrow, even before children are loaded. A file node is always a leaf.
     */
    @Override
    public boolean isLeaf() {
        return !isDirectory;
    }

    /**
     * Returns a human-readable label for this node. For the invisible synthetic root
     * and for the loading placeholder, special strings are returned.
     */
    @Override
    public String toString() {
        if (isPlaceholder()) {
            return "Loading\u2026"; // "Loading…"
        }
        if (file == null) {
            return "";  // synthetic root — should never be visible
        }
        // On Windows, File.listRoots() entries look like "C:\"; getName() returns "".
        String name = file.getName();
        if (name.isEmpty()) {
            return file.getAbsolutePath();
        }
        return name;
    }
}

