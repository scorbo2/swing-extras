package ca.corbett.forms.demo;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.demo.panels.PanelBuilder;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.NumberField;
import ca.corbett.forms.fields.TextField;

import javax.swing.JPanel;
import java.awt.Color;

public class FormHelpPanel extends PanelBuilder {
    @Override
    public String getTitle() {
        return "Forms: help tooltips";
    }

    @Override
    public JPanel build() {
        FormPanel panel = new FormPanel(Alignment.TOP_LEFT);
        panel.setBorderMargin(24);

        LabelField headerLabel = LabelField.createBoldHeaderLabel("Form fields can have help text!", 20);
        headerLabel.setBottomMargin(24);
        headerLabel.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE));
        LookAndFeelManager.addChangeListener(
                e -> headerLabel.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE)));
        panel.addFormField(headerLabel);

        TextField textField = new TextField("Text:", 12, 1, true);
        textField.setHelpText("Help icons show up whenever a field has help text");
        panel.addFormField(textField);

        NumberField numberField = new NumberField("Number:", 0, 0, 100, 1);
        numberField.setHelpText("Most form field types can have help text");
        panel.addFormField(numberField);

        TextField textBox = new TextField("Text area:", 12, 4, true);
        textBox.setHelpText("Even text areas can have help text");
        textBox.setExpandMultiLineTextBoxHorizontally(true);
        panel.addFormField(textBox);

        LabelField label = LabelField.createPlainHeaderLabel("This is a form label.", 14);
        label.setHelpText(
                "<html>Wow, even labels can have help text if you want<br>And they can be multiline<br>and as long as you need them to be<br><br>Even if you like them really long and wordy</html>");
        panel.addFormField(label);

        label = LabelField.createPlainHeaderLabel("If no help text is given, the icon remains hidden.", 14);
        panel.addFormField(label);

        panel.render();
        return panel;
    }
}
