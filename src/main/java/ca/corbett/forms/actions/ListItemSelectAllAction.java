package ca.corbett.forms.actions;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.forms.fields.ListField;

import javax.swing.Icon;
import java.awt.event.ActionEvent;

/**
 * An EnhancedAction that can be used with any ListField to select all items in the list.
 * We assume that the list supports multiple selection. If it does not, only the
 * last item in the list will be selected when the action executes. (This is standard
 * JList behavior.)
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.7
 */
public class ListItemSelectAllAction extends EnhancedAction {
    private static final String DEFAULT_NAME = "Select all";
    private final ListField<?> listField;

    /**
     * Creates a new ListItemSelectAllAction with the default name and the given ListField.
     *
     * @param listField The ListField in which items will be selected. Must not be null.
     */
    public ListItemSelectAllAction(ListField<?> listField) {
        this(DEFAULT_NAME, listField);
    }

    /**
     * Creates a new ListItemSelectAllAction with the specified name and ListField.
     *
     * @param name      The name for this action.
     * @param listField The ListField in which items will be selected. Must not be null.
     */
    public ListItemSelectAllAction(String name, ListField<?> listField) {
        super(name);
        this.listField = listField;
        setTooltip("Select all items in the list");
        if (listField == null) {
            throw new IllegalArgumentException("listField must not be null");
        }
    }

    /**
     * Creates a new ListItemSelectAllAction with the specified icon and ListField.
     *
     * @param icon      The icon for this action.
     * @param listField The ListField in which items will be selected. Must not be null.
     */
    public ListItemSelectAllAction(Icon icon, ListField<?> listField) {
        super(null, icon);
        this.listField = listField;
        setTooltip("Select all items in the list");
        if (listField == null) {
            throw new IllegalArgumentException("listField must not be null");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // We could throw an exception here if the list doesn't support multiple selection,
        // but JList will just select the last item in SINGLE_SELECTION mode, and that's
        // an acceptable fallback behavior. It's up to callers to set their list up correctly.
        int size = listField.getListModel().getSize();
        if (size > 0) {
            listField.getList().setSelectionInterval(0, size - 1);
        }
    }
}
