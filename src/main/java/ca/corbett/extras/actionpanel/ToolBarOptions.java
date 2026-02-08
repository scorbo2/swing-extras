package ca.corbett.extras.actionpanel;

import ca.corbett.forms.SwingFormsResources;

import javax.swing.ImageIcon;

/**
 * This class only exists to relieve some clutter from the ActionPanel class,
 * which is getting quite large. The idea is that you can use actionPanel.getToolBarOptions()
 * to get an instance of this class, and then use the methods in this class to set options for the toolbar,
 * rather than having all these options in the ActionPanel class itself. Note that
 * toolbar enabled/disabled is still a first-class property in ActionPanel itself,
 * as that's fairly fundamental. All other toolbar-related options are here.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
 */
public final class ToolBarOptions {

    /**
     * Our buttons are placed in a container panel at the bottom of each ActionGroup.
     * We can align them FlowLayout-style left, right, or center, or we can put them in a BoxLayout to
     * stretch them evenly across the entire width of the container. If the ButtonPosition is
     * anything other than Stretch, then you can also control the spacing between buttons
     * via the ActionPanel's setInternalPadding() method. In Stretch mode, the internal padding
     * is ignored and there is no spacing between buttons.
     */
    public enum ButtonPosition {
        AlignLeft, AlignRight, Center, Stretch
    }

    // Add item:
    private final ToolBarActionSupplier addItemActionSupplier;
    private ToolBarNewItemSupplier newItemSupplier;
    private boolean allowItemAdd;
    private ImageIcon addItemIcon;

    // Rename group:
    private ToolBarAction groupRenameAction;
    private boolean allowGroupRename;

    private boolean allowItemReorder;
    private boolean allowItemRemoval;
    private boolean allowGroupRemoval;

    private int iconSize;
    private ButtonPosition buttonPosition;

    /**
     * Should only be instantiated by ActionPanel.
     */
    ToolBarOptions() {
        this.allowItemAdd = true;
        this.newItemSupplier = null;
        addItemIcon = SwingFormsResources.getAddIcon(SwingFormsResources.NATIVE_SIZE);
        this.addItemActionSupplier = this::createItemAddAction;

        // Everything is allowed by default, but these options will still
        // be invisible if there is no corresponding action provided:
        this.allowItemAdd = true;
        this.allowGroupRename = true;
        this.allowItemReorder = true;
        this.allowItemRemoval = true;
        this.allowGroupRemoval = true;

        // We can supply some actions out of the box:
        this.groupRenameAction = new ToolBarGroupRenameAction(null, null, null, null);

        // We supply default icons, but callers can override these if they want:
        this.renameIcon = SwingFormsResources.getRenameIcon(SwingFormsResources.NATIVE_SIZE);
        this.editIcon = SwingFormsResources.getEditIcon(SwingFormsResources.NATIVE_SIZE);
        this.removeIcon = SwingFormsResources.getRemoveAllIcon(SwingFormsResources.NATIVE_SIZE);

        iconSize = ActionPanel.DEFAULT_ICON_SIZE;
        buttonPosition = ButtonPosition.Stretch;
    }

    /**
     * If allowItemAdd is true and a Supplier is given here, then an "Add item" button will
     * be shown in the toolbar. Clicking it will invoke the Supplier to get a new action to add.
     * If allowItemAdd is false, then no "Add item" button will be shown, even if a Supplier is given here.
     * <p>
     * If your supplier returns null, then it is assumed that the user canceled the add action,
     * so no change will be made. Otherwise, the new action will either be added to the end of the
     * current group, or will be sorted within the group, depending on the ActionPanel's sorting options.
     * </p>
     * <p>
     * A built-in "add" icon will be provided by default, as our buttons never show text.
     * You can change the icon via setAddItemIcon().
     * </p>
     *
     * @param newActionSupplier A ToolBarNewItemSupplier instance. The supplier can return null to cancel the add.
     * @return this ToolBarOptions instance, for method chaining.
     */
    public ToolBarOptions setNewActionSupplier(ToolBarNewItemSupplier newActionSupplier) {
        this.newItemSupplier = newActionSupplier;
        return this;
    }

