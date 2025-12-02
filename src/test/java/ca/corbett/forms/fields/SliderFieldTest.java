package ca.corbett.forms.fields;

import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.ValidationResult;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SliderFieldTest extends FormFieldBaseTests {

    @Override
    protected FormField createTestObject() {
        return new SliderField("test", 0, 100, 25);
    }

    @Test
    public void changeListener_shouldFire() {
        SliderField actualField = (SliderField)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);
        actualField.setValue(99);
        actualField.setValue(88);
        actualField.setValue(77);
        Mockito.verify(listener, Mockito.times(3)).formFieldValueChanged(actual);
    }

    @Test
    public void validate_invalidScenario() {
        actual.addFieldValidator(new TestValidator());
        ((SliderField)actual).setValue(5);
        assertFalse(actual.isValid());
        assertEquals(TestValidator.MSG, actual.getValidationLabel().getToolTipText());
    }

    @Test
    public void validate_validScenario() {
        actual.addFieldValidator(new TestValidator());
        assertTrue(actual.isValid());
        assertNull(actual.getValidationLabel().getToolTipText());
    }

    private static class TestValidator implements FieldValidator<SliderField> {

        public static final String MSG = "Value too low!";

        @Override
        public ValidationResult validate(SliderField fieldToValidate) {
            return fieldToValidate.getValue() > 10
                    ? ValidationResult.valid()
                    : ValidationResult.invalid(MSG);
        }
    }
}