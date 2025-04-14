package ca.corbett.extras.demo;

import java.awt.SplashScreen;

/**
 * Entry point for the DemoApp.
 *
 * @author scorbo2
 * @since 2025-03-09
 */
public class DemoAppLauncher {
    public static void main(String[] args) {
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
        java.awt.EventQueue.invokeLater(() -> DemoApp.getInstance().setVisible(true));
    }
}
