package ca.corbett.extras.properties;

import ca.corbett.forms.fields.LabelField;
import org.junit.jupiter.api.Test;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

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


}