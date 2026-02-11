package ca.corbett.extras.io;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * This class allows very easy registration of keyboard shortcuts for a given Window.
 * Shortcuts can be specified in string format (for example: "shift+F1" or "Ctrl+O")
 * or in KeyStroke format. Actions can be registered to respond to those shortcuts,
 * and all associated Actions will be executed when the relevant shortcut is pressed.
 * Multiple Actions can be registered for the same shortcut, in which case all
 * associated actions will be triggered when the shortcut is pressed.
 * The same Action can be registered multiple times for different shortcuts,
 * in case you want to allow multiple ways of launching the same action.
 * <H2>Usage Example:</H2>
 * <pre>
 *     // Create a KeyStrokeManager for your main window:
 *     KeyStrokeManager ksm = new KeyStrokeManager(mainWindow);
 *
 *     // Add some basic shortcuts:
 *     ksm.registerHandler("ctrl+O", openFileAction);
 *     ksm.registerHandler("ctrl+S", saveFileAction);
 *     ksm.registerHandler("ctrl+A", aboutAction);
 *
 *     // That's literally all there is to it!
 *     // As long as your main window is active,
 *     // those shortcuts will trigger the associated actions.
 *
 *     // You can temporarily disable the manager if needed:
 *     ksm.setEnabled(false); // disables shortcut processing
 *     ksm.setEnabled(true);  // re-enables shortcut processing
 *
 *     // And you can reassign shortcuts as needed:
 *     ksm.reassignHandler(saveFileAction, "ctrl+shift+S");
 * </pre>
 * <p>
 *     You should also look at the KeyStrokeProperty and KeyStrokeField
 *     classes to allow easy user customization of your shortcuts!
 *     With KeyStrokeProperty, you can easily add these to your
 *     application settings, where they will be automatically persisted
 *     between application sessions.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.7
 */
public class KeyStrokeManager {
    private static final Logger log = Logger.getLogger(KeyStrokeManager.class.getName());

    private boolean isDisposed = false;
    private final Set<Window> windows = new HashSet<>();
    private final Map<KeyStroke, List<Action>> keyMap = new ConcurrentHashMap<>();
    private boolean isEnabled;
    private boolean warnIfMultipleHandlers = false;
    private final KeyEventDispatcher keyDispatcher = new CustomKeyDispatcher();

