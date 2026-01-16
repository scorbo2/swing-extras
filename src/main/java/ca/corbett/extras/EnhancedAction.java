package ca.corbett.extras;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.KeyStroke;

/**
 * Extends the AbstractAction class to provide convenient wrappers around setting
 * the name, icon, short description, and accelerator. This saves a small amount of annoying
 * code having to interact with an obtuse Map of String to Object provided
 * by the Action and AbstractAction classes.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.7
 */
public abstract class EnhancedAction extends AbstractAction {

    /**
     * Creates an empty EnhancedAction with no name or icon.
     */
    public EnhancedAction() {
    }

    /**
     * Creates a new EnhancedAction with the specified name and no icon.
     * This is useful for an undecorated button or menu item.
     *
     * @param name The text to use as button or menu item label.
     */
    public EnhancedAction(String name) {
        super(name);
    }

    /**
     * Creates a new EnhancedAction with the specified icon and no name.
     * This is useful for an icon-only button.
     *
     * @param icon The icon to use for the button or menu item.
     */
    public EnhancedAction(Icon icon) {
        super(null, icon);
    }

    /**
     * Creates a new EnhancedAction with the specified name and icon.
     *
     * @param name The text to use as button or menu item label.
     * @param icon The icon to use for the button or menu item.
     */
    public EnhancedAction(String name, Icon icon) {
        super(name, icon);
    }

    /**
     * Sets a new name for this action. This will update any existing button
     * or menu item using this action.
     *
     * @param name The new text to use as button or menu item label.
     * @return This EnhancedAction, for method chaining.
     */
    public EnhancedAction setName(String name) {
        putValue(NAME, name);
        return this;
    }

    /**
     * Sets a new icon for this action. This will update any existing button
     * or menu item using this action.
     *
     * @param icon The new icon to use for the button or menu item.
     * @return This EnhancedAction, for method chaining.
     */
    public EnhancedAction setIcon(Icon icon) {
        putValue(SMALL_ICON, icon);
        return this;
    }

    /**
     * Sets a new short description for this action. This will update any existing button
     * or menu item using this action. The short description is typically used
     * as the tooltip text for buttons.
     *
     * @param description The new short description / tooltip text.
     * @return This EnhancedAction, for method chaining.
     */
    public EnhancedAction setDescription(String description) {
        putValue(SHORT_DESCRIPTION, description);
        return this;
    }

    /**
     * Synonym for setDescription, for improved readability when setting tooltips.
     *
     * @param tooltip The new tooltip text.
     * @return This EnhancedAction, for method chaining.
     */
    public EnhancedAction setTooltip(String tooltip) {
        return setDescription(tooltip);
    }

    /**
     * Sets a new accelerator key for this action. This will update any existing menu item
     * using this action. The accelerator key is the keyboard shortcut that
     * activates the action when the menu item is focused.
     *
     * @param key The new accelerator key.
     * @return This EnhancedAction, for method chaining.
     */
    public EnhancedAction setAcceleratorKey(KeyStroke key) {
        putValue(ACCELERATOR_KEY, key);
        return this;
    }

    /**
     * Returns the name of this action, or null if no name is set.
     *
     * @return The name of this action. Might be null.
     */
    public String getName() {
        return (String)getValue(NAME);
    }

    /**
     * Returns the icon of this action, or null if no icon is set.
     *
     * @return The icon of this action. Might be null.
     */
    public Icon getIcon() {
        return (Icon)getValue(SMALL_ICON);
    }

    /**
     * Returns the short description of this action, or null if no description is set.
     *
     * @return The short description of this action. Might be null.
     */
    public String getDescription() {
        return (String)getValue(SHORT_DESCRIPTION);
    }

    /**
     * Synonym for getDescription, for improved readability when getting tooltips.
     *
     * @return The tooltip text of this action. Might be null.
     */
    public String getTooltip() {
        return getDescription();
    }

    /**
     * Returns the accelerator key of this action, or null if no accelerator key is set.
     * Since the grandparent Action class uses a generic Object map, it's possible that
     * our accelerator key is not actually a KeyStroke, so we check the type before returning it.
     * If not recognized, we return null.
     *
     * @return The accelerator key of this action. Might be null.
     */
    public KeyStroke getAcceleratorKey() {
        Object something = getValue(ACCELERATOR_KEY);
        return (something instanceof KeyStroke) ? (KeyStroke)something : null;
    }
}
