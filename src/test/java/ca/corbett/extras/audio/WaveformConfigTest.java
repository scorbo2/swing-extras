package ca.corbett.extras.audio;

import ca.corbett.extras.properties.Properties;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


import java.awt.Color;

/**
 * Unit tests for WaveformConfig.
 *
 * @author scorbo2
 */
public class WaveformConfigTest {

    private WaveformConfig generateTestObject() {
        WaveformConfig config = new WaveformConfig();
        config.setBaselineColor(Color.BLUE);
        config.setBaselineEnabled(false);
        config.setBaselineThickness(4);
        config.setBgColor(Color.YELLOW);
        config.setFillColor(Color.WHITE);
        config.setOutlineColor(Color.BLACK);
        config.setOutlineEnabled(true);
        config.setOutlineThickness(5);
        config.setXScale(123);
        config.setYScale(456);
        return config;
    }

    private void assertConfigsEqual(WaveformConfig conf1, WaveformConfig conf2) {
        assertEquals(conf1.getBaselineColor(), conf2.getBaselineColor());
        assertEquals(conf1.isBaselineEnabled(), conf2.isBaselineEnabled());
        assertEquals(conf1.getBaselineThickness(), conf2.getBaselineThickness());
        assertEquals(conf1.getBgColor(), conf2.getBgColor());
        assertEquals(conf1.getFillColor(), conf2.getFillColor());
        assertEquals(conf1.getOutlineColor(), conf2.getOutlineColor());
        assertEquals(conf1.isOutlineEnabled(), conf2.isOutlineEnabled());
        assertEquals(conf1.getOutlineThickness(), conf2.getOutlineThickness());
        assertEquals(conf1.getXScale(), conf2.getXScale());
        assertEquals(conf1.getYScale(), conf2.getYScale());
    }

    @Test
    public void testWaveformSaveRestore() {
        WaveformConfig config = generateTestObject();
        Properties props = new Properties();
        config.saveToProps(props, "test.");
        WaveformConfig config2 = new WaveformConfig();
        config2.loadFromProps(props, "test.");
        assertConfigsEqual(config, config2);
    }

    @Test
    public void testWaveformClone() {
        WaveformConfig conf1 = generateTestObject();
        WaveformConfig conf2 = WaveformConfig.clonePreferences(conf1);
        assertConfigsEqual(conf1, conf2);
    }

}
