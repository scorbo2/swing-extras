package ca.corbett.extras;

import ca.corbett.forms.SwingFormsResources;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.Border;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A custom panel that can show groups of related actions, either as
 * clickable JLabels or as JButtons. The styling of the panel and each
 * action group is highly customizable. By default, the current Look and Feel
 * defaults are applied, but you can modify fonts, colors, borders, and
 * spacing to fit your application's design.
 * <p>
 * <b>Adding actions</b> - actions are grouped into action groups.
 * Each action group will contain a header with the group name and
 * a control button for expanding/collapsing the group. You can add
 * actions one-by-one, or via addAll():
 * </p>
 * <ul>
 * <li><code>add(String groupName, EnhancedAction action)</code> - adds a single action to the specified group.</li>
 * <li><code>addAll(String groupName, List&lt;EnhancedAction&gt; actions)</code> - adds multiple actions to
 *     the specified group.</li>
 * </ul>
 * <p>
 * The same action can be added to more than one group if desired. If the named group
 * does not yet exist, it will be created.
 * </p>
 * <p>
 * Group names are case-insensitive; adding an action to group "File" is the same
 * as adding it to group "file" or "FILE". Group headers will be displayed using
 * the name as first specified when adding an action.
 * </p>
 * <p>
 * <b>Customizing action groups</b> - you can associate an icon with an action group,
 * and you can optionally specify a Comparator to control how actions within each
 * group are sorted. By default, action groups have no icons, and actions are listed
 * in the order in which they were added. To customize this:
 * </p>
 * <ul>
 * <li><code>setGroupIcon(String groupName, Icon icon)</code> - sets the icon for the specified group.</li>
 * <li><code>setGroupComparator(Comparator&lt;String&gt;>)</code> - determines the order of action groups
 *     within the ActionPanel. By default, groups are presented in the order they were added.</li>
 * <li><code>setActionComparator(Comparator&lt;EnhancedAction&gt; comparator)</code> - sets
 *     the comparator for sorting actions within groups.</li>
 * </ul>
 * <p>
 * <b>Styling options</b> - methods are provided to customize fonts, colors, borders,
 *   and spacing for the ActionPanel as a whole, as well as for individual action groups.
 *   By default, the current Look and Feel defaults are applied. You can modify:
 * </p>
 * <ul>
 * <li><b>Labels vs Buttons</b> - use <code>setUseLabels()</code> or <code>setUseButtons()</code> to
 *    choose whether actions are presented as clickable JLabels or as JButtons. Default is JLabels.</li>
 * <li><b>Fonts</b> - use <code>setActionFont()</code> and <code>setGroupHeaderFont()</code> to set fonts for
 *     actions and group headers, respectively.</li>
 * <li><b>Icons</b> - if your actions have icons, they will be displayed next to the action name by default.
 *    You can disable this by calling setShowActionIcons(false). Group headers can also have icons,
 *    which can be set using setGroupIcon(). You can disable group icons with setShowGroupIcons(false).</li>
 * <li><b>Colors</b> - use <code>setActionForeground()</code>, <code>setActionBackground()</code>,
 *    <code>setGroupHeaderForeground()</code>, and <code>setGroupHeaderBackground()</code> to set foreground
 *    and background colors for actions and group headers, respectively. The ActionPanel itself also has
 *    a background color, visible if externalPadding is set to a non-zero value. The background color
 *    can be set using <code>setBackground()</code> inherited from JPanel.</li>
 * <li><b>Borders</b> - use <code>setGroupBorder()</code> to set a border around action groups.
 *    The default is no border. Use <code>setGroupHeaderBorder()</code> to set a border
 *    around the group header. The default is no border.</li>
 * <li><b>Spacing</b> - you can control the spacing both within and around action groups:
 *    use <code>setInternalPadding()</code> to control the space between actions and the
 *    edges of the ActionPanel, and also between the actions themselves.
 *    Use <code>setExternalPadding()</code> to control the space between action groups, and the
 *    space between action groups and the edge of the ActionPanel.</li>
 * <li><b>Expand/collapse state</b> - the user can expand or collapse action groups by clicking
 *    the button in the group header. All action groups are expanded initially by default.
 *    You can programmatically expand or collapse groups by calling
 *    <code>setExpanded(String groupName, boolean expanded)</code> on the desired action group.</li>
 * <li><b>Animation</b> - by default, expand/collapse operations are animated with a smooth sliding effect.
 *    The default animation duration is 200ms. You can customize the animation speed using
 *    <code>setAnimationDurationMs(int ms)</code>, or disable animation entirely with
 *    <code>setAnimationEnabled(false)</code> to revert to instantaneous expand/collapse.
 *    Note that programmatically calling setExpanded() will always expand/collapse instantly, without animation.</li>
 * </ul>
 * <p>
 *     <b>Development TODOs:</b>
 * </p>
 * <ul>
 *     <li>Customizable icon sizing for action and group icons</li>
 *     <li>Make the expand/collapse button icon customizable</li>
 *     <li>Add an option to click or double-click the header label for expand/collapse?</li>
 *     <li>Consider adding mouse-over highlighting for action labels/buttons? Underline the label under
 *         the mouse so it looks more like a link?</li>
 *     <li>Consider adding keyboard navigation support?</li>
 *     <li>Add font choosers to demo app to test out customizing the header/action fonts</li>
 *     <li>Cheesy, but I want a "whoosh" sound effect for the expand/collapse animation. Just because.</li>
 *     <li>Listeners for expand/collapse events and/or actions added/removed?</li>
 * </ul>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
 */
