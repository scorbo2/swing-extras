package ca.corbett.forms.demo.panels;

import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.CheckBoxField;
import ca.corbett.forms.fields.ColorField;
import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.PanelField;
import ca.corbett.forms.fields.TextField;
import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.ValidationResult;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Builds a FormPanel that shows how to add custom FieldValidators to make
 * form validation very easy to implement.
 *
 * @author scorbo2
 * @since 2029-11-25
 */
public class ValidationPanel extends PanelBuilder {
    @Override
    public String getTitle() {
        return "Validation";
    }

    @Override
    public JPanel build() {
        final FormPanel formPanel = new FormPanel();

        LabelField headerLabel = new LabelField("Form validation is easy and extensible!");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerLabel.setTopMargin(24);
        headerLabel.setBottomMargin(24);
        formPanel.addFormField(headerLabel);

        String sb = "<html>Oops! Looks like there are form validation errors!<br/>" +
                "No problem, just hover over the validation error<br/>" +
                "markers to see what went wrong.</html>";
        final LabelField warningLabel = LabelField.createPlainHeaderLabel(sb);

        final LabelField successLabel = LabelField.createPlainHeaderLabel("Hooray! No validation errors to fix.");

        final TextField textField = new TextField("Must be 3+ chars long: ", 15, 1, false);
        textField.setText("Example");
        textField.addFieldValidator(new FieldValidator<FormField>(textField) {
            @Override
            public ValidationResult validate() {
                ValidationResult result = new ValidationResult();
                if (((TextField)field).getText().length() < 3) {
                    result.setResult(false, "Text must be at least three characters!");
                }
                return result;
            }
        });
        formPanel.addFormField(textField);

        final ColorField colorField = new ColorField("Don't choose black!", Color.BLACK);
        colorField.addFieldValidator(new FieldValidator<FormField>(colorField) {
            @Override
            public ValidationResult validate() {
                ValidationResult result = new ValidationResult();
                if (Color.BLACK.equals(((ColorField)field).getColor())) {
                    result.setResult(false, "I said DON'T choose black!");
                }
                return result;
            }
        });
        formPanel.addFormField(colorField);

        final CheckBoxField checkbox = new CheckBoxField("I promise I didn't choose black.", true);
        checkbox.addFieldValidator(new FieldValidator<FormField>(checkbox) {
            @Override
            public ValidationResult validate() {
                ValidationResult result = new ValidationResult();
                if (((CheckBoxField)field).isChecked() && Color.BLACK.equals(colorField.getColor())) {
                    result.setResult(false, "You broke your promise!");
                }
                return result;
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
