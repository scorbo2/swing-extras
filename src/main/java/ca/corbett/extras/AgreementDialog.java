package ca.corbett.extras;

import ca.corbett.extras.io.KeyStrokeManager;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.Margins;
import ca.corbett.forms.fields.CheckBoxField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.LongTextField;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * A dialog for showing some kind of agreement, license text, terms of use, or some other text
 * that must be agreed to or confirmed before allowing the user to confirm.
 * The dialog cannot be confirmed until the user checks the agreement checkbox, which is disabled by default.
 * The text of the checkbox is configurable, as is the text for the confirm button.
 * <p>
 * The dialog also supports optional overview text that appears at the top of the dialog, above the agreement field.
 * This is useful for providing context as to what the user is agreeing to, or any other relevant information.
 * This overview text can be multi-line if desired, by wrapping the text in html tags and using br tags for line breaks.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 3.0
 */
public class AgreementDialog extends JDialog {

    private static final String DEFAULT_AGREEMENT_TEXT = "I agree to the terms and conditions";
    private static final String DEFAULT_AGREEMENT_ERROR = "You must agree to the terms and conditions to proceed.";

    protected final KeyStrokeManager keyStrokeManager;
    protected FormPanel formPanel;
    protected LabelField overviewLabel;
    protected LongTextField agreementTextField;
    protected CheckBoxField agreementCheckBox;
    protected JButton okButton;
    protected JButton cancelButton;
    private String errorText = DEFAULT_AGREEMENT_ERROR;
    private boolean wasAgreed;

    public AgreementDialog(String title) {
        this(null, title);
    }

    public AgreementDialog(Window owner, String title) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        this.keyStrokeManager = new KeyStrokeManager(this);
        wasAgreed = false;

        formPanel = new FormPanel(Alignment.TOP_CENTER);
        formPanel.setBorderMargin(new Margins(12, 0, 12, 12, 0));

        // FormPanel applies its border margin to the 0th component.
        // This is a problem, because we're deliberately hiding the overview label by default.
        // So, we tell the FormPanel to have a top margin of 0, and then we'll add a spacer
        // panel above it to create the desired top margin. This is pretty hacky,
        // but it works.
        overviewLabel = new LabelField("");
        overviewLabel.setVisible(false);
        formPanel.add(overviewLabel);
        agreementTextField = LongTextField.ofDynamicSizingMultiLine("", 12);
        agreementTextField.setEditable(false); // can't edit agreement text - like it or lump it
        formPanel.add(agreementTextField);

        agreementCheckBox = new CheckBoxField(DEFAULT_AGREEMENT_TEXT, false);
        agreementCheckBox.addValueChangedListener(_ -> okButton.setEnabled(agreementCheckBox.isChecked()));
        formPanel.add(agreementCheckBox);

