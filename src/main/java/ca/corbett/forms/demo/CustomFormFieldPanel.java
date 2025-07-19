package ca.corbett.forms.demo;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.demo.panels.PanelBuilder;
import ca.corbett.forms.FontDialog;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.FontField;
import ca.corbett.forms.fields.LabelField;

import javax.swing.JPanel;
import java.awt.Color;

public class CustomFormFieldPanel extends PanelBuilder {
    @Override
    public String getTitle() {
        return "Forms: custom fields";
    }

    @Override
    public JPanel build() {
        FormPanel formPanel = new FormPanel(FormPanel.Alignment.TOP_LEFT);
        formPanel.setStandardLeftMargin(24);

        final LabelField headerLabel = LabelField.createBoldHeaderLabel("Creating a custom FormField implementation",
                                                                        20);
        headerLabel.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE));
        LookAndFeelManager.addChangeListener(
                e -> headerLabel.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE)));
        headerLabel.setBottomMargin(24);
        formPanel.addFormField(headerLabel);

        String text = "<html>Sometimes the built-in form field components just aren't good enough,<br/>"
                + "and you need to build something custom.<br/><br/>"
                + "Here is an example of a custom font field that allows the user to select<br/>"
                + "various font properties all in one single FormField:</html>";
        LabelField label = LabelField.createPlainHeaderLabel(text, 14);
        formPanel.addFormField(label);

        FontField fontField = new FontField("Just font, no size:");
        fontField.setShowSizeField(false);
        fontField.setTopMargin(8);
        formPanel.addFormField(fontField);
        fontField = new FontField("Both font and size:");
        formPanel.addFormField(fontField);

        formPanel.addFormField(new FontField("Font and text color:", FontDialog.INITIAL_FONT, Color.RED));
        formPanel.addFormField(
                new FontField("Font and fg/bg color:", FontDialog.INITIAL_FONT, Color.BLUE, Color.ORANGE));

        label = LabelField.createPlainHeaderLabel("Full source code for this component is included!", 14);
        headerLabel.setTopMargin(24);
        formPanel.addFormField(label);
        label = LabelField.createPlainHeaderLabel("See the swing-extras book for a walkthrough of this form field!",
                                                  14);
        formPanel.addFormField(label);

        formPanel.render();
        return formPanel;
    }
}
