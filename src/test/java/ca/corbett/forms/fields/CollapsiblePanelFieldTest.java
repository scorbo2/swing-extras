package ca.corbett.forms.fields;

import javax.swing.JLabel;
import java.awt.FlowLayout;

class CollapsiblePanelFieldTest extends FormFieldBaseTests {

    @Override
    protected FormField createTestObject() {
        CollapsiblePanelField pf = new CollapsiblePanelField("Test", true, new FlowLayout(FlowLayout.LEFT));
        pf.getPanel().add(new JLabel("Panel contents"));
        return pf;
    }
}