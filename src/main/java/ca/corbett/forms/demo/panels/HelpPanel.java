package ca.corbett.forms.demo.panels;

import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.NumberField;
import ca.corbett.forms.fields.TextField;

import javax.swing.JPanel;
import java.awt.Font;

public class HelpPanel extends PanelBuilder {
    @Override
    public String getTitle() {
        return "Help";
    }

    @Override
    public JPanel build() {
        FormPanel panel = new FormPanel();

        LabelField headerLabel = new LabelField("Form fields can have help text!");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerLabel.setTopMargin(24);
        headerLabel.setBottomMargin(24);
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

        LabelField label = LabelField.createPlainHeaderLabel("This is a form label.");
        label.setHelpText(
                "<html>Wow, even labels can help text if you want<br>And they can be multiline<br>and as long as you need them to be<br><br>Even if you like them really long and wordy</html>");
        panel.addFormField(label);

        label = LabelField.createPlainHeaderLabel("If no help text is given, the icon remains hidden.");
        panel.addFormField(label);

        panel.render();
        return panel;
    }
}
