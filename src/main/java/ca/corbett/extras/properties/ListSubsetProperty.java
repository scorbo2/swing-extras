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

    private List<T> availableItems;
    private List<T> selectedItems;
    private int visibleRowCount;
    private int fixedCellWidth;
    private int[] selectedIndexes;

    public ListSubsetProperty(String fullyQualifiedName, String label) {
        this(fullyQualifiedName, label, List.of());
    }

    public ListSubsetProperty(String fullyQualifiedName, String label, List<T> availableItems) {
        this(fullyQualifiedName, label, availableItems, List.of());
    }

    public ListSubsetProperty(String fullyQualifiedName, String label, List<T> availableItems, List<T> selectedItems) {
        super(fullyQualifiedName, label);
        this.availableItems = new ArrayList<>(availableItems);
        this.selectedItems = new ArrayList<>(selectedItems);
        visibleRowCount = ListSubsetField.DEFAULT_VISIBLE_ROW_COUNT;
        fixedCellWidth = ListSubsetField.DEFAULT_FIXED_CELL_WIDTH;
        selectedIndexes = new int[0];
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

    public List<T> getAvailableItems() {
        return new ArrayList<>(availableItems);
    }

    public ListSubsetProperty<T> setAvailableItems(List<T> availableItems) {
        this.availableItems = new ArrayList<>(availableItems);
        return this;
    }

    public List<T> getSelectedItems() {
        return new ArrayList<>(selectedItems);
    }

    public ListSubsetProperty<T> setSelectedItems(List<T> selectedItems) {
        this.selectedItems = new ArrayList<>(selectedItems);
        return this;
    }

    @Override
    public void saveToProps(Properties props) {
        String propStr = "";
        if (selectedIndexes.length > 0) {
            propStr = Arrays.stream(selectedIndexes)
                            .mapToObj(String::valueOf)
                            .collect(Collectors.joining(","));
        }
        props.setString(fullyQualifiedName+".selected", propStr);
        props.setInteger(fullyQualifiedName+".visibleRowCount", visibleRowCount);
        props.setInteger(fullyQualifiedName+".fixedCellWidth", fixedCellWidth);
    }

    @Override
    public void loadFromProps(Properties props) {
        visibleRowCount = props.getInteger(fullyQualifiedName+".visibleRowCount", visibleRowCount);
        fixedCellWidth = props.getInteger(fullyQualifiedName+".fixedCellWidth", fixedCellWidth);
        String propStr = props.getString(fullyQualifiedName+".selected", "");
        if (propStr.isEmpty()) {
            selectedIndexes = new int[0];
        }
        else {
            selectedIndexes = Arrays.stream(propStr.split(","))
                                    .mapToInt(s -> Integer.parseInt(s.trim()))
                                    .toArray();
        }
    }

    @Override
    protected FormField generateFormFieldImpl() {
        ListSubsetField<T> field = new ListSubsetField<>(propertyLabel, availableItems, selectedItems);
        field.setVisibleRowCount(visibleRowCount);
        field.setFixedCellWidth(fixedCellWidth);
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
        this.availableItems = new ArrayList<>(listSubsetField.getAvailableItems());
        this.selectedItems = new ArrayList<>(listSubsetField.getSelectedItems());
        this.selectedIndexes = listSubsetField.getSelectedIndexes();
        this.visibleRowCount = listSubsetField.getVisibleRowCount();
        this.fixedCellWidth = listSubsetField.getFixedCellWidth();
    }
}
