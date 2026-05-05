package ca.corbett.extras.dirtree;

import javax.swing.SwingWorker;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A {@link SwingWorker} that loads the children of a {@link DirTreeNode} on a
 * background thread and publishes progress chunks back to the EDT as they are
 * discovered.
 * <p>
 * The worker respects {@link DirTreeOptions} for hidden-file and file-vs-directory
 * filtering. Results are published in sorted order (directories first, then files,
 * each group sorted alphabetically, case-insensitive).
 * <p>
 * Consumers should register a {@link DirTreeLoaderListener} on the {@link DirTree}
 * rather than interacting with this class directly.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class DirTreeLoader extends SwingWorker<List<File>, File> {

    // How many files to collect before publishing an intermediate progress chunk.
    private static final int PUBLISH_CHUNK_SIZE = 20;

    private final DirTree tree;
    private final DirTreeNode node;
    private final DirTreeOptions options;
    private Throwable failureCause;

    /**
     * Creates a loader for the given node.
     *
     * @param tree    the owning {@link DirTree}; used to fire loader events
     * @param node    the node whose children should be loaded
     * @param options the display options controlling filtering
     */
    public DirTreeLoader(DirTree tree, DirTreeNode node, DirTreeOptions options) {
        this.tree = tree;
        this.node = node;
        this.options = options;
    }

    /** Returns the node this loader is working on. */
    public DirTreeNode getNode() {
        return node;
    }

    // -------------------------------------------------------------------------
    // SwingWorker implementation
    // -------------------------------------------------------------------------

    /**
     * Runs on the background thread. Lists and filters the directory, publishing
     * chunks as they accumulate.
     */
    @Override
    protected List<File> doInBackground() throws Exception {
        File dir = node.getFile();
        if (dir == null || !dir.isDirectory()) {
            return Collections.emptyList();
        }

        File[] rawEntries = dir.listFiles();
        if (rawEntries == null) {
            // null means either an I/O error or permission denied
            throw new java.io.IOException("Cannot list directory: " + dir.getAbsolutePath());
        }

        List<File> dirs = new ArrayList<>();
        List<File> files = new ArrayList<>();

        for (File entry : rawEntries) {
            if (isCancelled()) {
                return Collections.emptyList();
            }
            if (!options.isShowHiddenFiles() && entry.isHidden()) {
                continue;
            }
            if (entry.isDirectory()) {
                dirs.add(entry);
            } else if (options.isShowFiles()) {
                files.add(entry);
            }
        }

        // Sort each group alphabetically, case-insensitive.
        Comparator<File> byName = Comparator.comparing(f -> f.getName().toLowerCase());
        dirs.sort(byName);
        files.sort(byName);

        // Publish in chunks so progress events flow to the EDT.
        List<File> ordered = new ArrayList<>(dirs.size() + files.size());
        ordered.addAll(dirs);
        ordered.addAll(files);

        List<File> chunk = new ArrayList<>(PUBLISH_CHUNK_SIZE);
        for (File f : ordered) {
            if (isCancelled()) {
                return Collections.emptyList();
            }
            chunk.add(f);
            if (chunk.size() >= PUBLISH_CHUNK_SIZE) {
                publish(chunk.toArray(new File[0]));
                chunk.clear();
            }
        }
        if (!chunk.isEmpty()) {
            publish(chunk.toArray(new File[0]));
        }

        return ordered;
    }

    /**
     * Called on the EDT with intermediate batches of discovered files.
     * Fires {@link DirTreeEvent.Type#LOAD_PROGRESS} events.
     */
    @Override
    protected void process(List<File> chunks) {
        if (isCancelled()) {
            return;
        }
        // We get individual File objects from publish()
        int count = chunks.size();
        tree.fireLoaderEvent(new DirTreeEvent(tree, node, count));
    }

    /**
     * Called on the EDT when the background work is done.
     * Updates the model and fires the appropriate terminal event.
     */
    @Override
    protected void done() {
        if (isCancelled()) {
            node.setLoadState(DirTreeNode.LoadState.UNLOADED);
            tree.fireLoaderEvent(new DirTreeEvent(DirTreeEvent.Type.LOAD_CANCELLED, tree, node));
            return;
        }

        List<File> results;
        try {
            results = get();
        } catch (Exception e) {
            failureCause = e.getCause() != null ? e.getCause() : e;
            node.setLoadState(DirTreeNode.LoadState.ERROR);
            tree.fireLoaderEvent(new DirTreeEvent(tree, node, failureCause));
            tree.onLoadFailed(node);
            return;
        }

        tree.onLoadComplete(node, results);
        tree.fireLoaderEvent(new DirTreeEvent(DirTreeEvent.Type.LOAD_COMPLETE, tree, node));
    }
}

