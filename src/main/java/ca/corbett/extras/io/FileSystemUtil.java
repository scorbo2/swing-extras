package ca.corbett.extras.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Provides a way to scan, organize, and search through a given file system, looking for
 * specific types of files recursively.
 *
 * @author scorbo2
 * @since 2012-07-28 (originally written for ICE, later generalized for ca.corbett.util)
 */
public final class FileSystemUtil {

  /**
   * Utility classes have no public constructor.
   */
  private FileSystemUtil() {
  }

  /**
   * Shorthand for findFiles without specifying a progress callback.
   * See findFiles(File,boolean,String,FileSearchListener) for details.
   *
   * @param rootDir see findFiles(File,boolean,String,FileSearchListener) for details.
   * @param recursive see findFiles(File,boolean,String,FileSearchListener) for details.
   * @param extension see findFiles(File,boolean,String,FileSearchListener) for details.
   * @return see findFiles(File,boolean,String,FileSearchListener) for details.
   */
  public static List<File> findFiles(final File rootDir,
                                     final boolean recursive,
                                     final String extension) {
    return findFiles(rootDir, recursive, extension, null);
  }

  /**
   * Shorthand for findFiles without specifying a progress callback.
   * See findFiles(File,boolean,List,FileSearchListener) for details.
   *
   * @param rootDir see findFiles(File,boolean,List,FileSearchListener) for details.
   * @param recursive see findFiles(File,boolean,List,FileSearchListener) for details.
   * @param extensions see findFiles(File,boolean,List,FileSearchListener) for details.
   * @return see findFiles(File,boolean,List,FileSearchListener) for details.
   */
  public static List<File> findFiles(final File rootDir,
                                     final boolean recursive,
                                     final List<String> extensions) {
    return findFiles(rootDir, recursive, extensions, null);
  }

  /**
   * Scans the given directory (and optionally all of its subdirectories recursively) looking for
   * files with the given extension. Files matching the extension will be returned, while all
   * other files will be excluded.
   *
   * @param rootDir The root directory for the search. Must exist and be readable.
   * @param recursive Indicates whether to search sub directories also or not.
   * @param extension A file extensions (eg "jpg") to search for.
   * @param listener A FileSearchListener which will receive notice for each file found.
   * @return An array of File objects, one for each file discovered in the search.
   */
  public static List<File> findFiles(final File rootDir,
                                     final boolean recursive,
                                     final String extension,
                                     final FileSearchListener listener) {
    List<String> extensions = new ArrayList<>();
    extensions.add(extension);
    return findFiles(rootDir, recursive, extensions, listener);
  }

  /**
   * Scans the given directory (and optionally all of its subdirectories recursively) looking for
   * files of one of the types specified in the given fileType list. Files matching any of the
   * extensions in that list will be returned, while all other files will be excluded.
   *
   * @param rootDir The root directory for the search. Must exist and be readable.
   * @param recursive Indicates whether to search sub directories also or not.
   * @param extensions A list of file extensions (eg "jpg") to search for.
   * @param listener A FileSearchListener which will receive notice for each file found.
   * @return An array of File objects, one for each file discovered in the search.
   */
  public static List<File> findFiles(final File rootDir,
                                     final boolean recursive,
                                     final List<String> extensions,
                                     final FileSearchListener listener) {
    File[] children = rootDir.listFiles();
    if (children == null) {
      return new ArrayList<>();
    }

    List<File> fileList = new ArrayList<>();
    for (File child : children) {
      if (child.isDirectory() && recursive) {
        fileList.addAll(findFiles(child, true, extensions, listener));
      }
      else if (!child.isDirectory()) {
        String filename = child.getName().toLowerCase();
        for (String ext : extensions) {
          if (filename.endsWith("." + ext.toLowerCase())) {
            if (listener != null) {
              if (!listener.fileFound(child)) {
                break;
              }
            }
            fileList.add(child);
          }
        }
      }
    }

    // Sort and return the response:
    sortFiles(fileList);
    return fileList;
  }

  /**
   * Shorthand for findFilesExcluding without specifying a progress callback.
   * See findFilesExcluding(File,boolean,String,FileSearchListener) for details.
   *
   * @param rootDir see findFilesExcluding(File,boolean,String,FileSearchListener) for details.
   * @param recursive see findFilesExcluding(File,boolean,String,FileSearchListener) for details.
   * @param extension see findFilesExcluding(File,boolean,String,FileSearchListener) for details.
   * @return see findFilesExcluding(File,boolean,String,FileSearchListener) for details.
   */
  public static List<File> findFilesExcluding(final File rootDir,
                                              final boolean recursive,
                                              final String extension) {
    return findFilesExcluding(rootDir, recursive, extension, null);
  }

