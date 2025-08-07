package ca.corbett.forms.fields;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}
