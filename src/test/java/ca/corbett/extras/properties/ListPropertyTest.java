package ca.corbett.extras.properties;

import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.ListField;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @Test
    public void formFieldGenerationListener_withButtonProperties_shouldSetButtonProperties() {
        // GIVEN a test prop with some optional button properties set:
        ListProperty<String> testProp = new ListProperty<>("test", "test");
        testProp.setButtonPosition(ListField.ButtonPosition.TOP);
        testProp.setButtonAlignment(FlowLayout.RIGHT);
        testProp.setButtonHgap(7);
        testProp.setButtonVgap(9);
        testProp.setButtonPreferredDimensions(200, 150);

        // WHEN we generate a form field:
        FormField formField = testProp.generateFormField();

        // THEN we should see our button properties were applied to the form field:
        assertInstanceOf(ListField.class, formField);
        ListField<?> listField = (ListField<?>)formField;
        assertEquals(ListField.ButtonPosition.TOP, listField.getButtonPosition());
        assertEquals(FlowLayout.RIGHT, listField.getButtonAlignment());
        assertEquals(7, listField.getButtonHgap());
        assertEquals(9, listField.getButtonVgap());
        Dimension preferredSize = listField.getButtonPreferredSize();
        assertNotNull(preferredSize);
        assertEquals(200, preferredSize.width);
        assertEquals(150, preferredSize.height);
    }

    @Test
    public void formFieldGenerationListener_withConvenienceMethodForButtonLayout_shouldSetButtonProperties() {
        // GIVEN a test prop with some optional button properties set via the convenience method:
        ListProperty<String> testProp = new ListProperty<>("test", "test");
        testProp.setButtonLayout(FlowLayout.CENTER, 5, 6);

        // WHEN we generate a form field:
        FormField formField = testProp.generateFormField();

        // THEN we should see our button properties were applied to the form field:
        assertInstanceOf(ListField.class, formField);
        ListField<?> listField = (ListField<?>)formField;
        assertEquals(FlowLayout.CENTER, listField.getButtonAlignment());
        assertEquals(5, listField.getButtonHgap());
        assertEquals(6, listField.getButtonVgap());
    }
}
