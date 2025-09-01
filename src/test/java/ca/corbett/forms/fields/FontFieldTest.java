package ca.corbett.forms.fields;

import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.ValidationResult;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FontFieldTest extends FormFieldBaseTests {

    @Override
    protected FormField createTestObject() {
        return new FontField("Test", FormField.getDefaultFont(), Color.BLUE, Color.BLACK);
    }

    @Test
    public void testGetComponent() {
        assertNotNull(actual.getFieldComponent());
        assertInstanceOf(JPanel.class, actual.getFieldComponent());
    }

    @Test
    public void testGetFieldLabel() {
        assertTrue(actual.hasFieldLabel());
        assertEquals("Test", actual.getFieldLabel().getText());
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
    public void testSetSelectedFont() {
        FontField actualField = (FontField)actual;
        assertEquals(FormField.getDefaultFont(), actualField.getSelectedFont());

        Font expectedFont = new Font(Font.MONOSPACED, Font.BOLD, 18);
        actualField.setSelectedFont(expectedFont);
        assertEquals(expectedFont, actualField.getSelectedFont());
    }

    @Test
    public void testSetColors() {
        FontField actualField = (FontField)actual;
        JLabel sampleLabel = (JLabel)findComponent(actualField, JLabel.class);
        assertNotNull(sampleLabel);
        assertEquals(Color.BLUE, sampleLabel.getForeground());
        assertEquals(Color.BLACK, sampleLabel.getBackground());
        assertEquals(Color.BLUE, actualField.getTextColor());
        assertEquals(Color.BLACK, actualField.getBgColor());

        actualField.setTextColor(Color.YELLOW);
        actualField.setBgColor(Color.GREEN);
        assertEquals(Color.YELLOW, sampleLabel.getForeground());
        assertEquals(Color.GREEN, sampleLabel.getBackground());
        assertEquals(Color.YELLOW, actualField.getTextColor());
        assertEquals(Color.GREEN, actualField.getBgColor());
    }

    @Test
    public void testSetEnabled_allComponents() {
        FontField actualField = (FontField)actual;
        actualField.setEnabled(false);
        JButton button = (JButton)findComponent(actualField, JButton.class);
        assertNotNull(button);
        assertFalse(button.isEnabled());
        assertFalse(actualField.isEnabled());

        actualField.setEnabled(true);
        assertTrue(button.isEnabled());
        assertTrue(actualField.isEnabled());
    }

    @Test
    public void testAddValueChangedListener() throws Exception {
        FontField actualField = (FontField)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);
        actualField.setSelectedFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        actualField.setSelectedFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        actualField.setSelectedFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14)); // shouldn't count as it isn't a value change
        Mockito.verify(listener, Mockito.times(2)).formFieldValueChanged(actual);
    }

    @Test
    public void testValueChangedTextColor() {
        FontField actualField = (FontField)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);
        actualField.setTextColor(Color.WHITE);
        Mockito.verify(listener, Mockito.times(1)).formFieldValueChanged(actual);
    }

    @Test
    public void testValueChangedBgColor() {
        FontField actualField = (FontField)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);
        actualField.setBgColor(Color.WHITE);
        Mockito.verify(listener, Mockito.times(1)).formFieldValueChanged(actual);
    }

    @Test
    public void testValueChangedBothColors() {
        FontField actualField = (FontField)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);
        actualField.setTextColor(Color.WHITE);
        actualField.setBgColor(Color.WHITE);
        Mockito.verify(listener, Mockito.times(2)).formFieldValueChanged(actual);
    }

    @Test
    public void testRemoveValueChangedListener() throws Exception {
        FontField actualField = (FontField)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);
        actual.removeValueChangedListener(listener);
        actualField.setSelectedFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        actualField.setSelectedFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
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
        FontField actualField = (FontField)actual;
        actual.addFieldValidator(new TestValidator());
        actualField.setSelectedFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        assertTrue(actual.isValid());
        assertNull(actual.getValidationLabel().getToolTipText());
    }

    private Component findComponent(FontField actualField, Class<?> searchClass) {
        JPanel wrapperPanel = (JPanel)actualField.getFieldComponent();
        for (Component candidate : wrapperPanel.getComponents()) {
            if (searchClass.isAssignableFrom(candidate.getClass())) {
                return candidate;
            }
        }
        return null;
    }

    private static class TestValidator implements FieldValidator<FontField> {

        public static final String MSG = "selected font must be monospaced";

        @Override
        public ValidationResult validate(FontField fieldToValidate) {
            return fieldToValidate.getSelectedFont().getFamily().equals(Font.MONOSPACED)
                    ? ValidationResult.valid()
                    : ValidationResult.invalid(MSG);
        }
    }
}