public class ActionPanel extends JPanel {

    public static final int DEFAULT_INTERNAL_PADDING = 2;
    public static final int DEFAULT_EXTERNAL_PADDING = 8;
    public static final int DEFAULT_ANIMATION_DURATION_MS = 200;
    public static final int ANIMATION_FRAME_DELAY_MS = 10;

    private final List<ActionGroup> actionGroups;
    private Comparator<String> groupComparator;
    private Comparator<EnhancedAction> actionComparator;
    private boolean useLabels;
    private Border groupBorder;
    private Border groupHeaderBorder;
    private Font actionFont;
    private Font groupHeaderFont;
    private int internalPadding;
    private int externalPadding;
    private int actionIndent;
    private Color panelBackground;
    private Color actionBackground;
    private Color actionForeground;
    private Color groupHeaderBackground;
    private Color groupHeaderForeground;
    private boolean showActionIcons;
    private boolean showGroupIcons;
    private int animationDurationMs;
    private boolean animationEnabled;

    public ActionPanel() {
        this.actionGroups = new ArrayList<>();
        this.groupComparator = null; // Default to add order
        this.actionComparator = null; // Default to add order
        this.useLabels = true; // Default to using labels
        this.groupBorder = null;
        this.groupHeaderBorder = null;
        this.actionFont = null; // Use L&F default
        this.groupHeaderFont = null; // Use L&F default
        this.panelBackground = null; // Use L&F default
        this.actionForeground = null; // Use L&F default
        this.actionBackground = null; // Use L&F default
        this.groupHeaderForeground = null; // Use L&F default
        this.groupHeaderBackground = null; // Use L&F default
        this.internalPadding = DEFAULT_INTERNAL_PADDING;
        this.externalPadding = DEFAULT_EXTERNAL_PADDING;
        this.actionIndent = 0; // no indent by default
        this.showActionIcons = true; // visible by default (if the action has an icon set)
        this.showGroupIcons = true; // visible by default (if the group has an icon set)
        this.animationDurationMs = DEFAULT_ANIMATION_DURATION_MS;
        this.animationEnabled = true; // enabled by default
    }

