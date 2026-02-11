package ca.corbett.extras.io;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class KeyStrokeManagerTest {

    private KeyStrokeManager keyManager;
    private final java.awt.Frame reference = new java.awt.Frame();


    @BeforeEach
    public void setup() {
        keyManager = new KeyStrokeManager(null);
    }

    @Test
    public void addWindow_nullReference_error() {
        try {
            keyManager.addWindow(null);
            fail("Expected IllegalArgumentException for null window");
        } catch (IllegalArgumentException ignored) {
            assertTrue(!keyManager.isEnabled());
        }
    }

    @Test
    public void addWindow_validInput_success() {
        keyManager.addWindow(reference);
        assertTrue(keyManager.isEnabled());
    }

    @Test
    public void removeWindow_nullReference_success() {
         keyManager.addWindow(reference);
        try {
            keyManager.addWindow(null);
            fail("Expected IllegalArgumentException for null window");
        } catch (IllegalArgumentException ignored) {
            assertTrue(keyManager.isEnabled());
        }
    }

    @Test
    public void removeWindow_validInput_success() {
        keyManager.addWindow(reference);
        keyManager.removeWindow(reference);
        assertTrue(!keyManager.isEnabled());
    }


    @Test
    public void parseKeyStroke_validInput_success() {
        // GIVEN some valid keystroke strings:
        String[] keyStrokes = {
                "ctrl+A",
                "shift+alt+F1",
                "meta+ENTER",
                "ctrl+shift+alt+X"
        };

        // WHEN we parse them:
        for (String keyStroke : keyStrokes) {
            KeyStroke result = keyManager.parseKeyStroke(keyStroke);

            // THEN we should get a non-null KeyStroke:
            assertNotNull(result, "Failed to parse valid key stroke: " + keyStroke);
        }
    }

    @Test
    public void parseKeyStroke_invalidInput_null() {
        // GIVEN some invalid keystroke strings:
        String[] keyStrokes = {
                "",
                "   ",
                "ctrl++A",
                "unknown+KEY",
                "Shift+somethingThatIsVeryObviouslyNotAKeyOnAnyStandardKeyboardButReallyShouldBeThatWouldBeCool",
                null,
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                "ctrl+a+b+c+d+e+f+g+h+i+j+k+l+m+n+o+p+q+r+s+t+u+v+w+x+y+z"
        };

        // WHEN we parse them:
        for (String keyStroke : keyStrokes) {
            KeyStroke result = KeyStrokeManager.parseKeyStroke(keyStroke);

            // THEN we should get a null KeyStroke:
            assertNull(result, "Parsed invalid key stroke: " + keyStroke);
        }
    }

    @Test
    public void parseKeyStroke_withoutModifiers_shouldReturnKey() {
        // GIVEN a simple keystroke string without modifiers:
        String[] keyStrokes = {
                "A",
                "B",
                "C",
                "D"
        };

        // WHEN we parse it:
        int expectedKeyCode = KeyEvent.VK_A;
        for (String keyStroke : keyStrokes) {
            KeyStroke result = KeyStrokeManager.parseKeyStroke(keyStroke);

            // THEN the result should match the input:
            assertNotNull(result);
            assertEquals(expectedKeyCode++, result.getKeyCode());
        }
    }

    @Test
    public void parseKeyStroke_withOneModifier_shouldReturnKeyWithModifier() {
        // GIVEN keystroke strings with one modifier:
        String[] keyStrokes = {
                "ctrl+A",
                "shift+B",
                "alt+C",
                "meta+D"
        };

        // WHEN we parse them:
        int[] expectedModifiers = {
                KeyEvent.CTRL_DOWN_MASK,
                KeyEvent.SHIFT_DOWN_MASK,
                KeyEvent.ALT_DOWN_MASK,
                KeyEvent.META_DOWN_MASK
        };
        int expectedKeyCode = KeyEvent.VK_A;
        for (int i = 0; i < keyStrokes.length; i++) {
            KeyStroke result = KeyStrokeManager.parseKeyStroke(keyStrokes[i]);

            // THEN the result should have the correct key code and modifier:
            assertNotNull(result);
            assertEquals(expectedKeyCode++, result.getKeyCode());
            int mods = result.getModifiers();
            assertEquals(expectedModifiers[i], mods & expectedModifiers[i]);
        }
    }

    @Test
    public void parseKeyStroke_withTwoModifiers_shouldReturnKeyWithModifiers() {
        // GIVEN keystroke strings with two modifiers:
        String[] keyStrokes = {
                "ctrl+shift+A",
                "alt+meta+B"
        };

        // WHEN we parse them:
        int[][] expectedModifiers = {
                {KeyEvent.CTRL_DOWN_MASK, KeyEvent.SHIFT_DOWN_MASK},
                {KeyEvent.ALT_DOWN_MASK, KeyEvent.META_DOWN_MASK}
        };
        int expectedKeyCode = KeyEvent.VK_A;
        for (int i = 0; i < keyStrokes.length; i++) {
            KeyStroke result = KeyStrokeManager.parseKeyStroke(keyStrokes[i]);

            // THEN the result should have the correct key code and modifiers:
            assertNotNull(result);
            assertEquals(expectedKeyCode++, result.getKeyCode());
            int mods = result.getModifiers();
            for (int mod : expectedModifiers[i]) {
                assertEquals(mod, mods & mod);
            }
        }
    }

    @Test
    public void register_unregister_reassignHandler_flow() throws Exception {
        // GIVEN a simply dummy action:
        AtomicInteger invoked = new AtomicInteger(0);
        Action actionUnderTest = new AbstractAction("MyAction") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                invoked.incrementAndGet();
            }
        };

        // WHEN we register that action with a keystroke:
        final String actionName = "MyAction";
        keyManager.registerHandler("ctrl+X", actionUnderTest);

        // THEN it should be registered correctly:
        List<Action> actions = keyManager.getActionsForKeyStroke("ctrl+x");
        assertEquals(1, actions.size(), "Should have one handler after register");
        assertEquals(actionUnderTest, actions.get(0), "Registered handler should be present");

        // AND the action should be associated with the keystroke:
        KeyStroke keyStroke = KeyStrokeManager.parseKeyStroke("ctrl+X");
        assertEquals(keyStroke, actionUnderTest.getValue(Action.ACCELERATOR_KEY),
                     "Action ACCELERATOR_KEY should be set");

        // WHEN we reassign the handler to a new keystroke:
        keyManager.reassignHandler(actionUnderTest, "alt+Y");

        // THEN the old assignment should be removed:
        assertTrue(keyManager.getActionsForKeyStroke(keyStroke).isEmpty(),
                   "Old shortcut should no longer have handlers");

        // AND the new assignment should be present:
        actions = keyManager.getActionsForKeyStroke("alt+y");
        assertEquals(1, actions.size(), "Should have one handler for new shortcut");
        assertEquals(actionUnderTest, actions.get(0), "Reassigned handler should be present");

        // WHEN we unregister the handler:
        keyManager.unregisterHandler(actionUnderTest);

        // THEN it should be removed from the keystroke:
        assertTrue(keyManager.getActionsForKeyStroke("alt+y").isEmpty(),
                   "Handlers list should be empty after unregister");
        assertTrue(keyManager.isEmpty(), "No shortcut should be associated after unregister");
    }


    @Test
    public void multipleActionsSameKeystroke_areBothRegistered_and_haveAccelerators() throws Exception {
        AtomicInteger a1 = new AtomicInteger(0);
        AtomicInteger a2 = new AtomicInteger(0);

        Action action1 = new AbstractAction("A1") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                a1.incrementAndGet();
            }
        };
        Action action2 = new AbstractAction("A2") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                a2.incrementAndGet();
            }
        };

        // WHEN we register two different actions to the same keystroke:
        keyManager.registerHandler("F5", action1);
        keyManager.registerHandler("F5", action2);

        // THEN they should both be registered:
        List<Action> actions = keyManager.getActionsForKeyStroke("f5");
        assertEquals(2, actions.size(), "Both actions should be registered for the same keystroke");

        // AND Each of our actions should be present exactly once:
        boolean foundAction1 = false;
        boolean foundAction2 = false;
        for (Action action : actions) {
            if (action == action1) {
                foundAction1 = true;
            }
            else if (action == action2) {
                foundAction2 = true;
            }
        }
        assertTrue(foundAction1, "Action1 should be registered");
        assertTrue(foundAction2, "Action2 should be registered");

        // Both actions should have ACCELERATOR_KEY set to the same KeyStroke
        KeyStroke expected = KeyStrokeManager.parseKeyStroke("F5");
        assertEquals(expected, action1.getValue(Action.ACCELERATOR_KEY));
        assertEquals(expected, action2.getValue(Action.ACCELERATOR_KEY));
    }

    @Test
    public void clear_withActionsRegistered_shouldClear() throws Exception {
        Action action = new AbstractAction("SomeAction") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // no-op
            }
        };

        // WHEN we register an action:
        keyManager.registerHandler("ctrl+Z", action);

        // THEN it should be registered:
        assertEquals(1, keyManager.getActionsForKeyStroke("ctrl+z").size(), "Should have one handler before clear");

        // WHEN we clear the key manager:
        keyManager.clear();

        // THEN it should be empty:
        assertTrue(keyManager.getActionsForKeyStroke("ctrl+z").isEmpty(), "Should have no handlers after clear");
        assertTrue(keyManager.isEmpty(), "No shortcut should be associated after clear");
    }

    @Test
    public void registerHandler_withNullAction_shouldThrow() {
        try {
            keyManager.registerHandler("ctrl+Q", null);
            fail("Expected IllegalArgumentException for null action, but didn't get one!");
        }
        catch (IllegalArgumentException ignored) {
            // Expected exception
            return;
        }
    }

    @Test
    public void registerHandler_withInvalidKeyStrokeString_shouldThrow() {
        Action action = new AbstractAction("InvalidKeyStrokeAction") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // no-op
            }
        };

        try {
            keyManager.registerHandler("thisIsNotAValidKeyStroke!", action);
            fail("Expected IllegalArgumentException for invalid keystroke, but didn't get one!");
        }
        catch (IllegalArgumentException ignored) {
            // Expected exception
        }
    }

    @Test
    public void registerHandler_withNullKeyStroke_shouldThrow() {
        Action action = new AbstractAction("NullKeyStrokeAction") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // no-op
            }
        };

        try {
            keyManager.registerHandler((KeyStroke)null, action);
            fail("Expected IllegalArgumentException for null keystroke, but didn't get one!");
        }
        catch (IllegalArgumentException ignored) {
            // Expected exception
            return;
        }
    }

    @Test
    public void isAvailable_withInvalidKeyStrokeString_shouldThrow() {
        // GIVEN some invalid keystroke strings:
        String[] invalidKeyStrokes = {
                "",
                "notAKeyStroke",
                "ctrl++A",
                null
        };

        for (String ks : invalidKeyStrokes) {
            // WHEN we ask if they are available:
            try {
                boolean available = keyManager.isAvailable(ks);
                fail("Expected IllegalArgumentException for invalid keystroke: " + ks +
                             ", but got availability: " + available);
            }
            catch (IllegalArgumentException ignored) {
                // Expected exception
            }
        }
    }

    @Test
    public void hasHandlers_withInvalidKeyStrokeString_shouldThrow() {
        // GIVEN some invalid keystroke strings:
        String[] invalidKeyStrokes = {
                "",
                "notAKeyStroke",
                "shift++B",
                null
        };

        for (String ks : invalidKeyStrokes) {
            // WHEN we ask if they have handlers:
            try {
                boolean hasHandlers = keyManager.hasHandlers(ks);
                fail("Expected IllegalArgumentException for invalid keystroke: " + ks +
                             ", but got hasHandlers: " + hasHandlers);
            }
            catch (IllegalArgumentException ignored) {
                // Expected exception
            }
        }
    }

    @Test
    public void getActionsForKeyStroke_withInvalidKeyStrokeString_shouldThrow() {
        // GIVEN some invalid keystroke strings:
        String[] invalidKeyStrokes = {
                "",
                "notAKeyStroke",
                "alt++C",
                null
        };

        for (String ks : invalidKeyStrokes) {
            // WHEN we try to get actions for them:
            try {
                keyManager.getActionsForKeyStroke(ks);
                fail("Expected IllegalArgumentException for invalid keystroke: " + ks);
            }
            catch (IllegalArgumentException ignored) {
                // Expected exception
            }
        }
    }

    @Test
    public void clear_withRegisteredAction_shouldClearActionAccelerator() throws Exception {
        // GIVEN a registered action:
        Action action = new AbstractAction("ClearAcceleratorAction") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // no-op
            }
        };
        keyManager.registerHandler("alt+del", action);
        assertNotNull(action.getValue(Action.ACCELERATOR_KEY), "Action should have received an accelerator.");

        // WHEN we clear the key manager:
        keyManager.clear();

        // THEN the action should no longer have an accelerator:
        assertNull(action.getValue(Action.ACCELERATOR_KEY), "Action accelerator should be cleared after clear()");
    }

    @Test
    public void dispose_withRegisteredHandlers_shouldClearAll() throws Exception {
        Action action = new AbstractAction("DisposableAction") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // no-op
            }
        };

        // WHEN we register an action and then immediately dispose():
        keyManager.registerHandler("shift+P", action);
        assertEquals(1, keyManager.getActionsForKeyStroke("shift+p").size(), "Should have one handler before dispose");
        keyManager.dispose();

        // THEN it should be cleared out:
        assertTrue(keyManager.getActionsForKeyStroke("shift+p").isEmpty(), "Should have no handlers after dispose");
        assertTrue(keyManager.isEmpty(), "No shortcut should be associated after dispose");

        // We can't test window==null because we don't supply one in unit tests...
        // But we can get KeyStrokeManager to set a "isDisposed" flag and just verify it got hit:
        // It's only set to true at the end of dispose(), which also sets window to null.
        assertTrue(keyManager.isDisposed());
    }

    @Test
    public void keyStrokeToString_and_parseKeyStroke_are_consistent() {
        String[] shortcuts = {
                "ctrl+shift+F3",
                "alt+ENTER",
                "space",
                "a",
                "ctrl+alt+delete",
                "meta+tab"
        };

        for (String s : shortcuts) {
            KeyStroke k1 = KeyStrokeManager.parseKeyStroke(s);
            assertNotNull(k1, "parseKeyStroke should accept: " + s);

            String s2 = KeyStrokeManager.keyStrokeToString(k1);
            assertNotNull(s2, "keyStrokeToString should not return null for: " + s);

            KeyStroke k2 = KeyStrokeManager.parseKeyStroke(s2);
            assertNotNull(k2, "parseKeyStroke should parse the string produced by keyStrokeToString: " + s2);

            assertEquals(k1.getKeyCode(), k2.getKeyCode(), "Key codes should match for " + s + " -> " + s2);
            assertEquals(k1.getModifiers(), k2.getModifiers(), "Modifiers should match for " + s + " -> " + s2);
        }
    }

    @Test
    public void warnIfMultipleHandlers_disabled_noWarning() throws Exception {
        // GIVEN a KeyStrokeManager with warning disabled (default):
        TestLogHandler logHandler = new TestLogHandler();
        Logger testLogger = Logger.getLogger(KeyStrokeManager.class.getName());
        testLogger.addHandler(logHandler);

        try {
            Action action1 = new AbstractAction("A1") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                }
            };
            Action action2 = new AbstractAction("A2") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                }
            };

            // WHEN we register two handlers for the same keystroke:
            keyManager.registerHandler("ctrl+T", action1);
            keyManager.registerHandler("ctrl+T", action2);

            // THEN no warning should be logged:
            assertFalse(logHandler.hasWarningContaining("Multiple handlers"));
        }
        finally {
            testLogger.removeHandler(logHandler);
        }
    }

    @Test
    public void warnIfMultipleHandlers_enabled_logsWarning() throws Exception {
        // GIVEN a KeyStrokeManager with warning enabled:
        TestLogHandler logHandler = new TestLogHandler();
        Logger testLogger = Logger.getLogger(KeyStrokeManager.class.getName());
        testLogger.addHandler(logHandler);

        try {
            keyManager.setWarnIfMultipleHandlers(true);
            assertTrue(keyManager.isWarnIfMultipleHandlers());

            Action action1 = new AbstractAction("A1") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                }
            };
            Action action2 = new AbstractAction("A2") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                }
            };
            Action action3 = new AbstractAction("A3") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                }
            };

            // WHEN we register multiple handlers for the same keystroke:
            keyManager.registerHandler("ctrl+M", action1);
            assertFalse(logHandler.hasWarningContaining("Multiple handlers"),
                       "No warning should be logged for first handler");

            keyManager.registerHandler("ctrl+M", action2);
            assertTrue(logHandler.hasWarningContaining("Multiple handlers"),
                      "Warning should be logged for second handler");
            assertTrue(logHandler.hasWarningContaining("Ctrl+M"),
                      "Warning should include keystroke name");
            assertTrue(logHandler.hasWarningContaining("2 handlers"),
                      "Warning should include handler count");

            // Clear log and add a third handler:
            logHandler.clear();
            keyManager.registerHandler("ctrl+M", action3);
            assertTrue(logHandler.hasWarningContaining("Multiple handlers"),
                      "Warning should be logged for third handler");
            assertTrue(logHandler.hasWarningContaining("3 handlers"),
                      "Warning should include updated handler count");
        }
        finally {
            testLogger.removeHandler(logHandler);
        }
    }

    @Test
    public void checkForMultipleHandlers_noMultiple_returnsEmpty() throws Exception {
        // GIVEN a manager with only single handlers:
        Action action1 = new AbstractAction("A1") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
            }
        };
        Action action2 = new AbstractAction("A2") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
            }
        };

        keyManager.registerHandler("F1", action1);
        keyManager.registerHandler("F2", action2);

        // WHEN we check for multiple handlers:
        List<KeyStroke> result = keyManager.checkForMultipleHandlers();

        // THEN the result should be empty:
        assertNotNull(result);
        assertTrue(result.isEmpty(), "Should return empty list when no keystrokes have multiple handlers");
    }

    @Test
    public void checkForMultipleHandlers_withMultiple_returnsCorrectList() throws Exception {
        // GIVEN a manager with some multiple handlers:
        Action action1 = new AbstractAction("A1") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
            }
        };
        Action action2 = new AbstractAction("A2") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
            }
        };
        Action action3 = new AbstractAction("A3") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
            }
        };

        // Register handlers: F1 has 2, F2 has 1, F3 has 3
        keyManager.registerHandler("F1", action1);
        keyManager.registerHandler("F1", action2);
        keyManager.registerHandler("F2", action3);
        keyManager.registerHandler("F3", action1);
        keyManager.registerHandler("F3", action2);
        keyManager.registerHandler("F3", action3);

        // WHEN we check for multiple handlers:
        List<KeyStroke> result = keyManager.checkForMultipleHandlers();

        // THEN the result should contain only F1 and F3:
        assertNotNull(result);
        assertEquals(2, result.size(), "Should return 2 keystrokes with multiple handlers");

        // Verify F1 and F3 are in the list:
        KeyStroke f1 = KeyStrokeManager.parseKeyStroke("F1");
        KeyStroke f3 = KeyStrokeManager.parseKeyStroke("F3");
        assertTrue(result.contains(f1), "Result should contain F1");
        assertTrue(result.contains(f3), "Result should contain F3");
    }

    @Test
    public void checkForMultipleHandlers_afterClear_returnsEmpty() throws Exception {
        // GIVEN a manager with multiple handlers:
        Action action1 = new AbstractAction("A1") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
            }
        };
        Action action2 = new AbstractAction("A2") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
            }
        };

        keyManager.registerHandler("ctrl+Q", action1);
        keyManager.registerHandler("ctrl+Q", action2);
        assertEquals(1, keyManager.checkForMultipleHandlers().size());

        // WHEN we clear the manager:
        keyManager.clear();

        // THEN the result should be empty:
        List<KeyStroke> result = keyManager.checkForMultipleHandlers();
        assertNotNull(result);
        assertTrue(result.isEmpty(), "Should return empty list after clear");
    }

    @Test
    public void registerHandler_withActionListenerAndStringShortcut_shouldRegister() throws Exception {
        // GIVEN an ActionListener:
        AtomicInteger invoked = new AtomicInteger(0);
        ActionListener actionListener = e -> invoked.incrementAndGet();

        // WHEN we register it to a keystroke using the ActionListener convenience method that accepts a String:
        keyManager.registerHandler("ctrl+L", actionListener);

        // THEN it should be registered correctly:
        List<Action> actions = keyManager.getActionsForKeyStroke("ctrl+l");
        assertEquals(1, actions.size(), "Should have one handler after register");

        // WHEN we clear it:
        keyManager.clear();

        // THEN the wrapped Action should be cleaned up like any other Action:
        assertTrue(keyManager.getActionsForKeyStroke("ctrl+l").isEmpty(), "Should have no handlers after clear");
    }

    @Test
    public void registerHandler_withActionListenerAndKeyStrokeShortcut_shouldRegister() throws Exception {
        // GIVEN an ActionListener:
        AtomicInteger invoked = new AtomicInteger(0);
        ActionListener actionListener = e -> invoked.incrementAndGet();

        // WHEN we register it to a keystroke using the ActionListener convenience method that accepts a KeyStroke:
        keyManager.registerHandler(KeyStrokeManager.parseKeyStroke("ctrl+L"), actionListener);

        // THEN it should be registered correctly:
        List<Action> actions = keyManager.getActionsForKeyStroke("ctrl+l");
        assertEquals(1, actions.size(), "Should have one handler after register");

        // WHEN we clear it:
        keyManager.clear();

        // THEN the wrapped Action should be cleaned up like any other Action:
        assertTrue(keyManager.getActionsForKeyStroke("ctrl+l").isEmpty(), "Should have no handlers after clear");
    }

    /**
     * Custom log handler for testing warning messages.
     */
    static class TestLogHandler extends Handler {
        private final List<LogRecord> records = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            records.add(record);
        }

        public boolean hasWarningContaining(String message) {
            return records.stream()
                          .anyMatch(r -> r.getLevel() == Level.WARNING &&
                                  r.getMessage().contains(message));
        }

        public void clear() {
            records.clear();
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }
}
