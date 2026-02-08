package ca.corbett.extras.actionpanel;

import ca.corbett.extras.image.ImageUtil;
import ca.corbett.forms.SwingFormsResources;

import javax.swing.ImageIcon;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class only exists to relieve some clutter from the ActionPanel class,
 * which is getting quite large. The idea is that you can use actionPanel.getToolBarOptions()
 * to get an instance of this class, and then use the methods in this class to set options for the toolbar,
 * rather than having all these options in the ActionPanel class itself. Note that
 * toolbar enabled/disabled is still a first-class property in ActionPanel itself,
 * as that's fairly fundamental. All other toolbar-related options are here.
 * <p>
 *     <b>Button positioning</b> - use setButtonPosition() to control how the buttons are aligned
 *        within their container panel. The default is Stretch, which stretches the buttons evenly
 *        across the entire width of the container panel. If you choose any other option, then the
 *        buttons will be aligned FlowLayout-style, and you can control the spacing between them
 *        via ActionPanel's setInternalPadding() method. In Stretch mode, there is no spacing
 *        between buttons and the internal padding is ignored.
 * </p>
 * <p>
 *     <b>Built-in actions</b> - there are several "built-in" actions that you can use
 *     in the ToolBar: "Add item", "Rename group", "Edit group", and "Remove group".
 *     These are all enabled by default, but can be selectively disabled via the setAllow...() methods.
 *     If enabled, they will appear in the toolbar for each ActionGroup. You can change the icons for
 *     these built-in actions via the appropriate setter methods in this class.
 *     The "Add item" action requires a ToolBarNewItemSupplier to be provided via setNewActionSupplier()
 *     in order to appear in the toolbar, but the other built-in actions do not require any suppliers.
 * </p>
 * <p>
 *     <b>Custom actions</b> - you can also add your own custom actions to the toolbar for each ActionGroup by adding
 *     ToolBarActionSuppliers via the addCustomActionSupplier() method. Each supplier will be invoked for each
 *     ActionGroup when the toolbar is built, and can return a ToolBarAction to add to that group. You can add
 *     as many suppliers as you want, and they will be invoked in the order they were added.
 *     Your custom ToolBarActions must supply icons! Our buttons are icon-based and do not show text.
 * </p>
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
    private ToolBarNewItemSupplier newItemSupplier;
    private boolean allowItemAdd;
    private ImageIcon addItemIcon;

    // Rename group:
    private boolean allowGroupRename;
    private ImageIcon groupRenameIcon;

    // Item reordering and removal are handled by the same internal action:
    private boolean allowItemReorder;
    private boolean allowItemRemoval;
    private ImageIcon groupEditIcon;

    // Group removal:
    private boolean allowGroupRemoval;
    private ImageIcon groupRemoveIcon;

    // Callers can supply their own actions to be added to the toolbar:
    private final List<ToolBarActionSupplier> actionSuppliers = new CopyOnWriteArrayList<>();

    // General options:
    private int iconSize;
    private ButtonPosition buttonPosition;

    /**
     * Should only be instantiated by ActionPanel.
     */
    ToolBarOptions() {
        // Add item is enabled by default, but will not appear in the ToolBar
        // unless a Supplier is provided via setNewActionSupplier():
        this.allowItemAdd = true;
        this.newItemSupplier = null;
        this.addItemIcon = SwingFormsResources.getAddIcon(SwingFormsResources.NATIVE_SIZE);

        // We supply a built-in group rename action that is enabled by default:
        this.allowGroupRename = true;
        this.groupRenameIcon = SwingFormsResources.getRenameIcon(SwingFormsResources.NATIVE_SIZE);

        // Item reordering and removal are also enabled by default with a built-in action:
        this.allowItemReorder = true;
        this.allowItemRemoval = true;
        this.groupEditIcon = SwingFormsResources.getEditIcon(SwingFormsResources.NATIVE_SIZE);

        // Group removal is enabled by default with a built-in action:
        this.allowGroupRemoval = true;
        this.groupRemoveIcon = SwingFormsResources.getRemoveAllIcon(SwingFormsResources.NATIVE_SIZE);

        // General options:
        iconSize = ActionPanel.DEFAULT_ICON_SIZE;
        buttonPosition = ButtonPosition.Stretch;
    }

    /**
     * Returns the current ButtonPosition for this toolbar.
     * This controls how the buttons are aligned within their container panel.
     *
     * @return The current ButtonPosition for this toolbar.
     */
    public ButtonPosition getButtonPosition() {
        return buttonPosition;
    }

    /**
     * Decides how buttons are to be presented in the ToolBar.
     * The default is Stretch, which stretches the buttons evenly across the entire width of the container panel.
     *
     * @param buttonPosition The ButtonPosition to use for this toolbar. Cannot be null.
     * @return this ToolBarOptions instance, for method chaining.
     */
    public ToolBarOptions getButtonPosition(ButtonPosition buttonPosition) {
        if (buttonPosition == null) {
            throw new IllegalArgumentException("buttonPosition cannot be null");
        }
        this.buttonPosition = buttonPosition;
        return this;
    }

    /**
     * Returns the current icon size for this toolbar. This controls the size of all icons in the toolbar, including
     * the built-in action icons and any custom action icons. The default is ActionPanel.DEFAULT_ICON_SIZE.
     *
     * @return The current icon size for this toolbar.
     */
    public int getIconSize() {
        return iconSize;
    }

    /**
     * Sets the icon size for this toolbar. This controls the size of all icons in the toolbar, including
     * the built-in action icons and any custom action icons. The default is ActionPanel.DEFAULT_ICON_SIZE.
     *
     * @param iconSize The icon size to use for this toolbar. Must be a positive integer.
     * @return this ToolBarOptions instance, for method chaining.
     */
    public ToolBarOptions setIconSize(int iconSize) {
        if (iconSize <= 0) {
            throw new IllegalArgumentException("iconSize must be a positive integer");
        }
        this.iconSize = iconSize;
        return this;
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
     * You can add your own buttons to the ToolBar in addition to (or instead of, if you disable them)
     * the built-in ones. The given ToolBarActionSupplier will be invoked for each ActionGroup when
     * the toolbar is built, and should return ToolBarAction to add to that group. You can add as
     * many suppliers as you want, and they will be invoked in the order they were added. If a supplier returns
     * null for a given group, it is ignored.
     *
     * @param actionSupplier A ToolBarActionSupplier that will be invoked for each ActionGroup when the toolbar is built. Cannot be null.
     * @return this ToolBarOptions instance, for method chaining.
     */
    public ToolBarOptions addCustomActionSupplier(ToolBarActionSupplier actionSupplier) {
        if (actionSupplier == null) {
            throw new IllegalArgumentException("actionSupplier cannot be null");
        }
        this.actionSuppliers.add(actionSupplier);
        return this;
    }

    /**
     * Removes a custom action supplier that was previously added via addCustomActionSupplier(). If the given supplier
     * was not previously added, then this method does nothing. If the given supplier is null,
     * then an IllegalArgumentException is thrown.
     *
     * @param actionSupplier The ToolBarActionSupplier to remove. Cannot be null.
     * @return this ToolBarOptions instance, for method chaining.
     */
    public ToolBarOptions removeCustomActionSupplier(ToolBarActionSupplier actionSupplier) {
        if (actionSupplier == null) {
            throw new IllegalArgumentException("actionSupplier cannot be null");
        }
        this.actionSuppliers.remove(actionSupplier);
        return this;
    }

    /**
     * Removes all custom action suppliers that were previously added via addCustomActionSupplier().
     *
     * @return this ToolBarOptions instance, for method chaining.
     */
    public ToolBarOptions clearCustomActionSuppliers() {
        this.actionSuppliers.clear();
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
     * If true, then an "Edit group" button will be shown in the toolbar for each ActionGroup, which allows the user
     * to reorder and remove items from that group.
     *
     * @param allowItemReorder Whether to allow reordering and removing items in the toolbar. Must also call setAllowItemRemoval() to allow item removal!
     * @return this ToolBarOptions instance, for method chaining.
     */
    public ToolBarOptions setAllowItemReorder(boolean allowItemReorder) {
        this.allowItemReorder = allowItemReorder;
        return this;
    }

    /**
     * If true, then a "Remove item" button will be shown in the toolbar for each ActionGroup, which allows the user
     * to remove items from that group.
     *
     * @param allowGroupRename Whether to allow removing items in the toolbar. Must also call setAllowItemReorder() to allow item reordering!
     * @return this ToolBarOptions instance, for method chaining.
     */
    public ToolBarOptions setAllowGroupRename(boolean allowGroupRename) {
        this.allowGroupRename = allowGroupRename;
        return this;
    }

    /**
     * If true, then a "Remove item" button will be shown in the toolbar for each ActionGroup, which allows the user
     * to remove items from that group.
     *
     * @param allowItemRemoval Whether to allow removing items in the toolbar. Must also call setAllowItemReorder() to allow item reordering!
     * @return this ToolBarOptions instance, for method chaining.
     */
    public ToolBarOptions setAllowItemRemoval(boolean allowItemRemoval) {
        this.allowItemRemoval = allowItemRemoval;
        return this;
    }

    /**
     * If true, then a "Remove group" button will be shown in the toolbar for each ActionGroup, which allows the user
     * to remove that group.
     *
     * @param allowGroupRemoval Whether to allow removing groups in the toolbar.
     * @return this ToolBarOptions instance, for method chaining.
     */
    public ToolBarOptions setAllowGroupRemoval(boolean allowGroupRemoval) {
        this.allowGroupRemoval = allowGroupRemoval;
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
        this.groupRenameIcon = renameIcon;
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
        this.groupEditIcon = editIcon;
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
        this.groupRemoveIcon = removeIcon;
        return this;
    }

    /**
     * Indicates whether the toolbar should show an "Add item" button for each ActionGroup.
     * If true, then an "Add item" button will be shown for each ActionGroup.
     * You must also supply a ToolBarNewItemSupplier via setNewActionSupplier()!
     * Otherwise, the "add item" button will not appear.
     *
     * @return Whether the toolbar should show an "Add item" button for each ActionGroup.
     */
    public boolean isAllowItemAdd() {
        return allowItemAdd;
    }

    /**
     * Indicates whether the built-in "Rename group" action is enabled.
     * If true, then a "Rename group" button will be shown for each ActionGroup.
     *
     * @return Whether the built-in "Rename group" action is enabled.
     */
    public boolean isAllowGroupRename() {
        return allowGroupRename;
    }

    /**
     * Indicates whether the built-in "Edit group" action for reordering and removing items is enabled.
     * If true, then an "Edit group" button will be shown for each ActionGroup, which allows the user
     * to reorder and remove items from that group.
     *
     * @return Whether the built-in "Edit group" action for reordering and removing items is enabled.
     */
    public boolean isAllowItemReorder() {
        return allowItemReorder;
    }

    /**
     * Indicates whether the built-in "Edit group" action for reordering and removing items is enabled.
     * If true, then an "Edit group" button will be shown for each ActionGroup, which allows the user
     * to reorder and remove items from that group.
     *
     * @return Whether the built-in "Edit group" action for reordering and removing items is enabled.
     */
    public boolean isAllowGroupRemoval() {
        return allowGroupRemoval;
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
        addAction.setIcon(ImageUtil.scaleIcon(addItemIcon, iconSize)); // Scale the current add item icon
        return addAction;
    }

    /**
     * If allowGroupRename is true, then this method will return a ToolBarAction for renaming the given ActionGroup.
     * Otherwise, it returns null. The built-in rename action handles name conflicts automatically, and won't
     * allow the rename if the new name (case-insensitive) is already in use by some other group.
     * You can change the icon for the rename action via setRenameIcon(), and you can disable the rename action
     * entirely via setAllowGroupRename(false).
     *
     * @param actionPanel The ActionPanel that will be supplied to the ToolBarGroupRenameAction when it is invoked.
     * @param groupName   The case-insensitive group name that will be supplied to the ToolBarGroupRenameAction when it is invoked.
     * @return A ToolBarAction for renaming the given ActionGroup. Might be null.
     */
    public ToolBarAction createRenameGroupAction(ActionPanel actionPanel, String groupName) {
        if (!allowGroupRename) {
            return null; // Not allowed, so no action.
        }
        ImageIcon scaledIcon = ImageUtil.scaleIcon(groupRenameIcon, iconSize); // Scale the current rename group icon
        return new ToolBarGroupRenameAction(actionPanel, groupName, "Rename group", scaledIcon);
    }

    /**
     * If allowItemReorder or allowItemRemoval is true, then this method will return a ToolBarAction
     * for editing the items in the given ActionGroup.
     *
     * @param actionPanel The ActionPanel that will be supplied to the ToolBarGroupEditAction when it is invoked.
     * @param groupName   The case-insensitive group name that will be supplied to the ToolBarGroupEditAction when it is invoked.
     * @return A ToolBarAction for editing the items in the given ActionGroup. Might be null.
     */
    public ToolBarAction createEditGroupAction(ActionPanel actionPanel, String groupName) {
        if (!allowItemReorder && !allowItemRemoval) {
            return null; // Not allowed, so no action.
        }
        ImageIcon scaledIcon = ImageUtil.scaleIcon(groupEditIcon, iconSize); // Scale the current edit group icon
        return new ToolBarGroupEditAction(actionPanel, groupName, "Edit group", scaledIcon);
    }

    /**
     * If allowGroupRemoval is true, then this method will return a ToolBarAction for removing the given ActionGroup.
     * Otherwise, it returns null.
     *
     * @param actionPanel The ActionPanel that will be supplied to the ToolBarGroupRemoveAction when it is invoked.
     * @param groupName   The case-insensitive group name that will be supplied to the ToolBarGroupRemoveAction when it is invoked.
     * @return A ToolBarAction for removing the given ActionGroup. Might be null.
     */
    public ToolBarAction createRemoveGroupAction(ActionPanel actionPanel, String groupName) {
        if (!allowGroupRemoval) {
            return null; // Not allowed, so no action.
        }
        ImageIcon scaledIcon = ImageUtil.scaleIcon(groupRemoveIcon, iconSize); // Scale the current remove group icon
        return new ToolBarGroupRemoveAction(actionPanel, groupName, "Remove group", scaledIcon);
    }

    /**
     * Returns a list of custom ToolBarActions to add to the toolbar for the given ActionGroup. This is done by invoking
     * each ToolBarActionSupplier that was added via addCustomActionSupplier() and collecting the
     * non-null results. If no suppliers were added, or if all suppliers returned null for the given group,
     * then this method returns an empty list.
     *
     * @param actionPanel The ActionPanel that will be supplied to each ToolBarActionSupplier when it is invoked.
     * @param groupName   The case-insensitive group name that will be supplied to each ToolBarActionSupplier when it is invoked.
     * @return A list of custom ToolBarActions to add to the toolbar for the given ActionGroup. Never null, but might be empty.
     */
    public List<ToolBarAction> createCustomActions(ActionPanel actionPanel, String groupName) {
        List<ToolBarAction> customActions = new ArrayList<>();
        for (ToolBarActionSupplier supplier : actionSuppliers) {
            ToolBarAction action = supplier.get(actionPanel, groupName);
            if (action != null) {
                customActions.add(action);
            }
        }
        return customActions;
    }
}
