package ca.corbett.extras.gradient;

import ca.corbett.extras.image.ImagePanel;
import ca.corbett.extras.image.ImagePanelConfig;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.PanelField;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Combines a JColorChooser with a GradientConfigDialog to allow you to select
 * either a solid color or a gradient.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public final class GradientColorChooser extends JPanel {

    public static final int OK = 0;
    public static final int CANCEL = 1;

    public enum SelectionMode {
        SOLID_COLOR,
        GRADIENT,
        BOTH;

        public boolean includesSolidColor() {
            return this == SOLID_COLOR || this == BOTH;
        }

        public boolean includesGradient() {
            return this == GRADIENT || this == BOTH;
        }

    }

    private final JColorChooser colorChooser;
    private final GradientConfigPanel gradientChooser;
    private final SelectionMode selectionMode;
    private JTabbedPane tabPane;
    private int dialogResult = OK;

    public GradientColorChooser() {
        this(SelectionMode.SOLID_COLOR);
    }

    public GradientColorChooser(SelectionMode selectionMode) {
        colorChooser = new JColorChooser();
        gradientChooser = new GradientConfigPanel("Gradient");
        gradientChooser.setBorder(null);
        this.selectionMode = selectionMode;
        resetToDefaults();
        initComponents();
    }

    public void setColor(Color c) {
        colorChooser.setColor(c);
    }

    public void setGradient(GradientConfig gradient) {
        gradientChooser.load(gradient);
    }

    public void resetToDefaults() {
        colorChooser.setColor(Color.BLACK);
        gradientChooser.load(new GradientConfig());
    }

    public Color getSelectedColor() {
        return colorChooser.getColor();
    }

    public GradientConfig getSelectedGradient() {
        return gradientChooser.getSelectedValue();
    }

    public Object getSelectedValue() {
        if (tabPane.getSelectedComponent() == gradientChooser) {
            return gradientChooser.getSelectedValue();
        }
        else {
            return colorChooser.getColor();
        }
    }

    private void initComponents() {
        tabPane = new JTabbedPane();

        if (selectionMode.includesSolidColor()) {
            for (AbstractColorChooserPanel panel : colorChooser.getChooserPanels()) {
                tabPane.addTab(panel.getDisplayName(), panel);
            }
        }

        if (selectionMode.includesGradient()) {
            tabPane.addTab("Gradient", gradientChooser);
        }

        setLayout(new BorderLayout());
        add(tabPane, BorderLayout.CENTER);
    }

    public int showDialog(Component owner) {
        return showDialog(findParentWindow(owner), "Color chooser", null, null, false);
    }

    public int showDialog(Component owner, String title) {
        return showDialog(findParentWindow(owner), title, null, null, false);
    }

    public int showDialog(Component owner, String title, Color color) {
        return showDialog(findParentWindow(owner), title, color, null, false);
    }

    public int showDialog(Component owner, String title, GradientConfig gradient) {
        return showDialog(findParentWindow(owner), title, null, gradient, false);
    }

    public int showDialog(Component owner, String title, Color color, GradientConfig config, boolean displaySolidColorInitially) {
        return showDialog(findParentWindow(owner), title, color, config, displaySolidColorInitially);
    }

    public int showDialog(Window owner) {
        return showDialog(owner, "Color chooser", null, null, false);
    }

    public int showDialog(Window owner, String title) {
        return showDialog(owner, title, null, null, false);
    }

    public int showDialog(Window owner, String title, Color color) {
        return showDialog(owner, title, color, null, false);
    }

    public int showDialog(Window owner, String title, GradientConfig gradient) {
        return showDialog(owner, title, null, gradient, false);
    }

    public int showDialog(Window owner, String title, Color color, GradientConfig gradient, boolean displaySolidColorInitially) {
        // Safeguard: if both were null, show solid color:
        if (color == null && gradient == null) {
            color = Color.BLACK;
        }

        // If both were passed in, decide which to show first:
        if (color != null && gradient != null) {
            setColor(color);
            setGradient(gradient);
            if (displaySolidColorInitially) {
                tabPane.setSelectedIndex(0);
            }
            else {
                tabPane.setSelectedComponent(gradientChooser);
            }
        }
        // If just color was passed in, show color chooser:
        else if (color != null) {
            setColor(color);
            tabPane.setSelectedIndex(0);
        }
        // If just gradient was passed in, show gradient chooser:
        else if (gradient != null) {
            setGradient(gradient);
            tabPane.setSelectedComponent(gradientChooser);
        }

        dialogResult = CANCEL; // safe default
        final JDialog dialog = new JDialog(owner, title);
        dialog.setModalityType(JDialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(new Dimension(640, 350));
        dialog.setMinimumSize(new Dimension(640, 350));
        dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        dialog.setLayout(new BorderLayout());
        dialog.add(this, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        JButton button = new JButton("OK");
        button.setPreferredSize(new Dimension(90, 23));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialogResult = OK;
                dialog.dispose();
            }

        });
        buttonPanel.add(button);

        button = new JButton("Cancel");
        button.setPreferredSize(new Dimension(90, 23));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialogResult = CANCEL;
                dialog.dispose();
            }

        });
        buttonPanel.add(button);

        button = new JButton("Reset");
        button.setPreferredSize(new Dimension(90, 23));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetToDefaults();
            }

        });
        buttonPanel.add(button);

        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
        return dialogResult;
    }

    private Window findParentWindow(Component panel) {
        Component candidate = panel.getParent();
        while (candidate != null) {
            if (candidate instanceof Window) {
                return (Window)candidate;
            }
            candidate = candidate.getParent();
        }
        return null;
    }

    public static class GradientConfigPanel extends JPanel {

        private static final int PREVIEW_WIDTH = 500;
        private static final int PREVIEW_HEIGHT = 500;

        private GradientConfig gradientConfig;
        private ImagePanel previewPanel;
        private GradientColorField color1Field;
        private GradientColorField color2Field;
        private ComboField<GradientUtil.GradientType> gradientTypeCombo;

        public GradientConfigPanel(String title) {
            this(title, new GradientConfig());
        }

        public GradientConfigPanel(String title, GradientConfig config) {
            this.gradientConfig = config == null ? new GradientConfig() : config;
            initComponents();
        }

        public GradientConfig getSelectedValue() {
            return new GradientConfig(gradientConfig);
        }

        /**
         * Loads settings from the given GradientConfig.
         */
        public void load(GradientConfig obj) {
            gradientConfig = new GradientConfig(obj);
            if (color1Field != null) {
                color1Field.setColor(obj.getColor1());
                color2Field.setColor(obj.getColor2());
                gradientTypeCombo.setSelectedItem(obj.getGradientType());
                updatePreview();
            }
        }

        private void initComponents() {
            setLayout(new BorderLayout());

            FormPanel formPanel = new FormPanel(Alignment.TOP_CENTER);

            LabelField labelField = new LabelField("Gradient configuration");
            labelField.setFont(FormField.getDefaultFont().deriveFont(Font.BOLD, 14f));
            formPanel.add(labelField);

            gradientTypeCombo = new ComboField<>("Type:", List.of(GradientUtil.GradientType.values()), 0, false);
            gradientTypeCombo.addValueChangedListener(field -> {
                gradientConfig.setGradientType(gradientTypeCombo.getSelectedItem());
                updatePreview();
            });
            formPanel.add(gradientTypeCombo);

            color1Field = new GradientColorField("Color 1:", gradientConfig.getColor1());
            color1Field.addValueChangedListener(field -> {
                gradientConfig.setColor1(color1Field.getColor());
                updatePreview();
            });
            formPanel.add(color1Field);

            color2Field = new GradientColorField("Color 2:", gradientConfig.getColor2());
            color2Field.addValueChangedListener(field -> {
                gradientConfig.setColor2(color2Field.getColor());
                updatePreview();
            });
            formPanel.add(color2Field);

            PanelField panelField = new PanelField();
            panelField.getPanel().setLayout(new FlowLayout(FlowLayout.CENTER));
            JButton button = new JButton("Swap colours");
            button.setPreferredSize(new Dimension(140, 23));
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Object obj1 = color1Field.getSelectedValue();
                    color1Field.setSelectedValue(color2Field.getSelectedValue());
                    gradientConfig.setColor1(color2Field.getColor());
                    color2Field.setSelectedValue(obj1);
                    gradientConfig.setColor2((Color)obj1);
                    updatePreview();
                }

            });
            panelField.getPanel().add(button);
            formPanel.add(panelField);

            add(formPanel, BorderLayout.WEST);

            ImagePanelConfig iPanelConf = ImagePanelConfig.createSimpleReadOnlyProperties();
            iPanelConf.setDisplayMode(ImagePanelConfig.DisplayMode.STRETCH);
            previewPanel = new ImagePanel(
                    GradientUtil.createGradientImage(gradientConfig, PREVIEW_WIDTH, PREVIEW_HEIGHT),
                    iPanelConf);
            add(previewPanel, BorderLayout.CENTER);
        }

        private void updatePreview() {
            previewPanel.setImage(GradientUtil.createGradientImage(gradientConfig, PREVIEW_WIDTH, PREVIEW_HEIGHT));
        }

    }

}
