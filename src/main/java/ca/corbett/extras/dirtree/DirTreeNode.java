package ca.corbett.extras.dirtree;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

/**
 * Represents a single directory within our DirTree.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2017-11-09
 */
public final class DirTreeNode extends DefaultMutableTreeNode {
    private final File dir;
    private boolean childrenLoaded;
    private final boolean hasChildren;
    private final boolean showHiddenDirs;

    public DirTreeNode(File dir, boolean showHiddenDirs) {
        super(dir.getName());
        this.dir = dir;
        this.hasChildren = hasChildren();
        setAllowsChildren(hasChildren);
        childrenLoaded = false;
        this.showHiddenDirs = showHiddenDirs;
    }

    @Override
    public boolean isLeaf() {
        return !hasChildren;
    }

    public File getDir() {
        return dir;
    }

    public boolean isShowHiddenDirs() {
        return showHiddenDirs;
    }

    public void loadChildren() {
        if (!childrenLoaded && dir.canRead()) {
            synchronized(this) {
                // Double-check after lock
                if (childrenLoaded) {
                    return;
                }
                int dirCount = 0;
                String[] fileNames = dir.list();
                if (fileNames != null) {
                    Arrays.sort(fileNames, Comparator.comparing(String::toLowerCase));
                    for (String fileName : fileNames) {
                        File file = new File(dir, fileName);
                        if (file.isDirectory()) {
                            // Skip hidden directories if the flag is set
                            if (file.isHidden() && !showHiddenDirs) {
                                continue;
                            }
                            add(new DirTreeNode(file, showHiddenDirs));
                            dirCount++;
                        }
                    }
                    childrenLoaded = true;
                    setAllowsChildren(dirCount > 0);
                }
            }
        }
    }

    public boolean hasChildren() {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // Skip hidden directories if the flag is set
                    if (file.isHidden() && !showHiddenDirs) {
                        continue;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof DirTreeNode that)) { return false; }
        if (dir == null && that.dir != null) {
            return false;
        }
        if (dir != null && that.dir == null) {
            return false;
        }
        return dir == null || dir.getAbsolutePath().equals(that.dir.getAbsolutePath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(dir);
    }
}
