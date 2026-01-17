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
        setTooltip(Direction.UP == direction ? "Move selected item(s) up" : "Move selected item(s) down");
        if (listField == null || direction == null) {
            throw new IllegalArgumentException("listField and direction must not be null");
        }
    }

    /**
     * Creates a new ListItemMoveAction with the specified icon and ListField.
     *
     * @param icon      The icon for this action.
     * @param listField The ListField in which selected items will be moved. Must not be null.
     * @param direction The direction in which selected items will be moved. Must not be null.
     */
    public ListItemMoveAction(Icon icon, ListField<T> listField, Direction direction) {
        super(null, icon);
        this.listField = listField;
        this.direction = direction;
        setTooltip(Direction.UP == direction ? "Move selected item(s) up" : "Move selected item(s) down");
        if (listField == null || direction == null) {
            throw new IllegalArgumentException("listField and direction must not be null");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int[] selectedIndices = listField.getSelectedIndexes();
        if (selectedIndices.length == 0) {
            return; // Nothing selected, nothing to do
        }

        // We need to preserve multiple selection, so we can't just setSelectedIndex after moving each item.
        int[] newSelectedIndices = new int[selectedIndices.length];

        // Programmer's note: The "wonky case" handling below seems to mightily confuse AI assistants.
        //     Both GitHub Copilot and also Claude Sonnet 4.5 (amazingly) want to incorrectly
        //     modify this code so that we look at the *old* selected indeces and not the new ones.
        //     The code below is correct! The AIs are both wrong! A rare day! Copilot also suggested
        //     starting from the end of the list when moving up, which is also incorrect.
        //     I'm particularly surprised that Claude Sonnet got this wrong, as it is usually excellent
        //     at stuff like this. Don't take my word for it - check the unit tests for proof.

        if (direction == Direction.UP) {
            for (int i = 0; i < selectedIndices.length; i++) {
                int index = selectedIndices[i];
                if (index > 0) {
                    // Wonky case: if we go to move an item up, but the item
                    // immediately above it is now selected, just skip it:
                    // (this happens when we move a group of items to the top of the list,
                    //  and then try to move them up again)
                    if (i > 0 && newSelectedIndices[i - 1] == index - 1) {
                        newSelectedIndices[i] = index; // Can't move up, stays the same
                        continue;
                    }

                    T item = listField.getListModel().getElementAt(index);
                    listField.getListModel().removeElementAt(index);
                    listField.getListModel().add(index - 1, item);
                    newSelectedIndices[i] = index - 1;
                }
                else {
                    newSelectedIndices[i] = index; // Can't move up, stays the same
                }
            }
        }
        else if (direction == Direction.DOWN) {
            for (int i = selectedIndices.length - 1; i >= 0; i--) {
                int index = selectedIndices[i];
                if (index < listField.getListModel().getSize() - 1) {
                    // Wonky case: if we go to move an item down, but the item
                    // immediately below it is now selected, just skip it:
                    // (this happens when we move a group of items to the bottom of the list,
                    //  and then try to move them down again)
                    if (i < selectedIndices.length - 1 && newSelectedIndices[i + 1] == index + 1) {
                        newSelectedIndices[i] = index; // Can't move down, stays the same
                        continue;
                    }
                    T item = listField.getListModel().getElementAt(index);
                    listField.getListModel().removeElementAt(index);
                    listField.getListModel().add(index + 1, item);
                    newSelectedIndices[i] = index + 1;
                }
                else {
                    newSelectedIndices[i] = index; // Can't move down, stays the same
                }
            }
        }
        listField.setSelectedIndexes(newSelectedIndices);
    }
}
