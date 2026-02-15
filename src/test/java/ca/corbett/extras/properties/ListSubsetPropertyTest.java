package ca.corbett.extras.properties;

import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.ListSubsetField;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListSubsetPropertyTest extends AbstractPropertyBaseTests {

    @Override
    protected AbstractProperty createTestObject(String fullyQualifiedName, String label) {
        return new ListSubsetProperty<String>(fullyQualifiedName, label);
    }

    @Test
    public void constructor_noListProvided_shouldBeEmpty() {
        ListSubsetProperty<?> listSubsetProperty = (ListSubsetProperty<?>) actual;

        // Verify that list of items is empty:
        assertTrue(listSubsetProperty.getAllItems().isEmpty());

        // And nothing should be selected, because there's nothing to select:
        assertTrue(listSubsetProperty.getSelectedItems().isEmpty());
    }

    @Test
    public void constructor_withAvailableAndSelectedLists_shouldSetLists() {
        ListSubsetProperty<String> listSubsetProperty = new ListSubsetProperty<>(
                "Category.Subcategory.ListProp",
                "Select Items",
                java.util.List.of("Item1", "Item2", "Item3"),
                new int[]{1}
        );

        // Verify that availableItems and selectedItems are set correctly
        assertEquals(3, listSubsetProperty.getAllItems().size());
        assertEquals(1, listSubsetProperty.getSelectedItems().size());
        assertEquals("Item2", listSubsetProperty.getSelectedItems().get(0));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void generateFormField_shouldReturnListSubsetField() {
        // GIVEN a ListSubsetProperty with some values:
        ListSubsetProperty<String> listSubsetProperty = (ListSubsetProperty<String>) actual;
        listSubsetProperty.setVisibleRowCount(7);
        listSubsetProperty.setFixedCellWidth(150);
        listSubsetProperty.setAllItems(List.of("A", "B", "C", "D", "E", "F"));
        listSubsetProperty.setSelectedIndexes(new int[]{1, 3}); // Select "B" and "D"

        // WHEN we generate a form field from it:
        FormField formField = listSubsetProperty.generateFormField();

        // THEN we should get a ListSubsetField with the correct configuration:
        assertInstanceOf(ListSubsetField.class, formField);
        ListSubsetField<?> listSubsetField = (ListSubsetField<?>)formField;
        Assertions.assertEquals(7, listSubsetField.getVisibleRowCount());
        Assertions.assertEquals(150, listSubsetField.getFixedCellWidth());
        Assertions.assertEquals(4, listSubsetField.getAvailableItems().size());
        Assertions.assertEquals(2, listSubsetField.getSelectedItems().size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void loadFromFormField_givenModifiedFormField_shouldUpdate() {
        // GIVEN a ListSubsetProperty and its corresponding FormField:
        ListSubsetProperty<String> listSubsetProperty = (ListSubsetProperty<String>) actual;
        listSubsetProperty.setAllItems(List.of("A", "B", "C", "D"));
        listSubsetProperty.setSelectedIndexes(new int[]{3}); // Select "D"
        listSubsetProperty.setVisibleRowCount(7);
        listSubsetProperty.setFixedCellWidth(155);
        FormField formField = listSubsetProperty.generateFormField();
        ListSubsetField<String> listSubsetField = (ListSubsetField<String>) formField;

        // WHEN we modify the FormField's selected items and load back into the property:
        listSubsetField.selectItems(List.of("B", "C")); // Select "B" and "C", adds to previous selection
        listSubsetField.setVisibleRowCount(5);
        listSubsetField.setFixedCellWidth(120);
        listSubsetProperty.loadFromFormField(listSubsetField);

        // THEN the property should reflect the updated selected items:
        Assertions.assertEquals(3, listSubsetProperty.getSelectedItems().size());
        Assertions.assertTrue(listSubsetProperty.getSelectedItems().contains("B"));
        Assertions.assertTrue(listSubsetProperty.getSelectedItems().contains("C"));
        Assertions.assertTrue(listSubsetProperty.getSelectedItems().contains("D"));
        Assertions.assertEquals(5, listSubsetProperty.getVisibleRowCount());
        Assertions.assertEquals(120, listSubsetProperty.getFixedCellWidth());
    }

    @Test
    public void getSelectedIndexesAsString_givenEmptyArray_shouldReturnEmptyString() {
        String result = ListSubsetProperty.getSelectedIndexesAsString(new int[0]);
        assertEquals("", result);
    }

    @Test
    public void getSelectedIndexesAsString_givenMultipleIndexes_shouldReturnCommaSeparatedString() {
        String result = ListSubsetProperty.getSelectedIndexesAsString(new int[]{0, 2, 4});
        assertEquals("0,2,4", result);
    }
}
