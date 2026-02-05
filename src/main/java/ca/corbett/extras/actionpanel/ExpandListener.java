package ca.corbett.extras.actionpanel;

@FunctionalInterface
public interface ExpandListener {

    /**
     * Fired when a group is either expanded or collapsed.
     *
     * @param groupName  The name of the group that changed.
     * @param isExpanded True if the group is now expanded, false if collapsed.
     */
    void groupExpandedChanged(String groupName, boolean isExpanded);
}
