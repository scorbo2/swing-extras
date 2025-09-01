package ca.corbett.forms.fields;

import ca.corbett.extras.CoalescingDocumentListener;
import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.ValidationResult;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.swing.JTextField;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShortTextFieldTest extends FormFieldBaseTests {

    @Override
    protected FormField createTestObject() {
        return new ShortTextField("label", 10).setText("value");
    }

    @Test
    public void testGetFieldComponent() {
        assertNotNull(actual.getFieldComponent());
        assertInstanceOf(JTextField.class, actual.getFieldComponent());
    }

    @Test
    public void testGetFieldLabel() {
        assertTrue(actual.hasFieldLabel());
        assertEquals("label", actual.getFieldLabel().getText());
    }

    @Test
    public void testIsMultiLine() {
        assertFalse(actual.isMultiLine());
    }

    @Test
    public void testShouldExpand() {
        assertFalse(actual.shouldExpand());
    }

    @Test
    public void testSetText() {
        ShortTextField actualField = (ShortTextField)actual;
        for (String value : List.of("One", "Two", "Three")) {
            actualField.setText(value);
            assertEquals(value, actualField.getText());
        }
    }

    @Test
    public void testGetColumns() {
        assertEquals(10, ((JTextField)actual.getFieldComponent()).getColumns());
    }

    @Test
    public void testAddValueChangedListener() throws Exception {
        ShortTextField actualField = (ShortTextField)actual;
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
        ShortTextField actualField = (ShortTextField)actual;
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
        ((ShortTextField)actual).setText("Hello");
        assertTrue(actual.isValid());
        assertNull(actual.getValidationLabel().getToolTipText());
    }

    private static class TestValidator implements FieldValidator<ShortTextField> {

        public static final String MSG = "text must say Hello";

        @Override
        public ValidationResult validate(ShortTextField fieldToValidate) {
            return "Hello".equals(fieldToValidate.getText())
                    ? ValidationResult.valid()
                    : ValidationResult.invalid(MSG);
        }
    }
}