package ca.corbett.extras.properties;

import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.FormField;

import javax.swing.UIManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LookAndFeelProperty extends AbstractProperty {

    private static final Logger logger = Logger.getLogger(LookAndFeelProperty.class.getName());

    private final List<String> displayNames;
    private final List<String> classNames;
    private final String defaultClassName;
    private int selectedIndex;

    public LookAndFeelProperty(String name, String label) {
        this(name, label, null);
    }

    public LookAndFeelProperty(String name, String label, String defaultClassName) {
        super(name, label);

        // If no default was given, grab whatever is currently in use:
        if (defaultClassName == null || defaultClassName.isBlank()) {
            defaultClassName = UIManager.getLookAndFeel().getClass().getName();
        }
        this.defaultClassName = defaultClassName;

        // Build out the list based on whatever is installed:
        List<UIManager.LookAndFeelInfo> list = Arrays.asList(UIManager.getInstalledLookAndFeels());
        list.sort(Comparator.comparing(UIManager.LookAndFeelInfo::getName));
        displayNames = new ArrayList<>(list.size());
        classNames = new ArrayList<>(list.size());
        int index = 0;
        for (UIManager.LookAndFeelInfo info : list) {
            displayNames.add(info.getName());
            classNames.add(info.getClassName());
            if (info.getClassName().equals(defaultClassName)) {
                selectedIndex = index;
            }
            index++;
        }
    }

    public LookAndFeelProperty setSelectedIndex(int index) {
        if (index < 0 || index >= classNames.size()) {
            return this;
        }
        selectedIndex = index;
        return this;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public LookAndFeelProperty setSelectedItem(String item) {
        for (int i = 0; i < displayNames.size(); i++) {
            if (displayNames.get(i).equals(item)) {
                selectedIndex = i;
                break;
            }
        }
        return this;
    }

    public int indexOfDisplayName(String itemName) {
        for (int i = 0; i < displayNames.size(); i++) {
            if (displayNames.get(i).equals(itemName)) {
                return i;
            }
        }
        return -1;
    }

    public int indexOfClassName(String itemName) {
        for (int i = 0; i < classNames.size(); i++) {
            if (classNames.get(i).equals(itemName)) {
                return i;
            }
        }
        return -1;
    }

    public String getSelectedLafClass() {
        return classNames.get(selectedIndex);
    }

    public String getLafClass(int index) {
        return (index >= 0 && index < classNames.size()) ? classNames.get(index) : getSelectedLafClass();
    }

    @Override
    public void saveToProps(Properties props) {
        props.setString(fullyQualifiedName, getSelectedLafClass());
    }

    @Override
    public void loadFromProps(Properties props) {
        int index = indexOfClassName(props.getString(fullyQualifiedName, getSelectedLafClass()));
        if (index == -1) {
            // If the one in props is not in our list, try to find our default item
            index = indexOfClassName(defaultClassName);
        }
        if (index == -1) {
            selectedIndex = 0; // fallback default
        }
        else {
            selectedIndex = index;
        }
    }

    @Override
    protected FormField generateFormFieldImpl() {
        return new ComboField(propertyLabel, displayNames, selectedIndex, false);
    }

    @Override
    public void loadFromFormField(FormField field) {
        if (field.getIdentifier() == null
                || !field.getIdentifier().equals(fullyQualifiedName)
                || !(field instanceof ComboField)) {
            logger.log(Level.SEVERE, "LookAndFeelProperty.loadFromFormField: received the wrong field \"{0}\"",
                       field.getIdentifier());
            return;
        }

        selectedIndex = ((ComboField)field).getSelectedIndex();
    }
}
