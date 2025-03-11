package ca.corbett.extras.io;

import java.io.File;
import java.util.Comparator;

/**
 * A Comparator implementation for lists of File objects that will sort them according to
 * depth, from shallowest to deepest. Dev note: I pulled this from FileSystemUtil's
 * findSubdirectories() method because it adds a fair bit of performance overhead during
 * large recursive searches, and also because it turns out I like the results from
 * FileNameComparator better anyway. Leaving this here in case it's ever useful.
 *
 * @author scorbo2
 * @since 2022-05-10
 */
public class FilePathDepthComparator implements Comparator<File> {

  private int countSlashes(String name) {
    int count = 0;
    for (char c : name.toCharArray()) {
      if (c == File.separatorChar) {
        count++;
      }
    }
    return count;
  }

  @Override
  public int compare(File o1, File o2) {
    int count1 = countSlashes(o1.getAbsolutePath());
    int count2 = countSlashes(o2.getAbsolutePath());

    // If the depth is the same, sort by full path+name instead:
    if (count1 == count2) {
      return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
    }

    // Otherwise sort by depth:
    return Integer.compare(count1, count2);
  }

}
