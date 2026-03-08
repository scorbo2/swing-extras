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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 * Provides a number of handy static utility methods for working with files and directories.
 * Searching for files by extension, reading and writing text files, sanitizing filenames,
 * and more.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2012-07-28 (originally written for ICE, later generalized for ca.corbett.util)
 */
public class FileSystemUtil {

    protected static final Pattern INVALID_CHARS = Pattern.compile("[^a-zA-Z0-9.-]");
    protected static final Pattern LEADING_DOTS = Pattern.compile("^\\.+");
    protected static final Pattern WINDOWS_RESERVED = Pattern.compile(
            "^(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(\\..*)?$",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Protected constructor to allow subclassing for application-specific utility methods
     * while preventing direct instantiation of this utility class.
     */
    protected FileSystemUtil() {
    }

    /**
     * Shorthand for findFiles without specifying a progress callback.
     * See findFiles(File,boolean,String,FileSearchListener) for details.
     *
     * @param rootDir   see findFiles(File,boolean,String,FileSearchListener) for details.
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
     * @param rootDir    see findFiles(File,boolean,List,FileSearchListener) for details.
     * @param recursive  see findFiles(File,boolean,List,FileSearchListener) for details.
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
     * @param rootDir   The root directory for the search. Must exist and be readable.
     * @param recursive Indicates whether to search sub directories also or not.
     * @param extension A file extensions (eg "jpg") to search for.
     * @param listener  A FileSearchListener which will receive notice for each file found.
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
     * @param rootDir    The root directory for the search. Must exist and be readable.
     * @param recursive  Indicates whether to search sub directories also or not.
     * @param extensions A list of file extensions (eg "jpg") to search for.
     * @param listener   A FileSearchListener which will receive notice for each file found.
     * @return An array of File objects, one for each file discovered in the search.
     */
    public static List<File> findFiles(final File rootDir,
                                       final boolean recursive,
                                       final List<String> extensions,
                                       final FileSearchListener listener) {
        // Pre-process extensions once (lowercase + add dots)
        Set<String> extSet = new HashSet<>();
        for (String ext : extensions) {
            extSet.add("." + ext.toLowerCase());
        }

        List<File> result = findFilesRecurse(rootDir, recursive, extSet, listener, false);
        sortFiles(result); // Sort only once at the end
        return result;
    }

    /**
     * Internally invoked as needed from findFiles to recurse through a directory structure.
     */
    protected static List<File> findFilesRecurse(final File rootDir,
                                               final boolean recursive,
                                               final Set<String> extSet,
                                               final FileSearchListener listener,
                                               final boolean invertSearch) {
        File[] children = rootDir.listFiles();
        if (children == null) {
            return new ArrayList<>();
        }

        List<File> fileList = new ArrayList<>();

        for (File child : children) {
            if (child.isDirectory() && recursive) {
                fileList.addAll(findFilesRecurse(child, recursive, extSet, listener, invertSearch));
            }
            else if (child.isFile()) {
                String filename = child.getName().toLowerCase();

                // if any extensions match, it's a hit:
                boolean fileMatched = false;
                for (String ext : extSet) {
                    if (filename.endsWith(ext)) {
                        fileMatched = true;
                        break; // Found match, no need to check other extensions
                    }
                }

                // If the file matched an extension and our search is not inverted, it's a hit:
                // OR if the file did NOT match any extension and our search IS inverted, it's a hit:
                if ((fileMatched && !invertSearch) || (!fileMatched && invertSearch)) {
                    if (listener != null) {
                        if (!listener.fileFound(child)) { // give caller a chance to cancel
                            break;
                        }
                    }
                    fileList.add(child);
                }
            }
        }

        return fileList; // No sorting in helper method
    }

    /**
     * Shorthand for findFilesExcluding without specifying a progress callback.
     * See findFilesExcluding(File,boolean,String,FileSearchListener) for details.
     *
     * @param rootDir   see findFilesExcluding(File,boolean,String,FileSearchListener) for details.
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
     * @param rootDir    see findFilesExcluding(File,boolean,List,FileSearchListener) for details.
     * @param recursive  see findFilesExcluding(File,boolean,List,FileSearchListener) for details.
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
     * @param rootDir   The root directory for the search. Must exist and be readable.
     * @param recursive Indicates whether to search sub directories also or not.
     * @param extension A file extension (eg "jpg") to exclude from the search.
     * @param listener  A FileSearchListener which will receive updates as files are found.
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
     * @param rootDir    The root directory for the search. Must exist and be readable.
     * @param recursive  Indicates whether to search sub directories also or not.
     * @param extensions A list of file extensions (eg "jpg") to exclude from the search.
     * @param listener   A FileSearchListener which will receive updates as files are found.
     * @return An array of File objects, one for each file that does not match the given extensions.
     */
    public static List<File> findFilesExcluding(final File rootDir,
                                                final boolean recursive,
                                                final List<String> extensions,
                                                final FileSearchListener listener) {
        // Pre-process extensions once (lowercase + add dots)
        Set<String> extSet = new HashSet<>();
        for (String ext : extensions) {
            extSet.add("." + ext.toLowerCase());
        }

        List<File> result = findFilesRecurse(rootDir, recursive, extSet, listener, true);
        sortFiles(result); // Sort only once at the end
        return result;
    }

    /**
     * Shorthand for findFiles without specifying a progress callback.
     * See findFiles(File,boolean,FileSearchListener) for details.
     *
     * @param rootDir   see findFiles(File,boolean,FileSearchListener) for details.
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
     * @param rootDir   The root directory for the search. Must exist and be readable.
     * @param recursive Indicates whether to search sub directories also or not.
     * @param listener  A FileSearchListener that will receive updates about files found.
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
     * @param rootDir   see findSubdirectories(File,boolean,FileSearchListener) for details.
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
     * @param rootDir   The root directory for the search. Must exist and be readable.
     * @param recursive Indicates whether to search subdirectories also or not.
     * @param listener  A FileSearchListener which will receive updates about found dirs.
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
     * @param rootDir   The root directory for the search. Must exist and be readable.
     * @param recursive Indicates whether to search subdirectories also or not.
     * @param listener  A FileSearchListener which will receive updates about found dirs.
     * @return An unsorted list of directories.
     */
    protected static List<File> findSubdirectoriesInternal(final File rootDir,
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
     * @param in      Any text file.
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
     * @param in      Any text file.
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
     * @param in      Any InputStream.
     * @param charset The Charset to use.
     * @return The contents of the InputStream as one long String.
     * @throws IOException If something goes wrong
     */
    public static String readStreamToString(InputStream in, String charset) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
            }
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
     * @param str     Any String
     * @param out     Any writable File
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
     * @param out   Any writable File. Will be overwritten if exists.
     * @throws IOException If something goes wrong.
     */
    public static void writeLinesToFile(List<String> lines, File out) throws IOException {
        writeLinesToFile(lines, out, StandardCharsets.UTF_8.name());
    }

    /**
     * Writes the given lines to the given output file using the given charset and
     * the System line delimiter.
     *
     * @param lines   A List of Strings.
     * @param out     Any writable File. Will be overwritten if exists.
     * @param charset The Charset to use.
     * @throws IOException If something goes wrong.
     */
    public static void writeLinesToFile(List<String> lines, File out, String charset) throws IOException {
        Files.write(out.toPath(), lines, Charset.forName(charset));
    }

    /**
     * Given a count of bytes, returns a human-readable String representation of it.
     */
    public static String getPrintableSize(long size) {
        if (size < 1024) { return size + " bytes"; }

        String[] units = {"KB", "MB", "GB", "TB", "PB"};
        int unitIndex = (int)(Math.log(size) / Math.log(1024)) - 1;
        unitIndex = Math.min(unitIndex, units.length - 1);

        double value = size / Math.pow(1024, unitIndex + 1);
        return String.format("%.1f %s", value, units[unitIndex]);
    }

    /**
     * Looks for a small text file of the given name within the given Jar file, and returns its contents
     * as a String if found. If not found, returns null. If the file appears to be binary data, an exception
     * is thrown.
     */
    public static String extractTextFileFromJar(String targetFilename, File jarFile) throws Exception {
        return extractTextFileFromJar(targetFilename, jarFile, StandardCharsets.UTF_8);
    }

    /**
     * Looks for a small text file of the given name within the given Jar file, and returns its contents
     * as a String if found. If not found, returns null. If the file appears to be binary data, an exception
     * is thrown.
     */
    public static String extractTextFileFromJar(String targetFilename, File jarFile, Charset charset) throws Exception {
        if (jarFile == null || !jarFile.exists() || !jarFile.isFile() || !jarFile.canRead()) {
            throw new Exception("Input jar file does not exist or can't be read!");
        }

        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> e = jar.entries();
            while (e.hasMoreElements()) {
                JarEntry je = e.nextElement();
                if (je.isDirectory()) {
                    continue; // skip directories
                }
                if (!je.getName().equals(targetFilename) && !je.getName().endsWith("/" + targetFilename)) {
                    continue; // Name doesn't match
                }

                try (InputStream is = jar.getInputStream(je)) {
                    byte[] bytes = is.readAllBytes();

                    // Check first N bytes for binary indicators
                    int sampleSize = Math.min(512, bytes.length);
                    for (int i = 0; i < sampleSize; i++) {
                        byte b = bytes[i];
                        // Allow printable ASCII, common whitespace, and UTF-8 continuation bytes
                        if (b < 32 && b != '\n' && b != '\r' && b != '\t') {
                            throw new Exception("File to extract appears to be binary data.");
                        }
                    }

                    return new String(bytes, charset);
                }
            }
        }

        // If we get here, we didn't find anything:
        return null;
    }

