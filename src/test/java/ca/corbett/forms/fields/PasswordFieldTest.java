package ca.corbett.forms.fields;

import ca.corbett.extras.CoalescingDocumentListener;
import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.ValidationResult;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.swing.JPanel;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordFieldTest extends FormFieldBaseTests {

    @Override
    protected FormField createTestObject() {
        return new PasswordField("label", 10).setPassword("password");
    }

    @Test
    public void testGetFieldComponent() {
        assertNotNull(actual.getFieldComponent());
        assertInstanceOf(JPanel.class, actual.getFieldComponent()); // it's a wrapper panel
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
        PasswordField actualField = (PasswordField)actual;
        for (String value : List.of("One", "Two", "Three")) {
            actualField.setPassword(value);
            assertEquals(value, actualField.getPassword());
        }
    }

    @Test
    public void testAddValueChangedListener() throws Exception {
        PasswordField actualField = (PasswordField)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);
        actualField.setPassword("Hello");
        Thread.sleep(CoalescingDocumentListener.DELAY_MS * 2); // cheesy!
        actualField.setPassword("Hello again");
        Thread.sleep(CoalescingDocumentListener.DELAY_MS * 2); // cheesy!
        actualField.setPassword("Hello again"); // shouldn't count as a change
        Thread.sleep(CoalescingDocumentListener.DELAY_MS * 2); // cheesy!
        Mockito.verify(listener, Mockito.times(2)).formFieldValueChanged(actual);
    }

    @Test
    public void testRemoveValueChangedListener() throws Exception {
        PasswordField actualField = (PasswordField)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);
        actual.removeValueChangedListener(listener);
        actualField.setPassword("Hello");
        Thread.sleep(CoalescingDocumentListener.DELAY_MS * 2); // cheesy!
        Mockito.verify(listener, Mockito.times(0)).formFieldValueChanged(actual);
    }

    @Test
    public void validate_invalidScenario() {
        actual.addFieldValidator(new TestValidator());
        ((PasswordField)actual).setPassword("password");
        assertFalse(actual.isValid());
        assertEquals(TestValidator.MSG, actual.getValidationLabel().getToolTipText());
    }

    @Test
    public void validate_withBlankValueAndNoBlanksAllowed_shouldFail() {
        ((PasswordField)actual).setAllowBlank(false);
        ((PasswordField)actual).setPassword("");
        assertFalse(actual.isValid());
    }

    @Test
    public void validate_withBlankValueAndBlanksAllowed_shouldPass() {
        ((PasswordField)actual).setAllowBlank(true);
        ((PasswordField)actual).setPassword("");
        assertTrue(actual.isValid());
    }

    @Test
    public void validate_validScenario() {
        actual.addFieldValidator(new TestValidator());
        ((PasswordField)actual).setPassword("Super secure password that will never get hacked");
        assertTrue(actual.isValid());
        assertNull(actual.getValidationLabel().getToolTipText());
    }

    private static class TestValidator implements FieldValidator<PasswordField> {

        public static final String MSG = "password can't be \"password\"";

        @Override
        public ValidationResult validate(PasswordField fieldToValidate) {
            return "password".equals(fieldToValidate.getPassword())
                    ? ValidationResult.invalid(MSG)
                    : ValidationResult.valid();
        }
    }
}