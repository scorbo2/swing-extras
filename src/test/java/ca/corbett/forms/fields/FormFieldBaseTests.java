package ca.corbett.forms.fields;

import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class FormFieldBaseTests {

    protected FormField actual;

    @BeforeEach
    public void setup() {
        actual = createTestObject();
    }

    protected abstract FormField createTestObject();

    @Test
    public void testSetEnabled() {
        assertTrue(actual.isEnabled());
        assertTrue(actual.getFieldComponent().isEnabled());
        if (actual.hasFieldLabel()) {
            assertTrue(actual.getFieldLabel().isEnabled());
        }
        if (actual.hasHelpLabel()) {
            assertTrue(actual.getHelpLabel().isEnabled());
        }
        if (actual.hasValidationLabel()) {
            assertTrue(actual.getValidationLabel().isEnabled());
        }

        actual.setEnabled(false);
        assertFalse(actual.isEnabled());
        assertFalse(actual.getFieldComponent().isEnabled());
        if (actual.hasFieldLabel()) {
            assertFalse(actual.getFieldLabel().isEnabled());
        }
        if (actual.hasHelpLabel()) {
            assertFalse(actual.getHelpLabel().isEnabled());
        }
        if (actual.hasValidationLabel()) {
            assertFalse(actual.getValidationLabel().isEnabled());
        }

        actual.setEnabled(true);
        assertTrue(actual.isEnabled());
        assertTrue(actual.getFieldComponent().isEnabled());
        if (actual.hasFieldLabel()) {
            assertTrue(actual.getFieldLabel().isEnabled());
        }
        if (actual.hasHelpLabel()) {
            assertTrue(actual.getHelpLabel().isEnabled());
        }
        if (actual.hasValidationLabel()) {
            assertTrue(actual.getValidationLabel().isEnabled());
        }
    }

    @Test
    public void testSetVisible() {
        assertTrue(actual.isVisible());
        assertTrue(actual.getFieldComponent().isVisible());
        if (actual.hasFieldLabel()) {
            assertTrue(actual.getFieldLabel().isVisible());
        }
        if (actual.hasHelpLabel()) {
            assertTrue(actual.getHelpLabel().isVisible());
        }
        if (actual.hasValidationLabel()) {
            assertTrue(actual.getValidationLabel().isVisible());
        }

        actual.setVisible(false);
        assertFalse(actual.isVisible());
        assertFalse(actual.getFieldComponent().isVisible());
        if (actual.hasFieldLabel()) {
            assertFalse(actual.getFieldLabel().isVisible());
        }
        if (actual.hasHelpLabel()) {
            assertFalse(actual.getHelpLabel().isVisible());
        }
        if (actual.hasValidationLabel()) {
            assertFalse(actual.getValidationLabel().isVisible());
        }

        actual.setVisible(true);
        assertTrue(actual.isVisible());
        assertTrue(actual.getFieldComponent().isVisible());
        if (actual.hasFieldLabel()) {
            assertTrue(actual.getFieldLabel().isVisible());
        }
        if (actual.hasHelpLabel()) {
            assertTrue(actual.getHelpLabel().isVisible());
        }
        if (actual.hasValidationLabel()) {
            assertTrue(actual.getValidationLabel().isVisible());
        }
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
    public void testClearValidationResults() {
        actual.addFieldValidator(new AlwaysFalseValidator());
        assertFalse(actual.isValid());
        actual.clearValidationResults();
        assertNull(actual.getValidationLabel().getToolTipText());
    }

    @Test
    public void testExtraAttributes() {
        assertNull(actual.getExtraAttribute("test"));
        actual.setExtraAttribute("test", "value");
        assertEquals("value", actual.getExtraAttribute("test"));
        actual.clearExtraAttribute("test");
        assertNull(actual.getExtraAttribute("test"));
    }

    @Test
    public void testSetAllExtraAttributes() {
        Map<String, Object> values = new HashMap<>();
        values.put("test1", "value1");
        values.put("test2", "value2");
        values.put("test3", "value3");

        actual.setAllExtraAttributes(values);
        assertEquals("value1", actual.getExtraAttribute("test1"));
        assertEquals("value2", actual.getExtraAttribute("test2"));
        assertEquals("value3", actual.getExtraAttribute("test3"));

        actual.clearExtraAttribute("test2");
        assertEquals("value1", actual.getExtraAttribute("test1"));
        assertNull(actual.getExtraAttribute("test2"));
        assertEquals("value3", actual.getExtraAttribute("test3"));

        actual.clearExtraAttributes();
        assertNull(actual.getExtraAttribute("test1"));
        assertNull(actual.getExtraAttribute("test2"));
        assertNull(actual.getExtraAttribute("test3"));
    }

    protected static class AlwaysFalseValidator implements FieldValidator<FormField> {

        @Override
        public ValidationResult validate(FormField fieldToValidate) {
            return ValidationResult.invalid("invalid");
        }
    }
}
