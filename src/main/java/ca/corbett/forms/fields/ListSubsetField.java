package ca.corbett.forms.fields;

import ca.corbett.forms.Resources;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.TransferHandler;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
 * <p>
 * <b>Drag and drop</b>: There are two uses of drag and drop within this component:
 * </p>
 * <ul>
 *     <li>Users can drag items from one list to the other to move them between lists.</li>
 *     <li>When auto-sorting is disabled, users can drag items within a list to reorder the items.</li>
 * </ul>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.6
 */
public class ListSubsetField<T> extends FormField {

    private static final Logger log = Logger.getLogger(ListSubsetField.class.getName());

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
        initDragAndDrop();
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
     * Gets the list of currently available items - that is, items that are still
     * present in the left list. If the returned list is empty, then all items are selected.
     */
    public List<T> getAvailableItems() {
        return Collections.list(availableListModel.elements());
    }

    /**
     * Gets the list of currently selected items - that is, items that are present
     * in the right list. If the returned list is empty, then no items are selected.
     */
    public List<T> getSelectedItems() {
        return Collections.list(selectedListModel.elements());
    }

    /**
     * Programmatically select the given items. This will only select items
     * that were not already selected. Any items not present in the available items
     * list will be ignored.
     */
    public ListSubsetField<T> selectItems(List<T> itemsToSelect) {
        if (itemsToSelect == null) {
            return this;
        }

        // Move the specified items to the selected list:
        // (note we don't just invoke selectItem() on each item because we only
        //  want to do the sorting once at the end, if needed, instead of after every item)
        boolean anyMoved = false;
        for (T item : itemsToSelect) {
            if (availableListModel.removeElement(item)) {
                selectedListModel.addElement(item);
                anyMoved = true;
            }
        }

        // Now sort the selected list if auto-sorting is enabled:
        if (autoSortingEnabled) {
            sortListModel(selectedListModel);
        }

        // Fire value changed event only if at least one item was moved:
        if (anyMoved) {
            fireValueChangedEvent();
        }

        return this;
    }

    /**
     * Clears the current selection - this is equivalent to calling unselectAllItems().
     */
    public ListSubsetField<T> clearSelection() {
        unselectAllItems();
        return this;
    }

    /**
     * Allow callers to programmatically move an item to the right list (select it).
     * Does nothing if the given item is not present in the available items list.
     */
    public ListSubsetField<T> selectItem(T item) {
        if (item == null) {
            return this;
        }
        if (availableListModel.removeElement(item)) {
            selectedListModel.addElement(item);
            if (autoSortingEnabled) {
                sortListModel(selectedListModel);
            }
            fireValueChangedEvent();
        }
        return this;
    }

    /**
     * Allow callers to programmatically move an item to the left list (unselect it).
     * Does nothing if the given item is not present in the selected items list.
     */
    public ListSubsetField<T> unselectItem(T item) {
        if (item == null) {
            return this;
        }
        if (selectedListModel.removeElement(item)) {
            availableListModel.addElement(item);
            if (autoSortingEnabled) {
                sortListModel(availableListModel);
            }
            fireValueChangedEvent();
        }
        return this;
    }

    /**
     * Allow callers to programmatically move all items right (select all).
     */
    public ListSubsetField<T> selectAllItems() {
        moveAllRight();
        return this;
    }

