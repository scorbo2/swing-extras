package ca.corbett.forms.actions;

import ca.corbett.forms.fields.ListField;
import org.junit.jupiter.api.Test;

import javax.swing.ListSelectionModel;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ListItemSelectAllActionTest {

    @Test
    public void selectAll_withMultipleItems_shouldSelectAll() {
        // GIVEN a ListField<String> that supports multi-select:
        ListField<String> listField = new ListField<>("Test:", List.of("Item 1", "Item 2", "Item 3", "Item 4"));
        listField.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // WHEN we create and execute a ListItemSelectAllAction:
        ListItemSelectAllAction selectAllAction = new ListItemSelectAllAction(listField);
        selectAllAction.actionPerformed(null);

        // THEN all items in the list should be selected:
        int expectedSelectionCount = listField.getListModel().getSize();
        int actualSelectionCount = listField.getList().getSelectedIndices().length;
        assertEquals(expectedSelectionCount, actualSelectionCount, "Expected all items to be selected.");
    }

    @Test
    public void selectAll_withSingleItem_shouldSelectThatItem() {
        // GIVEN a ListField<String> with a single item:
        ListField<String> listField = new ListField<>("Test:", List.of("Only Item"));
        listField.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // WHEN we create and execute a ListItemSelectAllAction:
        ListItemSelectAllAction selectAllAction = new ListItemSelectAllAction(listField);
        selectAllAction.actionPerformed(null);

        // THEN the single item in the list should be selected:
        int[] selectedIndices = listField.getList().getSelectedIndices();
        assertEquals(1, selectedIndices.length, "Expected one item to be selected.");
        assertEquals(0, selectedIndices[0], "Expected the first (and only) item to be selected.");
    }

    @Test
    public void selectAll_withEmptyList_shouldSelectNothing() {
        // GIVEN a ListField<String> with no items:
        ListField<String> listField = new ListField<>("Test:", List.of());
        listField.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // WHEN we create and execute a ListItemSelectAllAction:
        ListItemSelectAllAction selectAllAction = new ListItemSelectAllAction(listField);
        selectAllAction.actionPerformed(null);

        // THEN no items should be selected:
        int actualSelectionCount = listField.getList().getSelectedIndices().length;
        assertEquals(0, actualSelectionCount, "Expected no items to be selected.");
    }

    @Test
    public void selectAll_withSingleSelectListAndMultipleItems_shouldSelectLastItemOnly() {
        // GIVEN a ListField<String> with multiple items but SINGLE_SELECTION mode:
        ListField<String> listField = new ListField<>("Test:", List.of("Item 1", "Item 2", "Item 3"));
        listField.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // WHEN we create and execute a ListItemSelectAllAction:
        ListItemSelectAllAction selectAllAction = new ListItemSelectAllAction(listField);
        selectAllAction.actionPerformed(null);

        // THEN only the last item should be selected:
        int[] selectedIndices = listField.getList().getSelectedIndices();
        assertEquals(1, selectedIndices.length, "Expected one item to be selected.");
        assertEquals(2, selectedIndices[0], "Expected the last item to be selected.");
    }
}
