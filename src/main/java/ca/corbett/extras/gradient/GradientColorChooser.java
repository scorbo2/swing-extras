package ca.corbett.extras.gradient;

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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Combines a JColorChooser with a GradientConfigDialog to allow you to select
 * either a solid color or a gradient.
 *
 * @author scorbo2
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
        gradientChooser.setModelObject(new GradientConfig());
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
        dialog.setSize(new Dimension(500, 300));
        dialog.setMinimumSize(new Dimension(500, 300));
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

}
