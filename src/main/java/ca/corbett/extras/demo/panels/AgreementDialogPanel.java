package ca.corbett.extras.demo.panels;

import ca.corbett.extras.AgreementDialog;
import ca.corbett.extras.EnhancedAction;
import ca.corbett.extras.demo.DemoApp;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.ButtonField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.LongTextField;
import ca.corbett.forms.fields.ShortTextField;

import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * A demo panel for showing off the new AgreementDialog.
 * This dialog is for showing some kind of agreement,
 * license text, terms of use, or anything else that must
 * be confirmed or agreed to. The intention is that the
 * user cannot proceed until they click "I agree"
 * or whatever the checkbox is labeled.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 3.0
 */
public class AgreementDialogPanel extends PanelBuilder {

    private LongTextField agreementTextField;
    private ShortTextField confirmField;
    private ShortTextField helpTextField;
    private ShortTextField overviewTextField;
    private ShortTextField checkboxTextField;
    private LabelField resultLabel;

    private final String AGREEMENT = "Important agreement" +
            "\n\nI hereby agree to give all my money to the developers of swing-extras, " +
            "and to never file a bug report, because this library is just that amazing. " +
            "I understand that if I do not comply with these terms, " +
            "the developers of swing-extras will be very sad and may cry.";

    @Override
    public String getTitle() {
        return "AgreementDialog";
    }

    @Override
    public JPanel build() {
        FormPanel formPanel = buildFormPanel("AgreementDialog");

        String sb = "<html>The Agreement class allows you to display<br>" +
                " a license agreement, terms of use, or some other<br>" +
                "text that must be agreed to or confirmed before<br>" +
                "allowing the user to confirm." +
                "<br><br>Try it out!</html>";
        LabelField labelField = LabelField.createPlainHeaderLabel(sb, 14);
        labelField.getMargins().setTop(12).setBottom(12);
        formPanel.add(labelField);

        agreementTextField = LongTextField.ofFixedSizeMultiLine("Agreement text:", 8, 25);
        agreementTextField.setText(AGREEMENT);
        agreementTextField.setAllowBlank(true);
        agreementTextField.setHelpText("Whatever agreement or license text you want to show the user." +
                                               " Can be blank, but... why?");
        formPanel.add(agreementTextField);

        checkboxTextField = new ShortTextField("Checkbox text:", 25);
        checkboxTextField.setText("I agree to the terms above");
        checkboxTextField.setAllowBlank(false);
        checkboxTextField.setHelpText("The text that will be shown next to the checkbox. Cannot be blank.");
        formPanel.add(checkboxTextField);

        confirmField = new ShortTextField("Confirm button:", 25);
        confirmField.setText("I agree");
        confirmField.setAllowBlank(true);
        confirmField.setHelpText("The text that will be shown on the confirm button. Leave blank for default.");
        formPanel.add(confirmField);

        helpTextField = new ShortTextField("Help text:", 25);
        helpTextField.setHelpText("Sets optional help text to appear beside the input field.");
        formPanel.add(helpTextField);

        overviewTextField = new ShortTextField("Overview text:", 25);
        overviewTextField.setHelpText("Sets optional overview text to appear at the top of the dialog.");
        formPanel.add(overviewTextField);

        ButtonField buttonField = new ButtonField(List.of(new LaunchDialogAction()));
        formPanel.add(buttonField);

        resultLabel = new LabelField("Result:", "");
        resultLabel.getMargins().setTop(12);
        resultLabel.setVisible(false);
        formPanel.add(resultLabel);

        return formPanel;
    }

    /**
     * An example action that will fire up a AgreementDialog with the selected options.
     */
    private class LaunchDialogAction extends EnhancedAction {

        public LaunchDialogAction() {
            super("Show AgreementDialog");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            AgreementDialog dialog = new AgreementDialog(DemoApp.getInstance(),
                                                         "AgreementDialog demo");

            dialog.setAgreementText(agreementTextField.getText().trim());
            dialog.setCheckboxText(checkboxTextField.getText().trim());
            dialog.setConfirmLabel(confirmField.getText().trim());
            dialog.setHelpText(helpTextField.getText().trim());

            // Give it a little extra vertical height if the user gave us overview text:
            if (!overviewTextField.getText().trim().isBlank()) {
                dialog.setOverviewText(overviewTextField.getText().trim());
                dialog.setSize(dialog.getWidth(), dialog.getHeight() + 50);
            }

            dialog.setVisible(true);
            String result = dialog.wasAgreed() ? "User agreed to the terms." : "User did not agree to the terms.";
            resultLabel.setText(result);
            resultLabel.setVisible(true);
        }
    }
}
