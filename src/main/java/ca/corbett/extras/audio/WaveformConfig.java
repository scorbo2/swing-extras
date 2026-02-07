package ca.corbett.extras.audio;

import ca.corbett.extras.properties.AbstractProperty;
import ca.corbett.extras.properties.Properties;
import ca.corbett.forms.fields.FormField;

import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a preferences object that can be used with AudioUtil.generateWaveform().
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2018-01-03
 */
public class WaveformConfig extends AbstractProperty {

    private static final Logger logger = Logger.getLogger(WaveformConfig.class.getName());

    private Color fillColor;
    private Color bgColor;

    private boolean enableBaseline;
    private Color baselineColor;
    private int baselineThickness;

    private boolean enableOutline;
    private Color outlineColor;
    private int outlineThickness;

    private int topChannelIndex;
    private int bottomChannelIndex;

    private WaveformConfigField.Compression compression;
    private WaveformConfigField.WidthLimit widthLimit;

    /**
     * Returns a clone of the given WaveformConfig instance. If you pass in null,
     * you will receive a WaveformConfig instance with all default values, the same
     * as if you had used new WaveformConfig() instead.
     *
     * @param other The WaveformConfig instance from which to copy. May be null.
     * @return A clone of the given WaveformConfig, or a defaulted WaveformConfig.
     */
    public static WaveformConfig clonePreferences(WaveformConfig other) {
        WaveformConfig clone = new WaveformConfig(other == null ? "Waveform config" : other.fullyQualifiedName);
        if (other != null) {
            clone.setOutlineEnabled(other.isOutlineEnabled());
            clone.setOutlineColor(other.getOutlineColor());
            clone.setOutlineThickness(other.getOutlineThickness());
            clone.setFillColor(other.getFillColor());
            clone.setBgColor(other.getBgColor());
            clone.setBaselineThickness(other.getBaselineThickness());
            clone.setBaselineEnabled(other.isBaselineEnabled());
            clone.setBaselineColor(other.getBaselineColor());
            clone.setTopChannelIndex(other.getTopChannelIndex());
            clone.setBottomChannelIndex(other.getBottomChannelIndex());
            clone.setWidthLimit(other.getWidthLimit());
            clone.setCompression(other.getCompression());
        }
        return clone;
    }

    public WaveformConfig() {
        this("WaveformConfig");
    }

    /**
     * Creates a WaveFormPreferences instance with all defaulted values:
     * <ul>
     * <li>White background</li>
     * <li>Black outline</li>
     * <li>Light grey fill</li>
     * <li>Dark grey baseline</li>
     * <li>Outline line thickness of 1</li>
     * <li>Horizontal scale of 1/1024</li>
     * <li>Vertical scale of 1/64</li>
     * <li>Top half of waveform will be channel 0</li>
     * <li>Bottom half will be channel 1 (or mirror of channel 0 if single channel).</li>
     * </ul>
     */
    public WaveformConfig(String fullyQualifiedName) {
        super(fullyQualifiedName, "Waveform config");
        enableOutline = true;
        outlineColor = Color.BLACK;
        fillColor = Color.LIGHT_GRAY;
        enableBaseline = true;
        baselineThickness = 2;
        baselineColor = Color.DARK_GRAY;
        bgColor = Color.WHITE;

        outlineThickness = 1;

        compression = WaveformConfigField.Compression.NORMAL;
        widthLimit = WaveformConfigField.WidthLimit.NO_LIMIT;

        topChannelIndex = 0;
        bottomChannelIndex = 1;
    }

    /**
     * Enables or disables the drawing of an outline around the waveform. If enabled,
     * you should also call setOutlineColor() to choose a colour for the outline. You may
     * also call setOutlineThickness() to set the pixel width of the outline.
     *
     * @param enable Whether to enable or disable the outline.
     */
    public void setOutlineEnabled(boolean enable) {
        enableOutline = enable;
    }

    /**
     * Returns whether the drawing of an outline around the waveform is enabled.
     * See also the getOutlineColor() and getOutlineThickness() methods.
     *
     * @return Whether an outline will be drawn around the waveform.
     */
    public boolean isOutlineEnabled() {
        return enableOutline;
    }

    /**
     * Returns the current outline colour - this is the colour of the line to be drawn on the
     * outline of the waveform.
     *
     * @return The outline colour.
     */
    public Color getOutlineColor() {
        return outlineColor;
    }

    /**
     * Returns the current fill colour - this is the colour that will be used to fill the
     * waveform.
     *
     * @return The fill colour.
     */
    public Color getFillColor() {
        return fillColor;
    }

