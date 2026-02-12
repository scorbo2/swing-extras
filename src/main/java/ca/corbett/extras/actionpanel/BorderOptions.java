package ca.corbett.extras.actionpanel;

import javax.swing.border.Border;

/**
 * Encapsulates all border-related options for an ActionPanel. In addition to setting a border
 * around the ActionPanel itself, which can be done through the usual means, the following
 * additional border options are supported for the action groups within the ActionPanel::
 * <ul>
 *     <li><b>Group border</b> - extends around the entire action group.</li>
 *     <li><b>Header border</b> - extends around the header section of an action group.</li>
 *     <li><b>Action tray border</b> - extends around the action portion of an action group. That is,
 *         not including the header at the top, or the toolbar at the bottom. The middle section only.</li>
 *     <li><b>ToolBar border</b> - extends around the ToolBar section of an action group.</li>
 * </ul>
 * <p>
 *     The above options are set once, and apply to all action groups within an ActionPanel. It is not
 *     currently possible to have different borders per action group.
 * </p>
 * <p>
 *     The above options can be set independently, or can be used in any combination.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
 */
public class BorderOptions extends ActionPanelOptions {
    private Border groupBorder;
    private Border headerBorder;
    private Border actionTrayBorder;
    private Border toolBarBorder;

    /**
     * Should only be instantiated by ActionPanel.
     * Access via ActionPanel.getBorderOptions().
     */
    BorderOptions() {
        this.groupBorder = null;
        this.headerBorder = null;
        this.actionTrayBorder = null;
        this.toolBarBorder = null;
    }

    /**
     * Sets a border that will extend around the entire action group (all sections).
     *
     * @param border The border to use, or null for no border.
     * @return This BorderOptions instance, for method chaining.
     */
    public BorderOptions setGroupBorder(Border border) {
        groupBorder = border;
        fireOptionsChanged();
        return this;
    }

    /**
     * Returns the group border, or null if no border is set.
     *
     * @return A Border to use for action groups, or null for no border.
     */
    public Border getGroupBorder() {
        return groupBorder;
    }

    /**
     * Sets the border for action group headers. The header is the top section containing the
     * group name and expand/collapse button, not including the action tray or toolbar below it.
     *
     * @param border The border to use, or null for no border.
     * @return This BorderOptions instance, for method chaining.
     */
    public BorderOptions setHeaderBorder(Border border) {
        headerBorder = border;
        fireOptionsChanged();
        return this;
    }

    /**
     * Returns the border for action group headers, or null if no border is set.
     *
     * @return A Border to use for group headers, or null for no border.
     */
    public Border getHeaderBorder() {
        return headerBorder;
    }

    /**
     * Sets the border for the action tray section of action groups. The action tray is the middle section of an
     * action group, not including the header at the top, or the toolbar at the bottom.
     *
     * @param border The border to use, or null for no border.
     * @return This BorderOptions instance, for method chaining.
     */
    public BorderOptions setActionTrayBorder(Border border) {
        actionTrayBorder = border;
        fireOptionsChanged();
        return this;
    }

    /**
     * Returns the border for the action tray section of action groups, or null if no border is set.
     *
     * @return A Border to use for the action tray section of action groups, or null for no border.
     */
    public Border getActionTrayBorder() {
        return actionTrayBorder;
    }

    /**
     * Sets the border for the ToolBar section of action groups. The ToolBar is the bottom section of an
     * action group, not including the header at the top, or the action tray in the middle.
     *
     * @param border The border to use, or null for no border.
     * @return This BorderOptions instance, for method chaining.
     */
    public BorderOptions setToolBarBorder(Border border) {
        toolBarBorder = border;
        fireOptionsChanged();
        return this;
    }

    /**
     * Returns the border for the ToolBar section of action groups, or null if no border is set.
     *
     * @return A Border to use for the ToolBar section of action groups, or null for no border.
     */
    public Border getToolBarBorder() {
        return toolBarBorder;
    }
}
