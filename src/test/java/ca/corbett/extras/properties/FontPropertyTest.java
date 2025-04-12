package ca.corbett.extras.properties;

import ca.corbett.forms.fields.FontField;
import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FontPropertyTest {

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
}