    /**
     * Returns the current background colour - this is the colour for the area of the image
     * behind the waveform.
     *
     * @return The background color.
     */
    public Color getBgColor() {
        return bgColor;
    }

    /**
     * Enables or disables the drawing of the baseline at the center of the waveform. If enabled,
     * you should also call setBaselineColor() to choose a colour for the baseline. You may
     * also call setBaselineThickness() to set the pixel width of the outline.
     *
     * @param enable Whether to enable or disable the baseline.
     */
    public void setBaselineEnabled(boolean enable) {
        enableBaseline = enable;
    }

    /**
     * Returns whether the drawing of a baseline at the center of the waveform is enabled.
     * See also the getBaselineColor() and getBaselineThickness() methods.
     *
     * @return Whether a baseline will be drawn at the center of the waveform.
     */
    public boolean isBaselineEnabled() {
        return enableBaseline;
    }

    /**
     * Sets the pixel thickness of the baseline, if it is enabled.
     *
     * @param width The number of pixels wide for drawing the baseline.
     */
    public void setBaselineThickness(int width) {
        baselineThickness = width;
    }

    /**
     * Returns the pixel width for the baseline, if it is enabled.
     *
     * @return the number of pixels thick for the baseline.
     */
    public int getBaselineThickness() {
        return baselineThickness;
    }

    /**
     * Returns the current baseline colour - this is the colour for the center line of
     * the waveform. Set it to the same as fillColor to disable the baseline.
     *
     * @return The baseline color.
     */
    public Color getBaselineColor() {
        return baselineColor;
    }

    /**
     * Returns the thickness of the outline. Default is 1 pixel.
     *
     * @return The pixel width of the line to use to outline the waveform.
     */
    public int getOutlineThickness() {
        return outlineThickness;
    }

    /**
     * Returns the channel index that will be used for the top half of the waveform.
     *
     * @return The index of the channel for the top half of the waveform.
     */
    public int getTopChannelIndex() {
        return topChannelIndex;
    }

    /**
     * Returns the channel inde that will be used for the bottom half of the waveform.
     * If this is the same index as topChannelIndex, the waveform will be a mirror.
     *
     * @return The index of the channel for the bottom half of the waveform.
     */
    public int getBottomChannelIndex() {
        return bottomChannelIndex;
    }

    public WaveformConfigField.Compression getCompression() {
        return compression;
    }

    public void setCompression(WaveformConfigField.Compression compression) {
        this.compression = compression;
    }

    public WaveformConfigField.WidthLimit getWidthLimit() {
        return widthLimit;
    }

    public void setWidthLimit(WaveformConfigField.WidthLimit limit) {
        widthLimit = limit;
    }

    /**
     * Sets the colour to use to outline the waveform. Set this to the same as fillColor
     * or bgColor to disable the outline.
     *
     * @param outlineColor The colour to use to outline the waveform.
     */
    public void setOutlineColor(Color outlineColor) {
        this.outlineColor = outlineColor;
    }

    /**
     * Sets the colour to use to fill the waveform. Set this to the same as bgColor to disable
     * the fill.
     *
     * @param fillColor The colour to use to fill the waveform.
     */
    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    /**
     * Sets the colour to use to draw the horizontal center line. Set this to the same as
     * fillColor to disable the baseline.
     *
     * @param baselineColor The colour for the horizontal center line.
     */
    public void setBaselineColor(Color baselineColor) {
        this.baselineColor = baselineColor;
    }

    /**
     * Sets the line thickness to use for the outline of the waveform. Set this to 0 to
     * disable the outline. Values less than 0 or greater than 10 are ignored.
     *
     * @param thickness The pixel width of the line that outlines the waveform.
     */
    public void setOutlineThickness(int thickness) {
        this.outlineThickness = (thickness < 0) ? 0 : ((thickness > 10) ? 10 : thickness);
    }

    /**
     * Sets the colour to use for the background behind the waveform.
     *
     * @param bgColor The colour to use for the background.
     */
    public void setBgColor(Color bgColor) {
        this.bgColor = bgColor;
    }

    /**
     * Sets the index of the audio channel to use for the top half of the waveform.
     * This defaults to 0 and generally shouldn't be changed unless you're doing
     * something unusual. Values will be clipped as needed - for example, if this value is
     * less than 0, or greater than the highest available channel index, it is clipped to
     * be within that range.
     *
     * @param topChannelIndex The index of the audio channel for the top half of the waveform.
     */
    public void setTopChannelIndex(int topChannelIndex) {
        this.topChannelIndex = (topChannelIndex < 0) ? 0 : topChannelIndex;
    }

