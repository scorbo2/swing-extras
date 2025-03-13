package ca.corbett.extras.audio;

import ca.corbett.extras.config.ConfigPanel;
import ca.corbett.extras.properties.Properties;
import java.awt.BorderLayout;
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
 * A ConfigPanel instance that allows viewing and editing settings for a WaveformConfig object.
 *
 * @author scorbo2
 * @since 2018-01-25
 */
public final class WaveformConfigPanel extends ConfigPanel<WaveformConfig> {

  // Color choosers:
  private JPanel fillColorPanel;
  private JPanel bgColorPanel;
  private JPanel baselineColorPanel;
  private JPanel outlineColorPanel;

  // Enablers:
  private JCheckBox baselineCheckBox;
  private JCheckBox outlineCheckBox;

  // Line thickness choosers:
  private JSpinner baselineWidthSpinner;
  private JSpinner outlineWidthSpinner;

  // Scale choosers:
  private static final String[] scaleChoices = {
    "xx-high",
    "x-high",
    "high",
    "normal",
    "low",
    "x-low",
    "xx-low"
  };
  private static final int[] xScaleValues = {
    8192,
    4096,
    2048,
    1024,
    512,
    256,
    128
  };
  private static final int[] yScaleValues = {
    768,
    512,
    256,
    128,
    64,
    32,
    16
  };
  private JComboBox scaleCombo;

  // Optional waveform width limiter:
  private static final String[] xLimitChoices = {
    "No limit",
    "300px",
    "500px",
    "800px",
    "1200px",
    "1600px",
    "2400px"
  };
  private static final int[] xLimitValues = {
    Integer.MAX_VALUE,
    300,
    500,
    800,
    1200,
    1600,
    2400
  };
  private JComboBox xLimitCombo;

  /**
   * Creates a new panel with the default title and with no associated WaveformConfig
   * instance. Use setModelObject to associate this panel with a prefs instance.
   */
  public WaveformConfigPanel() {
    this("Waveform preferences", null);
  }

  /**
   * Creates a new panel with the given title and with no associated WaveformConfig
   * instance. Use setModelObject to associate this panel with a prefs instance.
   *
   * @param title The title to display in the TitledBorder.
   */
  public WaveformConfigPanel(String title) {
    this(title, null);
  }

  /**
   * Creates a new panel with the default title and then associates this panel with the
   * given WaveformConfig instance. All changes made by the user in the UI will
   * immediately be pushed to the given WaveformConfig object.
   *
   * @param prefs The WaveformConfig instance to associate with this panel.
   */
  public WaveformConfigPanel(WaveformConfig prefs) {
    this("Waveform preferences", prefs);
  }

  /**
   * Creates a new panel with the given title, and then associates this panel with the
   * given WaveformConfig instance. All changes made by the user in the UI will
   * immediately be pushed to the given WaveformConfig instance.
   *
   * @param title The title to display in the TitledBorder
   * @param prefs The WaveformConfig instance to associate with this panel.
   */
  public WaveformConfigPanel(String title, WaveformConfig prefs) {
    super(title);
    initComponents();
    if (prefs != null) {
      setModelObject(prefs);
    }
  }

  /**
   * Saves all current settings to the given java.util.Properties instance using the
   * given optional parameter name prefix. This does nothing if this panel is not associated
   * with a WaveformConfig instance.
   *
   * @param props The Properties instance to which to save current settings.
   * @param prefix An optional property name prefix to use when saving.
   */
  @Override
  public void save(Properties props, String prefix) {
    if (modelObject != null) {
      modelObject.saveToProps(props, prefix);
    }
  }

  /**
   * Loads all settings from the given java.util.Properties instance using the optional
   * given parameter name prefix. If this panel is not yet associated with a WaveformConfig
   * instance, one is created and populated using the settings from preferences.
   *
   * @param props The Properties instance from which to load settings.
   * @param prefix An optional property name prefix.
   */
  @Override
  public void load(Properties props, String prefix) {
    if (modelObject == null) {
      modelObject = new WaveformConfig();
      modelObject.loadFromProps(props, prefix);
      setModelObject(modelObject);
    }
    else {
      modelObject.loadFromProps(props, prefix);
      load(modelObject);
    }
  }

