package ca.corbett.forms.actions;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.forms.fields.ListField;

import javax.swing.Icon;
import java.awt.event.ActionEvent;

/**
 * An EnhancedAction that can be used with any ListField to clear all items from the list.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.7
 */
public class ListItemClearAction extends EnhancedAction {
    private static final String DEFAULT_NAME = "Clear";
    private final ListField<?> listField;


    /**
     * Creates a new ListItemClearAction with the default name and the given ListField.
     *
     * @param listField The ListField from which selected items will be removed. Must not be null.
     */
    public ListItemClearAction(ListField<?> listField) {
        this(DEFAULT_NAME, listField);
    }

    /**
     * Creates a new ListItemClearAction with the specified name and ListField.
     *
     * @param name      The name for this action.
     * @param listField The ListField from which selected items will be removed. Must not be null.
     */
    public ListItemClearAction(String name, ListField<?> listField) {
        super(name);
        this.listField = listField;
        setTooltip("Clear all items from the list");
        if (listField == null) {
            throw new IllegalArgumentException("listField must not be null");
        }
    }

    /**
     * Creates a new ListItemClearAction with the specified icon and ListField.
     *
     * @param icon      The icon for this action.
     * @param listField The ListField from which selected items will be removed. Must not be null.
     */
    public ListItemClearAction(Icon icon, ListField<?> listField) {
        super(null, icon);
        this.listField = listField;
        setTooltip("Clear all items from the list");
        if (listField == null) {
            throw new IllegalArgumentException("listField must not be null");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        listField.getListModel().clear();
    }
}