    /**
     * Adds a single action to the specified group. If the group does not exist, it will be created.
     *
     * @param groupName The name of the group to add the action to.
     * @param action    The action to add.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel add(String groupName, EnhancedAction action) {
        if (groupName == null || action == null || groupName.isEmpty()) {
            throw new IllegalArgumentException("Group name and action cannot be null or empty.");
        }
        ActionGroup group = findOrCreateGroup(groupName);
        group.addAction(action);
        rebuild();
        return this;
    }

    /**
     * Adds multiple actions to the specified group. If the group does not exist, it will be created.
     *
     * @param groupName The name of the group to add the actions to.
     * @param actions   The list of actions to add.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel addAll(String groupName, List<EnhancedAction> actions) {
        if (groupName == null || groupName.isEmpty()) {
            throw new IllegalArgumentException("Group name cannot be null or empty.");
        }
        if (actions == null || actions.isEmpty()) {
            return this; // Just treat it as a no-op
        }
        ActionGroup group = findOrCreateGroup(groupName);
        for (EnhancedAction action : actions) {
            group.addAction(action);
        }
        rebuild();
        return this;
    }

    /**
     * Sets the icon for the specified group.
     *
     * @param groupName The name of the group.
     * @param icon      The icon to set.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setGroupIcon(String groupName, Icon icon) {
        if (groupName == null || groupName.isEmpty()) {
            throw new IllegalArgumentException("Group name cannot be null or empty.");
        }
        ActionGroup group = findOrCreateGroup(groupName);
        group.setIcon(icon);
        rebuild();
        return this;
    }

    /**
     * Sets the comparator for sorting actions within action groups.
     * By default, actions will be sorted in the order in which they were added to the group.
     * You can pass null as the comparator to revert to the default behavior.
     *
     * @param comparator The comparator to use for sorting actions, or null for default order.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setActionComparator(Comparator<EnhancedAction> comparator) {
        actionComparator = comparator;
        for (ActionGroup group : actionGroups) {
            group.setComparator(comparator);
        }
        rebuild();
        return this;
    }

    /**
     * Sets the comparator for ordering action groups within the ActionPanel.
     * By default, groups will be presented in the order in which they were added.
     * You can pass null as the comparator to revert to the default behavior.
     *
     * @param comparator The comparator to use for sorting groups, or null for default order.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setGroupComparator(Comparator<String> comparator) {
        this.groupComparator = comparator;
        rebuild();
        return this;
    }

    /**
     * Sets whether actions should be rendered as JLabels (clickable labels).
     *
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setUseLabels() {
        useLabels = true;
        rebuild();
        return this;
    }

    /**
     * Sets whether actions should be rendered as JButtons.
     *
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setUseButtons() {
        useLabels = false;
        rebuild();
        return this;
    }

    /**
     * Sets the font for action items. This overrides the Look and Feel default font.
     * You can pass null to revert to the L&F default.
     *
     * @param font The font to use for actions, or null to use the L&F default.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setActionFont(Font font) {
        actionFont = font;
        rebuild();
        return this;
    }

    /**
     * Sets the font for group headers. This overrides the Look and Feel default font.
     * You can pass null to revert to the L&F default.
     *
     * @param font The font to use for group headers, or null to use the L&F default.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setGroupHeaderFont(Font font) {
        groupHeaderFont = font;
        rebuild();
        return this;
    }

    /**
     * We have to override this because we use a wrapper panel internally to manage
     * our BoxLayout, so we need to store the background color and apply it to
     * the wrapper panel during rebuild().
     *
     * @param bg the desired background <code>Color</code>
     */
    @Override
    public void setBackground(Color bg) {
        panelBackground = bg;
        rebuild();
    }

