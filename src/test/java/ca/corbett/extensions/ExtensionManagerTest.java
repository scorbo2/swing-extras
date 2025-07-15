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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        extManager.addExtension(ext1, true);
        assertEquals(1, extManager.getLoadedExtensionCount());
        extManager.addExtension(ext2, false);
        assertEquals(2, extManager.getLoadedExtensionCount());
    }

    @Test
    public void testIsEnabled() {
        assertFalse(extManager.isExtensionEnabled("some.class.that.does.not.exist"));
        extManager.addExtension(ext1, true);
        extManager.addExtension(ext2, false);
        assertTrue(extManager.isExtensionEnabled(ext1.getClass().getName()));
        assertFalse(extManager.isExtensionEnabled(ext2.getClass().getName()));
    }

    @Test
    public void testGetSourceJar() {
        assertNull(extManager.getSourceJar("some.class.that.does.not.exist"));
        extManager.addExtension(ext1, true);
        extManager.addExtension(ext2, false);
        assertNull(extManager.getSourceJar(ext1.getClass().getName()));
        assertNull(extManager.getSourceJar(ext2.getClass().getName()));
    }

    @Test
    public void testGetExtension() {
        assertNull(extManager.getLoadedExtension("some.class.that.does.not.exist"));
        extManager.addExtension(ext1, true);
        extManager.addExtension(ext2, false);
        assertEquals(ext1, extManager.getLoadedExtension(ext1.getClass().getName()));
        assertEquals(ext2, extManager.getLoadedExtension(ext2.getClass().getName()));
    }

    @Test
    public void testGetAllExtensions() {
        assertEquals(0, extManager.getAllLoadedExtensions().size());
        extManager.addExtension(ext1, true);
        extManager.addExtension(ext2, false);
        assertEquals(2, extManager.getAllLoadedExtensions().size());
    }

    @Test
    public void testGetEnabledExtensions() {
        assertEquals(0, extManager.getEnabledLoadedExtensions().size());
        extManager.addExtension(ext1, true);
        extManager.addExtension(ext2, false);
        assertEquals(1, extManager.getEnabledLoadedExtensions().size());
    }

    @Test
    public void testGetAllExtensionProperties() {
        assertEquals(0, extManager.getAllEnabledExtensionProperties().size());
        extManager.addExtension(ext1, true);
        assertEquals(0, extManager.getAllEnabledExtensionProperties().size()); // shouldn't change
        extManager.addExtension(ext2, true);
        assertEquals(1, extManager.getAllEnabledExtensionProperties().size()); // should change
    }

    @Test
    public void testAddDisabledExtensionGetProperties() {
        assertEquals(0, extManager.getAllEnabledExtensionProperties().size());
        extManager.addExtension(ext2, false);
        assertEquals(0, extManager.getAllEnabledExtensionProperties().size()); // shouldn't change
    }

    @Test
    public void testAddDuplicateProperties_shouldFilterDuplicates() {
        // Issue #39 - let's allow extensions to share configuration properties
        assertEquals(0, extManager.getAllEnabledExtensionProperties().size());
        extManager.addExtension(ext2, true);
        assertEquals(1, extManager.getAllEnabledExtensionProperties().size());
        extManager.addExtension(new AppExtensionImpl2WithDuplicateConfigProperty("dupe"), true);
        assertEquals(1, extManager.getAllEnabledExtensionProperties().size()); // shouldn't change
    }

    @Test
    public void testUnloadExtension() {
        extManager.addExtension(ext1, true);
        extManager.addExtension(ext2, true);
        assertTrue(extManager.unloadExtension(ext1.getClass().getName()));
        assertFalse(extManager.unloadExtension(ext1.getClass().getName())); // shouldn't remove twice
        assertEquals(1, extManager.getAllLoadedExtensions().size());
        assertTrue(extManager.unloadExtension(ext2.getClass().getName()));
        assertEquals(0, extManager.getAllLoadedExtensions().size());
    }

    @Test
    public void testUnloadAllExtensions() {
        extManager.addExtension(ext1, true);
        extManager.addExtension(ext2, true);
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
    public void testJarFileMeetsRequirements_withMatchingVersions_shouldSucceed() {
        AppExtensionInfo extInfo = new AppExtensionInfo.Builder("testEqual")
                .setVersion("3.0")
                .setTargetAppName("Test")
                .setTargetAppVersion("3.0")
                .build();

        boolean actual = extManager.jarFileMeetsRequirements(new File("test"), extInfo, "Test", "3.0");

        assertTrue(actual);
    }

    public static class AppExtensionImpl1 implements AppExtension {

        private final String name;

        public AppExtensionImpl1(String name) {
            this.name = name;
        }

        @Override
        public AppExtensionInfo getInfo() {
            return new AppExtensionInfo.Builder("Test")
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
        public List<AbstractProperty> getConfigProperties() {
            return null;
        }

        @Override
        public void onActivate() {
        }

        @Override
        public void onDeactivate() {
        }

        public String getName() {
            return name;
        }
    }

    public static class AppExtensionImpl2 implements AppExtension {

        private final String name;

        public AppExtensionImpl2(String name) {
            this.name = name;
        }

        @Override
        public AppExtensionInfo getInfo() {
            return new AppExtensionInfo.Builder("Test2")
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
        public List<AbstractProperty> getConfigProperties() {
            List<AbstractProperty> list = new ArrayList<>();
            list.add(new IntegerProperty("testProperty", "testProperty", 1));
            return list;
        }

        @Override
        public void onActivate() {

        }

        @Override
        public void onDeactivate() {

        }

        public String getName() {
            return name;
        }
    }

    public static class AppExtensionImpl2WithDuplicateConfigProperty implements AppExtension {

        private final String name;

        public AppExtensionImpl2WithDuplicateConfigProperty(String name) {
            this.name = name;
        }

        @Override
        public AppExtensionInfo getInfo() {
            return new AppExtensionInfo.Builder("Test2 with duplicate config property")
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
        public List<AbstractProperty> getConfigProperties() {
            List<AbstractProperty> list = new ArrayList<>();
            list.add(new IntegerProperty("testProperty", "testProperty", 1));
            return list;
        }

        @Override
        public void onActivate() {

        }

        @Override
        public void onDeactivate() {

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
