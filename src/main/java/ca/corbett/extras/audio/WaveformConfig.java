package ca.corbett.extras.audio;

import ca.corbett.extras.config.ConfigObject;
import ca.corbett.extras.properties.Properties;

import java.awt.Color;

/**
 * Represents a preferences object that can be used with AudioUtil.generateWaveform().
 *
 * @author scorbo2
 * @since 2018-01-03
 */
public class WaveformConfig implements ConfigObject {

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

  private int xScale;
  private int yScale;

  private int xLimit;

  /**
   * Returns a clone of the given WaveformConfig instance. If you pass in null,
   * you will receive a WaveformConfig instance with all default values, the same
   * as if you had used new WaveformConfig() instead.
   *
   * @param other The WaveformConfig instance from which to copy. May be null.
   * @return A clone of the given WaveformConfig, or a defaulted WaveformConfig.
   */
  public static WaveformConfig clonePreferences(WaveformConfig other) {
    WaveformConfig clone = new WaveformConfig();
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
      clone.setXScale(other.getXScale());
      clone.setYScale(other.getYScale());
      clone.setXLimit(other.getXLimit());
    }
    return clone;
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
  public WaveformConfig() {
    enableOutline = true;
    outlineColor = Color.BLACK;
    fillColor = Color.LIGHT_GRAY;
    enableBaseline = true;
    baselineThickness = 2;
    baselineColor = Color.DARK_GRAY;
    bgColor = Color.WHITE;

    outlineThickness = 1;

    xScale = 1024;
    yScale = 64;
    xLimit = Integer.MAX_VALUE;

    topChannelIndex = 0;
    bottomChannelIndex = 1;
  }

  /**
   * Loads all settings from the given Properties object. You can use the optional "prefix"
   * parameter to put a given label at the start of each property name. This allows you
   * to save multiple WaveformConfig objects to the same Properties instance, keeping
   * them separated by prefix. For example, if a property name is "enableOutline" and you
   * supply a prefix of "waveform1.", the property value will be saved under the name
   * "waveform1.enableOutline". If you specify null or an empty string for prefix, property names
   * will be specified as-is, and will overwrite any previous value.
   *
   * @param props The Properties instance from which to load.
   * @param prefix An optional string prefix to apply to all property names, or null.
   */
  @Override
  public void loadFromProps(Properties props, String prefix) {
    if (prefix == null) {
      prefix = "";
    }

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
    setXScale(props.getInteger(prefix + "xScale", getXScale()));
    setYScale(props.getInteger(prefix + "yScale", getYScale()));
    setXLimit(props.getInteger(prefix + "xLimit", getXLimit()));
  }

  /**
   * Saves all settings to the given Properties object. You can use the optional "prefix"
   * parameter to put a given label at the start of each property name. This allows you
   * to save multiple WaveformConfig objects to the same Properties instance, keeping
   * them separated by prefix. For example, if a property name is "enableOutline" and you
   * supply a prefix of "waveform1.", the property value will be saved under the name
   * "waveform1.enableOutline". If you specify null or an empty string for prefix, property names
   * will be specified as-is, and will overwrite any previous value.
   *
   * @param props The Properties instance to which to save.
   * @param prefix An optional string prefix to apply to all property names, or null.
   */
  @Override
  public void saveToProps(Properties props, String prefix) {
    if (prefix == null) {
      prefix = "";
    }

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
    props.setInteger(prefix + "xScale", getXScale());
    props.setInteger(prefix + "yScale", getYScale());
    props.setInteger(prefix + "xLimit", getXLimit());
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

  /**
   * Returns the current horizontal scale factor. A value of 1 means no scaling. Values greater
   * than one indicate how many consecutive samples are averaged together for each horizontal
   * pixel of the generated image. Default value is 1024.
   *
   * @return The current horizontal scale factor.
   */
  public int getXScale() {
    return xScale;
  }

  /**
   * Returns the current vertical scale factor. A value of 1 means no scaling. Values greater
   * than one will be applied to each sample (sample value is divided by this number). Default
   * value is 64.
   *
   * @return The current vertical scale factor.
   */
  public int getYScale() {
    return yScale;
  }

  /**
   * Returns the current width limit of the generated image. This is Integer.MAX_VALUE by default
   * to indicate effectively no limit. Smaller values will cause the generated image to be
   * scaled down (if needed) to fit within the width limit.
   *
   * @return The maximum width of the generated image, or Integer.MAX_VALUE if no limit.
   */
  public int getXLimit() {
    return xLimit;
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

  /**
   * Sets the horizontal scale factor. Set this to 1 to disable scaling, but be aware this will
   * result in huge images - CD quality audio is 44.1Khz (samples per second), meaning you will
   * need over 44 thousand horizontal pixels to represent one second of audio. The default value
   * is 1024. The value represents how many consecutive samples are averaged together to form
   * each horizontal pixel of the waveform.
   *
   * @param xScale The horizontal scale factor, as described above.
   */
  public void setXScale(int xScale) {
    this.xScale = (xScale < 1) ? 1 : ((xScale > 10000) ? 10000 : xScale);
  }

  /**
   * Sets the vertical scale factor. Set this to 1 to disable scaling, but be aware this will
   * result in huge images - 16 bit sample sizes can yield values up to 32K, which is doubled
   * to mirror the waveform, so in the worst case your image will be 64K pixels tall. The default
   * value is 64. The value specified here will be used as a divisor - each sample is divided
   * by this value.
   *
   * @param yScale The verticala scale factor, as described above.
   */
  public void setYScale(int yScale) {
    this.yScale = (yScale < 1) ? 1 : ((yScale > 1024) ? 1024 : yScale);
  }

  /**
   * Sets the maximum width of the generated image. This is set to Integer.MAX_VALUE by default
   * to indicate effectively no limit. Setting this to a smaller value will cause the generated
   * image to be scaled down (if needed) to fit within the maximum width.
   *
   * @param xLimit The width limit of the generated image, or Integer.MAX_VALUE for effectively no
   * limit.
   */
  public void setXLimit(int xLimit) {
    this.xLimit = xLimit;
  }

}
