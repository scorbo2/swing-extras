package ca.corbett.extras.actionpanel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for ToolBarOptions.
 */
class ToolBarOptionsTest {

    private ActionPanel actionPanel;
    private ToolBarOptions toolBarOptions;

    @BeforeEach
    void setUp() {
        actionPanel = new ActionPanel();
        toolBarOptions = actionPanel.getToolBarOptions();
    }

    // --- Default values ---

    @Test
    void defaults_buttonPositionShouldBeStretch() {
        assertEquals(ToolBarOptions.ButtonPosition.Stretch, toolBarOptions.getButtonPosition(),
                "Default button position should be Stretch");
    }

    @Test
    void defaults_iconSizeShouldBeDefaultIconSize() {
        assertEquals(ActionPanel.DEFAULT_ICON_SIZE, toolBarOptions.getIconSize(),
                "Default icon size should be ActionPanel.DEFAULT_ICON_SIZE");
    }

    @Test
    void defaults_allowItemAddShouldBeTrue() {
        assertTrue(toolBarOptions.isAllowItemAdd(), "Allow item add should be true by default");
    }

    @Test
    void defaults_allowGroupRenameShouldBeTrue() {
        assertTrue(toolBarOptions.isAllowGroupRename(), "Allow group rename should be true by default");
    }

    @Test
    void defaults_allowItemReorderShouldBeTrue() {
        assertTrue(toolBarOptions.isAllowItemReorder(), "Allow item reorder should be true by default");
    }

    @Test
    void defaults_allowItemRemovalShouldBeTrue() {
        assertTrue(toolBarOptions.isAllowItemRemoval(), "Allow item removal should be true by default");
    }

    @Test
    void defaults_allowGroupRemovalShouldBeTrue() {
        assertTrue(toolBarOptions.isAllowGroupRemoval(), "Allow group removal should be true by default");
    }

    @Test
    void defaults_newActionSupplierShouldBeNull() {
        assertNull(toolBarOptions.getNewActionSupplier(), "New action supplier should be null by default");
    }

    // --- setButtonPosition ---