    /**
     * Sets the foreground color for action items. If useLabels is true, this is the text
     * color; if useButtons is true, this is the button foreground color. This overrides
     * the Look and Feel default color. You can pass null to revert to the L&F default.
     *
     * @param color The foreground color, or null to use the L&F default.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setActionForeground(Color color) {
        actionForeground = color;
        rebuild();
        return this;
    }

    /**
     * Sets the background color for action items. This is the color that is shown
     * in the action area, behind the labels or buttons. Our labels are transparent,
     * but our buttons are not. So, if useLabels is false, this color will only be
     * visible in the padding areas around the buttons. It is not currently an option
     * to change the button background color itself.
     * <p>
     * This overrides the Look and Feel default color.
     * You can pass null to revert to the L&F default.
     * </p>
     *
     * @param color The background color, or null to use the L&F default.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setActionBackground(Color color) {
        actionBackground = color;
        rebuild();
        return this;
    }

    /**
     * Sets the foreground color for group headers. This overrides the
     * Look and Feel default color. You can pass null to revert to the L&F default.
     *
     * @param color The foreground color, or null to use the L&F default.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setGroupHeaderForeground(Color color) {
        groupHeaderForeground = color;
        rebuild();
        return this;
    }

    /**
     * Sets the background color for group headers. This overrides the
     * Look and Feel default color. You can pass null to revert to the L&F default.
     *
     * @param color The background color, or null to use the L&F default.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setGroupHeaderBackground(Color color) {
        groupHeaderBackground = color;
        rebuild();
        return this;
    }

    /**
     * Sets the border for action group headers.
     *
     * @param border The border to use, or null for no border.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setGroupHeaderBorder(Border border) {
        groupHeaderBorder = border;
        rebuild();
        return this;
    }

    /**
     * Sets the border for action groups.
     *
     * @param border The border to use, or null for no border.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setGroupBorder(Border border) {
        groupBorder = border;
        rebuild();
        return this;
    }

    /**
     * Sets the space between actions within an action group, and also the space between
     * the actions and the edges of the ActionPanel. The default is 2 pixels.
     *
     * @param padding The internal padding in pixels. Must be greater than or equal to 0.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setInternalPadding(int padding) {
        if (padding < 0) {
            throw new IllegalArgumentException("Internal padding cannot be negative.");
        }
        internalPadding = padding;
        rebuild();
        return this;
    }

    /**
     * Sets the space between action groups, and also the space between
     * the action groups and the edges of the ActionPanel. The default is 8 pixels.
     *
     * @param padding The external padding in pixels. Must be greater than or equal to 0.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setExternalPadding(int padding) {
        if (padding < 0) {
            throw new IllegalArgumentException("External padding cannot be negative.");
        }
        externalPadding = padding;
        rebuild();
        return this;
    }

    /**
     * Returns the left indent applied to action items within their group.
     *
     * @return The action indent in pixels.
     */
    public int getActionIndent() {
        return actionIndent;
    }

    /**
     * Sets an optional left indent to apply to action items within their group.
     * The default is 0 (no indent).
     *
     * @param actionIndent The action indent in pixels. Must be greater than or equal to 0.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setActionIndent(int actionIndent) {
        if (actionIndent < 0) {
            throw new IllegalArgumentException("Action indent cannot be negative.");
        }
        this.actionIndent = actionIndent;
        rebuild();
        return this;
    }

    /**
     * Reports whether action icons are shown next to action names. By default, if an action has an icon set,
     * it will be shown next to the action name. You can disable this by calling setShowActionIcons(false).
     */
    public boolean isShowActionIcons() {
        return showActionIcons;
    }

    /**
     * Controls whether action icons are shown next to action names. By default, if an action has an icon set,
     * it will be shown next to the action name. You can disable this by calling setShowActionIcons(false).
     */
    public ActionPanel setShowActionIcons(boolean showActionIcons) {
        this.showActionIcons = showActionIcons;
        rebuild();
        return this;
    }

    /**
     * Reports whether group icons are shown next to group names. By default, if a group has an icon set,
     * it will be shown next to the group name. You can disable this by calling setShowGroupIcons(false).
     */
    public boolean isShowGroupIcons() {
        return showGroupIcons;
    }

    /**
     * Controls whether group icons are shown next to group names. By default, if a group has an icon set,
     * it will be shown next to the group name. You can disable this by calling setShowGroupIcons(false).
     */
    public ActionPanel setShowGroupIcons(boolean showGroupIcons) {
        this.showGroupIcons = showGroupIcons;
        rebuild();
        return this;
    }

    /**
     * Reports whether the named group is currently expanded.
     *
     * @param groupName The name of the action group.
     * @return True if the group is expanded, false if it is collapsed.
     */
    public boolean isExpanded(String groupName) {
        if (groupName == null || groupName.isEmpty()) {
            throw new IllegalArgumentException("Group name cannot be null or empty.");
        }
        ActionGroup group = findOrCreateGroup(groupName);
        return group.isExpanded();
    }

