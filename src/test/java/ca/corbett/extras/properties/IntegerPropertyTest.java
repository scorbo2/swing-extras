package ca.corbett.extras.properties;

import ca.corbett.forms.fields.NumberField;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class IntegerPropertyTest extends AbstractPropertyBaseTests {

    @Override
    protected AbstractProperty createTestObject(String name, String label) {
        return new IntegerProperty(name, label);
    }

    @Test
    public void formFieldChangeListener_withFormFieldChanges_shouldFireChangeEvents() {
        // GIVEN a test prop with a mocked property form field change listener on it:
        IntegerProperty testProp = new IntegerProperty("test", "test", 3);
        PropertyFormFieldChangeListener listener = Mockito.mock(PropertyFormFieldChangeListener.class);
        testProp.addFormFieldChangeListener(listener);

        // WHEN we generate a form field and mess with it a bit:
        NumberField formField = (NumberField)testProp.generateFormField();
        formField.setCurrentValue(4);
        formField.setCurrentValue(5);

        // THEN we should see our change listener got invoked:
        Mockito.verify(listener, Mockito.times(2)).valueChanged(Mockito.any());
    }
}