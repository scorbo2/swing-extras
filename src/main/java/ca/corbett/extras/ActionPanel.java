package ca.corbett.extras;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Font;
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
 * <li><code>setGroupComparator(String groupName, Comparator&lt;EnhancedAction&gt; comparator)</code> - sets
 *     the comparator for sorting actions within the specified group.</li>
 * <li><code>setGroup(String groupName, Icon icon, Comparator&lt;EnhancedAction&gt; comparator)</code> -
 *     convenience method for setting icon and Comparator at once.</li>
 * <li><code>setComparator(Comparator&lt;String&gt;>)</code> - determines the order of action groups
 *     within the ActionPanel. By default, groups are presented in the order they were added.</li>
 * </ul>
 * <p>
 * <b>Styling options</b> - methods are provided to customize fonts, colors, borders,
 *   and spacing for the ActionPanel as a whole, as well as for individual action groups.
 *   By default, the current Look and Feel defaults are applied. You can modify:
 * </p>
 * <ul>
 * <li><b>Labels vs Buttons</b> - use <code>setUseLabels()</code> or <code>setUseButtons()</code> to
 *    choose whether actions are presented as clickable JLabels or as JButtons. Default is JLabels.</li>
 * <li><b>Fonts</b> - use <code>setActionFont()</code> and <code>setGroupFont()</code> to set fonts for
 *     actions and group headers, respectively.</li>
 * <li><b>Colors</b> - use <code>setActionForeground()</code>, <code>setActionBackground()</code>,
 *    <code>setGroupForeground()</code>, and <code>setGroupBackground()</code> to set foreground
 *    and background colors for actions and group headers, respectively.</li>
 * <li><b>Borders</b> - use <code>setGroupBorder()</code> to set a border around action groups.
 *    The default is no border. Use <code>setGroupHeaderBorder()</code> to set a border
 *    around the group header. The default is no border.</li>
 * <li><b>Spacing</b> - use <code>setActionVGap()</code> and <code>setGroupVGap()</code> to set
 *    the vertical spacing between actions and between groups, respectively. Use <code>setGroupMargin()</code>
 *    to set the margin between action groups and the edge of the ActionPanel.</li>
 * </ul>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
 */
public class ActionPanel extends JPanel {

    private static final int DEFAULT_ACTION_VGAP = 2;
    private static final int DEFAULT_GROUP_VGAP = 8;

    private final List<ActionGroup> actionGroups;
    private Comparator<String> groupComparator;
    private boolean useLabels;
    private Border groupBorder;
    private Border groupHeaderBorder;
    private Font actionFont;
    private Font groupHeaderFont;
    private int groupMargin;
    private Color actionBackground;
    private Color actionForeground;
    private Color groupHeaderBackground;
    private Color groupHeaderForeground;
    private int actionVGap;
    private int groupVGap;

    public ActionPanel() {
        this.actionGroups = new ArrayList<>();
        this.groupComparator = null;
        this.useLabels = true; // Default to using labels
        this.groupBorder = null;
        this.groupHeaderBorder = null;
        this.groupMargin = 0;
        this.actionFont = null; // Use L&F default
        this.groupHeaderFont = null; // Use L&F default
        this.actionForeground = null; // Use L&F default
        this.actionBackground = null; // Use L&F default
        this.groupHeaderForeground = null; // Use L&F default
        this.groupHeaderBackground = null; // Use L&F default
        this.actionVGap = DEFAULT_ACTION_VGAP;
        this.groupVGap = DEFAULT_GROUP_VGAP;
    }

    /**
     * Adds a single action to the specified group. If the group does not exist, it will be created.
     *
     * @param groupName The name of the group to add the action to.
     * @param action    The action to add.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel add(String groupName, EnhancedAction action) {
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
        ActionGroup group = findOrCreateGroup(groupName);
        group.setIcon(icon);
        rebuild();
        return this;
    }

    /**
     * Sets the comparator for sorting actions within the specified group.
     *
     * @param groupName  The name of the group.
     * @param comparator The comparator to use for sorting actions.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setGroupComparator(String groupName, Comparator<EnhancedAction> comparator) {
        ActionGroup group = findOrCreateGroup(groupName);
        group.setComparator(comparator);
        rebuild();
        return this;
    }

    /**
     * Sets both the icon and comparator for the specified group.
     *
     * @param groupName  The name of the group.
     * @param icon       The icon to set.
     * @param comparator The comparator to use for sorting actions.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setGroup(String groupName, Icon icon, Comparator<EnhancedAction> comparator) {
        setGroupIcon(groupName, icon);
        setGroupComparator(groupName, comparator);
        return this;
    }

    /**
     * Sets the comparator for ordering action groups within the ActionPanel.
     *
     * @param comparator The comparator to use for sorting groups.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setComparator(Comparator<String> comparator) {
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
     * Sets the vertical gap between action items.
     * The default is 2 pixels.
     *
     * @param vgap The vertical gap in pixels.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setActionVGap(int vgap) {
        if (vgap < 0) {
            throw new IllegalArgumentException("Action vertical gap cannot be negative.");
        }
        actionVGap = vgap;
        rebuild();
        return this;
    }

    /**
     * Sets the vertical gap between action groups.
     * The default is 8 pixels.
     *
     * @param vgap The vertical gap in pixels.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setGroupVGap(int vgap) {
        if (vgap < 0) {
            throw new IllegalArgumentException("Group vertical gap cannot be negative.");
        }
        groupVGap = vgap;
        rebuild();
        return this;
    }

    /**
     * Sets the spacing value (in pixels) to use between action groups and the edge of the ActionPanel.
     * The default is 0 pixels.
     *
     * @param margin The margin in pixels. Must be greater than or equal to 0.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setGroupMargin(int margin) {
        if (margin < 0) {
            throw new IllegalArgumentException("Margin cannot be negative.");
        }
        groupMargin = margin;
        rebuild();
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

    private void rebuild() {
        // TODO: Rebuild the UI based on current state
    }


    private class ActionGroup {
        private final String name;
        private Comparator<EnhancedAction> comparator;
        private final List<EnhancedAction> actions;
        private Icon icon;
        private boolean isExpanded;

        public ActionGroup(String name) {
            this.name = name;
            this.comparator = null;
            this.actions = new ArrayList<>();
            this.isExpanded = false;
            this.icon = null;
        }

        public String getName() {
            return name;
        }

        public Comparator<EnhancedAction> getComparator() {
            return comparator;
        }

        public void setComparator(Comparator<EnhancedAction> comparator) {
            this.comparator = comparator;
            if (comparator != null) {
                actions.sort(comparator);
            }
        }

        public boolean isExpanded() {
            return isExpanded;
        }

        public void setExpanded(boolean expanded) {
            isExpanded = expanded;
        }

        public void addAction(EnhancedAction action) {
            actions.add(action);
            if (comparator != null) {
                actions.sort(comparator);
            }
        }

        public Icon getIcon() {
            return icon;
        }

        public void setIcon(Icon icon) {
            this.icon = icon;
        }
    }
}
