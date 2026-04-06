package ca.corbett.extras.demo;

import ca.corbett.extras.FallbackExceptionHandler;
import ca.corbett.extras.LookAndFeelManager;
import com.formdev.flatlaf.FlatLightLaf;

import java.awt.EventQueue;
import java.awt.SplashScreen;

/**
 * Entry point for the DemoApp.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2025-03-09
 */
public class DemoAppLauncher {
    public static void main(String[] args) {

        // Register a FallbackExceptionHandler to catch any uncaught exceptions and log them properly:
        FallbackExceptionHandler.register();

        LookAndFeelManager.installExtraLafs();
        LookAndFeelManager.switchLaf(FlatLightLaf.class.getName());

        // Get the splash screen if there is one:
        final SplashScreen splashScreen = SplashScreen.getSplashScreen();
        if (splashScreen != null) {
            try {
                // Wait a second or so, so it doesn't just flash up and disappear immediately.
                Thread.sleep(744);
            }
            catch (InterruptedException ignored) {
                // ignored
            }
            splashScreen.close();
        }

        // Create and display the form
        EventQueue.invokeLater(() -> DemoApp.getInstance().setVisible(true));
    }
}