  /**
   * Shorthand for findFilesExcluding without specifying a progress callback.
   * See findFilesExcluding(File,boolean,List,FileSearchListener) for details.
   *
   * @param rootDir see findFilesExcluding(File,boolean,List,FileSearchListener) for details.
   * @param recursive see findFilesExcluding(File,boolean,List,FileSearchListener) for details.
   * @param extensions see findFilesExcluding(File,boolean,List,FileSearchListener) for details.
   * @return see findFilesExcluding(File,boolean,List,FileSearchListener) for details.
   */
  public static List<File> findFilesExcluding(final File rootDir,
                                              final boolean recursive,
                                              final List<String> extensions) {
    return findFilesExcluding(rootDir, recursive, extensions, null);
  }

  /**
   * Scans the given directory (and optionally all of its subdirectories recursively) looking for
   * any files that do NOT match the given extension. Files that do not match the given
   * extension are returned, while all other files will be excluded.
   *
   * @param rootDir The root directory for the search. Must exist and be readable.
   * @param recursive Indicates whether to search sub directories also or not.
   * @param extension A file extension (eg "jpg") to exclude from the search.
   * @param listener A FileSearchListener which will receive updates as files are found.
   * @return An array of File objects, one for each file that does not match the given extensions.
   */
  public static List<File> findFilesExcluding(final File rootDir,
                                              final boolean recursive,
                                              final String extension,
                                              final FileSearchListener listener) {
    List<String> extensions = new ArrayList<>();
    extensions.add(extension);
    return findFilesExcluding(rootDir, recursive, extensions, listener);
  }

  /**
   * Scans the given directory (and optionally all of its subdirectories recursively) looking for
   * any files that do NOT match one of the given extensions. Files that do not match any of
   * the given extensions are returned, while all other files will be excluded. To find and
   * return ALL files, leave "extensions" empty.
   *
   * @param rootDir The root directory for the search. Must exist and be readable.
   * @param recursive Indicates whether to search sub directories also or not.
   * @param extensions A list of file extensions (eg "jpg") to exclude from the search.
   * @param listener A FileSearchListener which will receive updates as files are found.
   * @return An array of File objects, one for each file that does not match the given extensions.
   */
  public static List<File> findFilesExcluding(final File rootDir,
                                              final boolean recursive,
                                              final List<String> extensions,
                                              final FileSearchListener listener) {
    File[] children = rootDir.listFiles();
    if (children == null) {
      return new ArrayList<>();
    }

    List<File> fileList = new ArrayList<>();
    for (File child : children) {
      if (child.isDirectory() && recursive) {
        fileList.addAll(findFilesExcluding(child, true, extensions));
      }
      else if (!child.isDirectory()) {
        String filename = child.getName().toLowerCase();
        boolean matched = false;
        for (String ext : extensions) {
          if (filename.endsWith("." + ext.toLowerCase())) {
            matched = true;
            break;
          }
        }
        if (!matched) {
          if (listener != null) {
            if (!listener.fileFound(child)) {
              break;
            }
          }
          fileList.add(child);
        }
      }
    }

    // Sort and return the response:
    sortFiles(fileList);
    return fileList;
  }

  /**
   * Shorthand for findFiles without specifying a progress callback.
   * See findFiles(File,boolean,FileSearchListener) for details.
   *
   * @param rootDir see findFiles(File,boolean,FileSearchListener) for details.
   * @param recursive see findFiles(File,boolean,FileSearchListener) for details.
   * @return see findFiles(File,boolean,FileSearchListener) for details.
   */
  public static List<File> findFiles(final File rootDir,
                                     final boolean recursive) {
    return findFiles(rootDir, recursive, (FileSearchListener)null);
  }

  /**
   * Scans the given directory (and optionally all of its subdirectories recursively) and will
   * return a list of all files found.
   *
   * @param rootDir The root directory for the search. Must exist and be readable.
   * @param recursive Indicates whether to search sub directories also or not.
   * @param listener A FileSearchListener that will receive updates about files found.
   * @return A list of File objects, one for each file found.
   */
  public static List<File> findFiles(final File rootDir,
                                     final boolean recursive,
                                     final FileSearchListener listener) {
    return findFilesExcluding(rootDir, recursive, new ArrayList<>(), listener);
  }

