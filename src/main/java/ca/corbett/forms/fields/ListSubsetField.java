package ca.corbett.forms.fields;

import ca.corbett.forms.Resources;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a FormField that allows selection of a subset from a list.
 * Two lists are shown: one with all available items, and another
 * with the selected subset. Controls are provided such that users
 * can move items between the two lists.
 * <p>
 * <b>Sorting</b>: by default, items in both lists maintain their original order
 * as provided in the constructor. You can enable automatic sorting by calling
 * setAutoSortingEnabled(true). When auto-sorting is enabled, items will be sorted
 * in their natural order (assuming T implements Comparable) when items are moved
 * between lists. You can provide a custom Comparator by calling setItemComparator()
 * to control the sort order.
 * </p>
 * <p>
 * <b>Controlling list size</b>: you can control how many rows are visible
 * in each list by calling setVisibleRowCount(). By default, both lists
 * show 4 rows. You can also specify a fixed cell width by calling setFixedCellWidth().
 * By default, the fixed cell width is -1, which means the cell width will
 * be automatically sized to fit the largest item in the list.
 * Additionally, you have the option of calling setShouldExpand(true) to have
 * the entire field expand horizontally to fill the available width of the form panel.
 * This defaults to false, meaning the field will be just wide enough to display
 * the two lists at their natural sizes.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.6
 */
public class ListSubsetField<T> extends FormField {

    public static final int DEFAULT_VISIBLE_ROW_COUNT = 4;
    public static final int DEFAULT_FIXED_CELL_WIDTH = -1;

    private final JPanel wrapperPanel;
    private final JList<T> availableList;
    private final JList<T> selectedList;
    private final DefaultListModel<T> availableListModel;
    private final DefaultListModel<T> selectedListModel;
    private final JButton moveLeftButton;
    private final JButton moveRightButton;
    private final JButton moveAllLeftButton;
    private final JButton moveAllRightButton;
    private Comparator<T> itemComparator = null;
    private boolean shouldExpand = false;
    private boolean autoSortingEnabled = false;

    /**
     * Creates an empty ListSubsetField with the given field label.
     */
    public ListSubsetField(String label) {
        fieldLabel.setText(label);
        availableListModel = new DefaultListModel<>();
        selectedListModel = new DefaultListModel<>();
        availableList = new JList<>(availableListModel);
        selectedList = new JList<>(selectedListModel);
        availableList.setVisibleRowCount(DEFAULT_VISIBLE_ROW_COUNT);
        selectedList.setVisibleRowCount(DEFAULT_VISIBLE_ROW_COUNT);
        availableList.setFixedCellWidth(DEFAULT_FIXED_CELL_WIDTH);
        selectedList.setFixedCellWidth(DEFAULT_FIXED_CELL_WIDTH);
        wrapperPanel = new JPanel();
        moveLeftButton = createActionButton("Move Selected Left", Resources.getMoveLeftIcon());
        moveRightButton = createActionButton("Move Selected Right", Resources.getMoveRightIcon());
        moveAllLeftButton = createActionButton("Move All Left", Resources.getMoveAllLeftIcon());
        moveAllRightButton = createActionButton("Move All Right", Resources.getMoveAllRightIcon());
        moveLeftButton.addActionListener(e -> moveSelectedLeft());
        moveRightButton.addActionListener(e -> moveSelectedRight());
        moveAllLeftButton.addActionListener(e -> moveAllLeft());
        moveAllRightButton.addActionListener(e -> moveAllRight());
        fieldComponent = wrapperPanel;
        initLayout();
    }

    /**
     * Creates a ListSubsetField with the given label and list of available items.
     * Nothing is selected by default.
     */
    public ListSubsetField(String label, List<T> availableItems) {
        this(label);
        for (T item : availableItems) {
            availableListModel.addElement(item);
        }
        if (autoSortingEnabled) {
            sortListModel(availableListModel);
        }
    }

    /**
     * Creates a ListSubsetField with the given label, list of available items,
     * and list of selected items. The selected items will be removed from the available items,
     * if present. The list of selected items may be empty, and may also reference items
     * that are not present in the available items list. The field's list of items is always
     * the union of the available items and the selected items.
     */
    public ListSubsetField(String label, List<T> availableItems, List<T> selectedItems) {
        this(label, availableItems);
        for (T item : selectedItems) {
            selectedListModel.addElement(item);
            availableListModel.removeElement(item);
        }
        if (autoSortingEnabled) {
            sortListModel(selectedListModel);
        }
    }

    /**
     * Gets the list of currently available items - this is, items that are still
     * present in the left list. If empty, all items are selected.
     */
    public List<T> getAvailableItems() {
        return Collections.list(availableListModel.elements());
    }

    /**
     * Gets the list of currently selected items - this is, items that are present
     * in the right list. If empty, no items are selected.
     */
    public List<T> getSelectedItems() {
        return Collections.list(selectedListModel.elements());
    }

