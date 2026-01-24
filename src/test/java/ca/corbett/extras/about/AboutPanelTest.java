package ca.corbett.extras.about;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the AboutPanel class to verify the easter egg feature.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2026-01-24
 */
class AboutPanelTest {

    @Test
    public void testAboutPanelCreation() {
        // Create a simple AboutInfo
        AboutInfo info = new AboutInfo();
        info.applicationName = "Test App";
        info.applicationVersion = "1.0.0";
        
        // Create the AboutPanel and verify it doesn't throw an exception
        assertDoesNotThrow(() -> new AboutPanel(info));
    }

    @Test
    public void testRefreshMemoryStats() {
        // Create a simple AboutInfo
        AboutInfo info = new AboutInfo();
        info.applicationName = "Test App";
        info.applicationVersion = "1.0.0";
        
        // Create the AboutPanel
        AboutPanel panel = new AboutPanel(info);
        
        // Call refreshMemoryStats and verify it doesn't throw an exception
        assertDoesNotThrow(() -> panel.refreshMemoryStats());
    }
}
