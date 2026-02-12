package ca.corbett.extras.actionpanel;

/**
 * Encapsulates all expand/collapse related options for an ActionPanel,
 * including animation options.
 * <p>
 * This class only exists to relieve some clutter from the ActionPanel class,
 * which is getting quite large.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
 */
public class ExpandCollapseOptions extends ActionPanelOptions {
    public static final int DEFAULT_ANIMATION_DURATION_MS = 200;
    public static final int ANIMATION_FRAME_DELAY_MS = 10;

    private final ActionPanel actionPanel;
    private int animationDurationMs;
    private boolean animationEnabled;
    private boolean allowHeaderDoubleClick;
    private boolean allowExpandCollapse;

    /**
     * Should only be instantiated by ActionPanel.
     * Access via ActionPanel.getExpandCollapseOptions().
     */
    ExpandCollapseOptions(ActionPanel owner) {
        this.actionPanel = owner;
        this.animationDurationMs = DEFAULT_ANIMATION_DURATION_MS;
        this.animationEnabled = true; // enabled by default
        this.allowHeaderDoubleClick = false; // disabled by default
        this.allowExpandCollapse = true; // why would you ever disable it? :)
    }

    /**
     * Allows or disallows the user to expand/collapse action groups.
     * By default, this is allowed. Note that programmatic expand/collapse
     * via setExpanded() is always allowed regardless of this setting.
     *
     * @param allow True to allow expand/collapse, false to disallow.
     * @return This ExpandCollapseOptions instance, for method chaining.
     */
    public ExpandCollapseOptions setExpandable(boolean allow) {
        this.allowExpandCollapse = allow;

        // Force-expand any currently collapsed groups if we're disallowing expand/collapse,
        // to avoid leaving the user with no way to see the contents of those groups.
        actionPanel.setAutoRebuildEnabled(false);
        try {
            if (!allow) {
                for (String groupName : actionPanel.getGroupNames()) {
                    actionPanel.setExpanded(groupName, true);
                }
            }
        }
        finally {
            // Only rebuild once after all groups are rebuilt.
            actionPanel.setAutoRebuildEnabled(true);
        }

        return this;
    }

    /**
     * Reports whether the user is allowed to expand/collapse action groups.
     *
     * @return True if expand/collapse is allowed, false otherwise.
     */
    public boolean isExpandable() {
        return allowExpandCollapse;
    }

    /**
     * Returns whether animation is enabled for expand/collapse operations.
     *
     * @return True if animation is enabled, false otherwise.
     */
    public boolean isAnimationEnabled() {
        return animationEnabled;
    }

    /**
     * Sets whether animation is enabled for expand/collapse operations.
     * When disabled, groups will expand and collapse instantaneously.
     *
     * @param animationEnabled True to enable animation, false to disable it.
     * @return This ExpandCollapseOptions instance, for method chaining.
     */
    public ExpandCollapseOptions setAnimationEnabled(boolean animationEnabled) {
        this.animationEnabled = animationEnabled;
        fireOptionsChanged();
        return this;
    }

    /**
     * Returns the duration of the expand/collapse animation in milliseconds.
     *
     * @return The animation duration in milliseconds.
     */
    public int getAnimationDurationMs() {
        return animationDurationMs;
    }

    /**
     * Sets the duration of the expand/collapse animation in milliseconds.
     * The default is 200ms. A longer duration will result in a slower animation.
     *
     * @param animationDurationMs The animation duration in milliseconds. Must be greater than 0.
     * @return This ExpandCollapseOptions instance, for method chaining.
     */
    public ExpandCollapseOptions setAnimationDurationMs(int animationDurationMs) {
        if (animationDurationMs <= 0) {
            throw new IllegalArgumentException("Animation duration must be greater than 0.");
        }
        this.animationDurationMs = animationDurationMs;
        fireOptionsChanged();
        return this;
    }
    /**
     * Allows or disallows double-clicking on the group header label
     * to toggle the expanded/collapsed state of the group.
     * By default, this is disabled.
     *
     * @param allow True to allow double-clicking on the header to toggle expand/collapse.
     * @return This ExpandCollapseOptions instance, for method chaining.
     */
    public ExpandCollapseOptions setAllowHeaderDoubleClick(boolean allow) {
        this.allowHeaderDoubleClick = allow;
        fireOptionsChanged();
        return this;
    }

    /**
     * Reports whether double-clicking on the group header label
     * is allowed to toggle the expanded/collapsed state of the group.
     *
     * @return True if double-clicking on the header toggles expand/collapse, false otherwise.
     */
    public boolean isAllowHeaderDoubleClick() {
        return allowHeaderDoubleClick;
    }
}
