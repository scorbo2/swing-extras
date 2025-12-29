package ca.corbett.forms.fields;

import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.ValidationResult;
import org.junit.jupiter.api.Test;

import javax.swing.JPanel;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListSubsetFieldTest extends FormFieldBaseTests {

    @Override
    protected FormField createTestObject() {
        return new ListSubsetField<>("Test Subset Field",
                                     List.of("Item 1", "Item 2", "Item 3", "Item 4", "Item 5"));
    }

    @Test
    public void getFieldComponent_shouldBeJPanel() {
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3"));
        assertInstanceOf(JPanel.class, subsetField.getFieldComponent());
    }

    @Test
    public void shouldExpand_defaultShouldBeFalse() {
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3"));
        assertFalse(subsetField.shouldExpand());
    }

    @Test
    public void constructor_withAvailableItems_shouldMaintainOrder() {
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 9", "Item 2", "Item 3"));
        assertNotNull(subsetField.getAvailableItems());
        assertEquals(4, subsetField.getAvailableItems().size());
        assertEquals("Item 1", subsetField.getAvailableItems().get(0));
        assertEquals("Item 9", subsetField.getAvailableItems().get(1));
        assertEquals("Item 2", subsetField.getAvailableItems().get(2));
        assertEquals("Item 3", subsetField.getAvailableItems().get(3));
    }

    @Test
    public void constructor_withAvailableAndSelectedItems_shouldMaintainOrder() {
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 9", "Item 2", "Item 3"),
                java.util.List.of("Item 2", "Item 3"));
        assertNotNull(subsetField.getAvailableItems());
        assertEquals(2, subsetField.getAvailableItems().size());
        assertEquals("Item 1", subsetField.getAvailableItems().get(0));
        assertEquals("Item 9", subsetField.getAvailableItems().get(1));

        assertNotNull(subsetField.getSelectedItems());
        assertEquals(2, subsetField.getSelectedItems().size());
        assertEquals("Item 2", subsetField.getSelectedItems().get(0));
        assertEquals("Item 3", subsetField.getSelectedItems().get(1));
    }

    @Test
    public void moveItemRight_withNonExistingItem_shouldDoNothing() {
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3"));
        subsetField.moveItemRight("Item 4"); // Item 4 does not exist
        assertEquals(3, subsetField.getAvailableItems().size());
        assertEquals(0, subsetField.getSelectedItems().size());
    }

    @Test
    public void moveItemLeft_withNonExistingItem_shouldDoNothing() {
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3"),
                java.util.List.of("Item 4")); // Item 4 is selected
        subsetField.moveItemLeft("Item 5"); // Item 5 does not exist
        assertEquals(3, subsetField.getAvailableItems().size());
        assertEquals(1, subsetField.getSelectedItems().size());
    }

    @Test
    public void moveItemRight_andMoveItemLeft_shouldUpdateListsCorrectly() {
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3"));

        subsetField.moveItemRight("Item 2");
        assertEquals(2, subsetField.getAvailableItems().size());
        assertEquals(1, subsetField.getSelectedItems().size());
        assertEquals("Item 1", subsetField.getAvailableItems().get(0));
        assertEquals("Item 3", subsetField.getAvailableItems().get(1));
        assertEquals("Item 2", subsetField.getSelectedItems().get(0));

        subsetField.moveItemLeft("Item 2");
        assertEquals(3, subsetField.getAvailableItems().size());
        assertEquals(0, subsetField.getSelectedItems().size());
        assertEquals("Item 1", subsetField.getAvailableItems().get(0));
        assertEquals("Item 3", subsetField.getAvailableItems().get(1));
        // When auto-sorting is disabled, moved items are appended to the end of the list
        assertEquals("Item 2", subsetField.getAvailableItems().get(2));
    }

    @Test
    public void moveAllItemsRight_andMoveAllItemsLeft_shouldUpdateListsCorrectly() {
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3"));

        subsetField.moveAllItemsRight();
        assertEquals(0, subsetField.getAvailableItems().size());
        assertEquals(3, subsetField.getSelectedItems().size());

        subsetField.moveAllItemsLeft();
        assertEquals(3, subsetField.getAvailableItems().size());
        assertEquals(0, subsetField.getSelectedItems().size());
    }

    @Test
    public void getAvailableAndSelectedItems_shouldReturnCorrectLists() {
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3"),
                java.util.List.of("Item 2"));

        List<String> availableItems = subsetField.getAvailableItems();
        List<String> selectedItems = subsetField.getSelectedItems();

        assertEquals(2, availableItems.size());
        assertEquals("Item 1", availableItems.get(0));
        assertEquals("Item 3", availableItems.get(1));

        assertEquals(1, selectedItems.size());
        assertEquals("Item 2", selectedItems.get(0));
    }

    @Test
    public void setVisibleRowCount_shouldUpdateRowCountIfValid() {
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3"));

        // GIVEN a valid row count:
        subsetField.setVisibleRowCount(10);

        // THEN the visible row count is updated:
        assertEquals(10, subsetField.getVisibleRowCount());

        // GIVEN an invalid row count:
        subsetField.setVisibleRowCount(-100);

        // THEN the visible row count remains unchanged:
        assertEquals(10, subsetField.getVisibleRowCount());
    }

    @Test
    public void setFixedCellWidth_shouldUpdateCellWidthIfValid() {
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3"));

        // GIVEN a valid cell width:
        subsetField.setFixedCellWidth(150);

        // THEN the fixed cell width is updated:
        assertEquals(150, subsetField.getFixedCellWidth());

        // GIVEN an invalid cell width:
        subsetField.setFixedCellWidth(-100);

        // THEN the fixed cell width remains unchanged:
        assertEquals(150, subsetField.getFixedCellWidth());
    }

    @Test
    public void selectIndexes_withValidIndexes_shouldSelectCorrectItems() {
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3", "Item 4"));

        subsetField.selectIndexes(new int[]{1, 3}); // Select "Item 2" and "Item 4"

        List<String> selectedItems = subsetField.getSelectedItems();
        assertEquals(2, selectedItems.size());
        assertEquals("Item 2", selectedItems.get(0));
        assertEquals("Item 4", selectedItems.get(1));
    }

    @Test
    public void selectIndexes_withSomeItemsAlreadySelected_shouldUpdateSelectionCorrectly() {
        // GIVEN a list with some available items:
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3", "Item 4"));

        // WHEN we move one item to the right...
        subsetField.moveItemRight("Item 1"); // Select "Item 1"

        // and WHEN we invoke selectIndexes(), the previous selection should be discarded and replaced:
        // selectIndexes uses a sorted reference frame, so indexes [2, 3] refer to "Item 3" and "Item 4"
        subsetField.selectIndexes(new int[]{2, 3}); // Select "Item 3" and "Item 4"

        // THEN only the newly selected items should be selected:
        List<String> selectedItems = subsetField.getSelectedItems();
        assertEquals(2, selectedItems.size());
        assertEquals("Item 3", selectedItems.get(0));
        assertEquals("Item 4", selectedItems.get(1));
    }

    @Test
    public void validate_invalidScenario() {
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3"));
        subsetField.addFieldValidator(new TestValidator());
        // No items selected
        assertFalse(subsetField.isValid());
        assertEquals(TestValidator.MSG, subsetField.getValidationLabel().getToolTipText());
    }

    @Test
    public void selectIndexes_withInvalidIndexes_shouldIgnoreInvalidIndexes() {
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3", "Item 4"));

        subsetField.selectIndexes(new int[]{0, 5, -1, 2}); // Only indexes 0 and 2 are valid

        List<String> selectedItems = subsetField.getSelectedItems();
        assertEquals(2, selectedItems.size());
        assertEquals("Item 1", selectedItems.get(0));
        assertEquals("Item 3", selectedItems.get(1));
    }

    @Test
    public void getSelectedIndexes_withNothingSelected_shouldReturnEmptyArray() {
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3"));

        int[] selectedIndexes = subsetField.getSelectedIndexes();
        assertEquals(0, selectedIndexes.length);
    }

    @Test
    public void getSelectedIndexes_withSelection_shouldReturnSelectedIndexes() {
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3", "Item 4"));

        subsetField.moveItemRight("Item 2"); // Select "Item 2"
        subsetField.moveItemRight("Item 4"); // Select "Item 4"

        int[] selectedIndexes = subsetField.getSelectedIndexes();
        assertEquals(2, selectedIndexes.length);
        // getSelectedIndexes uses a sorted reference frame, so "Item 2" is at index 1 and "Item 4" at index 3
        assertEquals(1, selectedIndexes[0]); // Index of "Item 2" in sorted list
        assertEquals(3, selectedIndexes[1]); // Index of "Item 4" in sorted list
    }

    @Test
    public void validate_validScenario() {
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3"));
        subsetField.addFieldValidator(new TestValidator());
        subsetField.moveItemRight("Item 2"); // Select an item
        assertTrue(subsetField.isValid());
        assertNull(subsetField.getValidationLabel().getToolTipText());
    }

    @Test
    public void autoSortingEnabled_defaultShouldBeFalse() {
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3"));
        assertFalse(subsetField.isAutoSortingEnabled());
    }

    @Test
    public void autoSortingEnabled_shouldSortWhenEnabled() {
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 9", "Item 2", "Item 3"));
        subsetField.setAutoSortingEnabled(true);
        
        // Move item to the right - should be sorted
        subsetField.moveItemRight("Item 9");
        subsetField.moveItemRight("Item 2");
        
        List<String> selectedItems = subsetField.getSelectedItems();
        assertEquals(2, selectedItems.size());
        assertEquals("Item 2", selectedItems.get(0));
        assertEquals("Item 9", selectedItems.get(1));
    }

    @Test
    public void autoSortingDisabled_shouldMaintainOrderWhenMoving() {
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 9", "Item 2", "Item 3"));
        
        // Move items to the right - should maintain order added
        subsetField.moveItemRight("Item 9");
        subsetField.moveItemRight("Item 2");
        
        List<String> selectedItems = subsetField.getSelectedItems();
        assertEquals(2, selectedItems.size());
        assertEquals("Item 9", selectedItems.get(0));
        assertEquals("Item 2", selectedItems.get(1));
    }

    @Test
    public void selectIndexes_withAutoSortingDisabled_shouldUseIndexesFromSortedReference() {
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("One", "Two", "Three", "Four", "Five", "Six"));
        
        // Even with auto-sorting disabled, selectIndexes uses a sorted reference frame
        // Sorted list: ["Five", "Four", "One", "Six", "Three", "Two"]
        // Indexes 4 and 5 refer to "Three" and "Two" in the sorted list
        subsetField.selectIndexes(new int[]{4, 5});
        
        List<String> availableItems = subsetField.getAvailableItems();
        assertEquals(4, availableItems.size());
        // Without auto-sorting, available items maintain their insertion order
        assertEquals("One", availableItems.get(0));
        assertEquals("Four", availableItems.get(1));
        assertEquals("Five", availableItems.get(2));
        assertEquals("Six", availableItems.get(3));
        
        List<String> selectedItems = subsetField.getSelectedItems();
        assertEquals(2, selectedItems.size());
        // Without auto-sorting, selected items maintain their insertion order
        assertEquals("Three", selectedItems.get(0));
        assertEquals("Two", selectedItems.get(1));
    }

    @Test
    public void moveItemRight_withAutoSortingDisabled_shouldPreserveInsertionOrder() {
        // This demonstrates the preferred way to select specific items when auto-sorting is disabled
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("One", "Two", "Three", "Four", "Five", "Six"));
        
        // Move specific items to the right (select them)
        subsetField.moveItemRight("One");
        subsetField.moveItemRight("Two");
        
        List<String> availableItems = subsetField.getAvailableItems();
        assertEquals(4, availableItems.size());
        // Available items maintain their original order (minus the selected ones)
        assertEquals("Three", availableItems.get(0));
        assertEquals("Four", availableItems.get(1));
        assertEquals("Five", availableItems.get(2));
        assertEquals("Six", availableItems.get(3));
        
        List<String> selectedItems = subsetField.getSelectedItems();
        assertEquals(2, selectedItems.size());
        // Selected items are in the order they were moved
        assertEquals("One", selectedItems.get(0));
        assertEquals("Two", selectedItems.get(1));
    }

    @Test
    public void constructor_withAutoSortingEnabled_shouldSortLists() {
        // Create a field with items in non-sorted order
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 9", "Item 2", "Item 3"));
        
        // Enabling auto-sort should immediately sort both lists
        subsetField.setAutoSortingEnabled(true);
        
        // The available list should now be sorted
        List<String> availableItems = subsetField.getAvailableItems();
        assertEquals(4, availableItems.size());
        assertEquals("Item 1", availableItems.get(0));
        assertEquals("Item 2", availableItems.get(1));
        assertEquals("Item 3", availableItems.get(2));
        assertEquals("Item 9", availableItems.get(3));
    }

    @Test
    public void setAutoSortingEnabled_whenEnabled_shouldSortBothLists() {
        // Create a field with items in both lists, unsorted
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Zebra", "Apple", "Mango", "Banana"));
        
        // Move some items without auto-sorting
        subsetField.moveItemRight("Mango");
        subsetField.moveItemRight("Apple");
        
        // At this point, lists are unsorted:
        // Available: [Zebra, Banana], Selected: [Mango, Apple]
        
        // Now enable auto-sorting - this should sort both lists immediately
        subsetField.setAutoSortingEnabled(true);
        
        // Verify both lists are now sorted
        List<String> availableItems = subsetField.getAvailableItems();
        assertEquals(2, availableItems.size());
        assertEquals("Banana", availableItems.get(0));
        assertEquals("Zebra", availableItems.get(1));
        
        List<String> selectedItems = subsetField.getSelectedItems();
        assertEquals(2, selectedItems.size());
        assertEquals("Apple", selectedItems.get(0));
        assertEquals("Mango", selectedItems.get(1));
    }

    @Test
    public void setAutoSortingEnabled_whenDisabled_shouldNotChangeOrder() {
        // Create a field with auto-sorting enabled and sorted items
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 9", "Item 2", "Item 3"));
        subsetField.setAutoSortingEnabled(true);
        
        // Move some items to create a specific order
        subsetField.moveItemRight("Item 2");
        subsetField.moveItemRight("Item 9");
        
        // Now disable auto-sorting - this should NOT change the current order
        subsetField.setAutoSortingEnabled(false);
        
        // Verify the order remains unchanged
        List<String> availableItems = subsetField.getAvailableItems();
        assertEquals(2, availableItems.size());
        assertEquals("Item 1", availableItems.get(0));
        assertEquals("Item 3", availableItems.get(1));
        
        List<String> selectedItems = subsetField.getSelectedItems();
        assertEquals(2, selectedItems.size());
        assertEquals("Item 2", selectedItems.get(0));
        assertEquals("Item 9", selectedItems.get(1));
    }

    private static class TestValidator implements FieldValidator<ListSubsetField<String>> {

        public static final String MSG = "selected items must not be empty";

        @Override
        public ValidationResult validate(ListSubsetField<String> fieldToValidate) {
            return fieldToValidate.getSelectedItems().isEmpty()
                    ? ValidationResult.invalid(MSG)
                    : ValidationResult.valid();
        }
    }
}