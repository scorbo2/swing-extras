package ca.corbett.forms.fields;

import ca.corbett.forms.SwingFormsResources;
import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.NonBlankFieldValidator;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;

/**
 * A FormField for password input. This field is very similar to ShortTextField,
 * with the exception that characters typed into the field are masked (with asterisk
 * by default, though this is customizable). There is an optional "show password"
 * button that can be used to toggle visibility of the typed characters.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.5
 */
public class PasswordField extends FormField implements DocumentListener {

    public static final char DEFAULT_ECHO_CHAR = '*';

    protected final JPasswordField textField;
    protected final JButton showPasswordButton;
    protected final JButton copyButton;
    protected char echoChar;

    /**
     * Creates a new PasswordField with the specified column width. By default, the
     * echo char will be an asterisk, but you can control this with setEchoChar.
     * Buttons are presented to allow toggling visibility of the password, and also
     * to copy the password to the clipboard. You can hide these buttons via
     * the setAllowShowPassword() and setAllowClipboard() methods. Setting them
     * to false will hide the button in question.
     */
    public PasswordField(String label, int cols) {
        fieldLabel.setText(label);
        textField = new JPasswordField(cols);
        setEchoChar(DEFAULT_ECHO_CHAR);
        showPasswordButton = new JButton(SwingFormsResources.getHiddenIcon(ICON_SIZE));
        showPasswordButton.setPreferredSize(new Dimension(26, 26));
        showPasswordButton.addActionListener(e -> toggleShowPassword());
        showPasswordButton.setToolTipText("Reveal/hide field contents");
        copyButton = new JButton(SwingFormsResources.getCopyIcon(ICON_SIZE));
        copyButton.setPreferredSize(new Dimension(26, 26));
        copyButton.addActionListener(e -> copyPassword());
        copyButton.setToolTipText("Copy password to clipboard");
        textField.getDocument().addDocumentListener(this);

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.add(textField, BorderLayout.WEST);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonPanel.add(showPasswordButton);
        buttonPanel.add(copyButton);
        wrapperPanel.add(buttonPanel, BorderLayout.EAST);

        fieldComponent = wrapperPanel;
    }

    /**
     * Overridden so we can disable all our child components when setEnabled is invoked.
     * We could leave the show password button and the copy to clipboard button enabled
     * when disabling the field, as they can't be used to change the field contents,
     * but it might be a bit weird to have enabled components on a disabled field,
     * so here we will enable or disable all at once.
     */
    @Override
    public FormField setEnabled(boolean isEnabled) {
        super.setEnabled(isEnabled);
        textField.setEnabled(isEnabled);
        showPasswordButton.setEnabled(isEnabled);
        copyButton.setEnabled(isEnabled);
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
    public PasswordField setAllowBlank(boolean allow) {
        if (allow) {
            removeNonBlankValidatorIfPresent();
        }
        else {
            addNonBlankValidatorIfNotPresent();
        }
        return this;
    }

    /**
     * Controls the visibility of the "show password" toggle button. If allowed,
     * the user can hit that toggle button to reveal the hidden text in the field,
     * and to hide it again.
     */
    public PasswordField setAllowShowPassword(boolean allow) {
        showPasswordButton.setVisible(allow);
        return this;
    }

    /**
     * Reports whether the "show password" toggle button is visible.
     */
    public boolean isAllowShowPassword() {
        return showPasswordButton.isVisible();
    }

    /**
     * Controls the visibility of the "copy to clipboard" button.
     */
    public PasswordField setAllowClipboard(boolean allow) {
        copyButton.setVisible(allow);
        return this;
    }

    /**
     * Reports whether the "copy to clipboard" button is visible.
     */
    public boolean isAllowClipboard() {
        return copyButton.isVisible();
    }

    /**
     * By default, characters typed into the password field will be shown as an asterisk.
     * But, you can set some other character to be used for that purpose.
     * <p>
     * <b>Warning:</b> setEchoChar((char)0) will disable hiding of the password. It's better
     * to use the revealPassword() and hidePassword() methods for this purpose instead,
     * as it allows you to toggle password visibility easily.
     * </p>
     */
    public PasswordField setEchoChar(char c) {
        echoChar = c;
        textField.setEchoChar(echoChar);
        return this;
    }

    /**
     * Returns the character that is used to mask user input in this field. By default,
     * this is an asterisk, but it can be controlled via setEchoChar.
     * <p>
     * <B>Note:</B> this method will return the configured echo char, even if the
     * user has hit the "reveal password" button. If you wish to check if the
     * password is currently revealed, use the isPasswordRevealed() method.
     * </p>
     */
    public char getEchoChar() {
        return echoChar;
    }

    /**
     * Programmatically reveals the password, without changing the currently configured echo character.
     * You can invoke hidePassword() to re-hide the password after invoking this method.
     * Does nothing if the password is already revealed.
     */
    public void revealPassword() {
        textField.setEchoChar((char)0);
        showPasswordButton.setIcon(SwingFormsResources.getRevealedIcon(ICON_SIZE));
    }

    /**
     * Programmatically hides the password, if it were previously revealed via the revealPassword() method,
     * or via the user clicking on the show/hide password toggle button. Does nothing if the password
     * was already hidden.
     * <p>
     * <b>Note:</b> If you have previously invoked setEchoChar((char)0) to manually reveal the password,
     * this method will reset the configured echo char back to the default (asterisk).
     * </p>
     */
    public void hidePassword() {
        // Check for weirdness...
        // If explicitly asked to hide the password but our echo char is 0, override it:
        if (echoChar == 0) {
            echoChar = DEFAULT_ECHO_CHAR;
        }
        textField.setEchoChar(echoChar);
        showPasswordButton.setIcon(SwingFormsResources.getHiddenIcon(ICON_SIZE));
    }

    /**
     * Indicates whether the password is currently revealed (either by an explicit call to revealPassword(),
     * or by the user clicking the show/hide password toggle button).
     */
    public boolean isPasswordRevealed() {
        return textField.getEchoChar() == 0;
    }

    /**
     * Sets the text for the password field.
     */
    public PasswordField setPassword(String password) {
        if (password == null) {
            password = ""; // if null, assume empty string
        }
        if (password.equals(getPassword())) {
            return this; // reject no-op changes
        }
        textField.setText(password);
        return this;
    }

    /**
     * Returns the current text entered in the field.
     */
    public String getPassword() {
        return new String(textField.getPassword());
    }

    /**
     * Invoked internally to toggle the current revealed/hidden state of the password text.
     */
    protected void toggleShowPassword() {
        if (isPasswordRevealed()) {
            hidePassword();
        }
        else {
            revealPassword();
        }
    }

    /**
     * Invoked internally to copy the current password text to the clipboard.
     */
    protected void copyPassword() {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(getPassword()), null);
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
