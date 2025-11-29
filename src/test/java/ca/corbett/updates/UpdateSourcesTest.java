package ca.corbett.updates;

import ca.corbett.extras.io.FileSystemUtil;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdateSourcesTest {

    public final static String updateSourcesJson = """
            {
              "applicationName": "TestApplication",
              "isAllowSnapshots": "true",
              "updateSources": [
                {
                  "name": "Some web host",
                  "baseUrl": "http://www.test.example/TestApplication",
                  "versionManifest": "version_manifest.json",
                  "publicKey": "public.key"
                },
                {
                  "name": "filesystem",
                  "baseUrl": "
            """
            + "file:" + System.getProperty("java.io.tmpdir") + "\"," + """
                  "versionManifest": "version_manifest.json",
                  "publicKey": "public.key"
                }
              ]
            }
            """;

    @Test
    public void parseViaUpdateManager_withValidJson_shouldParse() throws Exception {
        // GIVEN a valid update sources json file:
        File tempFile = File.createTempFile("UpdateSourcesTest", ".json");
        tempFile.deleteOnExit();
        FileSystemUtil.writeStringToFile(updateSourcesJson, tempFile);

        // WHEN we parse it out via UpdateManager:
        UpdateManager manager = new UpdateManager(tempFile);

        // THEN we should see expected values:
        assertEquals("TestApplication", manager.getApplicationName());
        assertEquals(2, manager.getUpdateSources().size());
        assertTrue(manager.isAllowSnapshots());
    }

    @Test
    public void fromJson_withValidJson_shouldParse() throws Exception {
        // GIVEN a valid update sources json string:
        // WHEN we parse it out via UpdateSources.fromJson:
        UpdateSources updateSources = UpdateSources.fromJson(updateSourcesJson);

        // THEN we should see it parsed okay:
        assertNotNull(updateSources);
        assertEquals("TestApplication", updateSources.getApplicationName());
        assertEquals(2, updateSources.getUpdateSources().size());
    }

    @Test
    public void fromJson_withVariableSubstitution_shouldSubstitute() throws Exception {
        // GIVEN UpdateSource json that includes a variable to substitute:
        System.setProperty("user.home", "/test/directory");
        final String testJson = """
                {
                  "name": "Test",
                  "baseUrl": "file:${user.home}/hello"
                }
                """;

        // WHEN we parse it via UpdateSource.fromJson:
        UpdateSources.UpdateSource updateSource = UpdateSources.UpdateSource.fromJson(testJson);

        // THEN we should see it parsed okay and did the variable substitution:
        assertNotNull(updateSource);
        assertEquals("Test", updateSource.getName());
        assertEquals("file:/test/directory/hello", updateSource.getBaseUrl().toString());
    }

    @Test
    public void pruneLocalSources_withNonExistentLocalSource_shouldPrune() throws Exception {
        // GIVEN UpdateSource json that references a non-existing local dir:
        final String testJson = """
                {
                  "applicationName": "TestApplication",
                  "isAllowSnapshots": "true",
                  "updateSources": [
                    {
                      "name": "Some web host",
                      "baseUrl": "http://www.test.example/TestApplication",
                      "versionManifest": "version_manifest.json",
                      "publicKey": "public.key"
                    },
                    {
                      "name": "filesystem",
                      "baseUrl": "file:/this/directory/does/not/exist/no/way/jose",
                      "versionManifest": "version_manifest.json",
                      "publicKey": "public.key"
                    }
                  ]
                }
                """;

        // WHEN we try to parse it out:
        UpdateSources updateSources = UpdateSources.fromJson(testJson);

        // THEN we should see that the non-existing source got pruned automatically:
        assertNotNull(updateSources);
        assertEquals(1, updateSources.getUpdateSources().size());
        assertEquals("Some web host", updateSources.getUpdateSources().get(0).getName());
    }
}