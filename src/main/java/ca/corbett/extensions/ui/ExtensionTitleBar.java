package ca.corbett.extensions.ui;

import ca.corbett.extensions.AppExtension;
import ca.corbett.extras.LookAndFeelManager;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * To be used in ExtensionManagerDialog - this class represents the title panel
 * for an Extension, showing the Extension's title, along with optional
 * controls for enabling/disabling, or installing/uninstalling/updating.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.5
 */
public class ExtensionTitleBar<T extends AppExtension> extends JPanel {

    private final T extension;
    private final JLabel titleLabel;
    private final JCheckBox enabledCheckBox;
    private final JButton installButton;
    private final JButton uninstallButton;
    private final JButton updateButton;

    public ExtensionTitleBar(T extension) {
        this.extension = extension;

        titleLabel = new JLabel();
        enabledCheckBox = new JCheckBox("Enabled", false);
        installButton = new JButton("Install");
        uninstallButton = new JButton("Uninstall");
        updateButton = new JButton("Update");

        if (extension != null) {
            initComponents();
        }
    }

    public ExtensionTitleBar<T> setAllowEnable(boolean allow) {
        enabledCheckBox.setVisible(allow);
        return this;
    }

    public ExtensionTitleBar<T> setAllowInstall(boolean allow) {
        installButton.setVisible(allow);
        return this;
    }

    public ExtensionTitleBar<T> setAllowUninstall(boolean allow) {
        uninstallButton.setVisible(allow);
        return this;
    }

    public ExtensionTitleBar<T> setAllowUpdate(boolean allow) {
        updateButton.setVisible(allow);
        return this;
    }

    public boolean isAllowEnabled() {
        return enabledCheckBox.isVisible();
    }

    public boolean isAllowInstall() {
        return installButton.isVisible();
    }

    public boolean isAllowUninstall() {
        return uninstallButton.isVisible();
    }

    public boolean isAllowUpdate() {
        return updateButton.isVisible();
    }

    public ExtensionTitleBar<T> setExtensionEnabled(boolean enable) {
        enabledCheckBox.setSelected(enable);
        return this;
    }

    public boolean isExtensionEnabled() {
        return enabledCheckBox.isSelected();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 12, 0, 12);
        titleLabel.setText(extension.getInfo().getName());
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 24f));
        titleLabel.setHorizontalAlignment(JLabel.LEFT);
        titleLabel.setVerticalAlignment(JLabel.CENTER);
        wrapperPanel.add(titleLabel, gbc);
        add(wrapperPanel, BorderLayout.CENTER);

        final Dimension buttonSize = new Dimension(90, 23);
        enabledCheckBox.setPreferredSize(buttonSize);
        installButton.setPreferredSize(buttonSize);
        uninstallButton.setPreferredSize(buttonSize);
        updateButton.setPreferredSize(buttonSize);
        enabledCheckBox.setVisible(false);
        installButton.setVisible(false);
        uninstallButton.setVisible(false);
        updateButton.setVisible(false);

        wrapperPanel = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(4,0,0,12);
        wrapperPanel.add(enabledCheckBox, gbc);

        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0,0,0,12);
        gbc.gridy++;
        wrapperPanel.add(installButton, gbc);

        gbc.gridy++;
        wrapperPanel.add(uninstallButton, gbc);

        gbc.gridy++;
        wrapperPanel.add(updateButton, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(4, 0,0,0);
        wrapperPanel.add(new JLabel(""), gbc); // spacer

        add(wrapperPanel, BorderLayout.EAST);
        Color borderColor = LookAndFeelManager.getLafColor("Separator.highlight", Color.LIGHT_GRAY);
        setBorder(BorderFactory.createLineBorder(borderColor, 1));
    }
}