  /**
   * Loads all settings from the given WaveformConfig instance. This is invoked
   * automatically from setModelObject(), but can also be invoked on its own if you
   * wish to load from the given instance without actually associating this panel with
   * that instance.
   *
   * @param obj The WaveformConfig object from which to copy settings.
   */
  @Override
  public void load(WaveformConfig obj) {
    if (modelObject != null) {
      modelObject.setFillColor(obj.getFillColor());
      modelObject.setBgColor(obj.getBgColor());
      modelObject.setBaselineColor(obj.getBaselineColor());
      modelObject.setOutlineColor(obj.getOutlineColor());
      modelObject.setBaselineEnabled(obj.isBaselineEnabled());
      modelObject.setOutlineEnabled(obj.isOutlineEnabled());
      modelObject.setBaselineThickness(obj.getBaselineThickness());
      modelObject.setOutlineThickness(obj.getOutlineThickness());
      modelObject.setXScale(obj.getXScale());
      modelObject.setYScale(obj.getYScale());
      modelObject.setXLimit(obj.getXLimit());
    }
    fillColorPanel.setBackground(obj.getFillColor());
    bgColorPanel.setBackground(obj.getBgColor());
    baselineColorPanel.setBackground(obj.getBaselineColor());
    outlineColorPanel.setBackground(obj.getOutlineColor());
    baselineCheckBox.setSelected(obj.isBaselineEnabled());
    outlineCheckBox.setSelected(obj.isOutlineEnabled());
    baselineWidthSpinner.setValue(obj.getBaselineThickness());
    outlineWidthSpinner.setValue(obj.getOutlineThickness());
    scaleCombo.setSelectedIndex(getScaleIndexFromValue(obj.getXScale(), obj.getYScale()));
    xLimitCombo.setSelectedIndex(getXLimitIndexFromValue(obj.getXLimit()));
    clearIsModified();
    notifyChangeListeners();
  }

  /**
   * Make a best attempt at converting the given x and y scale values to one of the
   * indeces in our scaleChoices combo.
   *
   * @param x The horizontal scale value
   * @param y The vertical scale value
   * @return An index into the xScaleValues and yScaleValues arrays that best matches.
   */
  private int getScaleIndexFromValue(int x, int y) {
    int index = 0;
    for (int xVal : xScaleValues) {
      if (xVal <= x) {
        return index;
      }
      index++;
    }

    return xScaleValues.length - 1;
  }

  private int getXScaleFromDescription(String description) {
    int index = 0;
    for (String v : scaleChoices) {
      if (v.equals(description)) {
        return xScaleValues[index];
      }
      index++;
    }
    return xScaleValues[2]; // arbitrary default
  }

  private int getYScaleFromDescription(String description) {
    int index = 0;
    for (String v : scaleChoices) {
      if (v.equals(description)) {
        return yScaleValues[index];
      }
      index++;
    }
    return yScaleValues[2]; // arbitrary default
  }

  private int getXLimitIndexFromValue(int val) {
    int index = 0;
    for (int limit : xLimitValues) {
      if (val == limit) {
        return index;
      }
      index++;
    }

    return 0; // default to no limit if we can't find it
  }

  private int getXLimitFromDescription(String desc) {
    int index = 0;
    for (String v : xLimitChoices) {
      if (v.equals(desc)) {
        return xLimitValues[index];
      }
      index++;
    }
    return xLimitValues[0]; // default to NO_LIMIT if we can't find it
  }

