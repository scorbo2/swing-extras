package ca.corbett.extras.io;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit tests for FileSystemUtil.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class FileSystemUtilTest {

    private static File testDir;
    private static File rootDir1;
    private static File rootDir2;
    private static File rootDir3;

    public FileSystemUtilTest() {
    }

    @BeforeAll
    public static void setup() {
        try {
            File tmpdir = new File(System.getProperty("java.io.tmpdir"));
            testDir = new File(tmpdir.getAbsolutePath() + "/sc-util-io-test");
            if (testDir.exists()) {
                tearDownClass();
            }
            testDir.mkdir();
            rootDir1 = new File(testDir, "rootDir1");
            rootDir2 = new File(testDir, "rootDir2");
            rootDir3 = new File(testDir, "rootDir3");
            rootDir1.mkdir();
            rootDir2.mkdir();
            rootDir3.mkdir();
            File subdir1 = new File(rootDir2, "subDir1");
            File subdir2 = new File(rootDir2, "subDir2");
            subdir1.mkdir();
            subdir2.mkdir();
            File nestedDir1 = new File(rootDir3, "nestedDir1");
            nestedDir1.mkdir();
            File nestedDir2 = new File(nestedDir1, "nestedDir2");
            nestedDir2.mkdir();
            File nestedDir3 = new File(nestedDir2, "nestedDir3");
            nestedDir3.mkdir();

            File dummy = new File(testDir, "test.txt");
            dummy.createNewFile();
            dummy = new File(rootDir1, "test.txt");
            dummy.createNewFile();
            dummy = new File(rootDir2, "test.txt");
            dummy.createNewFile();
            dummy = new File(subdir2, "test.txt");
            dummy.createNewFile();
            dummy = new File(nestedDir2, "test.txt");
            dummy.createNewFile();
        }
        catch (Exception e) {
            System.out.println("Unable to initialize FileSystemUtilTest: " + e.getMessage());
        }
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
        delete(testDir);
    }

    /**
     * If the input is a file, delete it, and if it's a directory,
     * recurse through it and delete it and everything it contains.
     *
     * @param f Either a file or a directory to be deleted.
     */
    static private void delete(File f) {
        if (f == null || !f.exists()) {
            return;
        }
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                delete(c);
            }
        }
        f.delete();
    }

    @Test
    public void testFindFiles_withFileExtensionList_shouldSucceed() {
        List<String> extensions = new ArrayList<>();
        assertEquals(0, FileSystemUtil.findFiles(rootDir1, true, extensions).size());

        extensions.add("txt");
        assertEquals(1, FileSystemUtil.findFiles(rootDir1, true, extensions).size());

        extensions.add("blah");
        assertEquals(1, FileSystemUtil.findFiles(rootDir1, true, extensions).size());
    }

    @Test
    public void testFindFiles_withFileExtensionString_shouldSucceed() {
        assertEquals(1, FileSystemUtil.findFiles(rootDir1, true, "txt").size());
        assertEquals(0, FileSystemUtil.findFiles(rootDir1, true, "blah").size());
    }

    @Test
    public void testFindFilesExcluding_withExtensionList_shouldSucceed() {
        List<String> extensions = new ArrayList<>();
        assertEquals(1, FileSystemUtil.findFilesExcluding(rootDir1, true, extensions).size());

        extensions.add("blah");
        assertEquals(1, FileSystemUtil.findFilesExcluding(rootDir1, true, extensions).size());

        extensions.add("txt");
        assertEquals(0, FileSystemUtil.findFilesExcluding(rootDir1, true, extensions).size());
    }

    @Test
    public void testFindFiles_withRecursion_shouldSucceed() {
        assertEquals(1, FileSystemUtil.findFiles(rootDir1, true).size());
        assertEquals(2, FileSystemUtil.findFiles(rootDir2, true).size());
        assertEquals(1, FileSystemUtil.findFiles(rootDir3, true).size());
    }

    @Test
    public void testFindSubdirectories_withNestedDirs_shouldSucceed() {
        List<File> result = FileSystemUtil.findSubdirectories(testDir, false);
        assertEquals(3, result.size());

        result = FileSystemUtil.findSubdirectories(testDir, true);
        assertEquals(8, result.size());

        assertEquals("rootDir", result.get(0).getName().substring(0, 7));
        assertEquals("nestedDir", result.get(7).getName().substring(0, 9));
    }

    @Test
    public void testFindSubdirectories_withLargeDirectoryTree_shouldBePerformant() throws Exception {
        File rootDir = new File(testDir, "fsperftest");
        createNestedTestDir(rootDir, 15, 15, 15, false);
        // Before:
        // Found 16275 subdirs in 395ms
        // Found 16275 subdirs in 464ms
        //
        // After sorting improvement:
        // Found 16275 subdirs in 427ms
        // Found 16275 subdirs in 423ms
        //
        // After removing countSlashes:
        // Found 16275 subdirs in 419ms
        // Found 16275 subdirs in 514ms
        //
        // After reducing if count:
        // Found 16275 subdirs in 411ms
        // Found 16275 subdirs in 329ms
        //
        // After reducing if count again:
        // Found 16275 subdirs in 337ms
        // Found 16275 subdirs in 352ms
        //
        // After fixing stupid redundancy bug calling wrong method:
        // Found 16275 subdirs in 360ms
        // Found 16275 subdirs in 306ms
        //
        // After reusing FileComparator instead of new()ing each time:
        // Found 16275 subdirs in 321ms
        // Found 16275 subdirs in 348ms
        // Found 16275 subdirs in 309ms
        long startTime = System.currentTimeMillis();
        List<File> list = FileSystemUtil.findSubdirectories(rootDir, true);
        long elapsedTime = System.currentTimeMillis() - startTime;
        assertEquals(Integer.valueOf(3615), Integer.valueOf(list.size()));
        assertTrue(elapsedTime < 750, "Find recursive executed slowly!");
        deleteDirectoryRecursively(rootDir);
    }

    @Test
    public void testFindFiles_withLargeDirectoryTree_shouldBePerformant() throws Exception {
        File rootDir = new File(testDir, "fsperftest");
        createNestedTestDir(rootDir, 18, 10, 10, true);

        // Round 1 results, testing on sclaptop6:
        // Original findFiles method: Enumerated 48828 files in 196ms.
        // findFilesOptimized: Enumerated 48828 files in 163ms.
        // findFilesNIO2: Enumerated 48828 files in 247ms.
        // findFilesWithVisitor: Enumerated 48828 files in 177ms.
        // findFilesParallel: Enumerated 48828 files in 305ms.

        // Round 2 results: Larger dataset!
        // original findFiles method: Enumerated 126843 files in 475ms.
        // findFilesOptimized: Enumerated 126843 files in 397ms.
        // findFilesNIO2: Enumerated 126843 files in 480ms.
        // findFilesWithVisitor: Enumerated 126843 files in 375ms.
        // findFilesParallel: Enumerated 126843 files in 453ms.

        long startTime = System.currentTimeMillis();
        List<File> list = FileSystemUtil.findFiles(rootDir, true, List.of("txt", "blah"));
        long elapsedTime = System.currentTimeMillis() - startTime;
        //System.out.println("Enumerated "+list.size()+" files in " + elapsedTime + "ms.");
        assertEquals(Integer.valueOf(5997), Integer.valueOf(list.size()));
        assertTrue(elapsedTime < 750, "Find files recursive executed slowly!");
        deleteDirectoryRecursively(rootDir);
    }

    @Test
    public void testSortFiles_withMixedUpperAndLower_shouldSortSanely() {
        List<File> fileList = new ArrayList<>();
        fileList.add(new File("A"));
        fileList.add(new File("a"));
        fileList.add(new File("B"));
        fileList.add(new File("b"));

        FileSystemUtil.sortFiles(fileList);
        assertEquals("a", fileList.get(1).getName());
        // original bug was that upper case was being sorted before lower case:
        //   input A,a,B,b would get sorted as A,B,a,b but should be A,a,B,b
    }

    @Test
    public void testReadFileToString_withInputOutput_shouldSucceed() throws Exception {
        File tmpFile = File.createTempFile("util", ".txt");
        tmpFile.deleteOnExit();
        String expected = "Some string\nWith multiple lines\n\nIncluding some longer lines just for fun\n\n\nhello";
        FileSystemUtil.writeStringToFile(expected, tmpFile);
        String actual = FileSystemUtil.readFileToString(tmpFile);
        assertEquals(expected, actual);
    }

    @Test
    public void testReadFileLines_withMultipleLinesAndBlankLines_shouldSucceed() throws Exception {
        File tmpFile = File.createTempFile("util", ".txt");
        tmpFile.deleteOnExit();
        List<String> lines = new ArrayList<>();
        lines.add("line 1");
        lines.add("line 2 is a longer line");
        lines.add("");
        lines.add("line 3 was blank, did you notice?");
        lines.add("");
        FileSystemUtil.writeLinesToFile(lines, tmpFile);
        List<String> actual = FileSystemUtil.readFileLines(tmpFile);
        assertEquals(lines.size(), actual.size());
        for (int i = 0; i < lines.size(); i++) {
            assertEquals(lines.get(i), actual.get(i));
        }
    }

    @Test
    public void getPrintableSize_withVariousSizes_shouldReportNicely() {
        assertEquals("0 bytes", FileSystemUtil.getPrintableSize(0));
        assertEquals("1.0 KB", FileSystemUtil.getPrintableSize(1024));
        assertEquals("1.5 KB", FileSystemUtil.getPrintableSize(1536));
        assertEquals("1.5 MB", FileSystemUtil.getPrintableSize(1024 * 1024 + 499999));
        assertEquals("1.0 GB", FileSystemUtil.getPrintableSize(1024 * 1024 * 1024));
        assertEquals("8192.0 PB", FileSystemUtil.getPrintableSize(Long.MAX_VALUE));
    }

    /**
     * This one only works on my machine, and I don't want to check in a jar file as a test resource
     * just for this purpose. So, this test is disabled by default.
     * TODO find a better home for these "not really a unit test" tests, or delete them. There's 3 of them in total.
     */
    @Disabled
    @Test
    public void extractTextFileFromJar_withValidJar_shouldExtract() throws Exception {
        // GIVEN a valid jar file with some text-based files in it:
        File jarFile = new File("/home/scorbett/Software/sc-releases/extensions/ImageViewer/2.2/ext-iv-ice-2.2.0.jar");

        // WHEN we try to extract some text files:
        String value1 = FileSystemUtil.extractTextFileFromJar("extInfo.json", jarFile);
        String value2 = FileSystemUtil.extractTextFileFromJar("META-INF/MANIFEST.MF", jarFile);
        String value3 = FileSystemUtil.extractTextFileFromJar("does/not/exist", jarFile);

        // THEN we should see expected results:
        assertNotNull(value1);
        assertFalse(value1.isEmpty());
        assertNotNull(value2);
        assertFalse(value2.isEmpty());
        assertNull(value3);
    }

    @Test
    public void extractTextFileFromJar_withInvalidJar_shouldFail() {
        try {
            FileSystemUtil.extractTextFileFromJar("hello.txt", null);
            fail("Expected exception but didn't get one!");
        }
        catch (Exception ignored) {
        }

        try {
            FileSystemUtil.extractTextFileFromJar("hello.txt", new File("/this/file/no/exist"));
            fail("Expected exception but didn't get one!");
        }
        catch (Exception ignored) {
        }

        try {
            File f = File.createTempFile("FileSystemUtilText", ".jar");
            f.deleteOnExit();
            FileSystemUtil.writeStringToFile("This file is not a jar file", f);
            FileSystemUtil.extractTextFileFromJar("hello.txt", f);
            fail("Expected exception but didn't get one!");
        }
        catch (Exception ignored) {
        }
    }

    @Test
    public void sanitizeFilename_withInvalidCharacters_shouldSucceed() {
        String input = "ThisIsA\\/:*?\"<>|Test";
        String expected = "ThisIsA_________Test";
        String actual = FileSystemUtil.sanitizeFilename(input);
        assertEquals(expected, actual);
    }

    @Test
    public void sanitizeFilename_withNullOrEmptyInput_shouldReturnDefaultName() {
        assertEquals("unnamed", FileSystemUtil.sanitizeFilename(""));
        assertEquals("unnamed", FileSystemUtil.sanitizeFilename(null));
        assertEquals("unnamed", FileSystemUtil.sanitizeFilename("     "));

        final String defaultName = "ThisIsTheDefaultFilename.txt";
        assertEquals(defaultName, FileSystemUtil.sanitizeFilename("", defaultName));
        assertEquals(defaultName, FileSystemUtil.sanitizeFilename(null, defaultName));
        assertEquals(defaultName, FileSystemUtil.sanitizeFilename("    ", defaultName));
    }

    @Test
    public void sanitizeFilename_withReservedWindowsFilenames_shouldReturnDefaultName() {
        String[] reservedNames = {
                "CON", "PRN", "AUX", "NUL",
                "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
                "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"
        };
        for (String reservedName : reservedNames) {
            assertEquals("_" + reservedName, FileSystemUtil.sanitizeFilename(reservedName));
            assertEquals("_" + reservedName + ".txt", FileSystemUtil.sanitizeFilename(reservedName + ".txt"));
        }
    }

    @Test
    public void sanitizeFilename_withNewlines_shouldReplaceWithUnderscores() {
        String input = "This_is_a_test\nwith+some\rnewlines\r\nin_it.";
        String expected = "This_is_a_test_with_some_newlines__in_it.";
        String actual = FileSystemUtil.sanitizeFilename(input);
        assertEquals(expected, actual);
    }

    @Test
    public void sanitizeFilename_withLeadingDots_shouldRemove() {
        String input = "......NotAllowed.txt";
        String expected = "NotAllowed.txt";
        String actual = FileSystemUtil.sanitizeFilename(input);
        assertEquals(expected, actual);
    }

    @Test
    public void sanitizeFilename_withVeryLongFilename_shouldTruncate() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            sb.append("a");
        }
        String input = sb + ".txt";
        String actual = FileSystemUtil.sanitizeFilename(input);
        assertEquals(200, actual.length(), "Filename was not truncated properly!");
    }

    @Test
    public void getUniqueDestinationFile_withNoConflicts_ShouldReturnInputFile() throws Exception {
        final File tempDir = new File(System.getProperty("java.io.tmpdir"));

        // GIVEN a candidate file that we want to copy/move to a directory where there are no name conflicts:
        final String expected = "testfile.txt";
        File toDelete = new File(tempDir, expected);
        if (toDelete.exists()) {
            // Make sure it doesn't exist
            if (!toDelete.delete()) {
                fail("Unable to delete existing test file: " + toDelete.getAbsolutePath());
            }
        }

        // WHEN we compute a unique destination file:
        File destinationFile = FileSystemUtil.getUniqueDestinationFile(tempDir, toDelete);

        // THEN we should get back the same file we sent in, because there are no conflicts:
        assertEquals(expected, destinationFile.getName());
    }

    @Test
    public void getUniqueDestinationFile_withFileWithoutExtension_ShouldReturnUniqueFile() throws Exception {
        final File tempDir = new File(System.getProperty("java.io.tmpdir"));

        // GIVEN a candidate File without an extension and a target directory with name conflicts:
        final String baseName = "testfile";
        File toDelete = new File("/some/source/directory", baseName);
        File conflict1 = new File(tempDir, "testfile"); // Oops! This filename is taken.
        File conflict2 = new File(tempDir, "testfile_1"); // Oops! Our fallback filename is also taken.
        if (!conflict1.createNewFile() || !conflict2.createNewFile()) {
            fail("Unable to create test files!");
        }

        // WHEN we compute a unique destination file:
        File destinationFile = FileSystemUtil.getUniqueDestinationFile(tempDir, toDelete);

        // THEN we should get back a uniquely named file with our fallback fallback name:
        assertEquals("testfile_2", destinationFile.getName());

        // Clean up
        if (!conflict1.delete() || !conflict2.delete()) {
            fail("Unable to delete test files!");
        }
    }

    @Test
    public void getUniqueDestinationFile_withExtensionHavingMultipleDots_ShouldReturnUniqueFile() throws Exception {
        final File tempDir = new File(System.getProperty("java.io.tmpdir"));

        // GIVEN a candidate File with multiple dots in the extension and a target directory with name conflicts:
        final String baseName = "testfile.tar.gz";
        File toDelete = new File("/some/source/directory", baseName);
        File conflict1 = new File(tempDir, "testfile.tar.gz"); // Oops! This filename is taken.
        File conflict2 = new File(tempDir, "testfile.tar_1.gz"); // Oops! Our fallback filename is also taken.
        if (!conflict1.createNewFile() || !conflict2.createNewFile()) {
            fail("Unable to create test files!");
        }

        // WHEN we compute a unique destination file:
        File destinationFile = FileSystemUtil.getUniqueDestinationFile(tempDir, toDelete);

        // THEN we should get back a uniquely named file with our fallback fallback name:
        // (yeah, it should probably be "testfile_2.tar.gz" but the code isn't THAT smart)
        assertEquals("testfile.tar_2.gz", destinationFile.getName());

        // Clean up
        if (!conflict1.delete() || !conflict2.delete()) {
            fail("Unable to delete test files!");
        }
    }

    @Test
    public void getUniqueDestinationFile_withHiddenFile_ShouldReturnUniqueFile() throws Exception {
        final File tempDir = new File(System.getProperty("java.io.tmpdir"));

        // GIVEN a candidate hidden File and a target directory with name conflicts:
        final String baseName = ".testfile";
        File toDelete = new File("/some/source/directory", baseName);
        File conflict1 = new File(tempDir, ".testfile"); // Oops! This filename is taken.
        File conflict2 = new File(tempDir, ".testfile_1"); // Oops! Our fallback filename is also taken.
        if (!conflict1.createNewFile() || !conflict2.createNewFile()) {
            fail("Unable to create test files!");
        }

        // WHEN we compute a unique destination file:
        File destinationFile = FileSystemUtil.getUniqueDestinationFile(tempDir, toDelete);

        // THEN we should get back a uniquely named file with our fallback fallback name:
        assertEquals(".testfile_2", destinationFile.getName());

        // Clean up
        if (!conflict1.delete() || !conflict2.delete()) {
            fail("Unable to delete test files!");
        }
    }

    @Test
    public void getUniqueDestinationFile_withConflicts_ShouldReturnUniqueFile() throws Exception {
        final File tempDir = new File(System.getProperty("java.io.tmpdir"));

        // GIVEN a candidate File and a target directory with name conflicts:
        final String baseName = "testfile.txt";
        File toDelete = new File("/some/source/directory", baseName);
        File conflict1 = new File(tempDir, "testfile.txt"); // Oops! This filename is taken.
        File conflict2 = new File(tempDir, "testfile_1.txt"); // Oops! Our fallback filename is also taken.
        if (!conflict1.createNewFile() || !conflict2.createNewFile()) {
            fail("Unable to create test files!");
        }

        // WHEN we compute a unique destination file:
        File destinationFile = FileSystemUtil.getUniqueDestinationFile(tempDir, toDelete);

        // THEN we should get back a uniquely named file with our fallback fallback name:
        assertEquals("testfile_2.txt", destinationFile.getName());

        // Clean up
        if (!conflict1.delete() || !conflict2.delete()) {
            fail("Unable to delete test files!");
        }
    }

    @Test
    public void getUniqueDestinationFile_withNullInputs_ShouldThrowException() {
        final File tempDir = new File(System.getProperty("java.io.tmpdir"));

        // GIVEN a null target directory:
        File toDelete = new File("/some/source/directory", "testfile.txt");
        try {
            FileSystemUtil.getUniqueDestinationFile(null, toDelete);
            fail("Expected exception but didn't get one!");
        }
        catch (IllegalArgumentException ignored) {
        }

        // GIVEN a null candidate file:
        try {
            FileSystemUtil.getUniqueDestinationFile(tempDir, null);
            fail("Expected exception but didn't get one!");
        }
        catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void getUniqueDestinationFile_withNonexistentTargetDir_ShouldThrowException() {
        // GIVEN a target directory that doesn't exist:
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File toDelete = new File("/some/source/directory", "testfile.txt");
        File nonexistentDir = new File(tempDir, "this_directory_should_not_exist_12345");
        try {
            FileSystemUtil.getUniqueDestinationFile(nonexistentDir, toDelete);
            fail("Expected exception but didn't get one!");
        }
        catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void getUniqueDestinationFile_withTargetDirThatIsNotADirectory_ShouldThrowException() throws Exception {
        // GIVEN a target directory that is actually a file:
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File toDelete = new File("/some/source/directory", "testfile.txt");
        File notADir = File.createTempFile("not_a_dir", ".txt", tempDir);
        try {
            FileSystemUtil.getUniqueDestinationFile(notADir, toDelete);
            fail("Expected exception but didn't get one!");
        }
        catch (IllegalArgumentException ignored) {
        }
        finally {
            if (notADir.exists()) {
                notADir.delete();
            }
        }
    }

    @Test
    public void getUniqueDestinationFile_withFilenameWithTrailingDot_shouldHandleProperly() throws Exception {
        final File tempDir = new File(System.getProperty("java.io.tmpdir"));

        // GIVEN a candidate File with a trailing dot in the name and a target directory with name conflicts:
        final String baseName = "testfile.";
        File toDelete = new File("/some/source/directory", baseName);
        File conflict1 = new File(tempDir, "testfile."); // Oops! This filename is taken.
        File conflict2 = new File(tempDir, "testfile_1."); // Oops! Our fallback filename is also taken.
        if (!conflict1.createNewFile() || !conflict2.createNewFile()) {
            fail("Unable to create test files!");
        }

        // WHEN we compute a unique destination file:
        File destinationFile = FileSystemUtil.getUniqueDestinationFile(tempDir, toDelete);

        // THEN we should get back a uniquely named file with our fallback fallback name:
        assertEquals("testfile_2.", destinationFile.getName());

        // Clean up
        if (!conflict1.delete() || !conflict2.delete()) {
            fail("Unable to delete test files!");
        }
    }

    private static void createNestedTestDir(File rootDir, int dirCount1, int dirCount2, int dirCount3, boolean createFiles)
            throws IOException {
        rootDir.mkdir();
        for (int i = 0; i < dirCount1; i++) {
            if (createFiles) {
                new File(rootDir, "test1.txt").createNewFile();
                new File(rootDir, "test2.txt").createNewFile();
                new File(rootDir, "test3.blah").createNewFile();
            }
            File subDir = new File(rootDir, "subdir" + (i + 1));
            subDir.mkdir();
            if (createFiles) {
                new File(subDir, "test1.txt").createNewFile();
                new File(subDir, "test2.txt").createNewFile();
                new File(subDir, "test3.blah").createNewFile();
            }
            for (int j = 0; j < dirCount2; j++) {
                File subsubDir = new File(subDir, "subsubdir" + (j + 1));
                subsubDir.mkdir();
                if (createFiles) {
                    new File(subsubDir, "test1.txt").createNewFile();
                    new File(subsubDir, "test2.txt").createNewFile();
                    new File(subsubDir, "test3.blah").createNewFile();
                }
                for (int k = 0; k < dirCount3; k++) {
                    File subsubsubDir = new File(subsubDir, "subsubsubdir" + (k + 1));
                    subsubsubDir.mkdir();
                    if (createFiles) {
                        new File(subsubsubDir, "test1.txt").createNewFile();
                        new File(subsubsubDir, "test2.txt").createNewFile();
                        new File(subsubsubDir, "test3.blah").createNewFile();
                    }
                }
            }
        }
    }

    private static void deleteDirectoryRecursively(File rootDir) throws IOException {
        Path path = rootDir.toPath();
        if (Files.exists(path)) {
            Files.walk(path)
                 .sorted(Comparator.reverseOrder())
                 .forEach(p -> {
                     try {
                         Files.delete(p);
                     }
                     catch (IOException e) {
                         throw new RuntimeException(e);
                     }
                 });
        }
    }
}
