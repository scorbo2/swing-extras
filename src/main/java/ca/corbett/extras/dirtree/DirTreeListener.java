package ca.corbett.extras.dirtree;

import java.io.File;

/**
 * Provides a way to listen for events from a DirTree instance.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2017-11-11
 */
public interface DirTreeListener {

    /**
     * Invoked before a selection change occurs within the DirTree.
     * You can return false here to veto the selection change.
     *
     * @param source         The DirTree instance triggering this event.
     * @param newSelectedDir The directory that is about to be selected.
     * @return True to allow the selection change, false to veto it.
     */
    boolean selectionWillChange(DirTree source, File newSelectedDir);

    /**
     * Fired when a directory is selected within the DirTree.
     *
     * @param source      The DirTree instance triggering this event.
     * @param selectedDir The newly selected directory.
     */
    void selectionChanged(DirTree source, File selectedDir);


    /**
     * Fired when a DirTree instance is locked to a specific subdirectory (like chroot).
     *
     * @param source  The DirTree instance triggering this event.
     * @param lockDir The subdirectory to which the DirTree is now locked.
     */
    void treeLocked(DirTree source, File lockDir);


    /**
     * Fired when a DirTree instance is unlocked, and will now show all directories.
     *
     * @param source The DirTree instance triggering this event.
     */
    void treeUnlocked(DirTree source);
}
