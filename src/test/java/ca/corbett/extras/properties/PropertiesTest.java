package ca.corbett.extras.properties;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Font;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * Unit tests for the Properties class.
 *
 * @author scorbo2
 */
public class PropertiesTest {

    @Test
    public void testTypeConversion() {
        Properties prop1 = createTestProps();

        assertEquals("string", prop1.getString("String", ""));
        assertEquals(Integer.valueOf(3), prop1.getInteger("Integer", 0));
        assertEquals(Float.valueOf(1.5f), prop1.getFloat("Float", 0.0f));
        assertEquals(Double.valueOf(1234.56), prop1.getDouble("Double", 0.0));
        assertEquals(Color.blue, prop1.getColor("Color", Color.WHITE));
    }

    @Test
    public void testRemove() {
        Properties prop1 = new Properties();
        prop1.setString("something", "blah");
        assertEquals("blah", prop1.getString("something", ""));
        prop1.remove("something");
        assertEquals("", prop1.getString("something", ""));
    }

    @Test
    public void testGetPropertyNames() {
        Properties props = createTestProps();
        List<String> propNames = props.getPropertyNames();
        assertEquals(5, propNames.size());
        assertEquals("Color", propNames.get(0));
        assertEquals("String", propNames.get(4));
    }

    @Test
    public void testColorDecoding() {
        String str1 = "#ffaabbcc";
        String str2 = "0xffaabbcc";
        // Note there is a Color.decode() method but it seems to fail on both above strings...
        Color color1 = new Color(Long.decode(str1).intValue());
        Color color2 = new Color(Long.decode(str2).intValue());
        Color answer = new Color(0xaa, 0xbb, 0xcc);
        assertEquals(color1, color2);
        assertEquals(answer, color2);
    }

    @Test
    public void testFontStorage() {
        Font input = new Font("Monospaced", Font.BOLD, 14);
        Properties props = new Properties();
        props.setFont("myFont", input);
        assertEquals("Monospaced", props.getString("myFont_familyName", ""));
        assertEquals(true, props.getBoolean("myFont_isBold", false));
        assertEquals(false, props.getBoolean("myFont_isItalic", true));
        assertEquals(14, props.getInteger("myFont_pointSize", 99));
    }

    @Test
    public void testFontRetrieval() {
        Font input = new Font("Monospaced", Font.BOLD, 14);
        Properties props = new Properties();
        props.setFont("myFont", input);
        Font actual = props.getFont("myFont", null);
        assertNotNull(actual);
        assertEquals(input.getFamily(), actual.getFamily());
        assertEquals(input.isBold(), actual.isBold());
        assertEquals(input.isItalic(), actual.isItalic());
        assertEquals(input.getSize(), actual.getSize());
    }

    private Properties createTestProps() {
        Properties prop1 = new Properties();
        prop1.setString("String", "string");
        prop1.setInteger("Integer", 3);
        prop1.setFloat("Float", 1.5f);
        prop1.setDouble("Double", 1234.56);
        prop1.setColor("Color", Color.blue);
        return prop1;
    }

}
