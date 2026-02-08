package ca.corbett.extras.image;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.properties.AbstractProperty;
import ca.corbett.extras.properties.Properties;
import ca.corbett.forms.fields.FormField;

import javax.swing.ImageIcon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Represents configuration options for ImagePanel. This class is optional - if you
 * don't specify it when creating and using an ImagePanel, an instance will be created
 * automatically with default values set for all properties. You can modify any property
 * in this class and hand the new instance to ImagePanel.setPanelProperties().
 *
 * @author scorbett
 * @since 2017-11-07
 */
public class ImagePanelConfig extends AbstractProperty implements ChangeListener {

    private static final Logger logger = Logger.getLogger(ImagePanelConfig.class.getName());

    public enum DisplayMode {
        NONE("None"),
        CENTER("Center"),
        BEST_FIT("Best fit"),
        STRETCH("Stretch"),
        CUSTOM("Custom");

        private final String label;

        DisplayMode(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }

        public static List<String> getLabels() {
            return Arrays.stream(values())
                         .map(Enum::toString)
                         .collect(Collectors.toList());
        }

        public static Optional<DisplayMode> fromLabel(String label) {
            return Arrays.stream(values())
                         .filter(e -> e.toString().equals(label))
                         .findFirst();
        }
    }

    public enum Quality {
        QUICK_AND_DIRTY("Quick and dirty"),
        SLOW_AND_ACCURATE("Slow and accurate");

        private final String label;

        Quality(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }

        public static List<String> getLabels() {
            return Arrays.stream(values())
                         .map(Enum::toString)
                         .collect(Collectors.toList());
        }

        public static Optional<Quality> fromLabel(String label) {
            return Arrays.stream(values())
                         .filter(e -> e.toString().equals(label))
                         .findFirst();
        }
    }

