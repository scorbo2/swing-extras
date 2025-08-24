package ca.corbett.extras.properties;

import ca.corbett.forms.fields.ListField;
import ca.corbett.forms.fields.LongTextField;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

class LongTextPropertyTest extends AbstractPropertyBaseTests {

    @Override
    protected AbstractProperty createTestObject(String name, String label) {
        return new LongTextProperty(name, label, LongTextField.TextFieldType.MULTI_LINE_FIXED_ROWS_COLS, 20, 3);
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