package ca.corbett.extras.gradient;

import ca.corbett.extras.image.ImagePanel;
import ca.corbett.extras.image.ImagePanelConfig;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.ColorField;
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

    private final JColorChooser colorChooser;
    private final GradientConfigPanel gradientChooser;
    private final ColorSelectionType selectionMode;
    private JTabbedPane tabPane;
    private int dialogResult = OK;

    public GradientColorChooser() {
        this(ColorSelectionType.SOLID);
    }

    public GradientColorChooser(ColorSelectionType selectionMode) {
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

    public void setGradient(Gradient gradient) {
        gradientChooser.setGradient(gradient);
    }

    public void resetToDefaults() {
        colorChooser.setColor(Color.BLACK);
        gradientChooser.setGradient(Gradient.createDefault());
    }

    public Color getSelectedColor() {
        return colorChooser.getColor();
    }

    public GradientType getGradientType() {
        return gradientChooser.getGradientType();
    }

    public Color getGradientColor1() {
        return gradientChooser.getGradientColor1();
    }

    public Color getGradientColor2() {
        return gradientChooser.getGradientColor2();
    }

    public Object getSelectedValue() {
        if (tabPane.getSelectedComponent() == gradientChooser) {
            return gradientChooser.getGradient();
        }
        else {
            return colorChooser.getColor();
        }
    }

    private void initComponents() {
        tabPane = new JTabbedPane();

        if (selectionMode == ColorSelectionType.SOLID || selectionMode == ColorSelectionType.EITHER) {
            for (AbstractColorChooserPanel panel : colorChooser.getChooserPanels()) {
                tabPane.addTab(panel.getDisplayName(), panel);
            }
        }

        if (selectionMode == ColorSelectionType.GRADIENT || selectionMode == ColorSelectionType.EITHER) {
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

    public int showDialog(Component owner, String title, Gradient gradient) {
        return showDialog(findParentWindow(owner), title, null, gradient, false);
    }

    public int showDialog(Component owner, String title, Color color, Gradient gradient, boolean displaySolidColorInitially) {
        return showDialog(findParentWindow(owner), title, color, gradient, displaySolidColorInitially);
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

    public int showDialog(Window owner, String title, Gradient gradient) {
        return showDialog(owner, title, null, gradient, false);
    }

    public int showDialog(Window owner, String title, Color color, Gradient gradient, boolean displaySolidColorInitially) {
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

        private ImagePanel previewPanel;
        private ColorField color1Field;
        private ColorField color2Field;
        private ComboField<GradientType> gradientTypeCombo;

        public GradientConfigPanel(String title) {
            this(title, null);
        }

        public GradientConfigPanel(String title, Gradient gradient) {
            if (gradient == null) {
                gradient = new Gradient(GradientType.VERTICAL_LINEAR, Color.WHITE, Color.BLACK);
            }

            gradientTypeCombo = new ComboField<>("Type:", List.of(GradientType.values()), 0);
            gradientTypeCombo.setSelectedItem(gradient.type());
            gradientTypeCombo.addValueChangedListener(e -> updatePreview());

            color1Field = new ColorField("Color 1:", ColorSelectionType.SOLID);
            color1Field.setColor(gradient.color1());
            color1Field.addValueChangedListener(e -> updatePreview());

            color2Field = new ColorField("Color 2:", ColorSelectionType.SOLID);
            color2Field.setColor(gradient.color2());
            color2Field.addValueChangedListener(e -> updatePreview());

            initComponents();
        }

        public Gradient getGradient() {
            return new Gradient(getGradientType(), getGradientColor1(), getGradientColor2());
        }

        public GradientType getGradientType() {
            return gradientTypeCombo.getSelectedItem();
        }

        public Color getGradientColor1() {
            return color1Field.getColor();
        }

        public Color getGradientColor2() {
            return color2Field.getColor();
        }

        public void setGradient(Gradient gradient) {
            setGradientType(gradient.type());
            setGradientColor1(gradient.color1());
            setGradientColor2(gradient.color2());
        }

        public void setGradientType(GradientType t) {
            gradientTypeCombo.setSelectedItem(t);
            updatePreview();
        }

        public void setGradientColor1(Color c1) {
            color1Field.setColor(c1);
            updatePreview();
        }

        public void setGradientColor2(Color c2) {
            color2Field.setColor(c2);
            updatePreview();
        }

        private void initComponents() {
            setLayout(new BorderLayout());

            FormPanel formPanel = new FormPanel(Alignment.TOP_CENTER);

            LabelField labelField = new LabelField("Gradient configuration");
            labelField.setFont(FormField.getDefaultFont().deriveFont(Font.BOLD, 14f));
            formPanel.add(labelField);

            formPanel.add(gradientTypeCombo);
            formPanel.add(color1Field);
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
                    color2Field.setSelectedValue(obj1);
                    updatePreview();
                }
            });
            panelField.getPanel().add(button);
            formPanel.add(panelField);

            add(formPanel, BorderLayout.WEST);

            ImagePanelConfig iPanelConf = ImagePanelConfig.createSimpleReadOnlyProperties();
            iPanelConf.setDisplayMode(ImagePanelConfig.DisplayMode.STRETCH);
            previewPanel = new ImagePanel(
                    GradientUtil.createGradientImage(getGradient(), PREVIEW_WIDTH, PREVIEW_HEIGHT),
                    iPanelConf);
            add(previewPanel, BorderLayout.CENTER);
        }

        private void updatePreview() {
            previewPanel.setImage(GradientUtil.createGradientImage(getGradient(), PREVIEW_WIDTH, PREVIEW_HEIGHT));
        }

    }

}
