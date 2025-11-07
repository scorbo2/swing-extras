package ca.corbett.extras.dirtree;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.BorderLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;

/**
 * A component that renders directories in the file system as a navigable tree view.
 * Note that this is pretty heavily targeted towards linux, with the concept
 * of a single root directory and an arbitrarily large hierarchy of subdirectories.
 * Take a look in the swing-extras demo app for an example of how this can be made
 * to work on systems that have multiple filesystem root directories. It can be done!
 * <p>
 * The DirTree can be "locked" to any given directory - this will treat that directory
 * as the root directory, so the tree will only show subdirectories of that directory.
 * By default, the DirTree is "unlocked" so the root directory is the actual root
 * directory of the filesystem. By default a DirTree component is not locked to any
 * subdirectory, so will show all directories from root on down.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2017-11-09
 */
public final class DirTree extends JPanel implements TreeSelectionListener {

    private static final String DEFAULT_ROOT = "/";

    private final JScrollPane scrollPane;
    private final JTree tree;
    private final ArrayList<DirTreeListener> listeners;

    private boolean isLocked;
    private DirTreeNode rootNode;
    private DirTreeNode currentNode;
    private boolean allowLock;
    private boolean allowUnlock;

    /**
     * Constructor is private to enforce factory access.
     */
    private DirTree(File rootDir) {
        listeners = new ArrayList<>();
        tree = new JTree();
        allowLock = true;
        allowUnlock = true;

        // Cosmetic adjustments to stock JTree:
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        TreeCellRenderer renderer = tree.getCellRenderer();
        ((DefaultTreeCellRenderer)renderer).setClosedIcon(null);
        ((DefaultTreeCellRenderer)renderer).setLeafIcon(null);
        ((DefaultTreeCellRenderer)renderer).setOpenIcon(null);

        // Force single selection and enable lazy load of subdirectories:
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeWillExpandListener(new DirTreeExpandListener());

        // chroot to the specified root directory:
        lock(rootDir);

        // Set up our layout and scrollpane:
        setLayout(new BorderLayout());
        scrollPane = new JScrollPane(tree);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Creates a defaulted DirTree instance for viewing the entire filesystem.
     *
     * @return The new DirTree instance.
     */
    public static DirTree createDirTree() {
        return createDirTree(new File(DEFAULT_ROOT));
    }

    /**
     * Creates a DirTree instance chrooted to the specified directory.
     *
     * @param dir The directory to which to lock this DirTree (use unlock() to unlock it).
     * @return The new DirTree instance.
     */
    public static DirTree createDirTree(File dir) {
        DirTree dirTree = new DirTree(dir);

        // Create a right click popup menu and associate it with this DirTree:
        dirTree.setComponentPopupMenu(DirTreePopupMenu.createPopup(dirTree));

        // Listen for selection events within our tree so we can notify our own listeners:
        dirTree.getTree().addTreeSelectionListener(dirTree);

        return dirTree;
    }

    /**
     * Returns the current root directory (may not be "/" if this DirTree is currently
     * locked to some subdirectory. This method will return whatever directory this
     * DirTree instance is currently treating as root.
     *
     * @return The current root directory (or chroot directory).
     */
    public File getRootDir() {
        return rootNode.getDir();
    }

    /**
     * Returns the currently selected directory, or null if nothing is selected.
     *
     * @return The current directory, or null if nothing selected.
     */
    public File getCurrentDir() {
        return (currentNode == null) ? null : currentNode.getDir();
    }

    /**
     * Registers a DirTreeListener with this DirTree.
     *
     * @param listener The DirTreeListener which will receive events from us.
     */
    public void addDirTreeListener(DirTreeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Unregisters a DirTreeListener from this instance.
     *
     * @param listener The DirTreeListener to unregister.
     */
    public void removeDirTreeListener(DirTreeListener listener) {
        listeners.remove(listener);
    }

    /**
     * For some reason, setEnabled is not forwarded from parent components to child components
     * by default. So, if someone invokes setEnabled on this DirTree, we want to forward that
     * message to our contained JTree and scroll pane.
     */
    @Override
    public void setEnabled(boolean enabled) {
        tree.setEnabled(enabled);
        scrollPane.setWheelScrollingEnabled(enabled);
        scrollPane.getHorizontalScrollBar().setEnabled(enabled);
        scrollPane.getVerticalScrollBar().setEnabled(enabled);
    }

    /**
     * Returns the actual JTree component. For internal package use only.
     *
     * @return The JTree contained by this component.
     */
    protected JTree getTree() {
        return tree;
    }

    /**
     * Indicates whether this DirTree is "locked" to a specific subdirectory
     * (similar to chroot). Use unlock() to revert to the default display where all
     * directories will be shown.
     *
     * @return Whether our display is filtered to a specific subdirectory (see getRootDirectory()).
     */
    public boolean isLocked() {
        return isLocked;
    }

    /**
     * Indicates whether this DirTree allows locking (chrooting). Default is true.
     *
     * @return Whether this DirTree allows locking.
     */
    public boolean getAllowLock() {
        return allowLock;
    }

    /**
     * Indicates whether this DirTree allows unlocking (de-chrooting). Default is true.
     *
     * @return Whether this DirTree allows unlocking.
     */
    public boolean getAllowUnlock() {
        return allowUnlock;
    }

    /**
     * Enables or disables locking within this DirTree (chrooting). Disabling this won't unlock
     * the DirTree if it was already locked. This will simply avoid creating the "lock to
     * directory" option from appearing in the popup menu.
     *
     * @param allow Whether to enable locking.
     */
    public void setAllowLock(boolean allow) {
        allowLock = allow;
    }

    /**
     * Enables or disables unlocking within this DirTree (chrooting). Disabling this will
     * prevent the "unlock" menu item from appearing in the popup menu.
     *
     * @param allow Whether to enable unlocking.
     */
    public void setAllowUnlock(boolean allow) {
        allowUnlock = allow;
    }

    /**
     * Reloads this DirTree and then selects the specified directory.
     *
     * @param dir The directory to select and scroll to, after reloading.
     */
    public void reload(File dir) {
        if (dir == null || !dir.exists()) {
            dir = rootNode.getDir();
        }

        rootNode = new DirTreeNode(rootNode.getDir());
        File[] files = rootNode.getDir().listFiles();
        if (files != null) {
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
                }

            });
            for (File file : files) {
                if (file.isDirectory()) {
                    rootNode.add(new DirTreeNode(file));
                }
            }
        }

        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
        treeModel.setAsksAllowsChildren(true);
        tree.setModel(new DefaultTreeModel(rootNode));
        selectAndScrollTo(dir);
    }

    /**
     * Sets the root directory of this tree. The root itself is not displayed (i.e. only
     * children of the root directory are shown). Use unlock() to revert to the default
     * display where all directories will be shown. If the given directory does not exist,
     * this will unlock the DirTree and default it back to "/".
     *
     * @param rootDir The new root directory.
     */
    public void lock(File rootDir) {
        if (rootDir == null || !rootDir.exists()) {
            rootDir = new File(DEFAULT_ROOT);
        }
        rootNode = new DirTreeNode(rootDir);
        File[] files = rootDir.listFiles();
        if (files != null) {
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
                }

            });
            for (File file : files) {
                if (file.isDirectory()) {
                    rootNode.add(new DirTreeNode(file));
                }
            }
        }

        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
        treeModel.setAsksAllowsChildren(true);
        tree.setModel(new DefaultTreeModel(rootNode));
        isLocked = !rootDir.getAbsolutePath().equals(DEFAULT_ROOT);

        if (isLocked) {
            fireLockEvent();
        }
        else {
            fireUnlockEvent();
        }
    }

    /**
     * Sets the root directory to be the root of the filesystem (ie All directories will be shown).
     * Does nothing if that was already the case.
     */
    public void unlock() {
        // Make a note of the old root so we can select it after refreshing the tree:
        DirTreeNode oldRoot = rootNode;

        // Unlock the tree (can be skipped if not currently locked):
        if (isLocked) {
            lock(new File(DEFAULT_ROOT));
        }

        // Select the old root:
        selectAndScrollTo(oldRoot.getDir());
    }

    /**
     * Selects and scrolls to the given directory. Does nothing if the directory doesn't exist.
     *
     * @param dir The directory to select.
     */
    public void selectAndScrollTo(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return;
        }

        // In order to find the given directory, we have to programmatically expand the tree path
        // to get to it (if it's more than one level deep, it won't exist in the tree right now
        // because sub-nodes are lazily loaded).
        // Start by breaking up the full path of the directory and going dir by dir:
        String[] path = dir.getAbsolutePath().split("/");
        String pathStr = "";

        // For each directory, find the tree node and tell it to load its children:
        for (String pathElement : path) {
            if (pathElement == null || pathElement.equals("")) {
                continue;
            }
            pathStr += "/" + pathElement;
            DirTreeNode tmpNode = new DirTreeNode(new File(pathStr));
            TreePath treePath = findPathToNode(tmpNode);
            if (treePath != null) {
                ((DirTreeNode)treePath.getLastPathComponent()).loadChildren();
            }
        }

        // Now we should be able to find the old root node, unless something went wrong:
        TreePath pathToOldRoot = findPathToNode(new DirTreeNode(dir));
        tree.getSelectionModel().setSelectionPath(pathToOldRoot);
        tree.scrollPathToVisible(pathToOldRoot);
    }

    /**
     * Removes the given directory from this DirTree. Assuming here that the caller
     * is invoking this to inform us that they have deleted the directory in question.
     *
     * @param dir The directory to find and remove from this DirTree.
     */
    public void removeDirectory(File dir) {
        if (dir == null) {
            return;
        }
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        TreePath path = findPathToNode(new DirTreeNode(dir));
        if (path != null) {
            DirTreeNode node = (DirTreeNode)path.getLastPathComponent();
            model.removeNodeFromParent(node);
        }
    }

    /**
     * Finds and returns a specific tree path by looking for the given node.
     * Implementation note: you may be wondering why not just invoke dirNode.getPath(). Well,
     * when locking and unlocking the tree, we play havoc with the "actual" path to any given
     * node (by converting a child node into a "root" (or chroot) node). So, this method
     * ignores getPath() and instead searches the entire tree looking for a node whose
     * directory matches the given node's directory. Inefficient but accurate.
     *
     * @param dirNode The DirTreeNode whose path we want to look up.
     * @return The TreePath for this node.
     */
    private TreePath findPathToNode(DirTreeNode dirNode) {
        File targetDir = (File)dirNode.getDir();
        Enumeration<TreeNode> e = rootNode.depthFirstEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.nextElement();
            if (((DirTreeNode)node).getDir().equals(targetDir)) {
                return new TreePath(node.getPath());
            }
        }

        return null;
    }

    /**
     * Overridden from TreeSelectionListener, so we can listen for selection events within
     * our JTree and notify our own listeners that the selection has changed.
     *
     * @param e The TreeSelectionEvent.
     */
    @Override
    public void valueChanged(TreeSelectionEvent e) {
        if (e.getNewLeadSelectionPath() == null
                || e.getNewLeadSelectionPath().getLastPathComponent() == null) {
            currentNode = null;
            return;
        }

        currentNode = (DirTreeNode)e.getNewLeadSelectionPath().getLastPathComponent();
        fireSelectionChangedEvent(currentNode);
    }

    /**
     * Used internally to notify listeners that this tree has been locked (chrooted) to a
     * specific subdirectory.
     */
    private void fireLockEvent() {
        for (DirTreeListener listener : new ArrayList<>(listeners)) {
            listener.treeLocked(this, rootNode.getDir());
        }
    }

    /**
     * Used internally to notify listeners that this tree has been unlocked.
     */
    private void fireUnlockEvent() {
        for (DirTreeListener listener : new ArrayList<>(listeners)) {
            listener.treeUnlocked(this);
        }
    }

    /**
     * Used internally to notify listeners that the selection has changed.
     *
     * @param node The newly selected node.
     */
    private void fireSelectionChangedEvent(DirTreeNode node) {
        for (DirTreeListener listener : new ArrayList<>(listeners)) {
            listener.selectionChanged(this, node.getDir());
        }
    }

}