  /**
   * Shorthand for findSubdirectories without specifying a progress callback.
   * See findSubdirectories(File,boolean,FileSearchListener) for details.
   *
   * @param rootDir see findSubdirectories(File,boolean,FileSearchListener) for details.
   * @param recursive see findSubdirectories(File,boolean,FileSearchListener) for details.
   * @return see findSubdirectories(File,boolean,FileSearchListener) for details.
   */
  public static List<File> findSubdirectories(final File rootDir,
                                              final boolean recursive) {
    return findSubdirectories(rootDir, recursive, null);
  }

  /**
   * Scans the given directory (and optionally all of its subdirectories recursively) and will
   * return a list of all subdirectories found. The given root directory is NOT included
   * in the results - only subdirectories. The returned list is sorted by full path name.
   *
   * @param rootDir The root directory for the search. Must exist and be readable.
   * @param recursive Indicates whether to search subdirectories also or not.
   * @param listener A FileSearchListener which will receive updates about found dirs.
   * @return A list of directories, sorted by absolute path.
   */
  public static List<File> findSubdirectories(final File rootDir,
                                              final boolean recursive,
                                              final FileSearchListener listener) {
    List<File> fileList = findSubdirectoriesInternal(rootDir, recursive, listener);
    fileList.sort(new FileNameComparator());
    return fileList;
  }

  /**
   * Invoked internally from findSubdirectories to do the actual work recursively.
   * This is pushed to a separate method so we can do sorting once after the recursive
   * search is complete.
   *
   * @param rootDir The root directory for the search. Must exist and be readable.
   * @param recursive Indicates whether to search subdirectories also or not.
   * @param listener A FileSearchListener which will receive updates about found dirs.
   * @return An unsorted list of directories.
   */
  private static List<File> findSubdirectoriesInternal(final File rootDir,
                                                       final boolean recursive,
                                                       final FileSearchListener listener) {
    File[] children = rootDir.listFiles();
    List<File> fileList = new ArrayList<>();
    if (children != null) {
      for (File child : children) {
        if (child.isDirectory()) {
          if (recursive) {
            fileList.addAll(findSubdirectoriesInternal(child, true, listener));
          }
          if (listener != null) {
            if (!listener.fileFound(child)) {
              break;
            }
          }
          fileList.add(child);
        }
      }
    }

    return fileList;
  }

  /**
   * Sorts the given list of File objects by their name, in a non-retarded way (by default,
   * Arrays.sort will sort upper case letters before lower case letters, such that A, B, and C
   * will come before a, b, and c - but this is rarely expected).
   *
   * @param fileList The list of files to sort. Will be sorted in place.
   */
  public static void sortFiles(List<File> fileList) {
    fileList.sort(new Comparator<File>() {
      @Override
      public int compare(File o1, File o2) {
        String name1 = o1.getAbsolutePath().toLowerCase();
        String name2 = o2.getAbsolutePath().toLowerCase();
        return name1.compareTo(name2);
      }

    });
  }

  /**
   * Reads the contents of the given text file into a single String, which will include
   * line delimiters as present in the file. No check on file size or content is done here,
   * so us with caution... larger files may cause problems. By default, we assume
   * a charset of UTF-8 (a pretty safe default), but you can use readFileToString(File,String)
   * to override that.
   * <p>
   * If you prefer to iterate over the lines in the file one by one, use
   * readFileLines() instead.
   *
   * @param in Any text file.
   * @return The contents of the file as one single String including line delimiters.
   * @throws IOException If something goes wrong.
   */
  public static String readFileToString(File in) throws IOException {
    return readFileToString(in, StandardCharsets.UTF_8.name());
  }

  /**
   * Reads the contents of the given text file into a single String using the given Charset.
   * This will include line delimiters as present in the file. No check on file size or
   * content is done here, so us with caution... larger files may cause problems.
   * <p>
   * If you prefer to iterate over the lines in the file one by one, use
   * readFileLines() instead.
   *
   * @param in Any text file.
   * @param charset The name of the charset to use.
   * @return The contents of the file as one single String including line delimiters.
   * @throws IOException If something goes wrong.
   */
  public static String readFileToString(File in, String charset) throws IOException {
    List<String> lines = readFileLines(in, charset);
    StringBuilder sb = new StringBuilder();
    for (String line : lines) {
      sb.append(line);
      sb.append(System.lineSeparator());
    }

    // Remove the last line separator that we added above:
    if (!lines.isEmpty()) {
      if (!lines.get(lines.size() - 1).isEmpty()) { // only if the line had content
        sb.delete(sb.length() - System.lineSeparator().length(), sb.length());
      }
    }

    return sb.toString();
  }

