package ca.corbett.extensions.ui;

import ca.corbett.extras.LookAndFeelManager;

import javax.swing.AbstractAction;
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
import java.awt.event.ActionEvent;

/**
 * To be used in ExtensionManagerDialog - this class represents the title panel
 * for an Extension, showing the Extension's title, along with optional
 * controls for enabling/disabling, or installing/uninstalling/updating.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.5
 */
public class ExtensionTitleBar extends JPanel {

    private final JLabel titleLabel;
    private final JCheckBox enabledCheckBox;
    private final JButton installButton;
    private final JButton uninstallButton;
    private final JButton updateButton;

    public ExtensionTitleBar(String title) {
        titleLabel = new JLabel(title);
        enabledCheckBox = new JCheckBox("Enabled", false);
        installButton = new JButton("Install");
        uninstallButton = new JButton("Uninstall");
        updateButton = new JButton("Update");

        if (title != null && !title.isBlank()) {
            initComponents();
        }
        else {
            setVisible(false);
        }
    }

    /**
     * Sets some action to execute when the enabled checkbox is selected or unselected.
     * This implicitly makes the checkbox visible, if it wasn't already.
     * You can supply null as the action, which hides the checkbox.
     */
    public ExtensionTitleBar setEnabledToggleAction(AbstractAction action) {
        enabledCheckBox.setVisible(action != null);
        if (action != null) {
            enabledCheckBox.addActionListener(e -> {
                ActionEvent evt = new ActionEvent(this, 0, "");
                action.actionPerformed(evt);
            });
        }
        return this;
    }

    /**
     * Sets some action to execute when the install button is pressed.
     * This implicitly makes the button visible, if it wasn't already.
     * You can supply null as the action, which hides the button.
     */
    public ExtensionTitleBar setInstallAction(AbstractAction action) {
        installButton.setVisible(action != null);
        if (action != null) {
            installButton.setAction(action);
        }
        return this;
    }

    /**
     * Sets some action to execute when the uninstall button is pressed.
     * This implicitly makes the button visible, if it wasn't already.
     * You can supply null as the action, which hides the button.
     */
    public ExtensionTitleBar setUninstallAction(AbstractAction action) {
        uninstallButton.setVisible(action != null);
        if (action != null) {
            uninstallButton.setAction(action);
        }
        return this;
    }

    /**
     * Sets some action to execute when the update button is pressed.
     * This implicitly makes the button visible, if it wasn't already.
     * You can supply null as the action, which hides the button.
     */
    public ExtensionTitleBar setUpdateAction(AbstractAction action) {
        updateButton.setVisible(action != null);
        if (action != null) {
            updateButton.setAction(action);
        }
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

    public ExtensionTitleBar setExtensionEnabled(boolean enable) {
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
