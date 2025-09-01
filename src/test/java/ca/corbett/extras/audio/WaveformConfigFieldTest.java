package ca.corbett.extras.audio;

import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.FormFieldBaseTests;
import ca.corbett.forms.fields.ValueChangedListener;
import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.ValidationResult;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WaveformConfigFieldTest extends FormFieldBaseTests {

    @Override
    protected FormField createTestObject() {
        return new WaveformConfigField("test");
    }

    @Test
    public void testAddValueChangedListener() {
        WaveformConfigField actualField = (WaveformConfigField)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);
        actualField.setWidthLimit(WaveformConfigField.WidthLimit.SMALL);
        actualField.setWidthLimit(WaveformConfigField.WidthLimit.LARGE);
        actualField.setWidthLimit(WaveformConfigField.WidthLimit.LARGE);
        Mockito.verify(listener, Mockito.times(2)).formFieldValueChanged(actual);
    }

    @Test
    public void testRemoveValueChangedListener() {
        WaveformConfigField actualField = (WaveformConfigField)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);
        actual.removeValueChangedListener(listener);
        actualField.setWidthLimit(WaveformConfigField.WidthLimit.SMALL);
        actualField.setWidthLimit(WaveformConfigField.WidthLimit.LARGE);
        Mockito.verify(listener, Mockito.times(0)).formFieldValueChanged(actual);
    }

    @Test
    public void validate_invalidScenario() {
        WaveformConfigField actualField = (WaveformConfigField)actual;
        actual.addFieldValidator(new TestValidator());
        actualField.setBgColor(Color.GREEN);
        assertFalse(actual.isValid());
        assertEquals(TestValidator.MSG, actual.getValidationLabel().getToolTipText());
    }

    @Test
    public void validate_validScenario() {
        actual.addFieldValidator(new TestValidator());
        ((WaveformConfigField)actual).setBgColor(Color.BLUE);
        assertTrue(actual.isValid());
        assertNull(actual.getValidationLabel().getToolTipText());
    }

    private static class TestValidator implements FieldValidator<WaveformConfigField> {

        public static final String MSG = "Background must be blue";

        @Override
        public ValidationResult validate(WaveformConfigField fieldToValidate) {
            return Color.BLUE.equals(fieldToValidate.getBgColor())
                    ? ValidationResult.valid()
                    : ValidationResult.invalid(MSG);
        }
    }
}