    /**
     * Sets whether the named group is expanded or collapsed.
     *
     * @param groupName The name of the action group.
     * @param expanded  True to expand the group, false to collapse it.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setExpanded(String groupName, boolean expanded) {
        if (groupName == null || groupName.isEmpty()) {
            throw new IllegalArgumentException("Group name cannot be null or empty.");
        }
        ActionGroup group = findOrCreateGroup(groupName);
        group.setExpanded(expanded);
        rebuild(); // set instantly - no animation for programmatic changes
        return this;
    }

    /**
     * Returns whether animation is enabled for expand/collapse operations.
     *
     * @return True if animation is enabled, false otherwise.
     */
    public boolean isAnimationEnabled() {
        return animationEnabled;
    }

    /**
     * Sets whether animation is enabled for expand/collapse operations.
     * When disabled, groups will expand and collapse instantaneously.
     *
     * @param animationEnabled True to enable animation, false to disable it.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setAnimationEnabled(boolean animationEnabled) {
        this.animationEnabled = animationEnabled;
        return this;
    }

    /**
     * Returns the duration of the expand/collapse animation in milliseconds.
     *
     * @return The animation duration in milliseconds.
     */
    public int getAnimationDurationMs() {
        return animationDurationMs;
    }

    /**
     * Sets the duration of the expand/collapse animation in milliseconds.
     * The default is 200ms. A longer duration will result in a slower animation.
     *
     * @param animationDurationMs The animation duration in milliseconds. Must be greater than 0.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setAnimationDurationMs(int animationDurationMs) {
        if (animationDurationMs <= 0) {
            throw new IllegalArgumentException("Animation duration must be greater than 0.");
        }
        this.animationDurationMs = animationDurationMs;
        return this;
    }

    /**
     * Invoked internally to find an existing action group by name,
     * or create a new one if it does not exist. Group names are
     * case-insensitive.
     *
     * @param groupName The name of the action group.
     * @return The existing or newly created ActionGroup.
     */
    private ActionGroup findOrCreateGroup(String groupName) {
        for (ActionGroup group : actionGroups) {
            if (group.getName().equalsIgnoreCase(groupName)) {
                return group;
            }
        }
        ActionGroup newGroup = new ActionGroup(groupName);
        actionGroups.add(newGroup);
        return newGroup;
    }

    /**
     * Rebuilds the UI by clearing all components and re-rendering
     * all action groups based on current configuration.
     */
    private void rebuild() {
        // Clear existing components
        removeAll();

        // Wonky case: if we have no action groups, just return an empty panel
        if (actionGroups == null || actionGroups.isEmpty()) {
            revalidate();
            repaint();
            return;
        }

        // Set up the main layout - vertical box layout
        JPanel wrapperPanel = new JPanel();
        wrapperPanel.setLayout(new BoxLayout(wrapperPanel, BoxLayout.Y_AXIS));

        // Sort groups if comparator is set
        List<ActionGroup> sortedGroups = new ArrayList<>(actionGroups);
        if (groupComparator != null) {
            sortedGroups.sort((g1, g2) -> groupComparator.compare(g1.getName(), g2.getName()));
        }

        // Render each action group
        for (ActionGroup group : sortedGroups) {
            // Add vertical gap between groups:
            wrapperPanel.add(Box.createVerticalStrut(externalPadding));

            // Create the group container
            JPanel groupPanel = createGroupPanel(group);
            JPanel groupWrapperPanel = new JPanel(new BorderLayout());
            groupWrapperPanel.add(groupPanel);
            // Add left/right margin:
            int pad = externalPadding;
            groupWrapperPanel.setBorder(BorderFactory.createEmptyBorder(0, pad, 0, pad));
            groupWrapperPanel.add(groupPanel, BorderLayout.CENTER);
            wrapperPanel.add(groupWrapperPanel);

            // Apply panel background if set:
            if (panelBackground != null) {
                groupWrapperPanel.setBackground(panelBackground);
                groupWrapperPanel.setOpaque(true);
            }
        }

        // Add bottom margin if specified
        if (externalPadding > 0) {
            wrapperPanel.add(Box.createVerticalStrut(externalPadding));
        }

        // Add glue to push everything to the top
        wrapperPanel.add(Box.createVerticalGlue());

        // Apply panel background if set:
        if (panelBackground != null) {
            wrapperPanel.setBackground(panelBackground);
            wrapperPanel.setOpaque(true);
        }

        // Add the wrapper panel to this ActionPanel in a way that won't stretch it to fill our vertical space:
        // (BoxLayout is finicky that way... without this BorderLayout wrapper, the action groups will
        //  stretch vertically if there are not enough groups to fill the available space. This is especially
        //  a problem when collapsing all groups and watching the headers grow vertically.)
        setLayout(new BorderLayout());
        add(wrapperPanel, BorderLayout.NORTH);

        // Refresh the display:
        revalidate();
        repaint();
    }

