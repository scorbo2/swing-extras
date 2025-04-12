package ca.corbett.extras.io;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for FileSystemUtil.
 *
 * @author scorbo2
 */
public class FileSystemUtilTest {

    private static File testDir;
    private static File rootDir1;
    private static File rootDir2;
    private static File rootDir3;

    public FileSystemUtilTest() {
    }

    // This should be in a @BeforeAll but strangely that never seems to trigger......
    static {
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
    public void testFindSubdirectories_withLargeDirectoryTree_shouldBePerformant() {
        File rootDir = new File(testDir, "fsperftest");
        rootDir.mkdir();
        for (int i = 0; i < 25; i++) {
            File subDir = new File(rootDir, "subdir" + (i + 1));
            subDir.mkdir();
            for (int j = 0; j < 25; j++) {
                File subsubDir = new File(subDir, "subsubdir" + (j + 1));
                subsubDir.mkdir();
                for (int k = 0; k < 25; k++) {
                    File subsubsubDir = new File(subsubDir, "subsubsubdir" + (k + 1));
                    subsubsubDir.mkdir();
                }
            }
        }
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
        assertEquals(Integer.valueOf(16275), Integer.valueOf(list.size()));
        assertTrue(elapsedTime < 500, "Find recursive executed slowly!");
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

}
