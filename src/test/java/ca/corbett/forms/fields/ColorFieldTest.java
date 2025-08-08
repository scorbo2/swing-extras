package ca.corbett.forms.fields;

import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.ValidationResult;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.swing.JPanel;
import java.awt.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColorFieldTest extends FormFieldBaseTests {

    @Override
    protected FormField createTestObject() {
        return new ColorField("Test", Color.BLUE);
    }

    @Test
    public void testGetFieldComponent() {
        assertNotNull(actual.getFieldComponent());
        assertInstanceOf(JPanel.class, actual.getFieldComponent());
    }

    @Test
    public void testGetFieldLabel() {
        assertTrue(actual.hasFieldLabel());
        assertEquals("Test", actual.getFieldLabel().getText());
    }

    @Test
    public void testSetColor() {
        ColorField actualField = (ColorField)actual;
        assertEquals(Color.BLUE, actualField.getColor());
        assertEquals(Color.BLUE, actualField.getFieldComponent().getBackground());

        actualField.setColor(Color.RED);
        assertEquals(Color.RED, actualField.getColor());
        assertEquals(Color.RED, actualField.getFieldComponent().getBackground());

        // setColor(null) should be ignored
        actualField.setColor(null);
        assertEquals(Color.RED, actualField.getColor());
        assertEquals(Color.RED, actualField.getFieldComponent().getBackground());
    }

    @Test
    public void testAddValueChangedListener() {
        ColorField actualField = (ColorField)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);
        actualField.setColor(Color.GREEN);
        actualField.setColor(Color.YELLOW);
        actualField.setColor(Color.YELLOW); // shouldn't count as it isn't a value change
        Mockito.verify(listener, Mockito.times(2)).formFieldValueChanged(actual);
    }

    @Test
    public void testRemoveValueChangedListener() {
        ColorField actualField = (ColorField)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);
        actual.removeValueChangedListener(listener);
        actualField.setColor(Color.GREEN);
        actualField.setColor(Color.RED);
        Mockito.verify(listener, Mockito.times(0)).formFieldValueChanged(actual);
    }

    @Test
    public void validate_invalidScenario() {
        ColorField actualField = (ColorField)actual;
        actual.addFieldValidator(new ColorFieldTest.TestValidator());
        actualField.setColor(Color.GREEN);
        assertFalse(actual.isValid());
        assertEquals(ColorFieldTest.TestValidator.MSG, actual.getValidationLabel().getToolTipText());
    }

    @Test
    public void validate_validScenario() {
        actual.addFieldValidator(new ColorFieldTest.TestValidator());
        ((ColorField)actual).setColor(Color.WHITE);
        assertTrue(actual.isValid());
        assertNull(actual.getValidationLabel().getToolTipText());
    }

    private static class TestValidator implements FieldValidator<ColorField> {

        public static final String MSG = "Color must be white";

        @Override
        public ValidationResult validate(ColorField fieldToValidate) {
            return Color.WHITE.equals(fieldToValidate.getColor())
                    ? ValidationResult.valid()
                    : ValidationResult.invalid(MSG);
        }
    }

}