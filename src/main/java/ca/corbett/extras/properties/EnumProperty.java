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
 * The generated ComboField will be typed to the given enum type, so if you need
 * to interact with the ComboField directly, you can do so in a type-safe manner.
 * Enum values are displayed to the user using their toString() value, but they
 * are persisted to properties using their name() value. This allows you to change
 * the toString() value over time (for localization, for example) without breaking
 * existing saved properties.
 *
 * @param <T> Supply your custom enum type.
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2025-03-26
 */
public class EnumProperty<T extends Enum<?>> extends AbstractProperty {

    private final Logger logger = Logger.getLogger(EnumProperty.class.getName());

    T value;

    /**
     * Creates a new EnumProperty whose choices will be taken from the values
     * of the supplied enum. Any combo box generated from this property will
     * use the result of toString() on each enum, allowing you to present
     * a user-friendly value in the combo box instead of using the name().
     *
     * @param name         The fully qualified property name.
     * @param label        The human-readable label for this property.
     * @param defaultValue A default value to use for initial selection.
     */
    public EnumProperty(String name, String label, T defaultValue) {
        super(name, label);
        this.value = defaultValue;
    }

    public EnumProperty<T> setSelectedItem(T item) {
        value = item;
        return this;
    }

    public T getSelectedItem() {
        return value;
    }

    @Override
    public void saveToProps(Properties props) {
        props.setString(fullyQualifiedName, getSelectedItem().name());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void loadFromProps(Properties props) {
        String valueName = props.getString(fullyQualifiedName, value.name());
        T[] enumConstants = (T[])value.getDeclaringClass().getEnumConstants();
        boolean isFound = false;
        for (T enumConstant : enumConstants) {
            if (enumConstant.name().equals(valueName)) {
                value = enumConstant;
                isFound = true;
                break;
            }
        }

        if (!isFound) {
            logger.warning("EnumProperty.loadFromProps: value \"" + valueName + "\" not found in enum " +
                                   value.getDeclaringClass().getName() + " -- using default \""
                                   + value.name() + "\"");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected FormField generateFormFieldImpl() {

        // ComboField wants a List<T> of options, and we need a selected index.
        T[] enumConstants = (T[])value.getDeclaringClass().getEnumConstants();
        List<T> options = new ArrayList<>();
        int selectedIndex = 0; // safe default
        for (int i = 0; i < enumConstants.length; i++) {
            T enumConstant = enumConstants[i];
            options.add(enumConstant);
            if (enumConstant.equals(value)) {
                selectedIndex = i;
            }
        }

        // Now we can generate the FormField:
        return new ComboField<>(propertyLabel, options, selectedIndex, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void loadFromFormField(FormField field) {
        if (field.getIdentifier() == null
                || !field.getIdentifier().equals(fullyQualifiedName)
                || !(field instanceof ComboField)) {
            logger.log(Level.SEVERE, "EnumProperty.loadFromFormField: received the wrong field \"{0}\"",
                       field.getIdentifier());
            return;
        }

        ComboField<T> comboField = (ComboField<T>)field;
        value = comboField.getSelectedItem();
    }
}
