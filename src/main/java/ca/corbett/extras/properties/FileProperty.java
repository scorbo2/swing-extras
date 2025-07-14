package ca.corbett.extras.properties;

import ca.corbett.forms.fields.FileField;
import ca.corbett.forms.fields.FormField;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a property field that contains a file name. Single-select only.
 * You can configure the field to allow blank values or not - this does not affect
 * this class, but rather is used when generating a FormField to allow selection.
 *
 * @author scorbo2
 * @since 2024-12-08
 */
public class FileProperty extends AbstractProperty {

    private static final Logger logger = Logger.getLogger(FileProperty.class.getName());

    protected boolean allowBlank;
    protected File file;
    protected int columns;

    public FileProperty(String name, String label) {
        this(name, label, false, null);
    }

    public FileProperty(String name, String label, boolean allowBlank) {
        this(name, label, allowBlank, null);
    }

    public FileProperty(String name, String label, File file) {
        this(name, label, false, file);
    }

    public FileProperty(String name, String label, boolean allowBlank, File file) {
        super(name, label);
        this.allowBlank = allowBlank;
        this.file = file;
        this.columns = 20;
    }

    public FileProperty setAllowBlank(boolean allow) {
        allowBlank = allow;
        return this;
    }

    public boolean isAllowBlank() {
        return allowBlank;
    }

    public File getFile() {
        return file;
    }

    public FileProperty setFile(File file) {
        this.file = file;
        return this;
    }

    public int getColumns() {
        return columns;
    }

    public FileProperty setColumns(int columns) {
        this.columns = columns;
        return this;
    }

    @Override
    public void saveToProps(Properties props) {
        props.setBoolean(fullyQualifiedName + ".allowBlank", allowBlank);
        props.setString(fullyQualifiedName + ".file", (file == null) ? "" : file.getAbsolutePath());
        props.setInteger(fullyQualifiedName + ".cols", columns);
    }

    @Override
    public void loadFromProps(Properties props) {
        columns = props.getInteger(fullyQualifiedName + ".cols", columns);
        allowBlank = props.getBoolean(fullyQualifiedName + ".allowBlank", allowBlank);
        String str = props.getString(fullyQualifiedName + ".file", (file == null) ? "" : file.getAbsolutePath());
        file = str.isEmpty() ? null : new File(str);
    }

    @Override
    public FormField generateFormField() {
        FileField field = new FileField(propertyLabel, file, columns,
                                        allowBlank
                                                ? ca.corbett.forms.fields.FileField.SelectionType.NonExistingFile
                                                : ca.corbett.forms.fields.FileField.SelectionType.ExistingFile);
        field.setIdentifier(fullyQualifiedName);
        field.setEnabled(!isReadOnly);
        field.setHelpText(helpText);
        return field;
    }

    @Override
    public void loadFromFormField(FormField field) {
        if (field.getIdentifier() == null
                || !field.getIdentifier().equals(fullyQualifiedName)
                || !(field instanceof FileField)) {
            logger.log(Level.SEVERE, "FileProperty.loadFromFormField: received the wrong field \"{0}\"",
                       field.getIdentifier());
            return;
        }

        file = ((FileField)field).getFile();
    }

}
