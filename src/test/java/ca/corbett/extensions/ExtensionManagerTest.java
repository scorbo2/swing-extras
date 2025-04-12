package ca.corbett.extensions;

import ca.corbett.extras.properties.AbstractProperty;
import ca.corbett.extras.properties.IntegerProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

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

    public static class ExtensionManagerImpl extends ExtensionManager<AppExtension> {
    }

}
