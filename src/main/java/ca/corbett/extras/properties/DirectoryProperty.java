package ca.corbett.extras.properties;

import ca.corbett.forms.fields.FileField;
import ca.corbett.forms.fields.FormField;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a field that contains a directory name.
 *
 * @author scorbo2
 * @since 2024-12-30
 */
public class DirectoryProperty extends AbstractProperty {

    private static final Logger logger = Logger.getLogger(DirectoryProperty.class.getName());

    protected File dir;
    protected int columns;
    protected boolean allowBlank;

    public DirectoryProperty(String name, String label) {
        this(name, label, false, null);
    }

    public DirectoryProperty(String name, String label, boolean allowBlank) {
        this(name, label, allowBlank, null);
    }

    public DirectoryProperty(String name, String label, boolean allowBlank, File dir) {
        super(name, label);
        this.dir = dir;
        this.columns = 20;
    }

    public File getDirectory() {
        return dir;
    }

    public DirectoryProperty setDirectory(File dir) {
        this.dir = dir;
        return this;
    }

    public int getColumns() {
        return columns;
    }

    public DirectoryProperty setColumns(int columns) {
        this.columns = columns;
        return this;
    }

    @Override
    public void saveToProps(Properties props) {
        props.setString(fullyQualifiedName + ".dir", (dir == null) ? "" : dir.getAbsolutePath());
        props.setInteger(fullyQualifiedName + ".cols", columns);
    }

    @Override
    public void loadFromProps(Properties props) {
        String str = props.getString(fullyQualifiedName + ".dir", (dir == null) ? "" : dir.getAbsolutePath());
        dir = str.isEmpty() ? null : new File(str);
        columns = props.getInteger(fullyQualifiedName + ".cols", columns);
    }

    @Override
    public FormField generateFormField() {
        FileField field = new FileField(propertyLabel, dir, columns, FileField.SelectionType.ExistingDirectory,
                                        allowBlank);
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
            logger.log(Level.SEVERE, "DirectoryProperty.loadFromFormField: received the wrong field \"{0}\"",
                       field.getIdentifier());
            return;
        }

        dir = ((FileField)field).getFile();
    }

}
