package ca.corbett.extras.logging;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for LogConsole.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class LogConsoleTest {

    public LogConsoleTest() {
    }

    @Test
    public void testGetRegisteredThemeNames_withDefaults_shouldReturnDefaults() {
        assertEquals(3, LogConsole.getInstance().getRegisteredThemeNames().size());
        LogConsoleTheme theme = LogConsoleTheme.createPaperStyledTheme();
    }

    @Test
    public void testGetRegisteredThemeNames_withCustomTheme_shouldReturnDefaultsAndCustomTheme() {
        LogConsoleTheme theme = LogConsoleTheme.createPaperStyledTheme();
        LogConsole.getInstance().registerTheme("testytest", theme, false);
        assertEquals(4, LogConsole.getInstance().getRegisteredThemeNames().size());
        LogConsole.getInstance().unregisterTheme("testytest");
    }

    @Test
    public void testSwitchTheme_whenSwitchingThemes_shouldSwitchThemes() {
        LogConsole.getInstance().switchTheme("Matrix");
        assertEquals("Matrix", LogConsole.getInstance().getCurrentThemeName());
        LogConsole.getInstance().switchTheme("Paper");
        assertEquals("Paper", LogConsole.getInstance().getCurrentThemeName());
        LogConsoleTheme theme = LogConsoleTheme.createPaperStyledTheme();
        LogConsole.getInstance().registerTheme("testytest", theme, true);
        assertEquals("testytest", LogConsole.getInstance().getCurrentThemeName());
        LogConsole.getInstance().unregisterTheme("testytest");
    }

    @Test
    public void testUnregisterTheme_withMultipleCustomThemes_shouldUnregister() {
        LogConsoleTheme theme = LogConsoleTheme.createPaperStyledTheme();
        for (int i = 0; i < 5; i++) {
            LogConsole.getInstance().registerTheme("testTheme" + i, theme);
        }
        assertEquals(8, LogConsole.getInstance().getRegisteredThemeNames().size());
        for (int i = 0; i < 5; i++) {
            LogConsole.getInstance().unregisterTheme("testTheme" + i);
        }
        assertEquals(3, LogConsole.getInstance().getRegisteredThemeNames().size());
    }

    @Test
    public void testUnregisterTheme_withDefault_shouldNotUnregisterDefault() {
        assertEquals(3, LogConsole.getInstance().getRegisteredThemeNames().size());
        LogConsole.getInstance().unregisterTheme(LogConsoleTheme.DEFAULT_STYLE_NAME);
        assertEquals(3, LogConsole.getInstance().getRegisteredThemeNames().size());
    }
}