    /**
     * Creates a KeyStrokeManager for the given window. This KeyStrokeManager is immediately
     * installed into the given window and enabled. You can use setEnabled(false) to temporarily
     * disable it if needed.
     *
     * @param window the window that must be active to receive shortcuts.
     */
    public KeyStrokeManager(Window window) {
       if (window != null){
            this.windows.add(window);
       }
        this.isEnabled = window != null; // We need a window to be enabled
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyDispatcher);
    }

    /**
     * Disposes this KeyStrokeManager, removing its key event dispatcher from the
     * KeyboardFocusManager and clearing all registered key bindings. If your KeyStrokeManager
     * is attached to your application's main window, you don't need to invoke this method.
     * It's intended more for temporary windows or dialogs that may be created and destroyed
     * multiple times during the application's lifetime.
     * <p>
     * Can safely be called multiple times; subsequent calls after the first will have no effect.
     * </p>
     */
    public void dispose() {
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.removeKeyEventDispatcher(keyDispatcher);
        keyMap.clear();
        windows.clear();
        isDisposed = true;
    }

    /**
     * Adds a new window to this KeyStrokeManager
     */
    public void addWindow(Window window){
       if (window == null){
            throw new IllegalArgumentException("Window cannot be null!");
       }
       
       this.windows.add(window);
       // previously to adding multiple window support it was the normal behavior to enable the manager during initial setup if the window != null
       // to mantain this behavior we validate if there is any window after the operation and update isEnabled
        this.isEnabled = !this.windows.isEmpty();
    }

    /**
     * Removes an specified window from this KeyStrokeManager
     */
    public void removeWindow(Window window){
        if(window != null){
           if(this.windows.remove(window)){
            log.fine("window reference was found and removed");
           }else{
            log.warning("window reference wasn't found in this KeystrokeManager instance");
           }
        } else {
            throw new IllegalArgumentException("Window cannot be null!");
        }

        //by giving the user the chance to delete any window from the manager we also give him the chance to remove them all, in case there are no more
        // active window we disable the manager
        this.isEnabled = !this.windows.isEmpty();

    }

    /**
     * Reports whether this KeyStrokeManager is currently enabled.
     */
    public boolean isEnabled() {
        return isEnabled;
    }

    /**
     * Returns whether this KeyStrokeManager will warn when multiple handlers
     * are registered for the same keystroke.
     *
     * @return true if warnings are enabled, false otherwise
     */
    public boolean isWarnIfMultipleHandlers() {
        return warnIfMultipleHandlers;
    }

    /**
     * Sets whether this KeyStrokeManager should warn when multiple handlers
     * are registered for the same keystroke. When enabled, a warning will be
     * logged if registerHandler is called for a keystroke that already has
     * at least one handler registered.
     *
     * @param warnIfMultipleHandlers true to enable warnings, false to disable
     * @return this manager, for fluent-style method chaining
     */
    public KeyStrokeManager setWarnIfMultipleHandlers(boolean warnIfMultipleHandlers) {
        this.warnIfMultipleHandlers = warnIfMultipleHandlers;
        return this;
    }

    /**
     * Can be used to enable or disable this KeyStrokeManager. When disabled, no keyboard
     * shortcuts will be processed. This does not unregister any assigned actions!
     * It simply means those actions will not be invoked until we are re-enabled.
     */
    public KeyStrokeManager setEnabled(boolean enabled) {
        // If our window is null, we cannot be enabled:
        if (windows.isEmpty()) {
            log.warning("KeyStrokeManager.setEnabled: cannot enable; there is no main window attached.");
            this.isEnabled = false;
            return this;
        }
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
     * Returns the KeyStroke(s) currently assigned to the given Action, or an empty list
     * if the given Action is not found.
     *
     * @param action The action to search for.
     * @return A List of KeyStrokes assigned to the action. Empty if none found.
     */
    public List<KeyStroke> getKeyStrokesForAction(Action action) {
        List<KeyStroke> keyStrokes = new ArrayList<>();
        for (KeyStroke key : keyMap.keySet()) {
            List<Action> actions = keyMap.get(key);
            if (actions != null && actions.contains(action)) {
                keyStrokes.add(key);
            }
        }
        return keyStrokes;
    }

    /**
     * Returns the Action(s) currently assigned to the given KeyStroke, or an empty list
     * if the KeyStroke is not found.
     *
     * @param keyStroke The KeyStroke to search for.
     * @return A List of Actions assigned to the KeyStroke. Empty if none found.
     */
    public List<Action> getActionsForKeyStroke(KeyStroke keyStroke) {
        List<Action> actions = keyMap.get(keyStroke);
        return actions != null ? new ArrayList<>(actions) : new ArrayList<>();
    }

    /**
     * Returns the Action(s) currently assigned to the given KeyStroke, or an empty list
     * if the KeyStroke is not found.
     *
     * @param keyStroke The String version of the KeyStroke to search for.
     * @return A List of Actions assigned to the KeyStroke. Empty if none found.
     * @throws IllegalArgumentException if the given keyStroke string is invalid
     */
    public List<Action> getActionsForKeyStroke(String keyStroke) {
        if (!isKeyStrokeValid(keyStroke)) {
            throw new IllegalArgumentException("getActionsForKeyStroke: invalid keyStroke string: " + keyStroke);
        }
        return getActionsForKeyStroke(parseKeyStroke(keyStroke));
    }

    /**
     * Removes all registered keyboard shortcuts and their associated actions.
     * This will also clear the accelerator keys from all associated Actions.
     */
    public KeyStrokeManager clear() {
        for (List<Action> actions : keyMap.values()) {
            for (Action action : actions) {
                action.putValue(Action.ACCELERATOR_KEY, null);
            }
        }
        keyMap.clear();
        return this;
    }

    /**
     * Reports whether any keyboard shortcuts are currently registered.
     */
    public boolean isEmpty() {
        return keyMap.isEmpty();
    }

    /**
     * Reports whether an action is already registered.
     *
     * @param action The action to search for.
     * @return true if the action has at least one registered shortcut, false otherwise
     */
    public boolean isRegistered(Action action) {
        return !getKeyStrokesForAction(action).isEmpty();
    }

    /**
     * Shorthand for !hasHandlers(keyStroke)
     */
    public boolean isAvailable(KeyStroke keyStroke) {
        return !hasHandlers(keyStroke);
    }

    /**
     * Shorthand for !hasHandlers(keyStroke)
     */
    public boolean isAvailable(String keyStroke) {
        if (!isKeyStrokeValid(keyStroke)) {
            throw new IllegalArgumentException("isAvailable: invalid keyStroke string: " + keyStroke);
        }
        return !hasHandlers(keyStroke);
    }

    /**
     * Reports whether any actions are registered for the given KeyStroke.
     *
     * @param keyStroke The KeyStroke to check.
     * @return true if at least one action is registered for the KeyStroke, false otherwise
     */
    public boolean hasHandlers(KeyStroke keyStroke) {
        return !getActionsForKeyStroke(keyStroke).isEmpty();
    }

    /**
     * Reports whether any actions are registered for the given KeyStroke.
     *
     * @param keyStroke The String version of the KeyStroke to check.
     * @return true if at least one action is registered for the KeyStroke, false otherwise
     * @throws IllegalArgumentException If the given keyStroke string is invalid
     */
    public boolean hasHandlers(String keyStroke) {
        if (!isKeyStrokeValid(keyStroke)) {
            throw new IllegalArgumentException("hasHandlers: invalid keyStroke string: " + keyStroke);
        }
        return hasHandlers(parseKeyStroke(keyStroke));
    }

    /**
     * Returns a list of all keystrokes that have more than one handler registered.
     * This can be used to programmatically detect and handle duplicate handler assignments.
     * An empty list is returned if no keystrokes have multiple handlers.
     *
     * @return A List of KeyStrokes that have more than one handler registered. Empty if none found.
     */
    public List<KeyStroke> checkForMultipleHandlers() {
        List<KeyStroke> result = new ArrayList<>();
        for (Map.Entry<KeyStroke, List<Action>> entry : keyMap.entrySet()) {
            if (entry.getValue() != null && entry.getValue().size() > 1) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /**
     * Registers a keyboard shortcut with an action.
     * Accepts shortcuts in the format: "ctrl+P", "alt+F4", "ctrl+shift+S", etc.
     * If the given shortcut already has an action registered, the new action will be added
     * to the list of actions to be executed when that shortcut is pressed.
     * If the given shortcut is invalid, an IllegalArgumentException will be thrown.
     * <p>
     * IllegalArgumentException will be thrown if any parameter is null.
     * </p>
     *
     * @param keyStroke the String version of the KeyStroke to register
     * @param action    the action to execute when the shortcut is pressed
     * @return this manager, for fluent-style method chaining
     * @throws IllegalArgumentException if the given shortcut string is invalid
     */
    public KeyStrokeManager registerHandler(String keyStroke, Action action) {
        if (!isKeyStrokeValid(keyStroke)) {
            throw new IllegalArgumentException("registerHandler: invalid keyStroke string: " + keyStroke);
        }
        return registerHandler(parseKeyStroke(keyStroke), action);
    }

    /**
     * A convenience method to allow registration of simple ActionListeners without having to create an Action object.
     * With lambdas, this can reduce the adding of a handler to a single line, in the case where your application
     * doesn't have a ready-made Action to use. For example:
     * <pre>
     *     ksm.registerHandler("ctrl+P", e -> openFile());
     *     ksm.registerHandler("ctrl+S", e -> saveFile());
     * </pre>
     * <p>
     * The drawback of this approach is that you will be unable to invoke unregisterHandler()
     * to remove your handler later - but you can still call clear() to remove all handlers if needed.
     * </p>
     *
     * @param keyStroke the String version of the KeyStroke to register
     * @param action    the ActionListener to execute when the shortcut is pressed
     * @return this manager, for fluent-style method chaining
     */
    public KeyStrokeManager registerHandler(String keyStroke, ActionListener action) {
        // Wrap this ActionListener in an anonymous AbstractAction so we can register it like normal:
        Action wrappedAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.actionPerformed(e);
            }
        };
        return registerHandler(keyStroke, wrappedAction);
    }

    /**
     * Registers a keyboard shortcut with an action.
     * Accepts shortcuts in the format: "ctrl+P", "alt+F4", "ctrl+shift+S", etc.
     * If the given shortcut already has an action registered, the new action will be added
     * to the list of actions to be executed when that shortcut is pressed.
     * If the given shortcut is invalid, an IllegalArgumentException will be thrown.
     * <p>
     * IllegalArgumentException will be thrown if any parameter is null.
     * </p>
     *
     * @param keyStroke the KeyStroke to register
     * @param action    the action to execute when the keyStroke is pressed
     * @return this manager, for fluent-style method chaining
     */
    public KeyStrokeManager registerHandler(KeyStroke keyStroke, Action action) {
        if (keyStroke == null || action == null) {
            throw new IllegalArgumentException("registerHandler: keyStroke and action must not be null.");
        }

        // Check if this keystroke already has handlers
        List<Action> existingHandlers = keyMap.get(keyStroke);
        boolean alreadyHasHandlers = existingHandlers != null && !existingHandlers.isEmpty();

        // Okay, we can register it now.
        keyMap.computeIfAbsent(keyStroke, k -> new ArrayList<>()).add(action);

        // If warning is enabled and we just added a second (or more) handler, log a warning
        if (warnIfMultipleHandlers && alreadyHasHandlers) {
            String keyStrokeStr = keyStrokeToString(keyStroke);
            log.warning("Multiple handlers registered for keystroke: " + keyStrokeStr + 
                       " (now has " + keyMap.get(keyStroke).size() + " handlers)");
        }

        // Store the accelerator in the Action so menu items can access it
        action.putValue(Action.ACCELERATOR_KEY, keyStroke);

        return this;
    }

    /**
     * A convenience method to allow registration of simple ActionListeners without having to create an Action object.
     * With lambdas, this can reduce the adding of a handler to a single line, in the case where your application
     * doesn't have a ready-made Action to use. For example:
     * <pre>
     *     ksm.registerHandler(parseKeyStroke("ctrl+P"), e -> openFile());
     *     ksm.registerHandler(parseKeyStroke("ctrl+S"), e -> saveFile());
     * </pre>
     * <p>
     * The drawback of this approach is that you will be unable to invoke unregisterHandler()
     * to remove your handler later - but you can still call clear() to remove all handlers if needed.
     * </p>
     *
     * @param keyStroke the KeyStroke to register
     * @param listener  the ActionListener to execute when the keyStroke is pressed
     * @return this manager, for fluent-style method chaining
     */
    public KeyStrokeManager registerHandler(KeyStroke keyStroke, ActionListener listener) {
        // Wrap this ActionListener in an anonymous AbstractAction so we can register it like normal:
        Action wrappedAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listener.actionPerformed(e);
            }
        };
        return registerHandler(keyStroke, wrappedAction);
    }

    /**
     * Unregisters the given action, removing its keyboard shortcut.
     * If the named action was not found, this method does nothing.
     *
     * @param action The action to unregister.
     * @return this manager, for fluent-style method chaining
     */
    public KeyStrokeManager unregisterHandler(Action action) {
        List<KeyStroke> assignedKeyStrokes = getKeyStrokesForAction(action);
        for (KeyStroke keyStroke : assignedKeyStrokes) {
            List<Action> actions = keyMap.get(keyStroke);
            if (actions != null) {
                actions.remove(action);
                if (actions.isEmpty()) {
                    keyMap.remove(keyStroke);
                }
            }
        }

        // Remove the accelerator from the Action:
        action.putValue(Action.ACCELERATOR_KEY, null);

        return this;
    }

    /**
     * If the given action is registered, reassigns its keyboard shortcut to the newKeyStroke.
     * If the action is not found, this method does nothing. If this action was previously registered
     * to multiple keystrokes, the previous assignments are removed. If that is not the intention,
     * then consider calling registerHandler() instead to add a new shortcut without removing existing ones.
     * <p>
     * This is therefore equivalent to unregisterHandler(action) followed by registerHandler(newShortcut, action).
     * </p>
     *
     * @param action       The action to reassign.
     * @param newKeyStroke The new keystroke in string format (e.g., "ctrl+P", "F5"). Must be valid!
     * @return this manager, for fluent-style method chaining
     * @throws IllegalArgumentException if the given newShortcut string is invalid
     */
    public KeyStrokeManager reassignHandler(Action action, String newKeyStroke) {
        // We should validate the keyStroke first, so we don't unregister the action if the new shortcut is invalid:
        KeyStroke ks = parseKeyStroke(newKeyStroke);
        if (ks == null) {
            throw new IllegalArgumentException("reassignHandler: invalid newShortcut string: " + newKeyStroke);
        }

        // Now we can make it so:
        unregisterHandler(action);
        registerHandler(ks, action);
        return this;
    }

    /**
     * If the given action is registered, reassigns its keyboard shortcut to the newKeyStroke.
     * If the action is not found, this method does nothing. If this action was previously registered
     * to multiple shortcuts, the previous assignments are removed. If that is not the intention,
     * then consider calling registerHandler() instead to add a new shortcut without removing existing ones.
     * <p>
     * This is therefore equivalent to unregisterHandler(action) followed by registerHandler(newShortcut, action).
     * </p>
     *
     * @param action       The action to reassign.
     * @param newKeyStroke The new keyboard shortcut string (e.g., "ctrl+P", "F5"). Must be valid!
     * @return this manager, for fluent-style method chaining
     */
    public KeyStrokeManager reassignHandler(Action action, KeyStroke newKeyStroke) {
        unregisterHandler(action);
        registerHandler(newKeyStroke, action);
        return this;
    }

    /**
     * Parses a string representation of a keyboard shortcut into a KeyStroke.
     * Supports formats like: "ctrl+P", "alt+F4", "ctrl+shift+S", "F5", etc.
     *
     * @param shortcut the shortcut string
     * @return the KeyStroke, or null if invalid
     */
    public static KeyStroke parseKeyStroke(String shortcut) {
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
                case "win":
                case "windows":
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
    protected static int getKeyCode(String keyName) {
        if (keyName == null) {
            log.warning("KeyStrokeManager.getKeyCode(null) invoked.");
            return KeyEvent.VK_UNDEFINED;
        }

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

        // Handle special keys:
        return switch (keyName) {
            case "enter" -> KeyEvent.VK_ENTER;
            case "escape", "esc" -> KeyEvent.VK_ESCAPE;
            case "space" -> KeyEvent.VK_SPACE;
            case "tab" -> KeyEvent.VK_TAB;
            case "backspace" -> KeyEvent.VK_BACK_SPACE;
            case "delete", "del" -> KeyEvent.VK_DELETE;
            case "insert", "ins" -> KeyEvent.VK_INSERT;
            case "pause" -> KeyEvent.VK_PAUSE;
            case "home" -> KeyEvent.VK_HOME;
            case "end" -> KeyEvent.VK_END;
            case "pageup", "pgup" -> KeyEvent.VK_PAGE_UP;
            case "pagedown", "pgdn", "pgdwn", "pgdown" -> KeyEvent.VK_PAGE_DOWN;
            case "up" -> KeyEvent.VK_UP;
            case "down" -> KeyEvent.VK_DOWN;
            case "left" -> KeyEvent.VK_LEFT;
            case "right" -> KeyEvent.VK_RIGHT;
            case "comma" -> KeyEvent.VK_COMMA;
            case "dot", "period" -> KeyEvent.VK_PERIOD;
            case "minus", "dash" -> KeyEvent.VK_MINUS;
            case "equals", "equal" -> KeyEvent.VK_EQUALS;
            case "slash", "forwardslash" -> KeyEvent.VK_SLASH;
            case "backslash" -> KeyEvent.VK_BACK_SLASH;
            case "semicolon" -> KeyEvent.VK_SEMICOLON;
            case "plus" -> KeyEvent.VK_PLUS;
            case "numpad0", "num0", "numpad_0" -> KeyEvent.VK_NUMPAD0;
            case "numpad1", "num1", "numpad_1" -> KeyEvent.VK_NUMPAD1;
            case "numpad2", "num2", "numpad_2" -> KeyEvent.VK_NUMPAD2;
            case "numpad3", "num3", "numpad_3" -> KeyEvent.VK_NUMPAD3;
            case "numpad4", "num4", "numpad_4" -> KeyEvent.VK_NUMPAD4;
            case "numpad5", "num5", "numpad_5" -> KeyEvent.VK_NUMPAD5;
            case "numpad6", "num6", "numpad_6" -> KeyEvent.VK_NUMPAD6;
            case "numpad7", "num7", "numpad_7" -> KeyEvent.VK_NUMPAD7;
            case "numpad8", "num8", "numpad_8" -> KeyEvent.VK_NUMPAD8;
            case "numpad9", "num9", "numpad_9" -> KeyEvent.VK_NUMPAD9;
            case "numpaddivide", "numpad_divide", "numdivide" -> KeyEvent.VK_DIVIDE;
            case "numpadmultiple", "numpad_multiply", "nummultiply" -> KeyEvent.VK_MULTIPLY;
            case "numpadsubstract", "numpad_subtract", "numsubtract", "numpad_minus", "numpadminus" ->
                    KeyEvent.VK_SUBTRACT;
            case "numpadadd", "numpad_add", "numadd", "numpad_plus", "numpadplus" -> KeyEvent.VK_ADD;
            case "numpaddot", "numpad_dot", "numdecimal" -> KeyEvent.VK_DECIMAL;
            case "numlock" -> KeyEvent.VK_NUM_LOCK;
            case "scrolllock", "scrlk" -> KeyEvent.VK_SCROLL_LOCK;
            case "printscreen", "prtsc" -> KeyEvent.VK_PRINTSCREEN;
            case "backtick" -> KeyEvent.VK_BACK_QUOTE;
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
    public static String keyStrokeToString(KeyStroke keyStroke) {
        if (keyStroke == null) {
            log.warning("KeyStrokeManager.keyStrokeToString(null) invoked.");
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
    protected static String getKeyName(int keyCode) {
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
            case KeyEvent.VK_PAUSE:
                return "Pause";
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
            case KeyEvent.VK_COMMA:
                return "Comma";
            case KeyEvent.VK_PERIOD:
                return "Dot";
            case KeyEvent.VK_MINUS:
                return "Minus";
            case KeyEvent.VK_EQUALS:
                return "Equals";
            case KeyEvent.VK_SLASH:
                return "Slash";
            case KeyEvent.VK_BACK_SLASH:
                return "Backslash";
            case KeyEvent.VK_SEMICOLON:
                return "Semicolon";
            case KeyEvent.VK_PLUS:
                return "Plus";
            case KeyEvent.VK_NUMPAD0:
                return "Numpad0";
            case KeyEvent.VK_NUMPAD1:
                return "Numpad1";
            case KeyEvent.VK_NUMPAD2:
                return "Numpad2";
            case KeyEvent.VK_NUMPAD3:
                return "Numpad3";
            case KeyEvent.VK_NUMPAD4:
                return "Numpad4";
            case KeyEvent.VK_NUMPAD5:
                return "Numpad5";
            case KeyEvent.VK_NUMPAD6:
                return "Numpad6";
            case KeyEvent.VK_NUMPAD7:
                return "Numpad7";
            case KeyEvent.VK_NUMPAD8:
                return "Numpad8";
            case KeyEvent.VK_NUMPAD9:
                return "Numpad9";
            case KeyEvent.VK_DIVIDE:
                return "NumpadDivide";
            case KeyEvent.VK_MULTIPLY:
                return "NumpadMultiply";
            case KeyEvent.VK_SUBTRACT:
                return "NumpadMinus";
            case KeyEvent.VK_ADD:
                return "NumpadPlus";
            case KeyEvent.VK_DECIMAL:
                return "NumpadDot";
            case KeyEvent.VK_NUM_LOCK:
                return "NumLock";
            case KeyEvent.VK_SCROLL_LOCK:
                return "ScrollLock";
            case KeyEvent.VK_PRINTSCREEN:
                return "PrintScreen";
            case KeyEvent.VK_BACK_QUOTE:
                return "Backtick";
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

    /**
     * Our internal KeyEventDispatcher that processes key events
     * and triggers registered actions as needed.
     */
    private class CustomKeyDispatcher implements KeyEventDispatcher {

        private Optional<Window> getActiveWindowIfAny()
        {
            // Only a single window can be active at the same time, but even if there is more than one the event must be only called once
            return windows.stream().filter(Window::isActive).findFirst();
        }

        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {

            Optional<Window> activeWindow = getActiveWindowIfAny();

            // Don't process if we're disabled:
            if (!isEnabled) {
                return false;
            }

            // Don't process if none of our windows are active
            if (activeWindow.isEmpty()) {
                return false;
            }

            // Only process KEY_PRESSED events:
            if (e.getID() != KeyEvent.KEY_PRESSED) {
                return false;
            }

            // Create KeyStroke from the event:
            KeyStroke keyStroke = KeyStroke.getKeyStrokeForEvent(e);
            String keyStrokeStr = keyStrokeToString(keyStroke);
            log.fine("KeyStrokeManager: Key pressed: " + keyStrokeStr);

            // Check if we have a registered handler for that KeyStroke:
            List<Action> actions = getActionsForKeyStroke(keyStroke);
            if (actions != null && !actions.isEmpty()) {
                synchronized(actions) {
                    // Multiple handlers can be registered for the same shortcut:
                    // (That's a bit wonky, but it can happen by accident if multiple
                    //  application extensions happen to want the same shortcut.
                    //  This conflict will be highlighted to the user in application
                    //  settings, and they can remap one of the conflicting shortcuts
                    //  to something else if desired).
                    for (Action action : actions) {
                        // Don't execute disabled actions:
                        if (action.isEnabled()) {
                            action.actionPerformed(new ActionEvent(activeWindow.get(),
                                                                   ActionEvent.ACTION_PERFORMED,
                                                                   keyStrokeStr));
                        }
                        else {
                            log.info("KeyStrokeManager: action for shortcut " +
                                             keyStrokeStr + " is disabled; not executing.");
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
        return isDisposed && keyMap.isEmpty();
    }

}
