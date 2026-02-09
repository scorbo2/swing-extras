package ca.corbett.extras.actionpanel;

import ca.corbett.extras.TextInputDialog;
import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.LongTextField;
import ca.corbett.forms.fields.ShortTextField;
import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.ValidationResult;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import java.awt.Window;
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
class ToolBarGroupRenameAction extends ToolBarAction {

    public ToolBarGroupRenameAction(ActionPanel actionPanel, String groupName, String tooltip, ImageIcon icon) {
        super(actionPanel, groupName, tooltip, icon);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Window ownerWindow = SwingUtilities.getWindowAncestor(actionPanel);
        TextInputDialog dialog = new TextInputDialog(ownerWindow, "Rename group", TextInputDialog.InputType.SingleLine);
        dialog.setAllowBlank(false);
        dialog.addValidator(new TextInputValidator(groupName));
        dialog.setInitialText(groupName); // convenient starting point for renaming
        dialog.setVisible(true);
        String newName = dialog.getResult();
        if (newName != null) {
            actionPanel.renameGroup(groupName, newName); // triggers a rebuild of the ActionPanel
        }
    }

    private class TextInputValidator implements FieldValidator<FormField> {

        private final String originalName;

        public TextInputValidator(String originalName) {
            this.originalName = originalName;

        }

        @Override
        public ValidationResult validate(FormField fieldToValidate) {
            String newName;
            if (fieldToValidate instanceof ShortTextField) {
                newName = ((ShortTextField)fieldToValidate).getText();
            }
            else {
                newName = ((LongTextField)fieldToValidate).getText();
            }

            // If the new name matches the original name, we'll allow it:
            // (this may seem like an unusual case, but you could actually do this to change
            //  the case of the name - e.g. "my group" -> "My Group" - we generally treat
            //  group names as case-insensitive, but the caller may care about it).
            if (originalName.equalsIgnoreCase(newName)) {
                return ValidationResult.valid();
            }

            // Otherwise, check for uniqueness, without considering case.
            // This means that if "My Group" already exists, you won't be able to rename another
            // group to "my group" (or any other case variation).
            if (actionPanel.hasGroup(newName)) {
                return ValidationResult.invalid("A group with this name already exists. Names must be unique.");
            }

            // If we get here, the new name is valid:
            // (we don't explicitly check for blank strings here because our TextInputDialog won't allow it)
            return ValidationResult.valid();
        }
    }
}
