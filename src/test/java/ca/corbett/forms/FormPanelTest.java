package ca.corbett.forms;

import ca.corbett.forms.fields.NumberField;
import ca.corbett.forms.fields.TextField;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class FormPanelTest {

    @Test
    public void testGetFormField_failureScenarios() {
        FormPanel formPanel = new FormPanel();
        assertNull(formPanel.getFormField("hello"));

        TextField textField = new TextField("Text:", 12, 1, true);
        textField.setIdentifier("textField1");
        formPanel.addFormField(textField);

        assertNull(formPanel.getFormField("TEXTFIELD1"));
        assertNull(formPanel.getFormField("textField11"));
        assertNull(formPanel.getFormField("textField"));
    }

    @Test
    public void testGetFormField_successScenarios() {
        NumberField numberField = new NumberField("Number:", 1, 0, 2, 1);
        numberField.setIdentifier("numberField1");
        FormPanel formPanel = new FormPanel(List.of(numberField));
        assertNotNull(formPanel.getFormField("numberField1"));

        TextField textField = new TextField("Text:", 12, 1, true);
        textField.setIdentifier("textField1");
        formPanel.addFormField(textField);
        assertNotNull(formPanel.getFormField("textField1"));

        formPanel.removeAllFormFields();
        assertNull(formPanel.getFormField("numberField1"));
        assertNull(formPanel.getFormField("textField1"));
    }

}