package ca.corbett.forms.fields;

import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.ValidationResult;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.swing.JComboBox;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComboFieldTest extends FormFieldBaseTests {

    @Override
    protected FormField createTestObject() {
        return new ComboField<>("test", List.of("One", "Two", "Three"), 0);
    }

    @Test
    public void testGetComponent() {
        assertNotNull(actual.getFieldComponent());
        assertInstanceOf(JComboBox.class, actual.getFieldComponent());
    }

    @Test
    public void testGetFieldLabel() {
        assertTrue(actual.hasFieldLabel());
        assertEquals("test", actual.getFieldLabel().getText());
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
    public void testSetSelectedIndex() {
        ComboField<?> actualCombo = (ComboField<?>)actual;
        assertEquals(0, actualCombo.getSelectedIndex());

        actualCombo.setSelectedIndex(2);
        assertEquals(2, actualCombo.getSelectedIndex());

        actualCombo.setSelectedIndex(999);
        assertEquals(2, actualCombo.getSelectedIndex());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSetSelectedItem() {
        ComboField<String> actualCombo = (ComboField<String>)actual;
        assertEquals("One", actualCombo.getSelectedItem());

        actualCombo.setSelectedItem("Three");
        assertEquals("Three", actualCombo.getSelectedItem());

        actualCombo.setSelectedItem("Flibberty gee");
        assertEquals("Three", actualCombo.getSelectedItem());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSetOptions() {
        ComboField<String> actualCombo = (ComboField<String>)actual;
        actualCombo.setOptions(List.of("1","2","3","4","5"), 4);
        assertEquals(5, actualCombo.getItemCount());
    }

    @Test
    public void testHasValidationLabel() {
        // ComboField has no validation label unless FieldValidators are explicitly assigned
        assertFalse(actual.hasValidationLabel());
    }

    @Test
    public void testAddValueChangedListener() {
        ComboField<?> actualField = (ComboField<?>)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);
        actualField.setSelectedIndex(1);
        actualField.setSelectedIndex(2);
        actualField.setSelectedIndex(2); // shouldn't count as it isn't a value change
        Mockito.verify(listener, Mockito.times(2)).formFieldValueChanged(actual);
    }

    @Test
    public void testRemoveValueChangedListener() {
        ComboField<?> actualField = (ComboField<?>)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);
        actual.removeValueChangedListener(listener);
        actualField.setSelectedIndex(1);
        actualField.setSelectedIndex(2);
        Mockito.verify(listener, Mockito.times(0)).formFieldValueChanged(actual);
    }

    @Test
    public void validate_invalidScenario() {
        ComboField<?> actualField = (ComboField<?>)actual;
        actual.addFieldValidator(new TestValidator());
        assertTrue(actual.hasValidationLabel());
        actualField.setSelectedIndex(1);
        assertFalse(actual.isValid());
        assertEquals(TestValidator.MSG, actual.getValidationLabel().getToolTipText());
    }

    @Test
    public void validate_validScenario() {
        actual.addFieldValidator(new TestValidator());
        assertTrue(actual.isValid());
        assertNull(actual.getValidationLabel().getToolTipText());
    }

    private static class TestValidator implements FieldValidator<ComboField<?>> {

        public static final String MSG = "selected index must be 0";

        @Override
        public ValidationResult validate(ComboField<?> fieldToValidate) {
            return fieldToValidate.getSelectedIndex() == 0
                    ? ValidationResult.valid()
                    : ValidationResult.invalid(MSG);
        }
    }
}
