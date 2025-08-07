package ca.corbett.forms.fields;

import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.ValidationResult;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.swing.JCheckBox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheckBoxFieldTest extends FormFieldBaseTests {

    @Override
    protected FormField createTestObject() {
        return new CheckBoxField("test", true);
    }

    @Test
    public void testGetFieldComponent() {
        assertNotNull(actual.getFieldComponent());
        assertInstanceOf(JCheckBox.class, actual.getFieldComponent());
    }

    @Test
    public void testGetFieldLabel() {
        assertFalse(actual.hasFieldLabel());
        assertTrue(actual.getFieldLabel().getText().isBlank());
    }

    @Test
    public void testCheck() {
        CheckBoxField actualField = (CheckBoxField)actual;
        assertTrue(actualField.isChecked());
        assertTrue(((JCheckBox)actual.getFieldComponent()).isSelected());
        actualField.setChecked(false);
        assertFalse(actualField.isChecked());
        assertFalse(((JCheckBox)actual.getFieldComponent()).isSelected());
    }

    @Test
    public void testAddValueChangedListener() {
        CheckBoxField actualField = (CheckBoxField)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);
        actualField.setChecked(false);
        actualField.setChecked(true);
        actualField.setChecked(true); // shouldn't count as it isn't a value change
        Mockito.verify(listener, Mockito.times(2)).formFieldValueChanged(actual);
    }

    @Test
    public void testRemoveValueChangedListener() {
        CheckBoxField actualField = (CheckBoxField)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);
        actual.removeValueChangedListener(listener);
        actualField.setChecked(false);
        actualField.setChecked(true);
        Mockito.verify(listener, Mockito.times(0)).formFieldValueChanged(actual);
    }

    @Test
    public void validate_invalidScenario() {
        CheckBoxField actualField = (CheckBoxField)actual;
        actual.addFieldValidator(new TestValidator());
        actualField.setChecked(false);
        assertFalse(actual.isValid());
        assertEquals(TestValidator.MSG, actual.getValidationLabel().getToolTipText());
    }

    @Test
    public void validate_validScenario() {
        actual.addFieldValidator(new TestValidator());
        assertTrue(actual.isValid());
        assertNull(actual.getValidationLabel().getToolTipText());
    }

    private static class TestValidator extends FieldValidator<CheckBoxField> {

        public static final String MSG = "Validation message";

        @Override
        public ValidationResult validate(CheckBoxField fieldToValidate) {
            return fieldToValidate.isChecked() ? ValidationResult.valid() : ValidationResult.invalid(MSG);
        }
    }
}