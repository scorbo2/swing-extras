package ca.corbett.extras.dirtree;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;

/**
 * A custom listener for the DirTree component that handles lazy loading of child nodes
 * when a node is expanded.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2017-11-09
 */
class DirTreeExpandListener implements TreeWillExpandListener {

    @Override
    public void treeWillExpand(TreeExpansionEvent event) {
        ((DirTreeNode)event.getPath().getLastPathComponent()).loadChildren();
    }

    @Override
    public void treeWillCollapse(TreeExpansionEvent event) {
    }
}
