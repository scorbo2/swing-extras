package ca.corbett.extras.properties;

import ca.corbett.extras.io.KeyStrokeManager;
import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.KeyStrokeField;

import javax.swing.Action;
import javax.swing.KeyStroke;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A property implementation for storing KeyStroke values.
 * This allows your application to expose keyboard shortcuts to the
 * user for customization, and then persist them to application settings,
 * rather than hard-coding them.
 * <p>
 * The Action to associate with the KeyStroke is also stored here,
 * though it will be ignored when persisting/loading the property.
 * This allows applications or extensions to associate functionality
 * with the KeyStrokeProperty for convenience.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.7
 */
public class KeyStrokeProperty extends AbstractProperty {

    private static final Logger log = Logger.getLogger(KeyStrokeProperty.class.getName());

    private String reservedKeyStrokeMsg = KeyStrokeField.RESERVED_MSG;
    private final List<KeyStroke> reservedKeyStrokes = new ArrayList<>();
    private boolean allowBlank;
    private KeyStroke keyStroke;
    private final Action action;

    /**
     * Creates a new, blank KeyStrokeProperty with the given name and label.
     * The initial KeyStroke value will be null to indicate no shortcut assigned,
     * and "allowBlank" will be implicitly enabled. The associated Action will be null.
     */
    public KeyStrokeProperty(String fullyQualifiedName, String label) {
        this(fullyQualifiedName, label, null, null);
    }

    /**
     * Creates a new, blank KeyStrokeProperty with the given name, label, and action.
     * The initial KeyStroke value will be null to indicate no shortcut assigned,
     * and "allowBlank" will be implicitly enabled.
     */
    public KeyStrokeProperty(String fullyQualifiedName, String label, Action action) {
        this(fullyQualifiedName, label, null, null);
    }

    /**
     * Creates a new KeyStrokeProperty with the given name, label, and initial KeyStroke value.
     * The initial KeyStroke value can be null to indicate no shortcut assigned. If so,
     * "allowBlank" will be implicitly enabled. The associated Action will be null.
     */
    public KeyStrokeProperty(String fullyQualifiedName, String label, KeyStroke keyStroke) {
        this(fullyQualifiedName, label, keyStroke, null);
    }

    /**
     * Creates a new KeyStrokeProperty with the given name, label, initial KeyStroke value, and action.
     * The initial KeyStroke value can be null to indicate no shortcut assigned. If so,
     * "allowBlank" will be implicitly enabled.
     */
    public KeyStrokeProperty(String fullyQualifiedName, String label, KeyStroke keyStroke, Action action) {
        super(fullyQualifiedName, label);
        this.keyStroke = keyStroke;
        this.allowBlank = keyStroke == null;
        this.action = action;
    }

    /**
     * Reports whether blank (null) KeyStroke values are allowed for this property.
     * The default is false, unless a null KeyStroke was supplied to the constructor.
     */
    public boolean isAllowBlank() {
        return allowBlank;
    }

    /**
     * Decides whether blank (null) KeyStroke values are allowed for this property.
     */
    public KeyStrokeProperty setAllowBlank(boolean allowBlank) {
        this.allowBlank = allowBlank;
        return this;
    }

    /**
     * Gets the Action associated with this property, if any.
     *
     * @return The Action associated with this property, or null if none.
     */
    public Action getAction() {
        return action;
    }

    /**
     * Gets the current KeyStroke value of this property.
     */
    public KeyStroke getKeyStroke() {
        return keyStroke;
    }

    /**
     * Gets the current KeyStroke value of this property as a String.
     */
    public String getKeyStrokeString() {
        return keyStroke == null ? "" : KeyStrokeManager.keyStrokeToString(keyStroke);
    }

    /**
     * Sets the current KeyStroke value of this property.
     */
    public KeyStrokeProperty setKeyStroke(KeyStroke keyStroke) {
        this.keyStroke = keyStroke;
        return this;
    }

    /**
     * Optionally sets a list of reserved KeyStrokes that cannot be assigned to this property.
     * By default, no KeyStrokes are reserved. The given list will replace any previously set reserved KeyStrokes.
     */
    public KeyStrokeProperty setReservedKeyStrokes(List<KeyStroke> reservedKeyStrokes) {
        this.reservedKeyStrokes.clear();
        if (reservedKeyStrokes == null) {
            return this;
        }
        this.reservedKeyStrokes.addAll(reservedKeyStrokes);
        return this;
    }

    /**
     * Returns the list of reserved KeyStrokes that cannot be assigned to this property.
     */
    public List<KeyStroke> getReservedKeyStrokes() {
        return new ArrayList<>(reservedKeyStrokes);
    }