  /**
   * Reads each line of the given input file and returns them in a list, with line
   * delimiters stripped out. No check on file size or content is done here, so use
   * with caution... larger files may cause problems. By default, we assume a charset
   * of UTF-8 (a pretty safe default), but you can use readFileLines(File, String)
   * to override this.
   * <p>
   * If you prefer to grab the file contents as one big String instead of a collection
   * of individual lines, use readFileToString() instead.
   *
   * @param in Any text file.
   * @return A List of lines from the file, with line delimiters stripped out.
   * @throws IOException If something goes wrong.
   */
  public static List<String> readFileLines(File in) throws IOException {
    return readFileLines(in, StandardCharsets.UTF_8.name());
  }

  /**
   * Reads each line of the given input file and returns them in a list, with line
   * delimiters stripped out. No check on file size or content is done here, so use
   * with caution... larger files may cause problems.
   * <p>
   * If you prefer to grab the file contents as one big String instead of a collection
   * of individual lines, use readFileToString() instead.
   *
   * @param in Any text file.
   * @param charset The charset to use.
   * @return A List of lines from the file, with line delimiters stripped out.
   * @throws IOException If something goes wrong.
   */
  public static List<String> readFileLines(File in, String charset) throws IOException {
    return Files.readAllLines(in.toPath(), Charset.forName(charset));
  }

  /**
   * Reads the contents of the given InputStream as one big String and returns it.
   * If the InputStream is considerably large, this operation may consume time and memory
   * in huge quantities. By default, we assume a charsetof UTF-8 (a pretty safe default),
   * but you can use readStreamToString(InputStream, String) to override this.
   *
   * @param in Any InputStream.
   * @return The contents of the InputStream as one long String.
   * @throws IOException If something goes wrong
   */
  public static String readStreamToString(InputStream in) throws IOException {
    return readStreamToString(in, StandardCharsets.UTF_8.name());
  }

  /**
   * Reads the contents of the given InputStream as one big String and returns it.
   * If the InputStream is considerably large, this operation may consume time and memory
   * in huge quantities.
   *
   * @param in Any InputStream.
   * @param charset The Charset to use.
   * @return The contents of the InputStream as one long String.
   * @throws IOException If something goes wrong
   */
  public static String readStreamToString(InputStream in, String charset) throws IOException {
    StringBuilder sb = new StringBuilder();
    try ( BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset))) {
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line);
        sb.append(System.lineSeparator());
      }
      reader.close();
    }
    return sb.toString();
  }

  /**
   * Writes the given String as-is to the given File. By default, we use UTF-8 for the charset,
   * but you can use writeStringToFile(String, File, String) to override this.
   *
   * @param str Any String
   * @param out Any writable File
   * @throws IOException If something goes wrong
   */
  public static void writeStringToFile(String str, File out) throws IOException {
    writeStringToFile(str, out, StandardCharsets.UTF_8.name());
  }

  /**
   * Writes the given String as-is to the given File.
   *
   * @param str Any String
   * @param out Any writable File
   * @param charset The name of the charset to use.
   * @throws IOException If something goes wrong
   */
  public static void writeStringToFile(String str, File out, String charset) throws IOException {
    Files.write(out.toPath(), str.getBytes(charset));
  }

  /**
   * Writes the given lines to the given output file using a default charset of UTF-8 and
   * the System line delimiter.
   *
   * @param lines A List of Strings.
   * @param out Any writable File. Will be overwritten if exists.
   * @throws IOException If something goes wrong.
   */
  public static void writeLinesToFile(List<String> lines, File out) throws IOException {
    writeLineToFile(lines, out, StandardCharsets.UTF_8.name());
  }

  /**
   * Writes the given lines to the given output file using the given charset and
   * the System line delimiter.
   *
   * @param lines A List of Strings.
   * @param out Any writable File. Will be overwritten if exists.
   * @param charset The Charset to use.
   * @throws IOException If something goes wrong.
   */
  public static void writeLineToFile(List<String> lines, File out, String charset) throws IOException {
    Files.write(out.toPath(), lines, Charset.forName(charset));
  }

}