    /**
     * Programmatically select the items with the given indexes. The indexes
     * are relative to the combined list of ALL items sorted in their natural order
     * (or using the custom comparator if set). This provides a consistent reference
     * frame for index-based selection, regardless of the auto-sorting setting.
     * <p>
     * <B>Note:</B> This replaces any existing selection! This is "set these indexes" and not
     * "add these indexes".
     * </p>
     */
    public ListSubsetField<T> selectIndexes(int[] indexes) {
        // First clear any existing selection by moving all items back to the available list:
        moveAllLeft();

        // Make a copy and sort it to create a consistent reference frame:
        List<T> allAvailableItems = Collections.list(availableListModel.elements());
        allAvailableItems.sort(itemComparator);

        // Now, move all the specified indexes to the selected list:
        for (int index : indexes) {
            if (index < 0 || index >= allAvailableItems.size()) {
                continue; // ignore indexes out of bounds
            }
            T item = allAvailableItems.get(index);
            selectedListModel.addElement(item);
            availableListModel.removeElement(item);
        }

        // Now sort the selected list if auto-sorting is enabled:
        if (autoSortingEnabled) {
            sortListModel(selectedListModel);
        }

        return this;
    }

    /**
     * Returns the indexes of the currently selected items. The indexes are relative
     * to the combined list of ALL items sorted in their natural order (or using the
     * custom comparator if set). This provides a consistent reference frame for
     * index-based selection, regardless of the auto-sorting setting.
     */
    public int[] getSelectedIndexes() {
        // Gather all items into one list:
        List<T> allItems = new ArrayList<>(availableListModel.getSize() + selectedListModel.getSize());
        allItems.addAll(Collections.list(availableListModel.elements()));
        allItems.addAll(Collections.list(selectedListModel.elements()));

        // Always sort this list for consistent index calculation:
        allItems.sort(itemComparator);

        // Now, find the indexes of the selected items:
        List<Integer> selectedIndexes = new ArrayList<>();
        for (int i = 0; i < allItems.size(); i++) {
            T item = allItems.get(i);
            if (selectedListModel.contains(item)) {
                selectedIndexes.add(i);
            }
        }

        // Convert to int array:
        return selectedIndexes.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Allow callers to programmatically move an item right (select it).
     * Does nothing if the given item is not present in the available items list.
     */
    public ListSubsetField<T> moveItemRight(T item) {
        if (availableListModel.removeElement(item)) {
            selectedListModel.addElement(item);
            if (autoSortingEnabled) {
                sortListModel(selectedListModel);
            }
        }
        return this;
    }

    /**
     * Allow callers to programmatically move an item left (unselect it).
     * Does nothing if the given item is not present in the selected items list.
     */
    public ListSubsetField<T> moveItemLeft(T item) {
        if (selectedListModel.removeElement(item)) {
            availableListModel.addElement(item);
            if (autoSortingEnabled) {
                sortListModel(availableListModel);
            }
        }
        return this;
    }

    /**
     * Allow callers to programmatically move all items right (select all).
     */
    public ListSubsetField<T> moveAllItemsRight() {
        moveAllRight();
        return this;
    }

    /**
     * Allow callers to programmatically move all items left (unselect all).
     */
    public ListSubsetField<T> moveAllItemsLeft() {
        moveAllLeft();
        return this;
    }

    /**
     * Gets the current visible row count. The default count is 4.
     * Internally, we return the visible row count for the available list,
     * but the two lists are kept in sync, so they will always be the same.
     */
    public int getVisibleRowCount() {
        return availableList.getVisibleRowCount();
    }

    /**
     * Sets the desired visible row count. The default count is 4.
     * Both the available list and the selected list will be set to this value.
     */
    public ListSubsetField<T> setVisibleRowCount(int count) {
        if (count <= 0) {
            return this; // ignore crazy values
        }
        availableList.setVisibleRowCount(count);
        selectedList.setVisibleRowCount(count);
        return this;
    }

    /**
     * You can optionally set a custom cell renderer if your list items have special display requirements.
     * Both the available list and the selected list will be set to use this renderer.
     */
    public ListSubsetField<T> setCellRenderer(ListCellRenderer<T> renderer) {
        availableList.setCellRenderer(renderer);
        selectedList.setCellRenderer(renderer);
        return this;
    }

    /**
     * Returns the effective list cell renderer.
     * Internally, we return the renderer for the available list,
     * but the two lists are kept in sync, so both lists use the same renderer.
     */
    public ListCellRenderer<? super T> getCellRenderer() {
        return availableList.getCellRenderer();
    }

    /**
     * Returns the comparator used to sort items when they are added to either list.
     * If null, the natural ordering of the items is used. The default value is null.
     */
    public Comparator<T> getItemComparator() {
        return itemComparator;
    }

    /**
     * Sets the comparator used to sort items when they are added to either list.
     * If null, the natural ordering of the items is used.
     */
    public ListSubsetField<T> setItemComparator(Comparator<T> comparator) {
        this.itemComparator = comparator;
        return this;
    }

    /**
     * Returns whether auto-sorting is enabled for both lists.
     * When enabled, items are sorted when moved between lists.
     * When disabled (default), items maintain their original order.
     */
    public boolean isAutoSortingEnabled() {
        return autoSortingEnabled;
    }

    /**
     * Sets whether auto-sorting is enabled for both lists.
     * When enabled, items are sorted when moved between lists.
     * When disabled (default), items maintain their original order.
     * <p>
     * Note: Changing this setting does not automatically re-sort existing items.
     * If you want to sort items after enabling this, you can move items between
     * lists to trigger sorting.
     * </p>
     */
    public ListSubsetField<T> setAutoSortingEnabled(boolean enabled) {
        this.autoSortingEnabled = enabled;
        return this;
    }

    /**
     * Returns the pixel width of each list cell.
     * A value of -1 here means the list cells will auto-size their widths
     * based on the width of the longest item in the list.
     * Internally, we return the fixed cell width for the available list,
     * but the two lists are kept in sync, so both lists have the same fixed cell width.
     * The default value is -1.
     */
    public int getFixedCellWidth() {
        return availableList.getFixedCellWidth();
    }

    /**
     * Sets the pixel width of each list cell.
     * The default value is -1, which will set each cell's width to the width of the largest item.
     * Both the available list and the selected list will be set to this value.
     */
    public ListSubsetField<T> setFixedCellWidth(int width) {
        if (width < -1 || width == 0) {
            return this; // reject crazy values
        }
        availableList.setFixedCellWidth(width);
        selectedList.setFixedCellWidth(width);
        return this;
    }

    /**
     * ListSubsetFields occupy more than one form row (at least, generally speaking,
     * that statement is true - you could of course set a visibleRowCount of 1,
     * but why would you do that).
     */
    @Override
    public boolean isMultiLine() {
        return true;
    }

    /**
     * Sets whether this field should expand horizontally to fill available form panel width.
     * The default is false, meaning the field will be just wide enough to display the
     * two lists at their natural sizes.
     */
    public ListSubsetField setShouldExpand(boolean should) {
        shouldExpand = should;
        return this;
    }

    /**
     * Overridden here to allow optional width expansion of the field to fill the form panel's width.
     * The default is false.
     */
    @Override
    public boolean shouldExpand() {
        return shouldExpand;
    }

    private void moveSelectedRight() {
        List<T> selectedValues = availableList.getSelectedValuesList();
        for (T value : selectedValues) {
            availableListModel.removeElement(value);
            selectedListModel.addElement(value);
        }
        if (autoSortingEnabled) {
            sortListModel(selectedListModel);
        }
    }

    private void moveSelectedLeft() {
        List<T> selectedValues = selectedList.getSelectedValuesList();
        for (T value : selectedValues) {
            selectedListModel.removeElement(value);
            availableListModel.addElement(value);
        }
        if (autoSortingEnabled) {
            sortListModel(availableListModel);
        }
    }

    private void moveAllRight() {
        int size = availableListModel.getSize();
        for (int i = 0; i < size; i++) {
            T value = availableListModel.getElementAt(0);
            availableListModel.removeElementAt(0);
            selectedListModel.addElement(value);
        }
        if (autoSortingEnabled) {
            sortListModel(selectedListModel);
        }
    }

    private void moveAllLeft() {
        int size = selectedListModel.getSize();
        for (int i = 0; i < size; i++) {
            T value = selectedListModel.getElementAt(0);
            selectedListModel.removeElementAt(0);
            availableListModel.addElement(value);
        }
        if (autoSortingEnabled) {
            sortListModel(availableListModel);
        }
    }

    private void sortListModel(DefaultListModel<T> model) {
        List<T> items = java.util.Collections.list(model.elements());
        items.sort(itemComparator);
        model.clear();
        for (T item : items) {
            model.addElement(item);
        }
    }

    /**
     * Invoked internally to initialize the layout of this field.
     * The available list will appear on the left, with the selected list on the right,
     * and a vertical panel of action buttons will appear in between the two lists.
     */
    private void initLayout() {
        // GridBagLayout is horrible to work with!
        // But, it is better for this component than BorderLayout in my testing.
        wrapperPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Available list on the left:
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        wrapperPanel.add(new JScrollPane(availableList), gbc);

        // Vertically-centered action buttons in the center:
        List<JButton> actionButtons = List.of(
                moveAllLeftButton,
                moveLeftButton,
                moveRightButton,
                moveAllRightButton
        );
        JPanel buttonPanel = new JPanel(new GridLayout(actionButtons.size(), 1, 0, 0));
        for (JButton button : actionButtons) {
            buttonPanel.add(button);
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.NONE;  // Don't stretch the panel
        gbc.anchor = GridBagConstraints.CENTER;  // Center it in the cell
        gbc.insets = new Insets(0, 5, 0, 5);
        wrapperPanel.add(buttonPanel, gbc);

        // Selected list on the right:
        gbc.gridx = 2;
        gbc.weightx = 0.5;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0); // reset insets
        wrapperPanel.add(new JScrollPane(selectedList), gbc);
    }

    /**
     * Invoked internally to create and configure an action button.
     */
    private JButton createActionButton(String tooltip, ImageIcon icon) {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(24,24));
        button.setToolTipText(tooltip);
        button.setIcon(icon);
        button.setFocusPainted(false);
        return button;
    }
}
