package ca.corbett.forms.fields;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import java.util.List;

/**
 * Wraps a JList to allow for multi-selection of some object type.
 *
 * @since swing-extras 2.3
 * @author scorbo2
 */
public class ListField<T> extends FormField {

    private final JList<T> list;

    public ListField(String label, List<T> items) {
        fieldLabel.setText(label);
        DefaultListModel<T> listModel = new DefaultListModel<>();
        listModel.addAll(items);
        list = new JList<>(listModel);
        list.setVisibleRowCount(4);
        list.setFixedCellWidth(20);
        fieldComponent = list;
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
    public void setSelectionMode(int selectionMode) {
        switch (selectionMode) {
            case ListSelectionModel.SINGLE_SELECTION:
            case ListSelectionModel.MULTIPLE_INTERVAL_SELECTION:
            case ListSelectionModel.SINGLE_INTERVAL_SELECTION:
                list.setSelectionMode(selectionMode);
        }
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
    public void setLayoutOrientation(int orientation) {
        switch (orientation) {
            case JList.VERTICAL:
            case JList.VERTICAL_WRAP:
            case JList.HORIZONTAL_WRAP:
                list.setLayoutOrientation(orientation);
        }
    }

    public int getVisibleRowCount() {
        return list.getVisibleRowCount();
    }

    /**
     * Sets the desired visible row count. The default count is 4.
     */
    public void setVisibleRowCount(int count) {
        list.setVisibleRowCount(count);
    }

    public int[] getSelectedIndexes() {
        return list.getSelectedIndices();
    }

    /**
     * Sets the selected indexes for the list. Note: this may not do what you expect
     * it to do depending on the current selection mode (default selection mode is MULTIPLE_INTERVAL_SELECTION,
     * which allows multiple non-contiguous items to be selected).
     */
    public void setSelectedIndexes(int[] selection) {
        list.setSelectedIndices(selection);
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
    public void setFixedCellWidth(int width) {
        list.setFixedCellWidth(width);
    }
}
