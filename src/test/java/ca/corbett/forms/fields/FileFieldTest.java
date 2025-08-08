package ca.corbett.forms.fields;

import ca.corbett.extras.CoalescingDocumentListener;
import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.ValidationResult;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Component;
import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileFieldTest extends FormFieldBaseTests {

    @Override
    protected FormField createTestObject() {
        return new FileField("test", null, 10, FileField.SelectionType.NonExistingFile, true);
    }

    @Test
    public void testGetComponent() {
        assertNotNull(actual.getFieldComponent());
        assertInstanceOf(JPanel.class, actual.getFieldComponent());
    }

    @Test
    public void testGetFieldLabel() {
        assertTrue(actual.hasFieldLabel());
        assertEquals("test", actual.getFieldLabel().getText());
    }

    @Test
    public void testSetSelectionType() {
        FileField actualField = (FileField)actual;
        assertSame(FileField.SelectionType.NonExistingFile, ((FileField)actual).getSelectionType());

        actualField.setSelectionType(FileField.SelectionType.ExistingFile);
        assertSame(FileField.SelectionType.ExistingFile, ((FileField)actual).getSelectionType());;
    }

    @Test
    public void testSetAllowBlank() {
        FileField actualField = (FileField)actual;
        assertTrue(actualField.isAllowBlankValues());
        assertTrue(actualField.isValid());

        actualField.setAllowBlankValues(false);
        assertFalse(actualField.isAllowBlankValues());
        assertFalse(actualField.isValid());
    }

    @Test
    public void testAddValueChangedListener() throws Exception {
        FileField actualField = (FileField)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);
        actualField.setFile(new File("1"));
        Thread.sleep(CoalescingDocumentListener.DELAY_MS*2); // cheesy!
        actualField.setFile(new File("2"));
        Thread.sleep(CoalescingDocumentListener.DELAY_MS*2); // cheesy!
        actualField.setFile(new File("2")); // shouldn't count as it isn't a value change
        Thread.sleep(CoalescingDocumentListener.DELAY_MS*2); // cheesy!
        Mockito.verify(listener, Mockito.times(2)).formFieldValueChanged(actual);
    }

    @Test
    public void testRemoveValueChangedListener() throws Exception {
        FileField actualField = (FileField)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);
        actual.removeValueChangedListener(listener);
        actualField.setFile(new File("1"));
        Thread.sleep(CoalescingDocumentListener.DELAY_MS*2); // cheesy!
        actualField.setFile(new File("2"));
        Thread.sleep(CoalescingDocumentListener.DELAY_MS*2); // cheesy!
        Mockito.verify(listener, Mockito.times(0)).formFieldValueChanged(actual);
    }

    @Test
    public void testSetEnabled_allComponents() {
        actual.setEnabled(false);
        JPanel wrapperPanel = (JPanel)actual.getFieldComponent();
        boolean foundTextField = false;
        boolean foundButton = false;
        for (Component component : wrapperPanel.getComponents()) {
            if (component instanceof JTextField) {
                foundTextField = true;
                assertFalse(component.isEnabled());
            }
            else if (component instanceof JButton) {
                foundButton = true;
                assertFalse(component.isEnabled());
            }
        }
        assertTrue(foundTextField);
        assertTrue(foundButton);

        foundTextField = false;
        foundButton = false;
        actual.setEnabled(true);
        for (Component component : wrapperPanel.getComponents()) {
            assertTrue(component.isEnabled());
            if (component instanceof JTextField) {
                foundTextField = true;
            }
            else if (component instanceof JButton) {
                foundButton = true;
            }
        }
        assertTrue(foundTextField);
        assertTrue(foundButton);
    }

    @Test
    public void validate_invalidScenario() {
        FileField actualField = (FileField)actual;
        actual.addFieldValidator(new TestValidator());
        actualField.setFile(new File("invalid"));
        assertFalse(actual.isValid());
        assertEquals(TestValidator.MSG, actual.getValidationLabel().getToolTipText());
    }

    @Test
    public void validate_validScenario() {
        actual.addFieldValidator(new TestValidator());
        ((FileField)actual).setFile(new File("hello.txt"));
        assertTrue(actual.isValid());
        assertNull(actual.getValidationLabel().getToolTipText());
    }

    private static class TestValidator implements FieldValidator<FileField> {

        public static final String MSG = "selected file must not be null and must be called hello.txt";

        @Override
        public ValidationResult validate(FileField fieldToValidate) {
            return fieldToValidate.getFile() != null && fieldToValidate.getFile().getName().equals("hello.txt")
                    ? ValidationResult.valid()
                    : ValidationResult.invalid(MSG);
        }
    }
}