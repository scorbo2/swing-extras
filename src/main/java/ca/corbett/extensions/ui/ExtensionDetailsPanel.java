package ca.corbett.extensions.ui;

import ca.corbett.extensions.AppExtension;
import ca.corbett.extensions.AppExtensionInfo;
import ca.corbett.extensions.ExtensionManager;
import ca.corbett.extras.properties.AbstractProperty;
import ca.corbett.extras.properties.LabelProperty;
import ca.corbett.extras.properties.Properties;
import ca.corbett.extras.properties.PropertiesManager;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.FontField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.LongTextField;
import ca.corbett.forms.fields.PanelField;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shows the details of a single AppExtension (by interrogating its AppExtensionInfo).
 * Can also be used to enable or disable an extension. You shouldn't generally need
 * to instantiate this panel yourself... much easier to go through ExtensionPanel
 * or ExtensionDialog.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2023-11-11
 */
public class ExtensionDetailsPanel extends JPanel {

    protected final Window owner;
    protected final ExtensionManager extManager;
    protected final List<ExtensionDetailsPanelListener> listeners;
    protected final AppExtension extension;
    protected FormPanel formPanel;

    protected JCheckBox enabledCheckBox;

    /**
     * Creates a new ExtensionDetailsPanel for the given AppExtension, which can be null - if
     * null, an empty disabled panel will be generated.
     *
     * @param owner The Window that owns this panel.
     * @param manager   The ExtensionManager that's managing this extension.
     * @param extension Any AppExtension, or null to generate a disabled empty details panel.
     * @param isEnabled Whether the given extension is enabled (ignored if extension is null).
     */
    public ExtensionDetailsPanel(Window owner, ExtensionManager manager, AppExtension extension, boolean isEnabled) {
        this.owner = owner;
        listeners = new ArrayList<>();
        this.extension = extension;
        this.extManager = manager;
        initComponents(isEnabled);
    }

    /**
     * Returns the state of the "enabled" checkbox, which can be manipulated by the user.
     *
     * @return Whether this extension is marked as enabled or not.
     */
    public boolean isExtensionEnabled() {
        return enabledCheckBox.isSelected();
    }

    /**
     * Allows programmatic setting of the "enabled" checkbox. Note that this doesn't actually
     * enable or disable the extension - it just sets the checkbox state in this
     * details panel, which can later be read by isExtensionEnabled().
     *
     * @param enabled The new value for the enabled checkbox.
     */
    public void setExtensionEnabled(boolean enabled) {
        enabledCheckBox.setSelected(enabled);
    }

    /**
     * Registers a listener that will be informed when our enabled checkbox state is modified.
     *
     * @param listener An ExtensionDetailsPanelListener
     */
    public void addExtensionDetailsPanelListener(ExtensionDetailsPanelListener listener) {
        listeners.add(listener);
    }

    /**
     * Unregisters the given listener.
     *
     * @param listener An ExtensionDetailsPanelListener
     */
    public void removeExtensionDetailsPanelListener(ExtensionDetailsPanelListener listener) {
        listeners.remove(listener);
    }

    protected void initComponents(boolean isEnabled) {
        setLayout(new BorderLayout());
        formPanel = new FormPanel(Alignment.TOP_LEFT);

        PanelField panelField = new PanelField(new FlowLayout(FlowLayout.RIGHT));
        JPanel panel = panelField.getPanel();
        enabledCheckBox = new JCheckBox("Enabled", isEnabled);
        enabledCheckBox.setOpaque(false);
        if (extension == null) {
            enabledCheckBox.setSelected(false);
            enabledCheckBox.setEnabled(false);
        }
        enabledCheckBox.addActionListener(e -> fireEnableChangeEvent());
        panel.add(enabledCheckBox);
        panelField.getMargins().setAll(0).setInternalSpacing(2);
        formPanel.add(panelField);

        AppExtensionInfo extInfo = extension == null ? null : extension.getInfo();
        String name = extInfo == null ? "" : trimString(extInfo.getName(), 40);
        LabelField nameField = new LabelField(name);
        nameField.setFont(FontField.getDefaultFont().deriveFont(Font.BOLD, 16f));
        nameField.getMargins().setAll(0).setTop(4).setRight(6);
        formPanel.add(nameField);

        File jarFile = getSourceJar();
        formPanel.add(new LabelField("Type:", extInfo == null ? "" : determineExtensionType()));
        if (jarFile != null) {
            String location = trimString(jarFile.getParentFile().getAbsolutePath());
            formPanel.add(new LabelField("Location:", location));
            formPanel.add(new LabelField("Jar file:", trimString(jarFile.getName())));
        }
        formPanel.add(new LabelField("Version:", extInfo == null ? "" : trimString(extInfo.getVersion())));
        if (extInfo != null && extInfo.getTargetAppName() != null && extInfo.getTargetAppVersion() != null) {
            String requires = trimString(extInfo.getTargetAppName() + " " + extInfo.getTargetAppVersion());
            formPanel.add(new LabelField("Requires:", requires));
        }
        formPanel.add(new LabelField("Author:", extInfo == null ? "" : trimString(extInfo.getAuthor())));

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

        if (extInfo != null && !extInfo.getCustomFieldNames().isEmpty()) {
            List<String> customFieldNames = extInfo.getCustomFieldNames();
            for (String fieldName : customFieldNames) {
                String fieldNameShort = trimString(fieldName, 20);
                String fieldValueShort = trimString(extInfo.getCustomFieldValue(fieldName));
                formPanel.add(new LabelField(fieldNameShort + ":", fieldValueShort));
            }
        }

        LongTextField descriptionField = LongTextField.ofDynamicSizingMultiLine("Description", 8);

        // Marking the TextField as disabled will unfortunately change the text color to something
        // much lighter, which makes it very hard or almost impossible to read in some look and feels:
        //descriptionField.setEnabled(false);

        // So instead, we'll leave it as "enabled" but mark the JTextArea itself as read-only:
        JTextArea jTextArea = descriptionField.getTextArea();
        jTextArea.setEditable(false);

        descriptionField.setText(extInfo == null ? "" : extInfo.getLongDescription());
        jTextArea.setCaretPosition(0); // scroll to top
        descriptionField.getMargins().setAll(4);
        formPanel.add(descriptionField);

        add(formPanel, BorderLayout.CENTER);
    }

    protected void fireEnableChangeEvent() {
        String className = extension == null ? null : extension.getClass().getName();
        if (className == null) {
            return;
        }
        for (ExtensionDetailsPanelListener listener : new ArrayList<>(listeners)) {
            if (enabledCheckBox.isSelected()) {
                listener.extensionEnabled(this, className);
            }
            else {
                listener.extensionDisabled(this, className);
            }
        }

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
        if (extension == null) {
            return null;
        }
        return extManager.getSourceJar(extension.getClass().getName());
    }

    protected String trimString(String input) {
        return trimString(input, 50);
    }

    protected String trimString(String input, final int LIMIT) {
        if (input == null) {
            return null;
        }
        if (input.length() >= LIMIT) {
            input = input.substring(0, LIMIT) + "...";
        }
        return input;
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
}
