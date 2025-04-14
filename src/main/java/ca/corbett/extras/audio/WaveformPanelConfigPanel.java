package ca.corbett.extras.audio;

import ca.corbett.extras.config.ConfigPanel;
import ca.corbett.extras.properties.Properties;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A ConfigPanel implementation for AudioWaveformPanel. Note that WaveformConfig
 * has its own ConfigPanel which should be used in conjunction with this one - this panel
 * is for configuring the AudioWaveformPanel options, such as control placement, editing
 * functions, and so on, while the WaveformConfigPanel is for viewing and
 * editing the cosmetic properties of the generated waveform (colour, scale, etc).
 *
 * @author scorbett
 * @since 2018-01-26
 */
public class WaveformPanelConfigPanel extends ConfigPanel<AudioWaveformPanel> {

    private final String[] controlPositions = {
            "Top left",
            "Top center",
            "Top right",
            "Left/Right split",
            "Bottom left",
            "Bottom center",
            "Bottom right",
            "Hidden"
    };
    private final JComboBox controlPositionCombo = new JComboBox(controlPositions);

    private final String[] controlSizes = {
            "Xsmall",
            "Small",
            "Normal",
            "Large",
            "XLarge"
    };
    private final JComboBox controlSizeCombo = new JComboBox(controlSizes);

    private final String[] controlTypes = {
            "Read-only",
            "Allow recording",
            "Allow editing",
            "Allow all"
    };
    private final JComboBox controlTypeCombo = new JComboBox(controlTypes);

    /**
     * Creates a new panel with the given title and with no associated AudioWaveformPanel.
     * Use setModelObject to associate this config panel with a waveform panel.
     *
     * @param title The title to display in the TitledBorder
     */
    public WaveformPanelConfigPanel(String title) {
        this(title, null);
    }

    /**
     * Creates a new panel with the given title, and then associates this panel with the
     * given AudioWaveformPanel.
     *
     * @param title The title to display in the TitledBorder
     * @param obj   The AudioWaveformPanel to which to associate this config panel.
     */
    public WaveformPanelConfigPanel(String title, AudioWaveformPanel obj) {
        super(title);
        initComponents();
        if (obj != null) {
            setModelObject(obj);
        }
    }

    @Override
    public void save(Properties props, String prefix) {
        if (prefix == null) {
            prefix = "";
        }

        props.setString(prefix + "controlPosition", (String)controlPositionCombo.getSelectedItem());
        props.setString(prefix + "controlSize", (String)controlSizeCombo.getSelectedItem());
        props.setString(prefix + "controlType", (String)controlTypeCombo.getSelectedItem());
    }

    @Override
    public void load(Properties props, String prefix) {
        if (prefix == null) {
            prefix = "";
        }

        String defaultPosition = controlPositions[3];
        String defaultSize = controlSizes[2];
        String defaultType = controlTypes[3];
        controlPositionCombo.setSelectedItem(props.getString(prefix + "controlPosition", defaultPosition));
        controlSizeCombo.setSelectedItem(props.getString(prefix + "controlSize", defaultSize));
        controlTypeCombo.setSelectedItem(props.getString(prefix + "controlType", defaultType));
        updateControlType(modelObject);
        updateControlSize(modelObject);
        updateControlPosition(modelObject);
        notifyChangeListeners();
    }

    /**
     * Applies the current settings to the given AudioWaveformPanel, without changing
     * the current model object.
     *
     * @param panel The panel to which settings will be applied.
     */
    public void applySettingsToPanel(AudioWaveformPanel panel) {
        updateControlType(panel);
        updateControlSize(panel);
        updateControlPosition(panel);
        notifyChangeListeners();
    }

    @Override
    public void load(AudioWaveformPanel obj) {
        //@formatter:off
        switch (obj.getControlPanelSize()) {
            case XSMALL: controlSizeCombo.setSelectedIndex(0); break;
            case SMALL:  controlSizeCombo.setSelectedIndex(1); break;
            case NORMAL: controlSizeCombo.setSelectedIndex(2); break;
            case LARGE:  controlSizeCombo.setSelectedIndex(3); break;
            case XLARGE: controlSizeCombo.setSelectedIndex(4); break;
        }

        switch (obj.getControlPanelPosition()) {
            case TOP_LEFT:   controlPositionCombo.setSelectedIndex(0); break;
            case TOP_CENTER: controlPositionCombo.setSelectedIndex(1); break;
            case TOP_RIGHT:  controlPositionCombo.setSelectedIndex(2); break;
            case SIDE_EDGES: controlPositionCombo.setSelectedIndex(3); break;
            case BOTTOM_LEFT:controlPositionCombo.setSelectedIndex(4); break;
            case BOTTOM_CENTER: controlPositionCombo.setSelectedIndex(5); break;
            case BOTTOM_RIGHT:  controlPositionCombo.setSelectedIndex(6); break;
            case HIDDEN:     controlPositionCombo.setSelectedIndex(7);  break;
        }
        //@formatter:on

        if (obj.isEditingAllowed() && obj.isRecordingAllowed()) {
            controlTypeCombo.setSelectedIndex(3);
        }
        else if (obj.isEditingAllowed()) {
            controlTypeCombo.setSelectedIndex(2);
        }
        else if (obj.isRecordingAllowed()) {
            controlTypeCombo.setSelectedIndex(1);
        }
        else {
            controlTypeCombo.setSelectedIndex(0);
        }
        updateControlType(modelObject);
        updateControlSize(modelObject);
        updateControlPosition(modelObject);
        notifyChangeListeners();
    }

