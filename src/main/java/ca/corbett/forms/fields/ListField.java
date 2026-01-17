package ca.corbett.forms.fields;

import ca.corbett.extras.properties.PropertiesDialog;

import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ListDataListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

/**
 * Wraps a JList to allow for multi-selection of some object type.
 * A common use case would be ListField&lt;String&gt; to wrap a simple
 * list of Strings. The underlying JList can be obtained by calling
 * getList(), if you need to do custom styling or whatnot on the JList.
 * <p>
 *     <b>An important note about value changed events</b> - this
 *     field's "value" is the list of selected items. Therefore,
 *     value changed events are only fired when the selection changes,
 *     and NOT when the list contents change. If you need to be notified
 *     when the list contents change, you will need to add a
 *     ListDataListener to the underlying ListModel yourself.
 *     You can do this by accessing the list model directly:
 * </p>
 * <pre>myListField.getListModel().addListDataListener(...);</pre>
 * <p>
 * Or, you can use the addListDataListener() convenience method:
 * </p>
 * <pre>myListField.addListDataListener(...);</pre>
 *
 * <h2>Adding action buttons to a ListField</h2>
 * <p>
 *     You can add action buttons to the ListField by using the addButton(Action) method.
 *     This works very similarly to ButtonField, in that you can control the alignment,
 *     hgap, and vgap of the button panel. ListField offers the option of placing the
 *     button panel either below the list (by default) or above the list, via the
 *     setButtonPosition() method. You can control the preferred size of buttons
 *     added to the button panel via the setButtonPreferredSize() method.
 * </p>
 * <p>
 *     There are some built-in actions included with swing-extras that are useful
 *     for ListFields, such as ListItemRemoveAction, ListItemMoveAction, and
 *     ListItemClearAction. These are found in the ca.corbett.forms.actions package.
 *     Adding your own actions is very easy - you can use any Action instance,
 *     but you should consider extending the EnhancedAction class to make
 *     setting things like icons and tooltips easier. See the example actions
 *     in the built-in demo application on the "Forms: lists and panels" tab!
 * </p>
 *
 * @since swing-extras 2.3
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class ListField<T> extends FormField {

    public static final ButtonPosition DEFAULT_BUTTON_POSITION = ButtonPosition.BOTTOM;
    public static final int DEFAULT_BUTTON_ALIGNMENT = FlowLayout.LEFT;
    public static final int DEFAULT_BUTTON_HGAP = 4;
    public static final int DEFAULT_BUTTON_VGAP = 4;

    public enum ButtonPosition {TOP, BOTTOM}

    private JPanel buttonPanel;
    private Dimension preferredButtonSize = null;
    private FlowLayout layout;
    private ButtonPosition buttonPosition;
    private final JList<T> list;
    private final DefaultListModel<T> listModel;
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
     * DefaultListModel. Either way, you can retrieve the model after creation
     * with the getListModel() method.
     */
    public ListField(String label, DefaultListModel<T> listModel) {
        fieldLabel.setText(label);
        this.listModel = listModel;
        list = new JList<>(listModel);
        list.setVisibleRowCount(4); // arbitrary default
        list.setFixedCellWidth(100); // arbitrary default
        list.setFont(getDefaultFont());
        fieldComponent = buildComponent();

        // ListField is generally intended to allow the user to select zero
        // or more items out of a static list of items. Therefore, our "value"
        // in swing-forms terms is the list of selected items, not the list contents.
        // We therefore do NOT fire a valueChangedEvent when the list contents change,
        // only when the selection changes. Callers should therefore be careful
        // to understand the difference:
        //      addValueChangedListener() -> notified when selection changes
        //      addListDataListener() -> notified when list contents change
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
        for (int i = 0; i < buttonPanel.getComponentCount(); i++) {
            buttonPanel.getComponent(i).setEnabled(isEnabled);
        }
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
    public DefaultListModel<T> getListModel() {
        return listModel;
    }

    /**
     * A convenience method to add a ListDataListener to the underlying ListModel.
     * <b>Important:</b> If you add a ValueChangedListener to this ListField, that listener
     * will only be notified when the selection changes, not when the list data changes.
     * If you need to be notified when the list data changes, you must add a ListDataListener
     * to the ListModel. This method makes that a bit easier.
     */
    public ListField<T> addListDataListener(ListDataListener listener) {
        listModel.addListDataListener(listener);
        return this;
    }

    /**
     * Convenience method to remove a ListDataListener from the underlying ListModel.
     */
    public ListField<T> removeListDataListener(ListDataListener listener) {
        listModel.removeListDataListener(listener);
        return this;
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

    /**
     * Gets the position of the button panel (above or below the list).
     */
    public ButtonPosition getButtonPosition() {
        return buttonPosition;
    }

    /**
     * Sets the position of the button panel (above or below the list).
     */
    public ListField<T> setButtonPosition(ButtonPosition position) {
        if (position == buttonPosition) {
            return this; // no change
        }

        // Remove the button panel from its current location:
        getFieldComponent().remove(buttonPanel);

        // Re-add it in the new location:
        switch (position) {
            case TOP -> getFieldComponent().add(buttonPanel, BorderLayout.NORTH);
            case BOTTOM -> getFieldComponent().add(buttonPanel, BorderLayout.SOUTH);
        }

        buttonPosition = position;

        // May require a layout update:
        getFieldComponent().revalidate();
        getFieldComponent().repaint();

        return this;
    }

    /**
     * Adds an Action to the button panel.
     * This will cause the button panel to become visible if it was not already.
     */
    public ListField<T> addButton(Action action) {
        JButton button = new JButton(action);
        // Apply any preferred size previously set for this button panel:
        if (preferredButtonSize != null) {
            button.setPreferredSize(preferredButtonSize);
        }

        buttonPanel.add(button);

        // May require a layout update:
        getFieldComponent().revalidate();
        getFieldComponent().repaint();

        return this;
    }

    /**
     * Allows callers to set a border on the button panel. The default is no border.
     */
    public ListField<T> setButtonPanelBorder(Border border) {
        buttonPanel.setBorder(border);
        return this;
    }

    /**
     * Gets the preferred size of buttons in the button panel, or null if buttons
     * should size to their content.
     */
    public Dimension getButtonPreferredSize() {
        return preferredButtonSize;
    }

    /**
     * Allow changing the preferred size of buttons in the button panel.
     * By default, buttons will size to their content.
     */
    public ListField<T> setButtonPreferredSize(Dimension dim) {
        // Update any existing buttons in the panel:
        boolean anyWereUpdated = false;
        for (int i = 0; i < buttonPanel.getComponentCount(); i++) {
            if (buttonPanel.getComponent(i) instanceof JButton) {
                buttonPanel.getComponent(i).setPreferredSize(dim);
                anyWereUpdated = true;
            }
        }

        // Update the UI if we changed any button sizes:
        if (anyWereUpdated) {
            // revalidate/repaint the whole wrapper panel, just to be safe:
            getFieldComponent().revalidate();
            getFieldComponent().repaint();
        }

        // Now save this value for any future buttons added to the panel:
        preferredButtonSize = dim;

        return this;
    }

    /**
     * One of FlowLayout's alignment options: LEFT, CENTER, RIGHT, LEADING, or TRAILING
     * from the FlowLayout class.
     */
    public int getButtonAlignment() {
        return layout.getAlignment();
    }

    /**
     * One of FlowLayout's alignment options: LEFT, CENTER, RIGHT, LEADING, or TRAILING
     * from the FlowLayout class.
     */
    public ListField<T> setButtonAlignment(int alignment) {
        layout.setAlignment(alignment);
        return this;
    }

    /**
     * Gets the horizontal gap between buttons.
     */
    public int getButtonHgap() {
        return layout.getHgap();
    }

    /**
     * Gets the vertical gap between button rows.
     */
    public int getButtonVgap() {
        return layout.getVgap();
    }

    /**
     * Sets the horizontal gap between buttons, and between buttons and the edge of the containing panel.
     */
    public ListField<T> setButtonHgap(int hgap) {
        layout.setHgap(hgap);
        return this;
    }

    /**
     * Sets the vertical gap between buttons, and between buttons and the edge of the containing panel.
     */
    public ListField<T> setButtonVgap(int vgap) {
        layout.setVgap(vgap);
        return this;
    }

    /**
     * Invoked internally to build our wrapper panel, our button panel, and our scroll pane.
     */
    private JComponent buildComponent() {
        JPanel wrapperPanel = new JPanel(new BorderLayout());

        // Create an initially empty button panel at the bottom:
        layout = new FlowLayout(DEFAULT_BUTTON_ALIGNMENT, DEFAULT_BUTTON_HGAP, DEFAULT_BUTTON_VGAP);
        buttonPanel = new JPanel(layout);
        buttonPosition = ButtonPosition.BOTTOM;
        wrapperPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Wrap our list in a scroll pane:
        JScrollPane scrollPane = PropertiesDialog.buildScrollPane(list);
        wrapperPanel.add(scrollPane, BorderLayout.CENTER);

        return wrapperPanel;
    }

    protected static <T> DefaultListModel<T> createDefaultListModel(List<T> items) {
        DefaultListModel<T> model = new DefaultListModel<>();
        model.addAll(items);
        return model;
    }
}
