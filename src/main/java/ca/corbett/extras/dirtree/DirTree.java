package ca.corbett.extras.dirtree;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A component that renders directories in the file system as a navigable tree view.
 * The DirTree can be "locked" to a specific directory, in which case it will only display
 * subdirectories of that directory. When "unlocked", the DirTree will display all
 * filesystem roots (e.g. "/" on Linux-based systems, or all available drives on Windows).
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2017-11-09
 */
public final class DirTree extends JPanel implements TreeSelectionListener {

    private static final Logger log = Logger.getLogger(DirTree.class.getName());

    // This is used to generate a "fake" root node when in unlocked mode. It is never visible to the user.
    private static final String FAKE_ROOT_NAME = "_NotARealRoot_";

    private final JScrollPane scrollPane;
    private final JTree tree = new JTree();
    private final List<DirTreeListener> listeners = new CopyOnWriteArrayList<>();

    private DirTreeNode fakeRootNode; // The "fake" node at the top of the tree - only used if lockNode == null
    private DirTreeNode lockNode;     // The node representing the locked root directory (will be null if unlocked)
    private DirTreeNode currentNode; // May be null if no selection - always represents a filesystem location if set
    private boolean allowLock;
    private boolean allowUnlock;
    private boolean showHiddenDirs;
    private boolean notificationsEnabled = true;

    /**
     * Creates a DirTree in "unlocked" mode, showing all filesystem roots.
     * You can then use the lock() method to lock (chroot) the DirTree to a
     * specific subdirectory.
     */
    public DirTree() {
        allowLock = true;
        allowUnlock = true;
        showHiddenDirs = true;

        // Cosmetic adjustments to stock JTree:
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        TreeCellRenderer renderer = tree.getCellRenderer();
        if (renderer instanceof DefaultTreeCellRenderer) {
            ((DefaultTreeCellRenderer)renderer).setClosedIcon(null);
            ((DefaultTreeCellRenderer)renderer).setLeafIcon(null);
            ((DefaultTreeCellRenderer)renderer).setOpenIcon(null);
        }

        // Force single selection and enable lazy load of subdirectories:
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeWillExpandListener(new DirTreeExpandListener());

        // This will load and populate our root node(s):
        unlock(true);

        // Use our custom selection model that allows us to veto selection changes:
        tree.setSelectionModel(new ValidatingTreeSelectionModel());

        // Set up our layout and scrollpane:
        setLayout(new BorderLayout());
        scrollPane = new JScrollPane(tree);
        add(scrollPane, BorderLayout.CENTER);

        // Add our right-click popup menu:
        setComponentPopupMenu(DirTreePopupMenu.createPopup(this));

        // Make sure we listen for selection events within our tree so we can notify our own listeners:
        tree.addTreeSelectionListener(this);
    }

    /**
     * Creates a new DirTree instance chrooted to the specified directory.
     */
    public DirTree(File rootDir) {
        this(); // Invoke no-arg constructor to do common initialization

        // Check our given rootDir to ensure it makes sense:
        if (rootDir == null || !rootDir.exists() || !rootDir.isDirectory()) {
            log.warning("DirTree: given rootDir is null, doesn't exist, or is not a directory."
                                + " Starting in unlocked mode.");
            // The no-arg constructor already invoked unlock(), so we're done here:
            return;
        }

        // Start in "locked" mode chrooted to this directory:
        lock(rootDir);
    }

    /**
     * Creates a defaulted DirTree instance for viewing the first available filesystem root.
     * Don't use this! It dates back to when DirTree was heavily linux-specific.
     * On Windows, this will only show the first available drive, which is probably not what you want.
     * The no-args constructor is the safest way to instantiate a DirTree
     * in a platform-independent way.
     *
     * @return The new DirTree instance.
     * @deprecated Use the constructor instead. This factory method may be removed in a future release.
     */
    @Deprecated(since = "swing-extras 2.7", forRemoval = true)
    public static DirTree createDirTree() {
        return createDirTree(getFilesystemRoots()[0]);
    }

    /**
     * Creates a DirTree instance chrooted to the specified directory.
     * As of swing-extras 2.7, this is exactly equivalent to new DirTree(dir).
     *
     * @param dir The directory to which to lock this DirTree (use unlock() to unlock it).
     * @return The new DirTree instance.
     * @deprecated Use the constructor instead. This factory method may be removed in a future release.
     */
    @Deprecated(since = "swing-extras 2.7", forRemoval = true)
    public static DirTree createDirTree(File dir) {
        return new DirTree(dir);
    }

