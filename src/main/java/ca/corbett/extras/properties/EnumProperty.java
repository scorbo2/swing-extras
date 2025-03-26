package ca.corbett.extras.properties;

import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.FormField;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EnumProperty<T extends Enum<?>> extends AbstractProperty {

    private final Logger logger = Logger.getLogger(EnumProperty.class.getName());

    private final List<String> names;
    private final List<String> labels;
    int selectedIndex;
    T defaultValue;
    private final boolean useNamesInsteadOfLabels;

    public EnumProperty(String name, String label, T defaultValue) {
        this(name, label, defaultValue, false);
    }

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

    public void setSelectedIndex(int index) {
        if (index < 0 || index >= names.size()) {
            return;
        }
        selectedIndex = index;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedItem(T item) {
        for (int i = 0; i < names.size(); i++) {
            if (names.get(i).equals(item.name())) {
                selectedIndex = i;
                break;
            }
        }
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
        return (T) defaultValue.getDeclaringClass().getEnumConstants()[selectedIndex];
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
        } else {
            selectedIndex = index;
        }
    }

    @Override
    public FormField generateFormField() {
        List<String> options = useNamesInsteadOfLabels ? names : labels;
        ComboField field = new ComboField(propertyLabel, options, selectedIndex, false);
        field.setIdentifier(fullyQualifiedName);
        return field;
    }

    @Override
    public void loadFromFormField(FormField field) {
        if (field.getIdentifier() == null
                || !field.getIdentifier().equals(fullyQualifiedName)
                || !(field instanceof ComboField)) {
            logger.log(Level.SEVERE, "EnumProperty.loadFromFormField: received the wrong field \"{0}\"", field.getIdentifier());
            return;
        }

        selectedIndex = ((ComboField) field).getSelectedIndex();
    }
}
