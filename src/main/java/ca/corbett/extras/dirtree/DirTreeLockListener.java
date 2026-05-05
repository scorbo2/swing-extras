package ca.corbett.extras.dirtree;

/**
 * Listener interface for lock and unlock events on a {@link DirTree}.
 * <p>
 * Lock events are informational only — they cannot be vetoed.
 * <p>
 * All methods have default no-op implementations so callers only need to
 * override the events they care about.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @see DirTree#chroot(java.io.File)
 * @see DirTree#lock(java.io.File)
 * @see DirTree#unlock()
 */
public interface DirTreeLockListener {

    /**
     * Called after the tree has been locked to a chroot directory.
     * {@link DirTreeEvent#getFile()} returns the directory the tree is now rooted at.
     *
     * @param event the event carrying the locked directory's node
     */
    default void treeLocked(DirTreeEvent event) {
    }

    /**
     * Called after the tree has been unlocked and restored to its previous roots.
     * {@link DirTreeEvent#getNode()} will be {@code null}.
     *
     * @param event the event
     */
    default void treeUnlocked(DirTreeEvent event) {
    }
}
