package ca.corbett.extras.demo.panels;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.extras.TextInputDialog;
import ca.corbett.extras.demo.DemoApp;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.ButtonField;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.LongTextField;
import ca.corbett.forms.fields.NumberField;
import ca.corbett.forms.fields.ShortTextField;
import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.ValidationResult;

import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Locale;

/**
 * A demo panel for showing off the new TextInputDialog.
 * Very similar to JOptionPane's showInputDialog, but with
 * optional configurable validation on the allowed input.
 * This dialog allows you to get either single-line or multi-line
 * text input from the user, and will not allow the user to confirm
 * until the input is valid according to the provided validators.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
 */
public class TextInputDialogPanel extends PanelBuilder {

    private ShortTextField initialTextField;
    private ShortTextField promptField;
    private ShortTextField confirmField;
    private ShortTextField disallowedTextField;
    private ComboField<String> inputTypeCombo;
    private NumberField minLengthField;
    private LabelField resultLabel;

    @Override
    public String getTitle() {
        return "TextInputDialog";
    }

    @Override
    public JPanel build() {
        FormPanel formPanel = buildFormPanel("TextInputDialog");

        String sb = "<html>The TextInputDialog class allows you to prompt<br>" +
                " the user for single-line or multi-line text input,<br>" +
                "with optional validation to ensure the input is<br>" +
                "valid before allowing the user to confirm." +
                "<br><br>Try it out!</html>";
        LabelField labelField = LabelField.createPlainHeaderLabel(sb, 14);
        labelField.getMargins().setTop(12).setBottom(12);
        formPanel.add(labelField);

        inputTypeCombo = new ComboField<>("Input type:", List.of("Single line", "Multi-line"), 0);
        formPanel.add(inputTypeCombo);

        initialTextField = new ShortTextField("Initial text:", 25);
        initialTextField.setText("Initial text (optional)");
        initialTextField.setAllowBlank(true);
        initialTextField.setHelpText("You can pre-populate the text field with some initial text, if you like.");
        formPanel.add(initialTextField);

        promptField = new ShortTextField("Prompt:", 25);
        promptField.setText("Input:");
        promptField.setAllowBlank(true);
        promptField.setHelpText("The prompt that will be shown next to the text field. Leave blank for default.");
        formPanel.add(promptField);

        confirmField = new ShortTextField("Confirm button:", 25);
        confirmField.setText("OK");
        confirmField.setAllowBlank(true);
        confirmField.setHelpText("The text that will be shown on the confirm button. Leave blank for default.");
        formPanel.add(confirmField);

        disallowedTextField = new ShortTextField("Disallowed text:", 25);
        disallowedTextField.setText("foobar");
        disallowedTextField.setAllowBlank(true);
        disallowedTextField.setHelpText("Optionally prevent this text from being typed into the field.");
        formPanel.add(disallowedTextField);

        minLengthField = new NumberField("Minimum length:", 3, 0, 99, 1);
        minLengthField.setHelpText("<html>Adding custom validation is easy! Here's one possible example.<br>" +
                                           "Set to 0 to disable this validator.</html>");
        formPanel.add(minLengthField);

        ButtonField buttonField = new ButtonField(List.of(new LaunchDialogAction()));
        formPanel.add(buttonField);

        resultLabel = new LabelField("Result:", "");
        resultLabel.getMargins().setTop(12);
        resultLabel.setVisible(false);
        formPanel.add(resultLabel);

        return formPanel;
    }

    /**
     * An example validator that will insist on input text having a certain minimum and maximum
     * length, and will reject text that contains a specific substring. This is just an
     * example of what you can do with a custom validator. You can create your
     * own validator to enforce whatever requirements your application has!
     */
    private class ExampleValidator implements FieldValidator<FormField> {

        @Override
        public ValidationResult validate(FormField fieldToValidate) {
            String text = null;
            final int minimumLength = minLengthField.getCurrentValue().intValue();
            if (minimumLength > 0) {
                if (fieldToValidate instanceof ShortTextField shortTextField) {
                    text = shortTextField.getText();
                }
                else if (fieldToValidate instanceof LongTextField longTextField) {
                    text = longTextField.getText();
                }

                if (text == null) {
                    return ValidationResult.valid();
                }

                if (text.length() < minimumLength) {
                    return ValidationResult.invalid("Input must be at least "
                                                            + minimumLength
                                                            + " characters long.");
                }

                String naughty = disallowedTextField.getText().toLowerCase(Locale.ROOT).trim();
                if (!naughty.isBlank()) {
                    if (text.toLowerCase().contains(naughty)) {
                        return ValidationResult.invalid("Input cannot contain \"" + naughty + "\".");
                    }
                }
            }

            return ValidationResult.valid();
        }
    }

    /**
     * An example action that will fire up a TextInputDialog with optional validators set.
     */
    private class LaunchDialogAction extends EnhancedAction {

        public LaunchDialogAction() {
            super("Show TextInputDialog");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            TextInputDialog.InputType inputType = inputTypeCombo.getSelectedIndex() == 0
                    ? TextInputDialog.InputType.SingleLine
                    : TextInputDialog.InputType.MultiLine;
            TextInputDialog dialog = new TextInputDialog(DemoApp.getInstance(),
                                                         "TextInputDialog demo",
                                                         inputType);

            String initialText = initialTextField.getText().trim();
            if (!initialText.isBlank()) {
                dialog.setInitialText(initialText);
            }

            String prompt = promptField.getText().trim();
            if (!prompt.isBlank()) {
                dialog.setPrompt(prompt);
            }

            String confirm = confirmField.getText().trim();
            if (!confirm.isBlank()) {
                dialog.setConfirmLabel(confirm);
            }

            dialog.setAllowBlank(false);
            dialog.addValidator(new ExampleValidator());

            dialog.setVisible(true);

            String result = dialog.getResult();
            if (result == null) {
                result = "User cancelled the dialog.";
            }
            resultLabel.setText(result);
            resultLabel.setVisible(true);
        }
    }
}
