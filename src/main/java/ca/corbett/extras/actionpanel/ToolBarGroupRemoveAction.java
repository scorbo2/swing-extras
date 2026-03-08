package ca.corbett.extras.actionpanel;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;

/**
 * An action for removing an ActionGroup. This will only be visible if allowGroupRemoval
 * is true in the ToolBarOptions for the ActionPanel. Otherwise, this action does nothing.
 * A confirmation prompt is shown before the group is actually removed. This is not
 * currently configurable, but you can disable this built-in action and supply your
 * own group removal action if you need finer control over confirmation (or
 * want to disable confirmation entirely).
 * <p>
 * This class is package-private and is only used internally by ToolBarOptions.
 * Callers can access this functionality by going through the ToolBarOptions class:
 * </p>
 * <pre>
 *     // Enabled by default, but you can turn it off if unneeded:
 *     myActionPanel.getToolBarOptions().setAllowGroupRemoval(false);
 *
 *     // To supply your own custom group removal action:
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
class ToolBarGroupRemoveAction extends ToolBarAction {

    private static final Logger log = Logger.getLogger(ToolBarGroupRemoveAction.class.getName());

    public ToolBarGroupRemoveAction(ActionPanel actionPanel, String groupName, String tooltip, ImageIcon icon) {
        super(actionPanel, groupName, tooltip, icon);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!actionPanel.hasGroup(groupName)) {
            // If the group doesn't exist, then we have nothing to remove, so we're done here.
            // This *should* never happen - if the button is visible, then the group should exist.
            log.fine("ToolBarGroupRemoveAction.actionPerformed() - rejected because group doesn't exist: \""
                             + groupName + "\"");
            return;
        }

        // Confirming the removal should probably be a configuration option, but eh,
        // callers always have the option of disabling this built-in action and supplying their own.
        Window owner = SwingUtilities.getWindowAncestor(actionPanel);
        if (JOptionPane.showConfirmDialog(owner, "Are you sure you want to remove the group \""
                                                  + groupName + "\"?",
                                          "Confirm group removal",
                                          JOptionPane.OK_CANCEL_OPTION,
                                          JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
            actionPanel.removeGroup(groupName);
        }
    }
}
