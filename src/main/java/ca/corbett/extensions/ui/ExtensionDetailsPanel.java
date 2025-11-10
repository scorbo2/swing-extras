package ca.corbett.extensions.ui;

import ca.corbett.extensions.AppExtension;
import ca.corbett.extensions.AppExtensionInfo;
import ca.corbett.extensions.ExtensionManager;
import ca.corbett.extras.properties.AbstractProperty;
import ca.corbett.extras.properties.LabelProperty;
import ca.corbett.extras.properties.Properties;
import ca.corbett.extras.properties.PropertiesDialog;
import ca.corbett.extras.properties.PropertiesManager;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.FontField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.LongTextField;
import ca.corbett.updates.VersionManifest;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO rewrite this javadoc because of the 2.5 changes
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2023-11-11
 */
public class ExtensionDetailsPanel extends JPanel {

    private static final Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;

    protected final Window owner;
    protected final ExtensionManager<?> extManager;
    protected final AppExtension extension;
    protected final AppExtensionInfo extInfo;
    protected final VersionManifest.ExtensionVersion extensionVersion;
    protected FormPanel formPanel;
    protected LabelField nameField;

    protected JCheckBox enabledCheckBox;

    public ExtensionDetailsPanel(Window owner, ExtensionManager<?> extManager, AppExtension extension) {
        this.owner = owner;
        this.extInfo = extension == null ? null : extension.getInfo();
        this.extension = extension;
        this.extManager = extManager;
        this.extensionVersion = null;
        initComponents();
    }

    public ExtensionDetailsPanel(Window owner, ExtensionManager<?> extManager, VersionManifest.ExtensionVersion extensionVersion) {
        this.owner = owner;
        this.extInfo = extensionVersion == null ? null : extensionVersion.getExtInfo();
        this.extManager = extManager;
        this.extension = null;
        this.extensionVersion = extensionVersion;
        initComponents();
    }

    public ExtensionDetailsPanel setNameFieldVisible(boolean visible) {
        nameField.setVisible(visible);
        return this;
    }

    protected void initComponents() {
        setLayout(new BorderLayout());
        formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.setBorderMargin(16);

        addNameField();
        addVersionField();
        addVersionRequiredField();
        addExtensionJarFields();
        addShortDescriptionField();
        addAuthorField();
        addConfigPropsField();
        addCustomFields();
        addLongDescriptionField();

        // TODO project url, release notes, screenshots (if any)
        // TODO figure out how to resolve screenshots from here

        add(PropertiesDialog.buildScrollPane(formPanel), BorderLayout.CENTER);
    }

    /**
     * Builds and returns a LabelField for displaying the extension name in slightly larger font.
     */
    protected void addNameField() {
        String name = extInfo == null ? "" : ExtensionManagerDialog.trimString(extInfo.getName(), 40);
        nameField = new LabelField(name);
        nameField.setFont(FontField.getDefaultFont().deriveFont(Font.BOLD, 16f));
        nameField.getMargins().setAll(0).setTop(4).setRight(6);
        nameField.setVisible(extInfo != null);
        formPanel.add(nameField);
    }

    protected void addExtensionJarFields() {
        if (extension != null && extInfo != null) {
            File jarFile = getSourceJar();
            formPanel.add(new LabelField("Type:", determineExtensionType()));
            if (jarFile != null) {
                String location = ExtensionManagerDialog.trimString(jarFile.getParentFile().getAbsolutePath());
                formPanel.add(new LabelField("Location:", location));
                formPanel.add(new LabelField("Jar file:", ExtensionManagerDialog.trimString(jarFile.getName())));
            }
        }
    }

    protected void addVersionField() {
        String version = extInfo == null ? "" : ExtensionManagerDialog.trimString(extInfo.getVersion());
        formPanel.add(new LabelField("Version:", version));
    }

    protected void addVersionRequiredField() {
        if (extInfo != null && extInfo.getTargetAppName() != null && extInfo.getTargetAppVersion() != null) {
            String requires = ExtensionManagerDialog.trimString(extInfo.getTargetAppName()
                                                                        + " "
                                                                        + extInfo.getTargetAppVersion());
            formPanel.add(new LabelField("Requires:", requires));
        }
    }

    protected void addAuthorField() {
        String author = "";
        if (extInfo != null) {
            author = ExtensionManagerDialog.trimString(extInfo.getAuthor());
        }
        formPanel.add(new LabelField("Author:", author));

        if (extInfo != null && extInfo.getAuthorUrl() != null && !extInfo.getAuthorUrl().isBlank()) {
            LabelField urlField = new LabelField("Author URL:", extInfo.getAuthorUrl());
            if (isUrl(extInfo.getAuthorUrl())) {
                urlField.setHyperlink(new HyperlinkAction(owner, extInfo.getAuthorUrl()));
            }
            formPanel.add(urlField);
        }
    }

    protected void addShortDescriptionField() {
        String desc = extInfo == null ? "" : extInfo.getShortDescription();
        LongTextField field = LongTextField.ofDynamicSizingMultiLine("Description:", 2);
        field.setText(desc);
        field.setEditable(false);
        field.getTextArea().setCaretPosition(0); // scroll to top
        formPanel.add(field);
    }

