package ca.corbett.forms.fields;

import ca.corbett.extras.LookAndFeelManager;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.plaf.SliderUI;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Wraps a JSlider and provides options for configuring it.
 * This implementation goes beyond the functionality offered by the Java Swing JSlider component.
 * With SliderField, you can optionally specify a set of color stops, and the slider will change
 * color based on the slider's current position. There are some built-in color stops that you can
 * use, or you can supply your own. The SliderField can also optionally show its current value
 * in a field label, either using the numeric value of the current slider position, or by showing
 * a caller-supplied String associated with a particular range of values.
 * <p>
 *     <b>Note:</b> Only horizontal sliders are supported. (This is not a technical restriction, but
 *     more of a form real estate restriction, because FormPanels are laid out vertically, and a vertical
 *     slider would take up more vertical space than is reasonable).
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.5
 */
public class SliderField extends FormField {

    private static boolean isDefaultBorderEnabled = true;

    private final JSlider slider;
    private final JPanel valueLabelWrapperPanel;
    private final JLabel valueLabel;
    private boolean showValueLabel;
    private boolean showNumericValueInLabel;
    private ColorInterpolatingSliderUI lastGeneratedUI;

    public SliderField(String labelText, int min, int max, int value) {
        fieldLabel.setText(labelText);
        slider = new JSlider(JSlider.HORIZONTAL, min, max, value);
        slider.addChangeListener(e -> handleValueChanged());
        valueLabel = new JLabel();
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.add(slider, BorderLayout.CENTER);
        int spacing = getMargins().getInternalSpacing();
        valueLabelWrapperPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, spacing, spacing));
        valueLabelWrapperPanel.add(valueLabel);
        wrapperPanel.add(valueLabelWrapperPanel, BorderLayout.SOUTH);
        fieldComponent = wrapperPanel;
        if (isDefaultBorderEnabled) {
            setDefaultBorder();
        }
        showValueLabel = true; // arbitrary default
        updateValueLabel();

        // We need to watch out for Look and Feel changes, otherwise we might
        // lose our custom ColorInterpolatingSliderUI:
        LookAndFeelManager.addChangeListener(e -> {
            if (lastGeneratedUI != null) {
                slider.setUI(lastGeneratedUI);
            }
        });
    }

    /**
     * Optionally set a list of color stops for this slider. If set, the color of the slider will
     * change along the given color gradation. For example:
     * <pre>setColorStops(List.of(Color.RED, Color.YELLOW, Color.GREEN));</pre>
     * will show a slider that is shaded red on the left side, yellow towards the middle, and green
     * towards the right side of the slider. This can be a good way to make the slider more visually
     * meaningful depending on the value it represents. If not set (which is the default state),
     * the slider will appear as a regular JSlider. Note that you will have problems if the
     * number of color stops exceeds the range of values allowed in the slider (some colors will
     * be dropped).
     */
    public SliderField setColorStops(List<Color> colorStops) {
        if (slider.getUI() instanceof ColorInterpolatingSliderUI) {
            ((ColorInterpolatingSliderUI)slider.getUI()).setColorStops(colorStops);
        }
        else {
            lastGeneratedUI = new ColorInterpolatingSliderUI(colorStops);
            slider.setUI(lastGeneratedUI);
        }
        return this;
    }

    /**
     * Optionally set human-readable labels to be used in place of (or alongside) the numeric value
     * label. Only applies if the value label is showing! The value label may be hidden with
     * setShowValueLabel(false). If setColorStops() has not also been invoked, a default
     * color pattern will be applied automatically. You can change the default color pattern
     * by invoking setColorStops().
     * <p>
     *     Note that the size of the label list doesn't necessarily have to match the size of the
     *     color stop list (though that usually makes sense). You can have more or fewer labels
     *     in this list than there are color stops, and the labels will be interpreted accordingly.
     *     For example, if you supply a list of four labels, the first label will apply to the left
     *     25% of the slider track, the second will apply to the next 25%, and so on, regardless
     *     of the color gradation within the slider. Note that you will have problems if the number
     *     of labels exceeds the range of values allowed in the slider (some labels will be dropped).
     * </p>
     */
    public SliderField setLabels(List<String> labels, boolean alsoShowNumericValue) {
        showNumericValueInLabel = alsoShowNumericValue;
        if (slider.getUI() instanceof ColorInterpolatingSliderUI) {
            ((ColorInterpolatingSliderUI)slider.getUI()).setStopLabels(labels);
        }
        else {
            lastGeneratedUI = new ColorInterpolatingSliderUI(null, labels);
            slider.setUI(lastGeneratedUI);
        }
        updateValueLabel();
        return this;
    }

    /**
     * Shows or hides the value label underneath the JSlider that gives a text representation
     * of the current value of the slider.
     */
    public SliderField setShowValueLabel(boolean show) {
        if (showValueLabel == show) {
            return this;
        }
        showValueLabel = show;
        if (showValueLabel) {
            valueLabelWrapperPanel.add(valueLabel);
        }
        else {
            valueLabelWrapperPanel.remove(valueLabel);
        }
        getFieldComponent().invalidate();
        getFieldComponent().validate();
        getFieldComponent().repaint();
        return this;
    }

    @Override
    public boolean shouldExpand() {
        return true;
    }

    @Override
    public FormField setEnabled(boolean isEnabled) {
        super.setEnabled(isEnabled);
        slider.setEnabled(isEnabled);
        valueLabel.setEnabled(isEnabled);
        return this;
    }

    public SliderField setValue(int value) {
        slider.setValue(value);
        return this;
    }

    public int getValue() {
        return slider.getValue();
    }

    /**
     * Sets a default line border around the slider in this FormField. You can also invoke
     * setBorder to set some custom border.
     */
    public void setDefaultBorder() {
        fieldComponent.setBorder(BorderFactory.createLineBorder(
                LookAndFeelManager.getLafColor("ColorPalette.separatorColor", Color.GRAY)));
    }

    /**
     * Sets the given border around the JSlider in this FormField. You can also invoke
     * setDefaultBorder() to easily set a simple line border.
     */
    public SliderField setBorder(Border border) {
        getFieldComponent().setBorder(border); // the wrapper panel, not the slider
        return this;
    }

    /**
     * Reports whether a default border will be added to all new SliderField instances automatically.
     */
    public static boolean isIsDefaultBorderEnabled() {
        return isDefaultBorderEnabled;
    }

    /**
     * By default, all new instances of this class will give themselves a default LineBorder.
     * You can disable that behavior with this method. Note that this only affects new instance
     * creation from this point on - it will not change the border of any already-created instances.
     */
    public static void setIsDefaultBorderEnabled(boolean enable) {
        isDefaultBorderEnabled = enable;
    }

    /**
     * Invoked internally when the slider value changes. We update our value label (if showing)
     * and notify listeners that the change occurred.
     */
    private void handleValueChanged() {
        updateValueLabel();
        fireValueChangedEvent();
    }

    /**
     * Invoked internally to update our value label, if the value label is visible.
     */
    private void updateValueLabel() {
        String labelText = String.valueOf(slider.getValue());
        SliderUI ui = slider.getUI();
        if (ui instanceof ColorInterpolatingSliderUI) {
            String label = ((ColorInterpolatingSliderUI)ui).getCurrentValueLabel();
            if (! label.isBlank()) {
                labelText = label;
                if (showNumericValueInLabel) {
                    labelText += " ("+slider.getValue()+")";
                }
            }
        }
        valueLabel.setText(labelText);
    }

    /**
     * Custom UI delegate that handles the color interpolation painting.
     * This class brought to you by claude.ai!
     */
    private static class ColorInterpolatingSliderUI extends BasicSliderUI {

        private final int orientation = JSlider.HORIZONTAL; // only supported option currently
        private Color[] colorStops;
        private float[] colorStopFractions;
        private String[] stopLabels;
        private float[] stopLabelFractions;

        public ColorInterpolatingSliderUI(List<Color> colorList) {
            this(colorList, null);
        }

        public ColorInterpolatingSliderUI(List<Color> colorList, List<String> labels) {
            if (colorList == null || colorList.isEmpty()) {
                setColorStops(List.of(Color.RED, Color.GREEN));
            }
            else if (colorList.size() == 1) {
                setColorStops(List.of(colorList.get(0), colorList.get(0)));
            }
            else {
                setColorStops(colorList);
            }

            if (labels == null || labels.isEmpty()) {
                setStopLabels(null);
            }
            else if (labels.size() == 1) {
                setStopLabels(List.of(labels.get(0), labels.get(0)));
            }
            else {
                setStopLabels(labels);
            }
        }

        public int getOrientation() {
            return orientation;
        }

        private void setColorStops(List<Color> input) {
            colorStops = new Color[input.size()];
            colorStopFractions = new float[input.size()];
            for (int i = 0; i < input.size(); i++) {
                colorStops[i] = input.get(i);
                colorStopFractions[i] = ((float)i) / (input.size()-1);
            }
        }

        private void setStopLabels(List<String> labels) {
            if (labels == null) {
                stopLabels = null;
                stopLabelFractions = null;
                return;
            }
            stopLabels = new String[labels.size()];
            stopLabelFractions = new float[labels.size()];
            for (int i = 0; i < labels.size(); i++) {
                stopLabels[i] = labels.get(i);
                stopLabelFractions[i] = ((float)i) / labels.size();
            }
        }

        @Override
        public void paintTrack(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Rectangle trackBounds = getTrackBounds();

            if (slider.getOrientation() == JSlider.HORIZONTAL) {
                paintHorizontalTrack(g2d, trackBounds);
            } else {
                paintVerticalTrack(g2d, trackBounds);
            }

            g2d.dispose();
        }

        @Override
        protected TrackListener createTrackListener(JSlider slider) {
            return new TrackListener() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    super.mouseDragged(e);
                    // Force full repaint during drag to prevent artifacts
                    slider.repaint();
                }
            };
        }

        private void paintHorizontalTrack(Graphics2D g2d, Rectangle trackBounds) {
            int trackHeight = trackBounds.height;
            int trackWidth = trackBounds.width;
            int trackY = trackBounds.y;
            int trackX = trackBounds.x;

            LinearGradientPaint gradient = new LinearGradientPaint(
                    trackX, trackY,
                    trackX + trackWidth, trackY,
                    colorStopFractions, colorStops
            );

            g2d.setPaint(gradient);
            g2d.fillRoundRect(trackX, trackY, trackWidth, trackHeight, 6, 6);

            // Add border
            g2d.setColor(Color.GRAY);
            g2d.setStroke(new BasicStroke(1));
            g2d.drawRoundRect(trackX, trackY, trackWidth, trackHeight, 6, 6);
        }

        /**
         * Currently unused, as we officially only support horizontal sliders, but adding support
         * for vertical orientation would be pretty easy.
         */
        private void paintVerticalTrack(Graphics2D g2d, Rectangle trackBounds) {
            int trackHeight = trackBounds.height;
            int trackWidth = trackBounds.width;
            int trackY = trackBounds.y;
            int trackX = trackBounds.x;

            LinearGradientPaint gradient = new LinearGradientPaint(
                    trackX, trackY,
                    trackX, trackY + trackHeight,
                    colorStopFractions, colorStops
            );

            g2d.setPaint(gradient);
            g2d.fillRoundRect(trackX, trackY, trackWidth, trackHeight, 6, 6);

            // Add border
            g2d.setColor(Color.GRAY);
            g2d.setStroke(new BasicStroke(1));
            g2d.drawRoundRect(trackX, trackY, trackWidth, trackHeight, 6, 6);
        }

        @Override
        public void paintThumb(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Get the thumb bounds and tweak them a bit to accommodate our larger stroke width:
            Rectangle thumbBounds = new Rectangle(getThumbBounds());
            if (orientation == JSlider.HORIZONTAL) {
                thumbBounds.y += 2;
                thumbBounds.height -= 4;
            }
            else {
                thumbBounds.x += 2;
                thumbBounds.width -= 4;
            }

            // Calculate the current color based on slider position
            Color thumbColor = getCurrentColor();

            // Paint thumb with current color
            g2d.setColor(thumbColor);
            g2d.fillOval(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height);

            // Add border to thumb
            g2d.setColor(Color.DARK_GRAY);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawOval(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height);
            // Adjust starting Y and height to accommodate the stroke width

            // Add highlight for better visibility - eh, I'm not wild about it
//            g2d.setColor(Color.WHITE);
//            g2d.setStroke(new BasicStroke(1));
//            int highlightSize = Math.min(thumbBounds.width, thumbBounds.height) / 3;
//            g2d.drawOval(
//                    thumbBounds.x + thumbBounds.width / 4,
//                    thumbBounds.y + thumbBounds.height / 4,
//                    highlightSize, highlightSize
//            );

            g2d.dispose();
        }

        /**
         * Calculate the current color based on the slider's position
         */
        private Color getCurrentColor() {
            double min = slider.getMinimum();
            double max = slider.getMaximum();
            double current = slider.getValue();

            // Normalize the current value to a 0-1 range
            double normalizedValue = (current - min) / (max - min);

            // Handle edge cases
            if (normalizedValue <= colorStopFractions[0]) {
                return colorStops[0];
            }
            if (normalizedValue >= colorStopFractions[colorStopFractions.length - 1]) {
                return colorStops[colorStops.length - 1];
            }

            // Find the two color stops that bracket the current normalized value
            for (int i = 0; i < colorStopFractions.length - 1; i++) {
                if (normalizedValue >= colorStopFractions[i] && normalizedValue <= colorStopFractions[i + 1]) {
                    // Calculate the interpolation factor (0-1) within this segment
                    double segmentStart = colorStopFractions[i];
                    double segmentEnd = colorStopFractions[i + 1];
                    double t = (normalizedValue - segmentStart) / (segmentEnd - segmentStart);

                    return interpolateColor(colorStops[i], colorStops[i + 1], t);
                }
            }

            // Fallback (should never reach here if fractions are properly sorted)
            return colorStops[colorStops.length - 1];
        }

        public String getCurrentValueLabel() {
            if (stopLabels == null || stopLabelFractions == null) {
                return "";
            }

            double min = slider.getMinimum();
            double max = slider.getMaximum();
            double current = slider.getValue();

            // Normalize the current value to a 0-1 range
            double normalizedValue = (current - min) / (max - min);

            // Handle edge cases
            if (normalizedValue <= stopLabelFractions[0]) {
                return stopLabels[0];
            }
            if (normalizedValue >= stopLabelFractions[stopLabelFractions.length - 1]) {
                return stopLabels[stopLabels.length - 1];
            }

            // Find the index where the current value lies:
            for (int i = 0; i < stopLabelFractions.length - 1; i++) {
                if (normalizedValue >= stopLabelFractions[i] && normalizedValue <= stopLabelFractions[i + 1]) {
                    return stopLabels[i];
                }
            }

            // Fallback (should never reach here if fractions are properly sorted)
            System.out.println("fallback!");
            return stopLabels[stopLabels.length - 1];
        }

        /**
         * Interpolate between two colors
         */
        private Color interpolateColor(Color c1, Color c2, double t) {
            // Clamp t to [0, 1]
            t = Math.max(0, Math.min(1, t));

            int r = (int) (c1.getRed() + t * (c2.getRed() - c1.getRed()));
            int g = (int) (c1.getGreen() + t * (c2.getGreen() - c1.getGreen()));
            int b = (int) (c1.getBlue() + t * (c2.getBlue() - c1.getBlue()));

            return new Color(r, g, b);
        }

        private Rectangle getTrackBounds() {
            int trackBuffer = 2;
            int trackHeight = 8;

            if (slider.getOrientation() == JSlider.HORIZONTAL) {
                int trackLength = trackRect.width;
                int trackY = trackRect.y + (trackRect.height - trackHeight) / 2;
                return new Rectangle(trackRect.x, trackY, trackLength, trackHeight);
            } else {
                int trackLength = trackRect.height;
                int trackX = trackRect.x + (trackRect.width - trackHeight) / 2;
                return new Rectangle(trackX, trackRect.y, trackHeight, trackLength);
            }
        }

        private Rectangle getThumbBounds() {
            return thumbRect;
        }
    }
}
