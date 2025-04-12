package ca.corbett.forms.demo.panels;

import ca.corbett.forms.FontDialog;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.FontField;
import ca.corbett.forms.fields.LabelField;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;

public class CustomFieldPanel extends PanelBuilder {
    @Override
    public String getTitle() {
        return "Custom fields";
    }

    @Override
    public JPanel build() {
        FormPanel formPanel = new FormPanel();

        LabelField headerLabel = new LabelField("Creating a custom FormField implementation");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerLabel.setTopMargin(24);
        headerLabel.setBottomMargin(24);
        formPanel.addFormField(headerLabel);

        String text = "<html>Sometimes the built-in form field components just aren't good enough,<br/>"
                + "and you need to build something custom.<br/><br/>"
                + "Here is an example of a custom font field that allows the user to select<br/>"
                + "various font properties all in one single FormField:</html>";
        headerLabel = LabelField.createPlainHeaderLabel(text);
        formPanel.addFormField(headerLabel);

        FontField fontField = new FontField("Just font, no size:");
        fontField.setShowSizeField(false);
        fontField.setTopMargin(8);
        formPanel.addFormField(fontField);
        fontField = new FontField("Both font and size:");
        formPanel.addFormField(fontField);

        formPanel.addFormField(new FontField("Font and text color:", FontDialog.INITIAL_FONT, Color.RED));
        formPanel.addFormField(
                new FontField("Font and fg/bg color:", FontDialog.INITIAL_FONT, Color.BLUE, Color.ORANGE));

        headerLabel = LabelField.createPlainHeaderLabel("Full source code for this component is included!");
        headerLabel.setTopMargin(24);
        formPanel.addFormField(headerLabel);
        headerLabel = LabelField.createPlainHeaderLabel("See the README for a walkthrough of this form field.");
        formPanel.addFormField(headerLabel);

        formPanel.render();
        return formPanel;
    }
}
