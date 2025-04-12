package ca.corbett.extensions;

import ca.corbett.extras.properties.AbstractProperty;
import ca.corbett.extras.properties.IntegerProperty;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class AppPropertiesTest {

    @Test
    public void reinitialize_withExtensionSuppliedProperties_shouldRemoveThem() throws Exception {
        // GIVEN a setup with some extension-supplied config properties:
        ExtensionManager<AppExtension> extManager = new ExtensionManagerImpl();
        extManager.addExtension(new AppExtensionImpl1("ext1"), true);
        extManager.addExtension(new AppExtensionImpl2("ext2"), true);
        File f = File.createTempFile("blah", ".blah");
        f.deleteOnExit();
        TestAppProperties appProps = new TestAppProperties(f, extManager);
        assertNotNull(appProps.getPropertiesManager().getProperty("General.General.testProperty"));

        // WHEN we remove the extension and reinitialize:
        extManager.unloadExtension(AppExtensionImpl2.class.getName());
        assertNotNull(appProps.getPropertiesManager().getProperty("General.General.testProperty"));
        appProps.reinitialize();

        // THEN we should see the property disappear:
        assertNull(appProps.getPropertiesManager().getProperty("General.General.testProperty"));
    }

    public static class TestAppProperties extends AppProperties<AppExtension> {

        public TestAppProperties(File f, ExtensionManager<AppExtension> extManager) {
            super("Test", f, extManager);
        }

        @Override
        protected List<AbstractProperty> createInternalProperties() {
            return List.of();
        }
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
            return List.of();
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