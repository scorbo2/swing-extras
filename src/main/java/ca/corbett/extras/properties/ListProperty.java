package ca.corbett.extras.properties;

import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.ListField;

import javax.swing.JList;
import javax.swing.ListSelectionModel;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Wraps a ListField to allow for a multi-select property.
 * You can use any type for the list items, but they must have
 * a meaningful toString() implementation for displaying in the list.
 * <p>
 * You can add action buttons to the generated ListField via
 * a FormFieldGenerationListener. See ListField for more information
 * on how to do this.
 * </p>
 *
 * @since swing-extras 2.3
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class ListProperty<T> extends AbstractProperty {

    private static final Logger logger = Logger.getLogger(ListProperty.class.getName());

    private final List<T> items;
    private int[] selectedIndexes;
    private int selectionMode;
    private int layoutOrientation;
    private int visibleRowCount;
    private int fixedCellWidth;

    // For optional buttons (requires a FormFieldGenerationListener):
    private ListField.ButtonPosition buttonPosition;
    private int buttonAlignment;
    private int buttonHgap;
    private int buttonVgap;
    private int buttonPreferredWidth;
    private int buttonPreferredHeight;

    public ListProperty(String name, String label) {
        super(name, label);
        items = new ArrayList<>();
        selectedIndexes = new int[0];
        selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
        layoutOrientation = JList.VERTICAL;
        visibleRowCount = 4;
        fixedCellWidth = -1;

        buttonPosition = ListField.DEFAULT_BUTTON_POSITION;
        buttonAlignment = ListField.DEFAULT_BUTTON_ALIGNMENT;
        buttonHgap = ListField.DEFAULT_BUTTON_HGAP;
        buttonVgap = ListField.DEFAULT_BUTTON_VGAP;
        buttonPreferredWidth = 0;  // no default
        buttonPreferredHeight = 0; // no default
    }

    /**
     * Returns a copy of the list of all items in this list, regardless of whether
     * they are selected or not.
     */
    public List<T> getItems() {
        return new ArrayList<>(items);
    }

    /**
     * Returns a list of all the items in this list that are currently selected.
     * This list may be empty if nothing is selected.
     */
    public List<T> getSelectedItems() {
        List<T> selected = new ArrayList<>();
        for (int i : selectedIndexes) {
            selected.add(items.get(i));
        }
        return selected;
    }

    /**
     * Sets the items to include in the list. This will reset any current selection.
     */
    public ListProperty<T> setItems(List<T> items) {
        this.items.clear();
        this.items.addAll(items);
        selectedIndexes = new int[0];
        return this;
    }

    /**
     * Clears any current selection and marks each of the given items as selected, if the item
     * exists. Note: this may not do what you expect
     * it to do depending on the current selection mode (default selection mode is MULTIPLE_INTERVAL_SELECTION,
     * which allows multiple non-contiguous items to be selected).
     */
    public ListProperty<T> setSelectedItems(List<T> items) {
        // Convert our input list into a list of items that actually exist in our items list:
        List<T> itemsThatExist = new ArrayList<>();
        for (T item : items) {
            if (this.items.contains(item)) {
                itemsThatExist.add(item);
            }
        }

        // Now get the index of each one and build our selection array:
        selectedIndexes = new int[itemsThatExist.size()];
        int i = 0;
        for (T item : itemsThatExist) {
            selectedIndexes[i++] = this.items.indexOf(item);
        }
        return this;
    }

    /**
     * Sets the list selection model (allowable values are ListSelectionModel.SINGLE_SELECTION,
     * ListSelectionModel.MULTIPLE_INTERVAL_SELECTION, and ListSelectionModel.SINGLE_INTERVAL_SELECTION).
     * Any other value is ignored. The default value is MULTIPLE_INTERVAL_SELECTION.
     */
    public ListProperty<T> setSelectionMode(int selectionMode) {
        switch (selectionMode) {
            case ListSelectionModel.SINGLE_SELECTION:
            case ListSelectionModel.MULTIPLE_INTERVAL_SELECTION:
            case ListSelectionModel.SINGLE_INTERVAL_SELECTION:
                this.selectionMode = selectionMode;
        }
        return this;
    }

    /**
     * Sets the layout orientation (allowable values are JList.VERTICAL,
     * JList.VERTICAL_WRAP, and JList.HORIZONTAL_WRAP).
     * Any other value is ignored. The default value is VERTICAL.
     */
    public ListProperty<T> setLayoutOrientation(int orientation) {
        switch (orientation) {
            case JList.VERTICAL:
            case JList.VERTICAL_WRAP:
            case JList.HORIZONTAL_WRAP:
                this.layoutOrientation = orientation;
        }
        return this;
    }

    /**
     * Sets the desired visible row count. The default count is 4.
     */
    public ListProperty<T> setVisibleRowCount(int count) {
        visibleRowCount = count;
        return this;
    }

    /**
     * Returns the pixel width of each list cell.
     * A value of -1 here means the list cells will auto-size their widths
     * based on the width of the longest item in the list.
     */
    public int getFixedCellWidth() {
        return fixedCellWidth;
    }

    /**
     * Sets the pixel width of each list cell.
     * The default value is -1, which will set each cell's width to the width of the largest item.
     */
    public ListProperty<T> setFixedCellWidth(int width) {
        fixedCellWidth = width;
        return this;
    }

    public ListField.ButtonPosition getButtonPosition() {
        return buttonPosition;
    }

    /**
     * If the ListField generated from this property is to include buttons,
     * this sets the position of those buttons (requires a FormFieldGenerationListener to add buttons).
     *
     * @param buttonPosition One of the ListField.ButtonPosition enum values.
     * @return This ListProperty, for method chaining.
     */
    public ListProperty<T> setButtonPosition(ListField.ButtonPosition buttonPosition) {
        this.buttonPosition = buttonPosition;
        return this;
    }

    public int getButtonAlignment() {
        return buttonAlignment;
    }

    /**
     * If the ListField generated from this property is to include buttons,
     * this sets the alignment of those buttons (requires a FormFieldGenerationListener to add buttons).
     *
     * @param buttonAlignment One of FlowLayout's alignment options: LEFT, CENTER, RIGHT, LEADING, or TRAILING
     *                        from the FlowLayout class.
     * @return This ListProperty, for method chaining.
     */
    public ListProperty<T> setButtonAlignment(int buttonAlignment) {
        this.buttonAlignment = buttonAlignment;
        return this;
    }

    public int getButtonHgap() {
        return buttonHgap;
    }

    /**
     * If the ListField generated from this property is to include buttons,
     * this sets the horizontal gap between those buttons (requires a FormFieldGenerationListener to add buttons).
     */
    public ListProperty<T> setButtonHgap(int buttonHgap) {
        this.buttonHgap = buttonHgap;
        return this;
    }

    public int getButtonVgap() {
        return buttonVgap;
    }

    /**
     * If the ListField generated from this property is to include buttons,
     * this sets the vertical gap between those buttons (requires a FormFieldGenerationListener to add buttons).
     *
     * @param buttonVgap The vertical gap in pixels.
     */
    public ListProperty setButtonVgap(int buttonVgap) {
        this.buttonVgap = buttonVgap;
        return this;
    }

    /**
     * A convenience method to set button alignment, horizontal gap, and vertical gap all at once.
     *
     * @param buttonAlignment One of the FlowLayout alignment options: LEFT, CENTER, RIGHT, LEADING, or TRAILING.
     * @param buttonHgap      The horizontal gap between buttons, in pixels.
     * @param buttonVgap      The vertical gap between the buttons and the list, in pixels.
     * @return This ListProperty, for method chaining.
     */
    public ListProperty setButtonLayout(int buttonAlignment, int buttonHgap, int buttonVgap) {
        this.buttonAlignment = buttonAlignment;
        this.buttonHgap = buttonHgap;
        this.buttonVgap = buttonVgap;
        return this;
    }

    /**
     * If the ListField generated from this property is to include buttons,
     * this gets the preferred dimensions of those buttons (requires a FormFieldGenerationListener to add buttons).
     *
     * @return The preferred dimensions, or null to use the button's default preferred size.
     */
    public Dimension getButtonPreferredDimensions() {
        return (buttonPreferredWidth > 0 && buttonPreferredHeight > 0)
                ? new Dimension(buttonPreferredWidth, buttonPreferredHeight)
                : null;
    }

    /**
     * If the ListField generated from this property is to include buttons,
     * this sets the preferred dimensions of those buttons (requires a FormFieldGenerationListener to add buttons).
     *
     * @param buttonPreferredWidth  The preferred width in pixels, or 0 to use the button's default preferred width.
     * @param buttonPreferredHeight The preferred height in pixels, or 0 to use the button's default preferred height.
     */
    public ListProperty<T> setButtonPreferredDimensions(int buttonPreferredWidth, int buttonPreferredHeight) {
        this.buttonPreferredWidth = buttonPreferredWidth;
        this.buttonPreferredHeight = buttonPreferredHeight;
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
        props.setString(fullyQualifiedName, propStr);
    }

    @Override
    public void loadFromProps(Properties props) {
        String propStr = props.getString(fullyQualifiedName, "");
        if (propStr.isBlank()) {
            selectedIndexes = new int[0];
            return;
        }
        selectedIndexes = Arrays.stream(propStr.split(","))
                                .mapToInt(s -> Integer.parseInt(s.trim()))
                                .toArray();
    }

    @Override
    protected FormField generateFormFieldImpl() {
        ListField<T> field = new ListField<>(propertyLabel, items);
        field.setLayoutOrientation(layoutOrientation);
        field.setVisibleRowCount(visibleRowCount);
        field.setSelectionMode(selectionMode);
        field.setSelectedIndexes(selectedIndexes);
        field.setFixedCellWidth(fixedCellWidth);

        // Button settings (these are optional, and require a FormFieldGenerationListener to add buttons):
        field.setButtonPosition(buttonPosition);
        field.setButtonAlignment(buttonAlignment);
        field.setButtonHgap(buttonHgap);
        field.setButtonVgap(buttonVgap);
        if (buttonPreferredWidth > 0 && buttonPreferredHeight > 0) {
            field.setButtonPreferredSize(new Dimension(buttonPreferredWidth, buttonPreferredHeight));
        }

        return field;
    }

    @Override
    public void loadFromFormField(FormField field) {
        if (field.getIdentifier() == null
                || !field.getIdentifier().equals(fullyQualifiedName)
                || !(field instanceof ListField<?> listField)) {
            logger.log(Level.SEVERE, "ListProperty.loadFromFormField: received the wrong field \"{0}\"",
                       field.getIdentifier());
            return;
        }

        if (!field.isValid()) {
            logger.log(Level.WARNING, "ListProperty.loadFromFormField: field \"{0}\" is not valid -- ignoring",
                       field.getIdentifier());
            return;
        }

        selectedIndexes = listField.getSelectedIndexes();
    }
}
