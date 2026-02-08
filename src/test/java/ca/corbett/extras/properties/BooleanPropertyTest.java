package ca.corbett.extras.properties;

import ca.corbett.forms.fields.CheckBoxField;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class BooleanPropertyTest extends AbstractPropertyBaseTests {

    @Override
    protected AbstractProperty createTestObject(String name, String label) {
        return new BooleanProperty(name, label, true);
    }

    @Test
    public void testChangeListeners() {
        // GIVEN a property with a mocked change listener on it:
        PropertyFormFieldChangeListener listener = Mockito.mock(PropertyFormFieldChangeListener.class);
        BooleanProperty testProp = (BooleanProperty)createTestObject("test", "test");
        testProp.addFormFieldChangeListener(listener);

        // WHEN we generate a form field and mess with it:
        CheckBoxField formField = (CheckBoxField)testProp.generateFormField();
        formField.setChecked(false);

        // THEN we should receive change events on it:
        Mockito.verify(listener, Mockito.times(1)).valueChanged(Mockito.any());
    }
}
