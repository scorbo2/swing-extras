package ca.corbett.extras.dirtree;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * A custom {@link JTree} that displays a filesystem as a navigable, lazy-loading
 * directory tree.
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>Cross-platform: works with single-root Linux filesystems and multi-root
 *       Windows drive-letter filesystems.</li>
 *   <li>Lazy loading: directory children are loaded on a background thread the
 *       first time a node is expanded.</li>
 *   <li>Event-driven: segregated listener interfaces for node events
 *       ({@link DirTreeNodeListener}), selection / mouse events
 *       ({@link DirTreeSelectionListener}), and load lifecycle events
 *       ({@link DirTreeLoaderListener}).</li>
 *   <li>Vetoable navigation: {@code nodeWillExpand} / {@code nodeWillCollapse}
 *       listeners can return {@code false} to cancel the action.</li>
 *   <li>Programmatic navigation: {@link #navigateTo(File)} loads and expands
 *       all intermediate nodes to bring a given directory into view and select it.</li>
 *   <li>Loading indicator with a Cancel button shown while a background load is
 *       in progress.</li>
 *   <li>Configurable via {@link DirTreeOptions}: show/hide hidden files, show/hide
 *       regular files.</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * DirTree tree = new DirTree();                       // uses File.listRoots()
 * DirTree tree = new DirTree(new File("/home/user")); // single custom root
 *
 * tree.addDirTreeNodeListener(new DirTreeNodeListener() {
 *     public boolean nodeWillExpand(DirTreeEvent e) {
 *         return true; // allow
 *     }
 * });
 * tree.addDirTreeSelectionListener(e -> System.out.println("Selected: " + e.getFile()));
 * }</pre>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class DirTree extends JTree {

    private static final Logger log = Logger.getLogger(DirTree.class.getName());

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    private DirTreeOptions options;
    private DirTreeModel dirTreeModel;

    /** The node whose background load is currently in progress (if any). */
    private DirTreeNode loadingNode;

    /** The roots currently displayed by the tree. Updated by {@link #applyRoots}. */
    private File[] currentRoots;

    /**
     * The roots saved before a {@link #chroot} / {@link #lock} call.
     * {@code null} when the tree is not locked.
     */
    private File[] savedRoots = null;

    // -------------------------------------------------------------------------
    // Listener lists
    // -------------------------------------------------------------------------

    private final List<DirTreeNodeListener> nodeListeners = new ArrayList<>();
    private final List<DirTreeSelectionListener> selectionListeners = new ArrayList<>();
    private final List<DirTreeLoaderListener> loaderListeners = new ArrayList<>();
    private final List<DirTreeLockListener> lockListeners = new ArrayList<>();

    // -------------------------------------------------------------------------
    // UI sub-components (loading indicator)
    // -------------------------------------------------------------------------

    /**
     * Panel shown at the bottom of the component during background loads.
     * This is <em>not</em> added to this JTree itself (which is not a container
     * in the usual sense) — callers wrap DirTree in a panel via
     * {@link #buildPanel()}, which includes the loading indicator.
     */
    private final JPanel loadingPanel;
    private final JLabel loadingLabel;
    private final JButton cancelButton;

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    /**
     * Creates a DirTree rooted at all physical filesystem roots
     * (equivalent to {@link File#listRoots()}).
     */
    public DirTree() {
        this(File.listRoots());
    }

    /**
     * Creates a DirTree with a single custom root directory.
     *
     * @param root the root directory to display
     */
    public DirTree(File root) {
        this(new File[]{root});
    }

    /**
     * Creates a DirTree with one or more custom root directories.
     *
     * @param roots the root directories to display
     */
    public DirTree(File[] roots) {
        this(roots, new DirTreeOptions.Builder().build());
    }

    /**
     * Full constructor.
     *
     * @param roots   the root directories to display
     * @param options initial display options
     */
    public DirTree(File[] roots, DirTreeOptions options) {
        super();
        this.options = options;
        this.currentRoots = roots;

        // --- Model ---
        dirTreeModel = new DirTreeModel(this, roots, options);
        setModel(dirTreeModel);

        // JTree: hide the invisible synthetic root, show its children as top-level.
        setRootVisible(false);
        setShowsRootHandles(true);

        // --- Loading indicator panel ---
        loadingLabel = new JLabel(" ");
        cancelButton = new JButton("Cancel");
        cancelButton.setMargin(new Insets(1, 6, 1, 6));
        cancelButton.setFocusable(false);
        cancelButton.addActionListener(e -> cancelCurrentLoad());

        loadingPanel = new JPanel(new BorderLayout(4, 0));
        loadingPanel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        loadingPanel.add(loadingLabel, BorderLayout.CENTER);
        loadingPanel.add(cancelButton, BorderLayout.EAST);
        loadingPanel.setVisible(false);

        // --- Swing event wiring ---
        wireTreeWillExpandListener();
        wireTreeSelectionListener();
        wireMouseListener();
    }

    // -------------------------------------------------------------------------
    // Public convenience: wrap in a panel with the loading indicator
    // -------------------------------------------------------------------------

    /**
     * Returns a {@link JPanel} that contains this DirTree (inside a
     * {@link JScrollPane}) and the loading-indicator bar at the bottom.
     * Callers should add the panel to their layout rather than this JTree directly
     * if they want to see the loading indicator.
     *
     * @return a panel suitable for adding to a layout
     */
    public JPanel buildPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(this), BorderLayout.CENTER);
        panel.add(loadingPanel, BorderLayout.SOUTH);
        return panel;
    }

    // -------------------------------------------------------------------------
    // Options
    // -------------------------------------------------------------------------

    /** Returns the current display options. */
    public DirTreeOptions getOptions() {
        return options;
    }

    /**
     * Applies new display options. The tree will be fully reloaded.
     * Must be called on the EDT.
     *
     * @param options the new options
     */
    public void setOptions(DirTreeOptions options) {
        this.options = options;
        dirTreeModel.setOptions(options);
    }

    /**
     * Shorthand way of setting the "show hidden files" option. The tree will be fully reloaded.
     */
    public void setShowHidden(boolean showHidden) {
        // Remember what was selected so we can try to restore it after reload.
        File previousSelection = null;
        TreePath selected = getSelectionPath();
        if (selected != null && selected.getLastPathComponent() instanceof DirTreeNode node) {
            previousSelection = node.getFile();
        }

        // Change the option and reload the tree:
        setOptions(options.toBuilder().showHiddenFiles(showHidden).build());

        // Navigate back to where we were:
        if (previousSelection != null) {
            navigateTo(previousSelection);
        }
    }

    /**
     * Shorthand way of setting the "show regular files" option. The tree will be fully reloaded.
     */
    public void setShowFiles(boolean showFiles) {
        // Remember what was selected so we can try to restore it after reload.
        File previousSelection = null;
        TreePath selected = getSelectionPath();
        if (selected != null && selected.getLastPathComponent() instanceof DirTreeNode node) {
            previousSelection = node.getFile();
        }

        // Change the option and reload the tree:
        setOptions(options.toBuilder().showFiles(showFiles).build());

        // Navigate back to where we were:
        if (previousSelection != null) {
            navigateTo(previousSelection);
        }
    }

    // -------------------------------------------------------------------------
    // Programmatic navigation
    // -------------------------------------------------------------------------

    /**
     * Programmatically navigates to the given directory: loads and expands all
     * intermediate nodes as necessary, then selects and scrolls to the target.
     * <p>
     * This method returns immediately; the actual navigation happens asynchronously
     * on background threads. The result is delivered via {@code onResult}.
     *
     * @param target   the directory to navigate to
     * @param onResult called on the EDT when navigation completes; receives the
     *                 {@link DirTreeNode} for the target, or {@code null} on failure
     * @return {@code false} if the target is {@code null}, does not exist, or lies
     * outside the current model roots (e.g. the tree is locked to a different
     * subtree); {@code true} if navigation was initiated (the callback remains
     * the authoritative source for final success or failure)
     */
    public boolean navigateTo(File target, DirTreeModel.NavigationCallback onResult) {
        return dirTreeModel.navigateTo(target, targetNode -> {
            if (targetNode != null) {
                TreePath path = buildTreePath(targetNode);
                if (path != null) {
                    setSelectionPath(path);
                    scrollPathToVisible(path);
                }
            }
            if (onResult != null) {
                onResult.onResult(targetNode);
            }
        });
    }

    /**
     * Programmatically navigates to the given directory without a result callback.
     *
     * @param target the directory to navigate to
     * @return {@code false} if the target is {@code null}, does not exist, or lies
     *         outside the current model roots; {@code true} if navigation was initiated
     */
    public boolean navigateTo(File target) {
        return navigateTo(target, null);
    }

    /**
     * Refreshes (reloads) the entire tree, discarding all loaded children and
     * re-reading every root from disk.
     * <p>
     * If a node is currently selected, the tree will attempt to re-select it
     * after the refresh completes (the path is navigated to asynchronously).
     * If the previously selected path no longer exists on disk, the selection
     * is simply not restored.
     * Must be called on the EDT.
     */
    public void refresh() {
        // Remember what was selected so we can try to restore it after reload.
        File previousSelection = null;
        TreePath selected = getSelectionPath();
        if (selected != null && selected.getLastPathComponent() instanceof DirTreeNode node) {
            previousSelection = node.getFile();
        }

        DirTreeNode syntheticRoot = (DirTreeNode)dirTreeModel.getRoot();
        for (int i = 0; i < syntheticRoot.getChildCount(); i++) {
            Object child = syntheticRoot.getChildAt(i);
            if (child instanceof DirTreeNode rootNode) {
                dirTreeModel.refresh(rootNode);
            }
        }

        if (previousSelection != null) {
            navigateTo(previousSelection);
        }
    }

    /**
     * Refreshes (reloads) a specific directory node, discarding its loaded
     * children and re-reading them from disk.
     * Must be called on the EDT.
     *
     * @param node the directory node to refresh; if {@code null} or not a
     *             directory, falls back to {@link #refresh()} for the entire tree
     */
    public void refresh(DirTreeNode node) {
        if (node != null && node.isDirectory() && !node.isPlaceholder()) {
            dirTreeModel.refresh(node);
        }
        else {
            refresh();
        }
    }

    /**
     * Refreshes the currently selected directory node. If nothing is selected,
     * or the selection is not a directory, falls back to refreshing the entire tree.
     * Must be called on the EDT.
     */
    public void refreshSelected() {
        TreePath selected = getSelectionPath();
        if (selected != null) {
            Object last = selected.getLastPathComponent();
            if (last instanceof DirTreeNode node && node.isDirectory()) {
                dirTreeModel.refresh(node);
                return;
            }
        }
        refresh();
    }

    // -------------------------------------------------------------------------
    // Chroot / lock / unlock
    // -------------------------------------------------------------------------

    /**
     * Restricts the tree to display only {@code dir} and its descendants.
     * <p>
     * The current root configuration is saved so that a subsequent call to
     * {@link #unlock()} can restore it. If the tree is already locked, the
     * new directory replaces the previous chroot view while the <em>original</em>
     * pre-lock roots are preserved for the eventual {@code unlock()}.
     * <p>
     * If {@code dir} is {@code null}, does not exist, or is not a directory,
     * the operation is silently ignored and the tree is unchanged.
     *
     * @param dir the directory to lock the tree to
     */
    public void chroot(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return;
        }
        // Preserve the original roots only on the first lock.
        if (savedRoots == null) {
            savedRoots = currentRoots;
        }
        applyRoots(new File[]{dir});
        DirTreeNode lockedNode = (DirTreeNode)((DirTreeNode)dirTreeModel.getRoot()).getChildAt(0);
        fireLockEvent(new DirTreeEvent(DirTreeEvent.Type.TREE_LOCKED, this, lockedNode));
    }

    /**
     * Synonym for {@link #chroot(File)}.
     *
     * @param dir the directory to lock the tree to
     */
    public void lock(File dir) {
        chroot(dir);
    }

    /**
     * Restores the tree to the root configuration it had before the most recent
     * {@link #chroot} / {@link #lock} call. If the tree is not currently locked,
     * this method does nothing.
     */
    public void unlock() {
        if (savedRoots == null) {
            return;
        }
        applyRoots(savedRoots);
        savedRoots = null;
        fireLockEvent(new DirTreeEvent(DirTreeEvent.Type.TREE_UNLOCKED, this, null));
    }

    /**
     * Returns {@code true} if the tree is currently locked to a chroot directory.
     *
     * @return true if locked
     */
    public boolean isLocked() {
        return savedRoots != null;
    }

    // -------------------------------------------------------------------------
    // Listener registration
    // -------------------------------------------------------------------------

    /** Adds a {@link DirTreeNodeListener} to receive structural node events. */
    public void addDirTreeNodeListener(DirTreeNodeListener listener) {
        if (listener != null) { nodeListeners.add(listener); }
    }

    /** Removes a previously registered {@link DirTreeNodeListener}. */
    public void removeDirTreeNodeListener(DirTreeNodeListener listener) {
        nodeListeners.remove(listener);
    }

    /** Adds a {@link DirTreeSelectionListener} to receive selection/mouse events. */
    public void addDirTreeSelectionListener(DirTreeSelectionListener listener) {
        if (listener != null) { selectionListeners.add(listener); }
    }

    /** Removes a previously registered {@link DirTreeSelectionListener}. */
    public void removeDirTreeSelectionListener(DirTreeSelectionListener listener) {
        selectionListeners.remove(listener);
    }

    /** Adds a {@link DirTreeLoaderListener} to receive load lifecycle events. */
    public void addDirTreeLoaderListener(DirTreeLoaderListener listener) {
        if (listener != null) { loaderListeners.add(listener); }
    }

    /** Removes a previously registered {@link DirTreeLoaderListener}. */
    public void removeDirTreeLoaderListener(DirTreeLoaderListener listener) {
        loaderListeners.remove(listener);
    }

    /** Adds a {@link DirTreeLockListener} to receive lock/unlock events. */
    public void addDirTreeLockListener(DirTreeLockListener listener) {
        if (listener != null) { lockListeners.add(listener); }
    }

    /** Removes a previously registered {@link DirTreeLockListener}. */
    public void removeDirTreeLockListener(DirTreeLockListener listener) {
        lockListeners.remove(listener);
    }

    // -------------------------------------------------------------------------
    // Event firing (package-private — called from DirTreeLoader)
    // -------------------------------------------------------------------------

    /**
     * Fires a lock lifecycle event to all registered {@link DirTreeLockListener}s.
     * Called on the EDT.
     */
    private void fireLockEvent(DirTreeEvent event) {
        for (DirTreeLockListener l : new ArrayList<>(lockListeners)) {
            switch (event.getType()) {
                case TREE_LOCKED -> l.treeLocked(event);
                case TREE_UNLOCKED -> l.treeUnlocked(event);
                default -> { }
            }
        }
    }

    /**
     * Fires a loader lifecycle event to all registered {@link DirTreeLoaderListener}s.
     * Also manages the visibility of the loading indicator panel.
     * Called on the EDT.
     */
    void fireLoaderEvent(DirTreeEvent event) {
        switch (event.getType()) {
            case LOAD_STARTED -> {
                loadingNode = event.getNode();
                updateLoadingIndicator(true, "Loading " + event.getNode() + "\u2026");
            }
            case LOAD_COMPLETE, LOAD_CANCELLED, LOAD_FAILED -> {
                loadingNode = null;
                updateLoadingIndicator(false, " ");
            }
            default -> { /* LOAD_PROGRESS — indicator already visible */ }
        }

        for (DirTreeLoaderListener l : new ArrayList<>(loaderListeners)) {
            switch (event.getType()) {
                case LOAD_STARTED -> l.loadStarted(event);
                case LOAD_PROGRESS -> l.loadProgress(event);
                case LOAD_COMPLETE -> l.loadComplete(event);
                case LOAD_CANCELLED -> l.loadCancelled(event);
                case LOAD_FAILED -> l.loadFailed(event);
                default -> { }
            }
        }
    }

    /**
     * Called by the model when a load completes; forwards to the model, then
     * fires {@link DirTreeEvent.Type#NODE_EXPANDED} for the node.
     */
    void onLoadComplete(DirTreeNode node, List<File> results) {
        dirTreeModel.onLoadComplete(node, results);
        // If the node is still expanded in the UI, fire nodeExpanded.
        TreePath path = buildTreePath(node);
        if (path != null && isExpanded(path)) {
            DirTreeEvent event = new DirTreeEvent(DirTreeEvent.Type.NODE_EXPANDED, this, node);
            for (DirTreeNodeListener l : new ArrayList<>(nodeListeners)) {
                l.nodeExpanded(event);
            }
        }
    }

    /**
     * Called by the model when a load fails; forwards to the model.
     */
    void onLoadFailed(DirTreeNode node) {
        dirTreeModel.onLoadFailed(node);
    }

    // -------------------------------------------------------------------------
    // Swing event wiring
    // -------------------------------------------------------------------------

    private void wireTreeWillExpandListener() {
        addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                Object last = event.getPath().getLastPathComponent();
                if (!(last instanceof DirTreeNode node)) { return; }
                if (node.isPlaceholder()) { throw new ExpandVetoException(event, "placeholder"); }

                // Fire vetoable nodeWillExpand to all listeners.
                DirTreeEvent dirEvent = new DirTreeEvent(DirTreeEvent.Type.NODE_WILL_EXPAND, DirTree.this, node);
                for (DirTreeNodeListener l : new ArrayList<>(nodeListeners)) {
                    if (!l.nodeWillExpand(dirEvent)) {
                        throw new ExpandVetoException(event, "vetoed by listener");
                    }
                }

                // Trigger lazy load if not yet loaded.
                if (node.getLoadState() == DirTreeNode.LoadState.UNLOADED
                        || node.getLoadState() == DirTreeNode.LoadState.ERROR) {
                    dirTreeModel.startLoad(node);
                }
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
                Object last = event.getPath().getLastPathComponent();
                if (!(last instanceof DirTreeNode node)) { return; }
                if (node.isPlaceholder()) { return; }

                DirTreeEvent dirEvent = new DirTreeEvent(DirTreeEvent.Type.NODE_WILL_COLLAPSE, DirTree.this, node);
                for (DirTreeNodeListener l : new ArrayList<>(nodeListeners)) {
                    if (!l.nodeWillCollapse(dirEvent)) {
                        throw new ExpandVetoException(event, "vetoed by listener");
                    }
                }
            }
        });

        // Informational post-collapse event.
        addTreeExpansionListener(new javax.swing.event.TreeExpansionListener() {
            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                // NODE_EXPANDED is fired from onLoadComplete to ensure children are present.
                // If already loaded, fire it directly here.
                Object last = event.getPath().getLastPathComponent();
                if (!(last instanceof DirTreeNode node)) { return; }
                if (node.getLoadState() == DirTreeNode.LoadState.LOADED) {
                    DirTreeEvent dirEvent = new DirTreeEvent(DirTreeEvent.Type.NODE_EXPANDED, DirTree.this, node);
                    for (DirTreeNodeListener l : new ArrayList<>(nodeListeners)) {
                        l.nodeExpanded(dirEvent);
                    }
                }
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {
                Object last = event.getPath().getLastPathComponent();
                if (!(last instanceof DirTreeNode node)) { return; }
                DirTreeEvent dirEvent = new DirTreeEvent(DirTreeEvent.Type.NODE_COLLAPSED, DirTree.this, node);
                for (DirTreeNodeListener l : new ArrayList<>(nodeListeners)) {
                    l.nodeCollapsed(dirEvent);
                }
            }
        });
    }

    private void wireTreeSelectionListener() {
        addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                TreePath path = e.getNewLeadSelectionPath();
                if (path == null) {
                    DirTreeEvent dirEvent = new DirTreeEvent(DirTreeEvent.Type.NODE_DESELECTED, DirTree.this, null);
                    for (DirTreeSelectionListener l : new ArrayList<>(selectionListeners)) {
                        l.nodeDeselected(dirEvent);
                    }
                    return;
                }
                Object last = path.getLastPathComponent();
                if (!(last instanceof DirTreeNode node)) { return; }
                if (node.isPlaceholder()) { return; }

                DirTreeEvent dirEvent = new DirTreeEvent(DirTreeEvent.Type.NODE_SELECTED, DirTree.this, node);
                for (DirTreeSelectionListener l : new ArrayList<>(selectionListeners)) {
                    l.nodeSelected(dirEvent);
                }
            }
        });
    }

    private void wireMouseListener() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // JTree's built-in behaviour selects on left-click mousePressed.
                // Mirror that for right-clicks so the selection always reflects
                // the clicked node before any popup-trigger listener fires.
                if (!SwingUtilities.isRightMouseButton(e)) {
                    return;
                }
                int row = getRowForLocation(e.getX(), e.getY());
                if (row < 0) { return; }
                TreePath path = getPathForRow(row);
                if (path == null) { return; }
                Object last = path.getLastPathComponent();
                if (!(last instanceof DirTreeNode node) || node.isPlaceholder()) { return; }
                setSelectionPath(path);
                maybeShowPopup(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                int row = getRowForLocation(e.getX(), e.getY());
                if (row < 0) { return; }
                TreePath path = getPathForRow(row);
                if (path == null) { return; }
                Object last = path.getLastPathComponent();
                if (!(last instanceof DirTreeNode node)) { return; }
                if (node.isPlaceholder()) { return; }

                if (SwingUtilities.isRightMouseButton(e)) {
                    // Selection was already applied in mousePressed; just fire the event.
                    DirTreeEvent dirEvent = new DirTreeEvent(DirTreeEvent.Type.NODE_RIGHT_CLICKED, DirTree.this, node);
                    for (DirTreeSelectionListener l : new ArrayList<>(selectionListeners)) {
                        l.nodeRightClicked(dirEvent);
                    }
                }
                else if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    DirTreeEvent dirEvent = new DirTreeEvent(DirTreeEvent.Type.NODE_DOUBLE_CLICKED, DirTree.this, node);
                    for (DirTreeSelectionListener l : new ArrayList<>(selectionListeners)) {
                        l.nodeDoubleClicked(dirEvent);
                    }
                }
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e) {
                if (!e.isPopupTrigger()) {
                    return;
                }
                TreePath path = getPathForLocation(e.getX(), e.getY());
                DirTreeNode node = (path != null && path.getLastPathComponent() instanceof DirTreeNode n)
                        ? n : null;

                JPopupMenu popup = new JPopupMenu();

                JMenuItem lockItem = new JMenuItem("Lock tree to this directory");
                lockItem.setEnabled(node != null && node.isDirectory() && !node.isPlaceholder());
                lockItem.addActionListener(ae -> {
                    if (node == null) {
                        unlock();
                    }
                    else {
                        chroot(node.getFile());
                    }
                });
                popup.add(lockItem);

                if (isLocked()) {
                    JMenuItem unlockItem = new JMenuItem("Unlock tree");
                    unlockItem.addActionListener(ae -> unlock());
                    popup.add(unlockItem);
                }

                if (node != null && node.isDirectory() && !node.isPlaceholder()) {
                    JMenuItem refreshItem = new JMenuItem("Refresh");
                    refreshItem.addActionListener(ae -> {
                        refresh(node);
                    });
                    popup.add(refreshItem);
                }

                JMenuItem showHiddenItems = new JCheckBoxMenuItem("Show hidden items",
                                                                  getOptions().isShowHiddenFiles());
                showHiddenItems.addActionListener(ev -> setShowHidden(showHiddenItems.isSelected()));
                popup.add(showHiddenItems);

                popup.show(DirTree.this, e.getX(), e.getY());
            }
        });
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Replaces the tree model with a new one built from the given roots.
     * Cancels any in-progress loads first. Must be called on the EDT.
     *
     * @param roots the new filesystem roots to display
     */
    private void applyRoots(File[] roots) {
        dirTreeModel.cancelAllLoaders();
        dirTreeModel = new DirTreeModel(this, roots, options);
        setModel(dirTreeModel);
        currentRoots = roots;
    }

    /**
     * Cancels the currently in-progress background load (if any).
     * Called when the user presses the Cancel button in the loading indicator.
     */
    private void cancelCurrentLoad() {
        if (loadingNode != null) {
            dirTreeModel.cancelLoad(loadingNode);
        }
    }

    /**
     * Shows or hides the loading indicator and updates its label text.
     * Must be called on the EDT.
     */
    private void updateLoadingIndicator(boolean visible, String message) {
        loadingLabel.setText(message);
        loadingPanel.setVisible(visible);
        loadingPanel.revalidate();
    }

    /**
     * Builds a {@link TreePath} from the synthetic root to the given node by
     * walking up the node's parent chain.
     *
     * @param node the target node
     * @return the tree path, or {@code null} if the node is not in the model
     */
    TreePath buildTreePath(DirTreeNode node) {
        List<Object> pathList = new ArrayList<>();
        DirTreeNode cursor = node;
        while (cursor != null) {
            pathList.add(0, cursor);
            Object parent = cursor.getParent();
            cursor = (parent instanceof DirTreeNode p) ? p : null;
        }
        if (pathList.isEmpty()) { return null; }
        return new TreePath(pathList.toArray());
    }
}
