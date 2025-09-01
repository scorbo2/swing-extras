package ca.corbett.forms.fields;

import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.ValidationResult;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.swing.JSpinner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NumberFieldTest extends FormFieldBaseTests {

    @Override
    protected FormField createTestObject() {
        return new NumberField("label", 0,0,10,1);
    }

    @Test
    public void testGetFieldComponent() {
        assertNotNull(actual.getFieldComponent());
        assertInstanceOf(JSpinner.class, actual.getFieldComponent());
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
    public void testSetCurrentValue_withValidValues_shouldSet() {
        NumberField actualField = (NumberField)actual;
        assertEquals(0, actualField.getCurrentValue());

        for (int value = 10; value > 0; value--) {
            actualField.setCurrentValue(value);
            assertEquals(value, actualField.getCurrentValue());
        }
    }

    @Test
    public void testSetCurrentValue_withInvalidValues_shouldIgnore() {
        NumberField actualField = (NumberField)actual;
        actualField.setCurrentValue(2); // valid
        actualField.setCurrentValue(null); // null should be ignored
        assertEquals(2, actualField.getCurrentValue());
    }

    @Test
    public void testAddValueChangedListener() {
        NumberField actualField = (NumberField)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);
        actualField.setCurrentValue(1);
        actualField.setCurrentValue(2);
        actualField.setCurrentValue(2); // shouldn't count as a change
        Mockito.verify(listener, Mockito.times(2)).formFieldValueChanged(actual);
    }

    @Test
    public void testRemoveValueChangedListener() {
        NumberField actualField = (NumberField)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);
        actual.removeValueChangedListener(listener);
        actualField.setCurrentValue(3);
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
        ((NumberField)actual).setCurrentValue(3);
        assertTrue(actual.isValid());
        assertNull(actual.getValidationLabel().getToolTipText());
    }

    private static class TestValidator implements FieldValidator<NumberField> {

        public static final String MSG = "current value must be 3";

        @Override
        public ValidationResult validate(NumberField fieldToValidate) {
            return Integer.valueOf(3).equals(fieldToValidate.getCurrentValue())
                    ? ValidationResult.valid()
                    : ValidationResult.invalid(MSG);
        }
    }
}