        setSize(new Dimension(500, 330));
        setLocationRelativeTo(owner);
        setResizable(true);
        setMinimumSize(new Dimension(350, 150));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowCloseListener()); // make sure our cancel path is hit if window is closed

        okButton = new JButton("OK");
        okButton.setEnabled(false); // can only be enabled by checking the agreement checkbox
        cancelButton = new JButton("Cancel");
        okButton.setPreferredSize(new Dimension(100, 24));
        okButton.addActionListener(e -> handleButtonClick(true));
        cancelButton.setPreferredSize(new Dimension(100, 24));
        cancelButton.addActionListener(e -> handleButtonClick(false));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        JPanel spacerPanel = new JPanel();
        spacerPanel.setBorder(null); // set null border to hide the fact we're using a hacky spacer panel
        spacerPanel.setPreferredSize(new Dimension(1, 12));
        formPanel.setBorder(null); // likewise, null border on the form panel to hide the hack

        setLayout(new BorderLayout());
        add(spacerPanel, BorderLayout.NORTH);
        JScrollPane scrollPane = ScrollUtil.buildScrollPane(formPanel);
        scrollPane.setBorder(null); // even the scroll pane has to have a null border, sigh
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            // Configure our keyboard shortcuts when the dialog is shown:
            // (want to avoid doing this in the constructor as it is overridable)
            configureKeyboardShortcuts();
        }
        super.setVisible(visible);
    }

    /**
     * Reports whether the dialog was agreed to or not.
     */
    public boolean wasAgreed() {
        return wasAgreed;
    }

    /**
     * Set the text to use for the agreement checkbox.
     * Defaults to "I agree to the terms and conditions".
     */
    public AgreementDialog setCheckboxText(String text) {
        agreementCheckBox.setCheckBoxText(text);
        return this;
    }

    /**
     * Returns the current text of the agreement checkbox.
     */
    public String getCheckboxText() {
        return agreementCheckBox.getCheckBoxText();
    }

    /**
     * If the user somehow manages to hit "OK" without checking the agreement checkbox,
     * this is the error message that will be shown to them. This shouldn't be possible,
     * as the button is disabled while the checkbox is unchecked, but just in case.
     */
    public AgreementDialog setErrorText(String errorText) {
        this.errorText = errorText;
        return this;
    }

    /**
     * Returns the current error text that will be shown if the user tries to
     * confirm without checking the agreement checkbox.
     */
    public String getErrorText() {
        return errorText;
    }

    /**
     * Optionally, you can specify help text to attach to the agreement field.
     * The default is no help text. If specified, an information icon will appear
     * next to the agreement field, and hovering over it will show the given help text in a tooltip.
     *
     * @param helpText Any help text. Use html tags with br tags for multi-line help text. Null or blank disables help.
     * @return This dialog, for method chaining.
     */
    public AgreementDialog setHelpText(String helpText) {
        agreementTextField.setHelpText(helpText);
        return this;
    }

    /**
     * Sets optional overview text to appear at the top of the dialog, above the agreement field.
     * This is useful for providing context as to what the user is agreeing to, or any other relevant information.
     * By default, no overview text is shown. The form panel will scroll vertically if needed, but you might
     * want to increase the height of the dialog if your overview text is large.
     * <p>
     * <b>Multi-line overview text:</b> you can accomplish this by wrapping the label text in html tags,
     * and using br tags for line breaks.
     * </p>
     *
     * @param overviewText Any overview text. Null or blank values hide the overview label.
     * @return This dialog, for method chaining.
     */
    public AgreementDialog setOverviewText(String overviewText) {
        overviewLabel.setText(overviewText);
        boolean oldVisibility = overviewLabel.isVisible();
        boolean newVisibility = overviewText != null && !overviewText.isBlank();
        overviewLabel.setVisible(newVisibility);
        if (oldVisibility != newVisibility) {
            // If the visibility changed, we need to revalidate and repaint to update the layout:
            formPanel.revalidate();
            formPanel.repaint();
        }
        return this;
    }

    /**
     * Returns the current agreement text.
     *
     * @return The current agreement text.
     */
    public String getHelpText() {
        return agreementTextField.getHelpText();
    }

    /**
     * Sets the text that should appear in the text field when the dialog is first shown. By default, this is blank.
     *
     * @param text the initial text to show in the text field when the dialog is first displayed.
     * @return this dialog, for method chaining.
     */
    public AgreementDialog setAgreementText(String text) {
        if (text == null) {
            // Not necessarily an error - some callers may assume setInitialText(null) means "no text".
            // Our text fields actually handle this without issue.
            // But we invoke length() on it below, so let's avoid NPEs:
            text = "";
        }
        agreementTextField.setText(text);

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
    public AgreementDialog setConfirmLabel(String label) {
        okButton.setText(label);

        // The button was initialized with a hard-coded size which may no longer fit the label.
        // Let's remove the hard-coded size and let the button resize itself to fit the new label:
        okButton.setPreferredSize(null);
        int newWidth = Math.max(okButton.getPreferredSize().width, 100); // enforce a minimum width of 100
        newWidth = Math.min(newWidth, 200); // enforce a maximum width of 200 to prevent it from getting absurdly wide
        okButton.setPreferredSize(new Dimension(newWidth, 24)); // keep the right height
        getContentPane().revalidate();
        getContentPane().repaint();
        return this;
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
            // Shouldn't be possible to get here if the checkbox isn't checked, but let's be safe:
            if (!agreementCheckBox.isChecked()) {
                JOptionPane.showMessageDialog(this, errorText, getTitle(), JOptionPane.INFORMATION_MESSAGE);
                return; // Can't close this dialog until you pass all validation rules (or cancel)
            }
            wasAgreed = true;
            dispose();
            return;
        }

        dispose();
    }

    /**
     * Invoked internally to configure keyboard shortcuts for this dialog.
     * By default, this registers "esc" to trigger the cancel button.
     */
    protected void configureKeyboardShortcuts() {
        // Treat "esc" as a click on the cancel button:
        keyStrokeManager.registerHandler("esc", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleButtonClick(false);
            }
        });
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
