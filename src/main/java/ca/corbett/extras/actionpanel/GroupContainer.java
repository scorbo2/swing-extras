package ca.corbett.extras.actionpanel;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;

/**
 * A GroupContainer houses a HeaderBar, an ActionContainer, and
 * an optional ToolBar to represent a single ActionGroup within an ActionPanel.
 * This class is package-private, so callers do not interact with it directly.
 * Use the ActionPanel API instead.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
 */
class GroupContainer extends JPanel {

    public GroupContainer(ActionPanel actionPanel, ActionGroup actionGroup, int topMargin) {
        // We want to respect the external padding defined in the ActionPanel,
        // but when we're stacked vertically with other GroupContainers, the
        // caller may want to specify a different top margin.
        int pad = actionPanel.getExternalPadding();
        setBorder(BorderFactory.createEmptyBorder(topMargin, pad, pad, pad));

        // Create a wrapper panel for the group contents:
        JPanel groupPanel = new JPanel();
        groupPanel.setLayout(new BoxLayout(groupPanel, BoxLayout.Y_AXIS));
        groupPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Apply group border if set
        if (actionPanel.getGroupBorder() != null) {
            groupPanel.setBorder(actionPanel.getGroupBorder());
        }

        // Create and add the group header
        groupPanel.add(new HeaderBar(actionPanel, actionGroup));

        // Create the actions panel (always create it, even if collapsed)
        JPanel actionsPanel = new ActionContainer(actionPanel, actionGroup);

        // Create an animated wrapper for the actions panel
        AnimatedPanel animatedWrapper = new AnimatedPanel(actionsPanel);
        animatedWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Store reference to the animated wrapper in the group for later access
        actionGroup.setAnimatedWrapper(animatedWrapper);

        // Set initial state based on expanded state
        if (actionGroup.isExpanded()) {
            animatedWrapper.setFullyExpanded();
        }
        else {
            animatedWrapper.setFullyCollapsed();
        }

        groupPanel.add(animatedWrapper);

        setLayout(new BorderLayout());
        setBackground(actionPanel.getBackground());
        add(groupPanel, BorderLayout.CENTER);
    }
}
