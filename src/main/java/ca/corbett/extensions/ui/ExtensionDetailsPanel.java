package ca.corbett.extensions.ui;

import ca.corbett.extensions.AppExtensionInfo;
import ca.corbett.extras.properties.AbstractProperty;
import ca.corbett.extras.properties.LabelProperty;
import ca.corbett.extras.properties.Properties;
import ca.corbett.extras.properties.PropertiesDialog;
import ca.corbett.extras.properties.PropertiesManager;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.FontField;
import ca.corbett.forms.fields.ImageListField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.LongTextField;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Displays details about an extension using a supplied AppExtensionInfo instance.
 * Optionally, callers can provide additional information about the extension to
 * display extra information:
 * <ul>
 *     <li>For locally installed and loaded extensions, you can supply a jar file
 *         to display information about where the extension is installed.
 *     <li>For downloadable extensions that are not yet installed, you can display
 *         information such as the list of screenshots provided (if any).
 *     <li>You can supply a null AppExtensionInfo instance for a "blank" display panel.
 * </ul>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2023-11-11
 */
public class ExtensionDetailsPanel extends JPanel {

    private static final Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;

    protected final Window owner;
    protected final AppExtensionInfo extInfo;
    protected FormPanel formPanel;
    protected LabelField nameField;
    protected LabelField extensionTypeField;
    protected LabelField jarLocationField;
    protected LabelField jarNameField;
    protected LabelField configPropsField;
    protected ImageListField screenshotsField;

    /**
     * Creates an ExtensionDetailsPanel for the given AppExtensionInfo instance (which may be null).
     * The Window parameter is to set ownership and position of any popups launched from this panel.
     */
    public ExtensionDetailsPanel(Window owner, AppExtensionInfo extInfo) {
        this.owner = owner;
        this.extInfo = extInfo;
        initComponents();
    }

    /**
     * The extension Name field can be optionally made hidden. This is useful if you are selecting
     * extensions out of a list, and the name of the extension is already obvious from context.
     */
    public ExtensionDetailsPanel setNameFieldVisible(boolean visible) {
        nameField.setVisible(visible);
        return this;
    }

    /**
     * Use this method to indicate that the extension in question is a locally-installed
     * extension with the given source jar. Doing so will display extra information
     * inferred from the supplied jar file (which may be null).
     * <p>
     * <b>If the given jar file is null:</b> the extension is considered a "built-in"
     * (application-supplied) extension that does not have an install location.
     * </p>
     * <p>
     * <b>If the given jar file is not null:</b>
     * </p>
     * <ul>
     *     <li>The install location of the jar file is displayed.
     *     <li>The name of the jar file is displayed.
     *     <li>Extension type: "system" if the jar is in a read-only location, or "user"
     *         if the jar is in a writable directory.
     * </ul>
     * <p>
     *     Note that this method does nothing if the AppExtensionInfo supplied to
     *     the constructor was null.
     * </p>
     */
    public ExtensionDetailsPanel setIsLocallyInstalledExtension(File jarFile) {
        if (extInfo == null) {
            return this;
        }

        // If there's no jar file, then it's an application-supplied extension.
        if (jarFile == null) {
            extensionTypeField.setText(AppExtensionInfo.EXT_TYPE_BUILTIN);
            extensionTypeField.setVisible(true);
        }

        // Otherwise, show information about the jar file:
        else {
            File parentDir = jarFile.getParentFile();
            extensionTypeField.setText(parentDir.canWrite()
                                               ? AppExtensionInfo.EXT_TYPE_USER
                                               : AppExtensionInfo.EXT_TYPE_SYSTEM);
            jarLocationField.setText(ExtensionManagerDialog.trimString(jarFile.getParentFile().getAbsolutePath()));
            jarNameField.setText(ExtensionManagerDialog.trimString(jarFile.getName()));
            extensionTypeField.setVisible(true);
            jarLocationField.setVisible(true);
            jarNameField.setVisible(true);
        }

        return this;
    }

