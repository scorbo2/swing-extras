package ca.corbett.extras.actionpanel;

import ca.corbett.extras.EnhancedAction;

import javax.swing.Icon;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * An internal class to represent a single action group.
 * This class knows nothing about rendering!
 * This is simply a data structure for holding the group name,
 * icon, actions, and expanded/collapsed state.
 * <p>
 * This class is package-private, so callers do not interact with it directly.
 * Use the ActionPanel API instead.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
 */
class ActionGroup {
    private String name;
    private Comparator<EnhancedAction> comparator;
    private final List<EnhancedAction> actionsAsAdded; // Maintain insertion order
    private final List<EnhancedAction> actionsSorted; // Maintain sorted order
    private Icon icon;
    private boolean isExpanded;
    private AnimatedPanel animatedWrapper;

    /**
     * Creates an empty action group with the specified name.
     * Group names should be unique, but nothing in the code enforces this.
     * The only constraint is that the name cannot be null or empty.
     *
     * @param name Any non-empty string to use as the group header.
     */
    public ActionGroup(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("ActionGroup name cannot be null or empty");
        }
        this.name = name;
        this.comparator = null;
        this.actionsAsAdded = new ArrayList<>();
        this.actionsSorted = new ArrayList<>();
        this.isExpanded = true; // expanded by default
        this.icon = null;
        this.animatedWrapper = null;
    }

    /**
     * Returns the name of this action group. This is used as the header text in the UI.
     * Group names are case-insensitive.
     *
     * @return The name of this action group.
     */
    public String getName() {
        return name;
    }

    /**
     * Renames this action group to the given new name.
     * The only constraint is that the new name cannot be null or empty.
     * It is recommended that groups in an ActionPanel should have unique names,
     * but it is up to calling code to enforce that recommendation.
     *
     * @param newName The new name for this action group. Must be a non-empty string.
     * @return This ActionGroup instance, for chaining. The name is changed in-place (no new group is created).
     */
    public ActionGroup renameTo(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("ActionGroup name cannot be null or empty");
        }
        this.name = newName; // We assume caller checked this name for uniqueness.
        return this; // for chaining
    }

    /**
     * Returns the Comparator used for sorting actions in this group, or null if no Comparator is set.
     *
     * @return The Comparator used for sorting actions in this group, or null if no Comparator is set.
     */
    public Comparator<EnhancedAction> getComparator() {
        return comparator;
    }

    /**
     * Groups can optionally have a Comparator to keep their actions sorted.
     * Null is perfectly fine here, in which case actions will be sorted
     * according to the order they were added.
     * <p>
     * If a comparator is set, sorting is performed automatically as
     * actions are added.
     * </p>
     *
     * @param comparator The Comparator to use for sorting actions in this group, or null.
     */
    public void setComparator(Comparator<EnhancedAction> comparator) {
        this.comparator = comparator;
        autoSort();
    }

    /**
     * Returns a copy of the list of actions in this group.
     * The returned list is sorted according to the group's comparator,
     * if one is set.
     */
    public List<EnhancedAction> getActions() {
        return actionsSorted;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    /**
     * Marks this group as expanded (true) or collapsed (false).
     * This does not trigger any UI updates! This class merely holds the state.
     */
    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    /**
     * Adds a new action to this group. There is no uniqueness check,
     * so the same action can be added multiple times if desired.
     * The only constraint is that the action cannot be null.
     *
     * @param action Any EnhancedAction to add to this group.
     */
    public void add(EnhancedAction action) {
        if (action == null) {
            throw new IllegalArgumentException("Cannot add null action to ActionGroup");
        }
        actionsAsAdded.add(action);
        autoSort();
    }

    /**
     * Adds all actions from the given list to this group. There is no uniqueness check,
     * so the same action can be added multiple times if desired.
     * The only constraint is that the action list and its contents cannot be null.
     *
     * @param actions A list of EnhancedAction objects to add to this group.
     */
    public void addAll(List<EnhancedAction> actions) {
        if (actions == null) {
            throw new IllegalArgumentException("Cannot add null action list to ActionGroup");
        }

        // Don't call add() for each one! We only want to sort once at the end.
        for (EnhancedAction action : actions) {
            if (action == null) {
                throw new IllegalArgumentException("Cannot add null action to ActionGroup");
            }
            actionsAsAdded.add(action);
        }

        // Now sort if needed:
        autoSort();
    }

    /**
     * Reports whether at least one instance of the given action is present in this group.
     */
    public boolean hasAction(EnhancedAction action) {
        return actionsAsAdded.contains(action);
    }

    /**
     * Reports whether at least once instance of an action with the given name is present in this group.
     *
     * @param actionName    The name to search for. Only actions with non-null names are considered.
     * @param caseSensitive Whether the name comparison should be case-sensitive.
     * @return True if at least one action with the given name is present, false otherwise.
     */
    public boolean hasAction(String actionName, boolean caseSensitive) {
        return actionsAsAdded
                .stream()
                .map(EnhancedAction::getName)
                .filter(Objects::nonNull)
                .anyMatch(n -> caseSensitive ? n.equals(actionName) : n.equalsIgnoreCase(actionName));
    }

    /**
     * Reports the number of actions in this group.
     */
    public int size() {
        return actionsAsAdded.size();
    }

    /**
     * Reports whether this group is empty.
     */
    public boolean isEmpty() {
        return actionsAsAdded.isEmpty();
    }

    /**
     * Removes the given action from this group, if it was present.
     * Note that ActionGroups allow duplication actions! The behavior
     * of this method may be unexpected in that case. All instances
     * of the given action will be removed.
     *
     * @return True if at least one instance of the action was found and removed, false otherwise.
     */
    public boolean remove(EnhancedAction action) {
        if (action == null) {
            return false; // Just ignore nulls
        }
        boolean anyRemoved = false;
        while (actionsAsAdded.contains(action)) {
            actionsAsAdded.remove(action);
            anyRemoved = true;
        }
        autoSort();
        return anyRemoved;
    }

    /**
     * Removes the named action from this group, if it was present.
     * Note that ActionGroups allow duplication actions! The behavior
     * of this method may be unexpected in that case. All instances
     * of actions with the given name will be removed.
     * Only actions with non-null names are considered.
     *
     * @param actionName    The name of the action(s) to remove.
     * @param caseSensitive Whether the name comparison should be case-sensitive.
     * @return True if at least one action with the given name was found and removed, false otherwise.
     */
    public boolean remove(String actionName, boolean caseSensitive) {
        List<EnhancedAction> toRemove = new ArrayList<>();
        for (EnhancedAction action : actionsAsAdded) {
            String name = action.getName();
            if (name != null) {
                boolean matches = caseSensitive ? name.equals(actionName) : name.equalsIgnoreCase(actionName);
                if (matches) {
                    toRemove.add(action);
                }
            }
        }
        if (toRemove.isEmpty()) {
            return false;
        }
        actionsAsAdded.removeAll(toRemove);
        autoSort();
        return true;
    }

    /**
     * Removes all actions from this group.
     */
    public void clear() {
        actionsAsAdded.clear();
        autoSort();
    }

    public Icon getIcon() {
        return icon;
    }

    /**
     * Sets an optional icon for this action group.
     * Null is perfectly fine here, indicating no icon.
     *
     * @param icon The icon to use for this action group, or null.
     */
    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    public AnimatedPanel getAnimatedWrapper() {
        return animatedWrapper;
    }

    /**
     * This is here as a convenience for the UI code - we do nothing
     * with it in this class.
     */
    public void setAnimatedWrapper(AnimatedPanel animatedWrapper) {
        this.animatedWrapper = animatedWrapper;
    }

    /**
     * Invoked as needed to re-sort the actions in this group.
     * Does nothing if no comparator is set.
     */
    private void autoSort() {
        actionsSorted.clear();
        actionsSorted.addAll(actionsAsAdded);
        if (comparator != null) {
            actionsSorted.sort(comparator);
        }
    }
}
