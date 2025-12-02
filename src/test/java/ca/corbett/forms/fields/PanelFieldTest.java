package ca.corbett.forms.fields;

import org.junit.jupiter.api.Test;

import javax.swing.JButton;
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
        assertFalse(actual.shouldExpand());
        ((PanelField)actual).setShouldExpand(true);
        assertTrue(actual.shouldExpand());
    }

    @Test
    public void testEnabledPropagatedStatus_withFalse_shouldNotPropagate() {
        // GIVEN a PanelField with default options (isEnabledStatusPropagated == false)
        PanelField panelField = new PanelField(new FlowLayout(FlowLayout.LEFT));
        JButton button1 = new JButton("Hello");
        JButton button2 = new JButton("There");
        panelField.getPanel().add(button1);
        panelField.getPanel().add(button2);

        // WHEN we set the PanelField to disabled:
        panelField.setEnabled(false);

        // THEN the contained components should not have been affected:
        assertFalse(panelField.isEnabled());
        assertTrue(button1.isEnabled());
        assertTrue(button2.isEnabled());
    }

    @Test
    public void testEnabledPropagatedStatus_withTrue_shouldPropagate() {
        // GIVEN a PanelField with some child components:
        PanelField panelField = new PanelField(new FlowLayout(FlowLayout.LEFT));
        JButton button1 = new JButton("Hello");
        JPanel childPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton button2 = new JButton("There");
        childPanel.add(button2);
        panelField.getPanel().add(button1);
        panelField.getPanel().add(childPanel);

        // WHEN we set enabled propagated status to true and disable the PanelField:
        panelField.setEnabledStatusIsPropagated(true);
        panelField.setEnabled(false);

        // THEN we should see that all child components (and grandchildren) are now disabled:
        assertFalse(panelField.isEnabled());
        assertFalse(button1.isEnabled());
        assertFalse(childPanel.isEnabled());
        assertFalse(button2.isEnabled()); // not a direct child of PanelField but should still be affected
    }

}