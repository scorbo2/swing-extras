package ca.corbett.extras.actionpanel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for ColorOptions.
 */
class ColorOptionsTest {

    private ActionPanel actionPanel;
    private ColorOptions colorOptions;

    @BeforeEach
    void setUp() {
        actionPanel = new ActionPanel();
        colorOptions = actionPanel.getColorOptions();
    }

    // --- useSystemDefaults ---

    @Test
    void useSystemDefaults_shouldSetAllColorsToNull() {
        // GIVEN some colors have been set:
        colorOptions.setPanelBackground(Color.RED);
        colorOptions.setActionForeground(Color.GREEN);
        colorOptions.setActionBackground(Color.BLUE);
        colorOptions.setGroupHeaderForeground(Color.CYAN);
        colorOptions.setGroupHeaderBackground(Color.MAGENTA);
        colorOptions.setActionButtonBackground(Color.ORANGE);
        colorOptions.setToolBarButtonBackground(Color.PINK);
        colorOptions.setFlatButtonBorderColor(Color.YELLOW);

        // WHEN we call useSystemDefaults:
        colorOptions.useSystemDefaults();

        // THEN all colors should be null:
        assertNull(colorOptions.getPanelBackground(), "Panel background should be null after useSystemDefaults");
        assertNull(colorOptions.getActionForeground(), "Action foreground should be null after useSystemDefaults");
        assertNull(colorOptions.getActionBackground(), "Action background should be null after useSystemDefaults");
        assertNull(colorOptions.getGroupHeaderForeground(), "Group header foreground should be null after useSystemDefaults");
        assertNull(colorOptions.getGroupHeaderBackground(), "Group header background should be null after useSystemDefaults");
        assertNull(colorOptions.getActionButtonBackground(), "Action button background should be null after useSystemDefaults");
        assertNull(colorOptions.getToolBarButtonBackground(), "Toolbar button background should be null after useSystemDefaults");
        assertNull(colorOptions.getFlatButtonBorderColor(), "Flat button border color should be null after useSystemDefaults");
    }

    @Test
    void useSystemDefaults_isDefaultState() {
        // WHEN a new ColorOptions is created (via ActionPanel):
        // THEN all colors should be null by default:
        assertNull(colorOptions.getPanelBackground(), "Panel background should be null by default");
        assertNull(colorOptions.getActionForeground(), "Action foreground should be null by default");
        assertNull(colorOptions.getActionBackground(), "Action background should be null by default");
        assertNull(colorOptions.getGroupHeaderForeground(), "Group header foreground should be null by default");
        assertNull(colorOptions.getGroupHeaderBackground(), "Group header background should be null by default");
        assertNull(colorOptions.getActionButtonBackground(), "Action button background should be null by default");
        assertNull(colorOptions.getToolBarButtonBackground(), "Toolbar button background should be null by default");
        assertNull(colorOptions.getFlatButtonBorderColor(), "Flat button border color should be null by default");
    }

    @Test
    void useSystemDefaults_shouldReturnSameInstance_forMethodChaining() {
        // WHEN we call useSystemDefaults:
        ColorOptions result = colorOptions.useSystemDefaults();

        // THEN it should return the same instance:
        assertSame(colorOptions, result, "useSystemDefaults should return the same instance for method chaining");
    }

    // --- setFromTheme ---

