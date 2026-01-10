package ca.corbett.extensions;

import ca.corbett.extras.properties.AbstractProperty;
import ca.corbett.extras.properties.IntegerProperty;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    public void peek_withNonExistentFile_shouldLogWarning() throws Exception {
        // Use our log capturing mechanism to spy on AppProperties:
        TestLogHandler logHandler = new TestLogHandler();
        Logger testLogger = Logger.getLogger(AppProperties.class.getName());
        testLogger.addHandler(logHandler);

        try {
            // GIVEN a non-existent properties file (for example, an application run for the first time):
            File propsFile = File.createTempFile("nonexistent", ".props");
            propsFile.delete();

            // WHEN we try to peek() a value from that file:
            String actual = AppProperties.peek(propsFile, "AnyProperty");

            // THEN we should find a simple warning in the log file and NOT a stack trace:
            assertTrue(logHandler.hasWarningContaining("The properties file does not yet exist"));
            assertFalse(logHandler.hasWarningContaining("Exception"));
            assertEquals("", actual);
        }
        finally {
            // Clean up our log capturing mechanism:
            testLogger.removeHandler(logHandler);
        }
    }

    static class TestLogHandler extends Handler {
        private List<LogRecord> records = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            records.add(record);
        }

        public boolean hasWarningContaining(String message) {
            return records.stream()
                          .anyMatch(r -> r.getLevel() == Level.WARNING &&
                                  r.getMessage().contains(message));
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
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

    public static class AppExtensionImpl1 extends AppExtension {

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
        protected void loadJarResources() {
        }

        @Override
        protected List<AbstractProperty> createConfigProperties() {
            return List.of();
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

    public static class ExtensionManagerImpl extends ExtensionManager<AppExtension> {
    }
}