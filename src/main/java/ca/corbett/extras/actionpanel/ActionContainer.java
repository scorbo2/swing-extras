package ca.corbett.extras.actionpanel;

import ca.corbett.extras.EnhancedAction;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;

/**
 * An ActionContainer holds and displays the actions for a specific ActionGroup
 * within an ActionPanel. It also holds the optional ToolBar for the group.
 * (That may seem as though it should be separate, but we want the toolbar to be included
 * with the "collapse" feature, instead of leaving it visible when the actions are hidden.)
 * <p>
 * This class is package-private, so callers do not
 * interact with it directly. Use the ActionPanel API instead.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
 */
class ActionContainer extends JPanel {

    private final ActionPanel actionPanel;

    public ActionContainer(ActionPanel actionPanel, ActionGroup actionGroup) {
        this.actionPanel = actionPanel;

        setLayout(new BorderLayout());

        JPanel actionsPanel = new JPanel();
        actionsPanel.setBackground(actionPanel.getColorOptions().getActionBackground());
        actionsPanel.setLayout(new BoxLayout(actionsPanel, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        // Set border if defined:
        if (actionPanel.getBorderOptions().getActionTrayBorder() != null) {
            actionsPanel.setBorder(actionPanel.getBorderOptions().getActionTrayBorder());
        }

        // Get the group actions (will be sorted if comparator is set)
        List<EnhancedAction> actions = actionGroup.getActions();

        // Add each action component:
        for (int i = 0; i < actions.size(); i++) {
            EnhancedAction action = actions.get(i);
            JPanel wrapperPanel = createComponentWrapperPanel(i == 0, i == actions.size() - 1);
            Component actionComponent = ActionComponentFactory.create(actionPanel, action);
            wrapperPanel.add(actionComponent, BorderLayout.CENTER);
            actionsPanel.add(wrapperPanel);

            // If this is the highlighted action, mark it visually:
            if (actionPanel.isHighlightLastActionEnabled()) {
                if (actionPanel.isHighlightedAction(action) && (actionComponent instanceof JLabel)) {
                    wrapperPanel.setBackground(ColorOptions.getHighlightColor(wrapperPanel.getBackground()));
                }
            }
        }

        add(actionsPanel, BorderLayout.CENTER);

        // Add the toolbar if enabled:
        if (actionPanel.isToolBarEnabled()) {
            ToolBar toolBar = new ToolBar(actionPanel, actionGroup.getName());
            toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(toolBar, BorderLayout.SOUTH);
        }
    }

    private JPanel createComponentWrapperPanel(boolean isFirst, boolean isLast) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        int leftMargin = actionPanel.getActionTrayMargins().getLeft();
        int rightMargin = actionPanel.getActionTrayMargins().getRight();

        // If this is the first action, then our top margin will be the top property
        // of the action tray margins. Otherwise, we want to use the internal spacing value:
        int topMargin = isFirst
                ? actionPanel.getActionTrayMargins().getTop()
                : actionPanel.getActionTrayMargins().getInternalSpacing();

        // If this is the last action, then our bottom margin will be the bottom property
        // of the action tray margins. Otherwise, it's zero, as the top margin of the next action
        // will handle the spacing between them.
        int bottomMargin = isLast ? actionPanel.getActionTrayMargins().getBottom() : 0;
        panel.setBorder(BorderFactory.createEmptyBorder(topMargin, leftMargin, bottomMargin, rightMargin));

        // Apply action background color if set:
        if (actionPanel.getColorOptions().getActionBackground() != null) {
            panel.setBackground(actionPanel.getColorOptions().getActionBackground());
        }

        return panel;
    }
}
