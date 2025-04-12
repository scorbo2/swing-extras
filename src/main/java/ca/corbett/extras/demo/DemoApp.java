package ca.corbett.extras.demo;

import ca.corbett.extras.Version;
import ca.corbett.extras.demo.panels.AboutDemoPanel;
import ca.corbett.extras.demo.panels.AudioDemoPanel;
import ca.corbett.extras.demo.panels.DesktopDemoPanel;
import ca.corbett.extras.demo.panels.DirTreeDemoPanel;
import ca.corbett.extras.demo.panels.ImageTextUtilDemoPanel;
import ca.corbett.extras.demo.panels.ImageUtilDemoPanel;
import ca.corbett.extras.demo.panels.IntroPanel;
import ca.corbett.extras.demo.panels.PanelBuilder;
import ca.corbett.extras.demo.panels.ProgressDemoPanel;
import ca.corbett.extras.demo.panels.PropertiesDemoPanel;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A built-in demo application which shows off the features and components
 * contained in this library. The jar-with-dependencies that is produced
 * from a maven build is executable, and when launched, will show this
 * demo app.
 *
 * @author scorbo2
 * @since 2025-03-09
 */
public class DemoApp extends JFrame {

    AudioDemoPanel audioDemoPanel = new AudioDemoPanel();
    private static DemoApp instance;

    public static DemoApp getInstance() {
        if (instance == null) {
            instance = new DemoApp();
        }
        return instance;
    }

    private DemoApp() {
        super(Version.FULL_NAME + " demo");
        setSize(900, 800);
        setMinimumSize(new Dimension(800, 800));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        URL url = getClass().getResource("/swing-extras/images/swing-extras-icon.jpg");
        if (url != null) {
            setIconImage(Toolkit.getDefaultToolkit().createImage(url));
        }

        final List<PanelBuilder> panels = new ArrayList<>();
        panels.add(new IntroPanel());
        panels.add(audioDemoPanel);
        panels.add(new DesktopDemoPanel());
        panels.add(new DirTreeDemoPanel());
        panels.add(new ImageUtilDemoPanel());
        panels.add(new ImageTextUtilDemoPanel());
        panels.add(new ProgressDemoPanel());
        panels.add(new PropertiesDemoPanel());
        panels.add(new AboutDemoPanel());

        setLayout(new BorderLayout());
        JTabbedPane tabPane = new JTabbedPane();
        tabPane.setTabPlacement(JTabbedPane.);
        for (PanelBuilder builder : panels) {
            tabPane.addTab(builder.getTitle(), builder.build());
        }
        add(tabPane, BorderLayout.CENTER);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    audioDemoPanel.generateWaveform(); // has to be done after frame is packed and shown.
                }
            });
        }
    }
}
