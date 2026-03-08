package ca.corbett.extras.actionpanel;

/**
 * Can be used to listen for when an ActionGroup is renamed within an ActionPanel.
 * If allowGroupRename is true and the toolbar is enabled, users can rename groups at will.
 * Even if allowGroupRename is false, this event can still be triggered if the renameGroup() method
 * is invoked programmatically on your ActionPanel. This event will fire regardless
 * of how the group was renamed.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
 */
@FunctionalInterface
public interface GroupRenamedListener {

    /**
     * Called when an ActionGroup is renamed within an ActionPanel. This can be used to update any references
     * to the group name that you may have stored elsewhere, such as in a Map of group names to custom actions.
     *
     * @param actionPanel The ActionPanel that the group was renamed in.
     * @param oldName     The old name of the group before it was renamed.
     * @param newName     The new name of the group after it was renamed.
     */
    void groupRenamed(ActionPanel actionPanel, String oldName, String newName);
}
