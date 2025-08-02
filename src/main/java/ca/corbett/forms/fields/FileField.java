package ca.corbett.forms.fields;

import ca.corbett.forms.FormPanel;
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
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.File;

/**
 * A FormField for choosing a single directory or file.
 * For directories, the chosen directory must exist.
 * For files, you can specify whether to browse for files that must exist,
 * or for files that must NOT exist (for example, for a save dialog).
 *
 * @author scorbo2
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

    private final JTextField textField;
    private final JFileChooser fileChooser;
    private JButton chooseButton;
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
        fieldLabel = new JLabel(label);
        fieldLabel.setFont(fieldLabelFont);
        textField = new JTextField(initialValue == null ? "" : initialValue.getAbsolutePath());
        fieldComponent = textField;
        textField.setColumns(cols);
        fileChooser = new JFileChooser(initialValue);
        fileChooser.setMultiSelectionEnabled(false);
        setSelectionType(selectionType, allowBlank);
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
    public void setSelectionType(SelectionType selectionType) {
        setSelectionType(selectionType, false);
    }

    /**
     * Sets the SelectionType for this FileField.
     * <ul>
     * <li>ExistingDirectory: You can browse for a directory, which must exist and be
     * readable/writable.
     * <li>ExistingFile: You can browse for a single file, which must exist and be readable/writable.
     * <li>NonExistingFile: You can browse for a single file, which must not exist.
     * </ul>
     * Note: if you call this method after you have added custom field validators with the
     * addFieldValidator() method, you must re-add your custom validators as the list will
     * have been cleared and rebuilt.
     *
     * @param allowBlankValues If false, the text field cannot be blanked out (i.e. no file specified at all).
     * @param selectionType    A SelectionType value as explained above.
     */
    public void setSelectionType(SelectionType selectionType, boolean allowBlankValues) {
        this.selectionType = selectionType;
        this.isAllowBlank = allowBlankValues;
        if (selectionType == SelectionType.NonExistingFile || selectionType == SelectionType.ExistingFile) {
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        }
        else {
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
        if (selectionType == SelectionType.ExistingFile || selectionType == SelectionType.ExistingDirectory) {
            removeAllFieldValidators();
            addFieldValidator(new FileMustExistValidator());
            addFieldValidator(new FileMustBeReadableValidator());
            addFieldValidator(new FileMustBeWritableValidator());

        }
        else {
            removeAllFieldValidators();
            addFieldValidator(new FileMustNotExistValidator());
            addFieldValidator(new FileMustBeCreatableValidator());
        }

        if (!allowBlankValues) {
            addFieldValidator(new FileMustBeSpecifiedValidator());
        }
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
     * Overridden so we can show/hide our choose button also.
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (chooseButton != null) {
            chooseButton.setVisible(visible);
        }
    }

    /**
     * Overridden so we can enable/disable our choose button also.
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (chooseButton != null) {
            chooseButton.setEnabled(enabled);
        }
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
    public void setFile(File file) {
        clearValidationResults();
        if (file == null) {
            textField.setText("");
        }
        else {
            textField.setText(file.getAbsolutePath());
        }
    }

    /**
     * Sets a FileFilter to use with the JFileChooser.
     *
     * @param filter An optional FileFilter to apply.
     */
    public void setFileFilter(FileFilter filter) {
        fileChooser.setFileFilter(filter);
    }

    /**
     * Renders this field into the given container.
     *
     * @param container   The containing form panel.
     * @param constraints The GridBagConstraints to use.
     */
    @Override
    public void render(JPanel container, GridBagConstraints constraints) {
        constraints.insets = new Insets(topMargin, leftMargin, bottomMargin, componentSpacing);
        constraints.gridy++;
        constraints.gridx = FormPanel.LABEL_COLUMN;
        fieldLabel.setFont(fieldLabelFont);
        container.add(fieldLabel, constraints);

        // UTIL-147: create new button every time render() is invoked to avoid duplicate action listeners
        chooseButton = new JButton("Choose...");
        chooseButton.setFont(fieldLabelFont);
        chooseButton.setEnabled(isEnabled);

        constraints.gridx = FormPanel.CONTROL_COLUMN;
        JPanel dirPanel = new JPanel();
        dirPanel.setBackground(container.getBackground());
        dirPanel.setLayout(new BoxLayout(dirPanel, BoxLayout.X_AXIS));
        dirPanel.add(textField);
        JLabel spacerLabel = new JLabel(" ");
        dirPanel.add(spacerLabel);
        chooseButton.setPreferredSize(new Dimension(105, 22));
        dirPanel.add(chooseButton);
        final JPanel thisPanel = container;
        chooseButton.addActionListener(e -> {
            int result = fileChooser.showDialog(thisPanel, "Choose");
            if (result == JFileChooser.APPROVE_OPTION) {
                textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
                fireValueChangedEvent();
            }
        });
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(topMargin, componentSpacing, bottomMargin, componentSpacing);
        container.add(dirPanel, constraints);
    }

}
