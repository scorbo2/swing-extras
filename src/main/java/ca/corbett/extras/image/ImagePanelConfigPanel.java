package ca.corbett.extras.image;

import ca.corbett.extras.config.ConfigPanel;
import ca.corbett.extras.properties.Properties;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Provides a ConfigPanel implementation for viewing/editing ImagePanelConfig instances.
 *
 * @author scorbett
 * @since 2018-01-26
 */
public class ImagePanelConfigPanel extends ConfigPanel<ImagePanelConfig> {

    private static final String[] displayModes = {
            "None",
            "Center",
            "Best fit",
            "Stretch"
    };
    private final JComboBox displayModeCombo = new JComboBox(displayModes);

    private static final String[] renderModes = {
            "Quick and dirty",
            "Slow and accurate"
    };
    private final JComboBox renderQualityCombo = new JComboBox(renderModes);

    private JPanel bgColorPanel;
    private JCheckBox mouseCursorCheckBox;
    private JCheckBox mouseDraggingCheckBox;
    private JSpinner zoomIncrementSpinner;
    private JCheckBox zoomOnClickCheckBox;
    private JCheckBox zoomOnWheelCheckBox;

    public ImagePanelConfigPanel(String title) {
        this(title, null);
    }

    public ImagePanelConfigPanel(String title, ImagePanelConfig props) {
        super(title);
        initComponents();
        if (props != null) {
            setModelObject(props);
        }
    }

    @Override
    public void save(Properties props, String prefix) {
        if (modelObject != null) {
            modelObject.saveToProps(props, prefix);
        }
    }

    @Override
    public void load(Properties props, String prefix) {
        if (modelObject == null) {
            modelObject = ImagePanelConfig.createDefaultProperties();
            modelObject.loadFromProps(props, prefix);
            setModelObject(modelObject);
        }
        else {
            modelObject.loadFromProps(props, prefix);
            load(modelObject);
        }
    }

