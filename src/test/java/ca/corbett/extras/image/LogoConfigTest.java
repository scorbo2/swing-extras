package ca.corbett.extras.image;

import ca.corbett.extras.properties.Properties;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Unit tests for LogoConfig class.
 *
 * @author scorbo2
 */
public class LogoConfigTest {

    private LogoConfig generateTestObject() {
        LogoConfig conf = new LogoConfig("test");
        conf.setAutoSize(false);
        conf.setBgColor(Color.YELLOW);
        conf.setBorderColor(Color.WHITE);
        conf.setBorderWidth(3);
        conf.setFontByFamilyName("Serif");
        conf.setTextColor(Color.BLUE);
        conf.setFontPointSize(11);
        conf.setHasBorder(true);
        conf.setLogoHeight(123);
        conf.setLogoWidth(456);
        return conf;
    }

    private void assertConfObjectsEqual(LogoConfig conf1, LogoConfig conf2) {
        assertEquals(conf1.isAutoSize(), conf2.isAutoSize());
        assertEquals(conf1.getBgColor(), conf2.getBgColor());
        assertEquals(conf1.getBorderColor(), conf2.getBorderColor());
        assertEquals(conf1.getBorderWidth(), conf2.getBorderWidth());
        assertEquals(conf1.getFont().getName(), conf2.getFont().getName());
        assertEquals(conf1.getTextColor(), conf2.getTextColor());
        assertEquals(conf1.getFontPointSize(), conf2.getFontPointSize());
        assertEquals(conf1.hasBorder(), conf2.hasBorder());
        assertEquals(conf1.getLogoHeight(), conf2.getLogoHeight());
        assertEquals(conf1.getLogoWidth(), conf2.getLogoWidth());
    }

    @Test
    public void testPropsLoadSave() {
        LogoConfig conf1 = generateTestObject();
        Properties props = new Properties();
        conf1.saveToProps(props, "test.");
        LogoConfig conf2 = new LogoConfig("blah");
        conf2.loadFromProps(props, "test.");
        assertConfObjectsEqual(conf1, conf2);
    }

    @Test
    public void testResetToDefaults() {
        LogoConfig conf1 = generateTestObject();
        conf1.resetToDefaults();
        LogoConfig conf2 = new LogoConfig("blah");
        assertConfObjectsEqual(conf1, conf2);
    }

    @Test
    public void testColorEncodeDecode() {
        Color testColor = new Color(128, 64, 255);
        int colorInt = testColor.getRGB();
        String colorStr = "0x" + Integer.toHexString(testColor.getRGB());
        Color result1 = new Color(colorInt);
        Color result2 = new Color(Long.decode(colorStr).intValue());
        assertEquals(testColor, result1);
        assertEquals(result1, result2);
    }

}
