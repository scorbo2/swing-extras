package ca.corbett.extras.actionpanel;

import ca.corbett.extras.EnhancedAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for ActionComponentFactory.
 */
class ActionComponentFactoryTest {

    private ActionPanel actionPanel;

    @BeforeEach
    void setUp() {
        actionPanel = new ActionPanel();
    }

    // --- create() dispatch ---

    @Test
    void create_withLabelsType_shouldReturnJLabel() {
        // GIVEN the action panel is configured to use labels:
        actionPanel.setUseLabels();
        EnhancedAction action = new TestAction("Test Action");

        // WHEN we create a component:
        JComponent component = ActionComponentFactory.create(actionPanel, action);

        // THEN the component should be a JLabel:
        assertInstanceOf(JLabel.class, component, "Should return JLabel when LABELS type is configured");
    }

    @Test
    void create_withButtonsType_shouldReturnJButton() {
        // GIVEN the action panel is configured to use buttons:
        actionPanel.setUseButtons();
        EnhancedAction action = new TestAction("Test Action");

        // WHEN we create a component:
        JComponent component = ActionComponentFactory.create(actionPanel, action);

        // THEN the component should be a JButton:
        assertInstanceOf(JButton.class, component, "Should return JButton when BUTTONS type is configured");
    }

    // --- createLabel ---

    @Test
    void createLabel_shouldContainActionName() {
        // GIVEN an action with a name:
        EnhancedAction action = new TestAction("My Action");

        // WHEN we create a label:
        JLabel label = ActionComponentFactory.createLabel(actionPanel, action);

        // THEN the label text should include the action name (wrapped in HTML):
        assertNotNull(label.getText(), "Label text should not be null");
        assertTrue(label.getText().contains("My Action"), "Label text should contain the action name");
    }

    @Test
    void createLabel_shouldReturnNonNull() {
        // GIVEN an action:
        EnhancedAction action = new TestAction("Test");

        // WHEN we create a label:
        JLabel label = ActionComponentFactory.createLabel(actionPanel, action);

        // THEN the label should not be null:
        assertNotNull(label, "createLabel should not return null");
    }

    @Test
    void createLabel_withCustomFont_shouldApplyFont() {
        // GIVEN an action panel with a custom font:
        Font customFont = new Font("Serif", Font.BOLD, 14);
        actionPanel.setActionFont(customFont);
        EnhancedAction action = new TestAction("Test");

        // WHEN we create a label:
        JLabel label = ActionComponentFactory.createLabel(actionPanel, action);

        // THEN the label should have the custom font:
        assertEquals(customFont, label.getFont(), "Label should have the custom font applied");
    }

    @Test
    void createLabel_withCustomForeground_shouldApplyColor() {
        // GIVEN an action panel with a custom foreground color:
        actionPanel.getColorOptions().setActionForeground(Color.RED);
        EnhancedAction action = new TestAction("Test");

        // WHEN we create a label:
        JLabel label = ActionComponentFactory.createLabel(actionPanel, action);

        // THEN the label should have the custom foreground color:
        assertEquals(Color.RED, label.getForeground(), "Label should have the custom foreground color applied");
    }

    @Test
    void createLabel_withNoCustomFont_shouldNotOverrideFont() {
        // GIVEN an action panel without a custom font (default null):
        // WHEN we create a label:
        EnhancedAction action = new TestAction("Test");
        JLabel label = ActionComponentFactory.createLabel(actionPanel, action);

        // THEN a label is still returned (no exception):
        assertNotNull(label, "Label should be created even without a custom font");
    }

    @Test
    void createLabel_withTooltip_shouldSetToolTip() {
        // GIVEN an action with a tooltip:
        EnhancedAction action = new TestAction("Test").setTooltip("This is a tooltip");

        // WHEN we create a label:
        JLabel label = ActionComponentFactory.createLabel(actionPanel, action);

        // THEN the label should have the tooltip:
        assertEquals("This is a tooltip", label.getToolTipText(), "Label should have the tooltip text applied");
    }

    @Test
    void createLabel_withoutTooltip_shouldNotSetToolTip() {
        // GIVEN an action without a tooltip:
        EnhancedAction action = new TestAction("Test"); // no tooltip set

        // WHEN we create a label:
        JLabel label = ActionComponentFactory.createLabel(actionPanel, action);

        // THEN the label should have no tooltip:
        assertNull(label.getToolTipText(), "Label should have no tooltip when action has no tooltip");
    }

    // --- createButton ---

    @Test
    void createButton_shouldReturnNonNull() {
        // GIVEN an action:
        EnhancedAction action = new TestAction("Test");

        // WHEN we create a button:
        JButton button = ActionComponentFactory.createButton(actionPanel, action);

        // THEN the button should not be null:
        assertNotNull(button, "createButton should not return null");
    }

    @Test
    void createButton_withCustomFont_shouldApplyFont() {
        // GIVEN an action panel with a custom font:
        Font customFont = new Font("Serif", Font.BOLD, 14);
        actionPanel.setActionFont(customFont);
        EnhancedAction action = new TestAction("Test");

        // WHEN we create a button:
        JButton button = ActionComponentFactory.createButton(actionPanel, action);

        // THEN the button should have the custom font:
        assertEquals(customFont, button.getFont(), "Button should have the custom font applied");
    }

    @Test
    void createButton_withCustomForeground_shouldApplyColor() {
        // GIVEN an action panel with a custom foreground color:
        actionPanel.getColorOptions().setActionForeground(Color.BLUE);
        EnhancedAction action = new TestAction("Test");

        // WHEN we create a button:
        JButton button = ActionComponentFactory.createButton(actionPanel, action);

        // THEN the button should have the custom foreground color:
        assertEquals(Color.BLUE, button.getForeground(), "Button should have the custom foreground color applied");
    }

    @Test
    void createButton_withCustomButtonBackground_shouldApplyBackground() {
        // GIVEN an action panel with a custom button background color:
        actionPanel.getColorOptions().setActionButtonBackground(Color.GREEN);
        EnhancedAction action = new TestAction("Test");

        // WHEN we create a button:
        JButton button = ActionComponentFactory.createButton(actionPanel, action);

        // THEN the button should have the custom background color:
        assertEquals(Color.GREEN, button.getBackground(), "Button should have the custom background color applied");
    }

    @Test
    void createButton_withShowActionIconsFalse_shouldHaveNullIcon() {
        // GIVEN an action panel that does not show action icons:
        actionPanel.setShowActionIcons(false);
        EnhancedAction action = new TestAction("Test");

        // WHEN we create a button:
        JButton button = ActionComponentFactory.createButton(actionPanel, action);

        // THEN the button should have no icon:
        assertNull(button.getIcon(), "Button should have no icon when showActionIcons is false");
    }

    /**
     * A very simple implementation of EnhancedAction that does nothing.
     */
    private static class TestAction extends EnhancedAction {

        public TestAction(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // No-op
        }
    }
}
