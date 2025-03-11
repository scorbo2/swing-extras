package ca.corbett.extras.io;

import java.io.File;
import java.util.Comparator;

/**
 * A simple Comparator implementation that compares File objects by comparing their
 * absolute paths.
 *
 * @author scorbo2
 * @since 2022-05-10
 */
public final class FileNameComparator implements Comparator<File> {

  @Override
  public int compare(File o1, File o2) {
    return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
  }

}