  private void initComponents() {
    setLayout(new BorderLayout());
    final JPanel contentPanel = new JPanel();
    contentPanel.setLayout(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();

    JLabel spacer = new JLabel("");
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.weightx = 0.2;
    contentPanel.add(spacer, constraints);

    spacer = new JLabel("");
    constraints.gridx = 3;
    constraints.weightx = 0.8;
    contentPanel.add(spacer, constraints);

    Font labelFont = new Font("SansSerif", 0, 12);
    JLabel label = new JLabel("Background colour:");
    label.setFont(labelFont);
    constraints.gridx = 1;
    constraints.gridy = 0;
    constraints.fill = GridBagConstraints.NONE;
    constraints.weightx = 0;
    constraints.insets = new Insets(2, 2, 2, 2);
    constraints.anchor = GridBagConstraints.EAST;
    contentPanel.add(label, constraints);

    bgColorPanel = new JPanel();
    bgColorPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
    bgColorPanel.setPreferredSize(new Dimension(30, 20));
    bgColorPanel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        JPanel srcPanel = (JPanel)e.getSource();
        Color color = srcPanel.getBackground();
        color = JColorChooser.showDialog(contentPanel, "Choose background colour", color);
        if (color != null) {
          srcPanel.setBackground(color);
          if (modelObject != null) {
            modelObject.setBgColor(color);
          }
          notifyChangeListeners();
        }
      }

    });
    constraints.gridx = 2;
    constraints.anchor = GridBagConstraints.WEST;
    contentPanel.add(bgColorPanel, constraints);

    label = new JLabel("Waveform colour:");
    label.setFont(labelFont);
    constraints.gridx = 1;
    constraints.gridy++;
    constraints.anchor = GridBagConstraints.EAST;
    contentPanel.add(label, constraints);

    fillColorPanel = new JPanel();
    fillColorPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
    fillColorPanel.setPreferredSize(new Dimension(30, 20));
    fillColorPanel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        JPanel srcPanel = (JPanel)e.getSource();
        Color color = srcPanel.getBackground();
        color = JColorChooser.showDialog(contentPanel, "Choose waveform fill colour", color);
        if (color != null) {
          srcPanel.setBackground(color);
          if (modelObject != null) {
            modelObject.setFillColor(color);
          }
          notifyChangeListeners();
        }
      }

    });
    constraints.gridx = 2;
    constraints.anchor = GridBagConstraints.WEST;
    contentPanel.add(fillColorPanel, constraints);

    label = new JLabel("Outline colour:");
    label.setFont(labelFont);
    constraints.gridx = 1;
    constraints.gridy++;
    constraints.anchor = GridBagConstraints.EAST;
    contentPanel.add(label, constraints);

    outlineColorPanel = new JPanel();
    outlineColorPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
    outlineColorPanel.setPreferredSize(new Dimension(30, 20));
    outlineColorPanel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        JPanel srcPanel = (JPanel)e.getSource();
        Color color = srcPanel.getBackground();
        color = JColorChooser.showDialog(contentPanel, "Choose waveform outline colour", color);
        if (color != null) {
          srcPanel.setBackground(color);
          if (modelObject != null) {
            modelObject.setOutlineColor(color);
          }
          notifyChangeListeners();
        }
      }

    });
    constraints.gridx = 2;
    constraints.anchor = GridBagConstraints.WEST;
    contentPanel.add(outlineColorPanel, constraints);

    label = new JLabel("Baseline colour:");
    label.setFont(labelFont);
    constraints.gridx = 1;
    constraints.gridy++;
    constraints.anchor = GridBagConstraints.EAST;
    contentPanel.add(label, constraints);

    baselineColorPanel = new JPanel();
    baselineColorPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
    baselineColorPanel.setPreferredSize(new Dimension(30, 20));
    baselineColorPanel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        JPanel srcPanel = (JPanel)e.getSource();
        Color color = srcPanel.getBackground();
        color = JColorChooser.showDialog(contentPanel, "Choose baseline colour", color);
        if (color != null) {
          srcPanel.setBackground(color);
          if (modelObject != null) {
            modelObject.setBaselineColor(color);
          }
          notifyChangeListeners();
        }
      }

    });
    constraints.gridx = 2;
    constraints.anchor = GridBagConstraints.WEST;
    contentPanel.add(baselineColorPanel, constraints);

    label = new JLabel("Baseline thickness:");
    label.setFont(labelFont);
    constraints.gridx = 1;
    constraints.gridy++;
    constraints.anchor = GridBagConstraints.EAST;
    contentPanel.add(label, constraints);

    baselineWidthSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 10, 1));
    baselineWidthSpinner.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        JSpinner spin = (JSpinner)e.getSource();
        if (modelObject != null) {
          modelObject.setBaselineThickness((Integer)spin.getValue());
        }
        notifyChangeListeners();
      }

    });
    constraints.gridx = 2;
    constraints.anchor = GridBagConstraints.WEST;
    contentPanel.add(baselineWidthSpinner, constraints);

    label = new JLabel("Outline thickness:");
    label.setFont(labelFont);
    constraints.gridx = 1;
    constraints.gridy++;
    constraints.anchor = GridBagConstraints.EAST;
    contentPanel.add(label, constraints);

    outlineWidthSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 10, 1));
    outlineWidthSpinner.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        JSpinner spin = (JSpinner)e.getSource();
        if (modelObject != null) {
          modelObject.setOutlineThickness((Integer)spin.getValue());
        }
        notifyChangeListeners();
      }

    });
    constraints.gridx = 2;
    constraints.anchor = GridBagConstraints.WEST;
    contentPanel.add(outlineWidthSpinner, constraints);

    baselineCheckBox = new JCheckBox("Draw zero baseline");
    baselineCheckBox.setFont(labelFont);
    baselineCheckBox.setSelected(true);
    baselineCheckBox.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (modelObject != null) {
          modelObject.setBaselineEnabled(((JCheckBox)e.getSource()).isSelected());
        }
        notifyChangeListeners();
      }

    });
    constraints.gridx = 1;
    constraints.gridy++;
    constraints.gridwidth = 2;
    constraints.anchor = GridBagConstraints.WEST;
    contentPanel.add(baselineCheckBox, constraints);

    outlineCheckBox = new JCheckBox("Draw wave outline");
    outlineCheckBox.setFont(labelFont);
    outlineCheckBox.setSelected(true);
    outlineCheckBox.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (modelObject != null) {
          modelObject.setOutlineEnabled(((JCheckBox)e.getSource()).isSelected());
        }
        notifyChangeListeners();
      }

    });
    constraints.gridx = 1;
    constraints.gridy++;
    contentPanel.add(outlineCheckBox, constraints);

    label = new JLabel("Compression:");
    label.setFont(labelFont);
    constraints.gridx = 1;
    constraints.gridwidth = 1;
    constraints.gridy++;
    constraints.anchor = GridBagConstraints.EAST;
    contentPanel.add(label, constraints);

    scaleCombo = new JComboBox(scaleChoices);
    scaleCombo.setEditable(false);
    scaleCombo.setFont(labelFont);
    scaleCombo.setSelectedIndex(2); // arbitrary
    scaleCombo.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (modelObject != null) {
          String value = (String)((JComboBox)e.getSource()).getSelectedItem();
          modelObject.setXScale(getXScaleFromDescription(value));
          modelObject.setYScale(getYScaleFromDescription(value));
        }
        notifyChangeListeners();
      }

    });
    constraints.gridx = 2;
    constraints.anchor = GridBagConstraints.WEST;
    contentPanel.add(scaleCombo, constraints);

    label = new JLabel("Width limit:");
    label.setFont(labelFont);
    constraints.gridx = 1;
    constraints.gridwidth = 1;
    constraints.gridy++;
    constraints.anchor = GridBagConstraints.EAST;
    contentPanel.add(label, constraints);

    xLimitCombo = new JComboBox(xLimitChoices);
    xLimitCombo.setEditable(false);
    xLimitCombo.setFont(labelFont);
    xLimitCombo.setSelectedIndex(0); // no limit
    xLimitCombo.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (modelObject != null) {
          String value = (String)((JComboBox)e.getSource()).getSelectedItem();
          modelObject.setXLimit(getXLimitFromDescription(value));
        }
        notifyChangeListeners();
      }

    });
    constraints.gridx = 2;
    constraints.anchor = GridBagConstraints.WEST;
    contentPanel.add(xLimitCombo, constraints);

    add(contentPanel, BorderLayout.CENTER);
  }

}