    /**
     * Creates a panel for a single action group, including header and actions.
     */
    private JPanel createGroupPanel(ActionGroup group) {
        JPanel groupPanel = new JPanel();
        groupPanel.setLayout(new BoxLayout(groupPanel, BoxLayout.Y_AXIS));
        groupPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Apply group border if set
        if (groupBorder != null) {
            groupPanel.setBorder(groupBorder);
        }

        // Create and add the group header
        groupPanel.add(createGroupHeader(group));

        // Create the actions panel (always create it, even if collapsed)
        JPanel actionsPanel = createActionsPanel(group);

        // Create an animated wrapper for the actions panel
        AnimatedPanel animatedWrapper = new AnimatedPanel(actionsPanel);
        animatedWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Store reference to the animated wrapper in the group for later access
        group.setAnimatedWrapper(animatedWrapper);

        // Set initial state based on expanded state
        if (group.isExpanded()) {
            animatedWrapper.setFullyExpanded();
        }
        else {
            animatedWrapper.setFullyCollapsed();
        }

        groupPanel.add(animatedWrapper);

        return groupPanel;
    }

    /**
     * Creates the header panel for an action group.
     */
    private JPanel createGroupHeader(ActionGroup group) {
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setAlignmentX(Component.LEFT_ALIGNMENT); // no idea why, but this affects action panel alignment

        // Apply header border if set
        if (groupHeaderBorder != null) {
            wrapperPanel.setBorder(groupHeaderBorder);
        }

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapperPanel.add(headerPanel, BorderLayout.CENTER);

        // Add internal padding:
        if (internalPadding > 0) {
            int pad = internalPadding;
            headerPanel.setBorder(BorderFactory.createEmptyBorder(pad, pad, pad, pad));
        }

        // Apply background color if set
        if (groupHeaderBackground != null) {
            headerPanel.setBackground(groupHeaderBackground);
            headerPanel.setOpaque(true);
        }

        // Add group icon if present
        int pad = internalPadding;
        if (group.getIcon() != null && showGroupIcons) {
            JLabel iconLabel = new JLabel(group.getIcon());
            iconLabel.setBorder(BorderFactory.createEmptyBorder(pad, 0, pad, pad)); // left padded by headerPanel
            headerPanel.add(iconLabel);
        }

        // Add group name label
        JLabel nameLabel = new JLabel(group.getName());
        if (groupHeaderFont != null) {
            nameLabel.setFont(groupHeaderFont);
        }
        else {
            // Make it bold by default
            nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
        }
        if (groupHeaderForeground != null) {
            nameLabel.setForeground(groupHeaderForeground);
        }
        headerPanel.add(nameLabel);

        // Add glue to push everything to the left
        headerPanel.add(Box.createHorizontalGlue());

        // Add expand/collapse button:
        Icon icon = group.isExpanded()
                ? SwingFormsResources.getMinusIcon(16)
                : SwingFormsResources.getPlusIcon(16);
        JButton toggleButton = new JButton(icon);
        toggleButton.setToolTipText(group.isExpanded() ? "Collapse group" : "Expand group");
        toggleButton.setBorder(BorderFactory.createEmptyBorder(pad, pad, pad, 0)); // right padded by headerPanel
        toggleButton.setContentAreaFilled(false);
        toggleButton.setFocusPainted(false);
        toggleButton.addActionListener(e -> {
            boolean newExpandedState = !group.isExpanded();
            group.setExpanded(newExpandedState);

            // Update button icon and tooltip
            toggleButton.setIcon(newExpandedState
                                         ? SwingFormsResources.getMinusIcon(16)
                                         : SwingFormsResources.getPlusIcon(16));
            toggleButton.setToolTipText(newExpandedState ? "Collapse group" : "Expand group");

            // Animate the expand/collapse
            AnimatedPanel wrapper = group.getAnimatedWrapper();
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
        headerPanel.add(toggleButton);

        return wrapperPanel;
    }

    /**
     * Creates the actions panel for an action group.
     */
    private JPanel createActionsPanel(ActionGroup group) {
        JPanel actionsPanel = new JPanel();
        actionsPanel.setLayout(new BoxLayout(actionsPanel, BoxLayout.Y_AXIS));
        actionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Get the group actions (will be sorted if comparator is set)
        List<EnhancedAction> actions = group.getActions();

        // Add each action
        for (EnhancedAction action : actions) {
            JPanel wrapperPanel = new JPanel(new BorderLayout());
            wrapperPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            if (internalPadding > 0 || actionIndent > 0) {
                int pad = internalPadding;
                wrapperPanel.setBorder(BorderFactory.createEmptyBorder(pad, pad + actionIndent, pad, pad));
            }

            // Apply background color if set
            if (actionBackground != null) {
                wrapperPanel.setBackground(actionBackground);
                wrapperPanel.setOpaque(true);
            }

            Component actionComponent = createActionComponent(action);
            wrapperPanel.add(actionComponent, BorderLayout.CENTER);
            actionsPanel.add(wrapperPanel);
        }

        return actionsPanel;
    }

    /**
     * Creates a component (JButton or JLabel) for a single action.
     */
    private Component createActionComponent(EnhancedAction action) {
        Component component;

        if (useLabels) {
            // Create a clickable label
            JLabel label = new JLabel(action.getName());
            if (action.getIcon() != null && showActionIcons) {
                label.setIcon(action.getIcon());
                label.setIconTextGap(internalPadding);
            }
            if (action.getTooltip() != null) {
                label.setToolTipText(action.getTooltip());
            }

            // Apply styling
            if (actionFont != null) {
                label.setFont(actionFont);
            }
            if (actionForeground != null) {
                label.setForeground(actionForeground);
            }

            // Make it clickable
            label.setCursor(Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    action.actionPerformed(new ActionEvent(label, ActionEvent.ACTION_PERFORMED, action.getName()));
                }
            });

            // Labels are transparent by default - that's what we want
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            component = label;

        }
        else {
            // Create a button
            JButton button = new JButton(action);

            // Apply styling
            if (actionFont != null) {
                button.setFont(actionFont);
            }
            if (actionForeground != null) {
                button.setForeground(actionForeground);
            }
            if (!showActionIcons) {
                button.setIcon(null);
            }

            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            component = button;
        }

        return component;
    }

