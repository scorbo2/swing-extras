package ca.corbett.forms.demo;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.demo.panels.PanelBuilder;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.LongTextField;
import ca.corbett.forms.fields.NumberField;
import ca.corbett.forms.fields.ShortTextField;

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
        headerLabel.getMargins().setBottom(24);
        headerLabel.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE));
        LookAndFeelManager.addChangeListener(
                e -> headerLabel.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE)));
        panel.add(headerLabel);

        ShortTextField textField = new ShortTextField("Text:", 18);
        textField.setHelpText("Help icons show up whenever a field has help text");
        panel.add(textField);

        NumberField numberField = new NumberField("Number:", 0, 0, 100, 1);
        numberField.setHelpText("Most form field types can have help text");
        panel.add(numberField);

        LongTextField textBox = LongTextField.ofFixedSizeMultiLine("Text area:", 4, 22);
        textBox.setHelpText("Even text areas can have help text");
        panel.add(textBox);

        LabelField label = LabelField.createPlainHeaderLabel("This is a form label.", 14);
        label.setHelpText(
                "<html>Wow, even labels can have help text if you want<br>And they can be multiline<br>and as long as you need them to be<br><br>Even if you like them really long and wordy</html>");
        panel.add(label);

        label = LabelField.createPlainHeaderLabel("If no help text is given, the icon is hidden.", 14);
        panel.add(label);

        return panel;
    }
}
