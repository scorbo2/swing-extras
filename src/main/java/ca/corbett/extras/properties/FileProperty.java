package ca.corbett.extras.properties;

import ca.corbett.forms.fields.FileField;
import ca.corbett.forms.fields.FormField;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a property field to allow selection of a file. By default, any file (existing or not)
 * will be allowed. You can specify a FileField.SelectionType if you want to allow only existing
 * or only non-existing files, or you can add a FormFieldGenerationListener to intercept the FormField
 * and add your own FieldValidator to it to only allow files with a certain extension or whatever.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2024-12-08
 */
public class FileProperty extends AbstractProperty {

    private static final Logger logger = Logger.getLogger(FileProperty.class.getName());

    protected boolean allowBlank;
    protected FileField.SelectionType selectionType;
    protected File file;
    protected int columns;

    public FileProperty(String name, String label) {
        this(name, label, false, null);
    }

    public FileProperty(String name, String label, boolean allowBlank) {
        this(name, label, allowBlank, null);
    }

    public FileProperty(String name, String label, FileField.SelectionType selectionType) {
        this(name, label, selectionType, false, null);
    }

    public FileProperty(String name, String label, File file) {
        this(name, label, false, file);
    }

    public FileProperty(String name, String label, FileField.SelectionType selectionType, File file) {
        this(name, label, selectionType, false, file);
    }

    public FileProperty(String name, String label, boolean allowBlank, File file) {
        this(name, label, FileField.SelectionType.AnyFile, allowBlank, file);
    }

    public FileProperty(String name, String label, FileField.SelectionType selectionType, boolean allowBlank, File file) {
        super(name, label);
        this.allowBlank = allowBlank;
        this.file = file;
        this.columns = 20;
        this.selectionType = selectionType;
    }

    public FileProperty setAllowBlank(boolean allow) {
        allowBlank = allow;
        return this;
    }

    public boolean isAllowBlank() {
        return allowBlank;
    }

    public FileField.SelectionType getSelectionType() {
        return selectionType;
    }

    public void setSelectionType(FileField.SelectionType selectionType) {
        this.selectionType = selectionType;
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
        props.setString(fullyQualifiedName + ".selectionType", selectionType.name());
    }

    @Override
    public void loadFromProps(Properties props) {
        columns = props.getInteger(fullyQualifiedName + ".cols", columns);
        allowBlank = props.getBoolean(fullyQualifiedName + ".allowBlank", allowBlank);
        String str = props.getString(fullyQualifiedName + ".file", (file == null) ? "" : file.getAbsolutePath());
        file = str.isEmpty() ? null : new File(str);
        String st = props.getString(fullyQualifiedName + ".selectionType", FileField.SelectionType.AnyFile.name());
        try {
            selectionType = FileField.SelectionType.valueOf(st);
        }
        catch (IllegalArgumentException ignored) {
            logger.warning("FileProperty.loadFromProps: unrecognized selection type \"" + st + "\" ignored");
            selectionType = FileField.SelectionType.AnyFile; // default if input is garbage
        }
    }

    @Override
    protected FormField generateFormFieldImpl() {
        return new FileField(propertyLabel, file, columns, selectionType, allowBlank);
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
