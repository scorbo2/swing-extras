package ca.corbett.extras.properties;

import ca.corbett.forms.fields.ListField;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListPropertyTest extends AbstractPropertyBaseTests {

    @Override
    protected AbstractProperty createTestObject(String name, String label) {
        return new ListProperty<String>(name, label).setItems(List.of("One", "Two", "Three"));
    }

    @Test
    public void testGetSelectedItems_withSelection_shouldReturnSelection() {
        //GIVEN a list property with some selected items:
        ListProperty<String> prop = new ListProperty<String>("test1", "hello")
                .setItems(List.of("One", "Two", "Three", "Four", "Five"))
                .setSelectedItems(List.of("Two", "Five"));

        //WHEN we query for the selected items:
        List<String> actual = prop.getSelectedItems();

        //THEN we should see the expected selection:
        assertEquals(2, actual.size());
        assertTrue(actual.containsAll(List.of("Two", "Five")));
    }

    @Test
    public void formFieldChangeListener_withFormFieldChanges_shouldFireChangeEvents() {
        // GIVEN a test prop with a mocked property form field change listener on it:
        ListProperty<String> testProp = new ListProperty<>("test", "test");
        testProp.setItems(List.of("one", "two", "three"));
        testProp.setSelectedItems(List.of("one"));
        PropertyFormFieldChangeListener listener = Mockito.mock(PropertyFormFieldChangeListener.class);
        testProp.addFormFieldChangeListener(listener);

        // WHEN we generate a form field and mess with it a bit:
        //noinspection unchecked
        ListField<String> formField = (ListField<String>)testProp.generateFormField();
        formField.setSelectedIndex(1);
        formField.setSelectedIndex(2);

        // THEN we should see our change listener got invoked:
        Mockito.verify(listener, Mockito.times(2)).valueChanged(Mockito.any());
    }
}