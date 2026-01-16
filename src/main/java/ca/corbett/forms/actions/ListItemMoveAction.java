package ca.corbett.forms.actions;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.forms.fields.ListField;

import javax.swing.Icon;
import java.awt.event.ActionEvent;

/**
 * An EnhancedAction that can be used with any ListField to move the selected items up or down in the list.
 *
 * @param <T> The type of items in the ListField.
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.7
 */
public class ListItemMoveAction<T> extends EnhancedAction {
    private final ListField<T> listField;
    private final Direction direction;

    /**
     * Specifies the direction in which list items will be moved when
     * this action is executed.
     */
    public enum Direction {
        UP,
        DOWN
    }

    /**
     * Creates a new ListItemMoveAction with the default name and the given ListField.
     *
     * @param listField The ListField in which selected items will be moved. Must not be null.
     * @param direction The direction in which selected items will be moved.
     */
    public ListItemMoveAction(ListField<T> listField, Direction direction) {
        this(Direction.UP == direction ? "Move up" : "Move down", listField, direction);
    }

    /**
     * Creates a new ListItemMoveAction with the specified name and ListField.
     *
     * @param name      The name for this action.
     * @param listField The ListField in which selected items will be moved. Must not be null.
     * @param direction The direction in which selected items will be moved. Must not be null.
     */
    public ListItemMoveAction(String name, ListField<T> listField, Direction direction) {
        super(name);
        this.listField = listField;
        this.direction = direction;
        if (listField == null || direction == null) {
            throw new IllegalArgumentException("listField and direction must not be null");
        }
    }

    /**
     * Creates a new ListItemMoveAction with the specified icon and ListField.
     *
     * @param icon      The icon for this action.
     * @param listField The ListField from in selected items will be moved. Must not be null.
     * @param direction The direction in which selected items will be moved. Must not be null.
     */
    public ListItemMoveAction(Icon icon, ListField<T> listField, Direction direction) {
        super(null, icon);
        this.listField = listField;
        this.direction = direction;
        if (listField == null || direction == null) {
            throw new IllegalArgumentException("listField must not be null");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int[] selectedIndices = listField.getSelectedIndexes();
        if (selectedIndices.length == 0) {
            return; // Nothing selected, nothing to do
        }
        if (direction == Direction.UP) {
            for (int index : selectedIndices) {
                if (index > 0) {
                    T item = listField.getListModel().getElementAt(index);
                    listField.getListModel().removeElementAt(index);
                    listField.getListModel().add(index - 1, item);
                    listField.setSelectedIndex(index - 1);
                }
            }
        }
        else if (direction == Direction.DOWN) {
            for (int i = selectedIndices.length - 1; i >= 0; i--) {
                int index = selectedIndices[i];
                if (index < listField.getListModel().getSize() - 1) {
                    T item = listField.getListModel().getElementAt(index);
                    listField.getListModel().removeElementAt(index);
                    listField.getListModel().add(index + 1, item);
                    listField.setSelectedIndex(index + 1);
                }
            }
        }
    }
}
