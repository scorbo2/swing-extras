package ca.corbett.forms.fields;

import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.ValidationResult;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NonHeaderLabelFieldTest extends FormFieldBaseTests {

    @Override
    protected FormField createTestObject() {
        return new LabelField("label", "value");
    }

    @Test
    public void testGetFieldComponent() {
        assertNotNull(actual.getFieldComponent());
        assertInstanceOf(JLabel.class, actual.getFieldComponent());
    }

    @Test
    public void testGetFieldLabel() {
        assertTrue(actual.hasFieldLabel());
        assertEquals("label", actual.getFieldLabel().getText());
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
    public void testHyperlink() {
        LabelField actualField = (LabelField)actual;
        assertFalse(actualField.isHyperlinked());

        actualField.setHyperlink(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
        assertTrue(actualField.isHyperlinked());

        actualField.clearHyperlink();
        assertFalse(actualField.isHyperlinked());
    }

    @Test
    public void testSetFont() {
        LabelField actualField = (LabelField)actual;
        Font expectedFont = new Font(Font.MONOSPACED, Font.BOLD, 14);
        actualField.setFont(expectedFont);
        assertEquals(expectedFont, actualField.getFont());
    }

    @Test
    public void testSetColor() {
        LabelField actualField = (LabelField)actual;
        Color expected = Color.RED;
        actualField.setColor(expected);
        assertEquals(expected, actualField.getColor());
    }

    @Test
    public void testSetText() {
        LabelField actualField = (LabelField)actual;
        final String text = "This is the expected text";
        actualField.setText(text);
        assertEquals(text, actualField.getText());
        JLabel label = (JLabel)actualField.getFieldComponent();
        assertEquals(text, label.getText());
    }

    @Test
    public void testAddValueChangedListener() throws Exception {
        LabelField actualField = (LabelField)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);
        actualField.setText("Hello");
        actualField.setText("Hello again");
        actualField.setText("Hello again"); // shouldn't count as it isn't a value change
        Mockito.verify(listener, Mockito.times(2)).formFieldValueChanged(actual);
    }

    @Test
    public void testRemoveValueChangedListener() throws Exception {
        LabelField actualField = (LabelField)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);
        actual.removeValueChangedListener(listener);
        actualField.setText("Hello again");
        actualField.setText("Hello again");
        Mockito.verify(listener, Mockito.times(0)).formFieldValueChanged(actual);
    }

    @Test
    public void validate_invalidScenario() {
        actual.addFieldValidator(new TestValidator());
        assertFalse(actual.isValid());
        assertEquals(TestValidator.MSG, actual.getValidationLabel().getToolTipText());
    }

    @Test
    public void validate_validScenario() {
        LabelField actualField = (LabelField)actual;
        actual.addFieldValidator(new TestValidator());
        actualField.setText("hello");
        assertTrue(actual.isValid());
        assertNull(actual.getValidationLabel().getToolTipText());
    }

    private static class TestValidator implements FieldValidator<LabelField> {

        public static final String MSG = "text must say hello";

        @Override
        public ValidationResult validate(LabelField fieldToValidate) {
            return "hello".equals(fieldToValidate.getText())
                    ? ValidationResult.valid()
                    : ValidationResult.invalid(MSG);
        }
    }

}