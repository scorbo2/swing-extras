package ca.corbett.extras.properties;

import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.FormField;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a single-select chooser field that allows selection from a list of items.
 *
 * @author scorbett
 * @since sc-util 1.8
 */
public class ComboProperty extends AbstractProperty {

    private static final Logger logger = Logger.getLogger(ComboProperty.class.getName());

    protected final List<String> items;
    protected int selectedIndex;
    protected boolean isEditable;

    public ComboProperty(String name, String label, List<String> items, int selectedIndex, boolean isEditable) {
        super(name, label);
        this.items = items;
        this.selectedIndex = selectedIndex;
        this.isEditable = isEditable;
    }

    public ComboProperty setSelectedIndex(int index) {
        if (index < 0 || index >= items.size()) {
            return this;
        }
        selectedIndex = index;
        return this;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public ComboProperty setSelectedItem(String item) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).equals(item)) {
                selectedIndex = i;
                break;
            }
        }
        return this;
    }

    public int indexOf(String item) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).equals(item)) {
                return i;
            }
        }
        return -1;
    }

    public String getSelectedItem() {
        return items.get(selectedIndex);
    }

    public boolean isEditable() {
        return isEditable;
    }

    @Override
    public void saveToProps(Properties props) {
        props.setString(fullyQualifiedName, getSelectedItem());
    }

    @Override
    public void loadFromProps(Properties props) {
        int index = indexOf(props.getString(fullyQualifiedName, getSelectedItem()));
        if (index == -1) {
            selectedIndex = 0; // fallback default
        }
        else {
            selectedIndex = index;
        }
    }

    @Override
    protected FormField generateFormFieldImpl() {
        return new ComboField(propertyLabel, items, selectedIndex, isEditable);
    }

    @Override
    public void loadFromFormField(FormField field) {
        if (field.getIdentifier() == null
                || !field.getIdentifier().equals(fullyQualifiedName)
                || !(field instanceof ComboField)) {
            logger.log(Level.SEVERE, "ComboProperty.loadFromFormField: received the wrong field \"{0}\"",
                       field.getIdentifier());
            return;
        }

        selectedIndex = ((ComboField)field).getSelectedIndex();
    }

}
