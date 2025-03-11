package ca.corbett.extras.dirtree;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Represents a single directory within our DirTree.
 * 
 * @author scorbo2
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
    return ! hasChildren;
  }
  
  public File getDir() {
    return dir;
  }
  
  public void loadChildren() {
    if (! childrenLoaded) {
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
          File file = new File(dir,fileName);
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
}
