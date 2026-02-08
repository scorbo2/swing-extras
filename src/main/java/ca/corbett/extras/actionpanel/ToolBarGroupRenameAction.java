package ca.corbett.extras.actionpanel;

import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;

/**
 * An action for renaming an ActionGroup. This will only be visible if allowGroupRename
 * is true in the ToolBarOptions for the ActionPanel. Otherwise, this action does nothing.
 * <p>
 * This class is package-private and is only used internally by ToolBarOptions.
 * Callers can access this functionality by going through the ToolBarOptions class:
 * </p>
 * <pre>
 *     // Enabled by default, but you can turn it off if unneeded:
 *     myActionPanel.getToolBarOptions().setAllowGroupRename(false);
 * </pre>
 * <p>
 * Alternatively, you can disable the ToolBar altogether:
 * </p>
 * <pre>
 *     myActionPanel.setToolBarEnabled(false); // hides all ToolBar actions
 * </pre>
 * <p>
 * <b>Duplicate checking</b> - this action will prompt the user for a new name for
 * the ActionGroup. If the new name (case-insensitive) conflicts with the name of
 * any other group in the ActionPanel, the user will not be able to submit the dialog.
 * This duplicate checking cannot currently be disabled, but you can supply your
 * own item rename action as a custom action, and disable this built-in action:
 * </p>
 * <pre>
 *     // Disable the built-in rename action:
 *     myActionPanel.getToolBarOptions().setAllowGroupRename(false);
 *
 *     // Supply your own action/dialog with whatever rules you want:
 *     myActionPanel.getToolBarOptions().addCustomActionSupplier(...);
 * </pre>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
 */
public class ToolBarGroupRenameAction extends ToolBarAction {

    public ToolBarGroupRenameAction(ActionPanel actionPanel, String groupName, String tooltip, ImageIcon icon) {
        super(actionPanel, groupName, tooltip, icon);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO
    }

    /**
     * Invoked internally to check the given name (case-insensitive) against the
     * existing group names in the ActionPanel.
     *
     * @param name The name to check for uniqueness. This is case-insensitive.
     * @return True if the name is unique (case-insensitive) among the existing group names in the ActionPanel, false otherwise.
     */
    private boolean nameIsUnique(String name) {
        return actionPanel.getGroupNames()
                          .stream()
                          .map(String::toLowerCase)
                          .noneMatch(existingName -> existingName.equals(name.toLowerCase()));
    }
}
