package ca.corbett.extras.dirtree;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

/**
 * Represents a dynamic right click menu for DirTree. When invoked, it will give you the
 * option to lock the DirTree to the selected directory. If the DirTree is already locked, 
 * it will also give you the option to unlock it (revert to file system root directory).
 * We also handle selecting a node on right click here, so a separate MouseListener isn't
 * needed for that.
 * 
 * @author scorbo2
 * @since 2017-11-09
 */
public class DirTreePopupMenu extends JPopupMenu {
  
  private final DirTree dirTree;
  private final JMenuItem unlockMenuItem;
  
  
  /**
   * Constructor is private to force factory access.
   */
  private DirTreePopupMenu(DirTree dirTree) {
    this.dirTree = dirTree;
    this.unlockMenuItem = buildUnlockMenuItem();
  }
  
  
  /**
   * Creates an empty popup menu and associates it with the given DirTree. No further
   * configuration is required from the calling code.
   * 
   * @param dirTree The DirTree for which this menu should serve as a popup.
   * @return A DirTreePopupMenu instance.
   */
  public static DirTreePopupMenu createPopup(DirTree dirTree) {
    DirTreePopupMenu menu = new DirTreePopupMenu(dirTree);
    dirTree.getTree().setComponentPopupMenu(menu);
    return menu;
  }
  

  /**
   * Invoked automatically to show this popup. The menu will be dynamically constructed
   * based on the DirTree instance associated with this menu.
   * 
   * @param source The JTree instance that triggered this popu.
   * @param x Mouse x location.
   * @param y Mouse y location.
   */
  @Override
  public void show(Component source, int x, int y) {
    if (! (source instanceof JTree)) {
      return;
    }
    
    // If our DirTree does not allow locking or unlocking, we're done here:
    if (! dirTree.getAllowLock() && ! dirTree.getAllowUnlock()) {
      return;
    }
    
    JTree tree = (JTree)source;
    int selRow = tree.getRowForLocation(x,y);
    TreePath selPath = tree.getPathForLocation(x,y);
    DirTreeNode node = null;
    if (selPath != null) {
      node = (DirTreeNode)selPath.getLastPathComponent();
      tree.setSelectionPath(selPath); 
    }
    if (selRow>-1){
       tree.setSelectionRow(selRow); 
    }

    rebuildMenu(node);    
    super.show(source,x,y);
  }  
  
  
  /**
   * Rebuilds the menu based on the state of the associated DirTree and the 
   * given selected node.
   */
  private void rebuildMenu(DirTreeNode selectedNode) {
    removeAll();
    
    JMenuItem reloadItem = new JMenuItem("Reload tree");
    reloadItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        dirTree.reload(dirTree.getCurrentDir());
      }      
    });
    add(reloadItem);
    
    if (selectedNode != null && dirTree.getAllowLock()) {
      add(buildLockMenuItem(selectedNode));
    }
    
    if (dirTree.isLocked() && dirTree.getAllowUnlock()) {
      add(this.unlockMenuItem);
    }
  }

  
  /**
   * Builds and returns a menu item to invoke lock() on the associated DirTree with the
   * given DirTreeNode.
   * 
   * @param node The DirTreeNode to which the associated DirTree should be locked.
   * @return The configured JMenuItem
   */
  private JMenuItem buildLockMenuItem(DirTreeNode node) {
    JMenuItem item = new JMenuItem("Lock to "+node.getDir().getAbsolutePath());
    item.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        dirTree.lock(node.getDir());
      }
      
    });
    
    return item;
  }
  
  /**
   * Builds and returns a menu item that invokes unlock() on the associated DirTree when clicked.
   * 
   * @return The configured JMenuItem.
   */
  private JMenuItem buildUnlockMenuItem() {
    JMenuItem item = new JMenuItem("Unlock tree");
    item.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        dirTree.unlock();
      }      
    });
    
    return item;
  }
}
