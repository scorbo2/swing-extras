package ca.corbett.updates;

import ca.corbett.extras.io.FileSystemUtil;
import com.google.gson.JsonSyntaxException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

class UpdateManagerTest {

    @Test
    public void newUpdateManager_withValidJson_shouldParse() throws Exception {
        // GIVEN a minimal but valid sources json:
        String json = """
                {
                  "applicationName": "Test",
                  "updateSources": [
                    {
                      "name": "Test source",
                      "versionManifestUrl": "http://www.test.example/blah.json",
                      "publicKeyUrl": "http://www.test.example/public.key"
                    }
                  ]
                }
                """;
        File sourcesFile = File.createTempFile("UpdateManagerTest", "json");
        sourcesFile.deleteOnExit();
        FileSystemUtil.writeStringToFile(json, sourcesFile);

        // WHEN we instantiate an UpdateManager with it:
        UpdateManager manager = new UpdateManager(sourcesFile);

        // THEN we should see expected results:
        assertNotNull(manager);
        assertEquals("Test", manager.getApplicationName());
        assertEquals(1, manager.getUpdateSources().size());
        assertEquals("Test source", manager.getUpdateSources().get(0).getName());
        assertEquals("http://www.test.example/blah.json",
                     manager.getUpdateSources().get(0).getVersionManifestUrl().toString());
        assertEquals("http://www.test.example/public.key",
                     manager.getUpdateSources().get(0).getPublicKeyUrl().toString());
    }

    @Test
    public void newUpdateManager_withInvalidURL_shouldFailToParse() throws Exception {
        // GIVEN a sources json with a bad url in it:
        String json = """
                {
                  "applicationName": "Test",
                  "updateSources": [
                    {
                      "versionManifestUrl": "This is not a url!",
                      "publicKeyUrl": "And neither is this."
                    }
                  ]
                }
                """;
        File sourcesFile = File.createTempFile("UpdateManagerTest", "json");
        sourcesFile.deleteOnExit();
        FileSystemUtil.writeStringToFile(json, sourcesFile);

        // WHEN we try to parse it:
        try {
            new UpdateManager(sourcesFile);
            fail("Expected MalformedURLException but didn't get one!");
        }
        catch (JsonSyntaxException ignored) {
        }
    }

    @Test
    public void addUpdateSource_withSave_shouldUpdateAndSave() throws Exception {
        // GIVEN an initially empty sources json:
        String json = """
                {
                  "applicationName": "Test",
                  "updateSources": [
                  ]
                }
                """;
        File sourcesFile = File.createTempFile("UpdateManagerTest", "json");
        sourcesFile.deleteOnExit();
        FileSystemUtil.writeStringToFile(json, sourcesFile);
        UpdateManager manager = new UpdateManager(sourcesFile);

        // WHEN we add an update source:
        URL versionManifest = new URL("http://www.test.example/manifest.json");
        URL publicKey = new URL("http://www.test.example/public.key");
        manager.addUpdateSource(new UpdateSources.UpdateSource("Test source", versionManifest, publicKey));

        // THEN we should see the json was saved correctly:
//        final String expected = """
//                {
//                  "applicationName": "Test",
//                  "updateSources": [
//                    {
//                      "versionManifestUrl": "http://www.test.example/manifest.json",
//                      "publicKeyUrl": "http://www.test.example/public.key"
//                    }
//                  ]
//                }""";
//        assertEquals(expected, FileSystemUtil.readFileToString(sourcesFile));

        // Probably safer to assert on Java objects instead of raw generated json...
        // whitespace, formatting changes, and even the order of keys are all flexible, after all
        UpdateManager manager2 = new UpdateManager(sourcesFile);
        assertEquals("Test", manager2.getApplicationName());
        assertEquals(1, manager.getUpdateSources().size());
        assertEquals("Test source", manager.getUpdateSources().get(0).getName());
        assertEquals("http://www.test.example/manifest.json",
                     manager.getUpdateSources().get(0).getVersionManifestUrl().toString());
        assertEquals("http://www.test.example/public.key",
                     manager.getUpdateSources().get(0).getPublicKeyUrl().toString());
    }
}