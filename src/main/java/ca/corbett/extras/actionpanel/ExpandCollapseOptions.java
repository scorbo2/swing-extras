package ca.corbett.extras.actionpanel;

/**
 * Encapsulates all expand/collapse related options for an ActionPanel,
 * including animation options.
 * <p>
 *     You can control whether the user is allowed to expand/collapse action groups at all using
 *     <code>setExpandable(boolean allow)</code>. When disallowed, the user will not be able to
 *     expand/collapse groups, and all groups will be forced to be expanded. Note that programmatic
 *     expand/collapse via setExpanded() is always allowed regardless of this setting.
 * </p>
 * <p>
 *     You can optionally allow double-clicking on the group header label to toggle the expanded/collapsed
 *     state of the group, in addition to the provided expand/collapse toggle button.
 *     Double-clicking is disabled by default, but can be enabled with <code>setAllowHeaderDoubleClick(true)</code>.
 * </p>
 * <p>
 *     <b>Animation</b> - by default, expand/collapse operations are animated with a smooth sliding effect.
 *     The default animation duration is 200ms. You can customize the animation speed using
 *     <code>setAnimationDurationMs(int ms)</code>, or disable animation entirely with
 *     <code>setAnimationEnabled(false)</code> to revert to instantaneous expand/collapse.
 *     Note that programmatically calling setExpanded() will always expand/collapse instantly, without animation.
 * </p>
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
        final boolean previousAutoRebuildState = actionPanel.isAutoRebuildEnabled();
        actionPanel.setAutoRebuildEnabled(false);
        try {
            if (!allow) {
                for (String groupName : actionPanel.getGroupNames()) {
                    ActionGroup actionGroup = actionPanel.getGroup(groupName);
                    if (!actionGroup.isExpanded()) {
                        // Mark the group as expanded.
                        // Note we don't go through actionPanel.setExpanded(),
                        // because that would fire off expansion events.
                        // We want to just do this silently.
                        actionGroup.setExpanded(true);
                    }
                }
            }
        }
        finally {
            // Only rebuild once after all groups are rebuilt.
            // Note: if auto-rebuild was disabled before we came along,
            //       then it will still be disabled after we're done.
            //       Our changes won't be visible until whoever disabled
            //       auto-rebuild chooses to re-enable it.
            //       If it WAS enabled before, then this will trigger an immediate rebuild.
            actionPanel.setAutoRebuildEnabled(previousAutoRebuildState);
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
