package ca.corbett.extras.actionpanel;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.forms.SwingFormsResources;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
 *    <code>setExpanded(String groupName, boolean expanded)</code> on the desired action group.
 *    You can optionally allow double-clicking on the group header label to also toggle
 *    the expanded/collapsed state, by calling <code>setAllowHeaderDoubleClick(true)</code>.
 *    This is disabled by default. You can disable user expand/collapse entirely by calling
 *    <code>setExpandable(false)</code> on the desired action group. This will "lock" all groups
 *    into the expanded state, and will remove the expand/collapse button. By default, expand/collapse is allowed.
 *    Note that programmatic expand/collapse is still allowed even if user expand/collapse is disabled.
 *    You can listen for expand/collapse events with <code>addExpandListener(ExpandListener listener)</code>.</li>
 * <li><b>Expand/collapse button icons</b> - by default, ActionPanel supplies built-in icons for use with
 *    the expand/collapse button in each group header. These look like a + and - sign. You can supply
 *    your own icons by calling <code>setExpandIcon(Icon icon)</code> and
 *    <code>setCollapseIcon(Icon icon)</code> on the ActionPanel.</li>
 * <li><b>Icon sizes</b> - by default, all icons (header icons, action icons, and the icons for the
 *    expand/collapse button) are rendered at 16x16 pixels. You can customize this size
 *    using <code>setHeaderIconSize(int size)</code> and <code>setActionIconSize(int size)</code>.
 *    The size applies to both width and height. Icons will be scaled as needed.</li>
 * <li><b>Animation</b> - by default, expand/collapse operations are animated with a smooth sliding effect.
 *    The default animation duration is 200ms. You can customize the animation speed using
 *    <code>setAnimationDurationMs(int ms)</code>, or disable animation entirely with
 *    <code>setAnimationEnabled(false)</code> to revert to instantaneous expand/collapse.
 *    Note that programmatically calling setExpanded() will always expand/collapse instantly, without animation.</li>
 * </ul>
 * <p>
 * For a complete working example of ActionPanel with all customization options, refer to the
 * demo application included with swing-extras! For more documentation and code examples,
 * refer to the <a href="https://www.corbett.ca/swing-extras-book/">swing-extras book</a>.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
 */
public class ActionPanel extends JPanel {

    public static final int DEFAULT_ICON_SIZE = 16;
    public static final int DEFAULT_INTERNAL_PADDING = 2;
    public static final int DEFAULT_EXTERNAL_PADDING = 8;
    public static final int DEFAULT_ANIMATION_DURATION_MS = 200;
    public static final int ANIMATION_FRAME_DELAY_MS = 10;

    private final List<ExpandListener> expandListeners = new CopyOnWriteArrayList<>();
    private final List<ActionGroup> actionGroups;
    private Comparator<String> groupComparator;
    private Comparator<EnhancedAction> actionComparator;
    private ActionComponentType componentType;
    private Border groupBorder;
    private Border groupHeaderBorder;
    private Font actionFont;
    private Font groupHeaderFont;
    private int headerInternalPadding;
    private int actionInternalPadding;
    private int toolBarInternalPadding;
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
    private boolean allowHeaderDoubleClick;
    private boolean allowExpandCollapse;
    private ImageIcon expandIcon;
    private ImageIcon collapseIcon;
    private int headerIconSize;
    private int actionIconSize;
    private boolean isToolBarEnabled;
    private final ToolBarOptions toolBarOptions;

