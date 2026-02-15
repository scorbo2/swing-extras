package ca.corbett.forms.fields;

import ca.corbett.extras.io.KeyStrokeManager;
import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.ValidationResult;

import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A custom FormField implementation that allows editing of a single
 * KeyStroke assignment. Intended to be used with KeyStrokeManager,
 * but can be used standalone as well.
 * <p>
 * Internally, this field is very similar to ShortTextField
 * with some default values and a custom FieldValidator to ensure
 * that the given shortcut string is valid. You can optionally
 * allow blank input to indicate "no shortcut assigned".
 * This is disabled by default (i.e. the given shortcut string
 * must always be a valid keystroke).
 * </p>
 * <p>
 * You can optionally specify a list of reserved KeyStrokes
 * that should not be allowed to be entered in this field.
 * This is useful if you want to prevent users from assigning
 * certain system-wide shortcuts that your application uses.
 * This list is blank by default, meaning that any valid
 * KeyStroke is allowed.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.7
 */
public class KeyStrokeField extends FormField implements DocumentListener {

    public static final String RESERVED_MSG = "This keystroke is reserved and cannot be used";
    public static final int DEFAULT_COLS = 15;

    private String reservedKeyStrokeMsg = RESERVED_MSG;
    private final Set<KeyStroke> reservedKeyStrokes = new HashSet<>();
    private final JTextField textField;
    private boolean allowBlank;
    private final FieldValidator<? extends FormField> fieldValidator;

    /**
     * Creates a new KeyStrokeField to represent the given KeyStroke, which can be null to
     * start with an initial blank value. This will fail validation by default, unless
     * you invoke setAllowBlank(true).
     */
    public KeyStrokeField(String label, KeyStroke keyStroke) {
        fieldLabel.setText(label);
        textField = new JTextField(DEFAULT_COLS);
        textField.getDocument().addDocumentListener(this); // never using CoalescingDocumentListener again. Ever.
        fieldComponent = textField;
        fieldComponent.setFont(getDefaultFont());
        allowBlank = false; // default: do not allow blank input
        fieldValidator = new KeyStrokeFieldValidator();
        addFieldValidator(fieldValidator);
        textField.setText(keyStroke != null ? KeyStrokeManager.keyStrokeToString(keyStroke) : "");
    }

    /**
     * Returns the KeyStroke currently represented by this field,
     * or null if the field is blank or contains an invalid KeyStroke string.
     */
    public KeyStroke getKeyStroke() {
        // Safe to invoke with blank or empty string; will return null:
        return KeyStrokeManager.parseKeyStroke(textField.getText());
    }

    /**
     * Sets the KeyStroke represented by this field. Setting null or blank string is allowed,
     * even if allowBlank is false, but the field will fail validation in that case.
     */
    public KeyStrokeField setKeyStroke(KeyStroke keyStroke) {
        String newText = (keyStroke == null) ? "" : KeyStrokeManager.keyStrokeToString(keyStroke);
        if (textField.getText().equals(newText)) {
            return this; // reject no-op changes
        }
        textField.setText(newText);
        return this;
    }

    /**
     * Returns the raw KeyStroke string currently in this field.
     * If the current text does not represent a valid KeyStroke, this
     * method will still return empty string.
     */
    public String getKeyStrokeString() {
        KeyStroke parsed = getKeyStroke();
        return (parsed == null) ? "" : KeyStrokeManager.keyStrokeToString(parsed); // return normalized string
    }

    /**
     * Returns the number of columns in the underlying JTextField.
     */
    public int getColumns() {
        return textField.getColumns();
    }

    /**
     * Sets the number of columns in the underlying JTextField.
     */
    public KeyStrokeField setColumns(int cols) {
        textField.setColumns(cols);
        return this;
    }

    /**
     * Reports whether this field allows blank input (no keystroke assigned).
     */
    public boolean isAllowBlank() {
        return allowBlank;
    }

    /**
     * Decides whether this field allows blank input (no keystroke assigned).
     * The default is false, meaning a valid KeyStroke string must always be entered.
     */
    public KeyStrokeField setAllowBlank(boolean allowBlank) {
        this.allowBlank = allowBlank;
        return this;
    }

    /**
     * Optionally set a list of reserved KeyStrokes that cannot be assigned
     * using this field. By default, no KeyStrokes are reserved.
     * The given list will replace any previously set reserved KeyStrokes.
     */
    public KeyStrokeField setReservedKeyStrokes(List<KeyStroke> reservedKeyStrokes) {
        setReservedKeyStrokes(reservedKeyStrokes, RESERVED_MSG);
        return this;
    }

