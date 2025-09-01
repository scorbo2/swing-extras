package ca.corbett.extras.audio;

import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.FormFieldBaseTests;
import ca.corbett.forms.fields.ValueChangedListener;
import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.ValidationResult;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WaveformPanelFormFieldTest extends FormFieldBaseTests {

    @Override
    protected FormField createTestObject() {
        return new WaveformPanelFormField("test");
    }

    @Test
    public void testAddValueChangedListener() {
        WaveformPanelFormField actualField = (WaveformPanelFormField)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);
        actualField.setControlPosition(AudioWaveformPanel.ControlPosition.BOTTOM_RIGHT);
        actualField.setControlPosition(AudioWaveformPanel.ControlPosition.BOTTOM_CENTER);
        actualField.setControlPosition(AudioWaveformPanel.ControlPosition.BOTTOM_CENTER);
        Mockito.verify(listener, Mockito.times(2)).formFieldValueChanged(actual);
    }

    @Test
    public void testRemoveValueChangedListener() {
        WaveformPanelFormField actualField = (WaveformPanelFormField)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);
        actual.removeValueChangedListener(listener);
        actualField.setControlPosition(AudioWaveformPanel.ControlPosition.BOTTOM_RIGHT);
        actualField.setControlPosition(AudioWaveformPanel.ControlPosition.BOTTOM_CENTER);
        Mockito.verify(listener, Mockito.times(0)).formFieldValueChanged(actual);
    }

    @Test
    public void validate_invalidScenario() {
        WaveformPanelFormField actualField = (WaveformPanelFormField)actual;
        actual.addFieldValidator(new TestValidator());
        actualField.setControlSize(AudioWaveformPanel.ControlSize.XSMALL);
        assertFalse(actual.isValid());
        assertEquals(TestValidator.MSG, actual.getValidationLabel().getToolTipText());
    }

    @Test
    public void validate_validScenario() {
        actual.addFieldValidator(new TestValidator());
        ((WaveformPanelFormField)actual).setControlSize(AudioWaveformPanel.ControlSize.XLARGE);
        assertTrue(actual.isValid());
        assertNull(actual.getValidationLabel().getToolTipText());
    }

    private static class TestValidator implements FieldValidator<WaveformPanelFormField> {

        public static final String MSG = "Controls must be extra large";

        @Override
        public ValidationResult validate(WaveformPanelFormField fieldToValidate) {
            return AudioWaveformPanel.ControlSize.XLARGE == fieldToValidate.getControlSize()
                    ? ValidationResult.valid()
                    : ValidationResult.invalid(MSG);
        }
    }
}