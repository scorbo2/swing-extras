package ca.corbett.extras.properties;

import ca.corbett.forms.fields.FontField;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.awt.Color;
import java.awt.Font;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FontPropertyTest extends AbstractPropertyBaseTests {

    @Override
    protected AbstractProperty createTestObject(String name, String label) {
        return new FontProperty(name, label);
    }

    @Test
    public void generateFormField_withoutColors_shouldNotExposeColors() {
        // GIVEN a FontProperty that does NOT specify foreground/background colors:
        FontProperty prop = new FontProperty("fontProperty", "Font");

        // WHEN we generate a form field:
        FontField formField = (FontField)prop.generateFormField();

        // THEN it should not have any colors in it:
        assertNull(formField.getTextColor());
        assertNull(formField.getBgColor());
    }

    @Test
    public void generateFormField_withColors_shouldExposeColors() {
        // GIVEN a FontProperty that DOES specify foreground/background colors:
        FontProperty prop = new FontProperty("fontProperty", "Font", Color.RED, Color.YELLOW);

        // WHEN we generate a form field:
        FontField formField = (FontField)prop.generateFormField();

        // THEN it should contain colors:
        assertEquals(Color.RED, formField.getTextColor());
        assertEquals(Color.YELLOW, formField.getBgColor());
    }

    @Test
    public void saveToProps_givenNoColors_shouldNotSaveColors() {
        // GIVEN a FontProperty that does not specify foreground/background colors:
        FontProperty prop = new FontProperty("FontProperty", "Font");

        // WHEN we save it to props:
        Properties props = new Properties();
        prop.saveToProps(props);

        // THEN we should not see colors in there:
        assertNull(props.getColor("General.General.FontProperty.textColor", null));
        assertNull(props.getColor("General.General.FontProperty.bgColor", null));
    }

    @Test
    public void saveToProps_givenColors_shouldSaveColors() {
        // GIVEN a FontProperty that DOES specify foreground/background colors:
        FontProperty prop = new FontProperty("FontProperty", "Font", Color.RED, Color.YELLOW);

        // WHEN we save it to props:
        Properties props = new Properties();
        prop.saveToProps(props);

        // THEN we should find our colors in there:
        assertEquals(Color.RED, props.getColor("General.General.FontProperty.textColor", null));
        assertEquals(Color.YELLOW, props.getColor("General.General.FontProperty.bgColor", null));
    }

    @Test
    public void loadFromProps_givenNoColors_shouldNotLoadColors() {
        // GIVEN a FontProperty that does not specify foreground/background colors:
        FontProperty prop = new FontProperty("FontProperty", "Font");

        // WHEN we save it to props and then load it:
        Properties props = new Properties();
        prop.saveToProps(props);
        FontProperty actual = new FontProperty("FontProperty", "Font");
        actual.loadFromProps(props);

        // THEN we should not see any colors in it:
        assertNull(actual.getTextColor());
        assertNull(actual.getBgColor());
    }

    @Test
    public void loadFromProps_givenColors_shouldLoadColors() {
        // GIVEN a FontProperty that DOES specify foreground/background colors:
        FontProperty prop = new FontProperty("FontProperty", "Font", Color.RED, Color.YELLOW);

        // WHEN we save it to props and then load it:
        Properties props = new Properties();
        prop.saveToProps(props);
        FontProperty actual = new FontProperty("FontProperty", "Font");
        actual.loadFromProps(props);

        // THEN we should find our colors in there:
        assertEquals(Color.RED, actual.getTextColor());
        assertEquals(Color.YELLOW, actual.getBgColor());
    }

    @Test
    public void formFieldChangeListener_withFormFieldChanges_shouldFireChangeEvents() {
        // GIVEN a test prop with a mocked property form field change listener on it:
        FontProperty testProp = new FontProperty("test", "test", new Font(Font.MONOSPACED, Font.PLAIN, 12));
        PropertyFormFieldChangeListener listener = Mockito.mock(PropertyFormFieldChangeListener.class);
        testProp.addFormFieldChangeListener(listener);

        // WHEN we generate a form field and mess with it a bit:
        FontField formField = (FontField)testProp.generateFormField();
        formField.setSelectedFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        formField.setSelectedFont(new Font(Font.SERIF, Font.BOLD, 14));

        // THEN we should see our change listener got invoked:
        Mockito.verify(listener, Mockito.times(2)).valueChanged(Mockito.any());
    }
}