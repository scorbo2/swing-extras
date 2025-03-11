package ca.corbett.extras.dirtree;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;

/**
 * Listener implementations for the FileTree component.
 * 
 * @author scorbo2
 * @since 2017-11-09
 */
public class DirTreeExpandListener implements TreeWillExpandListener {

  @Override
  public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
    ((DirTreeNode)event.getPath().getLastPathComponent()).loadChildren();
  }

  @Override
  public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
  }
  
}
