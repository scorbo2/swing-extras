package ca.corbett.extras.dirtree;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

/**
 * Represents a single node (directory or file) within our DirTree.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2017-11-09
 */
public final class DirTreeNode extends DefaultMutableTreeNode {
    private final File dir;
    private volatile boolean childrenLoaded;
    private final boolean hasChildren;
    private final boolean showHidden;
    private final boolean showFiles;
    private final FileFilter fileFilter;

    /**
     * Creates a new DirTreeNode for the given directory.
     *
     * @param dir            The directory (or file) this node represents.
     * @param showHiddenDirs Whether to show hidden directories and files.
     * @deprecated Use {@link #DirTreeNode(File, boolean, boolean, FileFilter)} instead.
     */
    @Deprecated(since = "swing-extras 2.8")
    public DirTreeNode(File dir, boolean showHiddenDirs) {
        this(dir, showHiddenDirs, false, null);
    }

    /**
     * Creates a new DirTreeNode for the given directory or file.
     *
     * @param dir        The directory (or file) this node represents.
     * @param showHidden Whether to show hidden directories and files.
     * @param showFiles  Whether to show files as child nodes of directories.
     * @param fileFilter An optional FileFilter to restrict which files are shown (null means all files).
     */
    public DirTreeNode(File dir, boolean showHidden, boolean showFiles, FileFilter fileFilter) {
        super(dir.getName());
        this.dir = dir;
        this.showHidden = showHidden;
        this.showFiles = showFiles;
        this.fileFilter = fileFilter;
        if (dir.isFile()) {
            this.hasChildren = false;
        }
        else {
            this.hasChildren = hasChildren();
        }
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

    /**
     * Returns whether this node represents a file (as opposed to a directory).
     *
     * @return true if this node represents a file.
     */
    public boolean isFileNode() {
        return dir.isFile();
    }

    /**
     * Indicates whether hidden directories and files are shown.
     *
     * @deprecated Use {@link #isShowHidden()} instead.
     */
    @Deprecated(since = "swing-extras 2.8")
    public boolean isShowHiddenDirs() {
        return showHidden;
    }

    /**
     * Indicates whether hidden directories and files are shown.
     */
    public boolean isShowHidden() {
        return showHidden;
    }

    public void loadChildren() {
        if (dir.isFile()) {
            return;
        }
        if (!childrenLoaded && dir.canRead()) {
            synchronized(this) {
                // Double-check after lock
                if (childrenLoaded) {
                    return;
                }
                int childCount = 0;
                String[] fileNames = dir.list();
                if (fileNames != null) {
                    Arrays.sort(fileNames, Comparator.comparing(String::toLowerCase));

                    // First pass: add directories
                    for (String fileName : fileNames) {
                        File file = new File(dir, fileName);
                        if (file.isDirectory()) {
                            if (file.isHidden() && !showHidden) {
                                continue;
                            }
                            add(new DirTreeNode(file, showHidden, showFiles, fileFilter));
                            childCount++;
                        }
                    }

                    // Second pass: add files (if showFiles is enabled)
                    if (showFiles) {
                        for (String fileName : fileNames) {
                            File file = new File(dir, fileName);
                            if (file.isFile()) {
                                if (file.isHidden() && !showHidden) {
                                    continue;
                                }
                                if (fileFilter != null && !fileFilter.accept(file)) {
                                    continue;
                                }
                                add(new DirTreeNode(file, showHidden, showFiles, fileFilter));
                                childCount++;
                            }
                        }
                    }

                    childrenLoaded = true;
                    setAllowsChildren(childCount > 0);
                }
            }
        }
    }

    public boolean hasChildren() {
        if (dir.isFile()) {
            return false;
        }
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (file.isHidden() && !showHidden) {
                        continue;
                    }
                    return true;
                }
            }
            // If showFiles is enabled, check for matching files too:
            if (showFiles) {
                for (File file : files) {
                    if (file.isFile()) {
                        if (file.isHidden() && !showHidden) {
                            continue;
                        }
                        if (fileFilter != null && !fileFilter.accept(file)) {
                            continue;
                        }
                        return true;
                    }
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
