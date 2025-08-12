package ca.corbett.forms.fields;

import ca.corbett.extras.CoalescingDocumentListener;
import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.ValidationResult;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MultiLineTextFieldTest extends FormFieldBaseTests {
    @Override
    protected FormField createTestObject() {
        return TextField.ofFixedSizeMultiLine("label", 4, 10);
    }

    @Test
    public void testGetFieldComponent() {
        assertNotNull(actual.getFieldComponent());
        assertInstanceOf(JScrollPane.class, actual.getFieldComponent());
    }

    @Test
    public void testGetFieldLabel() {
        assertTrue(actual.hasFieldLabel());
        assertEquals("label", actual.getFieldLabel().getText());
    }

    @Test
    public void testIsMultiLine() {
        assertTrue(actual.isMultiLine());
    }

    @Test
    public void testShouldExpand() {
        assertFalse(actual.shouldExpand());
    }

    @Test
    public void testSetText() {
        TextField actualField = (TextField)actual;
        for (String value : List.of("One", "Two", "Three")) {
            actualField.setText(value);
            assertEquals(value, actualField.getText());
        }
    }

    @Test
    public void testRowColSizing() {
        TextField actualField = (TextField)actual;
        assertEquals(4, ((JTextArea)actualField.getTextComponent()).getRows());
        assertEquals(10, ((JTextArea)actualField.getTextComponent()).getColumns());
        assertFalse(actual.shouldExpand());
    }

    @Test
    public void testPixelSizing() {
        TextField field = TextField.ofFixedPixelSizeMultiLine("test", 222, 111);
        assertEquals(222, ((JScrollPane)field.getFieldComponent()).getPreferredSize().getWidth());
        assertEquals(111, ((JScrollPane)field.getFieldComponent()).getPreferredSize().getHeight());
        assertFalse(field.shouldExpand());
    }

    @Test
    public void testDynamicSizing() {
        TextField field = TextField.ofDynamicSizingMultiLine("test", 8);
        assertEquals(8, ((JTextArea)field.getTextComponent()).getRows());
        assertTrue(field.shouldExpand());
    }

    @Test
    public void testAddValueChangedListener() throws Exception {
        TextField actualField = (TextField)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);
        actualField.setText("Hello");
        Thread.sleep(CoalescingDocumentListener.DELAY_MS*2); // cheesy!
        actualField.setText("Hello again");
        Thread.sleep(CoalescingDocumentListener.DELAY_MS*2); // cheesy!
        actualField.setText("Hello again"); // shouldn't count as a change
        Thread.sleep(CoalescingDocumentListener.DELAY_MS*2); // cheesy!
        Mockito.verify(listener, Mockito.times(2)).formFieldValueChanged(actual);
    }

    @Test
    public void testRemoveValueChangedListener() throws Exception {
        TextField actualField = (TextField)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);
        actual.removeValueChangedListener(listener);
        actualField.setText("Hello");
        Thread.sleep(CoalescingDocumentListener.DELAY_MS*2); // cheesy!
        Mockito.verify(listener, Mockito.times(0)).formFieldValueChanged(actual);
    }

    @Test
    public void validate_invalidScenario() {
        actual.addFieldValidator(new TestValidator());
        assertFalse(actual.isValid());
        assertEquals(TestValidator.MSG, actual.getValidationLabel().getToolTipText());
    }

    @Test
    public void validate_validScenario() {
        actual.addFieldValidator(new TestValidator());
        ((TextField)actual).setText("Hello");
        assertTrue(actual.isValid());
        assertNull(actual.getValidationLabel().getToolTipText());
    }

    private static class TestValidator implements FieldValidator<TextField> {

        public static final String MSG = "text must say Hello";

        @Override
        public ValidationResult validate(TextField fieldToValidate) {
            return "Hello".equals(fieldToValidate.getText())
                    ? ValidationResult.valid()
                    : ValidationResult.invalid(MSG);
        }
    }
}
