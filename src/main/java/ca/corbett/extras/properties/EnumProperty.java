package ca.corbett.extras.properties;

import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.FormField;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a multi-choice property, very similar to a ComboProperty, except that the
 * options come from a supplied enum instead of a List of String values.
 * You must supply a default value for the field, and you can choose whether the
 * form field generated from this property uses the enum name() or the enum toString()
 * for the combo box values. It's important to note that even if you use the toString()
 * for the combo box values, when the property saves itself to a Properties object,
 * it will use the enum name() to identify the field value. This is handy in case
 * the toString() value changes over time or is localized to another language.
 *
 * @param <T> Supply your custom enum type.
 * @author scorbo2
 * @since 2025-03-26
 */
public class EnumProperty<T extends Enum<?>> extends AbstractProperty {

    private final Logger logger = Logger.getLogger(EnumProperty.class.getName());

    private final List<String> names;
    private final List<String> labels;
    int selectedIndex;
    T defaultValue;
    private final boolean useNamesInsteadOfLabels;

    /**
     * Creates a new EnumProperty whose choices will be taken from the values
     * of the supplied enum. Any combo box generated from this property will
     * use the result of toString() on each enum, allowing you to present
     * a user-friendly value in the combo box instead of using the name().
     * Use the other constructor if you actually want to use name() instead
     * of toString() for the combo box values.
     *
     * @param name         The fully qualified property name.
     * @param label        The human-readable label for this property.
     * @param defaultValue A default value to use for initial selection.
     */
    public EnumProperty(String name, String label, T defaultValue) {
        this(name, label, defaultValue, false);
    }

    /**
     * Creates a new EnumProperty whose choices will be taken from the values
     * of the supplied enum, and lets you choose whether you want to use
     * name() or toString() for the possible combo box values.
     *
     * @param name                    The fully qualified property name.
     * @param label                   The human-readable label for this property.
     * @param defaultValue            A default value to use for initial selection.
     * @param useNamesInsteadOfLabels If true, name() will be used for combo box values instead of toString().
     */
    public EnumProperty(String name, String label, T defaultValue, boolean useNamesInsteadOfLabels) {
        super(name, label);
        this.defaultValue = defaultValue;
        this.useNamesInsteadOfLabels = useNamesInsteadOfLabels;
        names = new ArrayList<>();
        labels = new ArrayList<>();
        int i = 0;
        for (Enum<?> value : defaultValue.getDeclaringClass().getEnumConstants()) {
            names.add(value.name());
            labels.add(value.toString());
            if (value.name().equals(defaultValue.name())) {
                selectedIndex = i;
            }
            i++;
        }
    }

    public EnumProperty<T> setSelectedIndex(int index) {
        if (index < 0 || index >= names.size()) {
            return this;
        }
        selectedIndex = index;
        return this;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public EnumProperty<T> setSelectedItem(T item) {
        for (int i = 0; i < names.size(); i++) {
            if (names.get(i).equals(item.name())) {
                selectedIndex = i;
                break;
            }
        }
        return this;
    }

    public int indexOf(String itemName) {
        for (int i = 0; i < names.size(); i++) {
            if (names.get(i).equals(itemName)) {
                return i;
            }
        }
        return -1;
    }

    public int indexOf(T item) {
        return indexOf(item.name());
    }

    public T getSelectedItem() {
        return (T)defaultValue.getDeclaringClass().getEnumConstants()[selectedIndex];
    }

    @Override
    public void saveToProps(Properties props) {
        props.setString(fullyQualifiedName, getSelectedItem().name());
    }

    @Override
    public void loadFromProps(Properties props) {
        int index = indexOf(props.getString(fullyQualifiedName, getSelectedItem().name()));
        if (index == -1) {
            // If the one in props is not in our list, try to find our default item
            index = indexOf(defaultValue);
        }
        if (index == -1) {
            selectedIndex = 0; // fallback default
        }
        else {
            selectedIndex = index;
        }
    }

    @Override
    public FormField generateFormField() {
        List<String> options = useNamesInsteadOfLabels ? names : labels;
        ComboField field = new ComboField(propertyLabel, options, selectedIndex, false);
        field.setIdentifier(fullyQualifiedName);
        field.setEnabled(!isReadOnly);
        field.setHelpText(helpText);
        return field;
    }

    @Override
    public void loadFromFormField(FormField field) {
        if (field.getIdentifier() == null
                || !field.getIdentifier().equals(fullyQualifiedName)
                || !(field instanceof ComboField)) {
            logger.log(Level.SEVERE, "EnumProperty.loadFromFormField: received the wrong field \"{0}\"",
                       field.getIdentifier());
            return;
        }

        selectedIndex = ((ComboField)field).getSelectedIndex();
    }
}
