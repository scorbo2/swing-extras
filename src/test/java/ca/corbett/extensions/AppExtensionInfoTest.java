package ca.corbett.extensions;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.Writer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AppExtensionInfoTest {

    public AppExtensionInfoTest() {
    }

    @Test
    public void testToJson() {
        AppExtensionInfo info = new AppExtensionInfo.Builder("test")
                .setAuthor("me")
                .setVersion("1.0")
                .setReleaseNotes("release notes")
                .setTargetAppName("Test app")
                .setTargetAppVersion("2.0")
                .setShortDescription("short desc")
                .setLongDescription("long desc")
                .addCustomField("test1", "test1value")
                .build();
        String json = info.toJson();
        assertEquals("{\n"
                             + "  \"name\": \"test\",\n"
                             + "  \"version\": \"1.0\",\n"
                             + "  \"targetAppName\": \"Test app\",\n"
                             + "  \"targetAppVersion\": \"2.0\",\n"
                             + "  \"author\": \"me\",\n"
                             + "  \"releaseNotes\": \"release notes\",\n"
                             + "  \"shortDescription\": \"short desc\",\n"
                             + "  \"longDescription\": \"long desc\",\n"
                             + "  \"customFields\": {\n"
                             + "    \"test1\": \"test1value\"\n"
                             + "  }\n}", json);
    }

    /**
     * Test of fromJson method, of class AppExtensionInfo.
     */
    @Test
    public void testFromJson() {
        AppExtensionInfo info = new AppExtensionInfo.Builder("test")
                .setAuthor("me")
                .setVersion("1.0")
                .setTargetAppName("Test app")
                .setTargetAppVersion("2.0")
                .setShortDescription("short desc")
                .setReleaseNotes("release notes")
                .setLongDescription("long desc")
                .addCustomField("custom1", "custom1value")
                .addCustomField("custom2", "custom2value")
                .build();
        String json = info.toJson();
        AppExtensionInfo info2 = AppExtensionInfo.fromJson(json);
        assertNotNull(info2);
        assertEquals(info.getName(), info2.getName());
        assertEquals(info.getVersion(), info2.getVersion());
        assertEquals(info.getReleaseNotes(), info2.getReleaseNotes());
        assertEquals(info.getTargetAppName(), info2.getTargetAppName());
        assertEquals(info.getTargetAppVersion(), info2.getTargetAppVersion());
        assertEquals(info.getShortDescription(), info2.getShortDescription());
        assertEquals(info.getLongDescription(), info2.getLongDescription());
        assertEquals(info.getCustomFieldValue("custom1"), info2.getCustomFieldValue("custom1"));
        assertEquals(info.getCustomFieldValue("custom2"), info2.getCustomFieldValue("custom2"));
    }

    @Test
    public void testFromStream() throws Exception {
        AppExtensionInfo info = new AppExtensionInfo.Builder("test")
                .setAuthor("me")
                .setVersion("1.0")
                .setTargetAppName("Test app")
                .setTargetAppVersion("2.0")
                .setShortDescription("short desc")
                .setReleaseNotes("release notes")
                .setLongDescription("long desc")
                .addCustomField("custom1", "custom1value")
                .addCustomField("custom2", "custom2value")
                .build();
        String json = info.toJson();
        File tmpFile = File.createTempFile("util", ".json");
        tmpFile.deleteOnExit();
        try (Writer writer = new FileWriter(tmpFile)) {
            writer.write(json);
        }
        AppExtensionInfo info2 = AppExtensionInfo.fromStream(new FileInputStream(tmpFile));
        assertNotNull(info2);
        assertEquals(info, info2);
    }
}