    @Test
    void setButtonPosition_withNull_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> toolBarOptions.setButtonPosition(null),
                "setButtonPosition(null) should throw IllegalArgumentException");
    }

    @Test
    void setButtonPosition_withAlignLeft_shouldSetPosition() {
        toolBarOptions.setButtonPosition(ToolBarOptions.ButtonPosition.AlignLeft);
        assertEquals(ToolBarOptions.ButtonPosition.AlignLeft, toolBarOptions.getButtonPosition(),
                "Button position should be AlignLeft");
    }

    @Test
    void setButtonPosition_withAlignRight_shouldSetPosition() {
        toolBarOptions.setButtonPosition(ToolBarOptions.ButtonPosition.AlignRight);
        assertEquals(ToolBarOptions.ButtonPosition.AlignRight, toolBarOptions.getButtonPosition(),
                "Button position should be AlignRight");
    }

    @Test
    void setButtonPosition_withCenter_shouldSetPosition() {
        toolBarOptions.setButtonPosition(ToolBarOptions.ButtonPosition.Center);
        assertEquals(ToolBarOptions.ButtonPosition.Center, toolBarOptions.getButtonPosition(),
                "Button position should be Center");
    }

    @Test
    void setButtonPosition_withStretch_shouldSetPosition() {
        toolBarOptions.setButtonPosition(ToolBarOptions.ButtonPosition.AlignLeft);
        toolBarOptions.setButtonPosition(ToolBarOptions.ButtonPosition.Stretch);
        assertEquals(ToolBarOptions.ButtonPosition.Stretch, toolBarOptions.getButtonPosition(),
                "Button position should be Stretch");
    }

    @Test
    void setButtonPosition_shouldReturnSameInstance_forMethodChaining() {
        assertSame(toolBarOptions, toolBarOptions.setButtonPosition(ToolBarOptions.ButtonPosition.AlignLeft),
                "setButtonPosition should return the same instance for method chaining");
    }

    // --- setIconSize ---

    @Test
    void setIconSize_withZero_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> toolBarOptions.setIconSize(0),
                "setIconSize(0) should throw IllegalArgumentException");
    }

    @Test
    void setIconSize_withNegativeValue_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> toolBarOptions.setIconSize(-1),
                "setIconSize(-1) should throw IllegalArgumentException");
    }

    @Test
    void setIconSize_withPositiveValue_shouldSetSize() {
        toolBarOptions.setIconSize(32);
        assertEquals(32, toolBarOptions.getIconSize(), "Icon size should be 32");
    }

    @Test
    void setIconSize_shouldReturnSameInstance_forMethodChaining() {
        assertSame(toolBarOptions, toolBarOptions.setIconSize(32),
                "setIconSize should return the same instance for method chaining");
    }

    // --- setAddItemIcon ---

    @Test
    void setAddItemIcon_withNull_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> toolBarOptions.setAddItemIcon(null),
                "setAddItemIcon(null) should throw IllegalArgumentException");
    }

    @Test
    void setAddItemIcon_withValidIcon_shouldReturnSameInstance_forMethodChaining() {
        ImageIcon icon = createTestIcon();
        assertSame(toolBarOptions, toolBarOptions.setAddItemIcon(icon),
                "setAddItemIcon should return the same instance for method chaining");
    }

    // --- setRenameIcon ---

    @Test
    void setRenameIcon_withNull_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> toolBarOptions.setRenameIcon(null),
                "setRenameIcon(null) should throw IllegalArgumentException");
    }

    @Test
    void setRenameIcon_withValidIcon_shouldReturnSameInstance_forMethodChaining() {
        ImageIcon icon = createTestIcon();
        assertSame(toolBarOptions, toolBarOptions.setRenameIcon(icon),
                "setRenameIcon should return the same instance for method chaining");
    }

    // --- setEditIcon ---

    @Test
    void setEditIcon_withNull_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> toolBarOptions.setEditIcon(null),
                "setEditIcon(null) should throw IllegalArgumentException");
    }

    @Test
    void setEditIcon_withValidIcon_shouldReturnSameInstance_forMethodChaining() {
        ImageIcon icon = createTestIcon();
        assertSame(toolBarOptions, toolBarOptions.setEditIcon(icon),
                "setEditIcon should return the same instance for method chaining");
    }

    // --- setRemoveIcon ---

    @Test
    void setRemoveIcon_withNull_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> toolBarOptions.setRemoveIcon(null),
                "setRemoveIcon(null) should throw IllegalArgumentException");
    }

    @Test
    void setRemoveIcon_withValidIcon_shouldReturnSameInstance_forMethodChaining() {
        ImageIcon icon = createTestIcon();
        assertSame(toolBarOptions, toolBarOptions.setRemoveIcon(icon),
                "setRemoveIcon should return the same instance for method chaining");
    }

    // --- setAllowItemAdd / isAllowItemAdd ---

    @Test
    void setAllowItemAdd_withFalse_shouldDisallow() {
        toolBarOptions.setAllowItemAdd(false);
        assertFalse(toolBarOptions.isAllowItemAdd(), "Allow item add should be false");
    }

    @Test
    void setAllowItemAdd_withTrue_shouldAllow() {
        toolBarOptions.setAllowItemAdd(false);
        toolBarOptions.setAllowItemAdd(true);
        assertTrue(toolBarOptions.isAllowItemAdd(), "Allow item add should be true");
    }

    @Test
    void setAllowItemAdd_shouldReturnSameInstance_forMethodChaining() {
        assertSame(toolBarOptions, toolBarOptions.setAllowItemAdd(false),
                "setAllowItemAdd should return the same instance for method chaining");
    }

    // --- setAllowGroupRename / isAllowGroupRename ---

    @Test
    void setAllowGroupRename_withFalse_shouldDisallow() {
        toolBarOptions.setAllowGroupRename(false);
        assertFalse(toolBarOptions.isAllowGroupRename(), "Allow group rename should be false");
    }

    @Test
    void setAllowGroupRename_shouldReturnSameInstance_forMethodChaining() {
        assertSame(toolBarOptions, toolBarOptions.setAllowGroupRename(false),
                "setAllowGroupRename should return the same instance for method chaining");
    }

    // --- setAllowItemReorder / isAllowItemReorder ---

    @Test
    void setAllowItemReorder_withFalse_shouldDisallow() {
        toolBarOptions.setAllowItemReorder(false);
        assertFalse(toolBarOptions.isAllowItemReorder(), "Allow item reorder should be false");
    }

    @Test
    void setAllowItemReorder_shouldReturnSameInstance_forMethodChaining() {
        assertSame(toolBarOptions, toolBarOptions.setAllowItemReorder(false),
                "setAllowItemReorder should return the same instance for method chaining");
    }

    // --- setAllowItemRemoval / isAllowItemRemoval ---

    @Test
    void setAllowItemRemoval_withFalse_shouldDisallow() {
        toolBarOptions.setAllowItemRemoval(false);
        assertFalse(toolBarOptions.isAllowItemRemoval(), "Allow item removal should be false");
    }

    @Test
    void setAllowItemRemoval_shouldReturnSameInstance_forMethodChaining() {
        assertSame(toolBarOptions, toolBarOptions.setAllowItemRemoval(false),
                "setAllowItemRemoval should return the same instance for method chaining");
    }

    // --- setAllowGroupRemoval / isAllowGroupRemoval ---

    @Test
    void setAllowGroupRemoval_withFalse_shouldDisallow() {
        toolBarOptions.setAllowGroupRemoval(false);
        assertFalse(toolBarOptions.isAllowGroupRemoval(), "Allow group removal should be false");
    }

    @Test
    void setAllowGroupRemoval_shouldReturnSameInstance_forMethodChaining() {
        assertSame(toolBarOptions, toolBarOptions.setAllowGroupRemoval(false),
                "setAllowGroupRemoval should return the same instance for method chaining");
    }

    // --- setNewActionSupplier / getNewActionSupplier ---

    @Test
    void setNewActionSupplier_shouldStoreSupplier() {
        ToolBarNewItemSupplier supplier = (panel, groupName) -> null;
        toolBarOptions.setNewActionSupplier(supplier);
        assertSame(supplier, toolBarOptions.getNewActionSupplier(), "Supplier should be stored");
    }

    @Test
    void setNewActionSupplier_withNull_shouldClearSupplier() {
        ToolBarNewItemSupplier supplier = (panel, groupName) -> null;
        toolBarOptions.setNewActionSupplier(supplier);
        toolBarOptions.setNewActionSupplier(null);
        assertNull(toolBarOptions.getNewActionSupplier(), "Supplier should be null after clearing");
    }

    @Test
    void setNewActionSupplier_shouldReturnSameInstance_forMethodChaining() {
        assertSame(toolBarOptions, toolBarOptions.setNewActionSupplier((panel, groupName) -> null),
                "setNewActionSupplier should return the same instance for method chaining");
    }

    // --- addCustomActionSupplier / removeCustomActionSupplier / clearCustomActionSuppliers ---

    @Test
    void addCustomActionSupplier_withNull_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> toolBarOptions.addCustomActionSupplier(null),
                "addCustomActionSupplier(null) should throw IllegalArgumentException");
    }

    @Test
    void removeCustomActionSupplier_withNull_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> toolBarOptions.removeCustomActionSupplier(null),
                "removeCustomActionSupplier(null) should throw IllegalArgumentException");
    }

    @Test
    void addCustomActionSupplier_shouldReturnSameInstance_forMethodChaining() {
        ToolBarActionSupplier supplier = (panel, groupName) -> null;
        assertSame(toolBarOptions, toolBarOptions.addCustomActionSupplier(supplier),
                "addCustomActionSupplier should return the same instance for method chaining");
    }

    @Test
    void removeCustomActionSupplier_shouldReturnSameInstance_forMethodChaining() {
        ToolBarActionSupplier supplier = (panel, groupName) -> null;
        toolBarOptions.addCustomActionSupplier(supplier);
        assertSame(toolBarOptions, toolBarOptions.removeCustomActionSupplier(supplier),
                "removeCustomActionSupplier should return the same instance for method chaining");
    }

    @Test
    void clearCustomActionSuppliers_shouldReturnSameInstance_forMethodChaining() {
        assertSame(toolBarOptions, toolBarOptions.clearCustomActionSuppliers(),
                "clearCustomActionSuppliers should return the same instance for method chaining");
    }

    // --- addExcludedGroup / removeExcludedGroup / isGroupExcluded ---

    @Test
    void addExcludedGroup_withNull_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> toolBarOptions.addExcludedGroup(null),
                "addExcludedGroup(null) should throw IllegalArgumentException");
    }

    @Test
    void removeExcludedGroup_withNull_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> toolBarOptions.removeExcludedGroup(null),
                "removeExcludedGroup(null) should throw IllegalArgumentException");
    }

    @Test
    void isGroupExcluded_withNull_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> toolBarOptions.isGroupExcluded(null),
                "isGroupExcluded(null) should throw IllegalArgumentException");
    }

    @Test
    void addExcludedGroup_shouldExcludeGroup() {
        toolBarOptions.addExcludedGroup("SpecialGroup");
        assertTrue(toolBarOptions.isGroupExcluded("SpecialGroup"), "Group should be excluded");
    }

    @Test
    void addExcludedGroup_isCaseInsensitive() {
        toolBarOptions.addExcludedGroup("SpecialGroup");
        assertTrue(toolBarOptions.isGroupExcluded("specialgroup"), "Group exclusion should be case-insensitive (lowercase check)");
        assertTrue(toolBarOptions.isGroupExcluded("SPECIALGROUP"), "Group exclusion should be case-insensitive (uppercase check)");
        assertTrue(toolBarOptions.isGroupExcluded("SpecialGroup"), "Group exclusion should be case-insensitive (mixed case check)");
    }

    @Test
    void removeExcludedGroup_shouldRemoveExclusion() {
        toolBarOptions.addExcludedGroup("SpecialGroup");
        toolBarOptions.removeExcludedGroup("SpecialGroup");
        assertFalse(toolBarOptions.isGroupExcluded("SpecialGroup"), "Group should no longer be excluded after removal");
    }

    @Test
    void removeExcludedGroup_isCaseInsensitive() {
        toolBarOptions.addExcludedGroup("SpecialGroup");
        toolBarOptions.removeExcludedGroup("SPECIALGROUP");
        assertFalse(toolBarOptions.isGroupExcluded("SpecialGroup"), "Removal should be case-insensitive");
    }

    @Test
    void removeExcludedGroup_withNonExistentGroup_shouldNotThrow() {
        // Should silently do nothing:
        toolBarOptions.removeExcludedGroup("NonExistentGroup");
        assertFalse(toolBarOptions.isGroupExcluded("NonExistentGroup"), "Non-existent group should not be excluded");
    }

    @Test
    void addExcludedGroup_shouldReturnSameInstance_forMethodChaining() {
        assertSame(toolBarOptions, toolBarOptions.addExcludedGroup("SomeGroup"),
                "addExcludedGroup should return the same instance for method chaining");
    }

    @Test
    void removeExcludedGroup_shouldReturnSameInstance_forMethodChaining() {
        toolBarOptions.addExcludedGroup("SomeGroup");
        assertSame(toolBarOptions, toolBarOptions.removeExcludedGroup("SomeGroup"),
                "removeExcludedGroup should return the same instance for method chaining");
    }

    // --- createItemAddAction ---

    @Test
    void createItemAddAction_withAllowFalse_shouldReturnNull() {
        toolBarOptions.setAllowItemAdd(false);
        toolBarOptions.setNewActionSupplier((panel, groupName) -> null);
        assertNull(toolBarOptions.createItemAddAction(actionPanel, "group1"),
                "createItemAddAction should return null when allowItemAdd is false");
    }

    @Test
    void createItemAddAction_withNoSupplier_shouldReturnNull() {
        toolBarOptions.setAllowItemAdd(true);
        toolBarOptions.setNewActionSupplier(null);
        assertNull(toolBarOptions.createItemAddAction(actionPanel, "group1"),
                "createItemAddAction should return null when no supplier is set");
    }

    @Test
    void createItemAddAction_withAllowTrueAndSupplier_shouldReturnAction() {
        toolBarOptions.setAllowItemAdd(true);
        toolBarOptions.setNewActionSupplier((panel, groupName) -> null);
        assertNotNull(toolBarOptions.createItemAddAction(actionPanel, "group1"),
                "createItemAddAction should return a non-null action when allowed and supplier is set");
    }

    // --- createRenameGroupAction ---

    @Test
    void createRenameGroupAction_withAllowFalse_shouldReturnNull() {
        toolBarOptions.setAllowGroupRename(false);
        assertNull(toolBarOptions.createRenameGroupAction(actionPanel, "group1"),
                "createRenameGroupAction should return null when allowGroupRename is false");
    }

    @Test
    void createRenameGroupAction_withAllowTrue_shouldReturnAction() {
        toolBarOptions.setAllowGroupRename(true);
        assertNotNull(toolBarOptions.createRenameGroupAction(actionPanel, "group1"),
                "createRenameGroupAction should return a non-null action when allowed");
    }

    // --- createEditGroupAction ---

    @Test
    void createEditGroupAction_withBothAllowFalse_shouldReturnNull() {
        toolBarOptions.setAllowItemReorder(false);
        toolBarOptions.setAllowItemRemoval(false);
        assertNull(toolBarOptions.createEditGroupAction(actionPanel, "group1"),
                "createEditGroupAction should return null when both allowItemReorder and allowItemRemoval are false");
    }

    @Test
    void createEditGroupAction_withReorderAllowed_shouldReturnAction() {
        toolBarOptions.setAllowItemReorder(true);
        toolBarOptions.setAllowItemRemoval(false);
        assertNotNull(toolBarOptions.createEditGroupAction(actionPanel, "group1"),
                "createEditGroupAction should return a non-null action when allowItemReorder is true");
    }

    @Test
    void createEditGroupAction_withRemovalAllowed_shouldReturnAction() {
        toolBarOptions.setAllowItemReorder(false);
        toolBarOptions.setAllowItemRemoval(true);
        assertNotNull(toolBarOptions.createEditGroupAction(actionPanel, "group1"),
                "createEditGroupAction should return a non-null action when allowItemRemoval is true");
    }

    // --- createRemoveGroupAction ---

    @Test
    void createRemoveGroupAction_withAllowFalse_shouldReturnNull() {
        toolBarOptions.setAllowGroupRemoval(false);
        assertNull(toolBarOptions.createRemoveGroupAction(actionPanel, "group1"),
                "createRemoveGroupAction should return null when allowGroupRemoval is false");
    }

    @Test
    void createRemoveGroupAction_withAllowTrue_shouldReturnAction() {
        toolBarOptions.setAllowGroupRemoval(true);
        assertNotNull(toolBarOptions.createRemoveGroupAction(actionPanel, "group1"),
                "createRemoveGroupAction should return a non-null action when allowed");
    }

    // --- createCustomActions ---

    @Test
    void createCustomActions_withNoSuppliers_shouldReturnEmptyList() {
        List<ToolBarAction> actions = toolBarOptions.createCustomActions(actionPanel, "group1");
        assertNotNull(actions, "createCustomActions should never return null");
        assertTrue(actions.isEmpty(), "createCustomActions should return empty list when no suppliers are added");
    }

    @Test
    void createCustomActions_withSupplierReturningNull_shouldReturnEmptyList() {
        toolBarOptions.addCustomActionSupplier((panel, groupName) -> null);
        List<ToolBarAction> actions = toolBarOptions.createCustomActions(actionPanel, "group1");
        assertNotNull(actions, "createCustomActions should never return null");
        assertTrue(actions.isEmpty(), "createCustomActions should filter out null results from suppliers");
    }

    @Test
    void createCustomActions_withMultipleSuppliers_shouldReturnAllNonNullActions() {
        ToolBarAction mockAction1 = createMockToolBarAction("action1");
        ToolBarAction mockAction2 = createMockToolBarAction("action2");

        toolBarOptions.addCustomActionSupplier((panel, groupName) -> mockAction1);
        toolBarOptions.addCustomActionSupplier((panel, groupName) -> null); // this one returns null
        toolBarOptions.addCustomActionSupplier((panel, groupName) -> mockAction2);

        List<ToolBarAction> actions = toolBarOptions.createCustomActions(actionPanel, "group1");
        assertEquals(2, actions.size(), "createCustomActions should return 2 non-null actions");
        assertSame(mockAction1, actions.get(0), "First action should be mockAction1");
        assertSame(mockAction2, actions.get(1), "Second action should be mockAction2");
    }

    @Test
    void addThenRemoveCustomActionSupplier_shouldAffectCreateCustomActions() {
        ToolBarAction mockAction = createMockToolBarAction("action1");
        ToolBarActionSupplier supplier = (panel, groupName) -> mockAction;

        toolBarOptions.addCustomActionSupplier(supplier);
        assertFalse(toolBarOptions.createCustomActions(actionPanel, "group1").isEmpty(),
                "createCustomActions should return action after supplier is added");

        toolBarOptions.removeCustomActionSupplier(supplier);
        assertTrue(toolBarOptions.createCustomActions(actionPanel, "group1").isEmpty(),
                "createCustomActions should return empty list after supplier is removed");
    }

    @Test
    void clearCustomActionSuppliers_shouldRemoveAllSuppliers() {
        toolBarOptions.addCustomActionSupplier((panel, groupName) -> createMockToolBarAction("action1"));
        toolBarOptions.addCustomActionSupplier((panel, groupName) -> createMockToolBarAction("action2"));

        toolBarOptions.clearCustomActionSuppliers();

        assertTrue(toolBarOptions.createCustomActions(actionPanel, "group1").isEmpty(),
                "createCustomActions should return empty list after clearing all suppliers");
    }

    // --- Options listener ---

    @Test
    void addListener_withNull_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> toolBarOptions.addListener(null),
                "addListener(null) should throw IllegalArgumentException");
    }

    @Test
    void removeListener_withNull_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> toolBarOptions.removeListener(null),
                "removeListener(null) should throw IllegalArgumentException");
    }

    @Test
    void setButtonPosition_shouldFireOptionsChanged() {
        final boolean[] fired = {false};
        toolBarOptions.addListener(() -> fired[0] = true);

        toolBarOptions.setButtonPosition(ToolBarOptions.ButtonPosition.AlignLeft);

        assertTrue(fired[0], "Options listener should be notified when button position changes");
    }

    @Test
    void setIconSize_shouldFireOptionsChanged() {
        final boolean[] fired = {false};
        toolBarOptions.addListener(() -> fired[0] = true);

        toolBarOptions.setIconSize(32);

        assertTrue(fired[0], "Options listener should be notified when icon size changes");
    }

    @Test
    void addExcludedGroup_shouldFireOptionsChanged() {
        final boolean[] fired = {false};
        toolBarOptions.addListener(() -> fired[0] = true);

        toolBarOptions.addExcludedGroup("SomeGroup");

        assertTrue(fired[0], "Options listener should be notified when an excluded group is added");
    }

    // --- Helpers ---

    private static ImageIcon createTestIcon() {
        return new ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB));
    }

    private ToolBarAction createMockToolBarAction(String groupName) {
        return new ToolBarAction(actionPanel, groupName, "tooltip", createTestIcon()) {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // No-op
            }
        };
    }
}
