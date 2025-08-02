package ca.corbett.forms.demo;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.demo.panels.PanelBuilder;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.CheckBoxField;
import ca.corbett.forms.fields.ColorField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.PanelField;
import ca.corbett.forms.fields.TextField;
import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.ValidationResult;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Builds a FormPanel that shows how to add custom FieldValidators to make
 * form validation very easy to implement.
 *
 * @author scorbo2
 * @since 2029-11-25
 */
public class FormsValidationPanel extends PanelBuilder {
    @Override
    public String getTitle() {
        return "Forms: validation";
    }

    @Override
    public JPanel build() {
        final FormPanel formPanel = new FormPanel(FormPanel.Alignment.TOP_LEFT);
        formPanel.setStandardLeftMargin(24);

        final LabelField headerLabel = LabelField.createBoldHeaderLabel("Form validation is super easy!", 20);
        headerLabel.setBottomMargin(12);
        headerLabel.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE));
        LookAndFeelManager.addChangeListener(
                e -> headerLabel.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE)));
        formPanel.addFormField(headerLabel);
        LabelField label = LabelField.createPlainHeaderLabel("You can use built-in validators or make your own!", 14);
        label.setBottomMargin(18);
        formPanel.addFormField(label);

        String sb = "<html>Oops! Looks like there are form validation errors!<br/>" +
                "No problem, just hover over the validation error<br/>" +
                "markers to see what went wrong.</html>";
        final LabelField warningLabel = LabelField.createPlainHeaderLabel(sb, 14);

        final LabelField successLabel = LabelField.createPlainHeaderLabel("Hooray! No validation errors to fix.", 14);

        final TextField textField = new TextField("Must be 3+ chars long: ", 15, 1, false);
        textField.setText("Example");
        textField.addFieldValidator(new FieldValidator<TextField>() {
            @Override
            public ValidationResult validate(TextField fieldToValidate) {
                if (fieldToValidate.getText().length() < 3) {
                    return ValidationResult.invalid("Text must be at least three characters!");
                }
                return ValidationResult.valid();
            }
        });
        formPanel.addFormField(textField);

        final ColorField colorField = new ColorField("Don't choose black!", Color.BLACK);
        colorField.addFieldValidator(new FieldValidator<ColorField>() {
            @Override
            public ValidationResult validate(ColorField fieldToValidate) {
                if (Color.BLACK.equals(fieldToValidate.getColor())) {
                    return ValidationResult.invalid("I said DON'T choose black!");
                }
                return ValidationResult.valid();
            }
        });
        formPanel.addFormField(colorField);

        final CheckBoxField checkbox = new CheckBoxField("I promise I didn't choose black.", true);
        checkbox.addFieldValidator(new FieldValidator<CheckBoxField>() {
            @Override
            public ValidationResult validate(CheckBoxField fieldToValidate) {
                if (fieldToValidate.isChecked() && Color.BLACK.equals(colorField.getColor())) {
                    return ValidationResult.invalid("You broke your promise!");
                }
                return ValidationResult.valid();
            }
        });
        formPanel.addFormField(checkbox);

        PanelField panelField = new PanelField();
        JPanel panel = panelField.getPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JButton btn = new JButton("Validate form");
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isValid = formPanel.isFormValid();
                warningLabel.setVisible(!isValid);
                successLabel.setVisible(isValid);
            }
        });
        panel.add(btn);
        formPanel.addFormField(panelField);

        successLabel.setVisible(false);
        formPanel.addFormField(successLabel);

        warningLabel.setVisible(false);
        formPanel.addFormField(warningLabel);

        formPanel.render();
        return formPanel;
    }
}
