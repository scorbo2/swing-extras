package ca.corbett.extensions;

import ca.corbett.extras.properties.AbstractProperty;
import ca.corbett.extras.properties.IntegerProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExtensionManagerTest {

    private ExtensionManagerImpl extManager;
    private AppExtension ext1;
    private AppExtension ext2;

    public ExtensionManagerTest() {
    }

    @BeforeEach
    public void setUp() {
        extManager = new ExtensionManagerImpl();
        ext1 = new AppExtensionImpl1("test1");
        ext2 = new AppExtensionImpl2("test2");
    }

    @Test
    public void testGetExtensionCount() {
        assertEquals(0, extManager.getLoadedExtensionCount());
        boolean success = extManager.addExtension(ext1, true);
        assertTrue(success);
        assertEquals(1, extManager.getLoadedExtensionCount());
        success = extManager.addExtension(ext2, false);
        assertTrue(success);
        assertEquals(2, extManager.getLoadedExtensionCount());
    }

    @Test
    public void testIsEnabled() {
        assertFalse(extManager.isExtensionEnabled("some.class.that.does.not.exist"));
        boolean success = extManager.addExtension(ext1, true);
        assertTrue(success);
        success = extManager.addExtension(ext2, false);
        assertTrue(success);
        assertTrue(extManager.isExtensionEnabled(ext1.getClass().getName()));
        assertFalse(extManager.isExtensionEnabled(ext2.getClass().getName()));
    }

    @Test
    public void testGetSourceJar() {
        assertNull(extManager.getSourceJar("some.class.that.does.not.exist"));
        boolean success = extManager.addExtension(ext1, true);
        assertTrue(success);
        success = extManager.addExtension(ext2, false);
        assertTrue(success);
        assertNull(extManager.getSourceJar(ext1.getClass().getName()));
        assertNull(extManager.getSourceJar(ext2.getClass().getName()));
    }

    @Test
    public void testGetExtension() {
        assertNull(extManager.getLoadedExtension("some.class.that.does.not.exist"));
        boolean success = extManager.addExtension(ext1, true);
        assertTrue(success);
        success = extManager.addExtension(ext2, false);
        assertTrue(success);
        assertEquals(ext1, extManager.getLoadedExtension(ext1.getClass().getName()));
        assertEquals(ext2, extManager.getLoadedExtension(ext2.getClass().getName()));
    }

    @Test
    public void testGetAllExtensions() {
        assertEquals(0, extManager.getAllLoadedExtensions().size());
        boolean success = extManager.addExtension(ext1, true);
        assertTrue(success);
        success = extManager.addExtension(ext2, false);
        assertTrue(success);
        assertEquals(2, extManager.getAllLoadedExtensions().size());
    }

    @Test
    public void testGetEnabledExtensions() {
        assertEquals(0, extManager.getEnabledLoadedExtensions().size());
        boolean success = extManager.addExtension(ext1, true);
        assertTrue(success);
        success = extManager.addExtension(ext2, false);
        assertTrue(success);
        assertEquals(1, extManager.getEnabledLoadedExtensions().size());
    }

    @Test
    public void testFindExtensionByName() {
        boolean success = extManager.addExtension(ext1, true);
        assertTrue(success);
        success = extManager.addExtension(ext2, false);
        assertTrue(success);
        assertNotNull(extManager.findExtensionByName("test1"));
        assertNotNull(extManager.findExtensionByName("test2"));
        assertNull(extManager.findExtensionByName("test3"));
    }

    @Test
    public void testGetAllExtensionProperties() {
        assertEquals(0, extManager.getAllEnabledExtensionProperties().size());
        boolean success = extManager.addExtension(ext1, true);
        assertTrue(success);
        assertEquals(0, extManager.getAllEnabledExtensionProperties().size()); // shouldn't change
        success = extManager.addExtension(ext2, true);
        assertTrue(success);
        assertEquals(1, extManager.getAllEnabledExtensionProperties().size()); // should change
    }

    @Test
    public void testAddDisabledExtensionGetProperties() {
        assertEquals(0, extManager.getAllEnabledExtensionProperties().size());
        boolean success = extManager.addExtension(ext2, false);
        assertTrue(success);
        assertEquals(0, extManager.getAllEnabledExtensionProperties().size()); // shouldn't change
    }

    @Test
    public void testAddDuplicateProperties_shouldFilterDuplicates() {
        // Issue #39 - let's allow extensions to share configuration properties
        assertEquals(0, extManager.getAllEnabledExtensionProperties().size());
        boolean success = extManager.addExtension(ext2, true);
        assertTrue(success);
        assertEquals(1, extManager.getAllEnabledExtensionProperties().size());
        success = extManager.addExtension(new AppExtensionImpl2WithDuplicateConfigProperty("dupe"), true);
        assertTrue(success);
        assertEquals(1, extManager.getAllEnabledExtensionProperties().size()); // shouldn't change
    }

    @Test
    public void testUnloadExtension() {
        boolean success = extManager.addExtension(ext1, true);
        assertTrue(success);
        success = extManager.addExtension(ext2, true);
        assertTrue(success);
        assertTrue(extManager.unloadExtension(ext1.getClass().getName()));
        assertFalse(extManager.unloadExtension(ext1.getClass().getName())); // shouldn't remove twice
        assertEquals(1, extManager.getAllLoadedExtensions().size());
        assertTrue(extManager.unloadExtension(ext2.getClass().getName()));
        assertEquals(0, extManager.getAllLoadedExtensions().size());
    }

    @Test
    public void testUnloadAllExtensions() {
        boolean success = extManager.addExtension(ext1, true);
        assertTrue(success);
        success = extManager.addExtension(ext2, true);
        assertTrue(success);
        assertEquals(2, extManager.unloadAllExtensions());
        assertEquals(0, extManager.getAllLoadedExtensions().size());
    }

    @Test
    public void testSortExtensionJars_withValidButIncompleteLoadOrderFile_shouldPartiallySort() throws Exception {
        Path tmpDir = Files.createTempDirectory("testSortExtensionJars");
        try {
            Set<File> jarSet = new HashSet<>(5);
            for (int i = 1; i <= 5; i++) {
                File jarFile = new File(tmpDir.toFile(), i + ".jar");
                jarFile.createNewFile();
                jarSet.add(jarFile);
            }

            createLoadOrderFile(new File(tmpDir.toFile(), "ext-load-order.txt"), List.of("2.jar", "5.jar"));

            List<File> sortedJars = extManager.sortExtensionJarSet(tmpDir.toFile(), jarSet);

            assertEquals(5, sortedJars.size());
            assertEquals("2.jar", sortedJars.get(0).getName());
            assertEquals("5.jar", sortedJars.get(1).getName());
            assertEquals("1.jar", sortedJars.get(2).getName());
            assertEquals("3.jar", sortedJars.get(3).getName());
            assertEquals("4.jar", sortedJars.get(4).getName());
        }
        finally {
            deleteDirectory(tmpDir.toFile());
        }
    }

    @Test
    public void testSortExtensionJars_withInvalidLoadOrderFile_shouldIgnore() throws Exception {
        Path tmpDir = Files.createTempDirectory("testSortExtensionJars");
        try {
            Set<File> jarSet = new HashSet<>(5);
            for (int i = 1; i <= 5; i++) {
                File jarFile = new File(tmpDir.toFile(), "floogledyboogledy" + i + ".jar");
                jarFile.createNewFile();
                jarSet.add(jarFile);
            }

            createLoadOrderFile(new File(tmpDir.toFile(), "ext-load-order.txt"), List.of("2.jar", "5.jar"));

            List<File> sortedJars = extManager.sortExtensionJarSet(tmpDir.toFile(), jarSet);

            assertEquals(5, sortedJars.size());
            assertEquals("floogledyboogledy1.jar", sortedJars.get(0).getName());
            assertEquals("floogledyboogledy2.jar", sortedJars.get(1).getName());
            assertEquals("floogledyboogledy3.jar", sortedJars.get(2).getName());
            assertEquals("floogledyboogledy4.jar", sortedJars.get(3).getName());
            assertEquals("floogledyboogledy5.jar", sortedJars.get(4).getName());
        }
        finally {
            deleteDirectory(tmpDir.toFile());
        }
    }

    @Test
    public void testSortExtensionJars_withValidAndCompleteLoadOrderFile_shouldFullySort() throws Exception {
        Path tmpDir = Files.createTempDirectory("testSortExtensionJars");
        try {
            Set<File> jarSet = new HashSet<>(5);
            for (int i = 1; i <= 5; i++) {
                File jarFile = new File(tmpDir.toFile(), i + ".jar");
                jarFile.createNewFile();
                jarSet.add(jarFile);
            }

            createLoadOrderFile(new File(tmpDir.toFile(), "ext-load-order.txt"),
                                List.of("5.jar", "4.jar", "3.jar", "2.jar", "1.jar"));

            List<File> sortedJars = extManager.sortExtensionJarSet(tmpDir.toFile(), jarSet);

            assertEquals(5, sortedJars.size());
            assertEquals("5.jar", sortedJars.get(0).getName());
            assertEquals("4.jar", sortedJars.get(1).getName());
            assertEquals("3.jar", sortedJars.get(2).getName());
            assertEquals("2.jar", sortedJars.get(3).getName());
            assertEquals("1.jar", sortedJars.get(4).getName());
        }
        finally {
            deleteDirectory(tmpDir.toFile());
        }
    }

    @Test
    public void testSortExtensionJars_withMissingLoadOrderFile_shouldNaturalSort() throws Exception {
        Path tmpDir = Files.createTempDirectory("testSortExtensionJars");
        try {
            Set<File> jarSet = new HashSet<>(5);
            for (int i = 1; i <= 5; i++) {
                File jarFile = new File(tmpDir.toFile(), i + ".jar");
                jarFile.createNewFile();
                jarSet.add(jarFile);
            }

            List<File> sortedJars = extManager.sortExtensionJarSet(tmpDir.toFile(), jarSet);

            assertEquals(5, sortedJars.size());
            assertEquals("1.jar", sortedJars.get(0).getName());
            assertEquals("2.jar", sortedJars.get(1).getName());
            assertEquals("3.jar", sortedJars.get(2).getName());
            assertEquals("4.jar", sortedJars.get(3).getName());
            assertEquals("5.jar", sortedJars.get(4).getName());
        }
        finally {
            deleteDirectory(tmpDir.toFile());
        }
    }

    @Test
    public void testJarFileMeetsRequirements_givenOlderVersion_shouldFail() {
        AppExtensionInfo extInfo = new AppExtensionInfo.Builder("testOld")
                .setVersion("1.0")
                .setTargetAppName("Test")
                .setTargetAppVersion("1.0")
                .build();

        boolean actual = extManager.jarFileMeetsRequirements(new File("test"), extInfo, "Test", "2.0");

        assertFalse(actual);
    }

    @Test
    public void testJarFileMeetsRequirements_givenNewerVersion_shouldFail() {
        AppExtensionInfo extInfo = new AppExtensionInfo.Builder("testNew")
                .setVersion("2.0")
                .setTargetAppName("Test")
                .setTargetAppVersion("2.0")
                .build();

        boolean actual = extManager.jarFileMeetsRequirements(new File("test"), extInfo, "Test", "1.0");

        assertFalse(actual);
    }

    @Test
    public void testJarFileMeetsRequirements_withMismatchedAppName_shouldFail() {
        AppExtensionInfo extInfo = new AppExtensionInfo.Builder("testAppName")
                .setVersion("2.0")
                .setTargetAppName("TestApp")
                .setTargetAppVersion("2.0")
                .build();
        boolean actual = extManager.jarFileMeetsRequirements(new File("test"), extInfo, "DifferentApp", "2.0");
        assertFalse(actual);
    }

    @Test
    public void testJarFileMeetsRequirements_withNullAppName_shouldFail() {
        AppExtensionInfo extInfo = new AppExtensionInfo.Builder("testNullAppName")
                .setVersion("2.0")
                .setTargetAppVersion("2.0")
                .build();
        boolean actual = extManager.jarFileMeetsRequirements(new File("test"), extInfo, "Hello", "2.0");
        assertFalse(actual);
    }

    @Test
    public void testJarFileMeetsRequirements_withWrongMinorVersion_shouldStillMatch() {
        // Starting in swing-extras 2.6, an extension with a different minor target app version
        // but the same major version should still be considered compatible.
        AppExtensionInfo extInfo = new AppExtensionInfo.Builder("testMinor")
                .setVersion("2.1")
                .setTargetAppName("Test")
                .setTargetAppVersion("2.1")
                .build();

        boolean actual = extManager.jarFileMeetsRequirements(new File("test"), extInfo, "Test", "2.5");

        assertTrue(actual);
    }

    @Test
    public void testJarFileMeetsRequirements_withMatchingVersions_shouldSucceed() {
        AppExtensionInfo extInfo = new AppExtensionInfo.Builder("testEqual")
                .setVersion("3.0")
                .setTargetAppName("Test")
                .setTargetAppVersion("3.0")
                .build();

        boolean actual = extManager.jarFileMeetsRequirements(new File("test"), extInfo, "Test", "3.0");

        assertTrue(actual);
    }

    @Test
    public void jarFileMeetsRequirements_withNullExtensionInfo_shouldFail() {
        boolean actual = extManager.jarFileMeetsRequirements(new File("test"), null, "Test", "1.0");
        assertFalse(actual);
    }

    @Test
    public void jarFileMeetsRequirements_withNullJarFile_shouldFail() {
        AppExtensionInfo extInfo = new AppExtensionInfo.Builder("testNullJar")
                .setVersion("1.0")
                .setTargetAppName("Test")
                .setTargetAppVersion("1.0")
                .build();
        boolean actual = extManager.jarFileMeetsRequirements(null, extInfo, "Test", "1.0");
        assertFalse(actual);
    }

    @Test
    public void loadExtensions_withNullDirectory_shouldReturnZero() throws Exception {
        int loadedCount = extManager.loadExtensions(null, AppExtension.class, null, null);
        assertEquals(0, loadedCount);
    }

    @Test
    public void loadExtensions_withNonExistentDirectory_shouldReturnZero() throws Exception {
        File nonExistentDir = new File("this_directory_should_not_exist_12345");
        int loadedCount = extManager.loadExtensions(nonExistentDir, AppExtension.class, null, null);
        assertEquals(0, loadedCount);
    }

    @Test
    public void findCandidateExtensionJars_withNullDirectory_shouldReturnEmptySet() throws Exception {
        Map<File, AppExtensionInfo> jarFiles = extManager.findCandidateExtensionJars(null, null, null);
        assertNotNull(jarFiles);
        assertEquals(0, jarFiles.size());
    }

    @Test
    public void findCandidateExtensionJars_withNonExistentDirectory_shouldReturnEmptySet() throws Exception {
        File nonExistentDir = new File("this_directory_should_not_exist_12345");
        Map<File, AppExtensionInfo> jarFiles = extManager.findCandidateExtensionJars(nonExistentDir, null, null);
        assertNotNull(jarFiles);
        assertEquals(0, jarFiles.size());
    }

    @Test
    public void loadExtensionFromJar_withNullJar_file_shouldReturnNull() throws Exception {
        AppExtension ext = extManager.loadExtensionFromJar(null, AppExtension.class);
        assertNull(ext);
    }

    @Test
    public void loadExtensionFromJar_withNonExistentJar_file_shouldReturnNull() throws Exception {
        File nonExistentJar = new File("this_jar_file_should_not_exist_12345.jar");
        AppExtension ext = extManager.loadExtensionFromJar(nonExistentJar, AppExtension.class);
        assertNull(ext);
    }

    @Test
    public void extractExtInfo_withNullJar_file_shouldReturnNull() throws Exception {
        AppExtensionInfo extInfo = extManager.extractExtInfo(null);
        assertNull(extInfo);
    }

    @Test
    public void extractExtInfo_withNonExistentJarFile_shouldReturnNull() throws Exception {
        File nonExistentJar = new File("this_jar_file_should_not_exist_12345.jar");
        AppExtensionInfo extInfo = extManager.extractExtInfo(nonExistentJar);
        assertNull(extInfo);
    }

    @Test
    public void sortExtensionJarSet_withNullDirectory_shouldReturnEmptyList() throws Exception {
        List<File> sortedJars = extManager.sortExtensionJarSet(null, new HashSet<>());
        assertNotNull(sortedJars);
        assertEquals(0, sortedJars.size());
    }

    @Test
    public void sortExtensionJarSet_withNonExistentDirectory_shouldReturnEmptyList() throws Exception {
        File nonExistentDir = new File("this_directory_should_not_exist_12345");
        List<File> sortedJars = extManager.sortExtensionJarSet(nonExistentDir, new HashSet<>());
        assertNotNull(sortedJars);
        assertEquals(0, sortedJars.size());
    }

    @Test
    public void sortExtensionJarSet_withNullJarSet_shouldReturnEmptyList() throws Exception {
        Path tmpDir = Files.createTempDirectory("testSortExtensionJars");
        try {
            List<File> sortedJars = extManager.sortExtensionJarSet(tmpDir.toFile(), null);
            assertNotNull(sortedJars);
            assertEquals(0, sortedJars.size());
        }
        finally {
            deleteDirectory(tmpDir.toFile());
        }
    }

    @Test
    public void sortExtensionJarSet_withEmptyJarSet_shouldReturnEmptyList() throws Exception {
        Path tmpDir = Files.createTempDirectory("testSortExtensionJars");
        try {
            List<File> sortedJars = extManager.sortExtensionJarSet(tmpDir.toFile(), new HashSet<>());
            assertNotNull(sortedJars);
            assertEquals(0, sortedJars.size());
        }
        finally {
            deleteDirectory(tmpDir.toFile());
        }
    }

    @Test
    public void addExtension_withNullExtension_shouldReturnFalse() {
        boolean result = extManager.addExtension(null, true);
        assertFalse(result);
    }

    @Test
    public void addExtension_withInvalidExtension_shouldReturnFalse() {
        boolean result = extManager.addExtension(new AppExtensionImplWithInvalidExtInfo("test"), true);
        assertFalse(result);
    }

    public static class AppExtensionImpl1 extends AppExtension {

        private final String name;

        public AppExtensionImpl1(String name) {
            this.name = name;
        }

        @Override
        public AppExtensionInfo getInfo() {
            return new AppExtensionInfo.Builder(name)
                    .setAuthor("me")
                    .setVersion("1.0")
                    .setTargetAppName("Test app")
                    .setTargetAppVersion("1.0")
                    .setShortDescription("Just a test")
                    .setLongDescription("Just a test of AppExtension")
                    .setReleaseNotes("v1.0 - initial release")
                    .build();
        }

        @Override
        protected void loadJarResources() {
        }

        @Override
        protected List<AbstractProperty> createConfigProperties() {
            return null;
        }

        public String getName() {
            return name;
        }
    }

    public static class AppExtensionImpl2 extends AppExtension {

        private final String name;

        public AppExtensionImpl2(String name) {
            this.name = name;
        }

        @Override
        public AppExtensionInfo getInfo() {
            return new AppExtensionInfo.Builder(name)
                    .setAuthor("me2")
                    .setVersion("1.1")
                    .setTargetAppName("Test app")
                    .setTargetAppVersion("1.1")
                    .setShortDescription("Just a test2")
                    .setLongDescription("Just a test of AppExtension2")
                    .setReleaseNotes("v1.1 - initial release")
                    .build();
        }

        @Override
        protected void loadJarResources() {
        }

        @Override
        protected List<AbstractProperty> createConfigProperties() {
            List<AbstractProperty> list = new ArrayList<>();
            list.add(new IntegerProperty("testProperty", "testProperty", 1));
            return list;
        }

        public String getName() {
            return name;
        }
    }

    public static class AppExtensionImpl2WithDuplicateConfigProperty extends AppExtension {

        private final String name;

        public AppExtensionImpl2WithDuplicateConfigProperty(String name) {
            this.name = name;
        }

        @Override
        public AppExtensionInfo getInfo() {
            return new AppExtensionInfo.Builder(name)
                    .setAuthor("me2")
                    .setVersion("1.1")
                    .setTargetAppName("Test app")
                    .setTargetAppVersion("1.1")
                    .setShortDescription("Just a test2")
                    .setLongDescription("Just a test of AppExtension2 with duplicate config property")
                    .setReleaseNotes("v1.1 - initial release")
                    .build();
        }

        @Override
        protected void loadJarResources() {
        }

        @Override
        protected List<AbstractProperty> createConfigProperties() {
            List<AbstractProperty> list = new ArrayList<>();
            list.add(new IntegerProperty("testProperty", "testProperty", 1));
            return list;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * This one will not load! It is deliberately missing target app name and target app version.
     * This is for testing the failure case for addExtension().
     */
    public static class AppExtensionImplWithInvalidExtInfo extends AppExtension {

        private final String name;
        private final AppExtensionInfo extInfo;

        public AppExtensionImplWithInvalidExtInfo(String name) {
            this.name = name;
            this.extInfo = new AppExtensionInfo.Builder(name)
                    .setAuthor("me")
                    .setVersion("1.0")
                    // Missing target app name and version
                    .setShortDescription("Just a test")
                    .setLongDescription("Just a test of AppExtension with invalid ext info")
                    .setReleaseNotes("v1.0 - initial release")
                    .build();
        }

        @Override
        public AppExtensionInfo getInfo() {
            return extInfo;
        }

        @Override
        protected void loadJarResources() {
        }

        @Override
        protected List<AbstractProperty> createConfigProperties() {
            return null;
        }

        public String getName() {
            return name;
        }
    }

    public static class ExtensionManagerImpl extends ExtensionManager<AppExtension> {
    }

    private boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    private void createLoadOrderFile(File file, List<String> contents) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String line : contents) {
                writer.write(line);
                writer.newLine();
            }
            writer.flush();
        }
    }

}
