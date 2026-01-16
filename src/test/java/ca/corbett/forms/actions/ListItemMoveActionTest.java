package ca.corbett.forms.actions;

import ca.corbett.forms.fields.ListField;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ListItemMoveActionTest {

    private final List<String> testItems = List.of("Item 1", "Item 2", "Item 3", "Item 4", "Item 5");

    @Test
    public void moveItemsUp_withSingleItemSelected_shouldMoveItemUp() {
        // GIVEN a ListField with multiple items and one item selected:
        ListField<String> listField = new ListField<>("Test:", testItems);
        listField.getList().setSelectedIndex(2); // Select "Item 3"

        // WHEN we move the selected item up:
        ListItemMoveAction<String> moveUpAction = new ListItemMoveAction<>(listField, ListItemMoveAction.Direction.UP);
        moveUpAction.actionPerformed(null);

        // THEN the selected item should have moved up one position:
        String listItemAtIndex1 = listField.getListModel().getElementAt(1);
        String listItemAtIndex2 = listField.getListModel().getElementAt(2);
        assertEquals("Item 3", listItemAtIndex1); // This one moved up
        assertEquals("Item 2", listItemAtIndex2); // This one got bumped down
    }

    @Test
    public void moveItemsUp_withTopItemSelected_shouldNotChangeList() {
        // GIVEN a ListField with multiple items and the top item selected:
        ListField<String> listField = new ListField<>("Test:", testItems);
        listField.getList().setSelectedIndex(0); // Select "Item 1"

        // WHEN we move the selected item up:
        ListItemMoveAction<String> moveUpAction = new ListItemMoveAction<>(listField, ListItemMoveAction.Direction.UP);
        moveUpAction.actionPerformed(null);

        // THEN the list should remain unchanged:
        for (int i = 0; i < testItems.size(); i++) {
            assertEquals(testItems.get(i), listField.getListModel().getElementAt(i));
        }
    }

    @Test
    public void moveItemsUp_withMultipleItemsSelected_shouldMoveGroup() {
        // GIVEN a ListField with multiple items and multiple items selected:
        ListField<String> listField = new ListField<>("Test:", testItems);
        listField.getList().setSelectedIndices(new int[]{2, 3}); // Select "Item 3" and "Item 4"

        // WHEN we move the selected items up:
        ListItemMoveAction<String> moveUpAction = new ListItemMoveAction<>(listField, ListItemMoveAction.Direction.UP);
        moveUpAction.actionPerformed(null);

        // THEN the selected items should have moved up one position as a group:
        assertEquals("Item 3", listField.getListModel().getElementAt(1)); // Moved up
        assertEquals("Item 4", listField.getListModel().getElementAt(2)); // Moved up
        assertEquals("Item 2", listField.getListModel().getElementAt(3)); // Bumped down
    }

    @Test
    public void moveItemsUp_withMultipleItemsAtTopSelected_shouldNotChangeList() {
        // GIVEN a ListField with multiple items and the top two items selected:
        ListField<String> listField = new ListField<>("Test:", testItems);
        listField.getList().setSelectedIndices(new int[]{0, 1}); // Select "Item 1" and "Item 2"

        // WHEN we move the selected items up:
        ListItemMoveAction<String> moveUpAction = new ListItemMoveAction<>(listField, ListItemMoveAction.Direction.UP);
        moveUpAction.actionPerformed(null);

        // THEN the list should remain unchanged:
        for (int i = 0; i < testItems.size(); i++) {
            assertEquals(testItems.get(i), listField.getListModel().getElementAt(i));
        }
    }

    @Test
    public void moveItemsUp_withTopItemAndNonContiguousItemSelected_shouldMoveOnlyNonContiguousItem() {
        // GIVEN a ListField with multiple items and the top item and a non-contiguous item selected:
        ListField<String> listField = new ListField<>("Test:", testItems);
        listField.getList().setSelectedIndices(new int[]{0, 2}); // Select "Item 1" and "Item 3"

        // WHEN we move the selected items up:
        ListItemMoveAction<String> moveUpAction = new ListItemMoveAction<>(listField, ListItemMoveAction.Direction.UP);
        moveUpAction.actionPerformed(null);

        // THEN only the non-contiguous item should have moved up:
        assertEquals("Item 1", listField.getListModel().getElementAt(0)); // Remains in place
        assertEquals("Item 3", listField.getListModel().getElementAt(1)); // Moved up
        assertEquals("Item 2", listField.getListModel().getElementAt(2)); // Bumped down
    }

    @Test
    public void moveItemsDown_withSingleItemSelected_shouldMoveItemDown() {
        // GIVEN a ListField with multiple items and one item selected:
        ListField<String> listField = new ListField<>("Test:", testItems);
        listField.getList().setSelectedIndex(2); // Select "Item 3"

        // WHEN we move the selected item down:
        ListItemMoveAction<String> moveDownAction = new ListItemMoveAction<>(listField,
                                                                             ListItemMoveAction.Direction.DOWN);
        moveDownAction.actionPerformed(null);

        // THEN the selected item should have moved down one position:
        String listItemAtIndex2 = listField.getListModel().getElementAt(2);
        String listItemAtIndex3 = listField.getListModel().getElementAt(3);
        assertEquals("Item 4", listItemAtIndex2); // This one got bumped up
        assertEquals("Item 3", listItemAtIndex3); // This one moved down
    }

    @Test
    public void moveItemsDown_withBottomItemSelected_shouldNotChangeList() {
        // GIVEN a ListField with multiple items and the bottom item selected:
        ListField<String> listField = new ListField<>("Test:", testItems);
        listField.getList().setSelectedIndex(4); // Select "Item 5"

        // WHEN we move the selected item down:
        ListItemMoveAction<String> moveDownAction = new ListItemMoveAction<>(listField,
                                                                             ListItemMoveAction.Direction.DOWN);
        moveDownAction.actionPerformed(null);

        // THEN the list should remain unchanged:
        for (int i = 0; i < testItems.size(); i++) {
            assertEquals(testItems.get(i), listField.getListModel().getElementAt(i));
        }
    }

    @Test
    public void moveItemsDown_withMultipleItemsSelected_shouldMoveGroup() {
        // GIVEN a ListField with multiple items and multiple items selected:
        ListField<String> listField = new ListField<>("Test:", testItems);
        listField.getList().setSelectedIndices(new int[]{1, 2}); // Select "Item 2" and "Item 3"

        // WHEN we move the selected items down:
        ListItemMoveAction<String> moveDownAction = new ListItemMoveAction<>(listField,
                                                                             ListItemMoveAction.Direction.DOWN);
        moveDownAction.actionPerformed(null);

        // THEN the selected items should have moved down one position as a group:
        assertEquals("Item 4", listField.getListModel().getElementAt(1)); // Bumped up
        assertEquals("Item 2", listField.getListModel().getElementAt(2)); // Moved down
        assertEquals("Item 3", listField.getListModel().getElementAt(3)); // Moved down
    }

    @Test
    public void moveItemsDown_withMultipleItemsAtBottomSelected_shouldNotChangeList() {
        // GIVEN a ListField with multiple items and the bottom two items selected:
        ListField<String> listField = new ListField<>("Test:", testItems);
        listField.getList().setSelectedIndices(new int[]{3, 4}); // Select "Item 4" and "Item 5"

        // WHEN we move the selected items down:
        ListItemMoveAction<String> moveDownAction = new ListItemMoveAction<>(listField,
                                                                             ListItemMoveAction.Direction.DOWN);
        moveDownAction.actionPerformed(null);

        // THEN the list should remain unchanged:
        for (int i = 0; i < testItems.size(); i++) {
            assertEquals(testItems.get(i), listField.getListModel().getElementAt(i));
        }
    }

    @Test
    public void moveItemsDown_withBottomItemAndNonContiguousItemSelected_shouldMoveOnlyNonContiguousItem() {
        // GIVEN a ListField with multiple items and the bottom item and a non-contiguous item selected:
        ListField<String> listField = new ListField<>("Test:", testItems);
        listField.getList().setSelectedIndices(new int[]{2, 4}); // Select "Item 3" and "Item 5"

        // WHEN we move the selected items down:
        ListItemMoveAction<String> moveDownAction = new ListItemMoveAction<>(listField,
                                                                             ListItemMoveAction.Direction.DOWN);
        moveDownAction.actionPerformed(null);

        // THEN only the non-contiguous item should have moved down:
        assertEquals("Item 4", listField.getListModel().getElementAt(2)); // Bumped up
        assertEquals("Item 3", listField.getListModel().getElementAt(3)); // Moved down
        assertEquals("Item 5", listField.getListModel().getElementAt(4)); // Remains in place
    }
}
