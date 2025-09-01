package ca.corbett.extras.image;

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

class ImagePanelFormFieldTest extends FormFieldBaseTests {

    @Override
    protected FormField createTestObject() {
        return new ImagePanelFormField("test");
    }

    @Test
    public void testAddValueChangedListener() {
        ImagePanelFormField actualField = (ImagePanelFormField)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);
        actualField.setBgColor(Color.GREEN);
        actualField.setRenderQuality(ImagePanelConfig.Quality.QUICK_AND_DIRTY);
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
        ImagePanelFormField actualField = (ImagePanelFormField)actual;
        actual.addFieldValidator(new TestValidator());
        actualField.setBgColor(Color.BLUE);
        assertTrue(actual.isValid());
        assertNull(actual.getValidationLabel().getToolTipText());
    }

    private static class TestValidator implements FieldValidator<ImagePanelFormField> {

        public static final String MSG = "Background must be blue";

        @Override
        public ValidationResult validate(ImagePanelFormField fieldToValidate) {
            return Color.BLUE.equals(fieldToValidate.getBgColor())
                    ? ValidationResult.valid()
                    : ValidationResult.invalid(MSG);
        }
    }
}