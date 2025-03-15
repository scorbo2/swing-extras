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
 * A quick application to show off the features contained in this library.
 *
 * @author scorbo2
 * @since 2025-03-09
 */
public class DemoApp extends JFrame {

    AudioDemoPanel audioDemoPanel = new AudioDemoPanel();

    public DemoApp() {
        super(Version.FULL_NAME + " demo");
        setSize(840,800);
        setMinimumSize(new Dimension(840,800));
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
        panels.add(new AboutDemoPanel());

        setLayout(new BorderLayout());
        JTabbedPane tabPane = new JTabbedPane();
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
