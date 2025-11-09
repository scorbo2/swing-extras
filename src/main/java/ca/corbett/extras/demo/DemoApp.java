package ca.corbett.extras.demo;

import ca.corbett.extensions.demo.ExtensionsOverviewPanel;
import ca.corbett.extras.Version;
import ca.corbett.extras.demo.panels.AboutDemoPanel;
import ca.corbett.extras.demo.panels.AnimationScrollDemoPanel;
import ca.corbett.extras.demo.panels.AnimationTextDemoPanel;
import ca.corbett.extras.demo.panels.AudioDemoPanel;
import ca.corbett.extras.demo.panels.DesktopDemoPanel;
import ca.corbett.extras.demo.panels.DirTreeDemoPanel;
import ca.corbett.extras.demo.panels.ImageTextUtilDemoPanel;
import ca.corbett.extras.demo.panels.ImageUtilDemoPanel;
import ca.corbett.extras.demo.panels.IntroPanel;
import ca.corbett.extras.demo.panels.LogConsolePanel;
import ca.corbett.extras.demo.panels.PanelBuilder;
import ca.corbett.extras.demo.panels.ProgressDemoPanel;
import ca.corbett.extras.demo.panels.PropertiesDemoPanel;
import ca.corbett.extras.logging.LogConsole;
import ca.corbett.extras.properties.PropertiesDialog;
import ca.corbett.forms.demo.AdvancedFormPanel;
import ca.corbett.forms.demo.BasicFormPanel;
import ca.corbett.forms.demo.CustomFormFieldPanel;
import ca.corbett.forms.demo.FormActionsPanel;
import ca.corbett.forms.demo.FormHelpPanel;
import ca.corbett.forms.demo.FormsOverviewPanel;
import ca.corbett.forms.demo.FormsRendererPanel;
import ca.corbett.forms.demo.FormsValidationPanel;
import ca.corbett.forms.demo.ListFieldPanel;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.logging.Logger;

/**
 * A built-in demo application which shows off the features and components
 * contained in this library. The jar-with-dependencies that is produced
 * from a maven build is executable, and when launched, will show this
 * demo app.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2025-03-09
 */
public class DemoApp extends JFrame {

    private static final Logger logger = Logger.getLogger(DemoApp.class.getName());
    AudioDemoPanel audioDemoPanel = new AudioDemoPanel();
    private static DemoApp instance;
    private static final Desktop desktop;

    private DefaultListModel<String> cardListModel;
    private JList<String> cardList;
    private JPanel demoPanel;

    static {
        // The current JRE may or may not give us access to this:
        desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    }

    public static DemoApp getInstance() {
        if (instance == null) {
            instance = new DemoApp();
            instance.populateDemoPanels();
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
            Image image = Toolkit.getDefaultToolkit().createImage(url);
            setIconImage(image);
            LogConsole.getInstance().setIconImage(image);
        }

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildListPanel(), buildDemoPanel());
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(195);
        add(splitPane, BorderLayout.CENTER);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            // Generating the waveform can only be done after the containing frame is packed and shown:
            SwingUtilities.invokeLater(() -> audioDemoPanel.generateWaveform());
        }
    }

    /**
     * Creates all of our demo panels and adds them to the menu on the left side.
     */
    private void populateDemoPanels() {
        addDemoPanel(new IntroPanel());
        addDemoPanel(audioDemoPanel);
        addDemoPanel(new DesktopDemoPanel());
        addDemoPanel(new DirTreeDemoPanel());
        addDemoPanel(new ImageUtilDemoPanel());
        addDemoPanel(new ImageTextUtilDemoPanel());
        addDemoPanel(new AnimationTextDemoPanel());
        addDemoPanel(new AnimationScrollDemoPanel());
        addDemoPanel(new ProgressDemoPanel());
        addDemoPanel(new PropertiesDemoPanel());
        addDemoPanel(new FormsOverviewPanel());
        addDemoPanel(new BasicFormPanel());
        addDemoPanel(new AdvancedFormPanel());
        addDemoPanel(new ListFieldPanel());
        addDemoPanel(new FormsValidationPanel());
        addDemoPanel(new FormActionsPanel());
        addDemoPanel(new FormsRendererPanel());
        addDemoPanel(new CustomFormFieldPanel());
        addDemoPanel(new FormHelpPanel());
        addDemoPanel(new ExtensionsOverviewPanel());
        addDemoPanel(new LogConsolePanel());
        addDemoPanel(new AboutDemoPanel());
        cardList.setSelectedIndex(0);
    }

    /**
     * Invoked internally to add the given demo panel to our menu.
     */
    private void addDemoPanel(PanelBuilder panel) {
        // Keep the text from being directly against the window border:
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

        // We'll use a simple JList to show the available options:
        cardListModel = new DefaultListModel<>();
        cardList = new JList<>(cardListModel);
        cardList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cardList.setFont(cardList.getFont().deriveFont(Font.PLAIN, 16f));
        listPanel.add(PropertiesDialog.buildScrollPane(cardList), BorderLayout.CENTER);

        // When an option is selected, we'll show its demo panel in the main content area:
        cardList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || cardList.getSelectedValue() == null) {
                return;
            }

            // Flip to the given card:
            ((CardLayout)demoPanel.getLayout()).show(demoPanel, cardList.getSelectedValue().trim());
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

    /**
     * If the current JRE supports browsing, this action will open the given URI
     * in the user's default browser.
     */
    public static class BrowseAction extends AbstractAction {
        private final URI uri;

        public BrowseAction(URI uri) {
            this.uri = uri;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (isBrowsingSupported()) {
                try {
                    desktop.browse(uri);
                }
                catch (IOException ioe) {
                    logger.warning("Unable to browse URI: " + ioe.getMessage());
                }
            }
        }
    }

    /**
     * Reports whether the current JRE supports browsing (needed to open hyperlinks).
     */
    public static boolean isBrowsingSupported() {
        return desktop != null && desktop.isSupported(Desktop.Action.BROWSE);
    }

    /**
     * Does a very quick check on the given String to see if it looks like a URL.
     * This doesn't guarantee that it will parse as one! This is just a very quick check.
     */
    public static boolean isUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }
}
