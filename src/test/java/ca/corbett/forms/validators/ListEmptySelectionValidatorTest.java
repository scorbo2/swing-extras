package ca.corbett.forms.validators;

import ca.corbett.forms.fields.CheckBoxField;
import ca.corbett.forms.fields.ListField;
import ca.corbett.forms.fields.ListSubsetField;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListEmptySelectionValidatorTest {

    @Test
    public void validate_withEmptyListField_shouldFail() {
        // GIVEN an empty ListField with nothing selected:
        ListField<String> testField = new ListField<>("test", List.of("One", "Two", "Three"));
        testField.addFieldValidator(new ListEmptySelectionValidator());

        // WHEN we validate it:
        boolean result = testField.validate();

        // THEN validation should fail with the expected message:
        assertFalse(result);
        assertEquals(ListEmptySelectionValidator.MESSAGE, testField.getValidationLabel().getToolTipText());
    }

    @Test
    public void validate_withEmptyListSubsetField_shouldFail() {
        // GIVEN an empty ListSubsetField with nothing selected:
        ListSubsetField<String> testField = new ListSubsetField<>("test", List.of("One", "Two", "Three"));
        testField.addFieldValidator(new ListEmptySelectionValidator());

        // WHEN we validate it:
        boolean result = testField.validate();

        // THEN validation should fail with the expected message:
        assertFalse(result);
        assertEquals(ListEmptySelectionValidator.MESSAGE, testField.getValidationLabel().getToolTipText());
    }

    @Test
    public void validate_withNonEmptyListField_shouldValidate() {
        // GIVEN a ListField with an item selected:
        ListField<String> testField = new ListField<>("test", List.of("One", "Two", "Three"));
        testField.setSelectedIndexes(new int[]{0}); // Select "One"
        testField.addFieldValidator(new ListEmptySelectionValidator());

        // WHEN we validate it:
        boolean result = testField.validate();

        // THEN validation should pass:
        assertTrue(result);
    }

    @Test
    public void validate_withNonEmptyListSubsetField_shouldValidate() {
        // GIVEN a ListSubsetField with an item selected:
        ListSubsetField<String> testField = new ListSubsetField<>("test", List.of("One", "Two", "Three"));
        testField.selectIndexes(new int[]{1}); // Select "Two"
        testField.addFieldValidator(new ListEmptySelectionValidator());

        // WHEN we validate it:
        boolean result = testField.validate();

        // THEN validation should pass:
        assertTrue(result);
    }

    @Test
    public void validate_withNonListField_shouldPass() {
        // GIVEN a non-list field (e.g., a simple FormField):
        CheckBoxField testField = new CheckBoxField("test", true);
        testField.addFieldValidator(new ListEmptySelectionValidator());

        // WHEN we validate it:
        boolean result = testField.validate();

        // THEN validation should pass since the validator is not applicable:
        assertTrue(result);
    }
}