    public ActionPanel() {
        this.actionGroups = new ArrayList<>();
        this.groupComparator = null; // Default to add order
        this.actionComparator = null; // Default to add order
        this.componentType = ActionComponentType.LABELS;
        this.groupBorder = null;
        this.groupHeaderBorder = null;
        this.actionFont = null; // Use L&F default
        this.groupHeaderFont = null; // Use L&F default
        this.panelBackground = null; // Use L&F default
        this.actionForeground = null; // Use L&F default
        this.actionBackground = null; // Use L&F default
        this.groupHeaderForeground = null; // Use L&F default
        this.groupHeaderBackground = null; // Use L&F default
        this.headerInternalPadding = DEFAULT_INTERNAL_PADDING;
        this.actionInternalPadding = DEFAULT_INTERNAL_PADDING;
        this.toolBarInternalPadding = DEFAULT_INTERNAL_PADDING;
        this.externalPadding = DEFAULT_EXTERNAL_PADDING;
        this.actionIndent = 0; // no indent by default
        this.showActionIcons = true; // visible by default (if the action has an icon set)
        this.showGroupIcons = true; // visible by default (if the group has an icon set)
        this.animationDurationMs = DEFAULT_ANIMATION_DURATION_MS;
        this.animationEnabled = true; // enabled by default
        this.allowHeaderDoubleClick = false; // disabled by default
        this.allowExpandCollapse = true; // why would you ever disable it? :)
        this.expandIcon = SwingFormsResources.getPlusIcon(DEFAULT_ICON_SIZE);
        this.collapseIcon = SwingFormsResources.getMinusIcon(DEFAULT_ICON_SIZE);
        this.headerIconSize = DEFAULT_ICON_SIZE;
        this.actionIconSize = DEFAULT_ICON_SIZE;
        this.isToolBarEnabled = false; // hide the ToolBar by default.
        this.toolBarOptions = new ToolBarOptions(); // moved to its own class to reduce clutter here
        this.toolBarOptions.addListener(() -> { // rebuild when our toolbar changes
            if (isToolBarEnabled) { rebuild(); }
        });
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
        group.add(action);
        rebuild();
        return this;
    }

