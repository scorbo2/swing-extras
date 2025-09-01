package ca.corbett.extras.properties;

import ca.corbett.extras.CoalescingDocumentListener;
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
    public void testChangeListener() throws Exception {
        // GIVEN a property with a mocked change listener:
        DirectoryProperty testProp = new DirectoryProperty("test", "test", false);
        PropertyFormFieldChangeListener listener = Mockito.mock(PropertyFormFieldChangeListener.class);
        testProp.addFormFieldChangeListener(listener);

        // WHEN we generate a form field and mess with it:
        FileField formField = (FileField)testProp.generateFormField();
        formField.setFile(new File("blah"));
        Thread.sleep(CoalescingDocumentListener.DELAY_MS*2); // cheesy!

        // THEN we should see our change listener get invoked:
        Mockito.verify(listener, Mockito.times(1)).valueChanged(Mockito.any());
    }
}