    /**
     * If true, and if a Supplier is given via setNewActionSupplier(), then an "Add item"
     * button will be shown in the toolbar. Setting this to false does not remove the Supplier,
     * so you can later toggle it back to true without having to set the Supplier again.
     * If false, then no "Add item" button will be shown, even if a Supplier is given.
     *
     * @param allowItemAdd Whether to allow adding items to the toolbar. Must also call setNewActionSupplier()!
     * @return this ToolBarOptions instance, for method chaining.
     */
    public ToolBarOptions setAllowItemAdd(boolean allowItemAdd) {
        this.allowItemAdd = allowItemAdd;
        return this;
    }

    /**
     * Overrides the default "add" icon for the "Add item" button.
     * This only has an effect if allowItemAdd is true and a Supplier is given via setNewActionSupplier().
     *
     * @param addItemIcon The icon to use for the "Add item" button. Cannot be null.
     * @return this ToolBarOptions instance, for method chaining.
     */
    public ToolBarOptions setAddItemIcon(ImageIcon addItemIcon) {
        if (addItemIcon == null) {
            throw new IllegalArgumentException("addItemIcon cannot be null");
        }
        this.addItemIcon = addItemIcon;
        return this;
    }

    /**
     * Overrides the default "rename" icon for the "Rename group" button.
     * This only has an effect if allowGroupRename is true.
     *
     * @param renameIcon The icon to use for the "Rename group" button. Cannot be null.
     * @return this ToolBarOptions instance, for method chaining.
     */
    public ToolBarOptions setRenameIcon(ImageIcon renameIcon) {
        if (renameIcon == null) {
            throw new IllegalArgumentException("renameIcon cannot be null");
        }
        this.renameIcon = renameIcon;
        return this;
    }

    /**
     * Overrides the default "edit" icon for the "Edit item" button.
     * This only has an effect if allowItemReorder is true.
     *
     * @param editIcon The icon to use for the "Edit item" button. Cannot be null.
     * @return this ToolBarOptions instance, for method chaining.
     */
    public ToolBarOptions setEditIcon(ImageIcon editIcon) {
        if (editIcon == null) {
            throw new IllegalArgumentException("editIcon cannot be null");
        }
        this.editIcon = editIcon;
        return this;
    }

    /**
     * Overrides the default "remove" icon for the "Remove item" and "Remove group" buttons.
     * This only has an effect if allowItemRemoval or allowGroupRemoval is true.
     *
     * @param removeIcon The icon to use for the "Remove item" and "Remove group" buttons. Cannot be null.
     * @return this ToolBarOptions instance, for method chaining.
     */
    public ToolBarOptions setRemoveIcon(ImageIcon removeIcon) {
        if (removeIcon == null) {
            throw new IllegalArgumentException("removeIcon cannot be null");
        }
        this.removeIcon = removeIcon;
        return this;
    }

    /**
     * If allowItemAdd is true and a Supplier is given via setNewActionSupplier(), then this method will return
     * a ToolBarAction for adding a new item to the given ActionGroup. Otherwise, it returns null.
     *
     * @param actionPanel The ActionPanel that will be supplied to the Supplier when it is invoked.
     * @param groupName   The case-insensitive group name that will be supplied to the Supplier when it is invoked.
     * @return A ToolBarAction for adding a new item to the given ActionGroup. Might be null.
     */
    public ToolBarAction createItemAddAction(ActionPanel actionPanel, String groupName) {
        if (!allowItemAdd || newItemSupplier == null) {
            return null; // Not allowed or no supplier, so no action.
        }
        ToolBarAddItemAction addAction = new ToolBarAddItemAction(actionPanel, groupName);
        addAction.setNewItemSupplier(newItemSupplier); // Set the supplier for the add action
        addAction.setIcon(addItemIcon); // Use the current add item icon
        return addAction;
    }
}