    /**
     * Optionally set a list of reserved KeyStrokes that cannot be assigned,
     * and also sets a custom message to be used when validation fails
     * because a reserved KeyStroke was entered. The given list will replace
     * any previously set reserved KeyStrokes.
     */
    public KeyStrokeField setReservedKeyStrokes(List<KeyStroke> reservedKeyStrokes, String reservedMsg) {
        this.reservedKeyStrokes.clear();

        if (reservedKeyStrokes == null || reservedKeyStrokes.isEmpty()) {
            return this; // nothing to add
        }

        this.reservedKeyStrokeMsg = (reservedMsg == null || reservedMsg.isBlank())
                ? RESERVED_MSG
                : reservedMsg;

        this.reservedKeyStrokes.addAll(reservedKeyStrokes);
        return this;
    }

    /**
     * Adds additional reserved KeyStrokes to the list of KeyStrokes that cannot be assigned.
     * Duplicates are automatically pruned. This method does not affect the reserved KeyStroke message.
     */
    public KeyStrokeField addReservedKeyStrokes(List<KeyStroke> additionalKeyStrokes) {
        if (additionalKeyStrokes != null && !additionalKeyStrokes.isEmpty()) {
            this.reservedKeyStrokes.addAll(additionalKeyStrokes);
        }
        return this;
    }

    /**
     * Clears the list of reserved KeyStrokes, allowing all KeyStrokes to be assigned.
     */
    public KeyStrokeField clearReservedKeyStrokes() {
        this.reservedKeyStrokes.clear();
        return this;
    }

    /**
     * Returns a copy of the list of reserved KeyStrokes that cannot be assigned
     */
    public List<KeyStroke> getReservedKeyStrokes() {
        return new ArrayList<>(reservedKeyStrokes);
    }

    /**
     * Returns the validation message that will be given if a reserved
     * KeyStroke is entered.
     */
    public String getReservedKeyStrokeMsg() {
        return reservedKeyStrokeMsg;
    }

    /**
     * Sets the validation message that will be given if a reserved
     * KeyStroke is entered.
     */
    public KeyStrokeField setReservedKeyStrokeMsg(String reservedKeyStrokeMsg) {
        this.reservedKeyStrokeMsg = (reservedKeyStrokeMsg == null || reservedKeyStrokeMsg.isBlank())
                ? RESERVED_MSG
                : reservedKeyStrokeMsg;
        return this;
    }

    /**
     * Exposes the built-in FieldValidator that we use to validate KeyStroke input.
     * This is here for the unit tests.
     */
    FieldValidator getBuiltInValidator() {
        return fieldValidator;
    }

    /**
     * An internal validator that handles blank values intelligently, and validates
     * the parsability of non-blank values as KeyStrokes. You don't need to add
     * a NonBlankFieldValidator to this FormField! This built-in validator handles
     * it. Just call setAllowBlank(true) if you want to allow blank input.
     */
    private class KeyStrokeFieldValidator implements FieldValidator<KeyStrokeField> {

        @Override
        public ValidationResult validate(KeyStrokeField fieldToValidate) {
            String currentText = textField.getText();

            // If it's blank, then it's valid only if allowBlank is true:
            if (currentText.isBlank()) {
                return allowBlank
                        ? ValidationResult.valid()
                        : ValidationResult.invalid("Keystroke cannot be blank");
            }

            // Let's make sure it's a valid KeyStroke string:
            KeyStroke parsed = KeyStrokeManager.parseKeyStroke(currentText);
            if (parsed == null) {
                return ValidationResult.invalid("Invalid keystroke format");
            }

            // Now, it's only valid if it's not on the reserved list:
            for (KeyStroke reserved : reservedKeyStrokes) {
                if (parsed.equals(reserved)) {
                    return ValidationResult.invalid(reservedKeyStrokeMsg);
                }
            }

            // If we make it here, we're good!
            return ValidationResult.valid();
        }
    }

    // DocumentListener interface stuff below this line --------------------------------
    //
    // Yeah, it's horribly broken, but CoalescingDocumentListener is even worse,
    // and will be marked as deprecated in 2.7 (nuked for all time in 2.8).
    // So, until a better solution appears, we're stuck with DocumentListener.
    // https://github.com/scorbo2/swing-extras/issues/251 for the details.
    //
    // Because we have our own built-in validator, it seems unlikely that callers
    // will add ValueChangedListeners to this field, so the impact of this is maybe
    // not huge. But woe betide you if you need to do that.

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
