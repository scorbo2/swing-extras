package ca.corbett.extras.actionpanel;

import java.awt.Color;

/**
 * Provides a few built-in color themes that you can use to quickly change the look of your ActionPanel.
 * <p>
 * <b>USAGE:</b>
 * </p>
 * <pre>
 *     // Get the ColorOptions for your ActionPanel:
 *     ColorOptions colorOptions = actionPanel.getColorOptions();
 *
 *     // Set from one of the predefined themes:
 *     colorOptions.setFromTheme(ColorTheme.DEFAULT);
 *
 *     // Now you can customize any part of it, or leave it as-is:
 *     colorOptions.setActionForeground(Color.WHITE);
 * </pre>
 * <p>
 * At any time, you can very easily abandon color customization, and return to
 * default values supplied by your current Look and Feel, by calling:
 * </p>
 * <pre>
 *     // Disregard all custom colors and let the Look and Feel decide:
 *     colorOptions.useSystemDefaults();
 * </pre>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
 */
public enum ColorTheme {

    DEFAULT("ActionPanel default",
            Color.DARK_GRAY,                   // panel background
            Color.LIGHT_GRAY,                  // action background
            Color.BLACK,                       // action foreground
            new Color(70, 130, 180),  // group header background
            Color.WHITE,                       // group header foreground
            new Color(180, 180, 180), // action button background
            new Color(160, 160, 160)),// toolbar button background

    LIGHT("Light",
          new Color(255, 255, 255),  // panel background
          new Color(240, 240, 240),  // action background
          new Color(0, 0, 0),        // action foreground
          new Color(220, 220, 220),  // group header background
          new Color(0, 0, 0),        // group header foreground
          new Color(200, 200, 200),  // action button background
          new Color(200, 200, 200)), // toolbar button background

    DARK("Dark",
         new Color(45, 45, 45),     // panel background
         new Color(60, 60, 60),     // action background
         new Color(220, 220, 220),  // action foreground
         new Color(70, 70, 70),     // group header background
         new Color(220, 220, 220),  // group header foreground
         new Color(80, 80, 80),     // action button background
         new Color(80, 80, 80)),    // toolbar button background

    ICE("Ice",
        Color.DARK_GRAY,                    // panel background
        new Color(224, 255, 255),  // action background
        new Color(0, 0, 128),      // action foreground
        new Color(75, 110, 175),   // group header background
        new Color(185, 238, 238),  // group header foreground
        new Color(176, 204, 204),  // action button background
        new Color(176, 204, 204)), // toolbar button background

    MATRIX("Matrix",
           new Color(0, 48, 0),      // panel background
           new Color(0, 64, 0),      // action background
           new Color(0, 255, 0),     // action foreground
           new Color(0, 128, 0),     // group header background
           new Color(0, 255, 0),     // group header foreground
           new Color(0, 96, 0),      // action button background
           new Color(0, 96, 0)),     // toolbar button background

    GOT_THE_BLUES("Got the blues",
                  new Color(73, 46, 196),    // panel background
                  new Color(135, 206, 250),  // action background
                  new Color(0, 0, 139),      // action foreground
                  new Color(70, 130, 180),   // group header background
                  new Color(255, 255, 255),  // group header foreground
                  new Color(100, 149, 237),  // action button background
                  new Color(100, 149, 237)), // toolbar button background

    SHADES_OF_GRAY("Shades of gray",
                   new Color(192, 192, 192),  // panel background
                   new Color(160, 160, 160),  // action background
                   new Color(0, 0, 0),        // action foreground
                   new Color(128, 128, 128),  // group header background
                   new Color(255, 255, 255),  // group header foreground
                   new Color(140, 140, 140),  // action button background
                   new Color(140, 140, 140)), // toolbar button background

    HOT_DOG_STAND("Hotdog Stand", // Just for fun!
                  new Color(255, 228, 196),  // panel background
                  new Color(255, 160, 122),  // action background
                  new Color(0, 0, 0),        // action foreground
                  new Color(255, 69, 0),     // group header background
                  Color.YELLOW,                       // group header foreground
                  new Color(255, 140, 0),    // action button background
                  new Color(255, 140, 0));   // toolbar button background

    private final String label;
    private final Color panelBackground;
    private final Color actionBackground;
    private final Color actionForeground;
    private final Color groupHeaderBackground;
    private final Color groupHeaderForeground;
    private final Color actionButtonBackground;
    private final Color toolBarButtonBackground;

    ColorTheme(String label, Color panelBackground, Color actionBackground, Color actionForeground,
               Color groupHeaderBackground, Color groupHeaderForeground, Color actionButtonBackground,
               Color toolBarButtonBackground) {
        this.label = label;
        this.panelBackground = panelBackground;
        this.actionBackground = actionBackground;
        this.actionForeground = actionForeground;
        this.groupHeaderBackground = groupHeaderBackground;
        this.groupHeaderForeground = groupHeaderForeground;
        this.actionButtonBackground = actionButtonBackground;
        this.toolBarButtonBackground = toolBarButtonBackground;
    }

    @Override
    public String toString() {
        return label;
    }

    public Color getPanelBackground() {
        return panelBackground;
    }

    public Color getActionBackground() {
        return actionBackground;
    }

    public Color getActionForeground() {
        return actionForeground;
    }

    public Color getGroupHeaderBackground() {
        return groupHeaderBackground;
    }

    public Color getGroupHeaderForeground() {
        return groupHeaderForeground;
    }

    public Color getActionButtonBackground() {
        return actionButtonBackground;
    }

    public Color getToolBarButtonBackground() {
        return toolBarButtonBackground;
    }
}
