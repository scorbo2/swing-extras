package ca.corbett.forms.fields;

import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.ValidationResult;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
    public void selectItem_withNonExistingItem_shouldDoNothing() {
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3"));
        subsetField.selectItem("Item 4"); // Item 4 does not exist
        assertEquals(3, subsetField.getAvailableItems().size());
        assertEquals(0, subsetField.getSelectedItems().size());
    }

    @Test
    public void unselectItem_withNonExistingItem_shouldDoNothing() {
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3"),
                java.util.List.of("Item 4")); // Item 4 is selected
        subsetField.unselectItem("Item 5"); // Item 5 does not exist
        assertEquals(3, subsetField.getAvailableItems().size());
        assertEquals(1, subsetField.getSelectedItems().size());
    }

    @Test
    public void selectItem_and_unselectItem_shouldUpdateListsCorrectly() {
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3"));

        subsetField.selectItem("Item 2");
        assertEquals(2, subsetField.getAvailableItems().size());
        assertEquals(1, subsetField.getSelectedItems().size());
        assertEquals("Item 1", subsetField.getAvailableItems().get(0));
        assertEquals("Item 3", subsetField.getAvailableItems().get(1));
        assertEquals("Item 2", subsetField.getSelectedItems().get(0));

        subsetField.unselectItem("Item 2");
        assertEquals(3, subsetField.getAvailableItems().size());
        assertEquals(0, subsetField.getSelectedItems().size());
        assertEquals("Item 1", subsetField.getAvailableItems().get(0));
        assertEquals("Item 3", subsetField.getAvailableItems().get(1));
        // When auto-sorting is disabled, moved items are appended to the end of the list
        assertEquals("Item 2", subsetField.getAvailableItems().get(2));
    }

    @Test
    public void selectAllItems_and_unselectAllItems_shouldUpdateListsCorrectly() {
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3"));

        subsetField.selectAllItems();
        assertEquals(0, subsetField.getAvailableItems().size());
        assertEquals(3, subsetField.getSelectedItems().size());

        subsetField.unselectAllItems();
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
    public void selectItems_withValidItems_shouldSelectCorrectItems() {
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3", "Item 4"));

        subsetField.selectItems(List.of("Item 2", "Item 4"));

        List<String> selectedItems = subsetField.getSelectedItems();
        assertEquals(2, selectedItems.size());
        assertEquals("Item 2", selectedItems.get(0));
        assertEquals("Item 4", selectedItems.get(1));
    }

    @Test
    public void selectItems_withSomeItemsAlreadySelected_shouldUpdateSelectionCorrectly() {
        // GIVEN a list with some available items:
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3", "Item 4"));

        // WHEN we move one item to the right...
        subsetField.selectItem("Item 1");

        // and WHEN we invoke selectItems(), the previous selection should be updated and NOT replaced:
        subsetField.selectItems(List.of("Item 3", "Item 4")); // adds to existing selection!

        // THEN all items that have been selected should be in the right list:
        List<String> selectedItems = subsetField.getSelectedItems();
        assertEquals(3, selectedItems.size());
        assertEquals("Item 1", selectedItems.get(0));
        assertEquals("Item 3", selectedItems.get(1));
        assertEquals("Item 4", selectedItems.get(2));
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
    public void selectItems_withInvalidItems_shouldIgnoreInvalidItems() {
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3", "Item 4"));

        // WHEN we select items, including some that don't exist:
        subsetField.selectItems(List.of("Hello", "There", "Item 1", "Item 3")); // Only two of these exist

        // THEN only the valid items should be selected:
        List<String> selectedItems = subsetField.getSelectedItems();
        assertEquals(2, selectedItems.size());
        assertEquals("Item 1", selectedItems.get(0));
        assertEquals("Item 3", selectedItems.get(1));
    }

    @Test
    public void getSelectedItems_withNothingSelected_shouldReturnEmptyList() {
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3"));

        List<String> selectedItems = subsetField.getSelectedItems();
        assertEquals(0, selectedItems.size());
    }

    @Test
    public void getSelectedItems_withSelection_shouldReturnSelectedItems() {
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3", "Item 4"));

        subsetField.selectItem("Item 2");
        subsetField.selectItem("Item 4");

        List<String> selectedItems = subsetField.getSelectedItems();
        assertEquals(2, selectedItems.size());
        assertEquals("Item 2", selectedItems.get(0));
        assertEquals("Item 4", selectedItems.get(1));
    }

    @Test
    public void validate_validScenario() {
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3"));
        subsetField.addFieldValidator(new TestValidator());
        subsetField.selectItem("Item 2"); // Select an item
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
        subsetField.selectItem("Item 9");
        subsetField.selectItem("Item 2");
        
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
        subsetField.selectItem("Item 9");
        subsetField.selectItem("Item 2");
        
        List<String> selectedItems = subsetField.getSelectedItems();
        assertEquals(2, selectedItems.size());
        assertEquals("Item 9", selectedItems.get(0));
        assertEquals("Item 2", selectedItems.get(1));
    }

    @Test
    public void selectItems_withAutoSortingDisabled_shouldMaintainInputOrder() {
        // GIVEN an input list with auto-sorting disabled and items in some arbitrary order:
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("One", "Two", "Three", "Four", "Five", "Six"));

        // WHEN we move specific items to the right (select them) in a specific order:
        subsetField.selectItem("Three");
        subsetField.selectItem("Two");

        // THEN we should see that order maintained in both lists:
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
    public void selectItem_withAutoSortingDisabled_shouldPreserveInsertionOrder() {
        // This demonstrates the preferred way to select specific items when auto-sorting is disabled
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("One", "Two", "Three", "Four", "Five", "Six"));
        
        // Move specific items to the right (select them)
        subsetField.selectItem("One");
        subsetField.selectItem("Two");
        
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
        subsetField.selectItem("Mango");
        subsetField.selectItem("Apple");
        
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
        subsetField.selectItem("Item 2");
        subsetField.selectItem("Item 9");
        
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

    @Test
    public void selectItem_withValueChangedListener_shouldNotify() {
        // GIVEN a ListSubsetField with a ValueChangedListener:
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3"));
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        subsetField.addValueChangedListener(listener);

        // WHEN we select an item programmatically:
        subsetField.selectItem("Item 2");

        // THEN our listener should be notified once:
        Mockito.verify(listener, Mockito.times(1)).formFieldValueChanged(subsetField);
    }

    @Test
    public void unselectItem_withValueChangedListener_shouldNotify() {
        // GIVEN a ListSubsetField with a ValueChangedListener and an item already selected:
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3"),
                java.util.List.of("Item 2"));
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        subsetField.addValueChangedListener(listener);

        // WHEN we unselect the item:
        subsetField.unselectItem("Item 2");

        // THEN our listener should be notified once:
        Mockito.verify(listener, Mockito.times(1)).formFieldValueChanged(subsetField);
    }

    @Test
    public void selectItems_withValueChangedListener_shouldNotify() {
        // GIVEN a ListSubsetField with a ValueChangedListener:
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3", "Item 4"));
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        subsetField.addValueChangedListener(listener);

        // WHEN we select multiple items at once:
        subsetField.selectItems(List.of("Item 2", "Item 4"));

        // THEN our listener should be notified once (not twice):
        Mockito.verify(listener, Mockito.times(1)).formFieldValueChanged(subsetField);
    }

    @Test
    public void selectAllItems_withValueChangedListener_shouldNotify() {
        // GIVEN a ListSubsetField with a ValueChangedListener:
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3"));
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        subsetField.addValueChangedListener(listener);

        // WHEN we select all items:
        subsetField.selectAllItems();

        // THEN our listener should be notified once:
        Mockito.verify(listener, Mockito.times(1)).formFieldValueChanged(subsetField);
    }

    @Test
    public void unselectAllItems_withValueChangedListener_shouldNotify() {
        // GIVEN a ListSubsetField with a ValueChangedListener and some items selected:
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3"),
                java.util.List.of("Item 2", "Item 3"));
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        subsetField.addValueChangedListener(listener);

        // WHEN we unselect all items:
        subsetField.unselectAllItems();

        // THEN our listener should be notified once:
        Mockito.verify(listener, Mockito.times(1)).formFieldValueChanged(subsetField);
    }

    @Test
    public void selectItem_withNonExistingItem_shouldNotNotify() {
        // GIVEN a ListSubsetField with a ValueChangedListener:
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3"));
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        subsetField.addValueChangedListener(listener);

        // WHEN we try to select an item that doesn't exist:
        subsetField.selectItem("Item 99");

        // THEN our listener should NOT be notified because nothing changed:
        Mockito.verify(listener, Mockito.times(0)).formFieldValueChanged(subsetField);
    }

    @Test
    public void selectItems_withNoValidItems_shouldNotNotify() {
        // GIVEN a ListSubsetField with a ValueChangedListener:
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3"));
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        subsetField.addValueChangedListener(listener);

        // WHEN we try to select items that don't exist:
        subsetField.selectItems(List.of("Item 99", "Item 100"));

        // THEN our listener should NOT be notified because nothing changed:
        Mockito.verify(listener, Mockito.times(0)).formFieldValueChanged(subsetField);
    }

    @Test
    public void multipleSelections_withValueChangedListener_shouldNotifyMultipleTimes() {
        // GIVEN a ListSubsetField with a ValueChangedListener:
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3", "Item 4"));
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        subsetField.addValueChangedListener(listener);

        // WHEN we make multiple selection changes:
        subsetField.selectItem("Item 1");
        subsetField.selectItem("Item 2");
        subsetField.unselectItem("Item 1");
        subsetField.selectAllItems();
        subsetField.unselectAllItems();

        // THEN our listener should be notified 5 times (once per change):
        Mockito.verify(listener, Mockito.times(5)).formFieldValueChanged(subsetField);
    }

    @Test
    public void removeValueChangedListener_shouldStopNotifications() {
        // GIVEN a ListSubsetField with a ValueChangedListener that is later removed:
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3"));
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        subsetField.addValueChangedListener(listener);
        subsetField.removeValueChangedListener(listener);

        // WHEN we make changes:
        subsetField.selectItem("Item 1");
        subsetField.selectAllItems();

        // THEN our listener should NOT be notified:
        Mockito.verify(listener, Mockito.times(0)).formFieldValueChanged(subsetField);
    }

    @Test
    public void selectAllItems_withNoItemsToMove_shouldNotNotify() {
        // GIVEN a ListSubsetField with a ValueChangedListener and all items already selected:
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3"),
                java.util.List.of("Item 1", "Item 2", "Item 3"));
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        subsetField.addValueChangedListener(listener);

        // WHEN we try to select all items (but there are none to move):
        subsetField.selectAllItems();

        // THEN our listener should NOT be notified:
        Mockito.verify(listener, Mockito.times(0)).formFieldValueChanged(subsetField);
    }

    @Test
    public void unselectAllItems_withNoItemsToMove_shouldNotNotify() {
        // GIVEN a ListSubsetField with a ValueChangedListener and no items selected:
        ListSubsetField<String> subsetField = new ListSubsetField<>("Test Subset Field",
                java.util.List.of("Item 1", "Item 2", "Item 3"));
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        subsetField.addValueChangedListener(listener);

        // WHEN we try to unselect all items (but there are none to move):
        subsetField.unselectAllItems();

        // THEN our listener should NOT be notified:
        Mockito.verify(listener, Mockito.times(0)).formFieldValueChanged(subsetField);
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
