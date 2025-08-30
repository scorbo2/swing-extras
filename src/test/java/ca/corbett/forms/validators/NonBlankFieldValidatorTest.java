package ca.corbett.forms.validators;

import ca.corbett.forms.fields.CheckBoxField;
import ca.corbett.forms.fields.LongTextField;
import ca.corbett.forms.fields.ShortTextField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NonBlankFieldValidatorTest {

    @Test
    public void validate_withNonTextField_shouldPass() {
        // GIVEN a field that is neither ShortTextField nor LongTextField:
        CheckBoxField testField = new CheckBoxField("test", true);
        testField.addFieldValidator(new NonBlankFieldValidator());

        // WHEN we try to validate it:
        assertTrue(testField.validate());

        // THEN we should see no validation error:
        assertNull(testField.getValidationLabel().getToolTipText());
    }

    @Test
    public void validate_withEmptyShortTextField_shouldFail() {
        // GIVEN an empty ShortTextField:
        ShortTextField testField = new ShortTextField("test", 10);
        testField.addFieldValidator(new NonBlankFieldValidator());

        // WHEN we validate it:
        assertFalse(testField.validate());

        // THEN we should see the expected error:
        assertEquals(NonBlankFieldValidator.MESSAGE, testField.getValidationLabel().getToolTipText());
    }

    @Test
    public void validate_withEmptyLongTextField_shouldFail() {
        // GIVEN an empty LongTextField:
        LongTextField testField = LongTextField.ofFixedSizeMultiLine("test", 10, 2);
        testField.addFieldValidator(new NonBlankFieldValidator());

        // WHEN we validate it:
        assertFalse(testField.validate());

        // THEN we should see the expected error:
        assertEquals(NonBlankFieldValidator.MESSAGE, testField.getValidationLabel().getToolTipText());
    }

    @Test
    public void validate_withNonEmptyShortTextField_shouldFail() {
        // GIVEN an empty ShortTextField:
        ShortTextField testField = new ShortTextField("test", 10);
        testField.setText("hello");
        testField.addFieldValidator(new NonBlankFieldValidator());

        // WHEN we validate it:
        assertTrue(testField.validate());

        // THEN we should see no validation error:
        assertNull(testField.getValidationLabel().getToolTipText());
    }

    @Test
    public void validate_withNonEmptyLongTextField_shouldFail() {
        // GIVEN an empty LongTextField:
        LongTextField testField = LongTextField.ofFixedSizeMultiLine("test", 10, 2);
        testField.setText("hello");
        testField.addFieldValidator(new NonBlankFieldValidator());

        // WHEN we validate it:
        assertTrue(testField.validate());

        // THEN we should see no validation error:
        assertNull(testField.getValidationLabel().getToolTipText());
    }
}