    protected void addConfigPropsField() {
        if (extension != null) {
            final List<AbstractProperty> configProps = extension.getConfigProperties();
            if (hasVisibleProps(configProps)) {
                boolean isPlural = configProps.size() > 1;
                String labelText = configProps.size() + " " + (isPlural ? "properties" : "property");
                LabelField labelField = new LabelField("Config:", labelText);
                labelField.setHyperlink(new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        showConfigPreview(configProps);
                    }
                });
                formPanel.add(labelField);
            }
        }
    }

    protected void addCustomFields() {
        if (extInfo != null && !extInfo.getCustomFieldNames().isEmpty()) {
            List<String> customFieldNames = extInfo.getCustomFieldNames();
            for (String fieldName : customFieldNames) {
                String fieldNameShort = ExtensionManagerDialog.trimString(fieldName, 20);
                String fieldValueShort = ExtensionManagerDialog.trimString(extInfo.getCustomFieldValue(fieldName));
                formPanel.add(new LabelField(fieldNameShort + ":", fieldValueShort));
            }
        }
    }

    protected void addLongDescriptionField() {
        LongTextField descriptionField = LongTextField.ofDynamicSizingMultiLine("Description", 5);
        descriptionField.setEditable(false);
        descriptionField.setAllowPopoutEditing(true);
        descriptionField.setText(extInfo == null ? "" : extInfo.getLongDescription());
        descriptionField.getTextArea().setCaretPosition(0); // scroll to top
        descriptionField.getMargins().setAll(4);
        formPanel.add(descriptionField);
    }

    /**
     * If an extension jar exists in a read-only directory, we consider it a "system" extension,
     * and if it is in a readable directory, it is a "user" extension - there is a third class
     * of extensions called "application built-in" which are those provided directly by an application
     * without being externally loaded from a jar file. Applications can decide how to package
     * and install extensions, and users can decide where to put their own extension jars.
     * This classification, or "extension type" is displayed in the ExtensionDetailsPanel for
     * informational purposes, but it doesn't change the way we interact with those extensions.
     *
     * @return A String describing the type of extension: System, User, or Application built-in.
     */
    protected String determineExtensionType() {
        if (extension == null) {
            return "";
        }
        File sourceJar = getSourceJar();
        if (sourceJar == null) {
            return "Application built-in";
        }
        File parentDir = sourceJar.getParentFile();
        if (!parentDir.canWrite()) {
            return "System extension";
        }
        return "User extension";
    }

    /**
     * Returns the source jar file from which this extension was loaded, or null if there isn't one
     * (which will be the case for built-in extensions, which are not loaded from external jar files).
     *
     * @return A File representing the jar file from which this extension was loaded, or null.
     */
    protected File getSourceJar() {
        if (extension == null || extManager == null) {
            return null;
        }
        return extManager.getSourceJar(extension.getClass().getName());
    }

    /**
     * Returns true if the given list of properties is not null, not empty, and has at least
     * one config property that is exposed to the user.
     *
     * @param configProps The list of props to check. Can be null.
     * @return True if there's at least one user-exposed config property in the list.
     */
    protected boolean hasVisibleProps(List<AbstractProperty> configProps) {
        if (configProps == null || configProps.isEmpty()) {
            return false;
        }
        for (AbstractProperty prop : configProps) {
            if (prop.isExposed()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Shows a read-only preview of the given properties list, with default values populated.
     * Note that any form visibility logic will NOT be reflected here, as the code at this level
     * has absolutely no idea what fields should be visible by default (that logic lives at the
     * application level).
     * <p>
     * All props will be shown here read-only, as there's nowhere to save any changes.
     * </p>
     *
     * @param configProps A list of AbstractProperty instances to show.
     */
    protected void showConfigPreview(final List<AbstractProperty> configProps) {
        // Make a note of the existing initiallyEditable and enabled state of each Property, as we will be changing it:
        Map<String, Boolean> initiallyEditableMap = new HashMap<>();
        Map<String, Boolean> enabledMap = new HashMap<>();
        for (AbstractProperty prop : configProps) {
            initiallyEditableMap.put(prop.getFullyQualifiedName(), prop.isInitiallyEditable());
            enabledMap.put(prop.getFullyQualifiedName(), prop.isEnabled());
        }

        // Make a copy of the list so we can add our preview note label field:
        List<AbstractProperty> copy = new ArrayList<>(configProps);
        for (AbstractProperty prop : copy) {
            prop.setInitiallyEditable(false);
            prop.setEnabled(true); // unconditionally force them all enabled so they show up even if extension disabled
        }
        copy.add(new LabelProperty("Preview note.Config preview.label1",
                                   "<html>Read-only config preview generated by ExtensionManager.<br>"
                                           + "To change these settings, use the application's properties dialog.</html>"));

        // Create and show a properties dialog with this list:
        PropertiesManager manager = new PropertiesManager(new Properties(), copy, "preview");
        manager.setAlwaysShowSubcategoryLabels(true);
        manager.generateDialog(owner, "Config preview").setVisible(true);

        // Now restore all properties to their previous initiallyEditable state:
        for (AbstractProperty prop : configProps) {
            prop.setInitiallyEditable(initiallyEditableMap.get(prop.getFullyQualifiedName()));
            prop.setEnabled(enabledMap.get(prop.getFullyQualifiedName()));
        }
    }

    protected static class HyperlinkAction extends AbstractAction {

        private final Window ownerWindow;
        private final String url;

        public HyperlinkAction(Window ownerWindow, String url) {
            this.ownerWindow = ownerWindow;
            this.url = url;
        }

        private void copyToClipboard() {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(url), null);
            JOptionPane.showMessageDialog(ownerWindow, "Link copied to clipboard.");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (isBrowsingSupported() && desktop != null) {
                try {
                    desktop.browse(new URI(url));
                }
                catch (IOException | URISyntaxException | IllegalArgumentException ignored) {
                    copyToClipboard();
                }
            }
            else {
                copyToClipboard();
            }
        }
    }

    /**
     * Reports whether the current JRE supports browsing (needed to open hyperlinks).
     */
    protected static boolean isBrowsingSupported() {
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
