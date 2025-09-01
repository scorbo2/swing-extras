package ca.corbett.forms.demo;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.demo.panels.PanelBuilder;
import ca.corbett.extras.gradient.ColorSelectionType;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.CheckBoxField;
import ca.corbett.forms.fields.ColorField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.PanelField;
import ca.corbett.forms.fields.ShortTextField;
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
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2029-11-25
 */
public class FormsValidationPanel extends PanelBuilder {
    @Override
    public String getTitle() {
        return "Forms: validation";
    }

    @Override
    public JPanel build() {
        final FormPanel formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.setBorderMargin(24);

        final LabelField headerLabel = LabelField.createBoldHeaderLabel("Form validation is super easy!", 20, 0, 8);
        headerLabel.getMargins().setBottom(12);
        headerLabel.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE));
        LookAndFeelManager.addChangeListener(
                e -> headerLabel.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE)));
        formPanel.add(headerLabel);
        LabelField label = LabelField.createPlainHeaderLabel("You can use built-in validators or make your own!", 14);
        label.getMargins().setBottom(18);
        formPanel.add(label);

        String sb = "<html>Oops! Looks like there are form validation errors!<br/>" +
                "No problem, just hover over the validation error<br/>" +
                "markers to see what went wrong.</html>";
        final LabelField warningLabel = LabelField.createPlainHeaderLabel(sb, 14);

        final LabelField successLabel = LabelField.createPlainHeaderLabel("Hooray! No validation errors to fix.", 14);

        final ShortTextField textField = new ShortTextField("Must be 3+ chars long: ", 15).setAllowBlank(false);
        textField.setText("Example");
        textField.addFieldValidator(new FieldValidator<ShortTextField>() {
            @Override
            public ValidationResult validate(ShortTextField fieldToValidate) {
                if (fieldToValidate.getText().length() < 3) {
                    return ValidationResult.invalid("Text must be at least three characters!");
                }
                return ValidationResult.valid();
            }
        });
        formPanel.add(textField);

        final ColorField colorField = new ColorField("Don't choose black!", ColorSelectionType.SOLID).setColor(
                Color.BLACK);
        colorField.addFieldValidator(new FieldValidator<ColorField>() {
            @Override
            public ValidationResult validate(ColorField fieldToValidate) {
                if (Color.BLACK.equals(fieldToValidate.getColor())) {
                    return ValidationResult.invalid("I said DON'T choose black!");
                }
                return ValidationResult.valid();
            }
        });
        formPanel.add(colorField);

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
        formPanel.add(checkbox);

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
        formPanel.add(panelField);

        successLabel.setVisible(false);
        formPanel.add(successLabel);

        warningLabel.setVisible(false);
        formPanel.add(warningLabel);

        return formPanel;
    }
}
