package ca.corbett.extras.properties;

import ca.corbett.forms.fields.ShortTextField;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ShortTextPropertyTest extends AbstractPropertyBaseTests {

    @Override
    protected AbstractProperty createTestObject(String name, String label) {
        return new ShortTextProperty(name, label, "test");
    }

    @Test
    public void formFieldChangeListener_withFormFieldChanges_shouldFireChangeEvents() throws Exception {
        // GIVEN a test prop with a mocked property form field change listener on it:
        ShortTextProperty testProp = new ShortTextProperty("test", "test", "test");
        PropertyFormFieldChangeListener listener = Mockito.mock(PropertyFormFieldChangeListener.class);
        testProp.addFormFieldChangeListener(listener);

        // WHEN we generate a form field and mess with it a bit:
        ShortTextField formField = (ShortTextField)testProp.generateFormField();
        formField.setText("hello");
        formField.setText("goodbye");

        // THEN we should see our change listener got invoked twice per set, because
        //      DocumentListener is broken, and we don't have a good workaround for it yet.
        Mockito.verify(listener, Mockito.times(4)).valueChanged(Mockito.any());
    }
}