    /**
     * You can supply optional screenshots to accompany this extension's display.
     */
    public ExtensionDetailsPanel addScreenshot(BufferedImage screenshot, boolean isEditable) {
        screenshotsField.setVisible(true);
        screenshotsField.setEnabled(isEditable);
        screenshotsField.addImage(screenshot);
        return this;
    }

    /**
     * You can supply optional screenshots to accompany this extension's display.
     */
    public ExtensionDetailsPanel addScreenshot(BufferedImage thumbnail, ImageIcon screenshot, boolean isEditable) {
        screenshotsField.setVisible(true);
        screenshotsField.setEnabled(isEditable);
        screenshotsField.addImage(thumbnail, screenshot);
        return this;
    }

    public ExtensionDetailsPanel setScreenshotsVisible(boolean visible) {
        screenshotsField.setVisible(visible);
        return this;
    }

    public ExtensionDetailsPanel setScreenshotsEditable(boolean editable) {
        screenshotsField.setEnabled(editable);
        return this;
    }

    public int getScreenshotCount() {
        return screenshotsField.getImageCount();
    }

    public Object getScreenshotAtIndex(int i) {
        return screenshotsField.getImageAt(i);
    }

    public ExtensionDetailsPanel setScreenshotsThumbnailSize(int size) {
        screenshotsField.setVisible(true);
        screenshotsField.setThumbnailSize(size);
        return this;
    }

