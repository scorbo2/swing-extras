package ca.corbett.forms.demo;

import ca.corbett.forms.Version;
import ca.corbett.forms.demo.panels.AboutPanel;
import ca.corbett.forms.demo.panels.BasicFormPanel;
import ca.corbett.forms.demo.panels.CustomFieldPanel;
import ca.corbett.forms.demo.panels.FormActionsPanel;
import ca.corbett.forms.demo.panels.HelpPanel;
import ca.corbett.forms.demo.panels.IntroPanel;
import ca.corbett.forms.demo.panels.PanelBuilder;
import ca.corbett.forms.demo.panels.ValidationPanel;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A JFrame for showing off the swing-forms capabilities.
 * Refer to the various demo panels in the panels subpackage
 * for the more interesting code.
 *
 * @author scorbo2
 * @since 2019-11-25
 */
public final class DemoFrame extends JFrame {

    public DemoFrame() {
        super(Version.FULL_NAME + " demo");
        setSize(640, 600);
        setMinimumSize(new Dimension(640, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initComponents();
    }

    private void initComponents() {
        final List<PanelBuilder> panels = new ArrayList<>();
        panels.add(new IntroPanel());
        panels.add(new BasicFormPanel());
        panels.add(new ValidationPanel());
        panels.add(new FormActionsPanel());
        panels.add(new CustomFieldPanel());
        panels.add(new HelpPanel());
        panels.add(new AboutPanel());

        setLayout(new BorderLayout());
        JTabbedPane tabPane = new JTabbedPane();
        for (PanelBuilder builder : panels) {
            tabPane.addTab(builder.getTitle(), builder.build());
        }
        add(tabPane, BorderLayout.CENTER);

        URL url = getClass().getResource("/ca/corbett/swing-forms/images/swing-forms-icon.jpg");
        if (url != null) {
            setIconImage(Toolkit.getDefaultToolkit().createImage(url));
        }
    }
}
