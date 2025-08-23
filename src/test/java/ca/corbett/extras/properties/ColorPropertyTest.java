package ca.corbett.extras.properties;

import ca.corbett.extras.gradient.ColorSelectionType;
import ca.corbett.forms.fields.ColorField;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.awt.Color;

class ColorPropertyTest extends AbstractPropertyBaseTests {

    @Override
    protected AbstractProperty createTestObject(String name, String label) {
        return new ColorProperty(name, label, ColorSelectionType.SOLID);
    }

    @Test
    public void testChangeListener() {
        // GIVEN a property with a mocked change listener:
        ColorProperty testProp = (ColorProperty)createTestObject("test", "test");
        PropertyFormFieldChangeListener listener = Mockito.mock(PropertyFormFieldChangeListener.class);
        testProp.addFormFieldChangeListener(listener);

        // WHEN we generate a form field and mess with it:
        ColorField formField = (ColorField)testProp.generateFormField();
        formField.setColor(Color.BLUE);

        // THEN we should see our change listener get invoked:
        Mockito.verify(listener, Mockito.times(1)).valueChanged(Mockito.any());
    }
}