    /**
     * Sets the index of the audio channel to use for the bottom half of the waveform.
     * This defafults to 1 and generally shouldn't be changed unless you're doing
     * something unusual. Values will be clipped as needed - for example, if this value is
     * less than 0, or greater than the highest available channel index, it is clipped to
     * be within that range. Set this to the same as topChannelIndex if you want a mirror
     * waveform (this will happen automatically in the case of mono audio input).
     *
     * @param bottomChannelIndex The index of the audio channel for the bottom half of the waveform.
     */
    public void setBottomChannelIndex(int bottomChannelIndex) {
        this.bottomChannelIndex = (bottomChannelIndex < 0) ? 0 : bottomChannelIndex;
    }

    @Override
    public void saveToProps(Properties props) {
        String prefix = fullyQualifiedName + ".";

        props.setBoolean(prefix + "outlineEnabled", isOutlineEnabled());
        props.setColor(prefix + "outlineColor", getOutlineColor());
        props.setInteger(prefix + "outlineThickness", getOutlineThickness());
        props.setColor(prefix + "fillColor", getFillColor());
        props.setColor(prefix + "bgColor", getBgColor());
        props.setBoolean(prefix + "baselineEnabled", isBaselineEnabled());
        props.setInteger(prefix + "baselineThickness", getBaselineThickness());
        props.setColor(prefix + "baselineColor", getBaselineColor());
        props.setInteger(prefix + "topChannelIndex", getTopChannelIndex());
        props.setInteger(prefix + "bottomChannelIndex", getBottomChannelIndex());
        props.setString(prefix + "compression", compression.name());
        props.setString(prefix + "widthLimit", widthLimit.name());
    }

    @Override
    public void loadFromProps(Properties props) {
        String prefix = fullyQualifiedName + ".";

        setOutlineEnabled(props.getBoolean(prefix + "outlineEnabled", isOutlineEnabled()));
        setOutlineColor(props.getColor(prefix + "outlineColor", getOutlineColor()));
        setOutlineThickness(props.getInteger(prefix + "outlineThickness", getOutlineThickness()));
        setFillColor(props.getColor(prefix + "fillColor", getFillColor()));
        setBgColor(props.getColor(prefix + "bgColor", getBgColor()));
        setBaselineEnabled(props.getBoolean(prefix + "baselineEnabled", isBaselineEnabled()));
        setBaselineThickness(props.getInteger(prefix + "baselineThickness", getBaselineThickness()));
        setBaselineColor(props.getColor(prefix + "baselineColor", getBaselineColor()));
        setTopChannelIndex(props.getInteger(prefix + "topChannelIndex", getTopChannelIndex()));
        setBottomChannelIndex(props.getInteger(prefix + "bottomChannelIndex", getBottomChannelIndex()));
        setCompression(WaveformConfigField.Compression.valueOf(
                props.getString(prefix + "compression", WaveformConfigField.Compression.NORMAL.name())));
        setWidthLimit(WaveformConfigField.WidthLimit.valueOf(
                props.getString(prefix + "widthLimit", WaveformConfigField.WidthLimit.NO_LIMIT.name())));
    }

    @Override
    protected FormField generateFormFieldImpl() {
        WaveformConfigField formField = new WaveformConfigField(propertyLabel);
        formField.setBgColor(bgColor);
        formField.setWaveformColor(fillColor);
        formField.setOutlineColor(outlineColor);
        formField.setBaselineColor(baselineColor);
        formField.setBaselineWidth(baselineThickness);
        formField.setOutlineWidth(outlineThickness);
        formField.setEnableDrawBaseline(enableBaseline);
        formField.setEnableDrawOutline(enableOutline);
        formField.setCompression(compression);
        formField.setWidthLimit(widthLimit);

        return formField;
    }

    @Override
    public void loadFromFormField(FormField field) {
        if (field.getIdentifier() == null
                || !field.getIdentifier().equals(fullyQualifiedName)
                || !(field instanceof WaveformConfigField formField)) {
            logger.log(Level.SEVERE, "WaveformConfig.loadFromFormField: received the wrong field \"{0}\"",
                       field.getIdentifier());
            return;
        }

        if (!field.isValid()) {
            logger.log(Level.WARNING, "WaveformConfig.loadFromFormField: received invalid field \"{0}\"",
                       field.getIdentifier());
            return;
        }

        bgColor = formField.getBgColor();
        fillColor = formField.getWaveformColor();
        outlineColor = formField.getOutlineColor();
        baselineColor = formField.getBaselineColor();
        baselineThickness = formField.getBaselineWidth();
        outlineThickness = formField.getOutlineWidth();
        enableBaseline = formField.isEnableDrawBaseline();
        enableOutline = formField.isEnableDrawOutline();
        compression = formField.getCompression();
        widthLimit = formField.getWidthLimit();
    }
}
