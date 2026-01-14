package ca.corbett.forms.fields;

import ca.corbett.extras.io.KeyboardManager;

import javax.swing.Action;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * A custom FormField implementation that shows all configured keyboard
 * shortcuts in the given KeyboardManager, helps the user identify any conflicts,
 * and allows remapping of existing shortcut actions to some other key combination.
 * <p>
 * Internally, this field wraps a JTable to show the list of shortcuts and their
 * associated actions (by action name). The user can sort the table by clicking
 * on either column header, and can double-click on a row to edit the shortcut
 * key combination for that action. Conflicting shortcuts are highlighted in red
 * (or some color appropriate to the current Look and Feel) to help the user identify
 * and resolve conflicts.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.7
 */
public class KeyboardManagerField extends FormField {

    // TODO this is a bit of a mess - currently, KeyboardManager allows any Action to be assigned to
    //      a shortcut, but I think this is too loose. We maybe need a custom Action subclass with
    //      some kind of mandatory "ID" field (like name, but not necessarily the action name).
    //      It should be user-presentable, like a short description of the action, but we can't use
    //      the action's name because it's often button text, which may not be unique or descriptive enough.
    //      An action named "Enable" is useless to the user in this table, because it could be anything.
    //      We need something like "Enable Dark Mode" or "Enable Spell Check", etc.
    //      AbstractAction can store arbitrary properties, but we can't just require callers to
    //      specify a specific property - we need to enforce it somehow. A custom subclass with a mandatory
    //      constructor parameter would do it, but now we're forcing callers to use our Action subclass
    //      instead of whatever they already have. Sigh. Maybe we can provide a utility method to wrap existing Actions?
    //      Maybe KeyboardManager's registerHandler() could take one extra mandatory parameter for this ID?
    //      I don't hate that idea - callers get to continue using their existing Actions, but KeyboardManager
    //      would now be equipped with extra info that we can use here. The burden on callers to name
    //      their actions as they register them is small and reasonable. Let's do that.
    //
    // TODO actually... does it have to be unique? What if I have multiple shortcuts for the same action?
    //      Ctrl+P and F5 both trigger "Print Document", for example. So maybe not unique, but
    //      we still need something better than the action name. Maybe just "action description".
    //      We can require callers to provide a short description when registering an action.
    //      Maybe move our KeyAssignment class to KeyboardManager, add the Action to it, and store a list
    //      of them there. Then we could interrogate it easily here. Yeah, that sounds better.

    private final KeyboardManager keyManager;
    private final JTable table;
    private final KeyAssignmentTableModel tableModel; // wtf - you can't parameterize DefaultTableModel?

    public KeyboardManagerField(String label, KeyboardManager keyManager) {
        this.keyManager = keyManager;
        fieldLabel.setText(label);
        tableModel = createTableModel(keyManager);

    }

    private KeyAssignmentTableModel createTableModel(KeyboardManager keyManager) {
        List<KeyAssignment> assignments = new ArrayList<>();
        List<String> shortcuts = keyManager.getRegisteredShortcuts(); // start with all known shortcuts
        for (String shortcut : shortcuts) {
            List<Action> actionsForShortcut = keyManager.getHandlers(shortcut);
            for (Action action : actionsForShortcut) {
                KeyAssignment assignment = new KeyAssignment();
                assignment.actionName = action.getProperty(KeyboardManager.ACTION_NAME_KEY).toString();
                assignment.keyStroke = shortcut; // TODO user-presentable string
                assignments.add(assignment);
            }
        }
        for (String actionName : keyManager.getAllActionNames()) {
            KeyAssignment assignment = new KeyAssignment();
            assignment.actionName = actionName;
            assignment.keyStroke = keyManager.getKeyStrokeForAction(actionName)
                                             .toString(); // TODO user-presentable string
            assignments.add(assignment);
        }
        return new KeyAssignmentTableModel(assignments);
    }

    private static class KeyAssignment {
        String actionName; // TODO we're assuming our action names will be unique... I don't think that will fly.
        String keyStroke; // We'll use our user-presentable string ("Ctrl+P", "Shift+Alt+F1", etc.)
    }

    private static class KeyAssignmentTableModel extends AbstractTableModel {

        private final List<KeyAssignment> keyAssignments = new ArrayList<>();

        public KeyAssignmentTableModel(List<KeyAssignment> assignments) {
            keyAssignments.addAll(assignments);
        }

        @Override
        public int getRowCount() {
            return keyAssignments.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            KeyAssignment assignment = keyAssignments.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> assignment.actionName;
                case 1 -> assignment.keyStroke;
                default -> null;
            };
        }
    }
}
