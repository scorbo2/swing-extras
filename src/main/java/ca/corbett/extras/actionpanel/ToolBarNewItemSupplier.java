package ca.corbett.extras.actionpanel;

import ca.corbett.extras.EnhancedAction;

/**
 * A very simple Supplier-like interface for supplying new actions for an ActionGroup
 * within an ActionPanel. This is used for the "Add item" button in the ToolBar.
 * If the supplier returns null, it is assumed that the user canceled the add action,
 * and no change will be made. Otherwise, the supplied action will be added to the
 * named action group. The action is either added to the end of the group, or sorted
 * within the group, depending on the ActionPanel's sorting options.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
 */
@FunctionalInterface
public interface ToolBarNewItemSupplier {

    /**
     * Invoked when "add item" is clicked in an ActionPanel's ToolBar.
     * The supplied ActionPanel and groupName can be used to determine the context of the add action.
     *
     * @param actionPanel The ActionPanel that the new action will be added to.
     * @param groupName   The name of the ActionGroup that the new action will be added to. This is case-insensitive.
     * @return The new EnhancedAction to add to the ActionGroup, or null if the user canceled the add action.
     */
    EnhancedAction get(ActionPanel actionPanel, String groupName);
}
