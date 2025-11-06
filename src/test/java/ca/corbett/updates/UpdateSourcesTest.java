package ca.corbett.updates;

import ca.corbett.extras.io.FileSystemUtil;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdateSourcesTest {

    public final static String updateSources = """
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
                  "baseUrl": "file:/local/path/to/TestApplication",
                  "versionManifest": "version_manifest.json",
                  "publicKey": "public.key"
                }
              ]
            }
            """;

    @Test
    public void parse_withValidJson_shouldParse() throws Exception {
        // GIVEN a valid update sources json file:
        File tempFile = File.createTempFile("UpdateSourcesTest", ".json");
        tempFile.deleteOnExit();
        FileSystemUtil.writeStringToFile(updateSources, tempFile);

        // WHEN we parse it out:
        UpdateManager manager = new UpdateManager(tempFile);

        // THEN we should see expected values:
        assertEquals("TestApplication", manager.getApplicationName());
        assertEquals(2, manager.getUpdateSources().size());
        assertTrue(manager.isAllowSnapshots());
    }

}