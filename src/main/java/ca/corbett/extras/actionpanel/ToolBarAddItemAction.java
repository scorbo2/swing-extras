package ca.corbett.extras.actionpanel;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.forms.SwingFormsResources;

/**
 * An action for adding a new item to an ActionGroup. This will only be visible if allowItemAdd
 * is true in the ToolBarOptions for the ActionPanel. If a ToolBarActionSupplier is provided,
 * then this action will add a new item to the given ActionGroup when invoked.
 * <p>
 * This class is package-private and is only used internally by ToolBarOptions.
 * Callers can access this functionality by going through the ToolBarOptions class.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
 */
class ToolBarAddItemAction extends ToolBarAction {

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
    public void actionPerformed(java.awt.event.ActionEvent e) {
        // Note: we don't bother checking if allowAddItem is true here, because
        //       the ToolBar will not show our button if it isn't.

        if (newItemSupplier == null) {
            return; // No supplier, so we're done here.
        }

        EnhancedAction newAction = newItemSupplier.get(actionPanel, groupName);
        if (newAction == null) {
            return; // User canceled the add action, so we're done here.
        }

        // Add the new action to the ActionPanel in the right group - it will get sorted as needed:
        actionPanel.add(groupName, newAction);
    }
}
