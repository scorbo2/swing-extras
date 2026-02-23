package ca.corbett.extras.actionpanel;

import ca.corbett.extras.EnhancedAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.event.ActionEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for ExpandCollapseOptions.
 */
class ExpandCollapseOptionsTest {

    private ActionPanel actionPanel;
    private ExpandCollapseOptions options;

    @BeforeEach
    void setUp() {
        actionPanel = new ActionPanel();
        options = actionPanel.getExpandCollapseOptions();
    }

    // --- Default values ---

    @Test
    void defaults_animationShouldBeEnabled() {
        assertTrue(options.isAnimationEnabled(), "Animation should be enabled by default");
    }

    @Test
    void defaults_animationDurationShouldBeDefault() {
        assertEquals(ExpandCollapseOptions.DEFAULT_ANIMATION_DURATION_MS, options.getAnimationDurationMs(),
                "Default animation duration should be DEFAULT_ANIMATION_DURATION_MS");
    }

    @Test
    void defaults_headerDoubleClickShouldBeDisabled() {
        assertFalse(options.isAllowHeaderDoubleClick(), "Header double-click should be disabled by default");
    }

    @Test
    void defaults_expandableShouldBeEnabled() {
        assertTrue(options.isExpandable(), "Expand/collapse should be allowed by default");
    }

    // --- setAnimationDurationMs ---

    @Test
    void setAnimationDurationMs_withZero_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> options.setAnimationDurationMs(0),
                "setAnimationDurationMs(0) should throw IllegalArgumentException");
    }

    @Test
    void setAnimationDurationMs_withNegativeValue_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> options.setAnimationDurationMs(-1),
                "setAnimationDurationMs(-1) should throw IllegalArgumentException");
    }

    @Test
    void setAnimationDurationMs_withPositiveValue_shouldSetDuration() {
        options.setAnimationDurationMs(500);
        assertEquals(500, options.getAnimationDurationMs(), "Animation duration should be 500ms");
    }

    @Test
    void setAnimationDurationMs_shouldReturnSameInstance_forMethodChaining() {
        assertSame(options, options.setAnimationDurationMs(300),
                "setAnimationDurationMs should return the same instance for method chaining");
    }

    // --- setAnimationEnabled ---

    @Test
    void setAnimationEnabled_withFalse_shouldDisableAnimation() {
        options.setAnimationEnabled(false);
        assertFalse(options.isAnimationEnabled(), "Animation should be disabled");
    }

    @Test
    void setAnimationEnabled_withTrue_shouldEnableAnimation() {
        options.setAnimationEnabled(false);
        options.setAnimationEnabled(true);
        assertTrue(options.isAnimationEnabled(), "Animation should be re-enabled");
    }

    @Test
    void setAnimationEnabled_shouldReturnSameInstance_forMethodChaining() {
        assertSame(options, options.setAnimationEnabled(false),
                "setAnimationEnabled should return the same instance for method chaining");
    }

    // --- setAllowHeaderDoubleClick ---

    @Test
    void setAllowHeaderDoubleClick_withTrue_shouldEnable() {
        options.setAllowHeaderDoubleClick(true);
        assertTrue(options.isAllowHeaderDoubleClick(), "Header double-click should be enabled");
    }

    @Test
    void setAllowHeaderDoubleClick_withFalse_shouldDisable() {
        options.setAllowHeaderDoubleClick(true);
        options.setAllowHeaderDoubleClick(false);
        assertFalse(options.isAllowHeaderDoubleClick(), "Header double-click should be disabled");
    }

    @Test
    void setAllowHeaderDoubleClick_shouldReturnSameInstance_forMethodChaining() {
        assertSame(options, options.setAllowHeaderDoubleClick(true),
                "setAllowHeaderDoubleClick should return the same instance for method chaining");
    }

    // --- setExpandable ---

    @Test
    void setExpandable_withFalse_shouldDisallowExpandCollapse() {
        options.setExpandable(false);
        assertFalse(options.isExpandable(), "Expand/collapse should be disallowed");
    }

    @Test
    void setExpandable_withTrue_shouldAllowExpandCollapse() {
        options.setExpandable(false);
        options.setExpandable(true);
        assertTrue(options.isExpandable(), "Expand/collapse should be re-allowed");
    }

    @Test
    void setExpandable_withFalse_shouldForceExpandCollapsedGroups() {
        // GIVEN a group that is collapsed:
        actionPanel.add("group1", new TestAction("Action 1"));
        actionPanel.setExpanded("group1", false);
        assertFalse(actionPanel.isExpanded("group1"), "Group should be collapsed before test");

        // WHEN we disallow expand/collapse:
        options.setExpandable(false);

        // THEN the group should be forced to expand:
        assertTrue(actionPanel.isExpanded("group1"), "Collapsed group should be forced to expand when setExpandable(false)");
    }

    @Test
    void setExpandable_withFalse_shouldLeaveExpandedGroupsExpanded() {
        // GIVEN a group that is expanded (default):
        actionPanel.add("group1", new TestAction("Action 1"));
        assertTrue(actionPanel.isExpanded("group1"), "Group should be expanded before test");

        // WHEN we disallow expand/collapse:
        options.setExpandable(false);

        // THEN the group should still be expanded:
        assertTrue(actionPanel.isExpanded("group1"), "Expanded group should remain expanded when setExpandable(false)");
    }

    @Test
    void setExpandable_shouldReturnSameInstance_forMethodChaining() {
        assertSame(options, options.setExpandable(false),
                "setExpandable should return the same instance for method chaining");
    }

    // --- Options listener ---

    @Test
    void addListener_withNull_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> options.addListener(null),
                "addListener(null) should throw IllegalArgumentException");
    }

    @Test
    void removeListener_withNull_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> options.removeListener(null),
                "removeListener(null) should throw IllegalArgumentException");
    }

    @Test
    void setAnimationEnabled_shouldFireOptionsChanged() {
        // GIVEN a listener is registered:
        final boolean[] fired = {false};
        options.addListener(() -> fired[0] = true);

        // WHEN we change the animation enabled state:
        options.setAnimationEnabled(false);

        // THEN the listener should be notified:
        assertTrue(fired[0], "Options listener should be notified when animation enabled state changes");
    }

    @Test
    void setAnimationDurationMs_shouldFireOptionsChanged() {
        // GIVEN a listener is registered:
        final boolean[] fired = {false};
        options.addListener(() -> fired[0] = true);

        // WHEN we change the animation duration:
        options.setAnimationDurationMs(400);

        // THEN the listener should be notified:
        assertTrue(fired[0], "Options listener should be notified when animation duration changes");
    }

    @Test
    void setAllowHeaderDoubleClick_shouldFireOptionsChanged() {
        // GIVEN a listener is registered:
        final boolean[] fired = {false};
        options.addListener(() -> fired[0] = true);

        // WHEN we change the header double-click setting:
        options.setAllowHeaderDoubleClick(true);

        // THEN the listener should be notified:
        assertTrue(fired[0], "Options listener should be notified when header double-click setting changes");
    }

    /**
     * A very simple implementation of EnhancedAction that does nothing.
     */
    private static class TestAction extends EnhancedAction {

        public TestAction(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // No-op
        }
    }
}