    /**
     * Invoked internally to set the model object's control type based on our current setting. *
     */
    private void updateControlType(AudioWaveformPanel panel) {
        if (panel != null) {
            switch (controlTypeCombo.getSelectedIndex()) {
                case 0:
                    panel.setEditingAllowed(false);
                    panel.setRecordingAllowed(false);
                    break;

                case 1:
                    panel.setEditingAllowed(false);
                    panel.setRecordingAllowed(true);
                    break;

                case 2:
                    panel.setEditingAllowed(true);
                    panel.setRecordingAllowed(false);
                    break;

                case 3:
                    panel.setEditingAllowed(true);
                    panel.setRecordingAllowed(true);
                    break;
            }
            notifyChangeListeners();
        }
    }

    private void updateControlSize(AudioWaveformPanel panel) {
        if (panel != null) {
            //@formatter:off
            switch (controlSizeCombo.getSelectedIndex()) {
                case 0: panel.setControlPanelSize(AudioWaveformPanel.ControlPanelSize.XSMALL); break;
                case 1: panel.setControlPanelSize(AudioWaveformPanel.ControlPanelSize.SMALL); break;
                case 2: panel.setControlPanelSize(AudioWaveformPanel.ControlPanelSize.NORMAL); break;
                case 3: panel.setControlPanelSize(AudioWaveformPanel.ControlPanelSize.LARGE); break;
                case 4: panel.setControlPanelSize(AudioWaveformPanel.ControlPanelSize.XLARGE); break;
            }
            //@formatter:on
            notifyChangeListeners();
        }
    }

    private void updateControlPosition(AudioWaveformPanel panel) {
        if (panel != null) {
            //@formatter:off
            switch (controlPositionCombo.getSelectedIndex()) {
                case 0: panel.setControlPanelPosition(AudioWaveformPanel.ControlPanelPosition.TOP_LEFT); break;
                case 1: panel.setControlPanelPosition(AudioWaveformPanel.ControlPanelPosition.TOP_CENTER); break;
                case 2: panel.setControlPanelPosition(AudioWaveformPanel.ControlPanelPosition.TOP_RIGHT); break;
                case 3: panel.setControlPanelPosition(AudioWaveformPanel.ControlPanelPosition.SIDE_EDGES); break;
                case 4: panel.setControlPanelPosition(AudioWaveformPanel.ControlPanelPosition.BOTTOM_LEFT); break;
                case 5: panel.setControlPanelPosition(AudioWaveformPanel.ControlPanelPosition.BOTTOM_CENTER); break;
                case 6: panel.setControlPanelPosition(AudioWaveformPanel.ControlPanelPosition.BOTTOM_RIGHT); break;
                case 7: panel.setControlPanelPosition(AudioWaveformPanel.ControlPanelPosition.HIDDEN); break;
            }
            //@formatter:on
            notifyChangeListeners();
        }
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        JLabel spacer = new JLabel("");
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0.2;
        add(spacer, constraints);

        spacer = new JLabel("");
        constraints.gridx = 3;
        constraints.weightx = 0.8;
        add(spacer, constraints);

        JLabel label = new JLabel("Controls type:");
        Font labelFont = new Font("SansSerif", 0, 12);
        label.setFont(labelFont);
        constraints.gridx = 1;
        constraints.weightx = 0;
        constraints.insets = new Insets(2, 2, 2, 2);
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.EAST;
        add(label, constraints);

        controlTypeCombo.setEditable(false);
        controlTypeCombo.setPreferredSize(new Dimension(150, 22));
        controlTypeCombo.setFont(labelFont);
        constraints.gridx = 2;
        constraints.anchor = GridBagConstraints.WEST;
        add(controlTypeCombo, constraints);

        label = new JLabel("Controls size:");
        label.setFont(labelFont);
        constraints.gridx = 1;
        constraints.gridy++;
        constraints.anchor = GridBagConstraints.EAST;
        add(label, constraints);

        controlSizeCombo.setEditable(false);
        controlSizeCombo.setPreferredSize(new Dimension(150, 22));
        controlSizeCombo.setFont(labelFont);
        constraints.gridx = 2;
        constraints.anchor = GridBagConstraints.WEST;
        add(controlSizeCombo, constraints);

        label = new JLabel("Controls position:");
        label.setFont(labelFont);
        constraints.gridx = 1;
        constraints.gridy++;
        constraints.anchor = GridBagConstraints.WEST;
        add(label, constraints);

        controlPositionCombo.setEditable(false);
        controlPositionCombo.setPreferredSize(new Dimension(150, 22));
        controlPositionCombo.setFont(labelFont);
        constraints.gridx = 2;
        constraints.anchor = GridBagConstraints.WEST;
        add(controlPositionCombo, constraints);

        controlTypeCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateControlType(modelObject);
            }

        });

        controlSizeCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateControlSize(modelObject);
            }

        });

        controlPositionCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateControlPosition(modelObject);
            }

        });
    }
}
