package ca.corbett.extras.actionpanel;

import ca.corbett.extras.image.ImageUtil;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;

/**
 * The HeaderBar consists of an optional icon, a title label, and an expand/collapse button.
 * It is used as the header for an ActionGroup within an ActionPanel.
 * This class is package-private, so callers do not interact with it directly.
 * Use the ActionPanel API instead.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
 */
class HeaderBar extends JPanel {

    private final ActionPanel actionPanel;
    private final ActionGroup group;
    private final JPanel headerPanel;
    private JButton expandCollapseButton;
    private final DoubleClickListener doubleClickListener = new DoubleClickListener();

    /**
     * Creates a new HeaderBar for the specified ActionGroup within the given ActionPanel.
     *
     * @param actionPanel The ActionPanel that contains this header bar.
     * @param actionGroup The ActionGroup that this header bar represents.
     */
    public HeaderBar(ActionPanel actionPanel, ActionGroup actionGroup) {
        if (actionPanel == null || actionGroup == null) {
            throw new IllegalArgumentException("ActionPanel and ActionGroup cannot be null");
        }

        this.actionPanel = actionPanel;
        this.group = actionGroup;
        setLayout(new BorderLayout());
        setAlignmentX(Component.LEFT_ALIGNMENT);
        addMouseListener(doubleClickListener);

        // Apply header border if set. Note that for margin reasons, we set this border
        // on the HeaderBar itself, not the internal headerPanel.
        if (actionPanel.getGroupHeaderBorder() != null) {
            setBorder(actionPanel.getGroupHeaderBorder());
        }

        headerPanel = createHeaderPanel();
        addIcon();
        addLabel();
        addToggleButton();
        add(headerPanel, BorderLayout.CENTER);
    }

    /**
     * Invoked internally to create, style, and return the internal header panel.
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Add internal padding - this is the space between the outer edge of the HeaderBar
        // and the internal components.
        if (actionPanel.getHeaderInternalPadding() > 0) {
            int pad = actionPanel.getHeaderInternalPadding();
            headerPanel.setBorder(BorderFactory.createEmptyBorder(pad, pad, pad, pad));
        }

        // Apply background color if set:
        if (actionPanel.getGroupHeaderBackground() != null) {
            headerPanel.setBackground(actionPanel.getGroupHeaderBackground());
            headerPanel.setOpaque(true);
        }

        return headerPanel;
    }

    /**
     * If the group has an icon and group icons are enabled, adds the icon to the header panel.
     */
    private void addIcon() {
        int pad = actionPanel.getHeaderInternalPadding();
        if (group.getIcon() != null && actionPanel.isShowGroupIcons()) {
            Icon icon = null;
            if (group.getIcon() instanceof ImageIcon) {
                icon = ImageUtil.scaleIcon((ImageIcon)group.getIcon(), actionPanel.getHeaderIconSize());
            }
            JLabel iconLabel = new JLabel(icon);
            iconLabel.addMouseListener(doubleClickListener);

            // The headerPanel itself has padding on all sides, so we only need to pad
            // the right side of the icon here, to keep it away from the label.
            iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, pad));

