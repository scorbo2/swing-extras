package ca.corbett.extras.properties;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ListPropertyTest {

    @Test
    public void testGetSelectedItems_withSelection_shouldReturnSelection() {
        //GIVEN a list property with some selected items:
        ListProperty<String> prop = new ListProperty<String>("test1", "hello")
                .setItems(List.of("One", "Two", "Three", "Four", "Five"))
                .setSelectedItems(List.of("Two", "Five"));

        //WHEN we query for the selected items:
        List<String> actual = prop.getSelectedItems();

        //THEN we should see the expected selection:
        assertTrue(actual.containsAll(List.of("Two", "Five")));
    }

}