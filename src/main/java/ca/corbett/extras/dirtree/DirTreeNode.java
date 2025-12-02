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

    public DirTreeNode(File dir) {
        super(dir.getName());
        this.dir = dir;
        this.hasChildren = hasChildren();
        setAllowsChildren(hasChildren);
        childrenLoaded = false;
    }

    @Override
    public boolean isLeaf() {
        return !hasChildren;
    }

    public File getDir() {
        return dir;
    }

    public void loadChildren() {
        if (!childrenLoaded) {
            int dirCount = 0;
            String[] fileNames = dir.list();
            if (fileNames != null) {
                Arrays.sort(fileNames, new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        return o1.toLowerCase().compareTo(o2.toLowerCase());
                    }
                });
                for (String fileName : fileNames) {
                    File file = new File(dir, fileName);
                    if (file.isDirectory()) {
                        add(new DirTreeNode(file));
                        dirCount++;
                    }
                }
                childrenLoaded = true;
                setAllowsChildren(dirCount > 0);
            }
        }
    }

    public boolean hasChildren() {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
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
        if (dir != null && !dir.getAbsolutePath().equals(that.dir.getAbsolutePath())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dir);
    }
}
