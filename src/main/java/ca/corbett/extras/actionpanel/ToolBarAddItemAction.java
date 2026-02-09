package ca.corbett.extras.actionpanel;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.forms.SwingFormsResources;

import java.awt.event.ActionEvent;
import java.util.logging.Logger;

/**
 * An action for adding a new item to an ActionGroup. This will only be visible if allowItemAdd
 * is true in the ToolBarOptions for the ActionPanel AND a ToolBarNewItemSupplier has been provided.
 * Otherwise, this action does nothing.
 * <p>
 * This class is package-private and is only used internally by ToolBarOptions.
 * Callers can access this functionality by going through the ToolBarOptions class:
 * </p>
 * <pre>
 *     // It's enabled by default, so normally you won't need this:
 *     myActionPanel.getToolBarOptions().setAllowItemAdd(true);
 *
 *     // But you MUST provide a ToolBarNewItemSupplier for the button to appear!
 *     // No supplier means no button, and the action does nothing if triggered.
 *     myActionPanel.getToolBarOptions().setNewActionSupplier(...);
 * </pre>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
 */
class ToolBarAddItemAction extends ToolBarAction {

    private static final Logger log = Logger.getLogger(ToolBarAddItemAction.class.getName());
    private ToolBarNewItemSupplier newItemSupplier;

    public ToolBarAddItemAction(ActionPanel actionPanel, String groupName) {
        super(actionPanel,
              groupName,
              "Add a new action to this group",
              SwingFormsResources.getAddIcon(SwingFormsResources.NATIVE_SIZE));
        this.newItemSupplier = null; // We can't provide a meaningful default for this
    }

    public void setNewItemSupplier(ToolBarNewItemSupplier newItemSupplier) {
        this.newItemSupplier = newItemSupplier;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // If the permission related to adding items is off, then our button
        // won't even be shown, so in theory, this action could never fire.
        // But let's be defensive and check just in case:
        if (!actionPanel.getToolBarOptions().isAllowItemAdd()) {
            log.fine("ToolBarAddItemAction.actionPerformed() - rejected because not allowed.");
            return;
        }

        // If we have no supplier, then there's nothing we can do, so we're done here:
        if (newItemSupplier == null) {
            log.fine("ToolBarAddItemAction.actionPerformed() - rejected because no supplier.");
            return;
        }

        // Get a new action from our supplier:
        EnhancedAction newAction = newItemSupplier.get(actionPanel, groupName);

        // The supplier might return null. For example, it might have shown a dialog
        // to the user, and the user selected to cancel. This is not a big deal.
        // We will just abort the add action and do nothing in this case:
        if (newAction == null) {
            log.fine("ToolBarAddItemAction.actionPerformed() - supplier returned null, action canceled.");
            return;
        }

        // Add the new action to the ActionPanel in the right group - it will get sorted as needed:
        log.fine("ToolBarAddItemAction: adding new action from supplier: \"" + newAction.getTooltip() + "\"");
        actionPanel.add(groupName, newAction);
    }
}
