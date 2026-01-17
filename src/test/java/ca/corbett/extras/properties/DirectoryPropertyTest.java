package ca.corbett.extras.properties;

import ca.corbett.forms.fields.FileField;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;

class DirectoryPropertyTest extends AbstractPropertyBaseTests {

    @Override
    protected AbstractProperty createTestObject(String name, String label) {
        return new DirectoryProperty(name, label, false);
    }

    @Test
    public void testChangeListener_withNoValueSet_shouldNotifyListenerOnce() throws Exception {
        // GIVEN a property with a mocked change listener and no file set:
        DirectoryProperty testProp = new DirectoryProperty("test", "test", false);
        PropertyFormFieldChangeListener listener = Mockito.mock(PropertyFormFieldChangeListener.class);
        testProp.addFormFieldChangeListener(listener);

        // WHEN we generate a form field and mess with it:
        FileField formField = (FileField)testProp.generateFormField();
        formField.setFile(new File("blah"));

        // THEN we should see our change listener get invoked:
        Mockito.verify(listener, Mockito.times(1)).valueChanged(Mockito.any());
    }

    @Test
    public void testChangeListener_withValueSet_shouldNotifyListenerTwiceBecauseDocumentListenerIsBroken()
            throws Exception {
        // GIVEN a property with a mocked change listener and a value set:
        DirectoryProperty testProp = new DirectoryProperty("test", "test", false);
        testProp.setDirectory(new File("hello"));
        PropertyFormFieldChangeListener listener = Mockito.mock(PropertyFormFieldChangeListener.class);
        testProp.addFormFieldChangeListener(listener);

        // WHEN we generate a form field and mess with it:
        FileField formField = (FileField)testProp.generateFormField();
        formField.setFile(new File("blah"));

        // THEN we should see our change listener get invoked, not once but twice, because
        //      DocumentListener is broken, and we don't have a good workaround for it yet.
        Mockito.verify(listener, Mockito.times(2)).valueChanged(Mockito.any());
    }

}
