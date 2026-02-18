package ca.corbett.extras.properties;

import ca.corbett.extras.properties.dialog.PropertiesDialog;
import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.ShortTextField;
import org.junit.jupiter.api.Test;

import javax.swing.JFrame;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class PropertiesDialogTest {

    @Test
    public void findFormField_withValidFormField_shouldFind() {
        // GIVEN a properties dialog with a bunch of fields on it:
        PropertiesManager propsManager = new PropertiesManager(new Properties(), createTestProps(), "Test");
        PropertiesDialog dialog = propsManager.generateClassicDialog(new JFrame(), "Test", true);

        // WHEN we search for specific fields:
        LabelField labelField = (LabelField)dialog.findFormField("test.test.label2");
        ShortTextField textField = (ShortTextField)dialog.findFormField("test.test.text1");
        FormField formField = dialog.findFormField("this.field.does.not.exist");

        // THEN we should see expected results:
        assertNotNull(labelField);
        assertNotNull(textField);
        assertNull(formField);
    }

    private List<AbstractProperty> createTestProps() {
        List<AbstractProperty> props = new ArrayList<>();

        props.add(LabelProperty.createHeaderLabel("test.test.label1", "HELLO"));
        props.add(LabelProperty.createLabel("test.test.label2", "This is only a test."));
        props.add(new ShortTextProperty("test.test.text1", "Text:", "", 12));

        return props;
    }

    private List<AbstractProperty> createMultiTabTestProps() {
        List<AbstractProperty> props = createTestProps();

        props.add(LabelProperty.createLabel("test2.test2.label1", "This is a field on tab 2"));

        return props;
    }
}
