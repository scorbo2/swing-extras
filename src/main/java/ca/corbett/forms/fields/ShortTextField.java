package ca.corbett.forms.fields;

import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.NonBlankFieldValidator;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A FormField implementation specifically for short (single-line) text input.
 * The wrapped field is a JTextField. For convenience, a getTextField()
 * method is provided here to access the JTextField directly, but you can
 * also access it by calling getFieldComponent() from the FormField class
 * and casting the result to JTextField.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2019-11-23
 */
public class ShortTextField extends FormField implements DocumentListener {

    /**
     * Creates a ShortTextField with the given field label and the given number of columns.
     */
    public ShortTextField(String label, int cols) {
        fieldLabel.setText(label);
        fieldComponent = new JTextField(cols);
        fieldComponent.setFont(getDefaultFont());
        ((JTextField)fieldComponent).getDocument().addDocumentListener(this);
    }

    /**
     * Returns the text currently in this field.
     *
     * @return The current text value.
     */
    public String getText() {
        return ((JTextField)fieldComponent).getText();
    }

    /**
     * Sets the text in this field. Will overwrite any previous text.
     */
    public ShortTextField setText(String text) {
        if (text == null) {
            text = ""; // if null, assume empty string
        }
        if (Objects.equals(getText(), text)) {
            return this; // reject no-op changes
        }
        ((JTextField)fieldComponent).setText(text);
        return this;
    }

    /**
     * Reports whether a NonBlankFieldValidator has been added to this TextField.
     */
    public boolean isAllowBlank() {
        for (FieldValidator<? extends FormField> validator : fieldValidators) {
            if (validator instanceof NonBlankFieldValidator) {
                return false;
            }
        }
        return true;
    }

    /**
     * By default, TextField will allow blank values (empty text) to pass validation.
     * You can disallow that with this method - passing false will add a NonBlankFieldValidator
     * to this TextField. Passing true will remove the NonBlankFieldValidator if one is present.
     */
    public ShortTextField setAllowBlank(boolean allow) {
        if (allow) {
            removeNonBlankValidatorIfPresent();
        }
        else {
            addNonBlankValidatorIfNotPresent();
        }
        return this;
    }

    /**
     * Returns the underlying JTextField for this field.
     */
    public JTextField getTextField() {
        return (JTextField)fieldComponent;
    }

    protected void addNonBlankValidatorIfNotPresent() {
        boolean found = false;
        for (FieldValidator<? extends FormField> validator : fieldValidators) {
            if (validator instanceof NonBlankFieldValidator) {
                found = true;
                break;
            }
        }
        if (!found) {
            addFieldValidator(new NonBlankFieldValidator());
        }
    }

    protected void removeNonBlankValidatorIfPresent() {
        List<FieldValidator<? extends FormField>> foundList = new ArrayList<>();
        for (FieldValidator<? extends FormField> fieldValidator : fieldValidators) {
            if (fieldValidator instanceof NonBlankFieldValidator) {
                foundList.add(fieldValidator);
            }
        }
        for (FieldValidator<? extends FormField> validator : foundList) {
            fieldValidators.remove(validator);
        }
    }

    // DocumentListener interface stuff below this line --------------------------------
    //
    // Yeah, it's horribly broken, but CoalescingDocumentListener is even worse,
    // and will be marked as deprecated in 2.7 (nuked for all time in 2.8).
    // So, until a better solution appears, we're stuck with DocumentListener.
    // https://github.com/scorbo2/swing-extras/issues/251 for the details.

    @Override
    public void insertUpdate(DocumentEvent e) {
        fireValueChangedEvent();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        fireValueChangedEvent();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        fireValueChangedEvent();
    }
}
