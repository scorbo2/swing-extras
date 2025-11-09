package ca.corbett.forms.demo;

import ca.corbett.extras.demo.panels.PanelBuilder;
import ca.corbett.forms.FontDialog;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.FontField;
import ca.corbett.forms.fields.LabelField;

import javax.swing.JPanel;
import java.awt.Color;

/**
 * A demo panel to show a custom FormField implementation - FontField. This started out as an example
 * FormField implementation, but it was good enough to actually include as a usable form field
 * within swing-forms.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class CustomFormFieldPanel extends PanelBuilder {
    @Override
    public String getTitle() {
        return "Forms: custom fields";
    }

    @Override
    public JPanel build() {
        FormPanel formPanel = buildFormPanel("Creating a custom FormField");

        formPanel.add(new LabelField("<html>Sometimes the built-in form field components just aren't good enough,<br/>"
                                             + "and you need to build something custom.<br/><br/>"
                                             + "Here is an example of a custom font field that allows the user to select<br/>"
                                             + "various font properties all in one single FormField:</html>"));

        // Let's add some FontFields with various options:
        formPanel.add(new FontField("Just font, no size:")
                              .setShowSizeField(false));
        formPanel.add(new FontField("Both font and size:"));
        formPanel.add(new FontField("Font and text color:",
                                    FontDialog.INITIAL_FONT,
                                    Color.RED));
        formPanel.add(new FontField("Font and fg/bg color:",
                                    FontDialog.INITIAL_FONT,
                                    Color.BLUE,
                                    Color.ORANGE));

        LabelField label = new LabelField("Full source code for this component is included!");
        label.getMargins().setTop(24);
        formPanel.add(label);
        formPanel.add(new LabelField("In fact, it became a usable form field in swing-extras!"));
        formPanel.add(new LabelField("See the swing-extras book for a walkthrough of this form field!"));

        return formPanel;
    }
}
