package ca.corbett.extras.io;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeyboardManagerTest {

    private KeyboardManager keyManager;

    @BeforeEach
    public void setup() {
        keyManager = new KeyboardManager(null);
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
                null,
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                "ctrl+a+b+c+d+e+f+g+h+i+j+k+l+m+n+o+p+q+r+s+t+u+v+w+x+y+z"
        };

        // WHEN we parse them:
        for (String keyStroke : keyStrokes) {
            KeyStroke result = keyManager.parseKeyStroke(keyStroke);

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
            KeyStroke result = keyManager.parseKeyStroke(keyStroke);

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
            KeyStroke result = keyManager.parseKeyStroke(keyStrokes[i]);

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
            KeyStroke result = keyManager.parseKeyStroke(keyStrokes[i]);

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
    public void register_unregister_reassignHandler_flow() {
        AtomicInteger invoked = new AtomicInteger(0);
        Action action = new AbstractAction("MyAction") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                invoked.incrementAndGet();
            }
        };

        // Register to ctrl+X
        keyManager.registerHandler("ctrl+X", action);
        List<Action> handlers = keyManager.getHandlers("ctrl+x");
        assertEquals(1, handlers.size(), "Should have one handler after register");
        assertEquals(action, handlers.get(0), "Registered handler should be present");

        // Shortcut should be discoverable and accelerator stored
        String found = keyManager.getShortcutForHandler(action);
        assertNotNull(found, "getShortcutForHandler should return a string");
        KeyStroke ks = keyManager.parseKeyStroke(found);
        assertEquals(ks, action.getValue(Action.ACCELERATOR_KEY), "Action ACCELERATOR_KEY should be set");

        // Reassign to alt+Y
        keyManager.reassignHandler(action, "alt+Y");
        assertTrue(keyManager.getHandlers("ctrl+x").isEmpty(), "Old shortcut should no longer have handlers");
        List<Action> newHandlers = keyManager.getHandlers("alt+y");
        assertEquals(1, newHandlers.size(), "Should have one handler for new shortcut");
        assertEquals(action, newHandlers.get(0));

        // Unregister
        keyManager.unregisterHandler(action);
        assertTrue(keyManager.getHandlers("alt+y").isEmpty(), "Handlers list should be empty after unregister");
        assertNull(keyManager.getShortcutForHandler(action), "No shortcut should be associated after unregister");
    }

    @Test
    public void registeringSameActionTwice_doesNotDuplicate_and_allowsReassignment() {
        Action action = new AbstractAction("GuardedAction") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // no-op
            }
        };

        // Register to ctrl+A
        keyManager.registerHandler("ctrl+A", action);
        // Register again to same shortcut - implementation should unregister then re-add (no duplicate)
        keyManager.registerHandler("ctrl+A", action);

        List<Action> handlers = keyManager.getHandlers("ctrl+a");
        assertEquals(1, handlers.size(), "Duplicate registration should not create duplicate entries");
        assertEquals(action, handlers.get(0));

        // Now register same action to a different shortcut - should move it
        keyManager.registerHandler("shift+B", action);
        assertTrue(keyManager.getHandlers("ctrl+a").isEmpty(),
                   "Original shortcut should be cleared after reassignment");
        List<Action> newHandlers = keyManager.getHandlers("shift+b");
        assertEquals(1, newHandlers.size(), "Action should now be assigned to new shortcut only");
        assertEquals(action, newHandlers.get(0));
    }

    @Test
    public void multipleActionsSameKeystroke_areBothRegistered_and_haveAccelerators() {
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

        keyManager.registerHandler("F5", action1);
        keyManager.registerHandler("F5", action2);

        List<Action> handlers = keyManager.getHandlers("f5");
        assertEquals(2, handlers.size(), "Both actions should be registered for the same keystroke");
        assertTrue(handlers.contains(action1));
        assertTrue(handlers.contains(action2));

        // Both actions should have ACCELERATOR_KEY set to the same KeyStroke
        KeyStroke expected = keyManager.parseKeyStroke("F5");
        assertEquals(expected, action1.getValue(Action.ACCELERATOR_KEY));
        assertEquals(expected, action2.getValue(Action.ACCELERATOR_KEY));
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
            KeyStroke k1 = keyManager.parseKeyStroke(s);
            assertNotNull(k1, "parseKeyStroke should accept: " + s);

            String s2 = keyManager.keyStrokeToString(k1);
            assertNotNull(s2, "keyStrokeToString should not return null for: " + s);

            KeyStroke k2 = keyManager.parseKeyStroke(s2);
            assertNotNull(k2, "parseKeyStroke should parse the string produced by keyStrokeToString: " + s2);

            assertEquals(k1.getKeyCode(), k2.getKeyCode(), "Key codes should match for " + s + " -> " + s2);
            assertEquals(k1.getModifiers(), k2.getModifiers(), "Modifiers should match for " + s + " -> " + s2);
        }
    }
}