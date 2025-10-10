package ca.corbett.extras;

import ca.corbett.extras.io.FileSystemUtil;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UpdateManagerTest {

    @Test
    public void constructor_withValidJson_shouldParse() throws Exception {
        // GIVEN a minimal but valid sources json:
        String json = "{ \"name\": \"Test\", \"urls\": [ \"http://www.test.example\" ] }";
        File sourcesFile = File.createTempFile("UpdateManagerTest", "json");
        sourcesFile.deleteOnExit();
        FileSystemUtil.writeStringToFile(json, sourcesFile);

        // WHEN we instantiate an UpdateManager with it:
        UpdateManager manager = new UpdateManager(sourcesFile);

        // THEN we should see expected results:
        assertNotNull(manager);
        assertNotNull(manager.getSource());
        assertEquals("Test", manager.getSource().getName());
        assertEquals(1, manager.getSource().getUrls().size());
        assertEquals("http://www.test.example", manager.getSource().getUrls().get(0));
    }
}