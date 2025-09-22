package ca.corbett.extras.properties;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.Font;

/**
 * Regression test for issue #115.  Verifies that setters handle null values
 * gracefully and return default values on retrieval.
 */
public class PropertiesNullValueTest {

    @Test
    public void testNullValuesInSettersDoNotThrowAndReturnDefaultValues() {
        Properties props = new Properties();

        // Null String
        assertDoesNotThrow(() -> props.setString("myString", null));
        assertEquals("default", props.getString("myString", "default"));

        // Null Integer
        assertDoesNotThrow(() -> props.setInteger("myInt", null));
        assertEquals(Integer.valueOf(42), props.getInteger("myInt", 42));

        // Null Boolean
        assertDoesNotThrow(() -> props.setBoolean("myBool", null));
        assertEquals(Boolean.TRUE, props.getBoolean("myBool", true));

        // Null Float
        assertDoesNotThrow(() -> props.setFloat("myFloat", null));
        assertEquals(Float.valueOf(1.23f), props.getFloat("myFloat", 1.23f));

        // Null Double
        assertDoesNotThrow(() -> props.setDouble("myDouble", null));
        assertEquals(Double.valueOf(4.56), props.getDouble("myDouble", 4.56));

        // Null Color
        Color defaultColor = Color.RED;
        assertDoesNotThrow(() -> props.setColor("myColor", null));
        assertEquals(defaultColor, props.getColor("myColor", defaultColor));

        // Null Font
        Font defaultFont = new Font("Monospaced", Font.PLAIN, 12);
        assertDoesNotThrow(() -> props.setFont("myFont", null));
        Font retrieved = props.getFont("myFont", defaultFont);
        assertNotNull(retrieved);
        assertEquals(defaultFont.getFamily(), retrieved.getFamily());
        assertEquals(defaultFont.isBold(), retrieved.isBold());
        assertEquals(defaultFont.isItalic(), retrieved.isItalic());
        assertEquals(defaultFont.getSize(), retrieved.getSize());
    }
}
