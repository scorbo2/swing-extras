package ca.corbett.forms.fields;

import ca.corbett.extras.CoalescingDocumentListener;
import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.FileMustBeCreatableValidator;
import ca.corbett.forms.validators.FileMustBeReadableValidator;
import ca.corbett.forms.validators.FileMustBeSpecifiedValidator;
import ca.corbett.forms.validators.FileMustBeWritableValidator;
import ca.corbett.forms.validators.FileMustExistValidator;
import ca.corbett.forms.validators.FileMustNotExistValidator;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A FormField for choosing a single directory or file.
 * For directories, the chosen directory must exist.
 * For files, you can specify whether to browse for files that must exist,
 * or for files that must NOT exist (for example, for a save dialog).
 * <p>
 * The underlying JFileChooser is not directly exposed, but there are some
 * convenience methods here, like setFileFilter(), that can be used to
 * customize it.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2019-11-24
 */
public final class FileField extends FormField {

    /**
     * Currently supported selection modes for this field.
     */
    public enum SelectionType {
        /**
         * Browse for a single directory, which must exist and be readable and writable. *
         */
        ExistingDirectory,
        /**
         * Browse for a single file, which must exist and be readable and writable. *
         */
        ExistingFile,
        /**
         * Browse for a single file which must NOT already exist (eg, for a save dialog). *
         */
        NonExistingFile
    }

    private final List<FieldValidator<? extends FormField>> userAddedValidators = new ArrayList<>();
    private final JTextField textField;
    private final JFileChooser fileChooser;
    private final JButton chooseButton;
    private SelectionType selectionType;
    private boolean isAllowBlank;

    /**
     * Creates a FileField with the given parameters.
     *
     * @param label         The label to use for this field.
     * @param initialValue  The initial File to display in the field.
     * @param cols          The number of columns to use for the text box.
     * @param selectionType See SelectionType for details.
     */
    public FileField(String label, File initialValue, int cols, SelectionType selectionType) {
        this(label, initialValue, cols, selectionType, false);
    }

    /**
     * Creates a FileField with the given parameters.
     *
     * @param label         The label to use for this field.
     * @param initialValue  The initial File to display in the field.
     * @param cols          The number of columns to use for the text box.
     * @param selectionType See SelectionType for details.
     * @param allowBlank    whether to allow blank values in the field.
     */
    public FileField(String label, File initialValue, int cols, SelectionType selectionType, boolean allowBlank) {
        fieldLabel.setText(label);
        textField = new JTextField(initialValue == null ? "" : initialValue.getAbsolutePath());
        textField.setColumns(cols);
        textField.getDocument()
                 .addDocumentListener(new CoalescingDocumentListener(textField, e -> fireValueChangedEvent()));
        fileChooser = new JFileChooser(initialValue);
        fileChooser.setMultiSelectionEnabled(false);
        setSelectionType(selectionType, allowBlank);
        chooseButton = new JButton("Choose...");
        chooseButton.setFont(DEFAULT_FONT);
        JPanel dirPanel = new JPanel();
        dirPanel.setLayout(new BoxLayout(dirPanel, BoxLayout.X_AXIS));
        dirPanel.add(textField);
        JLabel spacerLabel = new JLabel(" ");
        dirPanel.add(spacerLabel);
        chooseButton.setPreferredSize(new Dimension(105, 22));
        dirPanel.add(chooseButton);
        fieldComponent = dirPanel;
    }

    /**
     * Sets the SelectionType for this FieldField.
     * <ul>
     * <li>ExistingDirectory: You can browse for a directory, which must exist and be
     * readable/writable.
     * <li>ExistingFile: You can browse for a single file, which must exist and be readable/writable.
     * <li>NonExistingFile: You can browse for a single file, which must not exist (eg. for a save dialog).
     * </ul>
     *
     * @param selectionType A SelectionType value as explained above.
     */
    public FileField setSelectionType(SelectionType selectionType) {
        setSelectionType(selectionType, false);
        return this;
    }