    /**
     * An internal class to represent a single action group.
     */
    private static class ActionGroup {
        private final String name;
        private Comparator<EnhancedAction> comparator;
        private final List<EnhancedAction> actionsAsAdded;
        private Icon icon;
        private boolean isExpanded;
        private AnimatedPanel animatedWrapper;

        public ActionGroup(String name) {
            this.name = name;
            this.comparator = null;
            this.actionsAsAdded = new ArrayList<>();
            this.isExpanded = true; // expanded by default
            this.icon = null;
            this.animatedWrapper = null;
        }

        public String getName() {
            return name;
        }

        public Comparator<EnhancedAction> getComparator() {
            return comparator;
        }

        public void setComparator(Comparator<EnhancedAction> comparator) {
            this.comparator = comparator;
        }

        public List<EnhancedAction> getActions() {
            List<EnhancedAction> sortedActions = new ArrayList<>(actionsAsAdded);
            if (comparator != null) {
                sortedActions.sort(comparator);
            }
            return sortedActions;
        }

        public boolean isExpanded() {
            return isExpanded;
        }

        public void setExpanded(boolean expanded) {
            isExpanded = expanded;
        }

        public void addAction(EnhancedAction action) {
            actionsAsAdded.add(action);
            if (comparator != null) {
                actionsAsAdded.sort(comparator);
            }
        }

