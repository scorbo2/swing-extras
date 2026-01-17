package ca.corbett.extras.dirtree;

/**
 * An adapter class for DirTreeListener with empty method implementations.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.7
 */
public class DirTreeAdapter implements DirTreeListener {

    @Override
    public boolean selectionWillChange(DirTree source, java.io.File newSelectedDir) {
        return true; // default allow change
    }

    @Override
    public void selectionChanged(DirTree source, java.io.File selectedDir) {
        // No-op
    }

    @Override
    public void showHiddenFilesChanged(DirTree source, boolean showHiddenFiles) {
        // No-op
    }

    @Override
    public void treeLocked(DirTree source, java.io.File lockDir) {
        // No-op
    }

    @Override
    public void treeUnlocked(DirTree source) {
        // No-op
    }
}
