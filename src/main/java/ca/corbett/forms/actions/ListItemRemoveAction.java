package ca.corbett.forms.actions;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.forms.fields.ListField;

import javax.swing.Icon;
import java.awt.event.ActionEvent;

/**
 * An EnhancedAction that can be used with any ListField to remove the selected items from the list.
 * The list may support multiple selection or single selection, it will work in either case.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.7
 */
public class ListItemRemoveAction extends EnhancedAction {

    private static final String DEFAULT_NAME = "Remove";
    private final ListField<?> listField;

    /**
     * Creates a new ListItemRemoveAction with the default name and the given ListField.
     *
     * @param listField The ListField from which selected items will be removed. Must not be null.
     */
    public ListItemRemoveAction(ListField<?> listField) {
        this(DEFAULT_NAME, listField);
        if (listField == null) {
            throw new IllegalArgumentException("listField must not be null");
        }
    }

    /**
     * Creates a new ListItemRemoveAction with the specified name and ListField.
     *
     * @param name      The name for this action.
     * @param listField The ListField from which selected items will be removed. Must not be null.
     */
    public ListItemRemoveAction(String name, ListField<?> listField) {
        super(name);
        this.listField = listField;
        if (listField == null) {
            throw new IllegalArgumentException("listField must not be null");
        }
    }

    /**
     * Creates a new ListItemRemoveAction with the specified icon and ListField.
     *
     * @param icon      The icon for this action.
     * @param listField The ListField from which selected items will be removed. Must not be null.
     */
    public ListItemRemoveAction(Icon icon, ListField<?> listField) {
        super(null, icon);
        this.listField = listField;
        if (listField == null) {
            throw new IllegalArgumentException("listField must not be null");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // The list may or may not allow multiple selection.
        // Just grab all selected indexes and remove them in reverse order.
        int[] selectedIndexes = listField.getSelectedIndexes();
        for (int i = selectedIndexes.length - 1; i >= 0; i--) {
            listField.getListModel().removeElementAt(selectedIndexes[i]);
        }
    }
}
