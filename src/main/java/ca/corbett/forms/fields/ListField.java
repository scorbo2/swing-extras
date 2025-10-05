package ca.corbett.forms.fields;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import java.util.List;

/**
 * Wraps a JList to allow for multi-selection of some object type.
 * A common use case would be ListField&lt;String&gt; to wrap a simple
 * list of Strings. The underlying JList can be obtained by calling
 * getList(), if you need to do custom styling or whatnot on the JList.
 *
 * @since swing-extras 2.3
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class ListField<T> extends FormField {

    private final JList<T> list;
    private final AbstractListModel<T> listModel;
    private boolean shouldExpand = false;

    /**
     * If you supply a List of T, we will create and use a DefaultListModel implicitly.
     * Use the overloaded constructor if you wish to use your own ListModel instead.
     */
    public ListField(String label, List<T> items) {
        this(label, createDefaultListModel(items));
    }

    /**
     * If you supply a ListModel of T, we will use that instead of creating a
     * DefaultListModel, but beware - you must populate the model beforehand.
     */
    public ListField(String label, AbstractListModel<T> listModel) {
        fieldLabel.setText(label);
        this.listModel = listModel;
        list = new JList<>(listModel);
        list.setVisibleRowCount(4);
        list.setFixedCellWidth(100);
        list.setFont(getDefaultFont());
        fieldComponent = new JScrollPane(list);
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fireValueChangedEvent();
            }
        });
    }

    /**
     * Gets the list selection model (allowable values are ListSelectionModel.SINGLE_SELECTION,
     * ListSelectionModel.MULTIPLE_INTERVAL_SELECTION, and ListSelectionModel.SINGLE_INTERVAL_SELECTION).
     */
    public int getSelectionMode() {
        return list.getSelectionMode();
    }

    /**
     * Sets the list selection model (allowable values are ListSelectionModel.SINGLE_SELECTION,
     * ListSelectionModel.MULTIPLE_INTERVAL_SELECTION, and ListSelectionModel.SINGLE_INTERVAL_SELECTION).
     * Any other value is ignored. The default value is MULTIPLE_INTERVAL_SELECTION.
     */
    public ListField<T> setSelectionMode(int selectionMode) {
        switch (selectionMode) {
            case ListSelectionModel.SINGLE_SELECTION:
            case ListSelectionModel.MULTIPLE_INTERVAL_SELECTION:
            case ListSelectionModel.SINGLE_INTERVAL_SELECTION: {
                list.setValueIsAdjusting(true);
                list.clearSelection();
                list.setSelectionMode(selectionMode);
                list.setValueIsAdjusting(false);
            }
        }
        return this;
    }

    /**
     * Gets the layout orientation (allowable values are JList.VERTICAL,
     * JList.VERTICAL_WRAP, and JList.HORIZONTAL_WRAP).
     */
    public int getLayoutOrientation() {
        return list.getLayoutOrientation();
    }

    /**
     * Sets the layout orientation (allowable values are JList.VERTICAL,
     * JList.VERTICAL_WRAP, and JList.HORIZONTAL_WRAP).
     * Any other value is ignored. The default value is VERTICAL.
     */
    public ListField<T> setLayoutOrientation(int orientation) {
        switch (orientation) {
            case JList.VERTICAL:
            case JList.VERTICAL_WRAP:
            case JList.HORIZONTAL_WRAP:
                list.setLayoutOrientation(orientation);
        }
        return this;
    }

    @Override
    public FormField setEnabled(boolean isEnabled) {
        super.setEnabled(isEnabled);
        list.setEnabled(isEnabled);
        return this;
    }

    public int getVisibleRowCount() {
        return list.getVisibleRowCount();
    }

    /**
     * Sets the desired visible row count. The default count is 4.
     */
    public ListField<T> setVisibleRowCount(int count) {
        if (count <= 0) {
            return this; // ignore crazy values
        }
        list.setVisibleRowCount(count);
        return this;
    }

    /**
     * Returns an array of selected item indexes.
     */
    public int[] getSelectedIndexes() {
        return list.getSelectedIndices();
    }

    /**
     * Sets the selected indexes for the list. Note: this may not do what you expect
     * it to do depending on the current selection mode (default selection mode is MULTIPLE_INTERVAL_SELECTION,
     * which allows multiple non-contiguous items to be selected).
     * <p>
     * Passing null or an empty list will clear the selection.
     */
    public ListField<T> setSelectedIndexes(int[] selection) {
        if (selection == null || selection.length == 0) {
            list.clearSelection();
            return this;
        }
        list.setValueIsAdjusting(true);
        list.setSelectedIndices(selection);
        list.setValueIsAdjusting(false);
        return this;
    }

    /**
     * Sets a specific list index to select. If out of bounds, this call is ignored.
     */
    public ListField<T> setSelectedIndex(int index) {
        if (index < 0 || index >= listModel.getSize()) {
            return this; // ignore out of bounds values
        }
        list.setValueIsAdjusting(true);
        list.setSelectedIndex(index);
        list.setValueIsAdjusting(false);
        return this;
    }

    /**
     * Provides direct access to the underlying JList if needed.
     */
    public JList<T> getList() {
        return list;
    }

    /**
     * Provides direct access to the underlying ListModel.
     * By default (unless the constructor was given something else), this will
     * return a DefaultListModel&lt;T&gt; instance.
     */
    public AbstractListModel<T> getListModel() {
        return listModel;
    }

    /**
     * You can optionally set a custom cell renderer if your list items have special display requirements.
     */
    public ListField<T> setCellRenderer(ListCellRenderer<T> renderer) {
        list.setCellRenderer(renderer);
        return this;
    }

    /**
     * Returns the effective list cell renderer.
     */
    public ListCellRenderer<? super T> getCellRenderer() {
        return list.getCellRenderer();
    }

    /**
     * Returns the pixel width of each list cell.
     * A value of -1 here means the list cells will auto-size their widths
     * based on the width of the longest item in the list.
     */
    public int getFixedCellWidth() {
        return list.getFixedCellWidth();
    }

    /**
     * Sets the pixel width of each list cell.
     * The default value is -1, which will set each cell's width to the width of the largest item.
     */
    public ListField<T> setFixedCellWidth(int width) {
        if (width < -1 || width == 0) {
            return this; // reject crazy values
        }
        list.setFixedCellWidth(width);
        return this;
    }

    /**
     * Sets whether this list should expand horizontally to fill available form panel width.
     * The default is false, meaning the list will be just wide enough to display the longest item.
     */
    public void setShouldExpand(boolean should) {
        shouldExpand = should;
    }

    /**
     * ListFields occupy more than one form row (generally - you can of course set
     * a visibleRowCount of 1, but why would you do that).
     */
    @Override
    public boolean isMultiLine() {
        return true;
    }

    /**
     * Overridden here to allow optional width expansion of the list to fill the form panel's width.
     * The default is false.
     */
    @Override
    public boolean shouldExpand() {
        return shouldExpand;
    }

    private static <T> DefaultListModel<T> createDefaultListModel(List<T> items) {
        DefaultListModel<T> model = new DefaultListModel<>();
        model.addAll(items);
        return model;
    }
}