    /**
     * Given any arbitrary String, returns a sanitized version that is safe to use as a filename
     * on any operating system. If the given input is null or empty, or if the resulting sanitized
     * filename is empty, "unnamed" will be returned.
     * <p>
     * The only allowable characters are alphanumeric characters, dots (.), hyphens (-), and underscores (_).
     * All other characters are replaced with underscores. Leading dots are removed. On Windows,
     * reserved filenames such as "CON" or "AUX" are prefixed with an underscore. The resulting filename
     * is truncated to a maximum of 200 characters.
     * </p>
     */
    public static String sanitizeFilename(String input) {
        return sanitizeFilename(input, "unnamed");
    }

    /**
     * Given any arbitrary String, returns a sanitized version that is safe to use as a filename
     * on any operating system. If the given input is null or empty, or if the resulting sanitized
     * filename is empty, the given defaultName will be returned.
     * <p>
     * The only allowable characters are alphanumeric characters, dots (.), hyphens (-), and underscores (_).
     * All other characters are replaced with underscores. Leading dots are removed. On Windows,
     * reserved filenames such as "CON" or "AUX" are prefixed with an underscore. The resulting filename
     * is truncated to a maximum of 200 characters.
     * </p>
     */
    public static String sanitizeFilename(String input, String defaultName) {
        if (input == null || input.trim().isEmpty()) {
            return defaultName;
        }

        String sanitized = INVALID_CHARS.matcher(input).replaceAll("_");
        sanitized = LEADING_DOTS.matcher(sanitized).replaceFirst("");

        if (WINDOWS_RESERVED.matcher(sanitized).matches()) {
            sanitized = "_" + sanitized;
        }

        if (sanitized.length() > 200) {
            sanitized = sanitized.substring(0, 200);
        }

        return sanitized.isEmpty() ? defaultName : sanitized;
    }

