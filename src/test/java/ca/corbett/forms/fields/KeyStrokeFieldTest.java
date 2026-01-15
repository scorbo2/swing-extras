package ca.corbett.forms.fields;

import ca.corbett.extras.io.KeyStrokeManager;
import ca.corbett.forms.validators.ValidationResult;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeyStrokeFieldTest extends FormFieldBaseTests {

    @Override
    protected FormField createTestObject() {
        return new KeyStrokeField("Test KeyStroke Field", KeyStrokeManager.parseKeyStroke("Ctrl+F1"));
    }

    @Test
    public void getFieldComponent_shouldReturnExpectedFieldType() {
        JComponent component = actual.getFieldComponent();
        assertInstanceOf(JTextField.class, component);
    }

    @Test
    public void isMultiLine_shouldAlwaysBeFalse() {
        assertFalse(actual.isMultiLine());
    }

    @Test
    public void shouldExpand_shouldAlwaysBeFalse() {
        assertFalse(actual.shouldExpand());
    }

    @Test
    public void getKeyStroke_withValidText_shouldReturnKeyStroke() {
        // GIVEN a KeyStrokeField with a valid shortcut already in it:
        KeyStrokeField keyStrokeField = (KeyStrokeField)actual;

        // WHEN we ask it for its KeyStroke:
        KeyStroke keyStroke = keyStrokeField.getKeyStroke();

        // THEN we should get back something non-null and correct:
        assertNotNull(keyStroke);
        assertEquals("Ctrl+F1", KeyStrokeManager.keyStrokeToString(keyStroke));
    }

    @Test
    public void getKeyStroke_withInvalidText_shouldReturnNull() {
        // GIVEN a KeyStrokeField with invalid shortcut text in it:
        KeyStrokeField keyStrokeField = (KeyStrokeField)actual;
        ((JTextField)keyStrokeField.getFieldComponent()).setText("This ain't no valid keystroke");

        // WHEN we ask it for its KeyStroke:
        KeyStroke keyStroke = keyStrokeField.getKeyStroke();

        // THEN we should get back null:
        assertNull(keyStroke);
    }

    @Test
    public void testGetColumns() {
        assertEquals(KeyStrokeField.DEFAULT_COLS, ((JTextField)actual.getFieldComponent()).getColumns());
    }

    @Test
    public void addValueChangedListener_withChanges_shouldReportChanges() {
        // GIVEN a KeyStrokeField with a registered listener:
        KeyStrokeField actualField = (KeyStrokeField)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);

        // WHEN we make some change in the field:
        actualField.setKeyStroke(KeyStrokeManager.parseKeyStroke("shift+a"));

        // THEN we *should* get a single notification, but because Swing's DocumentListener
        //      mechanism is horribly broken, we will actually get two. Yes, two.
        //      (First one from when the field is cleared, second from when new value is inserted. Sigh.)
        Mockito.verify(listener, Mockito.times(2)).formFieldValueChanged(actual);
    }

    @Test
    public void removeValueChangedListener_withChanges_shouldNotReportChanges() {
        // GIVEN a KeyStrokeField with a registered then removed listener:
        KeyStrokeField actualField = (KeyStrokeField)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);
        actual.removeValueChangedListener(listener);

        // WHEN we make changes in the field:
        actualField.setKeyStroke(KeyStrokeManager.parseKeyStroke("shift+a"));
        actualField.setKeyStroke(KeyStrokeManager.parseKeyStroke("shift+b"));

        // THEN we should NOT get any notifications:
        Mockito.verify(listener, Mockito.times(0)).formFieldValueChanged(actual);
    }

    @Test
    public void isValid_withValidShortcut_shouldValidate() {
        // GIVEN a KeyStrokeField with a valid shortcut:
        KeyStrokeField keyStrokeField = (KeyStrokeField)actual;

        // WHEN we validate it:
        boolean isValid = keyStrokeField.isValid();

        // THEN there should be no validation failures:
        assertTrue(isValid);
    }

    @Test
    public void isValid_withBlankShortcutAndAllowBlank_shouldValidate() {
        // GIVEN a KeyStrokeField that allows blank input:
        KeyStrokeField keyStrokeField = (KeyStrokeField)actual;
        keyStrokeField.setAllowBlank(true);
        keyStrokeField.setKeyStroke(null); // set to blank

        // WHEN we validate it:
        boolean isValid = keyStrokeField.isValid();

        // THEN there should be no validation failures:
        assertTrue(isValid);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void isValid_withBlankShortcutAndDisallowBlank_shouldNotValidate() {
        // GIVEN a KeyStrokeField that disallows blank input:
        KeyStrokeField keyStrokeField = (KeyStrokeField)actual;
        keyStrokeField.setAllowBlank(false);
        keyStrokeField.setKeyStroke(null); // set to blank

        // WHEN we validate it:
        boolean isValid = keyStrokeField.isValid();

        // THEN there should be validation failures:
        assertFalse(isValid);

        // AND the validation message should specifically mention blank input:
        ValidationResult result = keyStrokeField.getBuiltInValidator().validate(keyStrokeField);
        assertEquals("Keystroke cannot be blank", result.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void isValid_withInvalidShortcut_shouldNotValidate() {
        // GIVEN a KeyStrokeField with an invalid shortcut:
        KeyStrokeField keyStrokeField = (KeyStrokeField)actual;
        ((JTextField)keyStrokeField.getFieldComponent()).setText("This is not valid");

        // WHEN we validate it:
        boolean isValid = keyStrokeField.isValid();

        // THEN there should be validation failures:
        assertFalse(isValid);

        // AND the validation message should specifically mention invalid input:
        ValidationResult result = keyStrokeField.getBuiltInValidator().validate(keyStrokeField);
        assertEquals("Invalid keystroke format", result.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void isValid_withReservedShortcut_shouldNotValidate() {
        // GIVEN a KeyStrokeField with a reserved shortcut:
        KeyStrokeField keyStrokeField = (KeyStrokeField)actual;
        KeyStroke reserved = KeyStrokeManager.parseKeyStroke("Ctrl+F1");
        keyStrokeField.setReservedKeyStrokes(List.of(reserved));
        keyStrokeField.setKeyStroke(reserved);

        // WHEN we validate it:
        boolean isValid = keyStrokeField.isValid();

        // THEN there should be validation failures:
        assertFalse(isValid);

        // AND the validation message should specifically mention reserved input:
        ValidationResult result = keyStrokeField.getBuiltInValidator().validate(keyStrokeField);
        assertEquals(KeyStrokeField.RESERVED_MSG, result.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void isValid_withReservedShortcutAndCustomMessage_shouldNotValidate() {
        // GIVEN a KeyStrokeField with a reserved shortcut and a custom validation message:
        final String customMessage = "Hey! You can't do that!";
        KeyStrokeField keyStrokeField = (KeyStrokeField)actual;
        KeyStroke reserved = KeyStrokeManager.parseKeyStroke("Ctrl+F1");
        keyStrokeField.setReservedKeyStrokes(List.of(reserved));
        keyStrokeField.setReservedKeyStrokeMsg(customMessage);
        keyStrokeField.setKeyStroke(reserved);

        // WHEN we validate it:
        boolean isValid = keyStrokeField.isValid();

        // THEN there should be validation failures:
        assertFalse(isValid);

        // AND the validation message should specifically use our supplied message:
        ValidationResult result = keyStrokeField.getBuiltInValidator().validate(keyStrokeField);
        assertEquals(customMessage, result.getMessage());
    }

}