        public Icon getIcon() {
            return icon;
        }

        public void setIcon(Icon icon) {
            this.icon = icon;
        }

        public AnimatedPanel getAnimatedWrapper() {
            return animatedWrapper;
        }

        public void setAnimatedWrapper(AnimatedPanel animatedWrapper) {
            this.animatedWrapper = animatedWrapper;
        }
    }

    /**
     * An internal panel that wraps the actions panel and provides animated expand/collapse functionality.
     * The panel gradually changes its preferred height to create a smooth sliding effect.
     */
    private static class AnimatedPanel extends JPanel {
        private final JPanel contentPanel;
        private int currentHeight;
        private int targetHeight;
        private Timer animationTimer;

        public AnimatedPanel(JPanel contentPanel) {
            this.contentPanel = contentPanel;
            setLayout(new BorderLayout());
            add(contentPanel, BorderLayout.CENTER);

            // Initialize heights
            this.targetHeight = contentPanel.getPreferredSize().height;
            this.currentHeight = targetHeight;
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension contentSize = contentPanel.getPreferredSize();
            return new Dimension(contentSize.width, currentHeight);
        }

        @Override
        public Dimension getMaximumSize() {
            Dimension contentSize = contentPanel.getMaximumSize();
            return new Dimension(contentSize.width, currentHeight);
        }

        /**
         * Sets the panel to fully expanded state (instantaneous).
         */
        public void setFullyExpanded() {
            stopAnimation();
            targetHeight = contentPanel.getPreferredSize().height;
            currentHeight = targetHeight;
            revalidate();
            repaint();
        }

        /**
         * Sets the panel to fully collapsed state (instantaneous).
         */
        public void setFullyCollapsed() {
            stopAnimation();
            targetHeight = 0;
            currentHeight = 0;
            revalidate();
            repaint();
        }

        /**
         * Animates the panel expanding to its full height.
         *
         * @param durationMs The duration of the animation in milliseconds.
         */
        public void animateExpand(int durationMs) {
            stopAnimation();
            targetHeight = contentPanel.getPreferredSize().height;

            if (currentHeight >= targetHeight) {
                // Already expanded
                currentHeight = targetHeight;
                revalidate();
                repaint();
                return;
            }

            startAnimation(durationMs);
        }

        /**
         * Animates the panel collapsing to zero height.
         *
         * @param durationMs The duration of the animation in milliseconds.
         */
        public void animateCollapse(int durationMs) {
            stopAnimation();
            targetHeight = 0;

            if (currentHeight <= 0) {
                // Already collapsed
                currentHeight = 0;
                revalidate();
                repaint();
                return;
            }

            startAnimation(durationMs);
        }

        /**
         * Starts the animation timer.
         */
        private void startAnimation(int durationMs) {
            final int startHeight = currentHeight;
            final int heightDifference = targetHeight - startHeight;
            final long startTime = System.currentTimeMillis();

            animationTimer = new Timer(ANIMATION_FRAME_DELAY_MS, e -> {
                long elapsed = System.currentTimeMillis() - startTime;
                double progress = Math.min(1.0, (double)elapsed / durationMs);

                // Use ease-in-out function for smoother animation
                double easedProgress = easeInOutCubic(progress);

                currentHeight = startHeight + (int)(heightDifference * easedProgress);

                revalidate();
                repaint();

                if (progress >= 1.0) {
                    currentHeight = targetHeight;
                    stopAnimation();
                    revalidate();
                    repaint();
                }
            });
            animationTimer.start();
        }

        /**
         * Stops any running animation.
         */
        private void stopAnimation() {
            if (animationTimer != null && animationTimer.isRunning()) {
                animationTimer.stop();
            }
        }

        /**
         * Easing function for smoother animation (ease-in-out cubic).
         */
        private double easeInOutCubic(double t) {
            if (t < 0.5) {
                return 4 * t * t * t;
            }
            else {
                double f = 2 * t - 2;
                return 1 + f * f * f / 2;
            }
        }
    }
}