    @Test
    void setFromTheme_withNull_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> colorOptions.setFromTheme(null),
                "setFromTheme(null) should throw IllegalArgumentException");
    }

    @Test
    void setFromTheme_withValidTheme_shouldApplyColors() {
        // WHEN we apply the DEFAULT theme:
        colorOptions.setFromTheme(ColorTheme.DEFAULT);

        // THEN the colors should match the theme:
        assertEquals(ColorTheme.DEFAULT.getPanelBackground(), colorOptions.getPanelBackground(),
                "Panel background should match theme");
        assertEquals(ColorTheme.DEFAULT.getActionForeground(), colorOptions.getActionForeground(),
                "Action foreground should match theme");
        assertEquals(ColorTheme.DEFAULT.getActionBackground(), colorOptions.getActionBackground(),
                "Action background should match theme");
        assertEquals(ColorTheme.DEFAULT.getGroupHeaderForeground(), colorOptions.getGroupHeaderForeground(),
                "Group header foreground should match theme");
        assertEquals(ColorTheme.DEFAULT.getGroupHeaderBackground(), colorOptions.getGroupHeaderBackground(),
                "Group header background should match theme");
        assertEquals(ColorTheme.DEFAULT.getActionButtonBackground(), colorOptions.getActionButtonBackground(),
                "Action button background should match theme");
        assertEquals(ColorTheme.DEFAULT.getToolBarButtonBackground(), colorOptions.getToolBarButtonBackground(),
                "Toolbar button background should match theme");
        assertEquals(ColorTheme.DEFAULT.getFlatButtonBorderColor(), colorOptions.getFlatButtonBorderColor(),
                "Flat button border color should match theme");
    }

    @Test
    void setFromTheme_shouldReturnSameInstance_forMethodChaining() {
        // WHEN we apply a theme:
        ColorOptions result = colorOptions.setFromTheme(ColorTheme.DARK);

        // THEN it should return the same instance:
        assertSame(colorOptions, result, "setFromTheme should return the same instance for method chaining");
    }

    // --- setPanelBackground / getPanelBackground ---

    @Test
    void setPanelBackground_shouldStoreColor() {
        colorOptions.setPanelBackground(Color.RED);
        assertEquals(Color.RED, colorOptions.getPanelBackground(), "Panel background should be RED");
    }

    @Test
    void setPanelBackground_withNull_shouldClearColor() {
        colorOptions.setPanelBackground(Color.RED);
        colorOptions.setPanelBackground(null);
        assertNull(colorOptions.getPanelBackground(), "Panel background should be null after setting null");
    }

    @Test
    void setPanelBackground_shouldReturnSameInstance_forMethodChaining() {
        assertSame(colorOptions, colorOptions.setPanelBackground(Color.RED),
                "setPanelBackground should return the same instance for method chaining");
    }

    // --- setActionForeground / getActionForeground ---

    @Test
    void setActionForeground_shouldStoreColor() {
        colorOptions.setActionForeground(Color.WHITE);
        assertEquals(Color.WHITE, colorOptions.getActionForeground(), "Action foreground should be WHITE");
    }

    @Test
    void setActionForeground_withNull_shouldClearColor() {
        colorOptions.setActionForeground(Color.WHITE);
        colorOptions.setActionForeground(null);
        assertNull(colorOptions.getActionForeground(), "Action foreground should be null after setting null");
    }

    @Test
    void setActionForeground_shouldReturnSameInstance_forMethodChaining() {
        assertSame(colorOptions, colorOptions.setActionForeground(Color.WHITE),
                "setActionForeground should return the same instance for method chaining");
    }

    // --- setActionBackground / getActionBackground ---

    @Test
    void setActionBackground_shouldStoreColor() {
        colorOptions.setActionBackground(Color.BLUE);
        assertEquals(Color.BLUE, colorOptions.getActionBackground(), "Action background should be BLUE");
    }

    @Test
    void setActionBackground_withNull_shouldClearColor() {
        colorOptions.setActionBackground(Color.BLUE);
        colorOptions.setActionBackground(null);
        assertNull(colorOptions.getActionBackground(), "Action background should be null after setting null");
    }

    @Test
    void setActionBackground_shouldReturnSameInstance_forMethodChaining() {
        assertSame(colorOptions, colorOptions.setActionBackground(Color.BLUE),
                "setActionBackground should return the same instance for method chaining");
    }

    // --- setActionButtonBackground / getActionButtonBackground ---

    @Test
    void setActionButtonBackground_shouldStoreColor() {
        colorOptions.setActionButtonBackground(Color.GREEN);
        assertEquals(Color.GREEN, colorOptions.getActionButtonBackground(), "Action button background should be GREEN");
    }

    @Test
    void setActionButtonBackground_withNull_shouldClearColor() {
        colorOptions.setActionButtonBackground(Color.GREEN);
        colorOptions.setActionButtonBackground(null);
        assertNull(colorOptions.getActionButtonBackground(), "Action button background should be null after setting null");
    }

    @Test
    void setActionButtonBackground_shouldReturnSameInstance_forMethodChaining() {
        assertSame(colorOptions, colorOptions.setActionButtonBackground(Color.GREEN),
                "setActionButtonBackground should return the same instance for method chaining");
    }

    // --- setGroupHeaderForeground / getGroupHeaderForeground ---

    @Test
    void setGroupHeaderForeground_shouldStoreColor() {
        colorOptions.setGroupHeaderForeground(Color.YELLOW);
        assertEquals(Color.YELLOW, colorOptions.getGroupHeaderForeground(), "Group header foreground should be YELLOW");
    }

    @Test
    void setGroupHeaderForeground_withNull_shouldClearColor() {
        colorOptions.setGroupHeaderForeground(Color.YELLOW);
        colorOptions.setGroupHeaderForeground(null);
        assertNull(colorOptions.getGroupHeaderForeground(), "Group header foreground should be null after setting null");
    }

    @Test
    void setGroupHeaderForeground_shouldReturnSameInstance_forMethodChaining() {
        assertSame(colorOptions, colorOptions.setGroupHeaderForeground(Color.YELLOW),
                "setGroupHeaderForeground should return the same instance for method chaining");
    }

    // --- setGroupHeaderBackground / getGroupHeaderBackground ---

    @Test
    void setGroupHeaderBackground_shouldStoreColor() {
        colorOptions.setGroupHeaderBackground(Color.CYAN);
        assertEquals(Color.CYAN, colorOptions.getGroupHeaderBackground(), "Group header background should be CYAN");
    }

    @Test
    void setGroupHeaderBackground_withNull_shouldClearColor() {
        colorOptions.setGroupHeaderBackground(Color.CYAN);
        colorOptions.setGroupHeaderBackground(null);
        assertNull(colorOptions.getGroupHeaderBackground(), "Group header background should be null after setting null");
    }

    @Test
    void setGroupHeaderBackground_shouldReturnSameInstance_forMethodChaining() {
        assertSame(colorOptions, colorOptions.setGroupHeaderBackground(Color.CYAN),
                "setGroupHeaderBackground should return the same instance for method chaining");
    }

    // --- setToolBarButtonBackground / getToolBarButtonBackground ---

    @Test
    void setToolBarButtonBackground_shouldStoreColor() {
        colorOptions.setToolBarButtonBackground(Color.ORANGE);
        assertEquals(Color.ORANGE, colorOptions.getToolBarButtonBackground(), "Toolbar button background should be ORANGE");
    }

    @Test
    void setToolBarButtonBackground_withNull_shouldClearColor() {
        colorOptions.setToolBarButtonBackground(Color.ORANGE);
        colorOptions.setToolBarButtonBackground(null);
        assertNull(colorOptions.getToolBarButtonBackground(), "Toolbar button background should be null after setting null");
    }

    @Test
    void setToolBarButtonBackground_shouldReturnSameInstance_forMethodChaining() {
        assertSame(colorOptions, colorOptions.setToolBarButtonBackground(Color.ORANGE),
                "setToolBarButtonBackground should return the same instance for method chaining");
    }

    // --- setToolBarButtonsTransparent ---

    @Test
    void setToolBarButtonsTransparent_shouldSetTransparentColor() {
        // WHEN we call setToolBarButtonsTransparent:
        colorOptions.setToolBarButtonsTransparent();

        // THEN the toolbar button background should be transparent (alpha = 0):
        Color transparentColor = colorOptions.getToolBarButtonBackground();
        assertNotNull(transparentColor, "Toolbar button background should not be null after setToolBarButtonsTransparent");
        assertEquals(0, transparentColor.getAlpha(), "Toolbar button background should be fully transparent (alpha = 0)");
    }

    @Test
    void setToolBarButtonsTransparent_shouldReturnSameInstance_forMethodChaining() {
        assertSame(colorOptions, colorOptions.setToolBarButtonsTransparent(),
                "setToolBarButtonsTransparent should return the same instance for method chaining");
    }

    // --- setFlatButtonBorderColor / getFlatButtonBorderColor ---

    @Test
    void setFlatButtonBorderColor_shouldStoreColor() {
        colorOptions.setFlatButtonBorderColor(Color.YELLOW);
        assertEquals(Color.YELLOW, colorOptions.getFlatButtonBorderColor(), "Flat button border color should be YELLOW");
    }

    @Test
    void setFlatButtonBorderColor_withNull_shouldClearColor() {
        colorOptions.setFlatButtonBorderColor(Color.YELLOW);
        colorOptions.setFlatButtonBorderColor(null);
        assertNull(colorOptions.getFlatButtonBorderColor(), "Flat button border color should be null after setting null");
    }

    @Test
    void setFlatButtonBorderColor_shouldReturnSameInstance_forMethodChaining() {
        assertSame(colorOptions, colorOptions.setFlatButtonBorderColor(Color.YELLOW),
                "setFlatButtonBorderColor should return the same instance for method chaining");
    }

    @Test
    void setFlatButtonBorderColor_shouldFireOptionsChanged() {
        // GIVEN a listener is registered:
        final int[] count = {0};
        colorOptions.addListener(() -> count[0]++);

        // WHEN we set the flat button border color:
        colorOptions.setFlatButtonBorderColor(Color.YELLOW);

        // THEN the listener should be notified exactly once:
        assertEquals(1, count[0], "Options listener should be notified exactly once when flat button border color changes");
    }

    // --- getHighlightColor (static) ---

    @Test
    void getHighlightColor_withLightColor_shouldReturnDarkerColor() {
        // GIVEN a light color where all components are above RGB_THRESHOLD:
        Color lightColor = new Color(ColorOptions.RGB_THRESHOLD + 10, ColorOptions.RGB_THRESHOLD + 10, ColorOptions.RGB_THRESHOLD + 10);

        // WHEN we get the highlight color:
        Color highlight = ColorOptions.getHighlightColor(lightColor);

        // THEN the highlight should be darker:
        assertTrue(highlight.getRed() < lightColor.getRed(), "Red component should be darker");
        assertTrue(highlight.getGreen() < lightColor.getGreen(), "Green component should be darker");
        assertTrue(highlight.getBlue() < lightColor.getBlue(), "Blue component should be darker");
    }

    @Test
    void getHighlightColor_withDarkColor_shouldReturnLighterColor() {
        // GIVEN a dark color:
        Color darkColor = new Color(50, 50, 50);

        // WHEN we get the highlight color:
        Color highlight = ColorOptions.getHighlightColor(darkColor);

        // THEN the highlight should be lighter:
        assertTrue(highlight.getRed() > darkColor.getRed(), "Red component should be lighter");
        assertTrue(highlight.getGreen() > darkColor.getGreen(), "Green component should be lighter");
        assertTrue(highlight.getBlue() > darkColor.getBlue(), "Blue component should be lighter");
    }

    @Test
    void getHighlightColor_shouldPreserveAlpha() {
        // GIVEN a color with a specific alpha:
        Color colorWithAlpha = new Color(100, 100, 100, 128);

        // WHEN we get the highlight color:
        Color highlight = ColorOptions.getHighlightColor(colorWithAlpha);

        // THEN the alpha should be preserved:
        assertEquals(128, highlight.getAlpha(), "Alpha should be preserved");
    }

    @Test
    void getHighlightColor_withLightColor_shouldNotGoBelowZero() {
        // GIVEN a light color just above the RGB_THRESHOLD (all components > RGB_THRESHOLD):
        Color veryLightColor = new Color(ColorOptions.RGB_THRESHOLD + 15, ColorOptions.RGB_THRESHOLD + 15, ColorOptions.RGB_THRESHOLD + 15);

        // WHEN we get the highlight color:
        Color highlight = ColorOptions.getHighlightColor(veryLightColor);

        // THEN the components should be >= 0:
        assertTrue(highlight.getRed() >= 0, "Red component should be >= 0");
        assertTrue(highlight.getGreen() >= 0, "Green component should be >= 0");
        assertTrue(highlight.getBlue() >= 0, "Blue component should be >= 0");
    }

    @Test
    void getHighlightColor_withDarkColor_shouldNotGoAbove255() {
        // GIVEN a color that goes through the lightening branch (not all components > RGB_THRESHOLD)
        // and has components high enough that lightening would exceed 255 without clamping:
        Color darkHighColor = new Color(230, ColorOptions.RGB_THRESHOLD - 10, 240);

        // WHEN we get the highlight color:
        Color highlight = ColorOptions.getHighlightColor(darkHighColor);

        // THEN components that would exceed 255 must be clamped to 255:
        assertEquals(255, highlight.getRed(), "Red component should be clamped to 255");
        assertTrue(highlight.getGreen() <= 255, "Green component should be <= 255");
        assertEquals(255, highlight.getBlue(), "Blue component should be clamped to 255");
    }

    // --- Options listener (from ActionPanelOptions base class) ---

    @Test
    void addListener_withNull_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> colorOptions.addListener(null),
                "addListener(null) should throw IllegalArgumentException");
    }

    @Test
    void removeListener_withNull_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> colorOptions.removeListener(null),
                "removeListener(null) should throw IllegalArgumentException");
    }

    @Test
    void setPanelBackground_shouldFireOptionsChanged() {
        // GIVEN a listener is registered:
        final int[] count = {0};
        colorOptions.addListener(() -> count[0]++);

        // WHEN we set the panel background:
        colorOptions.setPanelBackground(Color.RED);

        // THEN the listener should be notified exactly once:
        assertEquals(1, count[0], "Options listener should be notified exactly once when panel background changes");
    }

    @Test
    void setFromTheme_shouldFireOptionsChanged() {
        // GIVEN a listener is registered:
        final int[] count = {0};
        colorOptions.addListener(() -> count[0]++);

        // WHEN we apply a theme:
        colorOptions.setFromTheme(ColorTheme.DARK);

        // THEN the listener should be notified exactly once:
        assertEquals(1, count[0], "Options listener should be notified exactly once when theme is applied");
    }

    @Test
    void useSystemDefaults_shouldFireOptionsChanged() {
        // GIVEN a listener is registered:
        final int[] count = {0};
        colorOptions.addListener(() -> count[0]++);

        // WHEN we call useSystemDefaults:
        colorOptions.useSystemDefaults();

        // THEN the listener should be notified exactly once:
        assertEquals(1, count[0], "Options listener should be notified exactly once when useSystemDefaults is called");
    }
}
