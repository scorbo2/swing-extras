package ca.corbett.forms.fields;

import org.junit.jupiter.api.Test;

import javax.swing.JLabel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HeaderLabelFieldTest extends FormFieldBaseTests {
    @Override
    protected FormField createTestObject() {
        return new LabelField("header label");
    }

    @Test
    public void testGetFieldComponent() {
        assertNotNull(actual.getFieldComponent());
        assertInstanceOf(JLabel.class, actual.getFieldComponent());
    }

    @Test
    public void testGetFieldLabel() {
        assertFalse(actual.hasFieldLabel());
        assertEquals("", actual.getFieldLabel().getText());
    }

    @Test
    public void testIsMultiLine() {
        assertFalse(actual.isMultiLine());
    }

    @Test
    public void testShouldExpand() {
        assertFalse(actual.shouldExpand());
    }
}