    /**
     * Allow callers to programmatically move all items left (unselect all).
     */
    public ListSubsetField<T> unselectAllItems() {
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
        if (renderer == null) {
            return this;
        }
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
     * Note: Enabling auto-sorting will immediately sort both lists.
     * Disabling auto-sorting will not change the current ordering of items.
     * </p>
     * <p>
     * Note: Enabling auto-sorting will automatically disable the ability
     * to drag items within a list to re-order the list. When auto-sorting
     * is disabled, users can drag items within a list to change their order.
     * </p>
     */
    public ListSubsetField<T> setAutoSortingEnabled(boolean enabled) {
        this.autoSortingEnabled = enabled;
        // When enabling auto-sort, immediately sort both lists
        if (enabled) {
            sortListModel(availableListModel);
            sortListModel(selectedListModel);
        }
        // When disabling, do nothing - keep current order
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
        fireValueChangedEvent();
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
        fireValueChangedEvent();
    }

    private void moveAllRight() {
        selectedListModel.addAll(Collections.list(availableListModel.elements()));
        availableListModel.clear();
        if (autoSortingEnabled) {
            sortListModel(selectedListModel);
        }
        fireValueChangedEvent();
    }

    private void moveAllLeft() {
        availableListModel.addAll(Collections.list(selectedListModel.elements()));
        selectedListModel.clear();
        if (autoSortingEnabled) {
            sortListModel(availableListModel);
        }
        fireValueChangedEvent();
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

    /**
     * Initializes drag and drop support for both lists.
     * Enables dragging items between lists and reordering within lists
     * (when auto-sorting is disabled).
     */
    private void initDragAndDrop() {
        // Create transfer handlers for both lists
        ListSubsetTransferHandler availableHandler = new ListSubsetTransferHandler(
                availableList, availableListModel, selectedList, selectedListModel, false);
        ListSubsetTransferHandler selectedHandler = new ListSubsetTransferHandler(
                selectedList, selectedListModel, availableList, availableListModel, true);

        // Configure available list
        availableList.setDragEnabled(true);
        availableList.setDropMode(DropMode.INSERT);
        availableList.setTransferHandler(availableHandler);

        // Configure selected list
        selectedList.setDragEnabled(true);
        selectedList.setDropMode(DropMode.INSERT);
        selectedList.setTransferHandler(selectedHandler);
    }

    /**
     * Custom TransferHandler for handling drag and drop operations in ListSubsetField.
     * Supports both inter-list transfers (between available and selected lists) and
     * intra-list reordering (within the same list, only when auto-sort is disabled).
     */
    private class ListSubsetTransferHandler extends TransferHandler {
        private final JList<T> sourceList;
        private final DefaultListModel<T> sourceModel;
        private final JList<T> targetList;
        private final DefaultListModel<T> targetModel;
        private final boolean isSelectedList;
        private final DataFlavor localObjectFlavor;

        public ListSubsetTransferHandler(JList<T> sourceList, DefaultListModel<T> sourceModel,
                                          JList<T> targetList, DefaultListModel<T> targetModel,
                                          boolean isSelectedList) {
            this.sourceList = sourceList;
            this.sourceModel = sourceModel;
            this.targetList = targetList;
            this.targetModel = targetModel;
            this.isSelectedList = isSelectedList;
            // Create a custom DataFlavor for transferring list items within the same JVM
            try {
                this.localObjectFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
            } catch (ClassNotFoundException e) {
                // According to the DataFlavor API documentation, the javaJVMLocalObjectMimeType constant
                // should always be parseable, so this exception should never occur in practice.
                // But, just in case, we handle it here:
                throw new IllegalStateException("Unable to create DataFlavor for drag and drop", e);
            }
        }

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            @SuppressWarnings("unchecked")
            JList<T> list = (JList<T>) c;
            List<T> selectedValues = list.getSelectedValuesList();
            if (selectedValues.isEmpty()) {
                return null;
            }
            String sourceId = (sourceList == availableList) ? "available" : "selected";
            return new ListItemsTransferable<>(selectedValues, localObjectFlavor, sourceId);
        }

        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {
            // Items are removed during importData, no cleanup needed here
        }

        @Override
        public boolean canImport(TransferSupport support) {
            if (!support.isDataFlavorSupported(localObjectFlavor)) {
                return false;
            }

            try {
                // Get the actual drag source from the transferable data
                Transferable t = support.getTransferable();
                @SuppressWarnings("unchecked")
                TransferData transferData = (TransferData)t.getTransferData(localObjectFlavor);

                String dragSourceId = transferData.sourceListId;
                boolean isSameList = dragSourceId.equals(isSelectedList ? "selected" : "available");

                // If dropping within the same list, only allow if auto-sorting is disabled
                if (isSameList && autoSortingEnabled) {
                    return false;
                }

                return true;
            }
            catch (UnsupportedFlavorException | java.io.IOException e) {
                log.log(Level.WARNING, "ListSubsetField.canImport: error checking data flavor", e);
                return false;
            }
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }

            try {
                @SuppressWarnings("unchecked")
                JList<T> dropList = (JList<T>) support.getComponent();
                JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
                int dropIndex = dl.getIndex();

                // Get the items being transferred
                Transferable t = support.getTransferable();
                @SuppressWarnings("unchecked")
                TransferData<T> transferData = (TransferData<T>)t.getTransferData(localObjectFlavor);

                List<T> items = transferData.items;
                String dragSourceId = transferData.sourceListId;
                boolean isSameList = dragSourceId.equals(isSelectedList ? "selected" : "available");

                DefaultListModel<T> dropModel;
                if (isSameList) {
                    dropModel = sourceModel;
                } else {
                    dropModel = isSelectedList ? selectedListModel : availableListModel;
                }

                if (isSameList) {
                    // Reordering within the same list (only allowed when auto-sort is disabled)
                    // Remove items from their current positions
                    List<Integer> indicesToRemove = new ArrayList<>();
                    for (T item : items) {
                        int index = sourceModel.indexOf(item);
                        if (index >= 0) {
                            indicesToRemove.add(index);
                        }
                    }

                    // Sort indices in descending order to avoid index shifting issues
                    indicesToRemove.sort(Collections.reverseOrder());
                    for (int index : indicesToRemove) {
                        sourceModel.remove(index);
                        // Adjust drop index if we removed items before the drop location
                        if (index < dropIndex) {
                            dropIndex--;
                        }
                    }

                    // Insert items at the new position
                    for (T item : items) {
                        sourceModel.add(dropIndex++, item);
                    }
                    // Do NOT fire value changed event for reordering within the same list
                } else {
                    // Moving between lists

                    // Determine which model to use based on the source identifier
                    DefaultListModel<T> dragSourceModel = dragSourceId.equals("available")
                            ? availableListModel
                            : selectedListModel;

                    // Remove items from the actual source list
                    for (T item : items) {
                        dragSourceModel.removeElement(item);
                    }

                    // Add items to target list
                    if (autoSortingEnabled) {
                        // If auto-sorting is enabled, add items and then sort
                        for (T item : items) {
                            dropModel.addElement(item);
                        }
                        sortListModel(dropModel);
                    } else {
                        // If auto-sorting is disabled, insert at drop location
                        for (T item : items) {
                            dropModel.add(dropIndex++, item);
                        }
                    }
                    // Fire value changed event since items moved between lists
                    fireValueChangedEvent();
                }

                return true;
            } catch (UnsupportedFlavorException | java.io.IOException e) {
                log.log(Level.WARNING, "ListSubsetField.importData: error importing data", e);
                return false;
            }
        }
    }

    /**
     * Transferable implementation for list items.
     */
    private static class ListItemsTransferable<T> implements Transferable {
        private final List<T> items;
        private final DataFlavor flavor;
        private final String sourceListId;

        public ListItemsTransferable(List<T> items, DataFlavor flavor, String sourceListId) {
            this.items = items;
            this.flavor = flavor;
            this.sourceListId = sourceListId;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{flavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return this.flavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            // Return a wrapper object containing both items and source list
            return new TransferData<>(items, sourceListId);
        }
    }

    private static class TransferData<U> {
        final List<U> items;
        final String sourceListId; // "available" or "selected"

        TransferData(List<U> items, String sourceListId) {
            this.items = items;
            this.sourceListId = sourceListId;
        }
    }
}
