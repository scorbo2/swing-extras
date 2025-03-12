package ca.corbett.extras.gradient;

import ca.corbett.extras.properties.Properties;
import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the GradientConfig class.
 *
 * @scorbo2
 * @since 2025-03-11
 */
class GradientConfigTest {

    @Test
    public void testResetToDefaults_withCustomValues_shouldReset() {
        GradientConfig config = new GradientConfig();
        Color initialColor1 = config.getColor1();
        Color initialColor2 = config.getColor2();
        GradientUtil.GradientType initialType = config.getGradientType();
        config.setColor1(Color.RED);
        config.setColor2(Color.BLUE);
        config.setGradientType(GradientUtil.GradientType.STAR);
        config.resetToDefaults();
        assertEquals(initialColor1, config.getColor1());
        assertEquals(initialColor2, config.getColor2());
        assertEquals(initialType, config.getGradientType());
    }

    @Test
    public void testSaveAndLoad_withCustomValues_shouldSucceed() {
        GradientConfig config1 = new GradientConfig();
        config1.setColor1(Color.RED);
        config1.setColor2(Color.BLUE);
        config1.setGradientType(GradientUtil.GradientType.STAR);
        Properties props = new Properties();
        config1.saveToProps(props, "test.");

        GradientConfig config2 = new GradientConfig();
        config2.loadFromProps(props, "test.");
        assertEquals(config1.getColor1(), config2.getColor1());
        assertEquals(config1.getColor2(), config2.getColor2());
        assertEquals(config1.getGradientType(), config2.getGradientType());
    }
}