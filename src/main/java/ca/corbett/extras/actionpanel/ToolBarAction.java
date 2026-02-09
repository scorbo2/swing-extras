package ca.corbett.extras.actionpanel;

import ca.corbett.extras.EnhancedAction;

import javax.swing.ImageIcon;

/**
 * An abstract base class for actions that are intended to be added to a ToolBar.
 * The principal difference between this class and the parent EnhancedAction class
 * is that we insist on an icon, and we ignore the action name.
 * Tooltips are recommended but not required. Instances of this class are also
 * linked to an ActionPanel, and more specifically to a named ActionGroup within
 * that panel. This provides context for the implementing classes so that they
 * can act on the correct ActionGroup when triggered.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
 */
public abstract class ToolBarAction extends EnhancedAction {

    protected final ActionPanel actionPanel;
    protected final String groupName;

    /**
     * Unlike a regular EnhancedAction, we don't care about action name here.
     * You must instead supply the name of the ActionGroup (case-insensitive)
     * that this action will act upon. You should also specify a non-null
     * tooltip for the button, though this can be null or empty for no tooltip.
     * <p>
     * Note that icons are required! Our buttons are icon-only and do not show text.
     * You can use any of the built-in icons in SwingFormsResources if you don't have one handy.
     * </p>
     *
     * @param actionPanel The containing ActionPanel.
     * @param groupName   The name of the ActionGroup that this action will act upon. This is case-insensitive.
     * @param tooltip     The tooltip to show on the button for this action. Can be null or empty for no tooltip.
     * @param icon        The icon for this action. Can't be null (our buttons are icon-only).
     */
    public ToolBarAction(ActionPanel actionPanel, String groupName, String tooltip, ImageIcon icon) {
        super("ToolBarAction"); // name is never shown
        if (actionPanel == null) {
            throw new IllegalArgumentException("actionPanel cannot be null");
        }
        if (groupName == null) {
            throw new IllegalArgumentException("groupName cannot be null");
        }
        if (icon == null) {
            throw new IllegalArgumentException("icon cannot be null");
        }
        this.actionPanel = actionPanel;
        this.groupName = groupName;
        setTooltip(tooltip);
        setIcon(icon);
    }

    /**
     * Gets the name of the ActionGroup that this action will act upon. This is case-insensitive.
     *
     * @return The name of the ActionGroup that this action will act upon. This is case-insensitive.
     */
    public String getGroupName() {
        return groupName;
    }
}
