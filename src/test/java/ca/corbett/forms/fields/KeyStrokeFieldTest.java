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

    @Test
    public void addReservedKeyStrokes_withAdditionalKeyStrokes_shouldAddThem() {
        // GIVEN a KeyStrokeField with one reserved keystroke:
        KeyStrokeField keyStrokeField = (KeyStrokeField)actual;
        KeyStroke reserved1 = KeyStrokeManager.parseKeyStroke("Ctrl+S");
        keyStrokeField.setReservedKeyStrokes(List.of(reserved1));

        // WHEN we add additional reserved keystrokes:
        KeyStroke reserved2 = KeyStrokeManager.parseKeyStroke("Ctrl+O");
        KeyStroke reserved3 = KeyStrokeManager.parseKeyStroke("Ctrl+Q");
        keyStrokeField.addReservedKeyStrokes(List.of(reserved2, reserved3));

        // THEN all three should be in the reserved list:
        List<KeyStroke> reserved = keyStrokeField.getReservedKeyStrokes();
        assertEquals(3, reserved.size());
        assertTrue(reserved.contains(reserved1));
        assertTrue(reserved.contains(reserved2));
        assertTrue(reserved.contains(reserved3));
    }

    @Test
    public void addReservedKeyStrokes_withDuplicates_shouldPruneThem() {
        // GIVEN a KeyStrokeField with one reserved keystroke:
        KeyStrokeField keyStrokeField = (KeyStrokeField)actual;
        KeyStroke reserved1 = KeyStrokeManager.parseKeyStroke("Ctrl+S");
        keyStrokeField.setReservedKeyStrokes(List.of(reserved1));

        // WHEN we add the same keystroke again plus a new one:
        KeyStroke reserved2 = KeyStrokeManager.parseKeyStroke("Ctrl+O");
        keyStrokeField.addReservedKeyStrokes(List.of(reserved1, reserved2));

        // THEN only two unique keystrokes should be in the list:
        List<KeyStroke> reserved = keyStrokeField.getReservedKeyStrokes();
        assertEquals(2, reserved.size());
        assertTrue(reserved.contains(reserved1));
        assertTrue(reserved.contains(reserved2));
    }

    @Test
    public void addReservedKeyStrokes_withNullList_shouldNotThrowException() {
        // GIVEN a KeyStrokeField with one reserved keystroke:
        KeyStrokeField keyStrokeField = (KeyStrokeField)actual;
        KeyStroke reserved1 = KeyStrokeManager.parseKeyStroke("Ctrl+S");
        keyStrokeField.setReservedKeyStrokes(List.of(reserved1));

        // WHEN we add null:
        keyStrokeField.addReservedKeyStrokes(null);

        // THEN the original reserved keystroke should still be there:
        List<KeyStroke> reserved = keyStrokeField.getReservedKeyStrokes();
        assertEquals(1, reserved.size());
        assertTrue(reserved.contains(reserved1));
    }

    @Test
    public void clearReservedKeyStrokes_shouldRemoveAllReservedKeyStrokes() {
        // GIVEN a KeyStrokeField with multiple reserved keystrokes:
        KeyStrokeField keyStrokeField = (KeyStrokeField)actual;
        KeyStroke reserved1 = KeyStrokeManager.parseKeyStroke("Ctrl+S");
        KeyStroke reserved2 = KeyStrokeManager.parseKeyStroke("Ctrl+O");
        keyStrokeField.setReservedKeyStrokes(List.of(reserved1, reserved2));

        // WHEN we clear the reserved keystrokes:
        keyStrokeField.clearReservedKeyStrokes();

        // THEN the list should be empty:
        List<KeyStroke> reserved = keyStrokeField.getReservedKeyStrokes();
        assertEquals(0, reserved.size());
    }

    @Test
    public void clearReservedKeyStrokes_shouldBeSameAsSettingEmptyList() {
        // GIVEN a KeyStrokeField with multiple reserved keystrokes:
        KeyStrokeField keyStrokeField1 = (KeyStrokeField)actual;
        KeyStroke reserved1 = KeyStrokeManager.parseKeyStroke("Ctrl+S");
        KeyStroke reserved2 = KeyStrokeManager.parseKeyStroke("Ctrl+O");
        keyStrokeField1.setReservedKeyStrokes(List.of(reserved1, reserved2));

        // AND GIVEN another KeyStrokeField with the same reserved keystrokes:
        KeyStrokeField keyStrokeField2 = new KeyStrokeField("Test 2", null);
        keyStrokeField2.setReservedKeyStrokes(List.of(reserved1, reserved2));

        // WHEN we clear one using clearReservedKeyStrokes() and the other using setReservedKeyStrokes(List.of()):
        keyStrokeField1.clearReservedKeyStrokes();
        keyStrokeField2.setReservedKeyStrokes(List.of());

        // THEN both should have the same empty list:
        assertEquals(keyStrokeField1.getReservedKeyStrokes(), keyStrokeField2.getReservedKeyStrokes());
    }

}