    /**
     * Adds multiple actions to the specified group. If the group does not exist, it will be created.
     * The given list can be null or empty, in which case no actions are added to the group if it exists.
     * But, an empty group will be created in that case if it does not already exist.
     *
     * @param groupName The name of the group to add the actions to.
     * @param actions   The list of actions to add.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel addAll(String groupName, List<EnhancedAction> actions) {
        if (groupName == null || groupName.isEmpty()) {
            throw new IllegalArgumentException("Group name cannot be null or empty.");
        }
        ActionGroup group = findOrCreateGroup(groupName);
        if (actions != null && !actions.isEmpty()) {
            for (EnhancedAction action : actions) {
                group.add(action);
            }
        }
        rebuild();
        return this;
    }

    /**
     * Creates an empty group with the given group name. If the named group already exists,
     * this method does nothing (existing group contents are unchanged).
     *
     * @param groupName The name of the group to create.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel createEmptyGroup(String groupName) {
        return addAll(groupName, null);
    }

    /**
     * Returns true if any action group contains the specified action.
     *
     * @param action The action to check for.
     * @return True if the action exists in any group, false otherwise.
     */
    public boolean hasAction(EnhancedAction action) {
        if (action == null) {
            return false; // just ignore null
        }
        for (ActionGroup group : actionGroups) {
            if (group.getActions().contains(action)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the specified group contains the specified action.
     * Group names are case-insensitive. If the named group does not exist
     * in this action panel, false is returned (the group is not created).
     *
     * @param groupName The name of the group to check. Case-insensitive.
     * @param action    The action to check for.
     * @return True if the action exists in the specified group, false otherwise.
     */
    public boolean hasAction(String groupName, EnhancedAction action) {
        if (action == null) {
            return false; // just ignore null/empty
        }
        ActionGroup group = findGroup(groupName);
        if (group != null) {
            return group.hasAction(action);
        }
        return false;
    }

    /**
     * Returns true if the named action exists within any action group.
     *
     * @param actionName    The name of the action to check for.
     * @param caseSensitive Whether the name comparison should be case-sensitive.
     * @return True if the action exists in any group, false otherwise.
     */
    public boolean hasAction(String actionName, boolean caseSensitive) {
        for (ActionGroup group : actionGroups) {
            if (group.hasAction(actionName, caseSensitive)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the specified group contains an action with the specified name.
     * Group names are case-insensitive. If the named group does not exist
     * in this action panel, false is returned (the group is not created).
     *
     * @param groupName               The name of the group to check. Case-insensitive.
     * @param actionName              The name of the action to check for.
     * @param actionNameCaseSensitive Whether the action name comparison should be case-sensitive.
     * @return True if the action exists in the specified group, false otherwise.
     */
    public boolean hasAction(String groupName, String actionName, boolean actionNameCaseSensitive) {
        ActionGroup group = findGroup(groupName);
        if (group != null) {
            return group.hasAction(actionName, actionNameCaseSensitive);
        }
        return false;
    }

    /**
     * Removes all instances of the specified action from all action groups.
     *
     * @param action The action to remove.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel removeAction(EnhancedAction action) {
        if (action == null) {
            return this; // just ignore null
        }
        boolean removed = false;
        for (ActionGroup group : actionGroups) {
            if (group.remove(action)) {
                removed = true;
            }
        }
        if (removed) {
            rebuild();
        }
        return this;
    }

    /**
     * Removes the specified action from the named action group.
     * If the named group does not exist, no action is taken.
     *
     * @param groupName The name of the group to remove the action from.
     * @param action    The action to remove.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel removeAction(String groupName, EnhancedAction action) {
        ActionGroup group = findGroup(groupName);
        if (group != null) {
            if (group.remove(action)) {
                rebuild();
            }
        }
        return this;
    }

    /**
     * Removes all instances of the named action from all action groups.
     *
     * @param actionName    The name of the action to remove.
     * @param caseSensitive Whether the name comparison should be case-sensitive.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel removeAction(String actionName, boolean caseSensitive) {
        boolean removed = false;
        for (ActionGroup group : actionGroups) {
            if (group.remove(actionName, caseSensitive)) {
                removed = true;
            }
        }
        if (removed) {
            rebuild();
        }
        return this;
    }

    /**
     * Removes all instances of the named action from the named action group.
     * If the named group does not exist, no action is taken.
     * Group names are case-insensitive, but you can decide whether the action name
     * comparison should be case-sensitive.
     *
     * @param groupName               The name of the group to remove the action from. Case-insensitive.
     * @param actionName              The name of the action to remove.
     * @param actionNameCaseSensitive Whether the name comparison should be case-sensitive.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel removeAction(String groupName, String actionName, boolean actionNameCaseSensitive) {
        ActionGroup group = findGroup(groupName);
        if (group != null) {
            if (group.remove(actionName, actionNameCaseSensitive)) {
                rebuild();
            }
        }
        return this;
    }

    /**
     * Removes all actions from all groups, and then optionally also removes all action
     * groups that are empty.
     */
    public ActionPanel clear(boolean pruneEmptyGroups) {
        for (ActionGroup group : actionGroups) {
            group.clear();
        }
        if (pruneEmptyGroups) {
            actionGroups.removeIf(ActionGroup::isEmpty);
        }
        rebuild();
        return this;
    }

    /**
     * Returns a total count of all actions in all action groups.
     *
     * @return The total number of actions in this ActionPanel.
     */
    public int getActionCount() {
        int count = 0;
        for (ActionGroup group : actionGroups) {
            count += group.size();
        }
        return count;
    }

    /**
     * Returns the count of actions in the named group.
     * Group names are case-insensitive.
     *
     * @param groupName The name of the group.
     * @return The number of actions in the specified group, or 0 if the group does not exist.
     */
    public int getActionCount(String groupName) {
        ActionGroup group = findGroup(groupName);
        if (group != null) {
            return group.size();
        }
        return 0;
    }

    /**
     * Returns the number of action groups in this ActionPanel.
     *
     * @return The number of action groups.
     */
    public int getGroupCount() {
        return actionGroups.size();
    }

    /**
     * Reports whether a group with the given name exists in this ActionPanel.
     * Group names are case-insensitive.
     *
     * @param groupName The name of the group to check.
     * @return True if the group exists, false otherwise.
     */
    public boolean hasGroup(String groupName) {
        return findGroup(groupName) != null;
    }

    /**
     * Invoked internally to retrieve the actual ActionGroup instance for the given group name (case-insensitive).
     * This returns null if no group with the given name exists.
     * This is package-protected for internal package use only.
     *
     * @param groupName The name of the group to find. Case-insensitive.
     * @return The ActionGroup instance for the given group name, or null if no such group exists.
     */
    ActionGroup getGroup(String groupName) {
        return findGroup(groupName);
    }

    /**
     * Removes the specified action group entirely, along with all its actions.
     * If the named group does not exist, no action is taken.
     * Group names are case-insensitive.
     *
     * @param groupName The name of the group to remove.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel removeGroup(String groupName) {
        ActionGroup group = findGroup(groupName);
        if (group != null) {
            actionGroups.remove(group);
            rebuild();
        }
        return this;
    }

    /**
     * Attempts to rename the given group to the new name.
     * This may fail, if the old name does not reference any existing group.
     * It may also fail if the given new name is already in use by another group.
     * Group names are case-insensitive.
     *
     * @param oldName The current name of the group to rename. Case-insensitive.
     * @param newName The new name for the group. Case-insensitive. Cannot be null or empty.
     * @return True if the rename was successful, false otherwise (e.g. if oldName does not exist, or newName is already in use).
     */
    public boolean renameGroup(String oldName, String newName) {
        if (newName == null || newName.isEmpty()) {
            throw new IllegalArgumentException("New group name cannot be null or empty.");
        }

        // Make sure the requested group exists:
        ActionGroup group = findGroup(oldName);
        if (group == null) {
            return false;
        }

        // Make sure the new name is not already taken by a *different* group (case-insensitive):
        String newNameLower = newName.toLowerCase();
        boolean newNameInUse = actionGroups.stream()
                                           .filter(g -> g != group) // exclude group in question
                                           .map(ActionGroup::getName)
                                           .filter(name -> name != null)
                                           .anyMatch(name -> name.equalsIgnoreCase(newNameLower));
        if (newNameInUse) {
            return false;
        }

        // All good:
        group.renameTo(newName);
        rebuild();
        return true;
    }

    /**
     * Returns a list of all group names in this ActionPanel.
     * The returned list will be sorted if we have a group comparator.
     *
     * @return A list of all group names in this ActionPanel. May be empty.
     */
    public List<String> getGroupNames() {
        List<ActionGroup> sortedGroups = getSortedGroups();
        List<String> groupNames = new ArrayList<>();
        for (ActionGroup group : sortedGroups) {
            groupNames.add(group.getName());
        }
        return groupNames;
    }

    /**
     * Sets the icon for the specified group.
     * If the named group does not exist, it is created.
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
     * Returns the Comparator that is used to auto-sort the list of actions within each action group.
     * If null, actions are presented in the order in which they were added to the group.
     *
     * @return The Comparator used for sorting actions, or null if actions are presented in add order.
     */
    public Comparator<EnhancedAction> getActionComparator() {
        return actionComparator;
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
     * Shorthand for setActionComponentType(ActionComponentType.LABELS).
     *
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setUseLabels() {
        return setActionComponentType(ActionComponentType.LABELS);
    }

    /**
     * Sets whether actions should be rendered as JButtons.
     * Shorthand for setActionComponentType(ActionComponentType.BUTTONS).
     *
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setUseButtons() {
        return setActionComponentType(ActionComponentType.BUTTONS);
    }

    /**
     * Sets the component type that will be used to represent actions.
     * You can use setUseButtons() or setUseLabels() as shorthand methods.
     *
     * @param type The ActionComponentType to use.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setActionComponentType(ActionComponentType type) {
        if (type == null) {
            throw new IllegalArgumentException("ActionComponentType cannot be null.");
        }
        this.componentType = type;
        rebuild();
        return this;
    }

    /**
     * Reports the component type that is used to represent actions.
     *
     * @return The ActionComponentType in use.
     */
    public ActionComponentType getActionComponentType() {
        return componentType;
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
     * Returns the font for action items. May be null if Look and Feel defaults are in use.
     *
     * @return The action font, or null if L&F default is in use.
     */
    public Font getActionFont() {
        return actionFont;
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
     * Returns the font for group headers. May be null if Look and Feel defaults are in use.
     *
     * @return The group header font, or null if L&F default is in use.
     */
    public Font getGroupHeaderFont() {
        return groupHeaderFont;
    }

    /**
     * We have to override this because we use a wrapper panel internally to manage
     * our BoxLayout, so we need to store the background color and apply it to
     * the wrapper panel during rebuild(). Setting null here will revert to the
     * Look and Feel default background color.
     *
     * @param bg the desired background <code>Color</code>
     */
    @Override
    public void setBackground(Color bg) {
        panelBackground = bg;

        // If it's null, talk to our Look and Feel manager to get the default background color:
        if (panelBackground == null) {
            panelBackground = LookAndFeelManager.getLafColor("Panel.background", Color.LIGHT_GRAY);
        }

        rebuild();
    }

    /**
     * We override to return our own stored background color. If null was passed to
     * setBackground(), this will return the Look and Feel default Panel background color.
     *
     * @return the background <code>Color</code>
     */
    @Override
    public Color getBackground() {
        return panelBackground;
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
     * Returns the foreground color for action items. May be null if Look and Feel defaults
     * are in use.
     *
     * @return The action foreground color, or null if L&F default is in use.
     */
    public Color getActionForeground() {
        return actionForeground;
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
     * Returns the background color for action items. May be null if Look and Feel defaults
     * are in use.
     *
     * @return The action background color, or null if L&F default is in use.
     */
    public Color getActionBackground() {
        return actionBackground;
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
     * Returns the foreground color for group headers. May be null if Look and Feel defaults
     * are in use.
     *
     * @return The group header foreground color, or null if L&F default is in use.
     */
    public Color getGroupHeaderForeground() {
        return groupHeaderForeground;
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
     * Returns the background color for group headers. May be null if Look and Feel defaults
     * are in use.
     *
     * @return The group header background color, or null if L&F default is in use.
     */
    public Color getGroupHeaderBackground() {
        return groupHeaderBackground;
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
     * Returns the border for action group headers, or null if no border is set.
     * This border is applied to the header area of each action group, which includes the group name and the
     * expand/collapse button. The default is no border.
     *
     * @return A Border to use for group headers, or null for no border.
     */
    public Border getGroupHeaderBorder() {
        return groupHeaderBorder;
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
     * Returns the border for action groups, or null if no border is set.
     * This border is applied around the entire area of each action group,
     * including the group header and all actions within the group. The default is no border.
     *
     * @return A Border to use for action groups, or null for no border.
     */
    public Border getGroupBorder() {
        return groupBorder;
    }

    /**
     * Sets the space between components within the header of an ActionGroup. The default is 2 pixels.
     *
     * @param padding The header's internal padding in pixels. Must be greater than or equal to 0.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setHeaderInternalPadding(int padding) {
        if (padding < 0) {
            throw new IllegalArgumentException("Header padding cannot be negative.");
        }
        headerInternalPadding = padding;
        rebuild();
        return this;
    }

    /**
     * Returns the internal padding between components in the header of an ActionGroup, in pixels.
     *
     * @return The internal padding in pixels.
     */
    public int getHeaderInternalPadding() {
        return headerInternalPadding;
    }

    /**
     * Sets the space between action labels/buttons in an ActionGroup. The default is 2 pixels.
     *
     * @param padding The internal padding between actions in pixels. Must be greater than or equal to 0.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setActionInternalPadding(int padding) {
        if (padding < 0) {
            throw new IllegalArgumentException("Action padding cannot be negative.");
        }
        actionInternalPadding = padding;
        rebuild();
        return this;
    }

    /**
     * Returns the internal padding between action labels/buttons in an ActionGroup, in pixels.
     *
     * @return The internal padding between actions in pixels.
     */
    public int getActionInternalPadding() {
        return actionInternalPadding;
    }

    /**
     * Sets the space between components in the ToolBar area. This is the padding that is applied
     * around the components in the ToolBar, and between them. This only applies if the
     * ToolBar's ButtonPosition is not Stretch. In "Stretch" mode, there is no space
     * between ToolBarButtons, no matter what this padding is set to, because the buttons
     * are stretched to fill all available space.
     *
     * @param padding The internal padding for the ToolBar in pixels. Must be greater than or equal to 0.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setToolBarInternalPadding(int padding) {
        if (padding < 0) {
            throw new IllegalArgumentException("ToolBar padding cannot be negative.");
        }
        toolBarInternalPadding = padding;
        rebuild();
        return this;
    }

    /**
     * Returns the internal padding for the ToolBar area, in pixels. This is the padding that is applied
     * around the components in the ToolBar, and between them. This only applies if the
     * ToolBar's ButtonPosition is not Stretch. In "Stretch" mode, there is no space
     * between ToolBarButtons, no matter what this padding is set to, because the buttons
     * are stretched to fill all available space.
     *
     * @return The internal padding for the ToolBar in pixels.
     */
    public int getToolBarInternalPadding() {
        return toolBarInternalPadding;
    }

    /**
     * Allows or disallows the user to expand/collapse action groups.
     * By default, this is allowed. Note that programmatic expand/collapse
     * via setExpanded() is always allowed regardless of this setting.
     *
     * @param allow True to allow expand/collapse, false to disallow.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setExpandable(boolean allow) {
        this.allowExpandCollapse = allow;

        // Force-expand any currently collapsed groups if we're disallowing expand/collapse,
        // to avoid leaving the user with no way to see the contents of those groups.
        if (!allow) {
            for (ActionGroup group : actionGroups) {
                group.setExpanded(true);
            }
        }

        rebuild();
        return this;
    }

    /**
     * Reports whether the user is allowed to expand/collapse action groups.
     *
     * @return True if expand/collapse is allowed, false otherwise.
     */
    public boolean isExpandable() {
        return allowExpandCollapse;
    }

    /**
     * Sets the size (width and height) at which header icons are rendered.
     * The default is 16 pixels. Icons will be scaled as needed.
     *
     * @param size The icon size in pixels. Must be greater than 0.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setHeaderIconSize(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Icon size must be greater than 0.");
        }
        this.headerIconSize = size;
        rebuild();
        return this;
    }

    /**
     * Returns the size (width and height) at which header icons are rendered.
     *
     * @return The header icon size in pixels.
     */
    public int getHeaderIconSize() {
        return headerIconSize;
    }

    /**
     * Sets the size (width and height) at which action icons are rendered.
     * The default is 16 pixels. Icons will be scaled as needed.
     *
     * @param size The icon size in pixels. Must be greater than 0.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setActionIconSize(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Icon size must be greater than 0.");
        }
        this.actionIconSize = size;
        rebuild();
        return this;
    }

    /**
     * Returns the size (width and height) at which action icons are rendered.
     *
     * @return The action icon size in pixels.
     */
    public int getActionIconSize() {
        return actionIconSize;
    }

    /**
     * Sets the icon to use for the expand button in action group headers.
     *
     * @param icon The expand icon. Cannot be null.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setExpandIcon(ImageIcon icon) {
        if (icon == null) {
            throw new IllegalArgumentException("Expand icon cannot be null. Use setExpandable(false) to hide it.");
        }
        this.expandIcon = icon;
        rebuild();
        return this;
    }

    /**
     * Returns the icon used for the expand button in action group headers.
     *
     * @return The expand icon.
     */
    public ImageIcon getExpandIcon() {
        return expandIcon;
    }

    /**
     * Sets the icon to use for the collapse button in action group headers.
     *
     * @param icon The collapse icon. Cannot be null.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setCollapseIcon(ImageIcon icon) {
        if (icon == null) {
            throw new IllegalArgumentException("Collapse icon cannot be null. Use setExpandable(false) to hide it.");
        }
        this.collapseIcon = icon;
        rebuild();
        return this;
    }

    /**
     * Returns the icon used for the collapse button in action group headers.
     *
     * @return The collapse icon.
     */
    public ImageIcon getCollapseIcon() {
        return collapseIcon;
    }

    /**
     * Allows or disallows double-clicking on the group header label
     * to toggle the expanded/collapsed state of the group.
     * By default, this is disabled.
     *
     * @param allow True to allow double-clicking on the header to toggle expand/collapse.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setAllowHeaderDoubleClick(boolean allow) {
        this.allowHeaderDoubleClick = allow;
        rebuild();
        return this;
    }

    /**
     * Reports whether double-clicking on the group header label
     * is allowed to toggle the expanded/collapsed state of the group.
     *
     * @return True if double-clicking on the header toggles expand/collapse, false otherwise.
     */
    public boolean isAllowHeaderDoubleClick() {
        return allowHeaderDoubleClick;
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
     * Returns the external padding between action groups and the edges of the ActionPanel.
     *
     * @return The external padding in pixels.
     */
    public int getExternalPadding() {
        return externalPadding;
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
     * If the named group does not exist, returns false.
     * Group names are case-insensitive.
     *
     * @param groupName The name of the action group.
     * @return True if the group is expanded, false if it is collapsed.
     */
    public boolean isExpanded(String groupName) {
        if (groupName == null || groupName.isEmpty()) {
            throw new IllegalArgumentException("Group name cannot be null or empty.");
        }
        ActionGroup group = findGroup(groupName);
        return group != null && group.isExpanded();
    }

    /**
     * Sets whether the named group is expanded or collapsed.
     * If the named group does not exist, does nothing.
     * Group names are case-insensitive.
     *
     * @param groupName The name of the action group.
     * @param expanded  True to expand the group, false to collapse it.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setExpanded(String groupName, boolean expanded) {
        if (groupName == null || groupName.isEmpty()) {
            throw new IllegalArgumentException("Group name cannot be null or empty.");
        }
        ActionGroup group = findGroup(groupName);
        if (group == null) {
            return this; // group does not exist - do nothing
        }
        group.setExpanded(expanded);
        rebuild(); // set instantly - no animation for programmatic changes
        fireExpandEvent(groupName, expanded);
        return this;
    }

    /**
     * Toggles the expanded/collapsed state of the named group.
     * If the named group does not exist, does nothing.
     * Group names are case-insensitive.
     *
     * @param groupName The name of the action group.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel toggleExpanded(String groupName) {
        if (groupName == null || groupName.isEmpty()) {
            throw new IllegalArgumentException("Group name cannot be null or empty.");
        }
        ActionGroup group = findGroup(groupName);
        if (group == null) {
            return this; // group does not exist - do nothing
        }
        group.setExpanded(!group.isExpanded());
        rebuild(); // set instantly - no animation for programmatic changes
        fireExpandEvent(groupName, group.isExpanded());
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
        rebuild();
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
        rebuild(); // do we seriously need to rebuild for this?
        return this;
    }

    /**
     * Reports whether the ToolBar is enabled. When enabled, the ToolBar is shown at the bottom of each ActionGroup.
     * It contains buttons related to the actions in that group. To configure the ToolBar,
     * use the ToolBarOptions object accessible via getToolBarOptions().
     *
     * @return True if the ToolBar is enabled, false otherwise.
     */
    public boolean isToolBarEnabled() {
        return isToolBarEnabled;
    }

    /**
     * Enables or disables the ToolBar, which is shown at the bottom of each ActionGroup and contains
     * buttons related to the actions in that group. To configure the ToolBar, use the ToolBarOptions
     * object accessible via getToolBarOptions().
     *
     * @param enabled True to enable the ToolBar, false to disable it.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setToolBarEnabled(boolean enabled) {
        this.isToolBarEnabled = enabled;
        rebuild();
        return this;
    }

    /**
     * Options related to the ToolBar are accessed via the ToolBarOptions class.
     * Developer note: yeah, they could all live here in this class, but this class
     * is already unreasonably large, and the ToolBar has a somewhat complicated
     * setup, so they were all moved over there. The only first-class ToolBar
     * option still in ActionPanel is isToolBarEnabled(), which is the master
     * switch that controls whether the ToolBar is shown at all.
     *
     * @return The ToolBarOptions instance containing options related to the ToolBar.
     */
    public ToolBarOptions getToolBarOptions() {
        return toolBarOptions;
    }

    /**
     * You can listen for group expand/collapse events by adding an ExpandListener.
     */
    public ActionPanel addExpandListener(ExpandListener listener) {
        if (listener != null) {
            expandListeners.add(listener);
        }
        return this;
    }

    /**
     * You can stop listening for group expand/collapse events by removing an ExpandListener.
     */
    public ActionPanel removeExpandListener(ExpandListener listener) {
        expandListeners.remove(listener);
        return this;
    }

    /**
     * Can be invoked to notify listeners about a group expand/collapse event.
     *
     * @param groupName The name of the group that changed.
     * @param expanded  True if the group is now expanded, false if collapsed.
     */
    void fireExpandEvent(String groupName, boolean expanded) {
        for (ExpandListener listener : expandListeners) {
            listener.groupExpandedChanged(groupName, expanded);
        }
    }

    /**
     * Invoked internally to get a sorted list of action groups.
     * If we have no group comparator, the returned list is just a straight copy of actionGroups.
     */
    private List<ActionGroup> getSortedGroups() {
        List<ActionGroup> sortedGroups = new ArrayList<>(actionGroups);
        if (groupComparator != null) {
            sortedGroups.sort((g1, g2) -> groupComparator.compare(g1.getName(), g2.getName()));
        }
        return sortedGroups;
    }

    /**
     * Invoked internally to find an existing action group by name.
     * Group names are case-insensitive.
     *
     * @param groupName The name of the action group.
     * @return The existing ActionGroup, or null if not found.
     */
    private ActionGroup findGroup(String groupName) {
        if (groupName == null || groupName.isBlank()) {
            return null;
        }
        for (ActionGroup group : actionGroups) {
            if (group.getName().equalsIgnoreCase(groupName)) {
                return group;
            }
        }
        return null;
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
        ActionGroup group = findGroup(groupName);
        if (group != null) {
            return group;
        }
        ActionGroup newGroup = new ActionGroup(groupName);
        newGroup.setComparator(actionComparator); // let new groups know about our action comparator
        actionGroups.add(newGroup);
        return newGroup;
    }

    /**
     * Rebuilds the UI by clearing all components and re-rendering
     * all action groups based on current configuration.
     */
    void rebuild() {
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
        List<ActionGroup> sortedGroups = getSortedGroups();

        // Render each action group
        boolean isFirstGroup = true;
        for (ActionGroup group : sortedGroups) {
            wrapperPanel.add(new GroupContainer(this, group, isFirstGroup ? externalPadding : 0));
            isFirstGroup = false;
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
}
