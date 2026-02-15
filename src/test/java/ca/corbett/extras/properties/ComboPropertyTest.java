package ca.corbett.extras.properties;

import ca.corbett.forms.fields.ComboField;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

class ComboPropertyTest extends AbstractPropertyBaseTests {

    @Override
    protected AbstractProperty createTestObject(String name, String label) {
        return new ComboProperty<>(name, label, List.of("ONE", "TWO", "THREE"), 0, false);
    }

    @Test
    public void testChangeListener() {
        // GIVEN a property with a mocked change listener:
        ComboProperty<String> testProp = new ComboProperty<>("test", "test", List.of("ONE", "TWO", "THREE"), 0, false);
        PropertyFormFieldChangeListener listener = Mockito.mock(PropertyFormFieldChangeListener.class);
        testProp.addFormFieldChangeListener(listener);

        // WHEN we generate a form field and mess with it:
        ComboField<?> formField = (ComboField<?>)testProp.generateFormField();
        formField.setSelectedIndex(1);
        formField.setSelectedIndex(2);

        // THEN we should see our change listener get invoked:
        Mockito.verify(listener, Mockito.times(2)).valueChanged(Mockito.any());
    }
}
