package ca.corbett.extras.io;

import javax.swing.Action;
import javax.swing.KeyStroke;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * This class allows very easy registration of keyboard shortcuts for a given Window.
 * Shortcuts can be specified in string format (for example: "shift+F1" or "Ctrl+O"),
 * and their associated Actions will be executed when the shortcuts are pressed.
 * Multiple handlers can be registered for the same shortcut, in which case all
 * associated actions will be triggered when the shortcut is pressed.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.7
 */
public class KeyboardManager {

    private static final Logger log = Logger.getLogger(KeyboardManager.class.getName());

    private boolean isDisposed = false;
    private Window window;
    private final Map<KeyStroke, List<Action>> keyBindings = new ConcurrentHashMap<>();
    private boolean isEnabled;
    private final KeyEventDispatcher keyDispatcher = new CustomKeyDispatcher();

    /**
     * Creates a keyboard manager for the given window. This keyboard manager is immediately
     * installed into the given window and enabled. You can use setEnabled(false) to temporarily
     * disable it if needed.
     *
     * @param window the window that must be active to receive shortcuts.
     */
    public KeyboardManager(Window window) {
        this.window = window;
        this.isEnabled = window != null; // disable if no window provided
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyDispatcher);
    }

    /**
     * Disposes this keyboard manager, removing its key event dispatcher from the
     * KeyboardFocusManager and clearing all registered key bindings. If your KeyboardManager
     * is attached to your application's main window, you don't need to invoke this method.
     * It's intended more for temporary windows or dialogs that may be created and destroyed
     * multiple times during the application's lifetime.
     */
    public void dispose() {
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.removeKeyEventDispatcher(keyDispatcher);
        keyBindings.clear();
        window = null;
        isDisposed = true;
    }

    /**
     * Reports whether this keyboard manager is currently enabled.
     */
    public boolean isEnabled() {
        return isEnabled;
    }

    /**
     * Can be used to enable or disable this keyboard manager. When disabled, no keyboard
     * shortcuts will be processed.
     */
    public KeyboardManager setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        return this;
    }

    /**
     * Shorthand for setEnabled(false).
     */
    public void suspend() {
        setEnabled(false);
    }

    /**
     * Shorthand for setEnabled(true).
     */
    public void resume() {
        setEnabled(true);
    }

    /**
     * Use this method to check if a given shortcut string is valid.
     *
     * @param shortcut Any candidate keyboard shortcut string. Example: "ctrl+P"
     * @return true if the shortcut is valid, false otherwise
     */
    public boolean isKeyStrokeValid(String shortcut) {
        return parseKeyStroke(shortcut) != null;
    }

    /**
     * Returns a list of all registered keyboard shortcuts.
     * You can use this in conjunction with getHandlers() to
     * build out a complete list of all registered shortcuts and their actions.
     *
     * @return A sorted list of all shortcuts that have at least one action registered.
     */
    public List<String> getRegisteredShortcuts() {
        List<String> shortcuts = new ArrayList<>();
        for (KeyStroke keyStroke : keyBindings.keySet()) {
            String shortcut = keyStrokeToString(keyStroke);
            if (shortcut != null) {
                shortcuts.add(shortcut);
            }
        }
        shortcuts.sort(null); // sort alphabetically for easier reading
        return shortcuts;
    }

    /**
     * Returns the action(s) registered for the given shortcut.
     * The returned list may be empty, but will not be null.
     *
     * @param shortcut The keyboard shortcut string to check
     * @return A list of Actions registered for that shortcut
     */
    public List<Action> getHandlers(String shortcut) {
        KeyStroke keyStroke = parseKeyStroke(shortcut);
        if (keyStroke != null) {
            List<Action> actions = keyBindings.get(keyStroke);
            if (actions != null) {
                synchronized(actions) {
                    if (!actions.isEmpty()) {
                        return new ArrayList<>(actions);
                    }
                }
            }
        }
        return new ArrayList<>();
    }

    /**
     * Returns the shortcut string registered for the given action, if the
     * action is registered. If the action is not registered, null is returned.
     *
     * @param action The action to check
     * @return The shortcut string, or null if not registered
     */
    public String getShortcutForHandler(Action action) {
        for (Map.Entry<KeyStroke, List<Action>> entry : keyBindings.entrySet()) {
            if (entry.getValue().contains(action)) {
                // We prevent actions from being registered multiple times,
                // so it's safe to return the first match we find:
                return keyStrokeToString(entry.getKey());
            }
        }
        return null;
    }

    /**
     * Removes all registered keyboard shortcuts and their associated actions.
     */
    public void clear() {
        keyBindings.clear();
    }

    /**
     * Registers a keyboard shortcut with an action.
     * Accepts shortcuts in the format: "ctrl+P", "alt+F4", "ctrl+shift+S", etc.
     * If the given shortcut is already registered, the new action will be added
     * to the list of actions to be executed when that shortcut is pressed.
     * If the given shortcut is invalid, a warning will be logged and
     * your action will not be registered!
     * <p>
     * An action can only be registered once. If you invoke registerHandler() with
     * an action that is already assigned to a shortcut, its previous assignment
     * will be removed, and this new assignment will replace it.
     * </p>
     *
     * @param shortcut the keyboard shortcut (e.g., "ctrl+P", "F5")
     * @param action   the action to execute
     * @return this manager, for fluent-style method chaining
     */
    public KeyboardManager registerHandler(String shortcut, Action action) {
        // For logging purposes, let's get the action name:
        String actionName = (String)action.getValue(Action.NAME);
        if (actionName == null) {
            actionName = action.toString(); // fallback
        }

        // First, check that the given shortcut is valid:
        KeyStroke keyStroke = parseKeyStroke(shortcut);
        if (keyStroke == null) {
            log.warning("registerHandler: Invalid shortcut format: " + shortcut +
                                "; action \"" + actionName + "\" - ignoring request.");
            return this;
        }

        // Is this action already registered for another shortcut?
        String existingShortcut = getShortcutForHandler(action);
        if (existingShortcut != null) {
            log.warning("registerHandler: Action \"" + actionName + "\" " +
                                "is already registered for shortcut " +
                                existingShortcut + "; replacing with new shortcut " + shortcut);
            // Unregister it first:
            unregisterHandler(action);
        }

        // We support multiple actions for the same shortcut:
        // (use synchronizedList to avoid concurrency issues)
        List<Action> actions = keyBindings.computeIfAbsent(keyStroke,
                                                           k -> Collections.synchronizedList(new ArrayList<>()));
        actions.add(action);

        // Store the accelerator in the Action so menu items can access it
        action.putValue(Action.ACCELERATOR_KEY, keyStroke);

        return this;
    }

    /**
     * Updates the keyboard shortcut for an action that's already registered.
     * This will automatically update any menu items using this action.
     * Actions are identified by their instance, so make sure you pass in
     * the same Action instance that was used to register the original shortcut.
     *
     * @param action      the action (must be the same instance used in menus)
     * @param newShortcut the new shortcut to register
     * @return this manager, for fluent-style method chaining
     */
    public KeyboardManager reassignHandler(Action action, String newShortcut) {
        unregisterHandler(action);
        registerHandler(newShortcut, action);
        return this;
    }

    /**
     * Removes a previously registered action.
     *
     * @param action The action to unregister.
     * @return this manager, for fluent-style method chaining
     */
    public KeyboardManager unregisterHandler(Action action) {
        String shortcut = getShortcutForHandler(action);
        if (shortcut != null) {
            KeyStroke keyStroke = parseKeyStroke(shortcut);
            if (keyStroke != null) {
                List<Action> actions = keyBindings.get(keyStroke);
                synchronized(actions) {
                    if (actions != null) {
                        actions.remove(action);
                        if (actions.isEmpty()) {
                            keyBindings.remove(keyStroke);
                        }
                    }
                }
            }

            // Remove the accelerator from the Action:
            action.putValue(Action.ACCELERATOR_KEY, null);
        }

        return this;
    }

    /**
     * Parses a string representation of a keyboard shortcut into a KeyStroke.
     * Supports formats like: "ctrl+P", "alt+F4", "ctrl+shift+S", "F5", etc.
     *
     * @param shortcut the shortcut string
     * @return the KeyStroke, or null if invalid
     */
    protected KeyStroke parseKeyStroke(String shortcut) {
        if (shortcut == null || shortcut.trim().isEmpty()) {
            log.fine("parseKeyStroke: Shortcut string is null or empty");
            return null;
        }

        String normalized = shortcut.trim().toLowerCase();
        String[] parts = normalized.split("\\+");

        int modifiers = 0;
        String keyName = null;

        for (String part : parts) {
            part = part.trim();
            switch (part) {
                case "ctrl":
                case "control":
                    modifiers |= KeyEvent.CTRL_DOWN_MASK;
                    break;
                case "alt":
                    modifiers |= KeyEvent.ALT_DOWN_MASK;
                    break;
                case "shift":
                    modifiers |= KeyEvent.SHIFT_DOWN_MASK;
                    break;
                case "meta":
                case "cmd":
                case "command":
                    modifiers |= KeyEvent.META_DOWN_MASK;
                    break;
                default:
                    if (keyName != null) {
                        log.fine("parseKeyStroke: Multiple key names found in shortcut: " + shortcut);
                        return null;
                    }
                    keyName = part;
                    break;
            }
        }

        if (keyName == null) {
            log.fine("parseKeyStroke: No key name found in shortcut: " + shortcut);
            return null;
        }

        // Handle function keys and special keys
        int keyCode = getKeyCode(keyName);
        if (keyCode == KeyEvent.VK_UNDEFINED) {
            log.fine("parseKeyStroke: Unrecognized key name: " + keyName);
            return null;
        }

        return KeyStroke.getKeyStroke(keyCode, modifiers);
    }

    /**
     * Gets the key code for a given key name.
     */
    protected int getKeyCode(String keyName) {
        // Handle single characters
        if (keyName.length() == 1) {
            char c = keyName.toUpperCase().charAt(0);
            return KeyEvent.getExtendedKeyCodeForChar(c);
        }

        // Handle function keys
        if (keyName.matches("f\\d+")) {
            try {
                int fNum = Integer.parseInt(keyName.substring(1));
                if (fNum >= 1 && fNum <= 24) {
                    return KeyEvent.VK_F1 + (fNum - 1);
                }
            }
            catch (NumberFormatException e) {
                log.warning("getKeyCode: Invalid function key number in key name: " + keyName);
                return KeyEvent.VK_UNDEFINED;
            }
        }

        // Handle special keys
        return switch (keyName) {
            case "enter" -> KeyEvent.VK_ENTER;
            case "escape", "esc" -> KeyEvent.VK_ESCAPE;
            case "space" -> KeyEvent.VK_SPACE;
            case "tab" -> KeyEvent.VK_TAB;
            case "backspace" -> KeyEvent.VK_BACK_SPACE;
            case "delete", "del" -> KeyEvent.VK_DELETE;
            case "insert", "ins" -> KeyEvent.VK_INSERT;
            case "home" -> KeyEvent.VK_HOME;
            case "end" -> KeyEvent.VK_END;
            case "pageup" -> KeyEvent.VK_PAGE_UP;
            case "pagedown" -> KeyEvent.VK_PAGE_DOWN;
            case "up" -> KeyEvent.VK_UP;
            case "down" -> KeyEvent.VK_DOWN;
            case "left" -> KeyEvent.VK_LEFT;
            case "right" -> KeyEvent.VK_RIGHT;
            default -> KeyEvent.VK_UNDEFINED;
        };
    }

    /**
     * Converts a KeyStroke to its string representation.
     * Produces formats like: "Ctrl+P", "Alt+F4", "Ctrl+Shift+S", "F5", etc.
     *
     * @param keyStroke the KeyStroke to convert
     * @return the string representation, or null if keyStroke is null
     */
    protected String keyStrokeToString(KeyStroke keyStroke) {
        if (keyStroke == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        int modifiers = keyStroke.getModifiers();

        // Add modifiers in a consistent order
        if ((modifiers & KeyEvent.CTRL_DOWN_MASK) != 0) {
            sb.append("Ctrl+");
        }
        if ((modifiers & KeyEvent.ALT_DOWN_MASK) != 0) {
            sb.append("Alt+");
        }
        if ((modifiers & KeyEvent.SHIFT_DOWN_MASK) != 0) {
            sb.append("Shift+");
        }
        if ((modifiers & KeyEvent.META_DOWN_MASK) != 0) {
            sb.append("Meta+");
        }

        // Add the key name
        int keyCode = keyStroke.getKeyCode();
        String keyName = getKeyName(keyCode);
        sb.append(keyName);

        return sb.toString();
    }

    /**
     * Gets the string name for a given key code.
     */
    protected String getKeyName(int keyCode) {
        // Handle special keys that might overlap with function key range
        switch (keyCode) {
            case KeyEvent.VK_ENTER:
                return "Enter";
            case KeyEvent.VK_ESCAPE:
                return "Esc";
            case KeyEvent.VK_SPACE:
                return "Space";
            case KeyEvent.VK_TAB:
                return "Tab";
            case KeyEvent.VK_BACK_SPACE:
                return "Backspace";
            case KeyEvent.VK_DELETE:
                return "Delete";
            case KeyEvent.VK_INSERT:
                return "Insert";
            case KeyEvent.VK_HOME:
                return "Home";
            case KeyEvent.VK_END:
                return "End";
            case KeyEvent.VK_PAGE_UP:
                return "PageUp";
            case KeyEvent.VK_PAGE_DOWN:
                return "PageDown";
            case KeyEvent.VK_UP:
                return "Up";
            case KeyEvent.VK_DOWN:
                return "Down";
            case KeyEvent.VK_LEFT:
                return "Left";
            case KeyEvent.VK_RIGHT:
                return "Right";
        }

        // Handle function keys (check after special keys to avoid conflicts)
        if (keyCode >= KeyEvent.VK_F1 && keyCode <= KeyEvent.VK_F24) {
            int fNum = keyCode - KeyEvent.VK_F1 + 1;
            return "F" + fNum;
        }

        // Return whatever KeyEvent.getKeyText provides for other keys:
        // (At worst this will be "Unknown keyCode: xxx" with some hex value)
        return KeyEvent.getKeyText(keyCode);
    }

    private class CustomKeyDispatcher implements KeyEventDispatcher {

        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            // Don't process if we're disabled:
            if (!isEnabled) {
                return false;
            }

            // Don't process if our window is null or isn't active:
            if (window == null || !window.isActive()) {
                return false;
            }

            // Only process KEY_PRESSED events:
            if (e.getID() != KeyEvent.KEY_PRESSED) {
                return false;
            }

            // Create KeyStroke from the event:
            KeyStroke keyStroke = KeyStroke.getKeyStrokeForEvent(e);

            // Check if we have a registered handler for that KeyStroke:
            List<Action> actions = keyBindings.get(keyStroke);
            synchronized(actions) {
                if (actions != null && !actions.isEmpty()) {

                    // Multiple handlers can be registered for the same shortcut:
                    // (That's a bit wonky, but it can happen by accident if multiple
                    //  application extensions happen to want the same shortcut.
                    //  This conflict will be highlighted to the user in application
                    //  settings, and they can remap one of the conflicting shortcuts
                    //  to something else if desired).
                    for (Action action : actions) {
                        // Don't execute disabled actions:
                        if (action.isEnabled()) {
                            action.actionPerformed(new ActionEvent(window,
                                                                   ActionEvent.ACTION_PERFORMED,
                                                                   keyStrokeToString(keyStroke)));
                        }
                        else {
                            log.info("KeyboardManager: action for shortcut " +
                                             keyStrokeToString(keyStroke) + " is disabled; not executing.");
                        }
                    }
                    return true;
                }
            }

            return false;
        }
    }

    /**
     * For testing purposes, to verify dispose()
     */
    boolean isDisposed() {
        return isDisposed && keyBindings.isEmpty();
    }
}