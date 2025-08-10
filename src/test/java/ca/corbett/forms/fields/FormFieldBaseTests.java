package ca.corbett.forms.fields;

import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
        assertNull(actual.getValidationLabel().getToolTipText());
    }

    @Test
    public void testClearValidationResults() {
        actual.addFieldValidator(new AlwaysFalseValidator());
        assertFalse(actual.isValid());
        actual.clearValidationResults();
        assertNull(actual.getValidationLabel().getToolTipText());
    }

    @Test
    public void testRemoveAllFieldValidators() {
        actual.addFieldValidator(new AlwaysFalseValidator());
        actual.removeAllFieldValidators();
        assertTrue(actual.isValid());
        assertNull(actual.getValidationLabel().getToolTipText());
    }

    @Test
    public void testRemoveFieldValidator() {
        FieldValidator<FormField> validator = new AlwaysFalseValidator();
        actual.addFieldValidator(validator);
        actual.removeFieldValidator(validator);
        assertTrue(actual.isValid());
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

    @Test
    public void testLayout() {
        FormPanel formPanel = new FormPanel(Alignment.CENTER);
        formPanel.add(actual);
        assertInstanceOf(GridBagLayout.class, formPanel.getLayout());

        assertExpectedComponentCount(actual, formPanel);

        GridBagLayout layout = (GridBagLayout)formPanel.getLayout();
        GridBagConstraints gbc;
        final int expectedRow = 1; // row 0 is the header margin row, after that are form fields
        final int SINGLE_WIDTH = 1;
        final int DOUBLE_WIDTH = 2;
        int componentIndex = 2; // skip top and left margin labels
        if (actual.hasFieldLabel()) {
            gbc = layout.getConstraints(formPanel.getComponent(componentIndex++));
            assertComponentLayoutProperties(gbc, expectedRow, FormPanel.LABEL_COLUMN, SINGLE_WIDTH);
            if (actual.getFieldComponent() != null) {
                gbc = layout.getConstraints(formPanel.getComponent(componentIndex++));
                assertComponentLayoutProperties(gbc, expectedRow, FormPanel.CONTROL_COLUMN, SINGLE_WIDTH);
            }
        }
        else if (actual.getFieldComponent() != null) {
            gbc = layout.getConstraints(formPanel.getComponent(componentIndex++));
            assertComponentLayoutProperties(gbc, expectedRow, FormPanel.FORM_FIELD_START_COLUMN, DOUBLE_WIDTH);
        }

        if (actual.hasHelpLabel()) {
            gbc = layout.getConstraints(formPanel.getComponent(componentIndex++));
            assertComponentLayoutProperties(gbc, expectedRow, FormPanel.HELP_COLUMN, SINGLE_WIDTH);
        }

        if (actual.hasValidationLabel()) {
            gbc = layout.getConstraints(formPanel.getComponent(componentIndex));
            assertComponentLayoutProperties(gbc, expectedRow, FormPanel.VALIDATION_COLUMN, SINGLE_WIDTH);
        }
    }

    protected static void assertExpectedComponentCount(FormField field, FormPanel panel) {
        // The FormPanel has a margin label on each edge: top, right, bottom, left
        int expectedComponentCount = 4;

        // The actual number of components added by the field will vary by field type:
        if (field.hasFieldLabel()) {
            expectedComponentCount++;
        }
        if (field.getFieldComponent() != null) {
            expectedComponentCount++;
        }
        if (field.hasHelpLabel()) {
            expectedComponentCount++;
        }
        if (field.hasValidationLabel()) {
            expectedComponentCount++;
        }

        // But we should have an exact count of it:
        assertEquals(expectedComponentCount, panel.getComponentCount());
    }

    protected static void assertComponentLayoutProperties(GridBagConstraints gbc,
                                                          final int expectedRow,
                                                          final int expectedCol,
                                                          final int expectedGridWidth) {
        assertEquals(expectedCol, gbc.gridx);
        assertEquals(expectedRow, gbc.gridy);
        assertEquals(expectedGridWidth, gbc.gridwidth);
    }

    protected static class AlwaysFalseValidator implements FieldValidator<FormField> {

        @Override
        public ValidationResult validate(FormField fieldToValidate) {
            return ValidationResult.invalid("invalid");
        }
    }
}
