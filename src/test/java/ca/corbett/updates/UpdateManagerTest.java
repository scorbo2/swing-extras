package ca.corbett.updates;

import ca.corbett.extras.io.FileSystemUtil;
import com.google.gson.JsonSyntaxException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
                      "baseUrl": "http://www.test.example",
                      "versionManifest": "blah.json",
                      "publicKey": "public.key"
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
                      "baseUrl": "This is not a url!",
                      "versionManifest": "This is not a url!",
                      "publicKey": "And neither is this."
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
        URL baseUrl = new URL("http://www.test.example");
        String versionManifest = "manifest.json";
        String publicKey = "public.key";
        manager.addUpdateSource(new UpdateSources.UpdateSource("Test source", baseUrl, versionManifest, publicKey));

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

    @Test
    public void resolveUrl_withValidData_shouldResolve() throws Exception {
        // GIVEN valid input:
        URL baseUrl = new URL("http://www.test.example");
        String path = "example.json";

        // WHEN we try to create a proper URL out of it:
        URL actual = UpdateManager.resolveUrl(baseUrl, path);

        // THEN we should see a good URL:
        assertEquals("http://www.test.example/example.json", actual.toString());
    }

    @Test
    public void resolveUrl_withBadData_shouldNotResolve() throws Exception {
        // bad data should crap out:
        assertNull(UpdateManager.resolveUrl(null, null));
        final String url = "http://test.example";
        assertEquals(url, UpdateManager.resolveUrl(new URL(url), null).toString());
    }

    @Test
    public void resolveUrl_withWindowsPath_shouldResolve() throws Exception {
        // GIVEN a path generated on a machine running windows:
        final String url = "http://www.test.example";
        final String path = "a\\b\\c.txt";

        // WHEN we resolve it:
        URL actual = UpdateManager.resolveUrl(new URL(url), path);

        // THEN there should be no surprises:
        assertEquals("http://www.test.example/a/b/c.txt", actual.toString());
    }

    @Test
    public void unresolveUrl_withValidData_shouldUnresolve() throws Exception {
        // GIVEN valid input:
        URL baseUrl = new URL("http://www.test.example/a");
        String path = "b/c.txt";

        // WHEN we resolve and then unresolve it:
        String actual = UpdateManager.unresolveUrl(baseUrl, UpdateManager.resolveUrl(baseUrl, path));

        // THEN we should see it unresolved correctly:
        assertEquals(path, actual);
    }
}