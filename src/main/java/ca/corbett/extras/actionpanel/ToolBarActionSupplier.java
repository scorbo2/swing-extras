package ca.corbett.extras.actionpanel;

/**
 * A simple Supplier-like interface for supplying ToolBarActions for an ActionPanel.
 * This can be used to add custom actions to the ToolBar for an ActionGroup within an ActionPanel,
 * without needing to subclass ActionPanel or modify the existing code.
 * Implementations of this interface can be registered with an ActionPanel via the ToolBarOptions class:
 * <pre>
 *     myActionPanel.getToolBarOptions().addCustomActionSupplier(...);
 * </pre>
 * <p>
 * This is useful for augmenting or replacing the built-in actions offered by ToolBarOptions.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
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
