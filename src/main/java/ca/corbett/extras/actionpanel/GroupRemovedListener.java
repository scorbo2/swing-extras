package ca.corbett.extras.actionpanel;

/**
 * Can be used to listen for when an ActionGroup is removed from an ActionPanel.
 * If allowGroupRemoval is true and the toolbar is enabled, users can remove groups at will.
 * Even if allowGroupRemoval is false, this event can still be triggered if the removeGroup()
 * method is invoked programmatically on your ActionPanel. This event will fire regardless
 * of how the group was removed.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
 */
@FunctionalInterface
public interface GroupRemovedListener {

    /**
     * Called when an ActionGroup is removed from an ActionPanel. This can be used to clean up any references
     * to the group name that you may have stored elsewhere, such as in a Map of group names to custom actions.
     *
     * @param actionPanel The ActionPanel that the group was removed from.
     * @param groupName   The name of the group that was removed.
     */
    void groupRemoved(ActionPanel actionPanel, String groupName);
}
