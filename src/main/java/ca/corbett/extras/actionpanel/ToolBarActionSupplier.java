package ca.corbett.extras.actionpanel;

/**
 * A simple Supplier-like interface for supplying ToolBarActions for an ActionPanel. This is used to
 * generate ToolBar buttons for each ActionGroup in an ActionPanel.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
@FunctionalInterface
public interface ToolBarActionSupplier {
    /**
     * Returns a ToolBarAction implementation class that can be added to the ToolBar for
     * an ActionGroup within an ActionPanel. The returned action should have a tooltip
     * and an icon specified, as per the Javadocs in ToolBarAction.
     * If the supplier returns null, it is ignored.
     *
     * @param actionPanel The ActionPanel that the ToolBarAction will be shown for.
     * @param groupName   The name of the ActionGroup that the ToolBarAction will be shown for. This is case-insensitive.
     * @return The ToolBarAction to show for the given ActionPanel and ActionGroup, or null if no action should be shown.
     */
    ToolBarAction get(ActionPanel actionPanel, String groupName);
}
