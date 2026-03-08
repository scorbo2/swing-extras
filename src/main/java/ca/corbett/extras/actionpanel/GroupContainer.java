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

    /**
     * Creates an instance of GroupContainer for the given ActionGroup.
     * Applies external padding based on the ActionPanel's settings.
     *
     * @param actionPanel  The parent ActionPanel, used to access padding and border settings.
     * @param actionGroup  The ActionGroup this container represents, used to create the header and actions panel.
     * @param isFirstGroup Indicates if this is the first group in the ActionPanel, which may affect top margin.
     * @param isLastGroup  Indicates if this is the last group in the ActionPanel, which may affect bottom margin.
     */
    public GroupContainer(ActionPanel actionPanel, ActionGroup actionGroup, boolean isFirstGroup, boolean isLastGroup) {
        // Get margin settings for left and right:
        int leftMargin = actionPanel.getActionGroupMargins().getLeft();
        int rightMargin = actionPanel.getActionGroupMargins().getRight();

        // If this is the first group, then our top margin will be the
        // external padding defined in the ActionPanel. Otherwise, we want to use the internal spacing value:
        int topMargin = isFirstGroup
                ? actionPanel.getActionGroupMargins().getTop()
                : actionPanel.getActionGroupMargins().getInternalSpacing();

        // If this is the last group, then our bottom margin will be the
        // external padding defined in the ActionPanel.
        // Otherwise, it's zero, as the top margin of the next group will handle the spacing between them.
        int bottomMargin = isLastGroup ? actionPanel.getActionGroupMargins().getBottom() : 0;
        setBorder(BorderFactory.createEmptyBorder(topMargin, leftMargin, bottomMargin, rightMargin));

        // Create a wrapper panel for the group contents:
        JPanel groupPanel = new JPanel();
        groupPanel.setLayout(new BoxLayout(groupPanel, BoxLayout.Y_AXIS));
        groupPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Apply group border if set
        if (actionPanel.getBorderOptions().getGroupBorder() != null) {
            groupPanel.setBorder(actionPanel.getBorderOptions().getGroupBorder());
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
