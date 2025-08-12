package ca.corbett.extras.properties;

import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.TextField;
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
        PropertiesDialog dialog = propsManager.generateDialog(new JFrame(), "Test");

        // WHEN we search for specific fields:
        LabelField labelField = (LabelField)dialog.findFormField("test.test.label2");
        TextField textField = (TextField)dialog.findFormField("test.test.text1");
        FormField formField = dialog.findFormField("this.field.does.not.exist");

        // THEN we should see expected results:
        assertNotNull(labelField);
        assertNotNull(textField);
        assertNull(formField);
    }

    @Test
    public void getFormPanelAt_withSingleFormPanel_shouldSucceed() {
        // GIVEN a properties dialog with only one form panel on it:
        PropertiesManager propsManager = new PropertiesManager(new Properties(), createTestProps(), "Test");
        PropertiesDialog dialog = propsManager.generateDialog(new JFrame(), "Test");

        // WHEN we search for specific form panels by index:
        FormPanel formPanel1 = dialog.getFormPanelAt(0);
        FormPanel formPanelNull1 = dialog.getFormPanelAt(99);
        FormPanel formPanelNull2 = dialog.getFormPanelAt(1);

        // THEN we should see expected results:
        assertNotNull(formPanel1);
        assertNull(formPanelNull1);
        assertNull(formPanelNull2);
    }

    @Test
    public void getFormPanelAt_withMultiFormPanels_shouldSucceed() {
        // GIVEN a properties dialog with multiple form panels on it:
        PropertiesManager propsManager = new PropertiesManager(new Properties(), createMultiTabTestProps(), "Test");
        PropertiesDialog dialog = propsManager.generateDialog(new JFrame(), "Test");

        // WHEN we search for specific form panels by index:
        FormPanel formPanel1 = dialog.getFormPanelAt(0);
        FormPanel formPanel2 = dialog.getFormPanelAt(1);
        FormPanel formPanelNull1 = dialog.getFormPanelAt(99);
        FormPanel formPanelNull2 = dialog.getFormPanelAt(2);

        // THEN we should see expected results:
        assertNotNull(formPanel1);
        assertNotNull(formPanel2);
        assertNull(formPanelNull1);
        assertNull(formPanelNull2);

    }

    private List<AbstractProperty> createTestProps() {
        List<AbstractProperty> props = new ArrayList<>();

        props.add(LabelProperty.createHeaderLabel("test.test.label1", "HELLO"));
        props.add(LabelProperty.createLabel("test.test.label2", "This is only a test."));
        props.add(TextProperty.ofSingleLine("test.test.text1", "Text:", 12));

        return props;
    }

    private List<AbstractProperty> createMultiTabTestProps() {
        List<AbstractProperty> props = createTestProps();

        props.add(LabelProperty.createLabel("test2.test2.label1", "This is a field on tab 2"));

        return props;
    }
}