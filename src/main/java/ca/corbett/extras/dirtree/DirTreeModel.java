package ca.corbett.extras.dirtree;

import javax.swing.tree.DefaultTreeModel;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tree model for {@link DirTree}.
 * <p>
 * Wraps a synthetic invisible root node that holds one {@link DirTreeNode}
 * per physical filesystem root (e.g. {@code /} on Linux, {@code C:\} and
 * {@code D:\} on Windows). This allows multi-root systems to work naturally
 * without requiring any special-casing from callers.
 * <p>
 * All mutation methods ({@link #onLoadComplete}, {@link #onLoadFailed},
 * {@link #setOptions}, {@link #refresh}) <strong>must</strong> be called on
 * the EDT.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class DirTreeModel extends DefaultTreeModel {

    /** The owning DirTree — needed so loaders can fire events. */
    private final DirTree tree;

    /** Invisible synthetic root that parents all physical root nodes. */
    private final DirTreeNode syntheticRoot;

    /** Current display options. */
    private DirTreeOptions options;

    /** Active loaders keyed by the node they are loading. */
    private final Map<DirTreeNode, DirTreeLoader> activeLoaders = new HashMap<>();

    /**
     * Queue of (node, callback) pairs for programmatic navigateTo operations.
     * When a load completes, we check whether the next segment needs loading.
     */
    private NavigationRequest pendingNavigation;

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    /**
     * Creates the model with the given physical root directories and options.
     *
     * @param tree    the owning DirTree
     * @param roots   one or more physical filesystem root directories
     * @param options initial display options
     */
    public DirTreeModel(DirTree tree, File[] roots, DirTreeOptions options) {
        super(new DirTreeNode(null, true), true); // synthetic root, asksAllowsChildren=true
        this.tree = tree;
        this.options = options;
        this.syntheticRoot = (DirTreeNode) getRoot();
        // The synthetic root itself must not appear to be a directory needing loading.
        syntheticRoot.setLoadState(DirTreeNode.LoadState.LOADED);
        syntheticRoot.removePlaceholder();

        for (File root : roots) {
            DirTreeNode rootNode = new DirTreeNode(root, true);
            syntheticRoot.add(rootNode);
        }
    }

    // -------------------------------------------------------------------------
    // Options
    // -------------------------------------------------------------------------

    /** Returns the current display options. */
    public DirTreeOptions getOptions() {
        return options;
    }

    /**
     * Applies new display options and reloads the entire tree.
     * Must be called on the EDT.
     *
     * @param options the new options
     */
    public void setOptions(DirTreeOptions options) {
        this.options = options;
        // Cancel any in-progress loads and reset all nodes to UNLOADED.
        cancelAllLoaders();
        resetNode(syntheticRoot);
        nodeStructureChanged(syntheticRoot);
    }

    // -------------------------------------------------------------------------
    // Loading
    // -------------------------------------------------------------------------

    /**
     * Starts a background load for the given node if it is not already loaded
     * or loading. Must be called on the EDT.
     *
     * @param node the node to load
     */
    public void startLoad(DirTreeNode node) {
        if (node.getLoadState() == DirTreeNode.LoadState.LOADED
                || node.getLoadState() == DirTreeNode.LoadState.LOADING) {
            return;
        }
        if (activeLoaders.containsKey(node)) {
            return;
        }

        node.setLoadState(DirTreeNode.LoadState.LOADING);
        DirTreeLoader loader = new DirTreeLoader(tree, node, options);
        activeLoaders.put(node, loader);
        tree.fireLoaderEvent(new DirTreeEvent(DirTreeEvent.Type.LOAD_STARTED, tree, node));
        loader.execute();
    }

    /**
     * Cancels the in-progress load for the given node, if any.
     * Must be called on the EDT.
     *
     * @param node the node whose load should be cancelled
     */
    public void cancelLoad(DirTreeNode node) {
        DirTreeLoader loader = activeLoaders.get(node);
        if (loader != null) {
            loader.cancel(true);
        }
    }

    /**
     * Cancels all in-progress loads.
     */
    public void cancelAllLoaders() {
        for (DirTreeLoader loader : activeLoaders.values()) {
            loader.cancel(true);
        }
        activeLoaders.clear();
    }

    /**
     * Called by {@link DirTreeLoader} on the EDT when a load completes successfully.
     *
     * @param node    the node that was loaded
     * @param results the ordered list of child files
     */
    public void onLoadComplete(DirTreeNode node, List<File> results) {
        activeLoaders.remove(node);
        node.removePlaceholder();

        for (File f : results) {
            boolean isDir = f.isDirectory();
            DirTreeNode child = new DirTreeNode(f, isDir);
            node.add(child);
        }

        node.setLoadState(DirTreeNode.LoadState.LOADED);
        nodeStructureChanged(node);

        // If there is a pending navigation waiting on this node, advance it.
        if (pendingNavigation != null && pendingNavigation.involves(node)) {
            pendingNavigation.advance(node);
        }
    }

    /**
     * Called by {@link DirTreeLoader} on the EDT when a load fails.
     *
     * @param node the node that failed to load
     */
    public void onLoadFailed(DirTreeNode node) {
        activeLoaders.remove(node);
        node.removePlaceholder();
        // Leave the node as ERROR state; it will appear as an empty, expandable directory.
        nodeStructureChanged(node);

        // Abort pending navigation if it involved this node.
        if (pendingNavigation != null && pendingNavigation.involves(node)) {
            pendingNavigation = null;
        }
    }

    // -------------------------------------------------------------------------
    // Programmatic navigation
    // -------------------------------------------------------------------------

    /**
     * Programmatically navigates to the given target directory, loading
     * intermediate nodes as necessary, and selects the node in the tree.
     * <p>
     * If the target path is outside the configured roots, or does not exist,
     * the {@code onResult} callback receives {@code null} and this method
     * returns {@code false}.
     *
     * @param target   the directory to navigate to
     * @param onResult called on the EDT when navigation succeeds (receives the
     *                 target node) or fails (receives {@code null})
     * @return {@code false} if the target is {@code null}, does not exist, or
     *         is not reachable from the current model roots (e.g. when the tree
     *         is locked to a different subtree); {@code true} if navigation was
     *         successfully initiated (note: the callback is still the authoritative
     *         source for whether navigation ultimately succeeds)
     */
    public boolean navigateTo(File target, NavigationCallback onResult) {
        if (target == null || !target.exists()) {
            onResult.onResult(null);
            return false;
        }

        // Build the path from a known root to the target.
        List<File> pathSegments = buildPathSegments(target);
        if (pathSegments == null) {
            onResult.onResult(null);
            return false;
        }

        // Cancel any previously pending navigation.
        pendingNavigation = null;

        pendingNavigation = new NavigationRequest(pathSegments, onResult);
        pendingNavigation.advance(syntheticRoot);
        return true;
    }

    /**
     * Refreshes the given node by discarding its children and reloading.
     * Must be called on the EDT.
     *
     * @param node the node to refresh
     */
    public void refresh(DirTreeNode node) {
        cancelLoad(node);
        node.removePlaceholder();
        node.removeAllChildren();
        node.add(DirTreeNode.LOADING_PLACEHOLDER);
        node.setLoadState(DirTreeNode.LoadState.UNLOADED);
        nodeStructureChanged(node);
        startLoad(node);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Finds the direct child of {@code parent} whose file matches {@code file}.
     *
     * @return the matching child node, or {@code null} if not found
     */
    DirTreeNode findChild(DirTreeNode parent, File file) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            DirTreeNode child = (DirTreeNode) parent.getChildAt(i);
            if (child.isPlaceholder()) continue;
            if (file.equals(child.getFile())) {
                return child;
            }
        }
        return null;
    }

    /**
     * Decomposes {@code target} into an ordered list of path segments starting
     * from the matching physical root node, through each ancestor, ending at
     * the target itself. Returns {@code null} if no configured root covers the
     * target path.
     */
    private List<File> buildPathSegments(File target) {
        // Collect all ancestors of target (including target itself).
        List<File> ancestors = new ArrayList<>();
        File cursor = target.getAbsoluteFile();
        while (cursor != null) {
            ancestors.add(0, cursor);
            cursor = cursor.getParentFile();
        }
        // ancestors[0] is the filesystem root (e.g. "/" or "C:\")

        // Find which physical root node matches ancestors[0].
        for (int i = 0; i < syntheticRoot.getChildCount(); i++) {
            DirTreeNode rootNode = (DirTreeNode) syntheticRoot.getChildAt(i);
            if (rootNode.isPlaceholder()) continue;
            if (rootNode.getFile() != null
                    && rootNode.getFile().getAbsoluteFile().equals(ancestors.get(0))) {
                return ancestors;
            }
        }
        return null; // target is under a root not in the tree
    }

    /**
     * Resets all descendant nodes of {@code node} to UNLOADED, removing their
     * children and re-adding the placeholder. Used when options change.
     */
    private void resetNode(DirTreeNode node) {
        if (node == syntheticRoot) {
            // Reset each physical root child; do NOT reset the synthetic root itself.
            for (int i = 0; i < node.getChildCount(); i++) {
                DirTreeNode child = (DirTreeNode) node.getChildAt(i);
                resetNode(child);
            }
            return;
        }
        node.removeAllChildren();
        if (node.isDirectory()) {
            node.add(DirTreeNode.LOADING_PLACEHOLDER);
            node.setLoadState(DirTreeNode.LoadState.UNLOADED);
        }
    }

    // -------------------------------------------------------------------------
    // Inner types
    // -------------------------------------------------------------------------

    /**
     * Callback interface for the result of a {@link #navigateTo} operation.
     */
    public interface NavigationCallback {
        /**
         * Called on the EDT when navigation concludes.
         *
         * @param targetNode the resolved node, or {@code null} on failure
         */
        void onResult(DirTreeNode targetNode);
    }

    /**
     * Tracks state for an ongoing programmatic navigation, walking down the
     * path segment by segment, triggering loads as needed.
     */
    private class NavigationRequest {

        private final List<File> segments;
        private final NavigationCallback callback;
        private int segmentIndex = 0;

        /**
         * The specific node we issued a {@link #startLoad} for and are now
         * waiting on. {@code null} until the first load is triggered.
         * Used by {@link #involves} to filter irrelevant load-complete callbacks
         * (important on multi-root trees where unrelated roots may finish loading
         * concurrently and must not interfere with the pending navigation).
         */
        private DirTreeNode waitingFor = null;

        NavigationRequest(List<File> segments, NavigationCallback callback) {
            this.segments = segments;
            this.callback = callback;
        }

        /** Returns true if this request is waiting on {@code node} to load. */
        boolean involves(DirTreeNode node) {
            return node == waitingFor;
        }

        /**
         * Called after a node finishes loading; tries to advance to the next segment.
         *
         * @param justLoaded the node that just completed loading
         */
        void advance(DirTreeNode justLoaded) {
            // Find the current segment node inside justLoaded's parent or itself.
            while (segmentIndex < segments.size()) {
                File segmentFile = segments.get(segmentIndex);

                // Is this segment already the justLoaded node?
                if (justLoaded.getFile() != null && justLoaded.getFile().equals(segmentFile)) {
                    segmentIndex++;
                    if (segmentIndex >= segments.size()) {
                        // We've reached the target — select it.
                        pendingNavigation = null;
                        callback.onResult(justLoaded);
                        return;
                    }
                    // Need to descend into the next segment.
                    continue;
                }

                // Look for the segment among the children of justLoaded.
                DirTreeNode child = findChild(justLoaded, segmentFile);
                if (child == null) {
                    // Path segment not found — navigation fails.
                    pendingNavigation = null;
                    callback.onResult(null);
                    return;
                }

                segmentIndex++;
                if (segmentIndex >= segments.size()) {
                    // child IS the target.
                    pendingNavigation = null;
                    callback.onResult(child);
                    return;
                }

                // Need to load child's children to continue.
                if (child.getLoadState() != DirTreeNode.LoadState.LOADED) {
                    waitingFor = child;
                    startLoad(child);
                    // advance() will be called again from onLoadComplete.
                    return;
                }

                // child is already loaded; recurse to continue walking.
                advance(child);
                return;
            }

            // Fell off the end without finding the target.
            pendingNavigation = null;
            callback.onResult(null);
        }
    }
}