    /**
     * Provides access to the TreeCellRenderer in the underlying JTree.
     * The default renderer is a standard DefaultTreeCellRenderer with all icons set to null.
     *
     * @return The TreeCellRenderer used by the underlying JTree.
     */
    public TreeCellRenderer getTreeCellRenderer() {
        return tree.getCellRenderer();
    }

    /**
     * Allow setting a custom TreeCellRenderer on the underlying JTree. Note that the default renderer is a
     * DefaultTreeCellRenderer with all icons set to null, so if you want to preserve that behavior, you will
     * need to set the icons to null on your custom renderer as well.
     *
     * @param renderer The TreeCellRenderer to use for rendering tree nodes.
     * @return This DirTree instance, for chaining.
     */
    public DirTree setTreeCellRenderer(TreeCellRenderer renderer) {
        tree.setCellRenderer(renderer);
        return this;
    }

    /**
     * This is overridden so that we can ensure the underlying JTree will pick
     * up the desired background color. If you've changed the background, and you want
     * to revert to letting the current Look and Feel choose the color selection for you,
     * you can use LookAndFeelManager.getLafColor() with "Tree.background" as the key name.
     *
     * @param color the desired background <code>Color</code>.
     */
    @Override
    public void setBackground(Color color) {
        super.setBackground(color);
        if (tree != null) {
            tree.setBackground(color);
        }
        if (scrollPane != null) {
            scrollPane.setBackground(color);
        }
    }

    /**
     * Returns all filesystem roots for the current platform.
     * On Unix-like systems, this typically returns a single root ("/").
     * On Windows, this returns all available drives.
     *
     * @return Array of all filesystem roots.
     */
    public static File[] getFilesystemRoots() {
        File[] roots = File.listRoots();
        return (roots != null)
                ? roots // Return all found roots
                : new File[]{new File("/")}; // Fallback to "/", but this *should* never happen
    }

