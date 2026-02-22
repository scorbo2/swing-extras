package ca.corbett.extras.actionpanel;

import ca.corbett.extras.LookAndFeelManager;
import com.formdev.flatlaf.ui.FlatButtonBorder;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;

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
    private final JPanel innerPanel;
    private final String groupName;
    private final ToolBarOptions options;

    public ToolBar(ActionPanel actionPanel, String groupName) {
        if (actionPanel == null) {
            throw new IllegalArgumentException("actionPanel cannot be null");
        }
        this.innerPanel = new JPanel();
        this.innerPanel.setBackground(actionPanel.getColorOptions().getActionBackground());
        this.actionPanel = actionPanel;
        this.groupName = groupName;
        this.options = actionPanel.getToolBarOptions();
        this.setBackground(actionPanel.getColorOptions().getActionBackground());

        // Set border if defined:
        // This border goes on the ToolBarPanel itself, NOT on our innerPanel:
        if (actionPanel.getBorderOptions().getToolBarBorder() != null) {
            setBorder(actionPanel.getBorderOptions().getToolBarBorder());
        }

        // Set margins around the buttons:
        // These go on the innerPanel, NOT on the ToolBarPanel itself:
        innerPanel.setBorder(BorderFactory.createEmptyBorder(
                actionPanel.getToolBarMargins().getTop(),
                actionPanel.getToolBarMargins().getLeft(),
                actionPanel.getToolBarMargins().getBottom(),
                actionPanel.getToolBarMargins().getRight()
        ));

        // Now we can set up the spacing between buttons:
        // Note that our internalPadding is supposed to represent ONLY the space between buttons,
        // but FlowLayout also applies this padding to the left and right of the first and last buttons,
        // which is not ideal. But it is what it is.
        int pad = actionPanel.getToolBarMargins().getInternalSpacing();
        switch (options.getButtonPosition()) {
            case AlignRight -> innerPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, pad, pad));
            case Center -> innerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, pad, pad));
            case AlignLeft -> innerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, pad, pad));

            // Stretch mode ignores internalPadding entirely and just fills our entire row:
            case Stretch -> innerPanel.setLayout(new GridLayout(1, 0)); // auto-size row of buttons
        }

        // If this group is in the "toolbar excluded" list, just skip it:
        if (options.isGroupExcluded(groupName)) {
            return;
        }

        // Add all buttons if they are allowed and if they are present:
        JButton addBtn = createButton(options.isAllowItemAdd(), options.createItemAddAction(actionPanel, groupName));
        if (addBtn != null) {
            innerPanel.add(addBtn);
        }
        JButton renameBtn = createButton(options.isAllowGroupRename(),
                                         options.createRenameGroupAction(actionPanel, groupName));
        if (renameBtn != null) {
            innerPanel.add(renameBtn);
        }
        JButton editBtn = createButton(options.isAllowItemReorder() || options.isAllowItemRemoval(),
                  options.createEditGroupAction(actionPanel, groupName));
        if (editBtn != null) {
            innerPanel.add(editBtn);
        }
        for (ToolBarAction customAction : options.createCustomActions(actionPanel, groupName)) {
            JButton btn = createButton(true, customAction); // custom actions are always allowed
            if (btn != null) {
                innerPanel.add(btn);
            }
        }
        JButton removeBtn = createButton(options.isAllowGroupRemoval(),
                                         options.createRemoveGroupAction(actionPanel, groupName));
        if (removeBtn != null) {
            innerPanel.add(removeBtn);
        }

        setLayout(new BorderLayout());
        add(innerPanel, BorderLayout.CENTER);
    }

    /**
     * Invoked to create and return a button representing the given ToolBarAction.
     * If the given action is null, it is skipped.
     *
     * @param permission Whether the action is allowed. If false, we skip this one.
     * @param action     the ToolBarAction to create a button for
     * @return a JButton for the given action, or null if the action is null or not allowed
     */
    private JButton createButton(boolean permission, ToolBarAction action) {
        if (action == null || !permission) {
            return null; // not an error - just means no button for this action
        }

        final int buttonSize = options.getIconSize() + (actionPanel.getButtonPadding() * 2); // apply padding as needed.
        JButton button = new JButton(action);
        button.setText(""); // our buttons are icons-only

        // The ext-iv-ice code that this was based on used to set up transparent buttons with no border,
        // but in this code, the button background color is configurable. There is a convenience
        // method in ColorOptions to set the button background to transparent, so you can mimic the old look.
        // We don't disable borders, though, because I find this approach works better across L&Fs.
        button.setBackground(actionPanel.getColorOptions().getToolBarButtonBackground());
        button.setFocusable(false);

        // Special case "flat buttons" because their default border handling is very stupid.
        // (they apply padding around buttons, I think as part of their "focus border", and it looks awful,
        //  particularly if you try to set toolbar internal spacing to 0.)
        // We can also apply the button padding from our ActionPanel, if it is set.
        int pad = actionPanel.getButtonPadding();
        if (button.getBorder() != null && button.getBorder() instanceof FlatButtonBorder) {
            Border lineBorder = BorderFactory.createLineBorder(
                    LookAndFeelManager.getLafColor("Button.default.startBorderColor", Color.LIGHT_GRAY),
                    1,
                    true);
            Border paddingBorder = BorderFactory.createEmptyBorder(pad, pad, pad, pad);
            button.setBorder(BorderFactory.createCompoundBorder(lineBorder, paddingBorder));
        }
        else {
            // Regular buttons can just use the margin for padding:
            button.setMargin(new Insets(pad, pad, pad, pad));
        }

        button.setPreferredSize(new Dimension(buttonSize, buttonSize)); // ignored in Stretch mode
        button.setIcon(action.getIcon());
        button.setToolTipText(action.getTooltip());
        return button;
    }
}
