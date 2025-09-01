package ca.corbett.extras.properties;

import ca.corbett.forms.fields.NumberField;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DecimalPropertyTest extends AbstractPropertyBaseTests {

    @Override
    protected AbstractProperty createTestObject(String name, String label) {
        return new DecimalProperty(name, label, 1.1);
    }

    @Test
    public void testChangeListener() {
        // GIVEN a property with a mocked change listener:
        DecimalProperty testProp = new DecimalProperty("test", "test", 99.99);
        PropertyFormFieldChangeListener listener = Mockito.mock(PropertyFormFieldChangeListener.class);
        testProp.addFormFieldChangeListener(listener);

        // WHEN we generate a form field and mess with it:
        NumberField formField = (NumberField)testProp.generateFormField();
        formField.setCurrentValue(33.33);
        formField.setCurrentValue(66.66);

        // THEN we should see our change listener get invoked:
        Mockito.verify(listener, Mockito.times(2)).valueChanged(Mockito.any());
    }
}