    /**
     * Returns the current lock directory, if the tree is locked, otherwise null.
     *
     * @return The current lock directory, or null if not locked.
     */
    public File getLockDir() {
        return lockNode == null ? null : lockNode.getDir();
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
    public DirTree addDirTreeListener(DirTreeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
        return this;
    }

    /**
     * Unregisters a DirTreeListener from this instance.
     *
     * @param listener The DirTreeListener to unregister.
     */
    public DirTree removeDirTreeListener(DirTreeListener listener) {
        listeners.remove(listener);
        return this;
    }

    /**
     * Calls to setEnabled are not forwarded from parent components to child components.
     * So, if someone invokes setEnabled on this DirTree, we have to forward that
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
    JTree getTree() {
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
        if (lockNode == null) {
            return false;
        }

        // Special case: if we are locked to "/" on a Linux-based system, consider ourselves unlocked:
        if (lockNode.getDir().getAbsolutePath().equals("/")
                && getFilesystemRoots().length == 1) {
            return false;
        }

        // Otherwise, we are locked:
        return true;
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
     * Enables or disables locking within this DirTree (chrooting). Passing false will
     * unlock the DirTree if it is currently locked, and will prevent the "lock" menu
     * item from appearing in the popup menu. Disallowing a tree lock will also
     * disallow unlocking.
     *
     * @param allow Whether to enable locking.
     */
    public DirTree setAllowLock(boolean allow) {
        allowLock = allow;
        if (!allowLock) {
            setAllowUnlock(false); // arguable, but makes sense to do this implicitly
            unlock(true); // force reload will reselect current dir if possible
        }
        setComponentPopupMenu(DirTreePopupMenu.createPopup(this)); // refresh popup menu
        return this;
    }

    /**
     * Enables or disables unlocking within this DirTree (chrooting). Disabling this will
     * prevent the "unlock" menu item from appearing in the popup menu.
     *
     * @param allow Whether to enable unlocking.
     */
    public DirTree setAllowUnlock(boolean allow) {
        allowUnlock = allow;
        setComponentPopupMenu(DirTreePopupMenu.createPopup(this)); // refresh popup menu
        return this;
    }

    /**
     * Indicates whether hidden directories are shown in this DirTree. Default is true.
     */
    public boolean getShowHiddenDirs() {
        return showHiddenDirs;
    }

    /**
     * Controls whether hidden directories are shown in this DirTree. Default is true.
     * What constitutes a "hidden" directory is platform-dependent.
     */
    public DirTree setShowHiddenDirs(boolean showHiddenDirs) {
        boolean oldValue = this.showHiddenDirs;
        this.showHiddenDirs = showHiddenDirs;

        // Don't reload or notify if the value didn't actually change:
        if (oldValue == showHiddenDirs) {
            return this;
        }

        reload(); // force a reload to apply the new setting
        fireHiddenFilesChangedEvent(); // notify listeners of the change
        return this;
    }

    /**
     * Reloads this DirTree. When the reload is complete, the currently selected
     * directory (if any) will be re-selected.
     */
    public void reload() {
        File currentDir = getCurrentDir();

        // If we're locked, we need to reload the locked directory:
        if (lockNode != null) {
            lock(lockNode.getDir(), true);
        }

        // Otherwise, just unlock with forceReload to reload the entire tree:
        else {
            unlock(true);
        }

        // Re-select the previously selected directory (if any):
        if (currentDir != null) {
            selectAndScrollTo(currentDir);
        }
    }

    /**
     * "Locks" this DirTree to the given directory, meaning that only subdirectories
     * of the given directory will be shown. This is similar to chrooting.
     * If the given directory does not exist, or is null, this method does nothing.
     *
     * @param newRootDir The new root directory.
     */
    public void lock(File newRootDir) {
        lock(newRootDir, false);
    }

    /**
     * Invoked internally to lock to the specified directory, with an option to force
     * a reload even if already locked to the same directory.
     */
    void lock(File newRootDir, boolean isForceReload) {
        if (newRootDir == null || !newRootDir.exists()) {
            log.warning("DirTree: given rootDir is null or doesn't exist. Ignoring request.");
            return;
        }

        // All of this is just to try to accommodate case-insensitive filesystems:
        String newRootDirCanonicalPath;
        String lockNodeCanonicalPath;
        try {
            newRootDirCanonicalPath = newRootDir.getCanonicalPath();
            lockNodeCanonicalPath = (lockNode == null)
                    ? null
                    : lockNode.getDir().getCanonicalPath();
        }
        catch (IOException ioe) {
            // Fallback to absolute paths... this may fail on case-insensitive filesystems
            // if the paths differ only by case, but it's the best we can do:
            newRootDirCanonicalPath = newRootDir.getAbsolutePath();
            lockNodeCanonicalPath = (lockNode == null)
                    ? null
                    : lockNode.getDir().getAbsolutePath();
            log.log(Level.WARNING, "DirTree.lock(): IOException while getting canonical paths.", ioe);
        }

        // If the new rootDir is the same as the current one, nothing to do, unless we're forcing a reload:
        if (lockNode != null
                && newRootDirCanonicalPath.equals(lockNodeCanonicalPath)
                && !isForceReload) {
            return;
        }

        // Load up the new root node:
        lockNode = new DirTreeNode(newRootDir, showHiddenDirs);
        File[] files = newRootDir.listFiles();
        if (files != null) {
            Arrays.sort(files, Comparator.comparing(o -> o.getName().toLowerCase()));
            for (File file : files) {
                if (file.isDirectory()) {
                    // Skip hidden directories if the flag is set
                    if (file.isHidden() && !showHiddenDirs) {
                        continue;
                    }
                    lockNode.add(new DirTreeNode(file, showHiddenDirs));
                }
            }
        }

        // Create a new tree model based on our new root node:
        DefaultTreeModel treeModel = new DefaultTreeModel(lockNode);
        treeModel.setAsksAllowsChildren(true);
        tree.setModel(treeModel);

        // We are officially locked. Let listeners know:
        fireLockEvent();
    }

    /**
     * Unlocking the DirTree reverts it back to showing all filesystem roots.
     */
    public void unlock() {
        unlock(false);
    }

    /**
     * Invoked internally to unlock the DirTree, with an option to force a reload
     * even if the tree is not currently locked.
     *
     * @param forceReload Reload tree contents even if not locked.
     */
    void unlock(boolean forceReload) {
        // If we're not locked, nothing to do, unless we're forcing a reload:
        if (lockNode == null && !forceReload) {
            return;
        }

        // Make a note of what's currently selected, so we can select it after refreshing the tree:
        DirTreeNode oldNode = currentNode;
        if (oldNode == null) {
            // If nothing's selected, but we're locked, we can use the lock node as the "old" node:
            oldNode = lockNode;
        }

        // Get our list of filesystem roots:
        File[] roots = getFilesystemRoots();

        // If there's only one, force a lock to it (this avoids the annoyance of a fake root with one child):
        if (roots.length == 1) {
            // Don't send a lockEvent!
            notificationsEnabled = false;
            lock(roots[0], forceReload);
            notificationsEnabled = true;
            if (oldNode != null) {
                selectAndScrollTo(oldNode.getDir());
            }
            fireUnlockEvent(); // Technically, we are locked, but from the user's perspective we are unlocked
            return;
        }

        // In unlocked mode with multiple filesystem roots, we use a "fake" root node to contain those roots:
        lockNode = null;
        fakeRootNode = new DirTreeNode(new File(FAKE_ROOT_NAME), showHiddenDirs);
        fakeRootNode.setAllowsChildren(true); // Override DirTreeNode logic for this fake root
        for (File root : roots) {
            fakeRootNode.add(new DirTreeNode(root, showHiddenDirs));
        }

        // Use our new root node to reset the tree model:
        DefaultTreeModel treeModel = new DefaultTreeModel(fakeRootNode);
        treeModel.setAsksAllowsChildren(true);
        tree.setModel(treeModel);

        // Select the old root (if there was one):
        if (oldNode != null) {
            selectAndScrollTo(oldNode.getDir());
        }

        // We are officially unlocked. Let listeners know:
        fireUnlockEvent();
    }

    /**
     * Selects and scrolls to the given directory. Does nothing if the directory doesn't exist.
     *
     * @param dir The directory to select.
     */
    public boolean selectAndScrollTo(File dir) {
        if (dir == null) {
            log.warning("DirTree.selectAndScrollTo(): given dir is null. Ignoring request.");
            return false;
        }
        if (!dir.exists() || !dir.isDirectory()) {
            log.warning("DirTree.selectAndScrollTo(): given dir \"" + dir.getAbsolutePath() + "\""
                                + " doesn't exist, or is not a directory. Ignoring request.");
            return false;
        }

        try {
            DirTreeNode startingNode = null;
            Path targetPath = Paths.get(dir.getAbsolutePath()).toRealPath();

            // If we're locked, make sure the given dir is within the locked tree:
            if (lockNode != null) { // We don't call isLocked() here because of our special "/" case
                Path lockPath = Paths.get(lockNode.getDir().getAbsolutePath()).toRealPath();
                if (!targetPath.startsWith(lockPath)) {
                    log.warning("DirTree.selectAndScrollTo(): given dir is outside locked tree."
                                        + " Ignoring request.");
                    return false;
                }

                // Our "starting node" for the search will be the lock node:
                startingNode = lockNode;
            }

            // If we're not locked, we need to find the starting node among the filesystem roots:
            else {
                for (int i = 0; i < fakeRootNode.getChildCount(); i++) {
                    DirTreeNode candidate = (DirTreeNode)fakeRootNode.getChildAt(i);
                    if (targetPath.startsWith(Paths.get(candidate.getDir().getAbsolutePath()).toRealPath())) {
                        startingNode = candidate;
                        break;
                    }
                }

                // This *should* never happen, but just in case:
                if (startingNode == null) {
                    log.warning("DirTree.selectAndScrollTo(): given dir is outside filesystem roots."
                                        + " Ignoring request.");
                    return false;
                }
            }

            // Now, we can start searching based on our starting node:
            Path treeRootPath = Paths.get(startingNode.getDir().getAbsolutePath()).toRealPath();

            // One more sanity check, just to be sure:
            if (!targetPath.startsWith(treeRootPath)) {
                log.warning("DirTree.selectAndScrollTo(): given dir is outside starting node tree."
                                    + " Ignoring request.");
                return false;
            }

            // Get the relative path from tree root to target
            Path relativePath = treeRootPath.relativize(targetPath);

            // Start from tree root and expand down
            Path currentPath = treeRootPath;
            expandNodeAtPath(currentPath); // Expand the root itself

            // Walk through each component of the relative path
            for (int i = 0; i < relativePath.getNameCount(); i++) {
                currentPath = currentPath.resolve(relativePath.getName(i));
                expandNodeAtPath(currentPath);
            }

            // Now we should be able to find the old root node, unless something went wrong:
            TreePath pathToOldRoot = findPathToNode(new DirTreeNode(dir, showHiddenDirs));
            tree.getSelectionModel().setSelectionPath(pathToOldRoot);
            tree.scrollPathToVisible(pathToOldRoot);
            return true;
        }
        catch (IOException ioe) {
            log.log(Level.SEVERE, "DirTree.selectAndScrollTo(): IOException while resolving paths.", ioe);
            return false;
        }
    }

    /**
     * Given a path, expands the corresponding node in the tree.
     */
    private void expandNodeAtPath(Path path) {
        DirTreeNode tmpNode = new DirTreeNode(path.toFile(), showHiddenDirs);
        TreePath treePath = findPathToNode(tmpNode);
        if (treePath != null) {
            ((DirTreeNode)treePath.getLastPathComponent()).loadChildren();
            tree.expandPath(treePath);
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
        // Our "root" node depends on whether we're locked or not:
        DirTreeNode rootNode = (lockNode != null) ? lockNode : fakeRootNode;

        // The rest of this logic is a brute-force search through the tree:
        File targetDir = dirNode.getDir();
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
        if (!notificationsEnabled) {
            return; // ignored
        }

        for (DirTreeListener listener : new ArrayList<>(listeners)) {
            listener.treeLocked(this, lockNode.getDir());
        }
    }

    /**
     * Used internally to notify listeners that this tree has been unlocked.
     */
    private void fireUnlockEvent() {
        if (!notificationsEnabled) {
            return; // ignored
        }

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
        if (!notificationsEnabled) {
            return; // ignored
        }

        File selectedDir = (node == null) ? null : node.getDir();
        for (DirTreeListener listener : new ArrayList<>(listeners)) {
            listener.selectionChanged(this, selectedDir);
        }
    }

    /**
     * Fired internally to notify listeners that the "show hidden files" setting has changed.
     */
    private void fireHiddenFilesChangedEvent() {
        if (!notificationsEnabled) {
            return; // ignored
        }

        for (DirTreeListener listener : new ArrayList<>(listeners)) {
            listener.showHiddenFilesChanged(this, showHiddenDirs);
        }
    }

    /**
     * Fired internally to notify listeners that the selection in the tree is about to change.
     * Listeners can veto this change by returning false.
     *
     * @param newNode The node that is about to be selected.
     * @return True to allow the selection change, false to veto it.
     */
    private boolean fireSelectionWillChangeEvent(DirTreeNode newNode) {
        if (!notificationsEnabled) {
            return true; // ignored, and assumed allowed
        }

        File newSelectedDir = (newNode == null) ? null : newNode.getDir();
        for (DirTreeListener listener : new ArrayList<>(listeners)) {
            if (!listener.selectionWillChange(this, newSelectedDir)) {
                return false;
            }
        }
        return true;
    }

    /**
     * A custom TreeSelectionModel that intercepts selection changes before they are finalized,
     * and gives us a way to veto the selection change if any of our listeners object to it.
     */
    private class ValidatingTreeSelectionModel extends DefaultTreeSelectionModel {

        @Override
        public void setSelectionPaths(TreePath[] paths) {
            if (paths != null && paths.length > 0 && canChangeSelection(paths[0])) {
                super.setSelectionPaths(paths);
            }
        }

        private boolean canChangeSelection(TreePath newPath) {
            TreePath[] currentPaths = getSelectionPaths();

            // If nothing is currently selected, allow the change:
            if (currentPaths == null || currentPaths.length == 0) {
                return true;
            }

            // If we're somehow selecting the same thing, just allow it:
            if (currentPaths.length == 1 && currentPaths[0].equals(newPath)) {
                return true;
            }

            // Get a DirTreeNode for the new selection:
            DirTreeNode newNode = (DirTreeNode)newPath.getLastPathComponent();

            // Give our listeners a chance to veto the selection change:
            // (For example, if there are unsaved changes in the UI that need to be addressed first)
            return fireSelectionWillChangeEvent(newNode);
        }
    }
}
