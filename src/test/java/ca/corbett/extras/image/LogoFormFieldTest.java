package ca.corbett.extras.image;

import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.FormFieldBaseTests;
import ca.corbett.forms.fields.ValueChangedListener;
import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.ValidationResult;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.awt.Color;
import java.awt.Font;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogoFormFieldTest extends FormFieldBaseTests {

    @Override
    protected FormField createTestObject() {
        return new LogoFormField("test");
    }

    @Test
    public void testAddValueChangedListener() {
        LogoFormField actualField = (LogoFormField)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);
        actualField.setSelectedFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        actualField.setBackgroundColor(Color.YELLOW);
        Mockito.verify(listener, Mockito.times(2)).formFieldValueChanged(actual);
    }

    @Test
    public void validate_invalidScenario() {
        actual.addFieldValidator(new TestValidator());
        assertFalse(actual.isValid());
        assertEquals(TestValidator.MSG, actual.getValidationLabel().getToolTipText());
    }

    @Test
    public void validate_validScenario() {
        LogoFormField actualField = (LogoFormField)actual;
        actual.addFieldValidator(new TestValidator());
        actualField.setBackgroundColor(Color.BLUE);
        assertTrue(actual.isValid());
        assertNull(actual.getValidationLabel().getToolTipText());
    }

    private static class TestValidator implements FieldValidator<LogoFormField> {

        public static final String MSG = "Background must be blue";

        @Override
        public ValidationResult validate(LogoFormField fieldToValidate) {
            return Color.BLUE.equals(fieldToValidate.getBackgroundColor())
                    ? ValidationResult.valid()
                    : ValidationResult.invalid(MSG);
        }
    }
}