    private boolean autoSetBackground = true;
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
    protected ImagePanelConfig(String fullyQualifiedName) {
        super(fullyQualifiedName, "Image properties");
        nullCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "null");
        LookAndFeelManager.addChangeListener(this);
    }

    /**
     * Creates an ImagePanelConfig instance with all default values set.
     *
     * @return A default ImagePanelConfig instance.
     */
    public static ImagePanelConfig createDefaultProperties() {
        return createDefaultProperties("default");
    }

    public static ImagePanelConfig createDefaultProperties(String name) {
        ImagePanelConfig props = new ImagePanelConfig(name);
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
        return createSimpleReadOnlyProperties("simple-read-only");
    }

    public static ImagePanelConfig createSimpleReadOnlyProperties(String name) {
        ImagePanelConfig props = new ImagePanelConfig(name);
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
        return cloneProperties(other, other == null ? "unnamed" : other.getFullyQualifiedName());
    }

    public static ImagePanelConfig cloneProperties(ImagePanelConfig other, String newName) {
        if (other == null) {
            return createDefaultProperties(newName);
        }

        ImagePanelConfig newProps = new ImagePanelConfig(newName);
        newProps.bgColor = other.bgColor;
        newProps.autoSetBackground = other.autoSetBackground;
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
        autoSetBackground = true;
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
     * Sets the background colour of the panel, defaults to the panel bg color
     * from the current Look and Feel. This can also be
     * set directly on the ImagePanel itself, as it is a JPanel property. It is here
     * as a convenience.
     * <p>
     *     Note that explicitly setting a bgcolor here will override the
     *     current look and feel, and this instance will ignore any future
     *     look and feel changes in favour of the given bg color.
     * </p>
     *
     * @param bgColor The desired background colour.
     */
    public void setBgColor(Color bgColor) {
        this.bgColor = bgColor;
        autoSetBackground = false;
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

    @Override
    public void saveToProps(Properties props) {
        String prefix = fullyQualifiedName + ".";
        props.setColor(prefix + "bgColor", bgColor);
        props.setBoolean(prefix + "autoSetBackground", autoSetBackground);
        props.setDouble(prefix + "zoomFactorIncrement", zoomFactorIncrement);
        props.setString(prefix + "displayMode", displayMode.name());
        props.setBoolean(prefix + "enableZoomOnMouseClick", enableZoomOnMouseClick);
        props.setBoolean(prefix + "enableZoomOnMouseWheel", enableZoomOnMouseWheel);
        props.setBoolean(prefix + "enableMouseDragging", enableMouseDragging);
        props.setBoolean(prefix + "enableMouseCursor", enableMouseCursor);
        props.setString(prefix + "renderQuality", renderingQuality.name());
    }

    @Override
    public void loadFromProps(Properties props) {
        String pfx = fullyQualifiedName + ".";
        resetToDefaults();
        bgColor = props.getColor(pfx + "bgColor", bgColor);
        autoSetBackground = props.getBoolean(pfx + "autoSetBackground", autoSetBackground);
        zoomFactorIncrement = props.getDouble(pfx + "zoomFactorIncrement", zoomFactorIncrement);
        displayMode = DisplayMode.valueOf(props.getString(pfx + "displayMode", displayMode.name()));
        enableZoomOnMouseClick = props.getBoolean(pfx + "enableZoomOnMouseClick", enableZoomOnMouseClick);
        enableZoomOnMouseWheel = props.getBoolean(pfx + "enableZoomOnMouseWheel", enableZoomOnMouseWheel);
        enableMouseDragging = props.getBoolean(pfx + "enableMouseDragging", enableMouseDragging);
        enableMouseCursor = props.getBoolean(pfx + "enableMouseCursor", enableMouseCursor);
        renderingQuality = Quality.valueOf(props.getString(pfx + "renderQuality", renderingQuality.name()));
    }

    @Override
    protected FormField generateFormFieldImpl() {
        ImagePanelFormField formField = new ImagePanelFormField(propertyLabel);
        formField.setBgColor(bgColor);
        formField.setZoomIncrement(zoomFactorIncrement);
        formField.setDisplayMode(displayMode);
        formField.setRenderQuality(renderingQuality);
        formField.setEnableMouseCursor(enableMouseCursor);
        formField.setEnableMouseDragging(enableMouseDragging);
        formField.setEnableZoomOnMouseClick(enableZoomOnMouseClick);
        formField.setEnableZoomOnMouseWheel(enableZoomOnMouseWheel);
        return formField;
    }

    @Override
    public void loadFromFormField(FormField field) {
        if (field.getIdentifier() == null
                || !field.getIdentifier().equals(fullyQualifiedName)
                || !(field instanceof ImagePanelFormField formField)) {
            logger.log(Level.SEVERE, "ImagePanelConfig.loadFromFormField: received the wrong field \"{0}\"",
                       field.getIdentifier());
            return;
        }

        if (!field.isValid()) {
            logger.log(Level.WARNING, "ImagePanelConfig.loadFromFormField: received invalid form field \"{0}\"",
                       field.getIdentifier());
            return;
        }

        bgColor = formField.getBgColor();
        zoomFactorIncrement = formField.getZoomIncrement();
        displayMode = formField.getDisplayMode();
        renderingQuality = formField.getRenderQuality();
        enableMouseCursor = formField.isEnableMouseCursor();
        enableMouseDragging = formField.isEnableMouseDragging();
        enableZoomOnMouseClick = formField.isEnableZoomOnMouseClick();
        enableZoomOnMouseWheel = formField.isEnableZoomOnMouseWheel();
    }

    /**
     * Invoked from LookAndFeelManager when the Look and Feel is changed.
     * If we have never been given an explicit background color, then we'll
     * auto-set it according to the new look and feel.
     *
     * @param e a ChangeEvent object
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        if (autoSetBackground) {
            bgColor = LookAndFeelManager.getLafColor("Panel.background", Color.DARK_GRAY);
        }
    }
}
