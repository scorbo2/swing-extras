package ca.corbett.forms.fields;

import javax.swing.JColorChooser;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A FormField implementation for selecting a solid color.
 * See also GradientColorField in ca.corbett.extras.gradient package if you want to
 * also allow selection of color gradients in addition to solid colors.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class ColorField extends FormField {

    private Color selectedColor;
    private final JPanel colorPanel;

    /**
     * Creates a ColorField that can be used to select a solid color only.
     *
     * @param label        The label to use with the field.
     * @param initialColor The starting colour.
     */
    public ColorField(String label, Color initialColor) {
        // Wonky case, if you pass null for initial colour, you get black:
        if (initialColor == null) {
            initialColor = Color.BLACK;
        }

        // Initialize our UI components:
        fieldLabel.setText(label);
        colorPanel = new JPanel();
        fieldComponent = colorPanel;
        colorPanel.setPreferredSize(new Dimension(30, 20));
        selectedColor = initialColor;
        colorPanel.setBackground(initialColor);
        colorPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!colorPanel.isEnabled()) {
                    return;
                }
                setColor(JColorChooser.showDialog(((JPanel)e.getSource()), "Choose color", selectedColor));
            }
        });
    }

    /**
     * Returns the current Color value for this field.
     *
     * @return The current Color value for this field.
     */
    public Color getColor() {
        return selectedColor;
    }

    /**
     * Sets the current Color value for this field.
     *
     * @param newColor The new newColor. Null values are ignored.
     */
    public ColorField setColor(Color newColor) {
        if (newColor != null && !selectedColor.equals(newColor)) {
            selectedColor = newColor;
            colorPanel.setBackground(newColor);
            fireValueChangedEvent();
        }
        return this;
    }
}