    /**
     * Given a destination directory and a candidate File that we want to copy or move there,
     * this method will check for name conflicts in the destination, and return a File object
     * representing a unique filename within the destination directory. If a file with the original
     * name already exists, a number will be appended to the name (before the extension)
     * to make it unique.
     * <p>
     * Both {@code destinationDir} and {@code candidateFile} must be non-null. If either argument
     * is {@code null}, this method will throw an {@link IllegalArgumentException}.
     * </p>
     *
     * @param destinationDir The directory where we want to place the file. Must exist and must be a directory.
     * @param candidateFile  The File we want to copy or move to the destination directory.
     * @return A File object representing a unique filename within the destination directory.
     */
    public static File getUniqueDestinationFile(File destinationDir, File candidateFile) {
        if (destinationDir == null) {
            throw new IllegalArgumentException("destinationDir must not be null");
        }
        if (candidateFile == null) {
            throw new IllegalArgumentException("candidateFile must not be null");
        }
        if (!destinationDir.exists() || !destinationDir.isDirectory()) {
            throw new IllegalArgumentException("destinationDir must exist, and must be a directory");
        }

        String candidateFileName = candidateFile.getName();
        File destFile = new File(destinationDir, candidateFileName);

        // The easiest check can come first: if we have no conflict, return the original file:
        if (!destFile.exists()) {
            return destFile;
        }

        // Separate the name and extension:
        String nameWithoutExt;
        String ext;
        int dotIndex = candidateFileName.lastIndexOf('.');
        if (dotIndex == -1) {
            // The file has no dot in it, so there is no extension:
            nameWithoutExt = candidateFileName;
            ext = "";
        }
        else if (dotIndex == 0) {
            // On Linux-based systems, it's common to have files that start with a dot and have no extension.
            // In this case, we'll just say the file has no extension:
            nameWithoutExt = candidateFileName;
            ext = "";
        }
        else {
            // The file has at least one dot, so we can separate name and extension:
            nameWithoutExt = candidateFileName.substring(0, dotIndex);
            ext = candidateFileName.substring(dotIndex);
        }

        // Just keep appending a number until we find one that doesn't exist:
        for (int counter = 1; counter < Integer.MAX_VALUE; counter++) {
            String newName = String.format("%s_%d%s", nameWithoutExt, counter, ext);
            destFile = new File(destinationDir, newName);
            if (!destFile.exists()) {
                return destFile;
            }
        }

        // Extremely unlikely, but just in case we hit the limit:
        return new File(destinationDir, nameWithoutExt + System.currentTimeMillis() + ext);
    }
}
