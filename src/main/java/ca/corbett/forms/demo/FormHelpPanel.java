package ca.corbett.forms.demo;

import ca.corbett.extras.demo.SnippetAction;
import ca.corbett.extras.demo.panels.PanelBuilder;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.LongTextField;
import ca.corbett.forms.fields.NumberField;
import ca.corbett.forms.fields.ShortTextField;

import javax.swing.JPanel;

/**
 * This demo panel shows how to add helpful informational messages to FormFields.
 * These show up as little ? icons on the right side of the FormField.
 * Hovering over them will show the info message in a tooltip.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class FormHelpPanel extends PanelBuilder {
    @Override
    public String getTitle() {
        return "Forms: help tooltips";
    }

    @Override
    public JPanel build() {
        FormPanel panel = buildFormPanel("Help text tooltips");

        ShortTextField textField = new ShortTextField("Text:", 18);
        textField.setHelpText("Help icons show up whenever a field has help text");
        panel.add(textField);

        NumberField numberField = new NumberField("Number:", 0, 0, 100, 1);
        numberField.setHelpText("Most form field types can have help text");
        panel.add(numberField);

        LongTextField textBox = LongTextField.ofFixedSizeMultiLine("Text area:", 4, 22);
        textBox.setHelpText("Even text areas can have help text");
        panel.add(textBox);

        LabelField label = new LabelField("This is a form label.");
        label.setHelpText("<html>Wow, even labels can have help text if you want<br>"
                                  + "And they can be multiline<br>and as long as you need them to be<br><br>"
                                  + "Even if you like them really long and wordy</html>");
        panel.add(label);

        panel.add(new LabelField("If no help text is given, the icon is hidden."));

        panel.add(createSnippetLabel(new HelpTextSnippetAction()));

        return panel;
    }

    private static class HelpTextSnippetAction extends SnippetAction {
        @Override
        protected String getSnippet() {
            return """
                    ShortTextField textField = new ShortTextField("Text:", 18);
                    textField.setHelpText("Help icons show up whenever a field has help text");
                    
                    NumberField numberField = new NumberField("Number:", 0, 0, 100, 1);
                    numberField.setHelpText("Most form field types can have help text");
                    
                    LongTextField textBox = LongTextField.ofFixedSizeMultiLine("Text area:", 4, 22);
                    textBox.setHelpText("Even text areas can have help text");
                    
                    LabelField label = new LabelField("This is a form label.");
                    label.setHelpText("<html>Wow, even labels can have help text if you want<br>"
                                              + "And they can be multiline<br>and as long as you need them to be<br><br>"
                                              + "Even if you like them really long and wordy</html>");
                    """;
        }
    }
}
