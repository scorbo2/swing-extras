package ca.corbett.extras.demo;

import ca.corbett.extras.Version;
import ca.corbett.extras.about.AboutInfo;
import ca.corbett.extras.about.AboutPanel;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.net.URL;

/**
 * A quick application to show off the features contained in this library.
 *
 * @author scorbo2
 * @since 2025-03-09
 */
public class DemoApp extends JFrame {

    private final AboutInfo aboutInfo;

    public DemoApp() {
        super(Version.FULL_NAME + " demo");
        setSize(840,800);
        setMinimumSize(new Dimension(840,800));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        aboutInfo = new AboutInfo();
        aboutInfo.applicationName = Version.NAME + " demo app";
        aboutInfo.applicationVersion = Version.VERSION;
        aboutInfo.logoImageLocation = "/swing-extras/images/swing-extras-logo.jpg";

        URL url = getClass().getResource("/swing-extras/images/swing-extras-icon.jpg");
        if (url != null) {
            setIconImage(Toolkit.getDefaultToolkit().createImage(url));
        }

        setLayout(new BorderLayout());
        add(new AboutPanel(aboutInfo), BorderLayout.CENTER);
    }
}
