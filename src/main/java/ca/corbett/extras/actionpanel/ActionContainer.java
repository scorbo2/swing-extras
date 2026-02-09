package ca.corbett.extras.actionpanel;

import ca.corbett.extras.EnhancedAction;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
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
    private final ActionGroup actionGroup;
    private final ToolBar toolBar;

    public ActionContainer(ActionPanel actionPanel, ActionGroup actionGroup) {
        this.actionPanel = actionPanel;
        this.actionGroup = actionGroup;

        setLayout(new BorderLayout());

        JPanel actionsPanel = new JPanel();
        actionsPanel.setBackground(actionPanel.getActionBackground());
        actionsPanel.setLayout(new BoxLayout(actionsPanel, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        // Get the group actions (will be sorted if comparator is set)
        List<EnhancedAction> actions = actionGroup.getActions();

        // Add each action component:
        boolean isFirst = true;
        for (EnhancedAction action : actions) {
            JPanel wrapperPanel = createComponentWrapperPanel(isFirst ? actionPanel.getInternalPadding() : 0);
            Component actionComponent = ActionComponentFactory.create(actionPanel, action);
            wrapperPanel.add(actionComponent, BorderLayout.CENTER);
            actionsPanel.add(wrapperPanel);
        }

        add(actionsPanel, BorderLayout.CENTER);

        // Add the toolbar if enabled:
        if (actionPanel.isToolBarEnabled()) {
            toolBar = new ToolBar(actionPanel, actionGroup.getName());
            toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(toolBar, BorderLayout.SOUTH);
        }
        else {
            toolBar = null;
        }
    }

    private JPanel createComponentWrapperPanel(int topMargin) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Apply internal padding and action indent if set:
        if (actionPanel.getInternalPadding() > 0 || actionPanel.getActionIndent() > 0) {
            int pad = actionPanel.getInternalPadding();
            int indent = actionPanel.getActionIndent();
            panel.setBorder(BorderFactory.createEmptyBorder(topMargin, pad + indent, pad, pad));
        }

        // Apply action background color if set:
        if (actionPanel.getActionBackground() != null) {
            panel.setBackground(actionPanel.getActionBackground());
        }

        return panel;
    }
}
