package ca.corbett.forms.fields;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.gradient.ColorSelectionType;
import ca.corbett.extras.gradient.Gradient;
import ca.corbett.extras.gradient.GradientColorChooser;
import ca.corbett.extras.gradient.GradientUtil;
import ca.corbett.extras.image.ImagePanel;
import ca.corbett.extras.image.ImagePanelConfig;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * A FormField implementation for selecting a solid color.
 * See also GradientColorField in ca.corbett.extras.gradient package if you want to
 * also allow selection of color gradients in addition to solid colors.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class ColorField extends FormField {

    private final ColorSelectionType colorSelectionType;
    private final ImagePanel colorPanel;
    private Color solidColor;
    private Gradient gradient;

    /**
     * Creates a ColorField with the given color selection option.
     * The ColorSelectionType determines whether solid color values or gradient
     * values should be accepted, or both.
     */
    public ColorField(String label, ColorSelectionType colorSelectionType) {
        this(label, colorSelectionType, true);
    }

    public ColorField(String label, ColorSelectionType colorSelectionType, boolean showSolidColorInitially) {
        // Wonky case, if you pass null for colorType, we'll assume you're okay with either:
        if (colorSelectionType == null) {
            colorSelectionType = ColorSelectionType.EITHER;
        }
        this.colorSelectionType = colorSelectionType;
        this.solidColor = Color.BLACK;
        this.gradient = Gradient.createDefault();

        fieldLabel.setText(label);
        ImagePanelConfig ipc = ImagePanelConfig.createSimpleReadOnlyProperties();
        colorPanel = new ImagePanel((BufferedImage)null, ipc);
        fieldComponent = colorPanel;
        colorPanel.setPreferredSize(new Dimension(30, 20));
        colorPanel.addMouseListener(new Listener());

        if (showSolidColorInitially) {
            setColor(solidColor);
        }
        else {
            setGradient(gradient);
        }

        // We need to listen for Look and Feel changes, because if we're displaying a
        // solid color, our panel background will get overridden by the new LaF:
        LookAndFeelManager.addChangeListener(e -> {
            if (colorPanel.getImage() == null && solidColor != null) {
                colorPanel.setBackground(solidColor);
            }
        });
    }

    /**
     * Returns the current Color value for this field.
     *
     * @return The current Color value for this field.
     */
    public Color getColor() {
        return solidColor;
    }

    /**
     * Sets the current Color value for this field.
     *
     * @param color The new color.
     */
    public ColorField setColor(Color color) {
        if (color == null) {
            color = Color.BLACK;
        }
        solidColor = color;
        colorPanel.setImage(null);
        colorPanel.setBackground(color);
        fireValueChangedEvent();
        return this;
    }

    /**
     * Returns whether this ColorField allows solid color selection,
     * gradient selection, or both.
     */
    public ColorSelectionType getColorSelectionType() {
        return colorSelectionType;
    }

    /**
     * Returns the current GradientConfig for this field.
     *
     * @return The current GradientConfig for this field.
     */
    public Gradient getGradient() {
        return gradient;
    }

    /**
     * Sets the current GradientConfig value for this field.
     *
     * @param gradient The new gradient.
     */
    public ColorField setGradient(Gradient gradient) {
        if (gradient == null) {
            gradient = Gradient.createDefault();
        }
        this.gradient = gradient;
        colorPanel.setImage(GradientUtil.createGradientImage(gradient, 30, 20));
        fireValueChangedEvent();
        return this;
    }

    /**
     * Returns the selected value for this field, which may either be a Color
     * or a GradientConfig, depending on what was selected. If you created the field
     * as a solid color field via ColorField(String, Color), you can ignore this
     * method and call getColor() instead. If you created this method as a gradient-only
     * field via ColorField(String, GradientConfig), you can ignore this method and
     * call getGradient() instead. This method is for the case where either a Color
     * or a Gradient can be set, if you used ColorField(String, Color, GradientConfig, boolean).
     *
     * @return Either a Color or a GradientConfig, depending on what is currently selected.
     */
    public Object getSelectedValue() {
        return colorPanel.getImage() != null ? gradient : solidColor;
    }

    /**
     * Sets the selected value for this field, which may be either a Color
     * or a GradientConfig. If the given object is neither, this call is ignored.
     * Otherwise, this is equivalent to invoking either setColor or setGradient.
     *
     * @param obj Either a Color or a GradientConfig.
     */
    public void setSelectedValue(Object obj) {
        if (obj instanceof Color) {
            setColor((Color)obj);
        }
        else if (obj instanceof Gradient) {
            setGradient((Gradient)obj);
        }
    }

    private class Listener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (!colorPanel.isEnabled()) {
                return;
            }
            GradientColorChooser colorChooser = new GradientColorChooser(colorSelectionType);
            final Color theColor = colorPanel.getImage() == null ? solidColor : null;
            final Gradient theGradient = colorPanel.getImage() == null ? null : gradient;
            if (colorChooser.showDialog(colorPanel, "Choose colour", theColor, theGradient,
                                        false) == GradientColorChooser.OK) {
                Object value = colorChooser.getSelectedValue();
                if (value instanceof Color) {
                    setColor((Color)value);
                }
                else {
                    setGradient((Gradient)value);
                }
            }
        }
    }
}
