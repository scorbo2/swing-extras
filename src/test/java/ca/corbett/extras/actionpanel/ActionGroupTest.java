package ca.corbett.extras.actionpanel;

import ca.corbett.extras.EnhancedAction;
import org.junit.jupiter.api.Test;

import java.awt.event.ActionEvent;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActionGroupTest {

    private final Comparator<EnhancedAction> nameComparator = Comparator.comparing(EnhancedAction::getName);
    private final Comparator<EnhancedAction> descComparator = Comparator.comparing(EnhancedAction::getDescription);

    @Test
    public void emptyGroup_shouldReportEmpty() {
        // GIVEN an empty ActionGroup:
        final String groupName = "Test Group";
        ActionGroup actual = new ActionGroup(groupName);

        // WHEN we query it:
        // THEN it should report itself as empty with no exceptions:
        assertNull(actual.getComparator());
        assertNull(actual.getAnimatedWrapper());
        assertNull(actual.getIcon());
        assertEquals(groupName, actual.getName());
        assertEquals(0, actual.size());
        assertNotNull(actual.getActions());
        assertTrue(actual.getActions().isEmpty());
        actual.clear(); // should not throw
    }

    @Test
    public void add_withDuplicateAction_shouldAllowDuplicates() {
        // GIVEN a single action and an empty group:
        EnhancedAction action = new TestAction("Action 1");
        ActionGroup actual = new ActionGroup("Test Group");

        // WHEN we add the same action instance multiple times:
        actual.add(action);
        actual.add(action);
        actual.add(action);

        // THEN the duplicates should have been allowed:
        assertEquals(3, actual.size());
    }

    @Test
    public void remove_withDuplicateAction_shouldRemoveAllInstances() {
        // GIVEN an action added multiple times to a group:
        EnhancedAction action = new TestAction("Action 1");
        ActionGroup actual = new ActionGroup("Test Group");
        actual.addAll(List.of(action, action, action));

        // WHEN we remove that action:
        actual.remove(action);

        // THEN we should see that ALL instances were removed:
        assertEquals(0, actual.size());
    }

    @Test
    public void setComparator_withNull_shouldMaintainInsertOrder() {
        // GIVEN a list of actions added in a specific order:
        EnhancedAction firstAction = new TestAction("zzz");
        EnhancedAction secondAction = new TestAction("aaa");
        EnhancedAction thirdAction = new TestAction("mmm");
        ActionGroup actual = new ActionGroup("Test Group");
        actual.addAll(List.of(firstAction, secondAction, thirdAction));

        // WHEN we set a null comparator (actually this is the default):
        actual.setComparator(null);

        // THEN we should see the actions returned to us in the order they were added:
        List<EnhancedAction> actions = actual.getActions();
        assertEquals(3, actions.size());
        assertEquals(firstAction, actions.get(0));
        assertEquals(secondAction, actions.get(1));
        assertEquals(thirdAction, actions.get(2));
    }

    @Test
    public void setComparator_withNameComparator_shouldSortByName() {
        // GIVEN a list of actions added in a specific order:
        EnhancedAction firstAction = new TestAction("zzz");
        EnhancedAction secondAction = new TestAction("aaa");
        EnhancedAction thirdAction = new TestAction("mmm");
        ActionGroup actual = new ActionGroup("Test Group");
        actual.addAll(List.of(firstAction, secondAction, thirdAction));

        // WHEN we set a name comparator:
        actual.setComparator(nameComparator);

        // THEN we should see the actions returned to us in name-sorted order:
        List<EnhancedAction> actions = actual.getActions();
        assertEquals(3, actions.size());
        assertEquals(secondAction.getName(), actions.get(0).getName());
        assertEquals(thirdAction.getName(), actions.get(1).getName());
        assertEquals(firstAction.getName(), actions.get(2).getName());
    }

    @Test
    public void setComparator_withDescriptionComparator_shouldSortByDescription() {
        // GIVEN a list of actions with descriptions added in a specific order:
        EnhancedAction firstAction = new TestAction("Action 1").setDescription("zzz");
        EnhancedAction secondAction = new TestAction("Action 2").setDescription("aaa");
        EnhancedAction thirdAction = new TestAction("Action 3").setDescription("mmm");
        ActionGroup actual = new ActionGroup("Test Group");
        actual.addAll(List.of(firstAction, secondAction, thirdAction));

        // WHEN we set a description comparator:
        actual.setComparator(descComparator);

        // THEN we should see the actions returned to us in description-sorted order:
        List<EnhancedAction> actions = actual.getActions();
        assertEquals(3, actions.size());
        assertEquals(secondAction.getName(), actions.get(0).getName());
        assertEquals(thirdAction.getName(), actions.get(1).getName());
        assertEquals(firstAction.getName(), actions.get(2).getName());
    }

    @Test
    public void setComparator_withComparatorThenNull_shouldRestoreInsertionOrder() {
        // GIVEN a group that is sorted by name:
        EnhancedAction firstAction = new TestAction("zzz");
        EnhancedAction secondAction = new TestAction("aaa");
        EnhancedAction thirdAction = new TestAction("mmm");
        ActionGroup actual = new ActionGroup("Test Group");
        actual.addAll(List.of(firstAction, secondAction, thirdAction));
        actual.setComparator(nameComparator); // auto-sorts here

        // WHEN we set the comparator to null:
        actual.setComparator(null);

        // THEN we should see the actions returned to us in the order they were added:
        List<EnhancedAction> actions = actual.getActions();
        assertEquals(3, actions.size());
        assertEquals(firstAction, actions.get(0));
        assertEquals(secondAction, actions.get(1));
        assertEquals(thirdAction, actions.get(2));
    }

    @Test
    public void hasAction_withMatch_shouldReturnTrue() {
        // GIVEN a group with some actions:
        EnhancedAction action1 = new TestAction("Action 1");
        EnhancedAction action2 = new TestAction("Action 2");
        ActionGroup actual = new ActionGroup("Test Group");
        actual.addAll(List.of(action1, action2));

        // WHEN we check for an existing action:
        boolean result = actual.hasAction(action1);

        // THEN it should return true:
        assertTrue(result);
    }

    @Test
    public void hasAction_withNoMatch_shouldReturnFalse() {
        // GIVEN a group with some actions:
        EnhancedAction action1 = new TestAction("Action 1");
        EnhancedAction action2 = new TestAction("Action 2");
        ActionGroup actual = new ActionGroup("Test Group");
        actual.addAll(List.of(action1, action2));

        // WHEN we check for a non-existing action:
        EnhancedAction action3 = new TestAction("Action 3");
        boolean result = actual.hasAction(action3);

        // THEN it should return false:
        assertFalse(result);
    }

    @Test
    public void hasActionByName_withExactMatchCaseSensitive_shouldReturnTrue() {
        // GIVEN a group with some actions:
        EnhancedAction action1 = new TestAction("Action 1");
        EnhancedAction action2 = new TestAction("Action 2");
        ActionGroup actual = new ActionGroup("Test Group");
        actual.addAll(List.of(action1, action2));

        // WHEN we check for an existing action by name, with case sensitivity:
        boolean result = actual.hasAction("Action 1", true);

        // THEN it should return true:
        assertTrue(result);
    }

    @Test
    public void hasActionByName_withNoMatchCaseSensitive_shouldReturnFalse() {
        // GIVEN a group with some actions:
        EnhancedAction action1 = new TestAction("Action 1");
        EnhancedAction action2 = new TestAction("Action 2");
        ActionGroup actual = new ActionGroup("Test Group");
        actual.addAll(List.of(action1, action2));

        // WHEN we check for a non-existing action by name, with case sensitivity:
        boolean result = actual.hasAction("action 1", true);

        // THEN it should return false:
        assertFalse(result);
    }

    @Test
    public void hasActionByName_withMatchCaseInsensitive_shouldReturnTrue() {
        // GIVEN a group with some actions:
        EnhancedAction action1 = new TestAction("Action 1");
        EnhancedAction action2 = new TestAction("Action 2");
        ActionGroup actual = new ActionGroup("Test Group");
        actual.addAll(List.of(action1, action2));

        // WHEN we check for an existing action by name, without case sensitivity:
        boolean result = actual.hasAction("action 1", false);

        // THEN it should return true:
        assertTrue(result);
    }

    @Test
    public void hasActionByName_withNoMatchCaseInsensitive_shouldReturnFalse() {
        // GIVEN a group with some actions:
        EnhancedAction action1 = new TestAction("Action 1");
        EnhancedAction action2 = new TestAction("Action 2");
        ActionGroup actual = new ActionGroup("Test Group");
        actual.addAll(List.of(action1, action2));

        // WHEN we check for a non-existing action by name, without case sensitivity:
        boolean result = actual.hasAction("Nonexistent Action", false);

        // THEN it should return false:
        assertFalse(result);
    }

    @Test
    public void remove_withMatch_shouldRemove() {
        // GIVEN a group with some actions:
        EnhancedAction action1 = new TestAction("Action 1");
        EnhancedAction action2 = new TestAction("Action 2");
        ActionGroup actual = new ActionGroup("Test Group");
        actual.addAll(List.of(action1, action2));

        // WHEN we remove an existing action:
        actual.remove(action1);

        // THEN the action should be removed:
        assertEquals(1, actual.size());
        assertFalse(actual.hasAction(action1));
    }

    @Test
    public void remove_withNoMatch_shouldNotRemove() {
        // GIVEN a group with some actions:
        EnhancedAction action1 = new TestAction("Action 1");
        EnhancedAction action2 = new TestAction("Action 2");
        ActionGroup actual = new ActionGroup("Test Group");
        actual.addAll(List.of(action1, action2));

        // WHEN we try to remove a non-existing action:
        EnhancedAction action3 = new TestAction("Action 3");
        actual.remove(action3);

        // THEN the group should remain unchanged:
        assertEquals(2, actual.size());
        assertTrue(actual.hasAction(action1));
        assertTrue(actual.hasAction(action2));
    }

    @Test
    public void removeByName_withMatch_shouldRemove() {
        // GIVEN a group with some actions:
        EnhancedAction action1 = new TestAction("Action 1");
        EnhancedAction action2 = new TestAction("Action 2");
        ActionGroup actual = new ActionGroup("Test Group");
        actual.addAll(List.of(action1, action2));

        // WHEN we remove an existing action by name:
        actual.remove("Action 1", true);

        // THEN the action should be removed:
        assertEquals(1, actual.size());
        assertFalse(actual.hasAction(action1));
    }

    @Test
    public void removeByName_withNoMatch_shouldNotRemove() {
        // GIVEN a group with some actions:
        EnhancedAction action1 = new TestAction("Action 1");
        EnhancedAction action2 = new TestAction("Action 2");
        ActionGroup actual = new ActionGroup("Test Group");
        actual.addAll(List.of(action1, action2));

        // WHEN we try to remove a non-existing action by name:
        actual.remove("Nonexistent Action", true);

        // THEN the group should remain unchanged:
        assertEquals(2, actual.size());
        assertTrue(actual.hasAction(action1));
        assertTrue(actual.hasAction(action2));
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
