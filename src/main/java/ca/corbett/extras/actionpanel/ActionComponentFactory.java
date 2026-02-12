package ca.corbett.extras.actionpanel;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.extras.image.ImageUtil;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A utility class used internally by ActionPanel to generate the specific
 * JComponent that represents an action. This class is package-private
 * and is not intended to be used directly by callers. Use the ActionPanel
 * API instead.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
 */
class ActionComponentFactory {

    private ActionComponentFactory() {
        // Private constructor to prevent instantiation
    }

    /**
     * Generates and returns a JComponent that represents the specified action.
     * The exact type of the returned component depends on how the ActionPanel is configured.
     *
     * @param actionPanel The ActionPanel requesting the component.
     * @param action      The EnhancedAction to create a component for.
     * @return A JComponent representing the action.
     */
    public static JComponent create(ActionPanel actionPanel, EnhancedAction action) {
        return switch (actionPanel.getActionComponentType()) {
            case LABELS -> createLabel(actionPanel, action);
            case BUTTONS -> createButton(actionPanel, action);
        };
    }

    /**
     * Invoked internally to create a clickable JLabel for the specified action.
     */
    static JLabel createLabel(ActionPanel actionPanel, EnhancedAction action) {
        // Note: we use html tags to wrap the label text, because otherwise, there's a slight
        //       vertical shift in the text when we add the underline effect on mouseover.
        JLabel label = new JLabel("<html>" + action.getName() + "</html>");
        if (action.getIcon() != null && actionPanel.isShowActionIcons() && (action.getIcon() instanceof ImageIcon)) {
            label.setIcon(ImageUtil.scaleIcon((ImageIcon)action.getIcon(), actionPanel.getActionIconSize()));
            label.setIconTextGap(actionPanel.getActionInternalPadding());
        }
        if (action.getTooltip() != null) {
            label.setToolTipText(action.getTooltip());
        }

        // Apply styling:
        if (actionPanel.getActionFont() != null) {
            label.setFont(actionPanel.getActionFont());
        }
        if (actionPanel.getColorOptions().getActionForeground() != null) {
            label.setForeground(actionPanel.getColorOptions().getActionForeground());
        }

        // Make it clickable and add our mouseover effects:
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new LabelMouseListener(actionPanel, label, action));

        // Left alignment is generally best for labels in a vertical list
        // This is not currently configurable...
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        return label;
    }

    /**
     * Invoked internally to create a JButton for the specified action.
     */
    static JButton createButton(ActionPanel actionPanel, EnhancedAction action) {
        // Create a button:
        JButton button = new JButton(action);

        // Spy on its action events so we can highlight it when invoked:
        button.addActionListener(new ActionSpy(actionPanel, action));

        // Apply styling:
        if (actionPanel.getActionFont() != null) {
            button.setFont(actionPanel.getActionFont());
        }
        if (actionPanel.getColorOptions().getActionForeground() != null) {
            button.setForeground(actionPanel.getColorOptions().getActionForeground());
        }
        if (actionPanel.getColorOptions().getActionButtonBackground() != null) {
            button.setBackground(actionPanel.getColorOptions().getActionButtonBackground());
        }

        // If this is the highlighted action, highlight it:
        if (actionPanel.isHighlightedAction(action)) {
            button.setBackground(ColorOptions.getHighlightColor(button.getBackground()));
        }

        // Null out the icon if we're not showing icons:
        if (!actionPanel.isShowActionIcons()) {
            button.setIcon(null);
        }

        // Otherwise, ensure the icon is scaled correctly:
        else {
            if (action.getIcon() != null && action.getIcon() instanceof ImageIcon) {
                button.setIcon(ImageUtil.scaleIcon((ImageIcon)action.getIcon(), actionPanel.getActionIconSize()));
                button.setHorizontalTextPosition(JButton.RIGHT); // text to the right of the icon
                button.setIconTextGap(actionPanel.getActionInternalPadding());
            }
        }

        // Left alignment is generally best for buttons in a vertical list
        // This is not currently configurable...
        button.setAlignmentX(Component.LEFT_ALIGNMENT);

        return button;
    }

    /**
     * A MouseListener for clickable JLabels representing actions.
     * When the label is clicked, it invokes the associated action.
     * We also provide visual feedback by underlining the text on mouseover.
     */
    private static class LabelMouseListener extends MouseAdapter {

        private final ActionPanel actionPanel;
        private final JLabel label;
        private final EnhancedAction action;

        public LabelMouseListener(ActionPanel actionPanel, JLabel label, EnhancedAction action) {
            this.actionPanel = actionPanel;
            this.label = label;
            this.action = action;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            // Invoke action when clicked
            action.actionPerformed(new ActionEvent(label, ActionEvent.ACTION_PERFORMED, action.getName()));

            // Notify ActionPanel to highlight this action
            actionPanel.setHighlightedAction(action);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            // Underline the text when mouse enters
            label.setText("<html><u>" + action.getName() + "</u></html>");
        }

        @Override
        public void mouseExited(MouseEvent e) {
            // Remove underline when mouse exits
            label.setText("<html>" + action.getName() + "</html>");
        }
    }

    /**
     * A "spy" action that we will use to monitor when an action is performed,
     * so that we can notify the ActionPanel to highlight the action.
     */
    private static class ActionSpy extends AbstractAction {

        private final ActionPanel actionPanel;
        private final EnhancedAction action;

        public ActionSpy(ActionPanel actionPanel, EnhancedAction action) {
            this.actionPanel = actionPanel;
            this.action = action;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            actionPanel.setHighlightedAction(action);
        }
    }
}