    @Override
    public void load(ImagePanelConfig obj) {
        if (modelObject != null) {
            modelObject.setDisplayMode(obj.getDisplayMode());
            modelObject.setRenderingQuality(obj.getRenderingQuality());
            modelObject.setBgColor(obj.getBgColor());
            modelObject.setEnableMouseCursor(obj.isEnableMouseCursor());
            modelObject.setEnableMouseDragging(obj.isEnableMouseDragging());
            modelObject.setEnableZoomOnMouseClick(obj.isEnableZoomOnMouseClick());
            modelObject.setEnableZoomOnMouseWheel(obj.isEnableZoomOnMouseWheel());
            modelObject.setZoomFactorIncrement(obj.getZoomFactorIncrement());
        }

        switch (obj.getDisplayMode()) {
            case CENTER:
                displayModeCombo.setSelectedIndex(1);
                break;
            case BEST_FIT:
                displayModeCombo.setSelectedIndex(2);
                break;
            case STRETCH:
                displayModeCombo.setSelectedIndex(3);
                break;

            case NONE:
            default:
                displayModeCombo.setSelectedIndex(0);
                break;
        }

        switch (obj.getRenderingQuality()) {
            case QUICK_AND_DIRTY:
                renderQualityCombo.setSelectedIndex(0);
                break;
            case SLOW_AND_ACCURATE:
                renderQualityCombo.setSelectedIndex(1);
                break;
        }

        bgColorPanel.setBackground(obj.getBgColor());
        mouseCursorCheckBox.setSelected(obj.isEnableMouseCursor());
        mouseDraggingCheckBox.setSelected(obj.isEnableMouseDragging());
        zoomIncrementSpinner.setValue(obj.getZoomFactorIncrement());
        zoomOnClickCheckBox.setSelected(obj.isEnableZoomOnMouseClick());
        zoomOnWheelCheckBox.setSelected(obj.isEnableZoomOnMouseWheel());

        notifyChangeListeners();
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

        JLabel label = new JLabel("Background:");
        Font labelFont = new Font("SansSerif", 0, 12);
        label.setFont(labelFont);
        constraints.gridx = 1;
        constraints.insets = new Insets(2, 2, 2, 2);
        constraints.anchor = GridBagConstraints.EAST;
        constraints.fill = GridBagConstraints.NONE;
        add(label, constraints);

        bgColorPanel = new JPanel();
        bgColorPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        bgColorPanel.setPreferredSize(new Dimension(30, 20));
        final JPanel thisPanel = this;
        bgColorPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JPanel srcPanel = (JPanel)e.getSource();
                Color color = srcPanel.getBackground();
                color = JColorChooser.showDialog(thisPanel, "Choose background colour", color);
                if (color != null) {
                    srcPanel.setBackground(color);
                    if (modelObject != null) {
                        modelObject.setBgColor(color);
                        notifyChangeListeners();
                    }
                }
            }

        });
        constraints.gridx = 2;
        constraints.anchor = GridBagConstraints.WEST;
        add(bgColorPanel, constraints);

        label = new JLabel("Display mode:");
        label.setFont(labelFont);
        constraints.gridx = 1;
        constraints.gridy++;
        constraints.anchor = GridBagConstraints.EAST;
        add(label, constraints);

        displayModeCombo.setEditable(false);
        displayModeCombo.setFont(labelFont);
        displayModeCombo.setPreferredSize(new Dimension(160, 24));
        displayModeCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (modelObject != null) {
                    String value = (String)displayModeCombo.getSelectedItem();
                    switch (value) {
                        case "None":
                            modelObject.setDisplayMode(ImagePanelConfig.DisplayMode.NONE);
                            break;
                        case "Center":
                            modelObject.setDisplayMode(ImagePanelConfig.DisplayMode.CENTER);
                            break;
                        case "Best fit":
                            modelObject.setDisplayMode(ImagePanelConfig.DisplayMode.BEST_FIT);
                            break;
                        case "Stretch":
                            modelObject.setDisplayMode(ImagePanelConfig.DisplayMode.STRETCH);
                            break;
                    }
                    notifyChangeListeners();
                }
            }

        });
        constraints.gridx = 2;
        constraints.anchor = GridBagConstraints.WEST;
        add(displayModeCombo, constraints);

        label = new JLabel("Render quality:");
        label.setFont(labelFont);
        constraints.gridx = 1;
        constraints.gridy++;
        constraints.anchor = GridBagConstraints.EAST;
        add(label, constraints);

        renderQualityCombo.setEditable(false);
        renderQualityCombo.setFont(labelFont);
        renderQualityCombo.setPreferredSize(new Dimension(160, 24));
        renderQualityCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (modelObject != null) {
                    String value = (String)displayModeCombo.getSelectedItem();
                    if (value == null) {
                        value = "Slow and accurate";
                    }
                    switch (value) {
                        case "Quick and dirty":
                            modelObject.setRenderingQuality(ImagePanelConfig.Quality.QUICK_AND_DIRTY);
                            break;

                        case "Slow and accurate":
                            modelObject.setRenderingQuality(ImagePanelConfig.Quality.SLOW_AND_ACCURATE);
                            break;
                    }
                    notifyChangeListeners();
                }
            }

        });
        constraints.gridx = 2;
        constraints.anchor = GridBagConstraints.WEST;
        add(renderQualityCombo, constraints);

        label = new JLabel("Zoom increment:");
        label.setFont(labelFont);
        constraints.gridx = 1;
        constraints.gridy++;
        constraints.anchor = GridBagConstraints.EAST;
        add(label, constraints);

        zoomIncrementSpinner = new JSpinner(new SpinnerNumberModel(0.1, 0.01, 1.0, 0.01));
        zoomIncrementSpinner.setFont(labelFont);
        zoomIncrementSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (modelObject != null) {
                    Double value = (Double)((JSpinner)e.getSource()).getValue();
                    modelObject.setZoomFactorIncrement(value);
                    notifyChangeListeners();
                }
            }

        });
        constraints.gridx = 2;
        constraints.anchor = GridBagConstraints.WEST;
        add(zoomIncrementSpinner, constraints);

        mouseCursorCheckBox = new JCheckBox("Enable mouse cursor");
        mouseCursorCheckBox.setFont(labelFont);
        mouseCursorCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (modelObject != null) {
                    modelObject.setEnableMouseCursor(((JCheckBox)e.getSource()).isSelected());
                    notifyChangeListeners();
                }
            }

        });
        constraints.gridx = 2;
        constraints.gridy++;
        constraints.anchor = GridBagConstraints.WEST;
        add(mouseCursorCheckBox, constraints);

        mouseDraggingCheckBox = new JCheckBox("Enable mouse dragging");
        mouseDraggingCheckBox.setFont(labelFont);
        mouseDraggingCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (modelObject != null) {
                    modelObject.setEnableMouseDragging(((JCheckBox)e.getSource()).isSelected());
                    notifyChangeListeners();
                }
            }

        });
        constraints.gridy++;
        add(mouseDraggingCheckBox, constraints);

        zoomOnClickCheckBox = new JCheckBox("Zoom on mouse click");
        zoomOnClickCheckBox.setFont(labelFont);
        zoomOnClickCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (modelObject != null) {
                    modelObject.setEnableZoomOnMouseClick(((JCheckBox)e.getSource()).isSelected());
                    notifyChangeListeners();
                }
            }

        });
        constraints.gridy++;
        add(zoomOnClickCheckBox, constraints);

        zoomOnWheelCheckBox = new JCheckBox("Zoom on mouse wheel");
        zoomOnWheelCheckBox.setFont(labelFont);
        zoomOnWheelCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (modelObject != null) {
                    modelObject.setEnableZoomOnMouseWheel(((JCheckBox)e.getSource()).isSelected());
                    notifyChangeListeners();
                }
            }

        });
        constraints.gridy++;
        add(zoomOnWheelCheckBox, constraints);
    }

}
