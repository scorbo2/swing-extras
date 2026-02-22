package ca.corbett.extras.actionpanel;

/**
 * Can be used to listen for when the items in an ActionGroup are reordered.
 * This does NOT include auto-sorting when a custom Comparator is supplied.
 * This specifically is for user-specified manual reordering via the
 * built-in group edit dialog.
 * <p>
 * If you wish to learn the ordering of items that was applied by
 * your custom Comparator, you can simply invoke
 * actionPanel.getActionsForGroup() with the affectedGroupName
 * after adding items. The getActionsForGroup() method will return
 * the items in the correct order according to your Comparator.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
 */
@FunctionalInterface
public interface GroupReorderedListener {

    /**
     * Invoked when the items in an ActionGroup are reordered within an ActionPanel.
     * You can invoke actionPanel.getActionsForGroup() with the affectedGroupName
     * to get the new order of actions for the group after the reorder.
     *
     * @param actionPanel       The ActionPanel that the group belongs to.
     * @param affectedGroupName The name of the group that was reordered.
     */
    void groupReordered(ActionPanel actionPanel, String affectedGroupName);
}
