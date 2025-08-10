package ca.corbett.forms.fields;

import org.junit.jupiter.api.Test;

import javax.swing.JPanel;
import java.awt.FlowLayout;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PanelFieldTest extends FormFieldBaseTests {

    @Override
    protected FormField createTestObject() {
        return new PanelField(new FlowLayout(FlowLayout.LEFT));
    }

    @Test
    public void testGetFieldComponent() {
        assertNotNull(actual.getFieldComponent());
        assertInstanceOf(JPanel.class, actual.getFieldComponent());
    }

    @Test
    public void testGetFieldLabel() {
        assertFalse(actual.hasFieldLabel());
    }

    @Test
    public void testIsMultiLine() {
        assertTrue(actual.isMultiLine());
    }

    @Test
    public void testShouldExpand() {
        assertTrue(actual.shouldExpand());
    }

}