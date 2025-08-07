package ca.corbett.forms.fields;

import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.swing.JCheckBox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheckBoxFieldTest {

    private CheckBoxField actual;

    @BeforeEach
    public void setup() {
        actual = new CheckBoxField("test", true);
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
        assertTrue(actual.isChecked());
        assertTrue(((JCheckBox)actual.getFieldComponent()).isSelected());
        actual.setChecked(false);
        assertFalse(actual.isChecked());
        assertFalse(((JCheckBox)actual.getFieldComponent()).isSelected());
    }

    @Test
    public void testAddValueChangedListener() {
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);
        actual.setChecked(false);
        actual.setChecked(true);
        actual.setChecked(true); // shouldn't count as it isn't a value change
        Mockito.verify(listener, Mockito.times(2)).formFieldValueChanged(actual);
    }

    @Test
    public void testRemoveValueChangedListener() {
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);
        actual.removeValueChangedListener(listener);
        actual.setChecked(false);
        actual.setChecked(true);
        Mockito.verify(listener, Mockito.times(0)).formFieldValueChanged(actual);

    }

    @Test
    public void testSetIdentifier() {
        assertNull(actual.getIdentifier());
        actual.setIdentifier("Hello");
        assertEquals("Hello", actual.getIdentifier());
        actual.setIdentifier(null);
        assertNull(actual.getIdentifier());
    }

    @Test
    public void testHelpLabel() {
        assertFalse(actual.hasHelpLabel());
        assertNull(actual.getHelpLabel().getToolTipText());
        actual.setHelpText("Hello there");
        assertTrue(actual.hasHelpLabel());
        assertEquals("Hello there", actual.getHelpLabel().getToolTipText());
    }

    @Test
    public void validate_withNoValidators_shouldBeValid() {
        assertTrue(actual.isValid());
    }

    @Test
    public void validate_invalidScenario() {
        actual.addFieldValidator(new TestValidator());
        actual.setChecked(false);
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