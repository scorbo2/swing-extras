package ca.corbett.extras.actionpanel;

import ca.corbett.extras.EnhancedAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit tests for ActionPanel, including animation functionality.
 */
class ActionPanelTest {

    private ActionPanel actionPanel;

    @BeforeEach
    void setUp() {
        actionPanel = new ActionPanel();
    }

    /**
     * Helper method to create a simple test action.
     */
    private EnhancedAction createTestAction(String name) {
        return new EnhancedAction(name) {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // Test action - does nothing
            }
        };
    }

    @Test
    void testDefaultAnimationEnabled() {
        assertTrue(actionPanel.isAnimationEnabled(), "Animation should be enabled by default");
    }

    @Test
    void testDefaultAnimationDuration() {
        assertEquals(ActionPanel.DEFAULT_ANIMATION_DURATION_MS, actionPanel.getAnimationDurationMs(),
                     "Default animation duration should be 200ms");
    }

    @Test
    void testSetAnimationEnabled() {
        actionPanel.setAnimationEnabled(false);
        assertFalse(actionPanel.isAnimationEnabled(), "Animation should be disabled");

        actionPanel.setAnimationEnabled(true);
        assertTrue(actionPanel.isAnimationEnabled(), "Animation should be enabled");
    }

    @Test
    void testSetAnimationDuration() {
        actionPanel.setAnimationDurationMs(500);
        assertEquals(500, actionPanel.getAnimationDurationMs(), "Animation duration should be 500ms");
    }

    @Test
    void testSetAnimationDurationThrowsExceptionForInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> actionPanel.setAnimationDurationMs(0),
                     "Should throw exception for duration of 0");

        assertThrows(IllegalArgumentException.class, () -> actionPanel.setAnimationDurationMs(-100),
                     "Should throw exception for negative duration");
    }

    @Test
    void testAddActionAndGroup() {
        EnhancedAction action = createTestAction("Test Action");
        actionPanel.add("Test Group", action);

        assertTrue(actionPanel.isExpanded("Test Group"), "Group should be expanded by default");
    }

    @Test
    void testSetExpandedState() {
        EnhancedAction action = createTestAction("Test Action");
        actionPanel.add("Test Group", action);

        // Test collapsing
        actionPanel.setExpanded("Test Group", false);
        assertFalse(actionPanel.isExpanded("Test Group"), "Group should be collapsed");

        // Test expanding
        actionPanel.setExpanded("Test Group", true);
        assertTrue(actionPanel.isExpanded("Test Group"), "Group should be expanded");
    }

    @Test
    void testMethodChaining() {
        ActionPanel result = actionPanel
                .setAnimationEnabled(false)
                .setAnimationDurationMs(300)
                .setUseButtons()
                .setInternalPadding(5)
                .setExternalPadding(10);

        assertSame(actionPanel, result, "Methods should return the same ActionPanel instance for chaining");
        assertFalse(actionPanel.isAnimationEnabled(), "Animation should be disabled");
        assertEquals(300, actionPanel.getAnimationDurationMs(), "Duration should be 300ms");
    }

    @Test
    void testInstantExpandCollapse() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        SwingUtilities.invokeLater(() -> {
            try {
                JFrame frame = new JFrame("Test");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                ActionPanel panel = new ActionPanel();
                panel.setAnimationEnabled(false); // Disable animation
                panel.add("Test Group", createTestAction("Action 1"));

                frame.add(panel);
                frame.pack();
                frame.setVisible(true);

                // Test instant collapse
                panel.setExpanded("Test Group", false);
                assertFalse(panel.isExpanded("Test Group"), "Group should be collapsed");

                // Test instant expand
                panel.setExpanded("Test Group", true);
                assertTrue(panel.isExpanded("Test Group"), "Group should be expanded");

                frame.dispose();
                latch.countDown();
            }
            catch (Exception e) {
                e.printStackTrace();
                fail("Instant expand/collapse test failed: " + e.getMessage());
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within 5 seconds");
    }

    @Test
    public void renameGroup_withNonExistingGroup_shouldFail() {
        // GIVEN an action panel with no content
        // WHEN we try to rename a group:
        boolean result = actionPanel.renameGroup("Well hello there, this group does not exist", "group1");

        // THEN the method should return false to indicate failure:
        assertFalse(result, "renameGroup should return false when the original group does not exist");
    }

    @Test
    public void renameGroup_withExistingGroup_shouldSucceed() {
        // GIVEN an action panel with a group named "group1":
        actionPanel.add("group1", createTestAction("Action 1"));

        try {
            // WHEN we try to rename that group:
            boolean result = actionPanel.renameGroup("group1", "new name");

            // THEN the method should return true to indicate success, and the group should be renamed:
            assertTrue(result, "renameGroup should return true when the original group exists");
            assertNotNull(actionPanel.getGroup("new name"), "The group should be renamed to 'new name'");
            assertNull(actionPanel.getGroup("group1"), "The old group name should no longer exist");
        }
        finally {
            actionPanel.clear(true); // clean up after the test so it doesn't affect other tests
        }
    }

    @Test
    public void renameGroup_newNameDiffersOnlyInCase_shouldRename() {
        // GIVEN a group named "group1":
        actionPanel.add("group1", createTestAction("Action 1"));

        try {
            // WHEN we try to rename that group to "GROUP1" (same name, different case):
            boolean result = actionPanel.renameGroup("group1", "GROUP1");

            // THEN the method should return true to indicate success, and the group should be renamed:
            assertTrue(result, "renameGroup should return true when the new name differs only in case");
            assertNotNull(actionPanel.getGroup("GROUP1"), "The group should be renamed to 'GROUP1'");

            // AND the group should still be accessible by the old name, because group names are case-insensitive:
            assertNotNull(actionPanel.getGroup("group1"), "The old group name should still exist.");

            // but if we examine the underlying ActionGroup directly, we should see the case-sensitive change:
            ActionGroup actionGroup = actionPanel.getGroup("group1");
            assertNotNull(actionGroup, "The group should still be accessible by the old name");
            assertEquals("GROUP1", actionGroup.getName(),
                         "The ActionGroup's name should be updated to 'GROUP1' with the new case");
        }
        finally {
            actionPanel.clear(true); // clean up after the test so it doesn't affect other tests
        }
    }

    @Test
    public void renameGroup_destinationNameInUse_shouldFail() {
        // GIVEN an action panel with groups named "group1" and "group2":
        actionPanel.add("group1", createTestAction("Action 1"));
        actionPanel.add("group2", createTestAction("Action 2"));

        try {
            // WHEN we try to rename one to the name of the other:
            boolean result = actionPanel.renameGroup("group1", "group2");

            // THEN the method should return false to indicate failure, and neither group should be renamed:
            assertFalse(result, "renameGroup should return false when the new name is already in use");
            assertNotNull(actionPanel.getGroup("group1"), "The original group 'group1' should still exist");
            assertNotNull(actionPanel.getGroup("group2"), "The original group 'group2' should still exist");
        }
        finally {
            actionPanel.clear(true); // clean up after the test so it doesn't affect other tests
        }
    }
}
