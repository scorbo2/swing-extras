package ca.corbett.extras.demo.panels;

import ca.corbett.extras.TextInputDialog;
import ca.corbett.extras.demo.DemoApp;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.LongTextField;
import ca.corbett.forms.fields.ShortTextField;
import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.ValidationResult;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;

import static ca.corbett.extras.TextInputDialog.InputType;

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
    @Override
    public String getTitle() {
        return "TextInputDialog";
    }

    @Override
    public JPanel build() {
        FormPanel formPanel = buildFormPanel("TextInputDialog");

        String sb = "<html>The TextInputDialog class allows you to prompt the user for<br>" +
                "single-line or multi-line text input, with optional validation<br>" +
                "to ensure the input is valid before allowing the user to confirm." +
                "<br><br>Try it out!</html>";
        LabelField labelField = LabelField.createPlainHeaderLabel(sb, 14);
        labelField.getMargins().setTop(12).setBottom(12);
        formPanel.add(labelField);

        LabelField link1 = new LabelField("Single-line with no validation rules");
        link1.setHyperlink(new LaunchDialogAction(InputType.SingleLine, false));
        formPanel.add(link1);
        LabelField link2 = new LabelField("Multi-line with no validation rules");
        link2.setHyperlink(new LaunchDialogAction(InputType.MultiLine, false));
        formPanel.add(link2);

        sb = "<html>You can add whatever validation rules you like!<br>" +
                "Let's try setting a minimum and maximum length,<br>" +
                "and also rejecting any input that contains<br>" +
                "the word \"foobar\" (just as an example).</html>";
        labelField = LabelField.createPlainHeaderLabel(sb, 14);
        labelField.getMargins().setTop(12).setBottom(12);
        formPanel.add(labelField);

        link1 = new LabelField("Single-line with custom validation rules");
        link1.setHyperlink(new LaunchDialogAction(InputType.SingleLine, true));
        formPanel.add(link1);
        link2 = new LabelField("Multi-line with custom validation rules");
        link2.setHyperlink(new LaunchDialogAction(InputType.MultiLine, true));
        formPanel.add(link2);

        return formPanel;
    }

    /**
     * An example validator that will insist on input text having a certain minimum and maximum
     * length, and will reject text that contains a specific substring. This is just an
     * example of what you can do with a custom validator. You can create your
     * own validator to enforce whatever requirements your application has!
     */
    private static class ExampleValidator implements FieldValidator<FormField> {

        public static final String NAUGHTY_WORD = "foobar";

        @Override
        public ValidationResult validate(FormField fieldToValidate) {
            String text;
            final int minimumLength = 3;
            int maximumLength;
            if (fieldToValidate instanceof ShortTextField) {
                text = ((ShortTextField)fieldToValidate).getText();
                maximumLength = 15;
            }
            else {
                text = ((LongTextField)fieldToValidate).getText();
                maximumLength = 50;
            }

            if (text.length() < minimumLength || text.length() > maximumLength) {
                return ValidationResult.invalid("Input must be between "
                                                        + minimumLength
                                                        + " and "
                                                        + maximumLength
                                                        + " characters.");
            }

            if (text.toLowerCase().contains(NAUGHTY_WORD)) {
                return ValidationResult.invalid("Input cannot contain \"" + NAUGHTY_WORD + "\".");
            }

            return ValidationResult.valid();
        }
    }

    /**
     * An example action that will fire up a TextInputDialog with optional validators set.
     */
    private static class LaunchDialogAction extends AbstractAction {

        private final TextInputDialog.InputType inputType;
        private final boolean addValidators;

        public LaunchDialogAction(TextInputDialog.InputType inputType, boolean addValidators) {
            this.inputType = inputType;
            this.addValidators = addValidators;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            TextInputDialog dialog = new TextInputDialog(DemoApp.getInstance(),
                                                         "TextInputDialog demo",
                                                         inputType);
            if (addValidators) {
                dialog.setAllowBlank(false);
                dialog.addValidator(new ExampleValidator());
            }

            dialog.setVisible(true);
        }
    }
}
