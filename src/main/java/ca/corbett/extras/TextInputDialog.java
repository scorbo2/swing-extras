package ca.corbett.extras;

import ca.corbett.extras.io.KeyStrokeManager;
import ca.corbett.extras.properties.PropertiesDialog;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.LongTextField;
import ca.corbett.forms.fields.ShortTextField;
import ca.corbett.forms.validators.FieldValidator;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Provides an easy way to get text input from the user, very similar to JOptionPane's showInputDialog method,
 * but with optional configurable validation on the allowed input.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
 */
public class TextInputDialog extends JDialog {

    private static final int DEFAULT_COLS = 20;
    private static final int DEFAULT_ROWS = 10;
    private static final String DEFAULT_CONFIRM_LABEL = "OK";
    private static final String DEFAULT_PROMPT = "Input:";

    public enum InputType {
        SingleLine, MultiLine
    }

    protected final InputType inputType;
    protected final KeyStrokeManager keyStrokeManager;
    protected FormPanel formPanel;
    protected ShortTextField shortTextField;
    protected LongTextField longTextField;
    protected JButton okButton;
    protected JButton cancelButton;
    protected String result;

    public TextInputDialog() {
        this(null, "Input", InputType.SingleLine);
    }

    public TextInputDialog(String title) {
        this(null, title, InputType.SingleLine);
    }

    public TextInputDialog(String title, InputType inputType) {
        this(null, title, inputType);
    }

    public TextInputDialog(Window owner, String title) {
        this(owner, title, InputType.SingleLine);
    }

    public TextInputDialog(Window owner, String title, InputType inputType) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        this.inputType = inputType;
        this.keyStrokeManager = new KeyStrokeManager(this);