    /**
     * Returns the validation message that will be given if a reserved KeyStroke is assigned.
     */
    public String getReservedKeyStrokeMsg() {
        return reservedKeyStrokeMsg;
    }

    /**
     * Sets the validation message that will be given if a reserved KeyStroke is assigned.
     * The message cannot be null or blank. If so, the previous message is retained.
     */
    public KeyStrokeProperty setReservedKeyStrokeMsg(String msg) {
        if (msg != null && !msg.isBlank()) {
            this.reservedKeyStrokeMsg = msg;
        }
        return this;
    }

    @Override
    public void saveToProps(Properties props) {
        props.setString(fullyQualifiedName + ".keyStroke", getKeyStrokeString());
        props.setBoolean(fullyQualifiedName + ".allowBlank", allowBlank);
        props.setString(fullyQualifiedName + ".reservedKeyStrokes", listToString(reservedKeyStrokes));
        props.setString(fullyQualifiedName + ".reservedKeyStrokeMsg", reservedKeyStrokeMsg);
    }

    @Override
    public void loadFromProps(Properties props) {
        allowBlank = props.getBoolean(fullyQualifiedName + ".allowBlank", allowBlank);
        String reservedStr = props.getString(fullyQualifiedName + ".reservedKeyStrokes",
                                             listToString(reservedKeyStrokes));
        reservedKeyStrokes.clear();
        reservedKeyStrokes.addAll(stringToList(reservedStr));
        reservedKeyStrokeMsg = props.getString(fullyQualifiedName + ".reservedKeyStrokeMsg", reservedKeyStrokeMsg);

        String str = props.getString(fullyQualifiedName + ".keyStroke", getKeyStrokeString());
        KeyStroke newValue = null;
        if (str != null && !str.isBlank()) {
            newValue = KeyStrokeManager.parseKeyStroke(str);
        }

        // Check if the persisted value was blank or somehow invalid:
        if (newValue == null) {
            if (allowBlank) {
                // This is fine as blank is explicitly allowed:
                // (yes, we're treating malformed persisted values as blank - don't hand-edit the props file)
                keyStroke = null;
            }
            else {
                // This is not fine, so log a warning and keep previous value:
                log.warning("KeyStrokeProperty.loadFromProps: invalid keystroke string \"" + str +
                                    "\" for property \"" + fullyQualifiedName + "\"; keeping previous value");
            }

            // Either way, we're done here:
            return;
        }

        // Otherwise, we have a valid new value:
        keyStroke = newValue;
    }

    @Override
    protected FormField generateFormFieldImpl() {
        KeyStrokeField field = new KeyStrokeField(propertyLabel, getKeyStroke());
        field.setAllowBlank(allowBlank);
        field.setReservedKeyStrokes(reservedKeyStrokes);
        field.setReservedKeyStrokeMsg(reservedKeyStrokeMsg);
        return field;
    }

    @Override
    public void loadFromFormField(FormField field) {
        // Make sure the field is of the expected type and matches our identifier:
        if (field.getIdentifier() == null
                || !field.getIdentifier().equals(fullyQualifiedName)
                || !(field instanceof KeyStrokeField)) {
            log.log(Level.SEVERE, "KeyStrokeProperty.loadFromFormField: received the wrong field \"{0}\"",
                    field.getIdentifier());
            return;
        }

        // Make sure the field is in a valid state:
        // (best practice is to prevent invalid form submissions, but poorly-coded clients can ignore this)
        if (!field.isValid()) {
            log.log(Level.WARNING, "KeyStrokeProperty.loadFromFormField: received an invalid field \"{0}\"",
                    field.getIdentifier());
            return;
        }

        // We're good at this point, so we can safely load the value:
        keyStroke = ((KeyStrokeField)field).getKeyStroke();
    }

    /**
     * Internal method to convert a List to a single comma-separated String.
     */
    static String listToString(List<KeyStroke> list) {
        if (list == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(KeyStrokeManager.keyStrokeToString(list.get(i)));
            if (i < list.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    /**
     * Internal method to convert a comma-separated String to a List.
     */
    static List<KeyStroke> stringToList(String str) {
        List<KeyStroke> list = new ArrayList<>();
        if (str == null || str.isBlank()) {
            return list;
        }
        String[] parts = str.split(",");
        for (String part : parts) {
            KeyStroke keyStroke = KeyStrokeManager.parseKeyStroke(part.trim());
            if (keyStroke != null) {
                list.add(keyStroke);
            }
        }
        return list;
    }
}
