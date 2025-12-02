package ca.corbett.forms.demo;

import ca.corbett.extras.demo.SnippetAction;
import ca.corbett.extras.demo.panels.PanelBuilder;
import ca.corbett.extras.gradient.ColorSelectionType;
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

/**
 * Builds a FormPanel that shows how to add custom FieldValidators to make
 * form validation very easy to implement.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2019-11-25
 */
public class FormsValidationPanel extends PanelBuilder {

    private FormPanel formPanel;
    private LabelField warningLabel;
    private LabelField successLabel;

    @Override
    public String getTitle() {
        return "Forms: validation";
    }

    @Override
    public JPanel build() {
        formPanel = buildFormPanel("Custom form validation");
        LabelField label = LabelField.createPlainHeaderLabel("You can use built-in validators or make your own!", 14);
        label.getMargins().setBottom(18);
        formPanel.add(label);

        // We can add a ShortTextField with a custom validator very easily:
        formPanel.add(new ShortTextField("Must be 3+ chars long:", 15)
                              .setAllowBlank(false)
                              .setText("Example")
                              .addFieldValidator(new CustomTextFieldValidator()));

        // Adding a custom validator to a ColorField is equally simply:
        final ColorField colorField = new ColorField("Don't choose black!", ColorSelectionType.SOLID);
        colorField.setColor(Color.BLACK);
        colorField.addFieldValidator(new CustomColorFieldValidator());
        formPanel.add(colorField);

        // Custom validators can reference other form fields, if you supply them.
        // Let's have our checkbox validator reference the ColorField we created above:
        formPanel.add(new CheckBoxField("I promise I didn't choose black.", true)
                              .addFieldValidator(new CustomCheckboxValidator(colorField)));

        // Now we can use PanelField to wrap a validation button:
        PanelField panelField = new PanelField(new FlowLayout(FlowLayout.LEFT));
        JButton btn = new JButton("Validate form");
        btn.addActionListener(e -> validateForm());
        panelField.getPanel().add(btn);
        formPanel.add(panelField);
        formPanel.add(createSnippetLabel(new ValidationSnippetAction()));

        // Create our validation labels, add them to the form panel, but hide them by default:
        // (they will get shown as needed when the form gets validated)
        warningLabel = LabelField.createPlainHeaderLabel(
                "<html>Oops! Looks like there are form validation errors!<br/>" +
                        "No problem, just hover over the validation error<br/>" +
                        "markers to see what went wrong.</html>",
                14);
        successLabel = LabelField.createPlainHeaderLabel("Hooray! No validation errors to fix.", 14);
        successLabel.setVisible(false);
        formPanel.add(successLabel);
        warningLabel.setVisible(false);
        formPanel.add(warningLabel);

        return formPanel;
    }

    /**
     * Invoked internally to do form validation. We show a success or a warning label here on the form
     * panel depending on the validation results, but this is not necessary - the form panel itself will
     * display validation error labels with helpful tooltips next to each form field that doesn't
     * successfully validate. The general workflow is that when the user tries to OK a form,
     * you should ask the form panel to validate itself before you allow the dialog to dismiss.
     */
    private void validateForm() {
        boolean isValid = formPanel.isFormValid();
        warningLabel.setVisible(!isValid);
        successLabel.setVisible(isValid);
    }

    /**
     * A simple FieldValidator implementation that we can hook onto a ShortTextField.
     */
    private static class CustomTextFieldValidator implements FieldValidator<ShortTextField> {
        @Override
        public ValidationResult validate(ShortTextField fieldToValidate) {
            // A real validator could do whatever logic it needed to in here...
            if (fieldToValidate.getText().length() < 3) {
                return ValidationResult.invalid("Text must be at least three characters!");
            }
            return ValidationResult.valid();
        }
    }

    /**
     * A simple FieldValidator implementation that we can hook onto a ColorField.
     */
    private static class CustomColorFieldValidator implements FieldValidator<ColorField> {
        @Override
        public ValidationResult validate(ColorField fieldToValidate) {
            if (Color.BLACK.equals(fieldToValidate.getColor())) {
                return ValidationResult.invalid("I said DON'T choose black!");
            }
            return ValidationResult.valid();
        }
    }

    /**
     * A simple FieldValidator implementation that we can hook onto a CheckBoxField.
     * This validator requires a ColorField to instantiate, as we will reference the current
     * value of that field when making our validation decision for the checkbox.
     */
    private static class CustomCheckboxValidator implements FieldValidator<CheckBoxField> {
        private final ColorField colorField;

        public CustomCheckboxValidator(ColorField colorField) {
            this.colorField = colorField;
        }

        @Override
        public ValidationResult validate(CheckBoxField fieldToValidate) {
            if (fieldToValidate.isChecked() && Color.BLACK.equals(colorField.getColor())) {
                return ValidationResult.invalid("You broke your promise!");
            }
            return ValidationResult.valid();
        }
    }

    /**
     * Shows a code snippet for putting a custom validator on a FormField.
     *
     * @author <a href="https://github.com/scorbo2">scorbo2</a>
     */
    private static class ValidationSnippetAction extends SnippetAction {
        @Override
        protected String getSnippet() {
            return """
                    // Adding a validator to any FormField is very easy:
                    ShortTextField textField = new ShortTextField("Must be 3+ chars long:", 15)
                              .setAllowBlank(false)
                              .setText("Example")
                              .addFieldValidator(new CustomTextFieldValidator());
                    
                    // And our CustomTextFieldValidator is easy to write:
                    private static class CustomTextFieldValidator implements FieldValidator<ShortTextField> {
                        @Override
                        public ValidationResult validate(ShortTextField fieldToValidate) {
                            // A real validator could do whatever logic it needed to in here...
                            // But for this demo, just check the length of the text:
                            if (fieldToValidate.getText().length() < 3) {
                                return ValidationResult.invalid("Text must be at least three characters!");
                            }
                            return ValidationResult.valid();
                        }
                    }
                    """;
        }
    }
}
