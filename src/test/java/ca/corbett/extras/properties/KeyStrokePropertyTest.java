package ca.corbett.extras.properties;

import ca.corbett.extras.io.KeyStrokeManager;
import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.KeyStrokeField;
import org.junit.jupiter.api.Test;

import javax.swing.KeyStroke;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeyStrokePropertyTest extends AbstractPropertyBaseTests {

    @Override
    protected AbstractProperty createTestObject(String fullyQualifiedName, String label) {
        return new KeyStrokeProperty(fullyQualifiedName, label, KeyStrokeManager.parseKeyStroke("Ctrl+F1"));
    }

    @Test
    public void listToString_withKeyStrokes_shouldReturnExpectedString() {
        // GIVEN a list of KeyStrokes:
        var keyStrokes = List.of(
                KeyStrokeManager.parseKeyStroke("Ctrl+S"),
                KeyStrokeManager.parseKeyStroke("Alt+F4"),
                KeyStrokeManager.parseKeyStroke("Shift+Enter")
        );

        // WHEN we convert it to a string:
        String result = KeyStrokeProperty.listToString(keyStrokes);

        // THEN we should get the expected result:
        assertEquals("Ctrl+S, Alt+F4, Shift+Enter", result);
    }

    @Test
    public void listToString_withEmptyList_shouldReturnEmptyString() {
        // GIVEN an empty list of KeyStrokes:
        var keyStrokes = List.<javax.swing.KeyStroke>of();

        // WHEN we convert it to a string:
        String result = KeyStrokeProperty.listToString(keyStrokes);

        // THEN we should get an empty string:
        assertEquals("", result);
    }

    @Test
    public void stringToList_withValidString_shouldReturnExpectedKeyStrokes() {
        // GIVEN a valid string of KeyStrokes:
        String input = "Ctrl+S, Alt+F4, Shift+Enter";

        // WHEN we convert it to a list:
        var result = KeyStrokeProperty.stringToList(input);

        // THEN we should get the expected KeyStrokes:
        assertEquals(3, result.size());
        assertEquals("Ctrl+S", KeyStrokeManager.keyStrokeToString(result.get(0)));
        assertEquals("Alt+F4", KeyStrokeManager.keyStrokeToString(result.get(1)));
        assertEquals("Shift+Enter", KeyStrokeManager.keyStrokeToString(result.get(2)));
    }

    @Test
    public void stringToList_withEmptyString_shouldReturnEmptyList() {
        // GIVEN an empty string:
        String input = "";

        // WHEN we convert it to a list:
        var result = KeyStrokeProperty.stringToList(input);

        // THEN we should get an empty list:
        assertEquals(0, result.size());
    }

    @Test
    public void generateFormField_withDefaults_shouldGenerateCorrectField() {
        // GIVEN a KeyStrokeProperty with default settings:
        KeyStroke testKeyStroke = KeyStrokeManager.parseKeyStroke("Ctrl+F1");
        KeyStrokeProperty property = new KeyStrokeProperty("test.property",
                                                           "Test Property",
                                                           testKeyStroke);

        // WHEN we generate the form field:
        FormField formField = property.generateFormFieldImpl();

        // THEN the field should be a KeyStrokeField with the correct initial value:
        assertInstanceOf(KeyStrokeField.class, formField);
        KeyStrokeField keyStrokeField = (KeyStrokeField)formField;
        assertEquals("Test Property", keyStrokeField.getFieldLabel().getText());
        assertEquals(testKeyStroke, keyStrokeField.getKeyStroke());

        // Expected defaults: allowBlank = false, no reserved keystrokes, default validation message:
        assertFalse(keyStrokeField.isAllowBlank());
        assertEquals(0, keyStrokeField.getReservedKeyStrokes().size());
        assertEquals(KeyStrokeField.RESERVED_MSG, keyStrokeField.getReservedKeyStrokeMsg());
    }

    @Test
    public void generateFormField_withCustomSettings_shouldGenerateCorrectField() {
        // GIVEN a KeyStrokeProperty with custom settings:
        KeyStroke testKeyStroke = KeyStrokeManager.parseKeyStroke("Ctrl+F2");
        KeyStrokeProperty property = new KeyStrokeProperty("test.property",
                                                           "Test Property",
                                                           testKeyStroke);
        property.setAllowBlank(true);
        List<KeyStroke> reservedKeyStrokes = List.of(KeyStrokeManager.parseKeyStroke("Alt+F4"));
        property.setReservedKeyStrokes(reservedKeyStrokes);
        property.setReservedKeyStrokeMsg("This shortcut is reserved.");

        // WHEN we generate the form field:
        FormField formField = property.generateFormFieldImpl();

        // THEN the field should be a KeyStrokeField with the correct initial value and settings:
        assertInstanceOf(KeyStrokeField.class, formField);
        KeyStrokeField keyStrokeField = (KeyStrokeField)formField;
        assertEquals("Test Property", keyStrokeField.getFieldLabel().getText());
        assertEquals(testKeyStroke, keyStrokeField.getKeyStroke());
        assertTrue(keyStrokeField.isAllowBlank());
        assertEquals(reservedKeyStrokes, keyStrokeField.getReservedKeyStrokes());
        assertEquals("This shortcut is reserved.", keyStrokeField.getReservedKeyStrokeMsg());
    }

    /**
     * Red/green test for issue #322
     */
    @Test
    public void generateFormFieldAfterLoad_withReservedKeyStrokes_shouldRespectReservedKeystrokes() {
        // GIVEN a reserved keystroke:
        final KeyStroke reservedKeyStroke = KeyStrokeManager.parseKeyStroke("Alt+F4");

        // AND GIVEN a KeyStrokeProperty with that reserved keystroke set:
        KeyStrokeProperty property = new KeyStrokeProperty("test.property",
                                                           "Test Property",
                                                           KeyStrokeManager.parseKeyStroke("Ctrl+F3"));
        List<KeyStroke> reservedKeyStrokes = List.of(reservedKeyStroke);
        property.setReservedKeyStrokes(reservedKeyStrokes);

        // WHEN we invoke load() on the property before generating a form field:
        // (This simulates what happens in ExtensionManager/AppProperties when generating an app settings dialog)
        // By giving it an empty Properties object here, the expected behavior is that it should use defaults
        // for any value not explicitly set, BUT should also preserve any value that WAS explicitly set.
        // The default value for the reserved keystroke list is an empty list, but we explicitly set one above
        // which should be preserved.
        property.loadFromProps(new Properties());

        // WHEN we now tell it to generate a form field:
        KeyStrokeField field = (KeyStrokeField)property.generateFormField();

        // And WHEN we try to set the field's keystroke to that reserved keystroke:
        field.setKeyStroke(reservedKeyStroke);

        // THEN the field should be in an invalid state:
        // (this was the failure in #322 - the form field would never hear about the reserved keystrokes!)
        assertFalse(field.isValid());

        // WHEN we now try to load that field's value back into the property:
        property.loadFromFormField(field);

        // THEN the property's value should not change, because the field is in an invalid state:
        // (new behavior after the fix for #322 - we now double-check field validity before accepting it)
        assertEquals(KeyStrokeManager.parseKeyStroke("Ctrl+F3"), property.getKeyStroke());
    }
}
