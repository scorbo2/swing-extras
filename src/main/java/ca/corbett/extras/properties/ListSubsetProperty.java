package ca.corbett.extras.properties;

import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.ListSubsetField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Represents an AbstractProperty wrapper around the ListSubset form field.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.6
 */
public class ListSubsetProperty<T> extends AbstractProperty {

    private static final Logger log = Logger.getLogger(ListSubsetProperty.class.getName());

    private List<T> allItems;
    private int visibleRowCount;
    private int fixedCellWidth;
    private int[] selectedIndexes;

    public ListSubsetProperty(String fullyQualifiedName, String label) {
        this(fullyQualifiedName, label, List.of());
    }

    public ListSubsetProperty(String fullyQualifiedName, String label, List<T> allItems) {
        this(fullyQualifiedName, label, allItems, new int[0]);
    }

    public ListSubsetProperty(String fullyQualifiedName, String label, List<T> allItems, int[] selectedIndexes) {
        super(fullyQualifiedName, label);
        this.allItems = new ArrayList<>(allItems);
        visibleRowCount = ListSubsetField.DEFAULT_VISIBLE_ROW_COUNT;
        fixedCellWidth = ListSubsetField.DEFAULT_FIXED_CELL_WIDTH;
        setSelectedIndexes(selectedIndexes);
    }

    public int getVisibleRowCount() {
        return visibleRowCount;
    }

    public ListSubsetProperty<T> setVisibleRowCount(int visibleRowCount) {
        this.visibleRowCount = visibleRowCount;
        return this;
    }

    public int getFixedCellWidth() {
        return fixedCellWidth;
    }

    public ListSubsetProperty<T> setFixedCellWidth(int fixedCellWidth) {
        this.fixedCellWidth = fixedCellWidth;
        return this;
    }

    public List<T> getAllItems() {
        return new ArrayList<>(allItems);
    }

    public ListSubsetProperty<T> setAllItems(List<T> allItems) {
        this.allItems = new ArrayList<>(allItems);
        return this;
    }

    public List<T> getSelectedItems() {
        List<T> selectedItems = new ArrayList<>();
        for (int index : selectedIndexes) {
            if (index >= 0 && index < allItems.size()) {
                selectedItems.add(allItems.get(index));
            }
        }
        return new ArrayList<>(selectedItems);
    }

    public ListSubsetProperty<T> setSelectedIndexes(int[] selectedIndexes) {
        this.selectedIndexes = selectedIndexes;
        return this;
    }

    @Override
    public void saveToProps(Properties props) {
        props.setString(fullyQualifiedName + ".selected", getSelectedIndexesAsString(selectedIndexes));
        props.setInteger(fullyQualifiedName+".visibleRowCount", visibleRowCount);
        props.setInteger(fullyQualifiedName+".fixedCellWidth", fixedCellWidth);
    }

    @Override
    public void loadFromProps(Properties props) {
        visibleRowCount = props.getInteger(fullyQualifiedName+".visibleRowCount", visibleRowCount);
        fixedCellWidth = props.getInteger(fullyQualifiedName+".fixedCellWidth", fixedCellWidth);
        String propStr = props.getString(fullyQualifiedName + ".selected", getSelectedIndexesAsString(selectedIndexes));
        if (propStr.isEmpty()) {
            selectedIndexes = new int[0];
        }
        else {
            try {
                selectedIndexes = Arrays.stream(propStr.split(","))
                                        .mapToInt(s -> Integer.parseInt(s.trim()))
                                        .toArray();
            }
            catch (NumberFormatException nfe) {
                log.log(Level.SEVERE, "ListSubsetProperty.loadFromProps: invalid selected indexes string \"{0}\"",
                        propStr);
                selectedIndexes = new int[0];
            }
        }
    }

    @Override
    protected FormField generateFormFieldImpl() {
        ListSubsetField<T> field = new ListSubsetField<>(propertyLabel, allItems);
        field.setVisibleRowCount(visibleRowCount);
        field.setFixedCellWidth(fixedCellWidth);
        field.selectIndexes(selectedIndexes);
        return field;
    }

    @Override
    public void loadFromFormField(FormField field) {
        if (field.getIdentifier() == null
                || !field.getIdentifier().equals(fullyQualifiedName)
                || !(field instanceof ListSubsetField)) {
            log.log(Level.SEVERE, "ListSubsetProperty.loadFromFormField: received the wrong field \"{0}\"",
                       field.getIdentifier());
            return;
        }

        ListSubsetField<T> listSubsetField = (ListSubsetField<T>) field;
        this.selectedIndexes = listSubsetField.getSelectedIndexes();
        this.visibleRowCount = listSubsetField.getVisibleRowCount();
        this.fixedCellWidth = listSubsetField.getFixedCellWidth();
    }

    /**
     * Converts a selectedIndexes array to a comma-separated string for serialization purposes.
     */
    static String getSelectedIndexesAsString(int[] selectedIndexes) {
        return Arrays.stream(selectedIndexes)
                     .mapToObj(String::valueOf)
                     .collect(Collectors.joining(","));
    }
}
