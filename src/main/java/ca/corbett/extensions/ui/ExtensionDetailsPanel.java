package ca.corbett.extensions.ui;

import ca.corbett.extensions.AppExtension;
import ca.corbett.extensions.AppExtensionInfo;
import ca.corbett.extensions.ExtensionManager;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.PanelField;
import ca.corbett.forms.fields.TextField;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows the details of a single AppExtension (by interrogating its AppExtensionInfo).
 * Can also be used to enable or disable an extension. You shouldn't generally need
 * to instantiate this panel yourself... much easier to go through ExtensionPanel
 * or ExtensionDialog.
 *
 * @author scorbo2
 * @since 2023-11-11
 */
public class ExtensionDetailsPanel extends JPanel {

    protected final ExtensionManager extManager;
    protected final List<ExtensionDetailsPanelListener> listeners;
    protected final AppExtension extension;
    protected FormPanel formPanel;

    protected JCheckBox enabledCheckBox;

    /**
     * Creates a new ExtensionDetailsPanel for the given AppExtension, which can be null - if
     * null, an empty disabled panel will be generated.
     *
     * @param manager   The ExtensionManager that's managing this extension.
     * @param extension Any AppExtension, or null to generate a disabled empty details panel.
     * @param isEnabled Whether the given extension is enabled (ignored if extension is null).
     */
    public ExtensionDetailsPanel(ExtensionManager manager, AppExtension extension, boolean isEnabled) {
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
        formPanel = new FormPanel(FormPanel.Alignment.TOP_LEFT);

        PanelField panelField = new PanelField();
        JPanel panel = panelField.getPanel();
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        enabledCheckBox = new JCheckBox("Enabled", isEnabled);
        enabledCheckBox.setOpaque(false);
        if (extension == null) {
            enabledCheckBox.setSelected(false);
            enabledCheckBox.setEnabled(false);
        }
        enabledCheckBox.addActionListener(e -> fireEnableChangeEvent());
        panel.add(enabledCheckBox);
        panelField.setMargins(0, 0, 0, 0, 2);
        formPanel.addFormField(panelField);

        AppExtensionInfo extInfo = extension == null ? null : extension.getInfo();
        String name = extInfo == null ? "" : trimString(extInfo.getName(), 40);
        LabelField nameField = new LabelField(name);
        nameField.setFont(nameField.getFieldLabelFont().deriveFont(Font.BOLD, 16f));
        nameField.setMargins(0, 4, 6, 0, 0);
        formPanel.addFormField(nameField);

        File jarFile = getSourceJar();
        formPanel.addFormField(new LabelField("Type:", extInfo == null ? "" : determineExtensionType()));
        if (jarFile != null) {
            String location = trimString(jarFile.getParentFile().getAbsolutePath());
            formPanel.addFormField(new LabelField("Location:", location));
            formPanel.addFormField(new LabelField("Jar file:", trimString(jarFile.getName())));
        }
        formPanel.addFormField(new LabelField("Version:", extInfo == null ? "" : trimString(extInfo.getVersion())));
        if (extInfo != null && extInfo.getTargetAppName() != null && extInfo.getTargetAppVersion() != null) {
            String requires = trimString(extInfo.getTargetAppName() + " " + extInfo.getTargetAppVersion());
            formPanel.addFormField(new LabelField("Requires:", requires));
        }
        formPanel.addFormField(new LabelField("Author:", extInfo == null ? "" : trimString(extInfo.getAuthor())));

        if (extInfo != null && !extInfo.getCustomFieldNames().isEmpty()) {
            List<String> customFieldNames = extInfo.getCustomFieldNames();
            for (String fieldName : customFieldNames) {
                String fieldNameShort = trimString(fieldName, 20);
                String fieldValueShort = trimString(extInfo.getCustomFieldValue(fieldName));
                formPanel.addFormField(new LabelField(fieldNameShort + ":", fieldValueShort));
            }
        }

        TextField descriptionField = new TextField("Description:", 40, 5, true);
        descriptionField.setEnabled(false);
        ((JTextArea)descriptionField.getFieldComponent()).setLineWrap(true);
        descriptionField.setText(extInfo == null ? "" : extInfo.getLongDescription());
        ((JTextArea)descriptionField.getFieldComponent()).setCaretPosition(0); // scroll to top
        descriptionField.setScrollPanePreferredSize(460, 100);
        descriptionField.setMargins(10, 4, 4, 4, 4);
        formPanel.addFormField(descriptionField);

        formPanel.render();
        add(formPanel, BorderLayout.CENTER);
    }

    protected void fireEnableChangeEvent() {
        String className = extension == null ? null : extension.getClass().getName();
        if (className == null) {
            return;
        }
        for (ExtensionDetailsPanelListener listener : listeners) {
            if (enabledCheckBox.isSelected()) {
                listener.extensionEnabled(this, className);
            }
            else {
                listener.extensionDisabled(this, className);
            }
        }

    }

    /**
     * TODO this is a goofy holdover from sc-util and should probably be removed or at least
     * adjusted. The concept of a "system extension" versus a "user extension" will probably
     * no longer exist. I do like the idea of differentiating user-supplied (i.e. loaded
     * from a jar file) extensions versus built-in (loaded by the application itself) ones though.
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
}
