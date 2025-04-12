package ca.corbett.forms.demo;

import java.awt.SplashScreen;

/**
 * Provides an entry point to the swing-forms demo application.
 *
 * @author scorbo2
 * @since 2029-11-25
 */
public class DemoLauncher {

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
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new DemoFrame().setVisible(true);
            }
        });
    }
}
