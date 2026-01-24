package ca.corbett.extras.about;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.Version;
import ca.corbett.extras.image.ImagePanel;
import ca.corbett.extras.image.ImagePanelConfig;
import ca.corbett.extras.image.ImageUtil;
import ca.corbett.extras.image.LogoGenerator;
import ca.corbett.extras.image.LogoProperty;
import ca.corbett.extras.io.FileSystemUtil;
import ca.corbett.extras.io.HyperlinkUtil;
import ca.corbett.extras.logging.LogConsole;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.HtmlLabelField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.PanelField;
import ca.corbett.updates.VersionManifest;
import ca.corbett.updates.VersionStringComparator;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A re-usable JPanel that can display information about a given application.
 * This is used by AboutDialog, but can be used also by applications that simply want
 * to embed the AboutPanel directly somewhere other than a popup dialog.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2018-02-14 (generified from existing code)
 */
public final class AboutPanel extends JPanel {

    private static final Logger logger = Logger.getLogger(AboutPanel.class.getName());
    private LabelField memoryUsageField;
    private final Map<String, LabelField> customFields;

    public AboutPanel(AboutInfo info) {
        this(info, Alignment.TOP_CENTER, 24);
    }

    /**
     * Creates a new AboutPanel with the given AboutInfo object.
     *
     * @param info The AboutInfo object to display.
     */
    public AboutPanel(AboutInfo info, Alignment alignment, int leftMargin) {
        super();

        customFields = new HashMap<>();
        info.registerAboutPanel(this);
        setLayout(new BorderLayout());
        FormPanel formPanel = new FormPanel(alignment);
        formPanel.setBorderMargin(leftMargin);

        BufferedImage logoImage = getLogoImage(info);
        PanelField logoPanel = new PanelField();
        logoPanel.setShouldExpand(true);

        if (info.logoDisplayMode != AboutInfo.LogoDisplayMode.STRETCH) {
            logoPanel.getPanel().setLayout(
                    new FlowLayout(formPanel.getAlignment().isLeftAligned() ? FlowLayout.LEFT : FlowLayout.CENTER));

            if (info.logoDisplayMode == AboutInfo.LogoDisplayMode.AS_IS_WITH_BORDER) {
                logoPanel.getPanel().setBorder(new LineBorder(Color.BLACK, 1));
                logoPanel.getPanel().setBackground(LookAndFeelManager.getLafColor("Panel.background", Color.DARK_GRAY));
            }

            ImageIcon icon = new ImageIcon(logoImage);
            JLabel imageLabel = new JLabel(icon);
            imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            logoPanel.getPanel().add(imageLabel);

            if (info.showLogConsole) {
                imageLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                imageLabel.addMouseListener(new LogoImageMouseListener(this));
            }
        }
        else {
            logoPanel.getMargins().setAll(8);
            logoPanel.getMargins().setInternalSpacing(0);
            logoPanel.getPanel().setLayout(new BorderLayout());
            ImagePanelConfig ipc = ImagePanelConfig.createDefaultProperties();
            ipc.setDisplayMode(ImagePanelConfig.DisplayMode.STRETCH);
            ipc.setEnableMouseDragging(false);
            ipc.setEnableZoomOnMouseClick(false);
            ipc.setEnableZoomOnMouseWheel(false);
            ImagePanel imgPanel = new ImagePanel(logoImage, ipc);
            imgPanel.setPreferredSize(new Dimension(200, 80));
            logoPanel.getPanel().add(imgPanel, BorderLayout.CENTER);

            if (info.showLogConsole) {
                imgPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                imgPanel.addMouseListener(new LogoImageMouseListener(this));
            }
        }
        formPanel.add(logoPanel);

        String labelText = info.applicationName + " " + info.applicationVersion;
        LabelField labelField = new LabelField(labelText);
        labelField.setFont(FormField.getDefaultFont().deriveFont(Font.BOLD, 24));
        labelField.getMargins().setLeft(12).setBottom(8);
        formPanel.add(labelField);

        if (info.shortDescription != null && !info.shortDescription.isBlank()) {
            String desc = (info.shortDescription.length() > 60)
                    ? info.shortDescription.substring(0, 60) + "..."
                    : info.shortDescription;
            labelField = new LabelField("\"" + desc + "\"");
            labelField.getMargins().setLeft(12).setTop(1).setBottom(1);
            formPanel.add(labelField);
        }

        if (info.copyright != null && !info.copyright.isBlank()) {
            labelField = new LabelField(info.copyright);
            labelField.getMargins().setLeft(12).setTop(1).setBottom(1);
            formPanel.add(labelField);
        }

        // Check for latest version if we have an update manager:
        if (info.updateManager != null && info.updateManager.getVersionManifest() != null) {
            VersionManifest.ApplicationVersion latestVersion = info.updateManager
                    .getVersionManifest()
                    .findLatestApplicationVersion();
            if (latestVersion != null) {
                labelField = new LabelField("");
                labelField.getMargins().setLeft(12).setTop(1).setBottom(1);
                formPanel.add(labelField);
                if (VersionStringComparator.isOlderThan(info.applicationVersion, latestVersion.getVersion())) {
                    labelField.setText("A newer version (" + latestVersion.getVersion() + ") is available!");
                }
                else {
                    labelField.setText("This version (" + info.applicationVersion + ") is up to date.");
                }
            }
        }

        if (info.projectUrl != null && !info.projectUrl.isBlank()) {
            labelField = new LabelField(info.projectUrl);
            addHyperlinkIfPossible(labelField, info.projectUrl);
            labelField.getMargins().setLeft(12).setTop(1).setBottom(1);
            formPanel.add(labelField);
        }

        if (info.license != null && !info.license.isBlank()) {
            labelField = new LabelField(info.license);
            addHyperlinkIfPossible(labelField, info.license);
            labelField.getMargins().setLeft(12).setTop(1).setBottom(1);
            formPanel.add(labelField);
        }

        // Add a warning about snapshot builds if appropriate:
        if (info.applicationVersion != null && info.applicationVersion.toLowerCase().contains("snapshot")) {
            labelField = new LabelField("This is a snapshot build and is subject to change.");
            labelField.setFont(LabelField.getDefaultFont().deriveFont(Font.BOLD));
            labelField.getMargins().setLeft(12).setTop(1).setBottom(1);
            formPanel.add(labelField);
        }

        // Include swing-extras info if desired:
        if (info.includeSwingExtrasVersion) {
            String linkHtml = "<html>Built with <a href='"
                    + Version.PROJECT_URL
                    + "'>swing-extras</a> v"
                    + Version.VERSION
                    + "</html>";
            HtmlLabelField htmlLabelField = new HtmlLabelField(linkHtml, new OpenProjectUrlAction(this));
            htmlLabelField.getMargins().setLeft(12).setTop(1).setBottom(1);
            formPanel.add(htmlLabelField);
        }

        memoryUsageField = new LabelField(getMemoryStats());
        memoryUsageField.getMargins().setLeft(12).setTop(1).setBottom(1);
        memoryUsageField.getFieldComponent().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    System.gc();
                    refreshMemoryStats();
                    logger.info("Memory stats after GC: " + memoryUsageField.getText());
                }
            }
        });
        formPanel.add(memoryUsageField);

        for (String customField : info.getCustomFieldNames()) {
            labelField = new LabelField(customField, info.getCustomFieldValue(customField));
            labelField.getMargins().setLeft(12).setTop(1).setBottom(1);
            formPanel.add(labelField);
            customFields.put(customField, labelField);
        }

        String releaseNotes = getReleaseNotesText(info);
        if (releaseNotes != null && !releaseNotes.isBlank()) {
            PanelField releaseNotesField = new PanelField();
            releaseNotesField.setShouldExpand(true);
            releaseNotesField.getPanel().setLayout(new BorderLayout());
            releaseNotesField.getMargins().setAll(0).setTop(12).setLeft(12).setInternalSpacing(4);
            JTextArea textArea = new JTextArea();
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 10));
            textArea.setColumns(70);
            textArea.setRows(16);
            textArea.setEditable(false);
            textArea.setText(releaseNotes);

            JPanel wrapperPanel = new JPanel();
            wrapperPanel.setLayout(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();
            JLabel dummy1 = new JLabel("");
            JLabel dummy2 = new JLabel("");
            constraints.gridx = 0;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.weightx = 0.5;
            constraints.weighty = 0.5;
            if (formPanel.getAlignment().isCenteredHorizontally()
                    || formPanel.getAlignment().isCenteredVertically()) {
                wrapperPanel.add(dummy1, constraints);
            }
            constraints.gridx = 2;
            wrapperPanel.add(dummy2, constraints);
            constraints = new GridBagConstraints();
            constraints.gridx = 1;
            constraints.fill = GridBagConstraints.BOTH;
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.getVerticalScrollBar().setValue(0);
            wrapperPanel.add(scrollPane, constraints);
            dummy1 = new JLabel("");
            constraints.gridx = 0;
            constraints.gridy = 1;
            constraints.gridwidth = 3;
            constraints.weighty = 1;
            constraints.fill = GridBagConstraints.BOTH;
            wrapperPanel.add(dummy1, constraints);
            releaseNotesField.getPanel().add(wrapperPanel, BorderLayout.CENTER);
            formPanel.add(releaseNotesField);
            textArea.setCaretPosition(0);
        }

        add(formPanel, BorderLayout.CENTER);
    }

    public void refreshMemoryStats() {
        memoryUsageField.setText(getMemoryStats());
    }

    public void updateCustomFieldValue(String name, String value) {
        LabelField labelField = customFields.get(name);
        if (labelField != null) {
            labelField.setText(value);
        }
    }

    /**
     * Will make a best attempt to add a hyperlink to the given labelField
     * if the given urlString appears to be a valid URL, and if browsing is supported
     * by the current platform. A warning is logged if the given URL is invalid,
     * and no link will be added. Likewise, no link is added if browsing is not supported.
     */
    private void addHyperlinkIfPossible(LabelField labelField, String urlString) {
        if (HyperlinkUtil.isBrowsingSupported() && HyperlinkUtil.isValidUrl(urlString)) {
            labelField.setHyperlink(HyperlinkUtil.BrowseAction.of(urlString, this));
        }
    }

    private String getReleaseNotesText(AboutInfo info) {
        if (info.releaseNotesText != null && !info.releaseNotesText.isBlank()) {
            return info.releaseNotesText;
        }

        if (info.releaseNotesLocation != null && !info.releaseNotesLocation.isBlank()) {
            try (InputStream inStream = getClass().getResourceAsStream(info.releaseNotesLocation)) {
                if (inStream != null) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
                    StringBuilder text = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        text.append(line);
                        text.append(System.lineSeparator());
                    }
                    return text.toString();
                }
            }
            catch (IOException ioe) {
                String err = "Unable to load release notes: " + ioe.getMessage();
                logger.log(Level.SEVERE, err, ioe);
                return err;
            }
        }

        return "";
    }

    private String getMemoryStats() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long freeMemory = runtime.freeMemory();
        long totalMemory = runtime.totalMemory();
        long memoryUsed = totalMemory - freeMemory;
        int memoryUsagePercent = (int)(((float)memoryUsed / totalMemory) * 100);
        return "Using " + FileSystemUtil.getPrintableSize(memoryUsed) + " of "
                + FileSystemUtil.getPrintableSize(totalMemory) + " (" + memoryUsagePercent + "%), "
                + FileSystemUtil.getPrintableSize(maxMemory) + " available";
    }

    /**
     * Try to load or create an application logo image for the given AboutInfo.
     * We look in the logoImageLocation field and try to load it from resources.
     * If that fails, we'll generate one with basic properties.
     *
     * @param info The AboutInfo containing logoImageLocation and applicationName that we will use.
     * @return A BufferedImage, either loaded from resources or generated.
     */
    private BufferedImage getLogoImage(AboutInfo info) {
        BufferedImage image = null;
        String appName = info.applicationName == null || info.applicationName.isBlank() ? "About" : info.applicationName;
        if (info.logoImageLocation == null || info.logoImageLocation.isBlank()) {
            image = generateLogoImage(450, 90, appName);
        }
        else {
            try {
                image = ImageUtil.loadImage(getClass().getResource(info.logoImageLocation));
            }
            catch (IOException ioe) {
                logger.log(Level.SEVERE, "Error loading logo image: " + ioe.getMessage(), ioe);
                image = generateLogoImage(450, 90, appName);
            }

        }
        return image;
    }

    private BufferedImage generateLogoImage(int width, int height, String name) {
        LogoProperty config = new LogoProperty(getClass().getName());
        config.setLogoWidth(width);
        config.setLogoHeight(height);
        config.setBgColor(Color.WHITE);
        config.setBorderColor(Color.BLACK);
        config.setTextColor(Color.BLACK);
        config.setBorderWidth(4);
        config.setFontByFamilyName("Sans-Serif");
        config.setAutoSize(true);
        return LogoGenerator.generateImage(name, config);
    }

    /**
     * A MouseListener that can be optionally added to the application logo image to launch
     * the LogConsole when clicked. If this AboutPanel is embedded on an AboutDialog,
     * the AboutDialog will be disposed so that the LogConsole can take focus.
     * See <a href="https://github.com/scorbo2/swing-extras/issues/173">Issue 173</a> for details.
     *
     * @since swing-extras 2.6
     */
    private static class LogoImageMouseListener extends MouseAdapter {

        private final AboutPanel aboutPanel;

        public LogoImageMouseListener(AboutPanel aboutPanel) {
            this.aboutPanel = aboutPanel;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            // We can't do this in the constructor because we haven't been added to a dialog yet.
            // So, we have to do it here:
            JDialog parentDialog = (JDialog)SwingUtilities.getAncestorOfClass(JDialog.class, aboutPanel);
            AboutDialog aboutDialog = (parentDialog instanceof AboutDialog) ? (AboutDialog)parentDialog : null;
            if (aboutDialog != null) {
                LogConsole.getInstance().setLocationRelativeTo(aboutDialog);
                aboutDialog.dispose();
            }

            LogConsole.getInstance().setVisible(true);
        }
    }

    /**
     * An Action that opens the swing-extras project URL in the user's default browser.
     * This is used by the HtmlLabelField in the AboutPanel to handle hyperlink clicks.
     * If browsing is not supported, the URL is copied to the clipboard as a fallback.
     *
     * @since swing-extras 2.6
     */
    private static class OpenProjectUrlAction extends AbstractAction {

        private final AboutPanel aboutPanel;

        public OpenProjectUrlAction(AboutPanel aboutPanel) {
            this.aboutPanel = aboutPanel;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Window ownerWindow = SwingUtilities.getWindowAncestor(aboutPanel);

            // Check if browsing is supported, otherwise copy to clipboard
            if (!HyperlinkUtil.isBrowsingSupported()) {
                Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new StringSelection(Version.PROJECT_URL), null);
                JOptionPane.showMessageDialog(ownerWindow,
                        "Browsing is not supported in this JRE - Project link copied to clipboard.");
                return;
            }

            // Validate the URL
            if (!HyperlinkUtil.isValidUrl(Version.PROJECT_URL)) {
                JOptionPane.showMessageDialog(ownerWindow,
                        "Invalid project URL: " + Version.PROJECT_URL,
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Try to open the URL in the default browser
            try {
                URI uri = URI.create(Version.PROJECT_URL);
                Desktop.getDesktop().browse(uri);
            }
            catch (IllegalArgumentException | IOException ex) {
                String errorMsg = "Unable to browse project URL: " + ex.getMessage();
                logger.warning(errorMsg);
                JOptionPane.showMessageDialog(ownerWindow,
                        errorMsg,
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
