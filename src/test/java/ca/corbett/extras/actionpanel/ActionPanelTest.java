package ca.corbett.extras.actionpanel;

import ca.corbett.extras.EnhancedAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
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
                .setHeaderInternalPadding(5)
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

    @Test
    void testAddCardActionWithoutContainer_shouldThrow() {
        CardAction cardAction = new CardAction("Test Card Action", "card1");
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> actionPanel.add("Test Group", cardAction),
            "Adding CardAction without setting a card container should throw IllegalStateException");
        
        assertTrue(exception.getMessage().contains("Card Container"),
            "Exception message should mention Card Container");
    }

    @Test
    void testSetCardContainerWithNonCardLayout_shouldThrow() {
        Container containerWithBorderLayout = new JPanel(new BorderLayout());
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> actionPanel.setCardContainer(containerWithBorderLayout),
            "Setting container with non-CardLayout should throw IllegalArgumentException");
        
        assertTrue(exception.getMessage().contains("CardLayout"),
            "Exception message should mention CardLayout");
    }

    @Test
    void testConvenienceAddMethodAssociatesContainer() {
        // GIVEN a card container with CardLayout
        Container cardContainer = new JPanel(new CardLayout());
        cardContainer.add(new JPanel(), "card1");
        cardContainer.add(new JPanel(), "card2");
        
        // WHEN we set the card container and add a CardAction using the convenience method
        actionPanel.setCardContainer(cardContainer);
        actionPanel.add("Test Group", "Show Card 1", "card1");
        
        // THEN the action should be added successfully
        ActionGroup group = actionPanel.getGroup("Test Group");
        assertNotNull(group, "Group should be created");
        assertEquals(1, group.getActions().size(), "Group should have one action");
        
        EnhancedAction action = group.getActions().get(0);
        assertTrue(action instanceof CardAction, "Action should be a CardAction");
        assertEquals("Show Card 1", action.getValue(EnhancedAction.NAME), "Action name should match");
    }

    @Test
    void testConvenienceAddMethodWithoutContainer_shouldThrow() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> actionPanel.add("Test Group", "Show Card 1", "card1"),
            "Using convenience add method without setting a card container should throw IllegalStateException");
        
        assertTrue(exception.getMessage().contains("Card Container"),
            "Exception message should mention Card Container");
    }

    @Test
    void testCardActionTriggersCardLayoutShow() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        SwingUtilities.invokeLater(() -> {
            try {
                // Create a card container with CardLayout
                JPanel cardContainer = new JPanel(new CardLayout());
                JPanel card1 = new JPanel();
                JPanel card2 = new JPanel();
                cardContainer.add(card1, "card1");
                cardContainer.add(card2, "card2");
                
                // Create ActionPanel with CardAction
                ActionPanel panel = new ActionPanel();
                panel.setCardContainer(cardContainer);
                CardAction cardAction = new CardAction("Show Card 2", "card2");
                panel.add("Test Group", cardAction);
                
                // Initially card1 should be visible (first card added)
                assertTrue(card1.isVisible(), "Card1 should be visible initially");
                
                // Trigger the CardAction
                cardAction.actionPerformed(null);
                
                // Now card2 should be visible
                assertFalse(card1.isVisible(), "Card1 should not be visible after switching");
                assertTrue(card2.isVisible(), "Card2 should be visible after triggering CardAction");
                
                latch.countDown();
            }
            catch (Exception e) {
                fail("CardAction trigger test failed: " + e.getMessage());
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within 5 seconds");
    }

    @Test
    void testSetCardContainerUpdatesExistingCardActions() {
        // GIVEN a card container and ActionPanel with CardActions
        Container cardContainer1 = new JPanel(new CardLayout());
        cardContainer1.add(new JPanel(), "card1");
        
        actionPanel.setCardContainer(cardContainer1);
        actionPanel.add("Test Group", "Show Card 1", "card1");
        
        // WHEN we set a new card container
        Container cardContainer2 = new JPanel(new CardLayout());
        cardContainer2.add(new JPanel(), "card1");
        actionPanel.setCardContainer(cardContainer2);
        
        // THEN the existing CardAction should be updated with the new container
        ActionGroup group = actionPanel.getGroup("Test Group");
        CardAction cardAction = (CardAction) group.getActions().get(0);
        
        // Verify the action works with the new container
        assertNotNull(cardAction, "CardAction should exist");
        assertEquals("Show Card 1", cardAction.getValue(EnhancedAction.NAME), "Action name should be preserved");
    }

    @Test
    void testSetCardContainerToNullWithCardActions_shouldThrow() {
        // GIVEN an ActionPanel with a card container and CardActions
        Container cardContainer = new JPanel(new CardLayout());
        cardContainer.add(new JPanel(), "card1");
        
        actionPanel.setCardContainer(cardContainer);
        actionPanel.add("Test Group", "Show Card 1", "card1");
        
        // WHEN we try to set the card container to null while CardActions exist
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> actionPanel.setCardContainer(null),
            "Setting card container to null with existing CardActions should throw IllegalStateException");
        
        assertTrue(exception.getMessage().contains("Card Actions are present"),
            "Exception message should mention Card Actions");
    }

    @Test
    void testGetCardContainer() {
        // GIVEN an ActionPanel with a card container
        Container cardContainer = new JPanel(new CardLayout());
        
        // WHEN we set the card container
        actionPanel.setCardContainer(cardContainer);
        
        // THEN getCardContainer should return the same container
        assertSame(cardContainer, actionPanel.getCardContainer(),
            "getCardContainer should return the same container that was set");
    }

    @Test
    void testGetCardContainerWhenNotSet() {
        // WHEN no card container is set
        // THEN getCardContainer should return null
        assertNull(actionPanel.getCardContainer(),
            "getCardContainer should return null when no container is set");
    }
}
