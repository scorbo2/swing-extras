package ca.corbett.extras.image;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.config.ConfigObject;
import ca.corbett.extras.properties.Properties;

import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

/**
 * Represents configuration options for ImagePanel. This class is optional - if you
 * don't specify it when creating and using an ImagePanel, an instance will be created
 * automatically with default values set for all properties. You can modify any property
 * in this class and hand the new instance to ImagePanel.setPanelProperties().
 *
 * @author scorbett
 * @since 2017-11-07
 */
public class ImagePanelConfig implements ConfigObject {

    public enum DisplayMode {
        NONE, CENTER, BEST_FIT, STRETCH, CUSTOM
    }

    public enum Quality {
        QUICK_AND_DIRTY, SLOW_AND_ACCURATE
    }

    private Color bgColor;
    private Cursor magnifierCursor;
    private final Cursor nullCursor;
    private boolean enableZoomOnMouseClick;
    private boolean enableZoomOnMouseWheel;
    private DisplayMode displayMode;
    private boolean enableMouseCursor;
    private boolean enableMouseDragging;
    private double zoomFactorIncrement;
    private Quality renderingQuality;

    /**
     * Constructor is protected to force callers to use the factory methods.
     */
    protected ImagePanelConfig() {
        nullCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "null");
    }

    /**
     * Creates an ImagePanelConfig instance with all default values set.
     *
     * @return A default ImagePanelConfig instance.
     */
    public static ImagePanelConfig createDefaultProperties() {
        ImagePanelConfig props = new ImagePanelConfig();
        props.resetToDefaults();
        return props;
    }

    /**
     * Creates an ImagePanelConfig suitable for simply display of a non-zoomable,
     * non-scrollable image.
     *
     * @return An ImagePanelConfig configured for simple non-interactive display.
     */
    public static ImagePanelConfig createSimpleReadOnlyProperties() {
        ImagePanelConfig props = new ImagePanelConfig();
        props.resetToDefaults();
        props.setEnableMouseDragging(false);
        props.setEnableZoomOnMouseClick(false);
        props.setEnableZoomOnMouseWheel(false);
        return props;
    }

    /**
     * Clones the given ImagePanelConfig into a new instance. If you pass in null,
     * you'll get back a defaulted instance, same as if you had called createDefaultProperties.
     *
     * @param other The ImagePanelConfig object to be cloned.
     * @return The new, cloned properties instance.
     */
    public static ImagePanelConfig cloneProperties(ImagePanelConfig other) {
        if (other == null) {
            return createDefaultProperties();
        }

        ImagePanelConfig newProps = new ImagePanelConfig();
        newProps.setBgColor(other.bgColor);
        newProps.setZoomFactorIncrement(other.getZoomFactorIncrement());
        newProps.setDisplayMode(other.getDisplayMode());
        newProps.setEnableZoomOnMouseClick(other.isEnableZoomOnMouseClick());
        newProps.setEnableZoomOnMouseWheel(other.isEnableZoomOnMouseWheel());
        newProps.setEnableMouseDragging(other.isEnableMouseDragging());
        newProps.setEnableMouseCursor(other.isEnableMouseCursor());
        newProps.setMagnifierCursor(other.getMagnifierCursor());
        newProps.setRenderingQuality(other.getRenderingQuality());
        return newProps;
    }

    /**
     * Resets all properties back to their default values.
     */
    public void resetToDefaults() {
        bgColor = LookAndFeelManager.getLafColor("Panel.background", Color.DARK_GRAY);
        zoomFactorIncrement = 0.1;
        enableZoomOnMouseClick = true;
        enableZoomOnMouseWheel = true;
        enableMouseDragging = true;
        enableMouseCursor = true;
        displayMode = DisplayMode.BEST_FIT;
        renderingQuality = Quality.SLOW_AND_ACCURATE;

        ImageIcon icon = new ImageIcon(getClass().getResource("/swing-extras/images/cursor_magnifier.gif"));
        magnifierCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                icon.getImage(), new Point(0, 0), "magnifier");
    }

    /**
     * Loads all settings from the given Properties object. You can use the optional "prefix"
     * parameter to put a given label at the start of each property name. This allows you
     * to save multiple ImagePanelConfig objects to the same Properties instance, keeping
     * them separated by prefix. For example, if a property name is "enableOutline" and you
     * supply a prefix of "waveform1.", the property value will be saved under the name
     * "waveform1.enableOutline". If you specify null or an empty string for prefix, property names
     * will be specified as-is, and will overwrite any previous value.
     *
     * @param props  The Properties instance from which to load.
     * @param prefix An optional string prefix to apply to all property names, or null.
     */
    @Override
    public void loadFromProps(Properties props, String prefix) {
        String pfx = (prefix == null) ? "" : prefix;
        resetToDefaults();
        bgColor = props.getColor(pfx + "bgColor", bgColor);
        zoomFactorIncrement = props.getDouble(pfx + "zoomFactorIncrement", zoomFactorIncrement);
        displayMode = DisplayMode.valueOf(props.getString(pfx + "displayMode", displayMode.name()));
        enableZoomOnMouseClick = props.getBoolean(pfx + "enableZoomOnMouseClick", enableZoomOnMouseClick);
        enableZoomOnMouseWheel = props.getBoolean(pfx + "enableZoomOnMouseWheel", enableZoomOnMouseWheel);
        enableMouseDragging = props.getBoolean(pfx + "enableMouseDragging", enableMouseDragging);
        enableMouseCursor = props.getBoolean(pfx + "enableMouseCursor", enableMouseCursor);
        renderingQuality = Quality.valueOf(props.getString(pfx + "renderQuality", renderingQuality.name()));
    }

    /**
     * Saves all settings to the given Properties object. You can use the optional "prefix"
     * parameter to put a given label at the start of each property name. This allows you
     * to save multiple ImagePanelConfig objects to the same Properties instance, keeping
     * them separated by prefix. For example, if a property name is "enableOutline" and you
     * supply a prefix of "waveform1.", the property value will be saved under the name
     * "waveform1.enableOutline". If you specify null or an empty string for prefix, property names
     * will be specified as-is, and will overwrite any previous value.
     *
     * @param props  The Properties instance to which to save.
     * @param prefix An optional string prefix to apply to all property names, or null.
     */
    @Override
    public void saveToProps(Properties props, String prefix) {
        if (prefix == null) {
            prefix = "";
        }

        props.setColor(prefix + "bgColor", bgColor);
        props.setDouble(prefix + "zoomFactorIncrement", zoomFactorIncrement);
        props.setString(prefix + "displayMode", displayMode.name());
        props.setBoolean(prefix + "enableZoomOnMouseClick", enableZoomOnMouseClick);
        props.setBoolean(prefix + "enableZoomOnMouseWheel", enableZoomOnMouseWheel);
        props.setBoolean(prefix + "enableMouseDragging", enableMouseDragging);
        props.setBoolean(prefix + "enableMouseCursor", enableMouseCursor);
        props.setString(prefix + "renderQuality", renderingQuality.name());
    }

    /**
     * The background colour of the panel, defaults to Color.DARK_GRAY.
     *
     * @return The panel background colour.
     */
    public Color getBgColor() {
        return bgColor;
    }

    /**
     * Sets the image rendering quality preference: QUICK_AND_DIRTY emphasizes speed over
     * quality, while SLOW_AND_ACCURATE does the opposite.
     *
     * @param quality The desired rendering quality.
     */
    public void setRenderingQuality(Quality quality) {
        this.renderingQuality = quality;
    }

    /**
     * Gets the image rendering quality preference: QUICK_AND_DIRTY emphasizes speed over
     * quality, while SLOW_AND_ACCURATE does the opposite.
     *
     * @return The current rendering quality.
     */
    public Quality getRenderingQuality() {
        return renderingQuality;
    }

    /**
     * Sets the background colour of the panel, defaults to Color.DARK_GRAY. This can also be
     * set directly on the ImagePanel itself, as it is a JPanel property. It is here
     * as a convenience.
     *
     * @param bgColor The desired background colour.
     */
    public void setBgColor(Color bgColor) {
        this.bgColor = bgColor;
    }

    /**
     * If enableZoomOnMouseClick or enableZoomOnMouseWheel are set, this cursor will be used
     * when the mouse pointer is over the panel, as a visual clue to the user that zoom operations
     * are supported. The default value is a magnifier cursor.
     *
     * @return The currently set mouse cursor.
     */
    public Cursor getMagnifierCursor() {
        return magnifierCursor;
    }

    /**
     * If enableZoomOnMouseClick or enableZoomOnMouseWheel are set, this cursor will be used
     * when the mouse pointer is over the panel, as a visual clue to the user that zoom operations
     * are supported. The default value is a magnifier cursor. If enableZoomOnMouseClick and
     * enableZoomOnMouseWheel are both disabled, this property is ignored. You can set null
     * here to force the use of the default system cursor even if zooming is enabled.
     *
     * @param magnifierCursor The mouse cursor to use.
     */
    public void setMagnifierCursor(Cursor magnifierCursor) {
        this.magnifierCursor = magnifierCursor;
    }

    /**
     * Returns a "null" cursor that can be used if the cursor is to be hidden.
     *
     * @return An empty cursor.
     */
    public Cursor getNullCursor() {
        return nullCursor;
    }

    /**
     * Enables the use of mouse clicks to zoom: left click to zoom in, right click to zoom out.
     * The default value is true.
     *
     * @return Whether mouse clicks can be used to zoom in and out of the image.
     */
    public boolean isEnableZoomOnMouseClick() {
        return enableZoomOnMouseClick;
    }

    /**
     * Enables the use of mouse clicks to zoom: left click to zoom in, right click to zoom out.
     * The default value is true.
     *
     * @param enableZoomOnMouseClick Whether mouse clicks can be used to zoom in and out of the image.
     */
    public void setEnableZoomOnMouseClick(boolean enableZoomOnMouseClick) {
        this.enableZoomOnMouseClick = enableZoomOnMouseClick;
    }

    /**
     * Enables the use of mouse wheel for zooming: wheel up to zoom in, wheel down to zoom out.
     * The default value is true.
     *
     * @return Whether the mouse wheel can be used to zoom in and out of the image.
     */
    public boolean isEnableZoomOnMouseWheel() {
        return enableZoomOnMouseWheel;
    }

    /**
     * Enables the use of mouse wheel for zooming: wheel up to zoom in, wheel down to zoom out.
     * The default value is true.
     *
     * @param enableZoomOnMouseWheel Whether the mouse wheel can be used to zoom in and out.
     */
    public void setEnableZoomOnMouseWheel(boolean enableZoomOnMouseWheel) {
        this.enableZoomOnMouseWheel = enableZoomOnMouseWheel;
    }

    /**
     * Returns the current DisplayMode.
     * <ul>
     * <li>NONE: just display the image unscaled at 0,0</li>
     * <li>CENTER: center the image in the panel, but don't resize it</li>
     * <li>BEST_FIT: center the image and scale it up or down as needed to fit the panel.</li>
     * <li>STRETCH: distort the image so it fully fills the panel.</li>
     * <li>CUSTOM: this is used internally when zooming in/out and should be ignored otherwise.</li>
     * </ul>
     *
     * @return The current display mode as outlined above.
     */
    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    /**
     * Sets the DisplayMode.
     * <ul>
     * <li>NONE: just display the image unscaled at 0,0</li>
     * <li>CENTER: center the image in the panel, but don't resize it</li>
     * <li>BEST_FIT: center the image and scale it up or down as needed to fit the panel.</li>
     * <li>STRETCH: distort the image so it fully fills the panel.</li>
     * <li>CUSTOM: this is used internally when zooming in/out and should be ignored otherwise.</li>
     * </ul>
     *
     * @param mode The DisplayMode to use, as outlined above.
     */
    public void setDisplayMode(DisplayMode mode) {
        displayMode = mode;
    }

    /**
     * If disabled, mouse cursor will be hidden while over the panel. This is enabled by default.
     * The cursor will either be the system default cursor if enableZoomOnMouseClick and
     * enableZoomOnMouseWheel are both disabled, or it will be set to the current
     * magnifier cursor otherwise (see setMagnifierCursor).
     *
     * @return Whether the mouse cursor will be visible over the panel.
     */
    public boolean isEnableMouseCursor() {
        return enableMouseCursor;
    }

    /**
     * If disabled, mouse cursor will be hidden while over the panel. This is enabled by default.
     * The cursor will either be the system default cursor if enableZoomOnMouseClick and
     * enableZoomOnMouseWheel are both disabled, or it will be set to the current
     * magnifier cursor otherwise (see setMagnifierCursor).
     *
     * @param enableMouseCursor Whether the mouse cursor will be visible over the panel.
     */
    public void setEnableMouseCursor(boolean enableMouseCursor) {
        this.enableMouseCursor = enableMouseCursor;
    }

    /**
     * If enabled, users can click and drag on a zoomed-in image to scroll it around within
     * the panel. Note that this property does nothing if the image is not zoomed in to the point
     * where it is larger than the panel. The default value is true.
     *
     * @return Whether mouse dragging is allowed to scroll zoomed-in images.
     */
    public boolean isEnableMouseDragging() {
        return enableMouseDragging;
    }

    /**
     * If enabled, users can click and drag on a zoomed-in image to scroll it around within
     * the panel. Note that this property does nothing if the image is not zoomed in to the point
     * where it is larger than the panel. The default value is true.
     *
     * @param enableMouseDragging Whether mouse dragging is allowed to scroll zoomed-in images.
     */
    public void setEnableMouseDragging(boolean enableMouseDragging) {
        this.enableMouseDragging = enableMouseDragging;
    }

    /**
     * When zooming in or out, represents the percentage step to apply up or down.
     * The default value is 0.1, meaning the image will be scaled up in incrememts of 10% for
     * each zoom operation.
     *
     * @return The current zoom increment.
     */
    public double getZoomFactorIncrement() {
        return zoomFactorIncrement;
    }

    /**
     * When zooming in or out, represents the percentage step to apply up or down.
     * The default value is 0.1, meaning the image will be scaled up in incrememts of 10% for
     * each zoom operation.
     *
     * @param zoomFactorIncrement The zoom increment to use.
     */
    public void setZoomFactorIncrement(double zoomFactorIncrement) {
        this.zoomFactorIncrement = zoomFactorIncrement;
    }

}
