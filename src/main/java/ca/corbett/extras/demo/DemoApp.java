package ca.corbett.extras.demo;

import ca.corbett.extensions.demo.ExtensionsOverviewPanel;
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
import ca.corbett.extras.properties.PropertiesDialog;
import ca.corbett.forms.demo.BasicFormPanel;
import ca.corbett.forms.demo.CustomFormFieldPanel;
import ca.corbett.forms.demo.FormActionsPanel;
import ca.corbett.forms.demo.FormHelpPanel;
import ca.corbett.forms.demo.FormsOverviewPanel;
import ca.corbett.forms.demo.FormsValidationPanel;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.net.URL;

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

    private DefaultListModel<String> cardListModel;
    private JList<String> cardList;
    private JPanel demoPanel;

    public static DemoApp getInstance() {
        if (instance == null) {
            instance = new DemoApp();
        }
        return instance;
    }

    private DemoApp() {
        super(Version.FULL_NAME + " demo");
        setSize(900, 660);
        setMinimumSize(new Dimension(500, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        URL url = getClass().getResource("/swing-extras/images/swing-extras-icon.jpg");
        if (url != null) {
            setIconImage(Toolkit.getDefaultToolkit().createImage(url));
        }

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildListPanel(), buildDemoPanel());
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(195);
        add(splitPane, BorderLayout.CENTER);

        addDemoPanel(new IntroPanel());
        addDemoPanel(audioDemoPanel);
        addDemoPanel(new DesktopDemoPanel());
        addDemoPanel(new DirTreeDemoPanel());
        addDemoPanel(new ImageUtilDemoPanel());
        addDemoPanel(new ImageTextUtilDemoPanel());
        addDemoPanel(new ProgressDemoPanel());
        addDemoPanel(new PropertiesDemoPanel());
        addDemoPanel(new FormsOverviewPanel());
        addDemoPanel(new BasicFormPanel());
        addDemoPanel(new FormsValidationPanel());
        addDemoPanel(new FormActionsPanel());
        addDemoPanel(new CustomFormFieldPanel());
        addDemoPanel(new FormHelpPanel());
        addDemoPanel(new ExtensionsOverviewPanel());
        addDemoPanel(new AboutDemoPanel());
        cardList.setSelectedIndex(0);
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

    private void addDemoPanel(PanelBuilder panel) {
        cardListModel.addElement("  " + panel.getTitle());
        demoPanel.add(PropertiesDialog.buildScrollPane(panel.build()), panel.getTitle());
    }

    /**
     * Invoked internally to create and return a JPanel that houses the list of
     * available cards. The list is empty at this point.
     *
     * @return A JPanel
     */
    private JPanel buildListPanel() {
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BorderLayout());

        cardListModel = new DefaultListModel<>();
        cardList = new JList<>(cardListModel);
        cardList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cardList.setFont(cardList.getFont().deriveFont(Font.PLAIN, 16f));
        listPanel.add(PropertiesDialog.buildScrollPane(cardList), BorderLayout.CENTER);

        cardList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                ((CardLayout)demoPanel.getLayout()).show(demoPanel, cardList.getSelectedValue().trim());
            }
        });

        return listPanel;
    }

    /**
     * Invoked internally to create and return the demo panel to show
     * the currently selected demo item from the list in the left.
     *
     * @return A JPanel
     */
    private JPanel buildDemoPanel() {
        demoPanel = new JPanel();
        demoPanel.setLayout(new CardLayout());
        return demoPanel;
    }
}
