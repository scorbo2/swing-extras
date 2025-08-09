package ca.corbett.extras.properties;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * For testing Properties and FileBasedProperties.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class FileBasedPropertiesTest {

    @Test
    public void testFileBasedProps() throws IOException {
        File file = File.createTempFile("test", ".test");
        file.deleteOnExit();
        FileBasedProperties props = new FileBasedProperties(file);
        props.setCommentHeader("Blah blah blah");
        props.setString("test1", "value1");
        props.setString("test2", "value2");
        props.setInteger("test3", 33);
        props.setColor("test4", Color.BLUE);
        props.save("This is a comment line.\nThis is a second comment line.\nThese comments are ignored.");

        assertTrue(file.exists());
        FileBasedProperties props2 = new FileBasedProperties(file);
        props2.load();
        assertEquals("value1", props2.getString("test1", ""));
        assertEquals("value2", props2.getString("test2", ""));
        assertEquals(Integer.valueOf(33), props2.getInteger("test3", 1));
        assertEquals(Color.BLUE, props2.getColor("test4", Color.WHITE));
    }

    @Test
    public void testColorRetrievalWithAlpha() throws IOException {
        File tmpFile = File.createTempFile("test", ".test");
        tmpFile.deleteOnExit();

        String halfGrey = "0x77777777";
        String fullGrey = "0xFF777777";
        String invisibleGrey = "0x00777777";
        String noAlphaSpecified = "0x777777";

        List<String> lines = new ArrayList<>();
        lines.add("halfGrey=" + halfGrey);
        lines.add("fullGrey=" + fullGrey);
        lines.add("invisibleGrey=" + invisibleGrey);
        lines.add("noAlphaSpecified=" + noAlphaSpecified);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile))) {
            for (String line : lines) {
                writer.append(line);
                writer.newLine();
            }
            writer.flush();
        }

        FileBasedProperties props = new FileBasedProperties(tmpFile);
        props.load();
        assertEquals(119, props.getColor("halfGrey", null).getAlpha());
        assertEquals(255, props.getColor("fullGrey", null).getAlpha());
        assertEquals(0, props.getColor("invisibleGrey", null).getAlpha());
        assertEquals(255, props.getColor("noAlphaSpecified", null).getAlpha());
    }
}