            headerPanel.add(iconLabel);
        }
    }

    /**
     * Adds the title label to the header panel.
     */
    private void addLabel() {
        JLabel nameLabel = new JLabel(group.getName());

        // Set the font to use:
        if (actionPanel.getGroupHeaderFont() != null) {
            nameLabel.setFont(actionPanel.getGroupHeaderFont());
        }
        else {
            // If there's no group header font explicitly set, then we are
            // using the defaults from the Look and Feel. But, since this
            // is a header, we want it to be bold.
            nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
        }

        // Set the foreground color:
        if (actionPanel.getGroupHeaderForeground() != null) {
            nameLabel.setForeground(actionPanel.getGroupHeaderForeground());
        }

        // If double-click is allowed to toggle expand/collapse, set that up:
        nameLabel.addMouseListener(doubleClickListener);

        // Add the label and use glue to push everything so far to the left:
        headerPanel.add(nameLabel);
        headerPanel.add(Box.createHorizontalGlue());
    }

    /**
     * Invoked internally to add the expand/collapse toggle button to the header panel.
     */
    private void addToggleButton() {
        // This can be disallowed by the ActionPanel:
        if (!actionPanel.isExpandable()) {
            return; // no button for you!
        }

        // Get our icons at proper scale:
        final ImageIcon expandIcon = getExpandIcon();
        final ImageIcon collapseIcon = getCollapseIcon();

        // Create the toggle button:
        expandCollapseButton = new JButton(group.isExpanded() ? collapseIcon : expandIcon);
        expandCollapseButton.setToolTipText(group.isExpanded() ? "Collapse group" : "Expand group");
        expandCollapseButton.setContentAreaFilled(false);
        expandCollapseButton.setFocusPainted(false);

        // The headerPanel itself has padding on all sides, so we only need to pad
        // the left side of the button here, to keep it away from the label.
        int pad = actionPanel.getHeaderInternalPadding();
        expandCollapseButton.setBorder(BorderFactory.createEmptyBorder(0, pad, 0, 0));

        // The listener for the button will either trigger an animation or just
        // instantly expand/collapse, based on the ActionPanel settings:
        final boolean animationEnabled = actionPanel.isAnimationEnabled();
        final int animationDurationMs = actionPanel.getAnimationDurationMs();
        expandCollapseButton.addActionListener(e -> {
            boolean newExpandedState = !group.isExpanded();
            group.setExpanded(newExpandedState);
            actionPanel.fireExpandEvent(group.getName(), newExpandedState);

            // Update button icon and tooltip
            expandCollapseButton.setIcon(newExpandedState ? collapseIcon : expandIcon);
            expandCollapseButton.setToolTipText(newExpandedState ? "Collapse group" : "Expand group");

            // Animate the expand/collapse
            AnimatedPanel wrapper = group.getAnimatedWrapper(); // can't be null, we set this up
            if (wrapper != null) {
                if (animationEnabled) {
                    if (newExpandedState) {
                        wrapper.animateExpand(animationDurationMs);
                    }
                    else {
                        wrapper.animateCollapse(animationDurationMs);
                    }
                }
                else {
                    // Instant expand/collapse
                    if (newExpandedState) {
                        wrapper.setFullyExpanded();
                    }
                    else {
                        wrapper.setFullyCollapsed();
                    }
                    revalidate();
                    repaint();
                }
            }
        });

        headerPanel.add(expandCollapseButton);
    }

    /**
     * Gets the currently configured expand icon, scaled to the proper size if needed.
     */
    private ImageIcon getExpandIcon() {
        ImageIcon icon = actionPanel.getExpandIcon();
        if (icon.getIconWidth() != actionPanel.getHeaderIconSize()) {
            icon = ImageUtil.scaleIcon(icon, actionPanel.getHeaderIconSize());
        }
        return icon;
    }

    /**
     * Gets the currently configured collapse icon, scaled to the proper size if needed.
     */
    private ImageIcon getCollapseIcon() {
        ImageIcon icon = actionPanel.getCollapseIcon();
        if (icon.getIconWidth() != actionPanel.getHeaderIconSize()) {
            icon = ImageUtil.scaleIcon(icon, actionPanel.getHeaderIconSize());
        }
        return icon;
    }

    /**
     * A simple mouse listener that will trigger an expand/collapse event on the
     * header bar when it detects a double-click.
     */
    private class DoubleClickListener extends MouseAdapter {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            // This can be disabled by the ActionPanel:
            if (!actionPanel.isAllowHeaderDoubleClick() || !actionPanel.isExpandable()) {
                return;
            }

            if (e.getClickCount() == 2) {
                // We don't want to just invoke actionPanel.toggleExpanded() here,
                // because we would bypass the animation if it's enabled.
                // So, let's simulate a click on the toggle button instead:
                expandCollapseButton.doClick();
            }
        }
    }
}
