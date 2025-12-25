package ca.corbett.extras.properties;

import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.ListSubsetField;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ListSubsetPropertyTest extends AbstractPropertyBaseTests {

    @Override
    protected AbstractProperty createTestObject(String fullyQualifiedName, String label) {
        return new ListSubsetProperty<>(fullyQualifiedName, label);
    }

    @Test
    public void constructor_noListsProvided_shouldBeEmpty() {
        ListSubsetProperty<?> listSubsetProperty = (ListSubsetProperty<?>) actual;
        assert listSubsetProperty != null;
        // Verify that availableItems and selectedItems are empty
        assert listSubsetProperty.getAvailableItems().isEmpty();
        assert listSubsetProperty.getSelectedItems().isEmpty();
    }

    @Test
    public void constructor_withAvailableAndSelectedLists_shouldSetLists() {
        ListSubsetProperty<String> listSubsetProperty = new ListSubsetProperty<>(
                "Category.Subcategory.ListProp",
                "Select Items",
                java.util.List.of("Item1", "Item3"),
                java.util.List.of("Item2")
        );
        assert listSubsetProperty != null;
        // Verify that availableItems and selectedItems are set correctly
        assert listSubsetProperty.getAvailableItems().size() == 2;
        assert listSubsetProperty.getSelectedItems().size() == 1;
        assert listSubsetProperty.getSelectedItems().get(0).equals("Item2");
    }

    @Test
    public void generateFormField_shouldReturnListSubsetField() {
        // GIVEN a ListSubsetProperty with some values:
        ListSubsetProperty<String> listSubsetProperty = (ListSubsetProperty<String>) actual;
        listSubsetProperty.setVisibleRowCount(7);
        listSubsetProperty.setFixedCellWidth(150);
        listSubsetProperty.setAvailableItems(List.of("A", "B", "C", "D"));
        listSubsetProperty.setSelectedItems(List.of("E", "F"));

        // WHEN we generate a form field from it:
        FormField formField = listSubsetProperty.generateFormField();

        // THEN we should get a ListSubsetField with the correct configuration:
        assertInstanceOf(ListSubsetField.class, formField);
        ListSubsetField<String> listSubsetField = (ListSubsetField<String>) formField;
        Assertions.assertEquals(7, listSubsetField.getVisibleRowCount());
        Assertions.assertEquals(150, listSubsetField.getFixedCellWidth());
        Assertions.assertEquals(4, listSubsetField.getAvailableItems().size());
        Assertions.assertEquals(2, listSubsetField.getSelectedItems().size());
    }

    @Test
    public void loadFromFormField_givenModifiedFormField_shouldUpdate() {
        // GIVEN a ListSubsetProperty and its corresponding FormField:
        ListSubsetProperty<String> listSubsetProperty = (ListSubsetProperty<String>) actual;
        listSubsetProperty.setAvailableItems(List.of("A", "B", "C"));
        listSubsetProperty.setSelectedItems(List.of("D"));
        listSubsetProperty.setVisibleRowCount(7);
        listSubsetProperty.setFixedCellWidth(155);
        FormField formField = listSubsetProperty.generateFormField();
        ListSubsetField<String> listSubsetField = (ListSubsetField<String>) formField;

        // WHEN we modify the FormField's selected items and load back into the property:
        listSubsetField.selectIndexes(new int[] {1, 2}); // Select "B" and "C"
        listSubsetField.setVisibleRowCount(5);
        listSubsetField.setFixedCellWidth(120);
        listSubsetProperty.loadFromFormField(listSubsetField);

        // THEN the property should reflect the updated selected items:
        Assertions.assertEquals(2, listSubsetProperty.getSelectedItems().size());
        Assertions.assertTrue(listSubsetProperty.getSelectedItems().contains("B"));
        Assertions.assertTrue(listSubsetProperty.getSelectedItems().contains("C"));
        Assertions.assertEquals(5, listSubsetProperty.getVisibleRowCount());
        Assertions.assertEquals(120, listSubsetProperty.getFixedCellWidth());
    }
}