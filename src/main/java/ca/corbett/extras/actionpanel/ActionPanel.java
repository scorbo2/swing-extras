package ca.corbett.extras.actionpanel;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.forms.Margins;
import ca.corbett.forms.SwingFormsResources;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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
 * the name as first specified when adding an action. Groups can be renamed
 * using <code>setGroupName(String oldName, String newName)</code>, which does allow
 * you to change the case of a group.
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
 * <li><b>Colors</b> - all colors are highly customizable. These options are found in the ColorOptions
 *    class, which you can access with getColorOptions(). Refer to ColorOptions class for more details.</li>
 * <li><b>Borders</b> - all borders are highly customizable. These options are found in the BorderOptions
 *    class, which you can access with getBorderOptions().Refer to BorderOptions class for more details.</li>
 * <li><b>Spacing</b> - you can control the spacing both within and around action groups:
 *    use <code>setInternalPadding()</code> to control the space between actions and the
 *    edges of the ActionPanel, and also between the actions themselves.
 *    Use <code>setExternalPadding()</code> to control the space between action groups, and the
 *    space between action groups and the edge of the ActionPanel.</li>
 * <li><b>Expand/collapse state</b> - the user can expand or collapse action groups by clicking
 *    the button in the group header. All action groups are expanded initially by default.
 *    Options related to group expansion and collapse are found in the ExpandCollapseOptions class,
 *    which you can access with getExpandCollapseOptions().
 *    Refer to the ExpandCollapseOptions class for more details on how to customize expand/collapse
 *    behavior and animation.
 *    You can listen for expand/collapse events with <code>addExpandListener(ExpandListener listener)</code>.</li>
 * <li><b>Expand/collapse button icons</b> - by default, ActionPanel supplies built-in icons for use with
 *    the expand/collapse button in each group header. These look like a + and - sign. You can supply
 *    your own icons by calling <code>setExpandIcon(Icon icon)</code> and
 *    <code>setCollapseIcon(Icon icon)</code> on the ActionPanel.</li>
 * <li><b>Icon sizes</b> - by default, all icons (header icons, action icons, and the icons for the
 *    expand/collapse button) are rendered at 16x16 pixels. You can customize this size
 *    using <code>setHeaderIconSize(int size)</code> and <code>setActionIconSize(int size)</code>.
 *    The size applies to both width and height. Icons will be scaled as needed.</li>
 * </ul>
 * <p>
 *     <b>Toolbars</b> - call setToolBarEnabled(true) to show a toolbar with each action group.
 *     The toolbar is highly configurable! There are built-in actions that allow you to support
 *     adding new actions to a group, renaming a group, reordering or removing items in a group,
 *     or removing groups. You can also supply custom actions to be displayed in the toolbar.
 *     Refer to ToolBarOptions for details on how to customize the toolbar and the actions it contains.
 *     Access the ToolBarOptions with getToolBarOptions().
 * </p>
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

    private final List<ExpandListener> expandListeners = new CopyOnWriteArrayList<>();
    private final List<ActionGroup> actionGroups;
    private EnhancedAction highlightedAction;
    private boolean highlightLastAction;
    private Comparator<String> groupComparator;
    private Comparator<EnhancedAction> actionComparator;
    private ActionComponentType componentType;
    private Container cardContainer;
    private Font actionFont;
    private Font groupHeaderFont;
    private final Margins headerMargins;
    private final Margins actionTrayMargins;
    private final Margins toolBarMargins;
    private final Margins actionGroupMargins;
    private int actionIconTextGap;
    private boolean showActionIcons;
    private boolean showGroupIcons;
    private ImageIcon expandIcon;
    private ImageIcon collapseIcon;
    private int headerIconSize;
    private int actionIconSize;
    private boolean isToolBarEnabled;
    private final ColorOptions colorOptions;
    private final ToolBarOptions toolBarOptions;
    private final ExpandCollapseOptions expandCollapseOptions;
    private final BorderOptions borderOptions;
    private boolean autoRebuildEnabled;

    public ActionPanel() {
        this.actionGroups = new ArrayList<>();
        this.groupComparator = null; // Default to add order
        this.actionComparator = null; // Default to add order
        this.highlightedAction = null;
        this.highlightLastAction = false;
        this.componentType = ActionComponentType.LABELS;
        this.cardContainer = null;
        this.actionFont = null; // Use L&F default
        this.groupHeaderFont = null; // Use L&F default
        this.headerMargins = new Margins(DEFAULT_INTERNAL_PADDING);
        this.actionTrayMargins = new Margins(DEFAULT_INTERNAL_PADDING);
        this.toolBarMargins = new Margins(DEFAULT_INTERNAL_PADDING);
        this.actionGroupMargins = new Margins(DEFAULT_EXTERNAL_PADDING);
        this.headerMargins.addListener(m -> rebuild()); // rebuild on change
        this.actionTrayMargins.addListener(m -> rebuild());
        this.toolBarMargins.addListener(m -> rebuild());
        this.actionGroupMargins.addListener(m -> rebuild());
        this.actionIconTextGap = 4; // default gap between action icons and text
        this.showActionIcons = true; // visible by default (if the action has an icon set)
        this.showGroupIcons = true; // visible by default (if the group has an icon set)
        this.expandIcon = SwingFormsResources.getPlusIcon(DEFAULT_ICON_SIZE);
        this.collapseIcon = SwingFormsResources.getMinusIcon(DEFAULT_ICON_SIZE);
        this.headerIconSize = DEFAULT_ICON_SIZE;
        this.actionIconSize = DEFAULT_ICON_SIZE;
        this.isToolBarEnabled = false; // hide the ToolBar by default.
        this.colorOptions = new ColorOptions(); // moved to its own class to reduce clutter here
        this.toolBarOptions = new ToolBarOptions(); // moved to its own class to reduce clutter here
        this.expandCollapseOptions = new ExpandCollapseOptions(this); // moved to its own class to reduce clutter here
        this.borderOptions = new BorderOptions(); // moved to its own class to reduce clutter here
        this.colorOptions.addListener(this::rebuild); // rebuild when our colors change
        this.toolBarOptions.addListener(() -> { // rebuild when our toolbar changes
            if (isToolBarEnabled) { rebuild(); } // but only if the toolbar is enabled
        });
        this.expandCollapseOptions.addListener(this::rebuild);
        this.borderOptions.addListener(this::rebuild);
        this.autoRebuildEnabled = true;
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
        if (action instanceof CardAction cardAction) {
            checkCardAction(cardAction);
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
        if (actions == null || actions.isEmpty()) {
            return this; // Just ignore
        }

        // First validate all CardActions to avoid creating groups if validation fails:
        for (EnhancedAction action : actions) {
            if (action instanceof CardAction cardAction) {
                checkCardAction(cardAction);
            }
        }

        // Now create the group if needed, and add the actions:
        ActionGroup group = findOrCreateGroup(groupName);
        for (EnhancedAction action : actions) {
            group.add(action);
        }

        rebuild();
        return this;
    }

    /**
     * A convenience method for adding a CardAction to this ActionPanel.
     * You must have already invoked setCardContainer() to set a companion CardLayout container!
     * Otherwise, you will get an IllegalStateException.
     * <p>
     * This method is shorthand for add(groupName, new CardAction(actionName, cardId));
     * </p>
     * <p>
     * <b>Note:</b> Due to the design of CardLayout, we can't validate the given cardId.
     * If it doesn't match the name of a card that was given to your layout, then
     * nothing will happen when the action is triggered. It's up to calling code
     * to make sure the cardId given here matches the name of a card in the card container.
     * </p>
     *
     * @param groupName  The name of the group to which the new action should belong.
     * @param actionName The text for the action.
     * @param cardId     The id of the card to show when the action is triggered.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel add(String groupName, String actionName, String cardId) {
        if (cardContainer == null) {
            throw new IllegalStateException(
                    "Cannot add CardAction with cardId without first setting a Card Container on the ActionPanel.");
        }
        if (groupName == null || groupName.isEmpty()) {
            throw new IllegalArgumentException("Group name cannot be null or empty.");
        }
        if (actionName == null || actionName.isEmpty()) {
            throw new IllegalArgumentException("Action name cannot be null or empty.");
        }
        if (cardId == null || cardId.isEmpty()) {
            throw new IllegalArgumentException("Card ID cannot be null or empty.");
        }
        return add(groupName, new CardAction(actionName, cardId));
    }

    /**
     * Invoked internally to handle validation of CardActions as they are added.
     * Namely, we ensure that a valid Card Container is set before accepting the action.
     *
     * @param cardAction The CardAction to add.
     */
    private void checkCardAction(CardAction cardAction) {
        if (cardContainer == null) {
            throw new IllegalStateException(
                    "Cannot add CardAction without first setting a Card Container on the ActionPanel.");
        }
        cardAction.setCardContainer(cardContainer);
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
     * Sets an optional companion CardLayout Container for this ActionPanel.
     * If set, you can add CardActions to this ActionPanel, or you can use the
     * convenience methods that accept a cardId parameter.
     * When a CardAction is triggered, the specified card will be shown in the CardLayout container.
     * You can pass null to disassociate any existing container, but note that if you have
     * any CardActions in this ActionPanel, you must remove them first, or you
     * will get an IllegalStateException.
     *
     * @param container Any Container with a CardLayout, or null to disassociate an existing container.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setCardContainer(Container container) {
        List<CardAction> cardActions = getCardActions();
        if (container == null && !cardActions.isEmpty()) {
            throw new IllegalStateException("Cannot set Card Container to null while Card Actions are present.");
        }
        if (container != null && !(container.getLayout() instanceof CardLayout)) {
            throw new IllegalArgumentException("Card Container must use CardLayout.");
        }

        // Update any existing CardActions to point to the new container:
        for (CardAction cardAction : cardActions) {
            cardAction.setCardContainer(container);
        }

        // Accept and rebuild:
        this.cardContainer = container;
        rebuild();
        return this;
    }

    /**
     * Returns the companion CardLayout Container instance, if one is set.
     *
     * @return The CardLayout Container instance that is associated with this ActionPanel, or null if none is set.
     */
    public Container getCardContainer() {
        return cardContainer;
    }

    /**
     * Returns true if the given action is currently highlighted.
     *
     * @param action The action to check.
     * @return True if the action is highlighted, false otherwise.
     */
    public boolean isHighlightedAction(EnhancedAction action) {
        if (!highlightLastAction) {
            return false; // no action is highlighted if the feature is disabled
        }
        return action == highlightedAction;
    }

    /**
     * Sets the specified action as the currently highlighted action.
     * Only one action can be highlighted at a time. Pass null to clear any highlighted action.
     *
     * @param action The action to highlight, or null to clear the highlighted action.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setHighlightedAction(EnhancedAction action) {
        // If highlighting is disabled, just record the last action without rebuilding the UI.
        if (!highlightLastAction) {
            this.highlightedAction = action;
            return this;
        }

        // If the highlighted action is unchanged, avoid an unnecessary rebuild.
        if (this.highlightedAction == action) {
            return this;
        }
        this.highlightedAction = action;
        rebuild();
        return this;
    }

    /**
     * Reports whether the last action to be executed in this ActionPanel should be
     * visually highlighted. The default is false.
     *
     * @return True if the last action is highlighted, false otherwise.
     */
    public boolean isHighlightLastActionEnabled() {
        return highlightLastAction;
    }

    /**
     * Enables or disables highlighting of the last action to be executed in this ActionPanel.
     * The default is false.
     *
     * @param highlightLastAction True to enable highlighting of the last action, false to disable it.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setHighlightLastActionEnabled(boolean highlightLastAction) {
        this.highlightLastAction = highlightLastAction;
        rebuild();
        return this;
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
                                           .filter(Objects::nonNull)
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
     * Options related to custom colors are accessed via the ColorOptions class.
     * Developer note: yeah, they could all live here in this class, but this class
     * is already unreasonably large, and there are many options related to color
     * customization, so they were all moved over there. The only first-class color-related
     * methods still in ActionPanel are setBackground() and getBackground(), which are
     * overridden for internal reasons. All other color options are accessed via the
     * ColorOptions instance returned by this method.
     *
     * @return The ColorOptions instance containing options related to color customization.
     */
    public ColorOptions getColorOptions() {
        return colorOptions;
    }

    /**
     * Options related to group expansion/collapse are accessed via the ExpandCollapseOptions class.
     * Developer note: yeah, they could all live here in this class, but this class
     * is already unreasonably large, and there are many options related to expansion and
     * collapse, so they were all moved over there.
     *
     * @return The ExpandCollapseOptions instance containing options related to expansion and collapse.
     */
    public ExpandCollapseOptions getExpandCollapseOptions() {
        return expandCollapseOptions;
    }

    /**
     * Options related to borders are accessed via the BorderOptions class.
     * Developer note: yeah, they could all live here in this class, but this class
     * is already unreasonably large, and there are many options related to borders,
     * so they were all moved over there.
     *
     * @return The BorderOptions instance containing options related to border customization.
     */
    public BorderOptions getBorderOptions() {
        return borderOptions;
    }

    /**
     * We have to override this because we use a wrapper panel internally to manage
     * our BoxLayout, so we need to store the background color and apply it to
     * the wrapper panel during rebuild(). This is not at all obvious to callers,
     * who may reasonably expect to be able to do actionPanel.setBackground().
     * So, we intercept it and do the right thing behind the scenes. All of our other
     * color options are stored in our ColorOptions instance.
     * <p>
     *     Setting null here will revert to the Look and Feel default background color.
     * </p>
     *
     * @param bg the desired background <code>Color</code>
     */
    @Override
    public void setBackground(Color bg) {
        // This can somehow get invoked before we are fully instantiated, in which
        // case our colorOptions is null. In that case, just ignore the call to avoid an NPE.
        if (colorOptions == null) {
            return;
        }

        // If it's null, talk to our Look and Feel manager to get the default background color:
        Color newColor = bg == null ? LookAndFeelManager.getLafColor("Panel.background", Color.LIGHT_GRAY) : bg;
        colorOptions.setPanelBackground(newColor); // will fire an optionsChanged and we will rebuild.
    }

    /**
     * We override to return our own stored background color. If null was passed to
     * setBackground(), this will return the Look and Feel default Panel background color.
     *
     * @return the background <code>Color</code> of the ActionPanel itself.
     */
    @Override
    public Color getBackground() {
        // This can somehow get invoked before we are fully instantiated, in which
        // case our colorOptions is null. In that case, just return null to avoid a NullPointerException.
        if (colorOptions == null) {
            return null;
        }

        // It's possible our value is set to null, meaning we want to use the Look and Feel default background
        // color. In that case, ask our Look and Feel manager for the default Panel background color:
        return colorOptions.getPanelBackground() == null
                ? LookAndFeelManager.getLafColor("Panel.background", Color.LIGHT_GRAY)
                : colorOptions.getPanelBackground();
    }

    /**
     * Returns the Margins instance for the header area of action groups.
     * This controls the space between the header components (e.g. icon, label, expand/collapse button)
     * and the edges of the header. The internalSpacing property controls the space between the header
     * components themselves.
     * <p>
     *     Note: changing any of the properties of the returned Margins instance will trigger
     *     an immediate rebuild of the ActionPanel. If you have multiple changes you wish to
     *     make, it may make sense to disable auto-rebuild until after your changes are complete,
     *     to avoid unnecessary intermediate rebuilds. For example:
     * </p>
     * <pre>
     *     actionPanel.setAutoRebuild(false);
     *     try {
     *         Margins margins = actionPanel.getHeaderMargins();
     *         margins.setTop(10);
     *         margins.setBottom(10);
     *         margins.setLeft(5);
     *         margins.setRight(5);
     *     }
     *     finally {
     *         // Re-enabling auto-rebuild will trigger
     *         // an immediate rebuild:
     *         actionPanel.setAutoRebuild(true);
     *     }
     * </pre>
     *
     * @return The Margins instance for the header area of action groups.
     */
    public Margins getHeaderMargins() {
        return headerMargins;
    }

    /**
     * Returns the Margins instance for the action tray area of action groups. This controls the space between the
     * action components (e.g. buttons/labels) and the edges of the action tray. The internalSpacing property controls
     * the space between the action components themselves.
     * <p>
     *     Note: changing any of the properties of the returned Margins instance will trigger
     *     an immediate rebuild of the ActionPanel. If you have multiple changes you wish to
     *     make, it may make sense to disable auto-rebuild until after your changes are complete,
     *     to avoid unnecessary intermediate rebuilds. For example:
     * </p>
     * <pre>
     *     actionPanel.setAutoRebuild(false);
     *     try {
     *         Margins margins = actionPanel.getActionTrayMargins();
     *         margins.setTop(10);
     *         margins.setBottom(10);
     *         margins.setLeft(5);
     *         margins.setRight(5);
     *     }
     *     finally {
     *         // Re-enabling auto-rebuild will trigger
     *         // an immediate rebuild:
     *         actionPanel.setAutoRebuild(true);
     *     }
     * </pre>
     *
     * @return The Margins instance for the action tray area of action groups.
     */
    public Margins getActionTrayMargins() {
        return actionTrayMargins;
    }

    /**
     * Returns the Margins instance for the ToolBar. This controls the space between the ToolBar
     * buttons and the edges of the ToolBar area. If the ToolBar is not in Stretch mode, then
     * the internalSpacing property controls the space between the ToolBar buttons themselves.
     * If the ToolBar is in Stretch mode, then there is no space between the buttons, regardless
     * of the internalSpacing setting, because the buttons are stretched to fill all available space.
     * <p>
     *     Note: changing any of the properties of the returned Margins instance will trigger
     *     an immediate rebuild of the ActionPanel. If you have multiple changes you wish to
     *     make, it may make sense to disable auto-rebuild until after your changes are complete,
     *     to avoid unnecessary intermediate rebuilds. For example:
     * </p>
     * <pre>
     *     actionPanel.setAutoRebuild(false);
     *     try {
     *         Margins margins = actionPanel.getToolBarMargins();
     *         margins.setTop(10);
     *         margins.setBottom(10);
     *         margins.setLeft(5);
     *         margins.setRight(5);
     *     }
     *     finally {
     *         // Re-enabling auto-rebuild will trigger
     *         // an immediate rebuild:
     *         actionPanel.setAutoRebuild(true);
     *     }
     * </pre>
     *
     * @return The Margins instance for the ToolBar.
     */
    public Margins getToolBarMargins() {
        return toolBarMargins;
    }

    /**
     * Returns the Margins instance for action groups. This controls the space between action groups
     * and the edges of the ActionPanel. The internalSpacing property controls the gap between
     * action groups themselves.
     * <p>
     *     Note: changing any of the properties of the returned Margins instance will trigger
     *     an immediate rebuild of the ActionPanel. If you have multiple changes you wish to
     *     make, it may make sense to disable auto-rebuild until after your changes are complete,
     *     to avoid unnecessary intermediate rebuilds. For example:
     * </p>
     * <pre>
     *     actionPanel.setAutoRebuild(false);
     *     try {
     *         Margins margins = actionPanel.getActionGroupMargins();
     *         margins.setTop(10);
     *         margins.setBottom(10);
     *         margins.setLeft(5);
     *         margins.setRight(5);
     *     }
     *     finally {
     *         // Re-enabling auto-rebuild will trigger
     *         // an immediate rebuild:
     *         actionPanel.setAutoRebuild(true);
     *     }
     * </pre>
     *
     * @return The Margins instance for action groups.
     */
    public Margins getActionGroupMargins() {
        return actionGroupMargins;
    }

    /**
     * Returns the gap between the action icon and the action text.
     * This is only relevant if showActionIcons is true and actions have icons.
     * Note we don't use the action tray's internalSpacing for this, because it's
     * nice to be able to control this text gap separately from the gap between the
     * action components themselves.
     *
     * @return The gap in pixels between the action icon and the action text.
     */
    public int getActionIconTextGap() {
        return actionIconTextGap;
    }

    /**
     * Sets the gap between action icons and their text labels.
     * This is only relevant if showActionIcons is true and actions have icons.
     * Note we don't use the action tray's internalSpacing for this, because it's
     * nice to be able to control this text gap separately from the gap between the
     * action components themselves.
     *
     * @param gap The gap in pixels between the action icon and the action text. Must be 0 or greater.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setActionIconTextGap(int gap) {
        if (gap < 0) {
            throw new IllegalArgumentException("Icon-text gap cannot be negative.");
        }
        this.actionIconTextGap = gap;
        rebuild();
        return this;
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
     * Normally, ActionPanel will automatically rebuild itself whenever any option
     * is modified. If you have multiple successive changes to make, and want to spare
     * the thrashing of multiple rebuilds, you can disable auto-rebuild,
     * make your changes, then re-enable auto-rebuild. Invoking setAutoRebuildEnabled(true)
     * will immediately trigger a rebuild if auto-rebuild was previously disabled.
     *
     * @param enabled True to enable auto-rebuild, false to disable it.
     * @return This ActionPanel, for method chaining.
     */
    public ActionPanel setAutoRebuildEnabled(boolean enabled) {
        // Do nothing if it's a no-op:
        if (this.autoRebuildEnabled == enabled) {
            return this;
        }

        // Accept the new value and rebuild immediately if enabling:
        this.autoRebuildEnabled = enabled;
        if (enabled) {
            rebuild();
        }
        return this;
    }

    /**
     * Reports whether auto-rebuild is enabled. When enabled, ActionPanel
     * will automatically rebuild itself whenever any option is modified.
     * When disabled, ActionPanel will not rebuild itself until
     * setAutoRebuildEnabled(true) is invoked.
     *
     * @return True if auto-rebuild is enabled, false otherwise.
     */
    public boolean isAutoRebuildEnabled() {
        return autoRebuildEnabled;
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
     * Invoked internally to return a flat list of all CardActions across all action groups.
     *
     * @return A list of all CardActions across all action groups. May be empty if there are no CardActions.
     */
    private List<CardAction> getCardActions() {
        List<CardAction> cardActions = new ArrayList<>();
        for (ActionGroup group : actionGroups) {
            for (EnhancedAction action : group.getActions()) {
                if (action instanceof CardAction) {
                    cardActions.add((CardAction)action);
                }
            }
        }
        return cardActions;
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
        // Do nothing if auto-rebuild is disabled:
        if (!autoRebuildEnabled) {
            return;
        }

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

        // Render each action group:
        for (int i = 0; i < sortedGroups.size(); i++) {
            ActionGroup group = sortedGroups.get(i);
            wrapperPanel.add(new GroupContainer(this,
                                                group,
                                                i == 0, // isFirstGroup
                                                i == sortedGroups.size() - 1)); // isLastGroup
        }

        // Add glue to push everything to the top
        wrapperPanel.add(Box.createVerticalGlue());

        // Apply panel background if set:
        if (colorOptions.getPanelBackground() != null) {
            wrapperPanel.setBackground(colorOptions.getPanelBackground());
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
