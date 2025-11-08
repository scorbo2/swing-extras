package ca.corbett.extras.properties;

import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.PasswordField;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A property to represent a password, which should by default be hidden in the UI.
 * This is very similar to a ShortTextProperty, except that the password is not
 * shown in the UI unless explicitly revealed.
 * <p>
 * <b>IMPORTANT NOTE:</b> This is an unusual property implementation in one
 * key respect - the current value of this property (that is, the actual
 * password), will NOT be saved to properties by default. This is for security
 * reasons, as the properties file is not encrypted, and we generally don't
 * want to have passwords stored in plain text. You can override this
 * with the setPasswordSavedToProps() method, if you are okay with
 * storing passwords in your properties file. Otherwise, this property will
 * only remember basic things about itself (number of columns, whether or
 * not to allow blank values, and so on).
 * </p>
 * <p>
 * <b>Suggestion:</b> add a checkbox in your UI to allow the user to make
 * this choice. The checked state of that checkbox should be handed
 * to setPasswordSavedToProps() before properties are saved.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.5
 */
public class PasswordProperty extends AbstractProperty {

    private static final Logger logger = Logger.getLogger(PasswordProperty.class.getName());

    protected String password;
    protected boolean isAllowBlank;
    protected int columns;
    protected boolean isPasswordSavedToProps;

    public PasswordProperty(String name, String label) {
        super(name, label);
        isAllowBlank = true; // arbitrary relaxed default
        isPasswordSavedToProps = false; // important default! See class javadocs.
        columns = 10; // arbitrary default
    }

    public PasswordProperty setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public PasswordProperty setAllowBlank(boolean allow) {
        isAllowBlank = allow;
        return this;
    }

    public boolean isAllowBlank() {
        return isAllowBlank;
    }

    public PasswordProperty setColumns(int cols) {
        columns = cols;
        return this;
    }

    public int getColumns() {
        return columns;
    }

    public PasswordProperty setPasswordSavedToProps(boolean isSaved) {
        isPasswordSavedToProps = isSaved;
        return this;
    }

    public boolean isPasswordSavedToProps() {
        return isPasswordSavedToProps;
    }

    @Override
    public void saveToProps(Properties props) {
        props.setInteger(fullyQualifiedName + ".cols", columns);
        props.setString(fullyQualifiedName + ".value", isPasswordSavedToProps ? password : "");
        props.setBoolean(fullyQualifiedName + ".allowBlank", isAllowBlank);
        props.setBoolean(fullyQualifiedName + ".isPasswordSaved", isPasswordSavedToProps);
    }

    @Override
    public void loadFromProps(Properties props) {
        columns = props.getInteger(fullyQualifiedName + ".cols", columns);
        password = props.getString(fullyQualifiedName + ".value", password);
        isAllowBlank = props.getBoolean(fullyQualifiedName + ".allowBlank", isAllowBlank);
        isPasswordSavedToProps = props.getBoolean(fullyQualifiedName + ".isPasswordSaved", isPasswordSavedToProps);
    }

    @Override
    protected FormField generateFormFieldImpl() {
        return new PasswordField(propertyLabel, columns)
                .setAllowBlank(isAllowBlank)
                .setPassword(password);
    }

    @Override
    public void loadFromFormField(FormField field) {
        if (field.getIdentifier() == null
                || !field.getIdentifier().equals(fullyQualifiedName)
                || !(field instanceof PasswordField)) {
            logger.log(Level.SEVERE, "PasswordProperty.loadFromFormField: received the wrong field \"{0}\"",
                       field.getIdentifier());
            return;
        }

        password = ((PasswordField)field).getPassword();
    }
}
