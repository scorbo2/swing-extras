package ca.corbett.extras.actionpanel;

import java.awt.Color;

/**
 * A class that encapsulates all color-related options for ActionPanel. This includes
 * options for the panel background, action foreground/background, group header foreground/background,
 * and toolbar button background.
 * <p>
 * This class only exists to relieve some clutter from the ActionPanel class,
 * which is getting quite large.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class ColorOptions extends ActionPanelOptions {

    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    private Color panelBackground;
    private Color actionBackground;
    private Color actionForeground;
    private Color groupHeaderBackground;
    private Color groupHeaderForeground;
    private Color actionButtonBackground;
    private Color toolBarButtonBackground;

    /**
     * Should only be instantiated by ActionPanel.
     * Access via ActionPanel.getColorOptions().
     */
    ColorOptions() {
        useSystemDefaults();
    }

    /**
     * Convenient shorthand for setting all properties to null, meaning that
     * the Look and Feel default colors will be used for everything.
     * This is the default state of a new ColorOptions instance.
     *
     * @return This ColorOptions instance, for method chaining.
     */
    public ColorOptions useSystemDefaults() {
        this.panelBackground = null; // Use L&F default
        this.actionForeground = null; // Use L&F default
        this.actionBackground = null; // Use L&F default
        this.groupHeaderForeground = null; // Use L&F default
        this.groupHeaderBackground = null; // Use L&F default
        this.actionButtonBackground = null; // Use L&F default
        this.toolBarButtonBackground = null; // Use L&F default
        fireOptionsChanged();
        return this;
    }

    /**
     * Sets all colors according to the supplied ColorTheme instance.
     *
     * @param theme The ColorTheme to apply. Cannot be null.
     * @return This ColorOptions instance, for method chaining.
     */
    public ColorOptions setFromTheme(ColorTheme theme) {
        if (theme == null) {
            throw new IllegalArgumentException("theme cannot be null");
        }
        this.panelBackground = theme.getPanelBackground();
        this.actionForeground = theme.getActionForeground();
        this.actionBackground = theme.getActionBackground();
        this.groupHeaderForeground = theme.getGroupHeaderForeground();
        this.groupHeaderBackground = theme.getGroupHeaderBackground();
        this.actionButtonBackground = theme.getActionButtonBackground();
        this.toolBarButtonBackground = theme.getToolBarButtonBackground();
        fireOptionsChanged();
        return this;
    }

    /**
     * Sets the background color for the entire panel. This is the color that is shown
     * behind the ActionGroups. This overrides the Look and Feel default color.
     * You can pass null to revert to the L&F default.
     *
     * @param color The background color, or null to use the L&F default.
     * @return This ColorOptions instance, for method chaining.
     */
    public ColorOptions setPanelBackground(Color color) {
        panelBackground = color;
        fireOptionsChanged();
        return this;
    }

    /**
     * Returns the background color for the entire panel. This is the color that is shown
     * behind the ActionGroups. May be null if Look and Feel defaults are in use.
     *
     * @return The panel background color, or null if L&F default is in use.
     */
    public Color getPanelBackground() {
        return panelBackground;
    }

    /**
     * Sets the foreground color for action items. If useLabels is true, this is the text
     * color; if useButtons is true, this is the button foreground color. This overrides
     * the Look and Feel default color. You can pass null to revert to the L&F default.
     *
     * @param color The foreground color, or null to use the L&F default.
     * @return This ColorOptions instance, for method chaining.
     */
    public ColorOptions setActionForeground(Color color) {
        actionForeground = color;
        fireOptionsChanged();
        return this;
    }

    /**
     * Returns the foreground color for action items. May be null if Look and Feel defaults
     * are in use.
     *
     * @return The action foreground color, or null if L&F default is in use.
     */
    public Color getActionForeground() {
        return actionForeground;
    }

    /**
     * Sets the background color for action items. This is the color that is shown
     * in the action area, behind the labels or buttons. Our labels are transparent,
     * but our buttons are not. So, if useLabels is false, this color will only be
     * visible in the padding areas around the buttons. To change the background
     * color of the action buttons themselves, use setActionButtonBackground().
     * <p>
     * This overrides the Look and Feel default color.
     * You can pass null to revert to the L&F default.
     * </p>
     *
     * @param color The background color, or null to use the L&F default.
     * @return This ColorOptions instance, for method chaining.
     */
    public ColorOptions setActionBackground(Color color) {
        actionBackground = color;
        fireOptionsChanged();
        return this;
    }

    /**
     * Returns the background color for action items. May be null if Look and Feel defaults
     * are in use.
     *
     * @return The action background color, or null if L&F default is in use.
     */
    public Color getActionBackground() {
        return actionBackground;
    }

    /**
     * If action buttons are enabled instead of labels, this is the background color of the buttons themselves.
     * This is not the same as the action background color, which is the color that is shown behind the buttons.
     * This overrides the Look and Feel default color. You can pass null to revert to the L&F default.
     *
     * @param color The background color for action buttons, or null to use the L&F default.
     * @return This ColorOptions instance, for method chaining.
     */
    public ColorOptions setActionButtonBackground(Color color) {
        actionButtonBackground = color;
        fireOptionsChanged();
        return this;
    }

    /**
     * If action buttons are enabled instead of labels, this is the background color of the buttons themselves.
     * This is not the same as the action background color, which is the color that is
     * shown behind the buttons. May be null if Look and Feel defaults are in use.
     *
     * @return The background color for action buttons, or null if L&F default is in use.
     */
    public Color getActionButtonBackground() {
        return actionButtonBackground;
    }

    /**
     * Sets the foreground color for group headers. This overrides the
     * Look and Feel default color. You can pass null to revert to the L&F default.
     *
     * @param color The foreground color, or null to use the L&F default.
     * @return This ColorOptions instance, for method chaining.
     */
    public ColorOptions setGroupHeaderForeground(Color color) {
        groupHeaderForeground = color;
        fireOptionsChanged();
        return this;
    }

    /**
     * Returns the foreground color for group headers. May be null if Look and Feel defaults
     * are in use.
     *
     * @return The group header foreground color, or null if L&F default is in use.
     */
    public Color getGroupHeaderForeground() {
        return groupHeaderForeground;
    }

    /**
     * Sets the background color for group headers. This overrides the
     * Look and Feel default color. You can pass null to revert to the L&F default.
     *
     * @param color The background color, or null to use the L&F default.
     * @return This ColorOptions instance, for method chaining.
     */
    public ColorOptions setGroupHeaderBackground(Color color) {
        groupHeaderBackground = color;
        fireOptionsChanged();
        return this;
    }

    /**
     * Returns the background color for group headers. May be null if Look and Feel defaults
     * are in use.
     *
     * @return The group header background color, or null if L&F default is in use.
     */
    public Color getGroupHeaderBackground() {
        return groupHeaderBackground;
    }

    /**
     * Sets the background color for toolbar buttons. You can pass null here to
     * use the L&F default color, or you can use the setToolBarButtonsTransparent() convenience
     * method to render the buttons as fully transparent (icons only, no button background).
     * <p>
     * Note that this is not the same as setting the action background color;
     * the action background color is the color that is shown behind the buttons,
     * while this is the color of the buttons themselves.
     * </p>
     *
     * @param color The background color for toolbar buttons, or null to use the L&F default.
     * @return This ColorOptions instance, for method chaining.
     */
    public ColorOptions setToolBarButtonBackground(Color color) {
        toolBarButtonBackground = color;
        fireOptionsChanged();
        return this;
    }

    /**
     * Sets the toolbar buttons to have a transparent background, so that only the icons are visible.
     * Whatever background color you have set for the action area will show through behind the icons.
     * This is a convenient shortcut for setToolBarButtonBackground(TRANSPARENT).
     *
     * @return This ColorOptions instance, for method chaining.
     */
    public ColorOptions setToolBarButtonsTransparent() {
        return setToolBarButtonBackground(TRANSPARENT);
    }

    /**
     * Returns the background color for toolbar buttons. May be null if Look and Feel defaults
     * are in use.
     *
     * @return The background color for toolbar buttons, or null if L&F default is in use.
     */
    public Color getToolBarButtonBackground() {
        return toolBarButtonBackground;
    }
}
