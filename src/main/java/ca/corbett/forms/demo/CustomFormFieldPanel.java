package ca.corbett.forms.demo;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.demo.panels.PanelBuilder;
import ca.corbett.forms.Alignment;
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
        FormPanel formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.setBorderMargin(24);

        final LabelField headerLabel = LabelField.createBoldHeaderLabel("Creating a custom FormField implementation",
                                                                        20, 0, 8);
        headerLabel.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE));
        LookAndFeelManager.addChangeListener(
                e -> headerLabel.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE)));
        headerLabel.getMargins().setBottom(24);
        formPanel.add(headerLabel);

        String text = "<html>Sometimes the built-in form field components just aren't good enough,<br/>"
                + "and you need to build something custom.<br/><br/>"
                + "Here is an example of a custom font field that allows the user to select<br/>"
                + "various font properties all in one single FormField:</html>";
        LabelField label = LabelField.createPlainHeaderLabel(text, 14);
        formPanel.add(label);

        FontField fontField = new FontField("Just font, no size:");
        fontField.setShowSizeField(false);
        fontField.getMargins().setTop(8);
        formPanel.add(fontField);
        fontField = new FontField("Both font and size:");
        formPanel.add(fontField);

        formPanel.add(new FontField("Font and text color:", FontDialog.INITIAL_FONT, Color.RED));
        formPanel.add(
                new FontField("Font and fg/bg color:", FontDialog.INITIAL_FONT, Color.BLUE, Color.ORANGE));

        label = LabelField.createPlainHeaderLabel("Full source code for this component is included!", 14);
        label.getMargins().setTop(24);
        formPanel.add(label);
        label = LabelField.createPlainHeaderLabel("See the swing-extras book for a walkthrough of this form field!",
                                                  14);
        formPanel.add(label);

        return formPanel;
    }
}