    /**
     * Sets the SelectionType for this FileField.
     * <ul>
     * <li>ExistingDirectory: You can browse for a directory, which must exist and be
     * readable/writable.
     * <li>ExistingFile: You can browse for a single file, which must exist and be readable/writable.
     * <li>NonExistingFile: You can browse for a single file, which must not exist.
     * </ul>
     *
     * @param allowBlankValues If false, the text field cannot be blanked out (i.e. no file specified at all).
     * @param selectionType    A SelectionType value as explained above.
     */
    public FileField setSelectionType(SelectionType selectionType, boolean allowBlankValues) {
        this.selectionType = selectionType;
        this.isAllowBlank = allowBlankValues;
        if (selectionType == SelectionType.NonExistingFile || selectionType == SelectionType.ExistingFile) {
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        }
        else {
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
        resetValidators();
        return this;
    }

    /**
     * Returns the SelectionType of this FileField.
     *
     * @return The current SelectionType of this field. Use setSelectionType to modify.
     */
    public SelectionType getSelectionType() {
        return selectionType;
    }

    /**
     * Returns whether or not blank values are permitted in this field. If true,
     * and no value is specified in the text field, then getFile() will return null.
     * If false, then the field will throw a validation error if no value is specified.
     *
     * @return Whether or not this field considers a blank value to be valid.
     */
    public boolean isAllowBlankValues() {
        return isAllowBlank;
    }

    /**
     * Specifies whether blank values should be allowed in the text field. If false, a
     * non-blank validator will be added automatically.
     */
    public FileField setAllowBlankValues(boolean allow) {
        isAllowBlank = allow;
        resetValidators();
        return this;
    }

    /**
     * Overridden so we can enable/disable our choose button also.
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        chooseButton.setEnabled(enabled);
        textField.setEnabled(enabled);
    }

    /**
     * Allows direct access to the underlying JTextField.
     */
    public JTextField getTextField() {
        return textField;
    }

    /**
     * Returns the currently selected File from this field.
     *
     * @return A File object.
     */
    public File getFile() {
        String path = textField.getText().trim();
        return path.isEmpty() ? null : new File(path);
    }

    /**
     * Sets the currently selected File for this field.
     *
     * @param file The File to select.
     */
    public FileField setFile(File file) {
        clearValidationResults();
        if (file == null) {
            textField.setText("");
        }
        else {
            textField.setText(file.getAbsolutePath());
        }
        return this;
    }

    /**
     * Sets a FileFilter to use with the JFileChooser.
     *
     * @param filter An optional FileFilter to apply.
     */
    public FileField setFileFilter(FileFilter filter) {
        fileChooser.setFileFilter(filter);
        return this;
    }

    public FileFilter getFileFilter() {
        return fileChooser.getFileFilter();
    }

    @Override
    public void preRender(JPanel container) {
        fieldComponent.setBackground(container.getBackground());

        // Remove any previous action listener added by us:
        for (ActionListener listener : chooseButton.getActionListeners()) {
            if (listener instanceof ButtonActionListener) {
                chooseButton.removeActionListener(listener);
            }
        }

        // Add a new one - the action listener needs the parent container to show the chooser dialog:
        chooseButton.addActionListener(new ButtonActionListener(this, container));
    }

    private static class ButtonActionListener implements ActionListener {
        private final FileField ownerField;
        private final JPanel container;

        public ButtonActionListener(FileField ownerField, JPanel container) {
            this.ownerField = ownerField;
            this.container = container;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int result = ownerField.fileChooser.showDialog(container, "Choose");
            if (result == JFileChooser.APPROVE_OPTION) {
                ownerField.textField.setText(ownerField.fileChooser.getSelectedFile().getAbsolutePath());
            }
        }
    }

    /**
     * Overridden so that it acts only on our "user-added" validators list. We need to keep these
     * separate from the validators that we add internally depending on selectionType.
     */
    @Override
    public FormField addFieldValidator(FieldValidator<? extends FormField> validator) {
        userAddedValidators.add(validator);
        resetValidators();
        return this;
    }

    /**
     * Overridden so that it acts only on our "user-added" validators list. We need to keep these
     * separate from the validators that we add internally depending on selectionType.
     */
    @Override
    public void removeFieldValidator(FieldValidator<FormField> validator) {
        userAddedValidators.remove(validator);
        resetValidators();
    }

    /**
     * Overridden so that it acts only on our "user-added" validators list. We need to keep these
     * separate from the validators that we add internally depending on selectionType.
     */
    @Override
    public void removeAllFieldValidators() {
        userAddedValidators.clear();
        resetValidators();
    }

    /**
     * Invoked internally when any change is made that might affect our FieldValidator list.
     * We maintain two separate lists - those validators that we add internally based on
     * allowable selection type, and also user-supplied ones that were added by callers.
     * This method will carefully rebuild the list so that we can change our internal
     * validators without affecting the user-supplied ones.
     */
    private void resetValidators() {
        // clear ALL validators - both our built-in ones and the user-added ones.
        fieldValidators.clear();

        // Figure out which validators we need based on our selectionType
        if (selectionType == SelectionType.ExistingFile || selectionType == SelectionType.ExistingDirectory) {
            fieldValidators.add(new FileMustExistValidator());
            fieldValidators.add(new FileMustBeReadableValidator());
            fieldValidators.add(new FileMustBeWritableValidator());
        }
        else {
            fieldValidators.add(new FileMustNotExistValidator());
            fieldValidators.add(new FileMustBeCreatableValidator());
        }

        // Also add a no-blank validator if required:
        if (!isAllowBlank) {
            fieldValidators.add(new FileMustBeSpecifiedValidator());
        }

        // now add the user-added validators back
        fieldValidators.addAll(userAddedValidators);
    }
}
