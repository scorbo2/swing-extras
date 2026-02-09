package ca.corbett.extras.actionpanel;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import java.awt.Window;
import java.awt.event.ActionEvent;

/**
 * An action that shows a dialog for editing the ActionGroup. This will only be visible if allowItemReorder
 * or allowItemRemoval are true in the ToolBarOptions for the ActionPanel. Otherwise, this action does nothing.
 * There are three actions possible in the edit dialog, each with their own permission:
 * <ul>
 *     <li><b>Rename group</b> - (related permission: allowGroupRename) - this allows the user to change the
 *     name of the ActionGroup. If allowGroupRename is false, then the group name is read-only on the dialog.</li>
 *     <li><b>Reorder items</b> - (related permission: allowItemReorder) - this allows the user to reorder the
 *     actions within the ActionGroup. If allowItemReorder is false, then this option will not be shown in the
 *     edit dialog.</li>
 *     <li><b>Remove items</b> - (related permission: allowItemRemoval) - this allows the user to remove actions
 *     from the ActionGroup. If allowItemRemoval is false, then this option will not be shown in the edit dialog.</li>
 * </ul>
 * <p>
 * This class is package-private and is only used internally by ToolBarOptions.
 * Callers can access this functionality by going through the ToolBarOptions class:
 * </p>
 * <pre>
 *     // Enabled by default, but you can turn it off if unneeded:
 *     myActionPanel.getToolBarOptions().setAllowItemReorder(false);
 *     myActionPanel.getToolBarOptions().setAllowItemRemoval(false);
 *
 *     // To supply your own custom group edit action if you don't like the built-in one:
 *     myActionPanel.getToolBarOptions().addCustomActionSupplier(...);
 * </pre>
 * <p>
 * Alternatively, you can disable the ToolBar altogether:
 * </p>
 * <pre>
 *     myActionPanel.setToolBarEnabled(false); // hides all ToolBar actions
 * </pre>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
 */
class ToolBarGroupEditAction extends ToolBarAction {

    public ToolBarGroupEditAction(ActionPanel actionPanel, String groupName, String tooltip, ImageIcon icon) {
        super(actionPanel, groupName, tooltip, icon);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Window owner = SwingUtilities.getWindowAncestor(actionPanel);
        ToolBarGroupEditDialog dialog = new ToolBarGroupEditDialog(owner, actionPanel, groupName);
        dialog.setVisible(true);
        if (dialog.wasOkayed()) {
            String groupName = this.groupName;
            if (dialog.groupWasRenamed()) {
                actionPanel.renameGroup(groupName, dialog.getModifiedGroupName());
                groupName = dialog.getModifiedGroupName(); // so the below code can work properly
            }

            if (dialog.listWasModified()) {
                ActionGroup actionGroup = actionPanel.getGroup(groupName);
                if (actionGroup != null) {
                    actionGroup.clear();
                    actionGroup.addAll(dialog.getModifiedList());
                    actionPanel.rebuild();
                }
            }
        }
    }
}
