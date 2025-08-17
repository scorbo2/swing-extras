package ca.corbett.extras.logging;

import ca.corbett.extras.properties.FileBasedProperties;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for LogConsoleTheme.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class LogConsoleThemeTest {

    public LogConsoleThemeTest() {
    }

    @Test
    public void testClear() {
        LogConsoleTheme theme = new LogConsoleTheme();
        theme.setDefaultBgColor(Color.yellow);
        theme.setStyle("blah", new LogConsoleStyleProperty("test"));
        assertEquals(Color.yellow, theme.getDefaultBgColor());
        assertEquals(2, theme.getStyleNames().size());
        theme.clear();
        assertEquals(Color.WHITE, theme.getDefaultBgColor());
        assertEquals(1, theme.getStyleNames().size());// default style still present after reset
    }

    @Test
    public void testThemeSaveAndLoad_withCustomStyle_shouldSaveAndRestore() throws IOException {
        LogConsoleTheme theme = LogConsoleTheme.createDefaultStyledTheme();
        theme.setStyle("testStyle", createTestStyle());
        File tmpFile = File.createTempFile("test", "theme");
        tmpFile.deleteOnExit();
        FileBasedProperties test = new FileBasedProperties(tmpFile);
        test.setEagerSave(true);
        theme.saveToProps(test, "defaultTheme.");

        LogConsoleTheme restoredTheme = new LogConsoleTheme();
        restoredTheme.loadFromProps(test, "defaultTheme.");
        assertTrue(areStylesEqual(theme.getStyle("testStyle"), restoredTheme.getStyle("testStyle")));
    }

    @Test
    public void testSetFontPointSize_withVariousSizes_shouldSucceed() {
        LogConsoleTheme instance = LogConsoleTheme.createMatrixStyledTheme();
        LogConsoleStyleProperty errorStyle = instance.getStyle("Errors");
        assertNotNull(errorStyle);
        assertEquals(12, errorStyle.getFontPointSize());
        instance.setFontPointSize(16);
        assertEquals(16, errorStyle.getFontPointSize());
    }

    @Test
    public void testGetStyleNames_withDefaults_shouldSucceed() {
        LogConsoleTheme theme = LogConsoleTheme.createPaperStyledTheme();
        List<String> styleNames = theme.getStyleNames();
        assertNotNull(styleNames);
        assertEquals(3, styleNames.size());
        assertEquals("Default", styleNames.get(0));
    }

    @Test
    public void testRemoveStyle_withValidAndInvalidDeletes_shouldSucceed() {
        LogConsoleTheme theme = LogConsoleTheme.createDefaultStyledTheme();
        assertEquals(3, theme.getStyleNames().size());
        theme.removeStyle("Errors");
        assertEquals(2, theme.getStyleNames().size());
        theme.removeStyle("This one doesnt exist");
        assertEquals(2, theme.getStyleNames().size());
        theme.removeStyle("Warnings");
        assertEquals(1, theme.getStyleNames().size());
    }

    @Test
    public void testGetMatchingStyle_withVariousConditions_shouldMatchCorrectly() {
        LogConsoleStyleProperty defaultStyle = new LogConsoleStyleProperty("test");

        LogConsoleStyleProperty errorStyle = new LogConsoleStyleProperty("test-error");
        errorStyle.setLogLevel(Level.SEVERE);
        errorStyle.setFontColor(Color.RED);

        LogConsoleStyleProperty infoStyle = new LogConsoleStyleProperty("test-info");
        infoStyle.setLogLevel(Level.INFO);
        infoStyle.setFontColor(Color.GREEN);

        LogConsoleStyleProperty warningStyle = new LogConsoleStyleProperty("test-warning");
        warningStyle.setLogLevel(Level.WARNING);
        warningStyle.setFontColor(Color.YELLOW);

        LogConsoleStyleProperty customStyle1 = new LogConsoleStyleProperty("test-foobar");
        customStyle1.setLogToken("foobar", true);

        LogConsoleStyleProperty customStyle2 = new LogConsoleStyleProperty("test-barfoo");
        customStyle2.setLogLevel(Level.INFO);
        customStyle2.setLogToken("barfoo", true);

        LogConsoleTheme theme = LogConsoleTheme.createPlainTheme();
        theme.setStyle(LogConsoleTheme.DEFAULT_STYLE_NAME, defaultStyle);

        // Sanity check, make sure the theme is using the default style we just set:
        assertEquals(defaultStyle, theme.getStyle(LogConsoleTheme.DEFAULT_STYLE_NAME));

        theme.setStyle("INFO", infoStyle);
        theme.setStyle("SEVERE", errorStyle);
        theme.setStyle("WARNING", warningStyle);
        theme.setStyle("custom1", customStyle1);
        theme.setStyle("custom2", customStyle2);

        // The easy checks... no matchers found or a single matcher found:
        assertEquals(defaultStyle, theme.getMatchingStyle("blah", Level.FINE));
        assertEquals(infoStyle, theme.getMatchingStyle("blah", Level.INFO));
        assertEquals(errorStyle, theme.getMatchingStyle("blah", Level.SEVERE));
        assertEquals(warningStyle, theme.getMatchingStyle("blah", Level.WARNING));

        // The tricky checks... two matchers found, should return right one:
        assertEquals(customStyle1, theme.getMatchingStyle("foobar", Level.INFO));
        assertEquals(customStyle2, theme.getMatchingStyle("barfoo", Level.INFO));

        // Really tricky... three matchers found, tiebreaker should be alphabetical name:
        assertEquals(customStyle1, theme.getMatchingStyle("foobar barfoo", Level.INFO));
    }

    private LogConsoleStyleProperty createTestStyle() {
        LogConsoleStyleProperty style = new LogConsoleStyleProperty("test");
        style.setIsBold(true);
        style.setFontColor(Color.BLUE);
        style.setLogToken("test", true);
        style.setLogLevel(Level.FINE);
        style.setFontBgColor(Color.YELLOW);
        style.setFontPointSize(14);
        style.setFontFamilyName("SansSerif");
        style.setIsItalic(true);
        style.setIsUnderline(true);
        return style;
    }

    public boolean areStylesEqual(LogConsoleStyleProperty style1, LogConsoleStyleProperty style2) {
        boolean allEqual = (style1.isBold() == style2.isBold());
        allEqual = allEqual && (style1.getFontColor().equals(style2.getFontColor()));
        allEqual = allEqual && (style1.getLogToken().equals(style2.getLogToken()));
        allEqual = allEqual && (style1.getLogLevel().equals(style2.getLogLevel()));
        allEqual = allEqual && (style1.getFontBgColor().equals(style2.getFontBgColor()));
        allEqual = allEqual && (style1.getFontPointSize() == style2.getFontPointSize());
        allEqual = allEqual && (style1.getFontFamilyName().equals(style2.getFontFamilyName()));
        allEqual = allEqual && (style1.isItalic() == style2.isItalic());
        allEqual = allEqual && (style1.isUnderline() == style2.isUnderline());
        return allEqual;
    }
}
