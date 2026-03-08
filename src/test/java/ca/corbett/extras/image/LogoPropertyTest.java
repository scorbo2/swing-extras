package ca.corbett.extras.image;

import ca.corbett.extras.properties.AbstractProperty;
import ca.corbett.extras.properties.AbstractPropertyBaseTests;
import ca.corbett.extras.properties.Properties;
import ca.corbett.extras.properties.PropertyFormFieldChangeListener;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Unit tests for LogoConfig class.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class LogoPropertyTest extends AbstractPropertyBaseTests {

    @Override
    protected AbstractProperty createTestObject(String name, String label) {
        return new LogoProperty(name, label);
    }

    private LogoProperty generateTestObject() {
        LogoProperty conf = new LogoProperty("test");
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

    private void assertConfObjectsEqual(LogoProperty conf1, LogoProperty conf2) {
        assertEquals(conf1.isAutoSize(), conf2.isAutoSize());
        assertEquals(conf1.getBgColor(), conf2.getBgColor());
        assertEquals(conf1.getBorderColor(), conf2.getBorderColor());
        assertEquals(conf1.getBorderWidth(), conf2.getBorderWidth());
        assertEquals(conf1.getFont().getFamily(), conf2.getFont().getFamily());
        assertEquals(conf1.getFont().isBold(), conf2.getFont().isBold());
        assertEquals(conf1.getFont().isItalic(), conf2.getFont().isItalic());
        assertEquals(conf1.getFont().getSize(), conf2.getFont().getSize());
        assertEquals(conf1.getTextColor(), conf2.getTextColor());
        assertEquals(conf1.hasBorder(), conf2.hasBorder());
        assertEquals(conf1.getLogoHeight(), conf2.getLogoHeight());
        assertEquals(conf1.getLogoWidth(), conf2.getLogoWidth());
    }

    @Test
    public void testPropsLoadSave() {
        LogoProperty conf1 = generateTestObject();
        Properties props = new Properties();
        conf1.saveToProps(props);
        LogoProperty conf2 = new LogoProperty("test");
        conf2.loadFromProps(props);
        assertConfObjectsEqual(conf1, conf2);
    }

    @Test
    public void testResetToDefaults() {
        LogoProperty conf1 = generateTestObject();
        conf1.resetToDefaults();
        LogoProperty conf2 = new LogoProperty("test");
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

    @Test
    public void testChangeListener() {
        // GIVEN a property with a mocked change listener:
        LogoProperty testProp = (LogoProperty)createTestObject("test", "test");
        PropertyFormFieldChangeListener listener = Mockito.mock(PropertyFormFieldChangeListener.class);
        testProp.addFormFieldChangeListener(listener);

        // WHEN we generate a form field and mess with it:
        LogoFormField formField = (LogoFormField)testProp.generateFormField();
        formField.setBackgroundColor(Color.BLUE);
        formField.setImageWidth(99);

        // THEN we should see our change listener get invoked:
        Mockito.verify(listener, Mockito.times(2)).valueChanged(Mockito.any());
    }

}