        formPanel = new FormPanel(Alignment.TOP_CENTER);
        formPanel.setBorderMargin(12);
        shortTextField = new ShortTextField(DEFAULT_PROMPT, getCols());
        longTextField = LongTextField.ofDynamicSizingMultiLine(DEFAULT_PROMPT, getRows());
        formPanel.add(inputType == InputType.SingleLine ? shortTextField : longTextField);
        Dimension dim = switch (inputType) {
            case SingleLine -> new Dimension(350, 150);
            case MultiLine -> new Dimension(400, 300);
        };
        setSize(dim);
        setResizable(inputType == InputType.MultiLine);
        setMinimumSize(new Dimension(350, 150));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowCloseListener()); // make sure our cancel path is hit if window is closed

        // Our text area only resizes horizontally, not vertically, so
        // I want to set a maximum height on the dialog to prevent pointless
        // vertical resizes. However, in my testing on linux, this
        // maximum size seems to be utterly ignored.
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 350));

        okButton = new JButton(DEFAULT_CONFIRM_LABEL);
        cancelButton = new JButton("Cancel");
        okButton.setPreferredSize(new Dimension(100, 24));
        okButton.addActionListener(e -> handleButtonClick(true));
        cancelButton.setPreferredSize(new Dimension(100, 24));
        cancelButton.addActionListener(e -> handleButtonClick(false));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        setLayout(new BorderLayout());
        add(PropertiesDialog.buildScrollPane(formPanel), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        if (owner != null) {
            setLocationRelativeTo(owner);
        }
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            // Ensure the text field gets focus when the dialog is opened:
            if (inputType == InputType.SingleLine) {
                shortTextField.getTextField().requestFocusInWindow();
            }
            else {
                longTextField.getTextArea().requestFocusInWindow();
            }

            // Configure our keyboard shortcuts when the dialog is shown:
            // (want to avoid doing this in the constructor as it is overridable)
            configureKeyboardShortcuts();
        }
        super.setVisible(visible);
    }

    /**
     * Returns the text input by the user, or null if the user canceled.
     *
     * @return The user-entered text, or null if cancel was selected.
     */
    public String getResult() {
        return result;
    }

    /**
     * Static convenience method to show a TextInputDialog and return the result in one line of code.
     *
     * @param owner      the parent window to center this dialog on (can be null)
     * @param title      the title to show on the dialog
     * @param inputType  the type of input to allow (single-line or multi-line)
     * @param allowBlank whether to allow blank values (true to allow, false to disallow)
     * @param validators optional FieldValidators to apply to the input (can be empty)
     * @return the text input by the user, or null if the user canceled or closed the dialog manually.
     */
    public static String showDialog(Window owner, String title, InputType inputType, boolean allowBlank,
                                    FieldValidator<? extends FormField>... validators) {
        TextInputDialog dialog = new TextInputDialog(owner, title, inputType);
        dialog.setAllowBlank(allowBlank);
        for (FieldValidator<? extends FormField> validator : validators) {
            dialog.addValidator(validator);
        }
        dialog.setVisible(true);
        return dialog.getResult();
    }

    /**
     * Decides whether blank values should be allowed in the text field.
     * Blank values are allowed by default.
     *
     * @param allow true to allow blank values, false to disallow them.
     * @return this dialog, for method chaining.
     */
    public TextInputDialog setAllowBlank(boolean allow) {
        shortTextField.setAllowBlank(allow);
        longTextField.setAllowBlank(allow);
        return this;
    }

    /**
     * Reports whether blank values are allowed in the text field.
     *
     * @return true if blank values are allowed, false otherwise.
     */
    public boolean isAllowBlank() {
        return shortTextField.isAllowBlank();
    }

    /**
     * Sets the text that should appear in the text field when the dialog is first shown. By default, this is blank.
     *
     * @param text the initial text to show in the text field when the dialog is first displayed.
     * @return this dialog, for method chaining.
     */
    public TextInputDialog setInitialText(String text) {
        if (text == null) {
            // Not necessarily an error - some callers may assume setInitialText(null) means "no text".
            // Our text fields actually handle this without issue.
            // But we invoke length() on it below, so let's avoid NPEs:
            text = "";
        }
        shortTextField.setText(text);
        longTextField.setText(text);

        // Preselect the text in each component so that typing will immediately replace it:
        JTextComponent textComponent = inputType == InputType.SingleLine
                ? shortTextField.getTextField()
                : longTextField.getTextArea();
        textComponent.setCaretPosition(0);
        textComponent.moveCaretPosition(text.length());

        return this;
    }

    /**
     * Adds a FieldValidator to the text field, allowing whatever custom
     * validation rules you want to enforce on the input. You can add as many validators as you like,
     * and they will all be applied when the user clicks the confirm button. If any validator fails,
     * the dialog will remain open and will show validation messages to the user.
     * <p>
     *     The given validator can be typed to any subclass of FormField.
     *     This lets you use some of the built-in text validators in swing-extras,
     *     from the ca.corbett.forms.validators package, which are typed to ShortTextField or LongTextField.
     * </p>
     *
     * @param validator the FieldValidator to add to the text field.
     * @return this dialog, for method chaining.
     */
    public TextInputDialog addValidator(FieldValidator<? extends FormField> validator) {
        shortTextField.addFieldValidator(validator);
        longTextField.addFieldValidator(validator);
        return this;
    }

    /**
     * Removes the given FieldValidator from the text field.
     *
     * @param validator the FieldValidator to remove from the text field.
     * @return this dialog, for method chaining.
     */
    public TextInputDialog removeValidator(FieldValidator<FormField> validator) {
        shortTextField.removeFieldValidator(validator);
        longTextField.removeFieldValidator(validator);
        return this;
    }

    /**
     * Returns the label used on the confirm button (defaults to "OK").
     *
     * @return the label used on the confirm button.
     */
    public String getConfirmLabel() {
        return okButton.getText();
    }

    /**
     * Changes the label used on the confirm button (defaults to "OK").
     *
     * @param label the new label to use on the confirm button.
     * @return this dialog, for method chaining.
     */
    public TextInputDialog setConfirmLabel(String label) {
        okButton.setText(label);
        return this;
    }

    public String getPrompt() {
        return shortTextField.getFieldLabel().getText();
    }

    public TextInputDialog setPrompt(String prompt) {
        shortTextField.getFieldLabel().setText(prompt);
        longTextField.getFieldLabel().setText(prompt);

        // In single-line mode, the dialog is not resizable, so setting a long prompt
        // string might cause display issues. Let's expand the dialog if needed
        // so the prompt and our text box still fit within it:
        if (inputType == InputType.SingleLine && prompt.length() > 6) {
            int extraWidth = (prompt.length() - 6) * 7; // Estimate 7 pixels per character beyond the first 6
            int newWidth = getWidth() + extraWidth;
            setSize(new Dimension(newWidth, getHeight()));
        }

        return this;
    }

    /**
     * If InputType is SingleLine, this controls the column width of the text field.
     * If InputType is MultiLine, this value is ignored.
     * Due to the way our FormPanel is created, this is not directly configurable.
     * However, you can extend this class and override this method if you
     * really want a different width for the text field.
     *
     * @return the column width of the text field (defaults to 20).
     */
    protected int getCols() {
        return DEFAULT_COLS;
    }

    /**
     * If InputType is SingleLine, this value is ignored.
     * If InputType is MultiLine, this controls the row height of the text area.
     * Note that this merely controls the initial height. In MultiLine mode,
     * the dialog is freely resizable, and the text area will resize as needed.
     *
     * @return the row height of the text area.
     */
    protected int getRows() {
        return DEFAULT_ROWS;
    }

    /**
     * Invoked internally to close the dialog.
     * If the confirm button was clicked, this method first checks if the form is valid according to all validators.
     * If the form is not valid, it will not close and will instead show validation messages to the user.
     * If the form is valid, or if the cancel button was clicked, the dialog will close.
     *
     * @param isOkButton true if the confirm button was clicked, false if the cancel button was clicked.
     */
    protected void handleButtonClick(boolean isOkButton) {
        if (isOkButton) {
            if (!formPanel.isFormValid()) {
                return; // Can't close this dialog until you pass all validation rules (or cancel)
            }
            result = inputType == InputType.SingleLine ? shortTextField.getText() : longTextField.getText();
            dispose();
            return;
        }

        result = null; // user canceled
        dispose();
    }

    /**
     * Invoked internally to configure keyboard shortcuts for this dialog.
     * By default, this registers "esc" to trigger the cancel button,
     * and if in single-line mode, it also registers "enter" to trigger the confirm button.
     */
    protected void configureKeyboardShortcuts() {
        // Treat "esc" as a click on the cancel button:
        keyStrokeManager.registerHandler("esc", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleButtonClick(false);
            }
        });

        // If in single-line mode, hijack "enter" to trigger the confirm button:
        // (DON'T do this in multi-line mode, because that would be really painful for typing stuff)
        if (inputType == InputType.SingleLine) {
            keyStrokeManager.registerHandler("enter", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleButtonClick(true);
                }
            });
        }
    }

    /**
     * Listens for window close events and treats them the same way we handle "cancel".
     * This ensures our cancel path is hit even if the user manually closes our dialog.
     */
    protected class WindowCloseListener extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            handleButtonClick(false); // treat closing the dialog as canceling
            // make sure to clean up our keystrokes even if the user closes the dialog manually
            keyStrokeManager.dispose(); // idempotent, we can call this safely from multiple locations
        }

        @Override
        public void windowClosed(WindowEvent e) {
            // make sure to clean up our keystrokes even if the user closes the dialog manually
            keyStrokeManager.dispose(); // idempotent, we can call this safely from multiple locations
        }
    }
}
