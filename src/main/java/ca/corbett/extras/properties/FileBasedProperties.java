package ca.corbett.extras.properties;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Extends the Properties class to provide for saving and loading of properties to and from
 * a flat file on disk.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2022-05-10
 */
public final class FileBasedProperties extends Properties {

    private static final Logger logger = Logger.getLogger(FileBasedProperties.class.getName());

    private boolean eagerSave;
    private final File file;
    private String commentHeader;

    /**
     * Creates a new, empty FileBasedProperties pointed at the given File object.
     * No actual disk i/o will be undertaken until save() or load() are invoked.
     *
     * @param file The File on disk which will store these properties. Will be created if needed.
     */
    public FileBasedProperties(File file) {
        super();
        this.file = file;
        this.eagerSave = false;
    }

    /**
     * You can set an optional comment header that will be written out at the top of
     * the file whenever save() is invoked. For a multi-line comment block, just embed "\n" as
     * needed into the string. A null value disables the header (this is the default).
     *
     * @param header Any string value to be written out as a comment header.
     */
    public void setCommentHeader(String header) {
        commentHeader = header;
    }

    /**
     * Indicates whether this FileBasedProperties instance has a comment header set.
     *
     * @return True if a comment header block has been specified in this instance.
     */
    public boolean hasCommentHeader() {
        return commentHeader != null;
    }

    /**
     * Returns the comment header block that was set in this FileBasedProperties.
     *
     * @return a comment header block, or null if one was not set.
     */
    public String getCommentHeader() {
        return commentHeader;
    }

    /**
     * Sets whether this object should save eagerly (default is false). If true, then each time
     * any of the various set methods are invoked, a disk save will be undertaken immediately.
     * If false, the properties in memory are never saved to disk unless save() is invoked.
     *
     * @param eager Whether to save every time a property value is set, or to wait until save().
     */
    public void setEagerSave(boolean eager) {
        eagerSave = eager;
    }

    /**
     * Returns whether this instance is set to eagerly save changes or not.
     *
     * @return true if this instance is set to eagerly save.
     */
    public boolean isEagerSave() {
        return eagerSave;
    }

    /**
     * Attempts to load property values from our file.
     *
     * @throws IOException If something goes wrong.
     */
    public void load() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            props.load(reader);
        }
    }

    /**
     * Saves the current properties to our file.
     *
     * @throws IOException If something goes wrong.
     */
    public void save() throws IOException {
        save(null);
    }

    /**
     * Returns the File around which this instance is based.
     *
     * @return A File.
     */
    public File getFile() {
        return file;
    }

    /**
     * Saves the current properties to our file. You can set an optional comment block here
     * which will be appended to the comment header set in this class (if any).
     *
     * @param comments An optional comment block (use \n in the string to add multiple lines).
     * @throws IOException If something goes wrong.
     */
    public void save(String comments) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writeCommentBlock(writer, commentHeader);
            writeCommentBlock(writer, comments);
            for (String key : getPropertyNames()) {
                // Property names can't contain spaces, but you can escape them with \u0020
                String sanitizedKey = key.replaceAll(" ", "\\\\u0020");

                // Property values can't contain newlines, but you can escape them with \n
                String sanitizedValue = props.getProperty(key).replaceAll("\n", "\\\\n");

                writer.write(sanitizedKey + "=" + sanitizedValue);
                writer.newLine();
            }
        }
    }

    /**
     * Removes the property with the given name, if any.
     *
     * @param name The property name.
     */
    @Override
    public void remove(String name) {
        super.remove(name);
        if (eagerSave) {
            saveWithoutException();
        }
    }

    /**
     * Attempts to save without throwing an IOException -if one occurs,
     * it will be logged without being rethrown.
     */
    public void saveWithoutException() {
        saveWithoutException(null);
    }

    /**
     * Attempts to save without throwing an IOException - if one occurs,
     * it will be logged without being rethrown.
     *
     * @param comments An optional comment block (use \n in the string to add multiple lines).
     */
    public void saveWithoutException(String comments) {
        try {
            save(comments);
        }
        catch (IOException ioe) {
            logger.log(Level.SEVERE, "Error saving properties: " + ioe.getMessage(), ioe);
        }
    }

    /**
     * Sets a Color property. Replaces any previous value for the given property name.
     *
     * @param name  The property name.
     * @param value The property value.
     */
    @Override
    public void setColor(String name, Color value) {
        super.setColor(name, value);
        if (eagerSave) {
            saveWithoutException();
        }
    }

    /**
     * Sets a Double property. Replaces any previous value for the given property name.
     *
     * @param name  The property name.
     * @param value The property value.
     */
    @Override
    public void setDouble(String name, Double value) {
        super.setDouble(name, value);
        if (eagerSave) {
            saveWithoutException();
        }
    }

    /**
     * Sets a Float property. Replaces any previous value for the given property name.
     *
     * @param name  The property name.
     * @param value The property value.
     */
    @Override
    public void setFloat(String name, Float value) {
        super.setFloat(name, value);
        if (eagerSave) {
            saveWithoutException();
        }
    }

    /**
     * Sets a Boolean value for the given property name. Replaces any previous value for the
     * named property.
     *
     * @param name  The property name.
     * @param value The property value.
     */
    @Override
    public void setBoolean(String name, Boolean value) {
        super.setBoolean(name, value);
        if (eagerSave) {
            saveWithoutException();
        }
    }

    /**
     * Sets an Integer property. Replaces any previous value for the given property name.
     *
     * @param name  The property name.
     * @param value The property value.
     */
    @Override
    public void setInteger(String name, Integer value) {
        super.setInteger(name, value);
        if (eagerSave) {
            saveWithoutException();
        }
    }

    /**
     * Sets a String name/value pair. Replaces any previous value for the given name.
     *
     * @param name  The property name.
     * @param value The property value.
     */
    @Override
    public void setString(String name, String value) {
        super.setString(name, value);
        if (eagerSave) {
            saveWithoutException();
        }
    }

    @Override
    public void setFont(String name, Font font) {
        super.setFont(name, font);
        if (eagerSave) {
            saveWithoutException();
        }
    }

    /**
     * Invoked internally to write out the given comment header.
     */
    private void writeCommentBlock(BufferedWriter writer, String comments) throws IOException {
        if (comments != null && !comments.isEmpty()) {
            String[] commentArr = comments.split("\\n");
            if (commentArr != null && commentArr.length > 0) {
                for (String c : commentArr) {
                    writer.write("# " + c);
                    writer.newLine();
                }
            }
        }

    }

}
