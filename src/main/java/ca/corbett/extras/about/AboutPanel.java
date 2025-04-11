package ca.corbett.extras.about;

import ca.corbett.extras.image.ImagePanel;
import ca.corbett.extras.image.ImagePanelConfig;
import ca.corbett.extras.image.ImageUtil;
import ca.corbett.extras.image.LogoConfig;
import ca.corbett.extras.image.LogoGenerator;
import ca.corbett.extras.logging.LogConsole;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.PanelField;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
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
 * @author scorbo2
 * @since 2018-02-14 (generified from existing code)
 */
public final class AboutPanel extends JPanel {

    private static final Logger logger = Logger.getLogger(AboutPanel.class.getName());
    private LabelField memoryUsageField;
    private final Map<String, LabelField> customFields;
    private final Desktop desktop;

    /**
     * Creates a new AboutPanel with the given AboutInfo object.
     *
     * @param info The AboutInfo object to display.
     */
    public AboutPanel(AboutInfo info) {
        super();

        desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;

        customFields = new HashMap<>();
        info.registerAboutPanel(this);
        setLayout(new BorderLayout());
        FormPanel formPanel = new FormPanel(FormPanel.Alignment.TOP_CENTER);

        BufferedImage logoImage = getLogoImage(info);
        PanelField logoPanel = new PanelField();

        if (info.logoDisplayMode != AboutInfo.LogoDisplayMode.STRETCH) {
            logoPanel.getPanel().setLayout(new FlowLayout(FlowLayout.CENTER));

            if (info.logoDisplayMode == AboutInfo.LogoDisplayMode.AS_IS_WITH_BORDER) {
                logoPanel.getPanel().setBorder(new LineBorder(Color.BLACK, 1));
                logoPanel.getPanel().setBackground(Color.DARK_GRAY);
            }

            ImageIcon icon = new ImageIcon(logoImage);
            JLabel imageLabel = new JLabel(icon);
            imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            logoPanel.getPanel().add(imageLabel);

            if (info.showLogConsole) {
                imageLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                imageLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        LogConsole.getInstance().setVisible(true);
                    }

                });
            }
        }
        else {
            logoPanel.setMargins(8, 8, 8, 8, 0);
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
                imgPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        LogConsole.getInstance().setVisible(true);
                    }

                });
            }
        }
        formPanel.addFormField(logoPanel);

        String labelText = info.applicationName + " " + info.applicationVersion;
        LabelField labelField = new LabelField(labelText);
        labelField.setFont(new Font("SansSerif", Font.BOLD, 24));
        labelField.setMargins(2, 8, 2, 2, 0);
        formPanel.addFormField(labelField);

        Font labelFont = new Font("SansSerif", Font.PLAIN, 12);
        if (info.shortDescription != null && !info.shortDescription.isBlank()) {
            String desc = (info.shortDescription.length() > 60)
                    ? info.shortDescription.substring(0, 60) + "..."
                    : info.shortDescription;
            labelField = new LabelField("\"" + desc + "\"");
            labelField.setFont(labelFont);
            labelField.setMargins(2, 8, 2, 2, 0);
            formPanel.addFormField(labelField);
        }

        if (info.copyright != null && !info.copyright.isBlank()) {
            labelField = new LabelField(info.copyright);
            labelField.setFont(labelFont);
            labelField.setMargins(2, 8, 2, 2, 0);
            formPanel.addFormField(labelField);
        }

        if (info.projectUrl != null && !info.projectUrl.isBlank()) {
            labelField = new LabelField(info.projectUrl);
            labelField.setFont(labelFont);
            if (isBrowsingSupported() && isUrl(info.projectUrl)) {
                try {
                    labelField.setHyperlink(new BrowseAction(URI.create(info.projectUrl)));
                }
                catch (IllegalArgumentException e) {
                    logger.warning("Project URL is not well-formed.");
                }
            }
            labelField.setMargins(2, 8, 2, 2, 0);
            formPanel.addFormField(labelField);
        }

        if (info.license != null && !info.license.isBlank()) {
            labelField = new LabelField(info.license);
            labelField.setFont(labelFont);
            if (isBrowsingSupported() && isUrl(info.license)) {
                try {
                    labelField.setHyperlink(new BrowseAction(URI.create(info.license)));
                }
                catch (IllegalArgumentException e) {
                    logger.warning("License URL is not well-formed.");
                }
            }
            labelField.setMargins(2, 8, 2, 2, 0);
            formPanel.addFormField(labelField);
        }

        memoryUsageField = new LabelField(getMemoryStats());
        memoryUsageField.setFont(labelFont);
        memoryUsageField.setMargins(2, 8, 2, 2, 0);
        formPanel.addFormField(memoryUsageField);

        for (String customField : info.getCustomFieldNames()) {
            labelField = new LabelField(customField, info.getCustomFieldValue(customField));
            labelField.setFont(labelFont);
            labelField.setMargins(4, 8, 4, 2, 0);
            formPanel.addFormField(labelField);
            customFields.put(customField, labelField);
        }

        String releaseNotes = getReleaseNotesText(info);
        if (releaseNotes != null && !releaseNotes.isBlank()) {
            PanelField releaseNotesField = new PanelField();
            releaseNotesField.getPanel().setLayout(new BorderLayout());
            releaseNotesField.setMargins(12, 0, 0, 0, 4);
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
            wrapperPanel.add(dummy1, constraints);
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
            formPanel.addFormField(releaseNotesField);
            textArea.setCaretPosition(0);
        }

        formPanel.render();
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
        int maxMemory = (int)(runtime.maxMemory() / 1024 / 1024);
        int freeMemory = (int)(runtime.freeMemory() / 1024 / 1024);
        int totalMemory = (int)(runtime.totalMemory() / 1024 / 1024);
        int memoryUsed = totalMemory - freeMemory;
        int memoryUsagePercent = (int)(((float)memoryUsed / totalMemory) * 100);
        return "Using " + memoryUsed + "M of "
                + totalMemory + "M (" + memoryUsagePercent + "%), "
                + maxMemory + "M available";
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
        LogoConfig config = new LogoConfig(getClass().getName());
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

    private class BrowseAction extends AbstractAction {
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

    ;

    private boolean isBrowsingSupported() {
        return desktop != null && desktop.isSupported(Desktop.Action.BROWSE);
    }

    private boolean isUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }
}
