package ca.corbett.extras.properties;

import ca.corbett.forms.fields.LabelField;
import org.junit.jupiter.api.Test;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LabelPropertyTest extends AbstractPropertyBaseTests {

    @Override
    protected AbstractProperty createTestObject(String name, String label) {
        return new LabelProperty(name, label);
    }

    @Test
    public void testHyperlink() {
        // GIVEN a label prop with a hyperlink:
        LabelProperty label = (LabelProperty)createTestObject("test", "test");
        AbstractAction action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        };
        label.setHyperlink(action);

        // WHEN we generate a form field from it:
        LabelField formField = (LabelField)label.generateFormField();

        // THEN we should see it has a hyperlink set:
        assertTrue(formField.isHyperlinked());
    }

    @Test
    public void setFieldLabelText_withNoTextGiven_shouldGenerateLabelWithoutFieldLabel() {
        // GIVEN a fully-default label property with no explicit field label text:
        LabelProperty labelProperty = (LabelProperty)createTestObject("testLabel", "Test Label");

        // WHEN we generate a FormField from it:
        LabelField labelField = (LabelField)labelProperty.generateFormField();

        // THEN it should have no field label:
        assertFalse(labelField.hasFieldLabel());
        assertEquals("", labelField.getFieldLabel().getText()); // Just to be sure
    }

    @Test
    public void setFieldLabelText_withTextGiven_shouldGenerateLabelWithFieldLabel() {
        // GIVEN a label property with explicit field label text:
        LabelProperty labelProperty = (LabelProperty)createTestObject("testLabel", "Test Label");
        labelProperty.setFieldLabelText("My Field Label:");

        // WHEN we generate a FormField from it:
        LabelField labelField = (LabelField)labelProperty.generateFormField();

        // THEN it should have the expected field label:
        assertTrue(labelField.hasFieldLabel());
        assertEquals("My Field Label:", labelField.getFieldLabel().getText());
    }

    @Test
    public void setFieldLabelText_withTextGivenAndFormFieldGenerationListener_shouldAllowOverride() {
        // GIVEN a label property with explicit field label text:
        LabelProperty labelProperty = (LabelProperty)createTestObject("testLabel", "Test Label");
        labelProperty.setFieldLabelText("My Field Label:");

        // And GIVEN a form field generation listener that overrides the field label:
        labelProperty.addFormFieldGenerationListener(
                (p, f) -> f.getFieldLabel().setText("Overridden Field Label:"));

        // WHEN we generate a FormField from it:
        LabelField labelField = (LabelField)labelProperty.generateFormField();

        // THEN we should see that our input value was overridden by the FormFieldGenerationListener:
        assertTrue(labelField.hasFieldLabel());
        assertEquals("Overridden Field Label:", labelField.getFieldLabel().getText());
    }
}
