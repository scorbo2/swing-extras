package ca.corbett.extras.gradient;

import ca.corbett.extras.image.ImagePanel;
import ca.corbett.extras.image.ImagePanelConfig;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.FormField;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * A FormField implementation for selecting either a solid color, or a gradient fill.
 *
 * @author scorbett
 */
public class GradientColorField extends FormField {

    private final ImagePanel colorPanel;
    private final GradientColorChooser.SelectionMode selectionMode;
    private Color selectedColor;
    private GradientConfig selectedGradient;

    /**
     * Creates a ColorField that can be used to select a solid color only.
     *
     * @param label        The label to use with the field.
     * @param initialColor The starting colour.
     */
    public GradientColorField(String label, Color initialColor) {
        this(label, initialColor, null, false);
    }

    /**
     * Creates a ColorField that can be used to select a color gradient only.
     *
     * @param label           The label to use with the field.
     * @param initialGradient The starting gradient.
     */
    public GradientColorField(String label, GradientConfig initialGradient) {
        this(label, null, initialGradient, false);
    }

    /**
     * Creates a ColorField that can be used to select either a solid color or
     * a color gradient.
     *
     * @param label                   The label to use with the field.
     * @param initialColor            The starting solid color.
     * @param initialGradient         The starting gradient.
     * @param showSolidColorInitially If both initialColor and initialGradient are supplied, this
     *                                parameter decides which is shown initially.
     */
    public GradientColorField(String label, Color initialColor, GradientConfig initialGradient, boolean showSolidColorInitially) {
        // Wonky case, if you pass null for both initial values, we'll assume you're okay with either:
        if (initialColor == null && initialGradient == null) {
            initialColor = Color.BLACK;
            initialGradient = new GradientConfig();
        }

        // Initialize our UI components:
        fieldLabel = new JLabel(label);
        fieldLabel.setFont(fieldLabelFont);
        ImagePanelConfig ipc = ImagePanelConfig.createSimpleReadOnlyProperties();
        colorPanel = new ImagePanel((BufferedImage)null, ipc);
        fieldComponent = colorPanel;
        colorPanel.setPreferredSize(new Dimension(30, 20));

        // Otherwise, figure out if we're a solid color chooser or a gradient chooser, or both:
        if (initialColor != null && initialGradient != null) {
            selectionMode = GradientColorChooser.SelectionMode.BOTH;
            selectedColor = initialColor;
            selectedGradient = new GradientConfig(initialGradient);
            if (showSolidColorInitially) {
                colorPanel.setBackground(initialColor);
            }
            else {
                colorPanel.setImage(GradientUtil.createGradientImage(initialGradient, 30, 20));
            }
        }
        else if (initialColor != null) {
            selectionMode = GradientColorChooser.SelectionMode.SOLID_COLOR;
            selectedColor = initialColor;
            colorPanel.setBackground(initialColor);
        }
        else {
            selectionMode = GradientColorChooser.SelectionMode.GRADIENT;
            selectedGradient = new GradientConfig(initialGradient);
            colorPanel.setImage(GradientUtil.createGradientImage(initialGradient, 30, 20));
        }
        colorPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!colorPanel.isEnabled()) {
                    return;
                }
                ImagePanel srcPanel = (ImagePanel)e.getSource();
                GradientColorChooser colorChooser = new GradientColorChooser(selectionMode);
                final Color theColor = colorPanel.getImage() == null ? selectedColor : null;
                final GradientConfig theGradient = colorPanel.getImage() == null ? null : selectedGradient;
                if (colorChooser.showDialog(colorPanel, "Choose colour", theColor, theGradient,
                                            false) == GradientColorChooser.OK) {
                    Object value = colorChooser.getSelectedValue();
                    if (value instanceof Color) {
                        selectedColor = (Color)value;
                        srcPanel.setImage(null);
                        srcPanel.setBackground(selectedColor);
                    }
                    else {
                        selectedGradient = (GradientConfig)value;
                        srcPanel.setImage(GradientUtil.createGradientImage(selectedGradient, 30, 20));
                    }
                    fireValueChangedEvent();
                }
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
     * @param color The new color.
     */
    public void setColor(Color color) {
        selectedColor = color;
        colorPanel.setImage(null);
        if (selectionMode.includesSolidColor()) {
            colorPanel.setBackground(color);
        }
    }

    /**
     * Returns the current GradientConfig for this field.
     *
     * @return The current GradientConfig for this field.
     */
    public GradientConfig getGradient() {
        return new GradientConfig(selectedGradient);
    }

    /**
     * Sets the current GradientConfig value for this field.
     *
     * @param gradient The new gradient.
     */
    public void setGradient(GradientConfig gradient) {
        selectedGradient = new GradientConfig(gradient);
        if (selectionMode.includesGradient()) {
            colorPanel.setImage(GradientUtil.createGradientImage(selectedGradient, 30, 20));
        }
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
        return colorPanel.getImage() != null ? selectedGradient : selectedColor;
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
        else if (obj instanceof GradientConfig) {
            setGradient((GradientConfig)obj);
        }
    }

    /**
     * Renders this field into the given container.
     *
     * @param container   The containing form panel.
     * @param constraints The GridBagConstraints to use.
     */
    @Override
    public void render(JPanel container, GridBagConstraints constraints) {
        constraints.insets = new Insets(topMargin, leftMargin, bottomMargin, componentSpacing);
        constraints.gridy++;
        constraints.gridx = FormPanel.LABEL_COLUMN;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        fieldLabel.setFont(fieldLabelFont);
        container.add(fieldLabel, constraints);

        constraints.gridx = FormPanel.CONTROL_COLUMN;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(topMargin, componentSpacing, bottomMargin, componentSpacing);
        container.add(colorPanel, constraints);
    }

}
