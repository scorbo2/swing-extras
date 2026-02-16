package ca.corbett.extras.demo;

import ca.corbett.extensions.demo.ExtensionsOverviewPanel;
import ca.corbett.extras.Version;
import ca.corbett.extras.actionpanel.ActionPanel;
import ca.corbett.extras.demo.panels.AboutDemoPanel;
import ca.corbett.extras.demo.panels.ActionPanelDemoPanel;
import ca.corbett.extras.demo.panels.AnimationPanelEffectsPanel;
import ca.corbett.extras.demo.panels.AnimationScrollDemoPanel;
import ca.corbett.extras.demo.panels.AnimationTextDemoPanel;
import ca.corbett.extras.demo.panels.AudioDemoPanel;
import ca.corbett.extras.demo.panels.DesktopDemoPanel;
import ca.corbett.extras.demo.panels.DirTreeDemoPanel;
import ca.corbett.extras.demo.panels.ImageTextUtilDemoPanel;
import ca.corbett.extras.demo.panels.ImageUtilDemoPanel;
import ca.corbett.extras.demo.panels.IntroPanel;
import ca.corbett.extras.demo.panels.KeyStrokeManagerPanel;
import ca.corbett.extras.demo.panels.LogConsolePanel;
import ca.corbett.extras.demo.panels.PanelBuilder;
import ca.corbett.extras.demo.panels.ProgressDemoPanel;
import ca.corbett.extras.demo.panels.PropertiesDemoPanel;
import ca.corbett.extras.demo.panels.TextInputDialogPanel;
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

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

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

    AudioDemoPanel audioDemoPanel = new AudioDemoPanel();
    private static DemoApp instance;
    private final ActionPanel actionPanel;
    private final JPanel demoPanel;

    private static final String INTRO = "Intro";
    private static final String UI = "UI components";
    private static final String IMAGES = "Image handling";
    private static final String ANIMATION = "Animation";
    private static final String EXTENSIONS = "Extensions";
    private static final String FORMS = "Forms";
    private static final String MISC = "Misc. utilities";

    public static DemoApp getInstance() {
        if (instance == null) {
            instance = new DemoApp();
            instance.populateDemoPanels();
        }
        return instance;
    }

    private DemoApp() {
        super(Version.FULL_NAME + " demo");
        setSize(990, 680);
        setMinimumSize(new Dimension(500, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        URL url = getClass().getResource("/swing-extras/images/swing-extras-icon.jpg");
        if (url != null) {
            Image image = Toolkit.getDefaultToolkit().createImage(url);
            setIconImage(image);
            LogConsole.getInstance().setIconImage(image);
        }

        // Create our card layout and our action panel, and link them together:
        demoPanel = new JPanel();
        demoPanel.setLayout(new CardLayout());
        actionPanel = new ActionPanel();
        actionPanel.setCardContainer(demoPanel);
        actionPanel.getActionTrayMargins().setLeft(12); // Indent a little bit
        actionPanel.getActionTrayMargins().setBottom(6); // Add a little vertical padding
        actionPanel.getBorderOptions().setGroupBorder(BorderFactory.createLoweredBevelBorder());
        actionPanel.getExpandCollapseOptions().setAllowHeaderDoubleClick(true);

        // Make our action panel scrollable:
        JScrollPane actionPanelScrollPane = PropertiesDialog.buildScrollPane(actionPanel);

        // Now add it all together in a split pane::
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, actionPanelScrollPane, demoPanel);
        splitPane.setOneTouchExpandable(false); // sadly, this does not play well with certain Look and Feels
        splitPane.setDividerLocation(205);
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
        // We just want to build once at the end, as opposed
        // to rebuilding every time we add a panel:
        actionPanel.setAutoRebuildEnabled(false);
        try {
            addDemoPanel(INTRO, new IntroPanel());
            addDemoPanel(INTRO, new AboutDemoPanel());
            addDemoPanel(UI, new ActionPanelDemoPanel());
            addDemoPanel(UI, audioDemoPanel);
            addDemoPanel(UI, new DesktopDemoPanel());
            addDemoPanel(UI, new DirTreeDemoPanel());
            addDemoPanel(UI, new ProgressDemoPanel());
            addDemoPanel(UI, new TextInputDialogPanel());
            addDemoPanel(FORMS, new FormsOverviewPanel());
            addDemoPanel(FORMS, new BasicFormPanel());
            addDemoPanel(FORMS, new AdvancedFormPanel());
            addDemoPanel(FORMS, new ListFieldPanel());
            addDemoPanel(FORMS, new FormsValidationPanel());
            addDemoPanel(FORMS, new FormActionsPanel());
            addDemoPanel(FORMS, new FormsRendererPanel());
            addDemoPanel(FORMS, new CustomFormFieldPanel());
            addDemoPanel(FORMS, new FormHelpPanel());
            addDemoPanel(ANIMATION, new AnimationTextDemoPanel());
            addDemoPanel(ANIMATION, new AnimationPanelEffectsPanel()); // These are just for fun
            addDemoPanel(ANIMATION, new AnimationScrollDemoPanel()); // Not really a "swing extra", but okay
            addDemoPanel(IMAGES, new ImageUtilDemoPanel());
            addDemoPanel(IMAGES, new ImageTextUtilDemoPanel());
            addDemoPanel(EXTENSIONS, new ExtensionsOverviewPanel());
            addDemoPanel(EXTENSIONS, new PropertiesDemoPanel());
            addDemoPanel(MISC, new KeyStrokeManagerPanel());
            addDemoPanel(MISC, new LogConsolePanel());

            // Collapse all groups but the first by default, otherwise it looks too busy:
            actionPanel.setExpanded(UI, false);
            actionPanel.setExpanded(FORMS, false);
            actionPanel.setExpanded(ANIMATION, false);
            actionPanel.setExpanded(IMAGES, false);
            actionPanel.setExpanded(EXTENSIONS, false);
            actionPanel.setExpanded(MISC, false);
        }

        finally {
            // Re-enabling auto-rebuild will force an immediate rebuild:
            actionPanel.setAutoRebuildEnabled(true);
        }

        // Force the first card to show:
        ((CardLayout)demoPanel.getLayout()).show(demoPanel, INTRO);
    }

    /**
     * Invoked internally to add the given demo panel to our menu.
     */
    private void addDemoPanel(String group, PanelBuilder panel) {
        actionPanel.add(group, panel.getTitle(), panel.getTitle());
        demoPanel.add(PropertiesDialog.buildScrollPane(panel.build()), panel.getTitle());
    }
}
