package ca.corbett.extras.actionpanel;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

/**
 * A ToolBar can be optionally presented within an ActionGroup in an ActionPanel.
 * It can contain buttons for adding new items to the group, reordering items in
 * the group, removing items from the group, or removing the group itself.
 * All of this can be customized in ActionPanel.
 * <p>
 * This class is package-private, so callers do not
 * interact with it directly. Use the ActionPanel API instead.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
 */
class ToolBar extends JPanel {
    private final ActionPanel actionPanel;
    private final String groupName;
    private final ToolBarOptions options;

    public ToolBar(ActionPanel actionPanel, String groupName) {
        if (actionPanel == null) {
            throw new IllegalArgumentException("actionPanel cannot be null");
        }
        this.actionPanel = actionPanel;
        this.groupName = groupName;
        this.options = actionPanel.getToolBarOptions();
        this.setBackground(actionPanel.getColorOptions().getActionBackground());

        // Set border if defined:
        if (actionPanel.getBorderOptions().getToolBarBorder() != null) {
            setBorder(actionPanel.getBorderOptions().getToolBarBorder());
        }

        // Set our alignment:
        int pad = actionPanel.getToolBarInternalPadding();
        switch (options.getButtonPosition()) {
            case AlignRight -> setLayout(new FlowLayout(FlowLayout.RIGHT, pad, pad));
            case Center -> setLayout(new FlowLayout(FlowLayout.CENTER, pad, pad));
            case AlignLeft -> setLayout(new FlowLayout(FlowLayout.LEFT, pad, pad));
            case Stretch -> setLayout(new GridLayout(1, 0)); // auto-size row of buttons
        }

        // If this group is in the "toolbar excluded" list, just skip it:
        if (options.isGroupExcluded(groupName)) {
            return;
        }

        // Add all buttons if they are allowed and if they are present:
        addButton(options.isAllowItemAdd(), options.createItemAddAction(actionPanel, groupName));
        addButton(options.isAllowGroupRename(), options.createRenameGroupAction(actionPanel, groupName));
        addButton(options.isAllowItemReorder() || options.isAllowItemRemoval(),
                  options.createEditGroupAction(actionPanel, groupName));
        for (ToolBarAction customAction : options.createCustomActions(actionPanel, groupName)) {
            addButton(true, customAction); // custom actions are always allowed
        }
        addButton(options.isAllowGroupRemoval(), options.createRemoveGroupAction(actionPanel, groupName));
    }

    /**
     * Invoked to create and add a button representing the given ToolBarAction.
     * If the given action is null, it is skipped.
     *
     * @param permission Whether the action is allowed. If false, we skip this one.
     * @param action     the ToolBarAction to create a button for
     */
    private void addButton(boolean permission, ToolBarAction action) {
        if (action == null || !permission) {
            return; // not an error - just means no button for this action
        }

        final int buttonSize = options.getIconSize() + 4; // arbitrary padding to make sure icons fit comfortably
        JButton button = new JButton(action);
        button.setText(""); // our buttons are icons-only

        // The ext-iv-ice code that this was based on used to set up transparent buttons with no border,
        // but in this code, the button background color is configurable. There is a convenience
        // method in ColorOptions to set the button background to transparent, so you can mimic the old look.
        // We don't disable borders, though, because I find this approach works better across L&Fs.
        button.setBackground(actionPanel.getColorOptions().getToolBarButtonBackground());

        button.setPreferredSize(new Dimension(buttonSize, buttonSize)); // ignored in Stretch mode
        button.setIcon(action.getIcon());
        button.setToolTipText(action.getTooltip());
        add(button);
    }
}
