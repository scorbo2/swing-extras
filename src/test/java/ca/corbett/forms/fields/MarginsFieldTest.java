package ca.corbett.forms.fields;

import ca.corbett.forms.Margins;
import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.ValidationResult;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import java.awt.Component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

class MarginsFieldTest extends FormFieldBaseTests {

    @Override
    protected FormField createTestObject() {
        return new MarginsField("Test Margins");
    }

    @Test
    public void setHeaderLabel_withEmptyString_shouldReturnNull() {
        // GIVEN a MarginsField:
        MarginsField field = new MarginsField("Test Margins");

        // WHEN we set the header label to an empty string:
        field.setHeaderLabel("");

        // THEN we should get back null when we call getHeaderLabel():
        assertNull(field.getHeaderLabel());
    }

    @Test
    public void getFieldComponent_shouldReturnJPanel() {
        // GIVEN a MarginsField:
        MarginsField field = new MarginsField("Test Margins");

        // WHEN we call getFieldComponent():
        JComponent component = field.getFieldComponent();

        // THEN we should get back a non-null JPanel:
        assertNotNull(component);
        assertInstanceOf(JPanel.class, component);
    }

    @Test
    public void isMultiLine_shouldReturnTrue() {
        // GIVEN a MarginsField:
        MarginsField field = new MarginsField("Test Margins");

        // WHEN we call isMultiLine():
        boolean multiLine = field.isMultiLine();

        // THEN we should always get back true:
        assertTrue(multiLine);
    }

    @Test
    public void addChangeListener_withChangedValue_shouldBeNotified() {
        // GIVEN a MarginsField and a mock ChangeListener:
        MarginsField field = new MarginsField("Test Margins");
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        field.addValueChangedListener(listener);

        // WHEN we change the value of the field:
        field.getMarginsObject().setLeft(10); // This should trigger a change event.
        field.getMarginsObject().setRight(10); // This should trigger a change event.

        // THEN the listener should have been notified:
        Mockito.verify(listener, Mockito.times(2)).formFieldValueChanged(any());
    }

    @Test
    public void removeChangeListener_withChangedValue_shouldNotBeNotified() {
        // GIVEN a MarginsField and a mock ChangeListener that we will remove:
        MarginsField field = new MarginsField("Test Margins");
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        field.addValueChangedListener(listener);

        // WHEN we remove the listener and then change the value of the field:
        field.removeValueChangedListener(listener);
        field.getMarginsObject().setLeft(10); // This should NOT trigger a change event.
        field.getMarginsObject().setRight(10); // This should NOT trigger a change event.

        // THEN the listener should not have been notified at all:
        Mockito.verify(listener, Mockito.never()).formFieldValueChanged(any());
    }

    @Test
    public void testSetEnabled_allComponents() {
        actual.setEnabled(false);
        JPanel wrapperPanel = (JPanel)actual.getFieldComponent();
        int spinnersFound = 0;
        int labelsFound = 0;
        for (Component component : wrapperPanel.getComponents()) {
            assertFalse(component.isEnabled());
            if (component instanceof JSpinner) {
                spinnersFound++;
            }
            else if (component instanceof JLabel) {
                labelsFound++;
            }
        }
        assertEquals(5, spinnersFound);
        assertEquals(6, labelsFound); // 1 label per spinner + header label

        actual.setEnabled(true);
        spinnersFound = 0;
        labelsFound = 0;
        for (Component component : wrapperPanel.getComponents()) {
            assertTrue(component.isEnabled());
            if (component instanceof JSpinner) {
                spinnersFound++;
            }
            else if (component instanceof JLabel) {
                labelsFound++;
            }
        }
        assertEquals(5, spinnersFound);
        assertEquals(6, labelsFound); // 1 label per spinner + header label
    }

    @Test
    public void isValid_withValidFormField_shouldValidate() {
        // GIVEN a MarginsField with a test validator that fails if any margin is greater than 99:
        MarginsField field = new MarginsField("Test Margins");
        field.addFieldValidator(new TestValidator());

        // WHEN we set valid margin values:
        field.getMarginsObject().setLeft(10);
        field.getMarginsObject().setTop(20);
        field.getMarginsObject().setRight(30);
        field.getMarginsObject().setBottom(40);
        field.getMarginsObject().setInternalSpacing(50);

        // THEN the field should be valid and have no validation message:
        assertTrue(field.isValid());
        assertNull(field.getValidationLabel().getToolTipText());
    }

    @Test
    public void isValid_withInvalidFormField_shouldNotValidate() {
        // GIVEN a MarginsField with a test validator that fails if any margin is greater than 99:
        MarginsField field = new MarginsField("Test Margins");
        field.addFieldValidator(new TestValidator());

        // WHEN we set an invalid margin value:
        field.getMarginsObject().setLeft(10);
        field.getMarginsObject().setTop(20);
        field.getMarginsObject().setRight(30);
        field.getMarginsObject().setBottom(40);
        field.getMarginsObject().setInternalSpacing(150); // Invalid!

        // THEN the field should be invalid and have the expected validation message:
        assertFalse(field.isValid());
        assertEquals(TestValidator.MSG, field.getValidationLabel().getToolTipText());
    }

    /**
     * A simple test validator that will fail if any margin value is greater than 99.
     */
    private static class TestValidator implements FieldValidator<MarginsField> {

        private static final int MAX_MARGIN = 99;
        private static final String MSG = "INVALID!";

        @Override
        public ValidationResult validate(MarginsField fieldToValidate) {
            Margins m = fieldToValidate.getMarginsObject();
            if (m.getLeft() > MAX_MARGIN
                    || m.getTop() > MAX_MARGIN
                    || m.getRight() > MAX_MARGIN
                    || m.getBottom() > MAX_MARGIN
                    || m.getInternalSpacing() > MAX_MARGIN) {
                return ValidationResult.invalid(MSG);
            }
            return ValidationResult.valid();
        }
    }
}