    /**
     * You can supply a list of configuration properties for the extension, in which case a hyperlink
     * field will be shown to display a preview of those properties. Note that if the given list does
     * not contain any exposed properties (isExposed() == true), then the field will not be shown.
     */
    public ExtensionDetailsPanel setConfigProperties(List<AbstractProperty> configProps) {
        if (hasVisibleProps(configProps)) {
            configPropsField.setVisible(true);
            configPropsField.clearHyperlink();
            boolean isPlural = configProps.size() > 1;
            String labelText = configProps.size() + " " + (isPlural ? "properties" : "property");
            configPropsField.setText(labelText);
            configPropsField.setHyperlink(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showConfigPreview(configProps);
                }
            });
        }
        else {
            configPropsField.setVisible(false);
            configPropsField.clearHyperlink();
        }
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
        addProjectUrlField();
        addReleaseNotesField();
        addScreenshotsField();

        add(PropertiesDialog.buildScrollPane(formPanel), BorderLayout.CENTER);
    }

    /**
     * Adds a LabelField for displaying the extension name in slightly larger font.
     * This can be hidden via setNameFieldVisible().
     */
    protected void addNameField() {
        String name = extInfo == null
                ? "(nothing selected)"
                : ExtensionManagerDialog.trimString(extInfo.getName(), 40);
        nameField = new LabelField(name);
        nameField.setEnabled(extInfo != null);
        nameField.setFont(FontField.getDefaultFont().deriveFont(Font.BOLD, 16f));
        nameField.getMargins().setAll(0).setTop(4).setRight(6);
        nameField.setVisible(true);
        formPanel.add(nameField);
    }

    /**
     * Adds initially-hidden fields for displaying information about locally-installed extensions.
     * These fields can be made visible via setIsLocallyInstalledExtension().
     */
    protected void addExtensionJarFields() {
        if (extInfo != null) {
            extensionTypeField = new LabelField("Type:", "");
            jarLocationField = new LabelField("Location:", "");
            jarNameField = new LabelField("Jar file:", "");
            extensionTypeField.setVisible(false);
            jarLocationField.setVisible(false);
            jarNameField.setVisible(false);
            formPanel.add(extensionTypeField);
            formPanel.add(jarLocationField);
            formPanel.add(jarNameField);
        }
    }

    /**
     * Adds a LabelField for showing the extension version (if one is set).
     */
    protected void addVersionField() {
        String version = extInfo == null ? "" : ExtensionManagerDialog.trimString(extInfo.getVersion());
        formPanel.add(new LabelField("Version:", version));
    }

    /**
     * If the extension has a targetAppName and a targetAppVersion (which it typically will),
     * adds a LabelField to show this.
     */
    protected void addVersionRequiredField() {
        if (extInfo != null && extInfo.getTargetAppName() != null && extInfo.getTargetAppMajorVersion() != 0) {
            String requires = ExtensionManagerDialog.trimString(extInfo.getTargetAppName()
                                                                        + " v"
                                                                        + extInfo.getTargetAppMajorVersion()
                                                                        + ".x");
            formPanel.add(new LabelField("Compatible with " + requires));
        }
    }

    /**
     * Adds a LabelField to show the author name. If an author URL is also set,
     * adds a LabelField to show that as well.
     */
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

    /**
     * Adds a multi-line text field for showing the short description of this extension.
     */
    protected void addShortDescriptionField() {
        String desc = extInfo == null ? "" : extInfo.getShortDescription();
        LongTextField field = LongTextField.ofDynamicSizingMultiLine("Description:", 2);
        field.setText(desc);
        field.setEditable(false);
        field.getTextArea().setCaretPosition(0); // scroll to top
        formPanel.add(field);
    }

    /**
     * Adds an initially-hidden LabelField which can show a hyperlink for showing a preview
     * of any config properties specified by this extension. This field can be made
     * visible via setConfigProperties().
     */
    protected void addConfigPropsField() {
        configPropsField = new LabelField("Config:", "");
        configPropsField.setVisible(false);
        formPanel.add(configPropsField);
    }

    /**
     * If any custom fields are defined in the AppExtensionInfo instance, this will
     * add one LabelField for each defined field.
     */
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

    /**
     * Adds a LongTextField with popout capabilities to display the extension's long description.
     */
    protected void addLongDescriptionField() {
        LongTextField descriptionField = LongTextField.ofDynamicSizingMultiLine("Description:", 5);
        descriptionField.setEditable(false);
        descriptionField.setAllowPopoutEditing(true);
        descriptionField.setText(extInfo == null ? "" : extInfo.getLongDescription());
        descriptionField.getTextArea().setCaretPosition(0); // scroll to top
        descriptionField.getMargins().setAll(4);
        formPanel.add(descriptionField);
    }

    /**
     * If the AppExtensionInfo specifies a project URL, adds a hyperlink label for viewing it.
     */
    protected void addProjectUrlField() {
        if (extInfo != null && extInfo.getProjectUrl() != null && !extInfo.getProjectUrl().isBlank()) {
            LabelField urlField = new LabelField("Project URL:", extInfo.getProjectUrl());
            if (isUrl(extInfo.getAuthorUrl())) {
                urlField.setHyperlink(new HyperlinkAction(owner, extInfo.getProjectUrl()));
            }
            formPanel.add(urlField);
        }
    }

    /**
     * If the AppExtensionInfo specifies release notes, adds a LongTextField with popout capabilities for viewing them.
     */
    protected void addReleaseNotesField() {
        if (extInfo != null && extInfo.getReleaseNotes() != null && !extInfo.getReleaseNotes().isBlank()) {
            LongTextField releaseNotesField = LongTextField.ofDynamicSizingMultiLine("Release notes:", 5);
            releaseNotesField.setEditable(false);
            releaseNotesField.setAllowPopoutEditing(true);
            releaseNotesField.setText(extInfo.getReleaseNotes());
            releaseNotesField.getTextArea().setCaretPosition(0); // scroll to top
            releaseNotesField.getMargins().setAll(4);
            formPanel.add(releaseNotesField);
        }
    }

    /**
     * Adds an initially-hidden ImageListPanel for viewing extension screenshots.
     * This field can be made visible via either of the addScreenshot() methods.
     */
    protected void addScreenshotsField() {
        screenshotsField = new ImageListField("Screenshots:", 1);
        screenshotsField.setShouldExpand(true);
        screenshotsField.setVisible(false);
        screenshotsField.getImageListPanel().setOwnerWindow(owner);
        formPanel.add(screenshotsField);
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

    /**
     * A generic action that can be hooked onto any hyperlinked LabelField to open
     * the given url in the user's default browser. If the given URL can't be parsed,
     * or if the current JRE doesn't support link browsing, then the url is copied
     * to the system clipboard and an informational popup is shown to that effect.
     *
     * @author <a href="https://github.com/scorbo2">scorbo2</a>
     */
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
