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
 * Unit tests for the ImagePanelConfig class.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class ImagePanelConfigTest extends AbstractPropertyBaseTests {

    @Override
    protected AbstractProperty createTestObject(String name, String label) {
        return ImagePanelConfig.createSimpleReadOnlyProperties(name).setPropertyLabel(label);
    }

    private ImagePanelConfig generateTestObject() {
        ImagePanelConfig conf = new ImagePanelConfig("test");
        conf.setBgColor(Color.YELLOW);
        conf.setDisplayMode(ImagePanelConfig.DisplayMode.CUSTOM);
        conf.setEnableMouseCursor(false);
        conf.setEnableMouseDragging(true);
        conf.setEnableZoomOnMouseClick(false);
        conf.setEnableZoomOnMouseWheel(true);
        conf.setRenderingQuality(ImagePanelConfig.Quality.QUICK_AND_DIRTY);
        conf.setZoomFactorIncrement(2.2);
        return conf;
    }

    private void assertConfObjectsEqual(ImagePanelConfig conf1, ImagePanelConfig conf2) {
        assertEquals(conf1.getBgColor(), conf2.getBgColor());
        assertEquals(conf1.getDisplayMode(), conf2.getDisplayMode());
        assertEquals(conf1.isEnableMouseCursor(), conf2.isEnableMouseCursor());
        assertEquals(conf1.isEnableMouseDragging(), conf2.isEnableMouseDragging());
        assertEquals(conf1.isEnableZoomOnMouseClick(), conf2.isEnableZoomOnMouseClick());
        assertEquals(conf1.isEnableZoomOnMouseWheel(), conf2.isEnableZoomOnMouseWheel());
        assertEquals(conf1.getRenderingQuality(), conf2.getRenderingQuality());
        assertEquals(conf1.getZoomFactorIncrement(), conf2.getZoomFactorIncrement(), 0.01);
    }

    @Test
    public void testPropsSaveLoad() {
        ImagePanelConfig conf1 = generateTestObject();
        Properties props = new Properties();
        conf1.saveToProps(props);
        ImagePanelConfig conf2 = new ImagePanelConfig("test");
        conf2.loadFromProps(props);
        assertConfObjectsEqual(conf1, conf2);
    }

    @Test
    public void testChangeListener() throws Exception {
        // GIVEN a property with a mocked change listener:
        ImagePanelConfig testProp = (ImagePanelConfig)createTestObject("test", "test");
        PropertyFormFieldChangeListener listener = Mockito.mock(PropertyFormFieldChangeListener.class);
        testProp.addFormFieldChangeListener(listener);

        // WHEN we generate a form field and mess with it:
        ImagePanelFormField formField = (ImagePanelFormField)testProp.generateFormField();
        formField.setRenderQuality(ImagePanelConfig.Quality.QUICK_AND_DIRTY);
        formField.setEnableZoomOnMouseWheel(true);

        // THEN we should see our change listener get invoked:
        Mockito.verify(listener, Mockito.times(2)).valueChanged(Mockito.any());
    }

}
