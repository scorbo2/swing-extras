package ca.corbett.extras.image;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.RedispatchingMouseAdapter;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * A custom JPanel extension that can display an image in configurable ways, with optional
 * handling of mouse events to allow zooming and scrolling. Defaults are provided for
 * all configuration options. Alternatively, you can create an ImagePanelConfig
 * instance and override some or all of those defaults.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @see ca.corbett.extras.image.ImagePanelConfig
 * @since 2012-09-22 (originally for StegPng, later generified for ca.corbett.util.ui)
 */
public class ImagePanel extends JPanel implements MouseListener, MouseWheelListener, MouseMotionListener {

    /**
     * An optional map of extra, caller-supplied attributes. *
     */
    protected final Map<String, Object> extraAttributes;

    /**
     * A handle on the image which will be displayed in this panel.
     * This is set to null and ignored if an ImageIcon is set instead.
     */
    protected BufferedImage dBuffer;

    /**
     * A handle on the ImageIcon which will be displayed in this panel.
     * This is set to null and ignored if a BufferedImage is set instead.
     */
    protected ImageIcon imageIcon;

    /**
     * A wrapper label for the imageIcon, if set. If a BufferedImage is set
     * instead of an ImageIcon, then this property is ignored.
     */
    protected JLabel imageIconLabel;

    /**
     * The last rendered width of our image. If the image is scaled or if the panel
     * is resized, the image may need to be re-rendered at a different size.
     */
    protected int lastRenderedImageWidth;

    /**
     * The last rendered height of our image.If the image is scaled or if the panel
     * is resized, the image may need to be re-rendered at a different size.
     */
    protected int lastRenderedImageHeight;

    /**
     * A handle on our ImagePanelConfig configuration object. *
     */
    protected ImagePanelConfig properties;

    /**
     * A handle on the original ImagePanelConfig (needed because we'll modify these). *
     */
    protected ImagePanelConfig originalProperties;

    /**
     * Stores the current zoomFactor (1 for no zoom, higher numbers = more zoom). *
     */
    protected double zoomFactor = 1.0;

    /**
     * A Point which represents the co-ordinates around which zoom operations will happen. *
     */
    protected Point zoomCenter;

    /**
     * Used internally for mouse dragging operations. *
     */
    protected Point mouseDragPoint;

    /**
     * Used internally for mouse dragging operations. *
     */
    protected Point mouseDragDelta;

    /**
     * Used internally for image placement. *
     */
    protected int imageX;

    /**
     * Used internally for image placement. *
     */
    protected int imageY;

    /**
     * An optional popup menu for this ImagePanel. *
     */
    protected JPopupMenu popupMenu;

    /**
     * Creates a new ImagePanel with a default ImagePanelConfig and no image.
     */
    public ImagePanel() {
        this(null, null, null);
    }

    /**
     * Creates an ImagePanel with the given image and with a default ImagePanelConfig.
     *
     * @param image The image to display in this panel. Can be null.
     */
    public ImagePanel(BufferedImage image) {
        this(image, null, null);
    }

    /**
     * Creates an empty ImagePanel (no image) with the given ImagePanelConfig.
     *
     * @param ipc The ImagePanelConfig specifying our panel configuration.
     */
    public ImagePanel(ImagePanelConfig ipc) {
        this(null, null, ipc);
    }

    /**
     * Creates an ImagePanel with the given ImageIcon and a default ImagePanelConfig.
     *
     * @param icon The ImageIcon to display in this panel. Can be null.
     */
    public ImagePanel(ImageIcon icon) {
        this(null, icon, null);
    }

    /**
     * Creates an ImagePanel with the given image and the given ImagePanelConfig.
     *
     * @param image The image to display in this panel. Can be null.
     * @param props The ImagePanelConfig specifying our panel configuration.
     */
    public ImagePanel(BufferedImage image, ImagePanelConfig props) {
        this(image, null, props);
    }

    /**
     * Creates an ImagePanel with the given ImageIcon and ImagePanelConfig.
     *
     * @param icon  The ImageIcon to display in this panel. Can be null.
     * @param props The ImagePanelConfig specifying our panel configuration.
     */
    public ImagePanel(ImageIcon icon, ImagePanelConfig props) {
        this(null, icon, props);
    }

    /**
     * Invoked internally to set up a new ImagePanel with EITHER the given BufferedImage
     * or the given ImageIcon - one of those two must be null, as both can't be displayed.
     *
     * @param image A BufferedImage to display. Must be null if icon is set.
     * @param icon  An ImageIcon to display. Must be null if image is set.
     * @param props The ImagePanelConfig to use. Can be null for default configuration.
     */
    protected ImagePanel(BufferedImage image, ImageIcon icon, ImagePanelConfig props) {
        setLayout(null);
        zoomFactor = 1.0;
        extraAttributes = new HashMap<>();
        if (props == null) {
            props = ImagePanelConfig.createDefaultProperties();
        }

        final ImagePanel thisPanel = this;
        dBuffer = image;
        imageIcon = icon;
        lastRenderedImageWidth = 0;
        lastRenderedImageHeight = 0;
        imageIconLabel = new JLabel("");
        imageIconLabel.setIcon(imageIcon);
        add(imageIconLabel);
        thisPanel.applyProperties(props);

        // Apply all mouse listeners:
        addMouseListener(thisPanel);
        addMouseWheelListener(thisPanel);
        addMouseMotionListener(thisPanel);

        // Allow redispatching of mouse events to parent components:
        addMouseListener(new RedispatchingMouseAdapter());
        addMouseWheelListener(new RedispatchingMouseAdapter());
        addMouseMotionListener(new RedispatchingMouseAdapter());

        // Allow resizing to auto adjust the image being displayed:
        thisPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                //@formatter:off
                switch (thisPanel.originalProperties.getDisplayMode()) {
                    case BEST_FIT: thisPanel.zoomBestFit(); break;
                    case STRETCH: thisPanel.stretchImage(); break;
                }
                //@formatter:on
            }
        });

        LookAndFeelManager.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                ImagePanel.super.setBackground(LookAndFeelManager.getLafColor("Panel.background", Color.DARK_GRAY));
            }
        });
    }

    /**
     * Applies the configuration options in the given ImagePanelConfig instance to this panel.
     * Changes take effect immediately and do not require a manual call to repaint(). If the
     * supplied ImagePanelConfig is null, a default properties instance will be used instead.
     *
     * @param props The ImagePanelConfig instance containing our new configuration.
     */
    public void applyProperties(ImagePanelConfig props) {
        if (props == null) {
            props = ImagePanelConfig.createDefaultProperties();
        }
        this.properties = ImagePanelConfig.cloneProperties(props); // make a copy
        this.originalProperties = props; // store the original in case we need to muck with our copy

        // Cosmetic properties:
        this.setBackground(properties.getBgColor());

        // Update zoom settings if needed:
        //@formatter:off
        switch (properties.getDisplayMode()) {
            case BEST_FIT: zoomBestFit(); break;
            case STRETCH: stretchImage(); break;

            case CENTER:
            case NONE:
            case CUSTOM:
            default:
                zoomFactor = 1.0;
                resetZoomCenter();
                break;
        }
        //@formatter:on

        // Set cursor as appropriate:
        if ((properties.isEnableZoomOnMouseClick()
                || properties.isEnableZoomOnMouseWheel())
                && properties.getMagnifierCursor() != null) {
            setCursor(properties.getMagnifierCursor());
        }
        else {
            setCursor(Cursor.getDefaultCursor());
        }
        if (!properties.isEnableMouseCursor()) {
            setCursor(properties.getNullCursor());
        }

        repaint();
    }

    /**
     * Sets an optional "extra attribute", which can be anything caller-defined. Can be retrieved
     * later by getExtraAttribute.
     *
     * @param name  The name of this extra attribute. Case insensitive.
     * @param value The value of this extra attribute. Accepted as-is and not modified by this class.
     */
    public void setExtraAttribute(String name, Object value) {
        extraAttributes.put(name.toLowerCase(), value);
    }

    /**
     * Returns the value of the named extra attribute, if it exists.
     *
     * @param name The name of the extra attribute. Case insensitive.
     * @return The named extra attribute, or null if no such attribute is set.
     */
    public Object getExtraAttribute(String name) {
        return extraAttributes.get(name.toLowerCase());
    }

    /**
     * Clears the list of extra attributes.
     */
    public void clearExtraAttributes() {
        extraAttributes.clear();
    }

    /**
     * Returns the width of the current BufferedImage or the current ImageIcon, depending
     * on which one is set. If neither is set, returns 0.
     *
     * @return The width of the current image, or 0 if no image is shown.
     */
    public int getImageWidth() {
        if (dBuffer != null) {
            return dBuffer.getWidth();
        }
        else if (imageIcon != null) {
            return imageIcon.getIconWidth();
        }
        return 0;
    }

    /**
     * Returns the height of the current BufferedImage or the current ImageIcon, depending
     * on which one is set. If neither is set, returns 0.
     *
     * @return The height of the current image, or 0 if no image is shown.
     */
    public int getImageHeight() {
        if (dBuffer != null) {
            return dBuffer.getHeight();
        }
        else if (imageIcon != null) {
            return imageIcon.getIconHeight();
        }
        return 0;
    }

    /**
     * Sets the factor for zooming - this is scaled around 1.0 being synonymous with "no zoom".
     * A value of 0.5 means half normal size, a value of 2.0 means double normal size, and so on.
     * Setting any value here will automatically disable autoBestFit. To re-enable best bit,
     * you can either re-apply the ImagePanelConfig or simply call zoomBestFit in this class.
     *
     * @param newFactor The new zoom factor.
     */
    public void setZoomFactor(double newFactor) {
        this.zoomFactor = newFactor;

        // Put a floor on it:
        if (this.zoomFactor <= 0.1) {
            this.zoomFactor = 0.1;
        }

        // Put a ceiling on it for animated images:
        if (imageIcon != null) {
            float maxHorizontalZoom = ((float)getWidth()) / imageIcon.getIconWidth();
            float maxVerticalZoom = ((float)getHeight()) / imageIcon.getIconHeight();
            if (zoomFactor > Math.min(maxHorizontalZoom, maxVerticalZoom)) {
                zoomFactor = Math.min(maxHorizontalZoom, maxVerticalZoom);
            }
        }

        mouseDragPoint = null;
        mouseDragDelta = null;
        properties.setDisplayMode(ImagePanelConfig.DisplayMode.CUSTOM);
        repaint();
    }

    /**
     * Returns the current zoom factor.
     *
     * @return The current zoom factor. See setZoomFactor for more information.
     */
    public double getZoomFactor() {
        return zoomFactor;
    }

    /**
     * Zooms in around the given point. Invoking this will automatically disable autoBestFit.
     * To re-enable it, try zoomBestFit() or re-apply your ImagePanelConfig.
     *
     * @param point The Point around which to zoom. To zoom to the center, use zoomIn() instead.
     */
    public void zoomIn(Point point) {
        this.zoomCenter = new Point(point);
        setZoomFactor(zoomFactor + properties.getZoomFactorIncrement());
        repaint();
    }

    /**
     * Zooms in towards the center of the image. Invoking this will automatically disable
     * autoBestFit. To re-enable best fit, try zoomBestFit() or re-apply your ImagePanelConfig().
     */
    public void zoomIn() {
        resetZoomCenter();
        zoomIn(zoomCenter);
    }

    /**
     * Zooms out from the given point. Invoking this will automatically disable
     * autoBestFit. To re-enable best fit, try zoomBestFit() or re-apply your ImagePanelConfig().
     *
     * @param point The point around which to zoom out. To zoom out from center, try zoomOut instead.
     */
    public void zoomOut(Point point) {
        this.zoomCenter = new Point(point);
        setZoomFactor(zoomFactor - properties.getZoomFactorIncrement());
        repaint();
    }

    /**
     * Zooms out from the center of the image. Invoking this will automatically disable
     * autoBestFit. To re-enable best fit, try zoomBestFit() or re-apply your ImagePanelConfig().
     */
    public void zoomOut() {
        resetZoomCenter();
        zoomOut(zoomCenter);
    }

    /**
     * Enables (or re-enables) autoBestFit, and renders the image to fit the panel.
     */
    public void zoomBestFit() {
        properties.setDisplayMode(ImagePanelConfig.DisplayMode.BEST_FIT);
        repaint();
    }

    /**
     * Enabled (or re-enabled) STRETCH DisplayMode, and distorts the image to fit the panel.
     */
    public void stretchImage() {
        properties.setDisplayMode(ImagePanelConfig.DisplayMode.STRETCH);
        repaint();
    }

    /**
     * Sets the point around which to base zoom operations. Only used if displayMode is CUSTOM.
     * By default, the zoom center is the center of the image.
     *
     * @param point The new point within the image around which to base zoom in/zoom out operations.
     */
    public void setZoomCenter(Point point) {
        this.zoomCenter = new Point(point);
        repaint();
    }

    /**
     * Returns the current zoom point, if set.
     *
     * @return The current point around which to base zoom operations, or null if not set.
     */
    public Point getZoomCenter() {
        return new Point(zoomCenter);
    }

    /**
     * Resets the zoom center to be the center of the image.
     */
    public void resetZoomCenter() {
        if (dBuffer == null) {
            zoomCenter = new Point(0, 0);
            return;
        }

        int imgWidth = dBuffer.getWidth(this);
        int imgHeight = dBuffer.getHeight(this);
        zoomCenter = new Point(imgWidth / 2, imgHeight / 2);
        repaint();
    }

    /**
     * Takes a co-ordinate inside this panel and translates it into a co-ordinate inside the image,
     * taking current zoom factor and location into account.
     *
     * @param p The point inside this panel
     * @return A point inside the image that corresponds to the input point.
     */
    public Point getTranslatedPoint(Point p) {
        return translatePoint(p);
    }

    /**
     * Replaces the current image with the given one. If autoBestFit is enabled, the image
     * will be scaled up or down as needed. Else it is simply shown at scale factor 1.
     *
     * @param image The new image to be displayed, or null.
     */
    public void setImage(BufferedImage image) {
        dBuffer = image;
        imageIcon = null;
        imageIconLabel.setIcon(null);
        lastRenderedImageWidth = 0;
        lastRenderedImageHeight = 0;

        // If the image is null, disable our popup menu:
        if (image == null) {
            setComponentPopupMenu(null);
        }

        // Otherwise, register our popup menu:
        else {
            setComponentPopupMenu(popupMenu);
        }

        //@formatter:off
        switch (originalProperties.getDisplayMode()) {
            case BEST_FIT: zoomBestFit(); break;
            case STRETCH: stretchImage(); break;
            default: setZoomFactor(1.0); break;
        }
        //@formatter:on

        repaint();
    }

    public void setImageIcon(ImageIcon icon) {
        imageIcon = icon;
        imageIconLabel.setIcon(icon);
        dBuffer = null;
        lastRenderedImageWidth = 0;
        lastRenderedImageHeight = 0;

        // If the image is null, disable our popup menu:
        if (icon == null) {
            setComponentPopupMenu(null);
        }

        // Otherwise, register our popup menu:
        else {
            setComponentPopupMenu(popupMenu);
            imageIconLabel.setComponentPopupMenu(popupMenu);
        }

        //@formatter:off
        switch (originalProperties.getDisplayMode()) {
            case BEST_FIT: zoomBestFit(); break;
            case STRETCH: stretchImage(); break;
            default: setZoomFactor(1.0); break;
        }
        //@formatter:on

        repaint();
    }

    /**
     * Returns the current BufferedImage, if one is set.
     *
     * @return The BufferedImage currently being displayed in this panel, if there is one.
     */
    public BufferedImage getImage() {
        return dBuffer;
    }

    /**
     * Returns the current ImageIcon, if one is set.
     *
     * @return The ImageIcon being displayed in this panel, if there is one.
     */
    public ImageIcon getImageIcon() {
        return imageIcon;
    }

    /**
     * Overridden from JComponent, this method renders the image using current options.
     *
     * @param g The Graphics object to use for rendering.
     */
    @Override
    public void paintComponent(Graphics g) {

        // Our parent class can handle basic painting first:
        super.paintComponent(g);

        // If we have no image, we're done here:
        if (dBuffer == null && imageIcon == null) {
            return;
        }

        int srcImgWidth = dBuffer == null ? imageIcon.getIconWidth() : dBuffer.getWidth();
        int srcImgHeight = dBuffer == null ? imageIcon.getIconHeight() : dBuffer.getHeight();

        // If the zoomCenter has not been set yet, default to center:
        if (zoomCenter == null) {
            resetZoomCenter();
        }

        // If we have a border set, we have to adjust for its insets.
        // Otherwise, we might overdraw the image on top of the border.
        int borderOffsetLeft = 0;
        int borderOffsetTop = 0;
        int borderWidthTotal = 0;
        int borderHeightTotal = 0;
        if (getBorder() != null) {
            // We have a border! Let's be careful to take its size into account.
            // The imageX and imageY calculations further down will use these offsets,
            // and we'll also use this to computer our myWidth and myHeight values later.
            borderOffsetLeft = getBorder().getBorderInsets(this).left;
            borderOffsetTop = getBorder().getBorderInsets(this).top;
            int borderOffsetRight = getBorder().getBorderInsets(this).right;
            int borderOffsetBottom = getBorder().getBorderInsets(this).bottom;
            borderWidthTotal = borderOffsetLeft + borderOffsetRight;
            borderHeightTotal = borderOffsetTop + borderOffsetBottom;
        }

        // Gather image information:
        Graphics2D graphics2D = (Graphics2D)g;
        int myWidth = getWidth() - borderWidthTotal;
        int myHeight = getHeight() - borderHeightTotal;
        int imgWidth = (int)(srcImgWidth * zoomFactor);
        int imgHeight = (int)(srcImgHeight * zoomFactor);

        // If best fit is enabled, we need to determine the zoom factor to use:
        if (properties.getDisplayMode() == ImagePanelConfig.DisplayMode.BEST_FIT) {
            imgWidth = srcImgWidth;
            imgHeight = srcImgHeight;
            float imgAspect = (float)imgWidth / (float)imgHeight;
            float myAspect = (float)myWidth / (float)myHeight;
            if (imgAspect >= 1.0) {
                if (myAspect >= imgAspect) {
                    zoomFactor = (double)myHeight / imgHeight;
                }
                else {
                    zoomFactor = (double)myWidth / imgWidth;
                }
            }
            else {
                if (myAspect <= imgAspect) {
                    zoomFactor = (double)myWidth / imgWidth;
                }
                else {
                    zoomFactor = (double)myHeight / imgHeight;
                }
            }

            if (zoomFactor <= 0.0) {
                zoomFactor = 1;
            }

            // Now disable best fit so we don't need to do this again:
            properties.setDisplayMode(ImagePanelConfig.DisplayMode.CUSTOM);
            imgWidth *= zoomFactor;
            imgHeight *= zoomFactor;
        }

        // If we're stretching the image, distort it to fill the panel:
        else if (properties.getDisplayMode() == ImagePanelConfig.DisplayMode.STRETCH
                || originalProperties.getDisplayMode() == ImagePanelConfig.DisplayMode.STRETCH) {
            imgWidth = myWidth;
            imgHeight = myHeight;
            properties.setDisplayMode(ImagePanelConfig.DisplayMode.CUSTOM);
        }

        // Figure out the center point:
        int centerX = myWidth / 2;
        int centerY = myHeight / 2;
        imageX = centerX - (int)(zoomCenter.getX() * zoomFactor);
        imageY = centerY - (int)(zoomCenter.getY() * zoomFactor);

        // Apply mouse dragging, if enabled and in progress:
        if (mouseDragPoint != null && properties.isEnableMouseDragging()) {
            imageX += mouseDragDelta.x;
            imageY += mouseDragDelta.y;
        }

        // Handle horizontal overflow:
        if (imgWidth >= myWidth) {
            if (imageX > borderOffsetLeft) {
                imageX = borderOffsetLeft;
            }
            else if (imageX < (borderOffsetLeft + myWidth - imgWidth)) {
                imageX = borderOffsetLeft + myWidth - imgWidth;
            }
        }

        // Handle vertical overflow:
        if (imgHeight >= myHeight) {
            if (imageY > borderOffsetTop) {
                imageY = borderOffsetTop;
            }
            else if (imageY < (borderOffsetTop + myHeight - imgHeight)) {
                imageY = borderOffsetTop + myHeight - imgHeight;
            }
        }

        // Handle image placement (already calculated above, tweak it if need be):
        switch (properties.getDisplayMode()) {

            // Unconditionally place it at 0,0 (adjusted for border):
            case NONE:
            case STRETCH:
                imageX = borderOffsetLeft;
                imageY = borderOffsetTop;
                break;

            // Otherwise, try to keep it centered:
            default:
                imageX = (imgWidth < myWidth) ? borderOffsetLeft + (int)((myWidth - imgWidth) / 2) : borderOffsetLeft;
                imageY = (imgHeight < myHeight) ? borderOffsetTop + (int)((myHeight - imgHeight) / 2) : borderOffsetTop;
                break;
        }

        // Set Rendering quality, draw the image, and we're done:
        if (dBuffer != null) {
            setRenderingQuality(graphics2D);
            graphics2D.drawImage(dBuffer, imageX, imageY, imgWidth, imgHeight, null);

            // DON'T dispose the graphics object we were given!
            // This will prevent further painting operations from working correctly.
            //graphics2D.dispose();
        }
        else {
            if (lastRenderedImageWidth != imgWidth || lastRenderedImageHeight != imgHeight) {
                imageIconLabel.setIcon(new ImageIcon(
                        imageIcon.getImage().getScaledInstance(imgWidth, imgHeight, Image.SCALE_DEFAULT)));
                lastRenderedImageWidth = imgWidth;
                lastRenderedImageHeight = imgHeight;
            }
            imageIconLabel.setBounds(imageX, imageY, imgWidth, imgHeight);
        }
    }

    /**
     * Internal method to set rendering quality hints on a given Graphics2D instance
     * based on the current ImagePanelConfig.getRenderingQuality() value.
     *
     * @param graphics2D The Graphics2D object to update.
     */
    protected void setRenderingQuality(Graphics2D graphics2D) {
        switch (properties.getRenderingQuality()) {
            case SLOW_AND_ACCURATE: {
                graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                            RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                graphics2D.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                                            RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING,
                                            RenderingHints.VALUE_RENDER_QUALITY);
            }
            break;

            case QUICK_AND_DIRTY: {
                graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                graphics2D.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                                            RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
                graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING,
                                            RenderingHints.VALUE_RENDER_SPEED);
            }
            break;
        }
    }

    /**
     * Takes a co-ordinate inside this panel and translates it into a co-ordinate inside the image,
     * taking current zoom factor and location into account.
     *
     * @param p The point inside this panel
     * @return A point inside the image that corresponds to the input point.
     */
    protected Point translatePoint(Point p) {

        int translatedX = p.x - imageX;
        int translatedY = p.y - imageY;

        if (mouseDragPoint != null && mouseDragDelta != null && properties.isEnableMouseDragging()) {
            translatedX += mouseDragDelta.x * zoomFactor;
            translatedY += mouseDragDelta.y * zoomFactor;
        }

        translatedX /= zoomFactor;
        translatedY /= zoomFactor;

        return new Point(translatedX, translatedY);
    }

    /**
     * Invoked automatically when the user clicks on the panel. If click to zoom is enabled
     * in our properties, you can left click to zoom in and right click to zoom out. Otherwise
     * this does nothing.
     *
     * @param e The MouseEvent in question.
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        if (!properties.isEnableZoomOnMouseClick()) {
            return;
        }

        if (SwingUtilities.isLeftMouseButton(e)) {
            zoomIn(translatePoint(e.getPoint()));
        }
        else if (SwingUtilities.isRightMouseButton(e)) {
            zoomOut(translatePoint(e.getPoint()));
        }
    }

    /**
     * Invoked automatically when the user presses a mouse button over the panel. If mouse
     * dragging is enabled, you can scroll the image by clicking and dragging it (only if the
     * image is larger than the panel or is zoomed in to that extent). If mouse dragging is
     * disabled, or if the image is smaller than the panel, this does nothing.
     *
     * @param e The MouseEvent in question.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        if (!properties.isEnableMouseDragging()) {
            mouseDragPoint = null;
            mouseDragDelta = null;
            return;
        }

        mouseDragPoint = e.getPoint();
        if (mouseDragDelta == null) {
            mouseDragDelta = new Point(0, 0);
        }
    }

    /**
     * Ignored; does nothing.
     *
     * @param e Ignored.
     */
    @Override
    public void mouseReleased(MouseEvent e) {
    }

    /**
     * Ignored; does nothing.
     *
     * @param e Ignored.
     */
    @Override
    public void mouseEntered(MouseEvent e) {
    }

    /**
     * Ignored; does nothing.
     *
     * @param e Ignored.
     */
    @Override
    public void mouseExited(MouseEvent e) {
    }

    /**
     * Invoked automatically when the user mouse wheels while over this panel. If zoom on
     * mouse wheel is enabled, you can zoom in by wheeling up and zoom out by wheeling down.
     * If zoom on mouse wheel is disabled, this does nothing.
     *
     * @param e The MouseEvent in question.
     */
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (!properties.isEnableZoomOnMouseWheel()) {
            return;
        }

        if (e.getWheelRotation() < 0) {
            zoomIn(translatePoint(e.getPoint()));
        }
        else {
            zoomOut(translatePoint(e.getPoint()));
        }
    }

    /**
     * Invoked automatically when the user drags the mouse on this panel. If mouse dragging
     * is enabled, you can click and drag to scroll the image around in cases where the image
     * is larger than the panel (or is zoomed in to that extent). If mouse dragging is disabled,
     * or if the image is smaller than the panel, this does nothing.
     *
     * @param e The MouseEvent in question.
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        if (dBuffer == null) {
            return;
        }

        if (mouseDragPoint == null) {
            mouseDragPoint = e.getPoint();
            mouseDragDelta = new Point(0, 0);
        }

        int deltaX = (int)(e.getPoint().getX() - mouseDragPoint.getX());
        int deltaY = (int)(e.getPoint().getY() - mouseDragPoint.getY());
        int limitDragX = (int)(dBuffer.getWidth() * zoomFactor) - getWidth();
        int limitDragY = (int)(dBuffer.getHeight() * zoomFactor) - getHeight();
        if (deltaX < 0 && mouseDragDelta.x > -limitDragX) {
            mouseDragDelta.x += deltaX;
        }
        else if (deltaX > 0 && mouseDragDelta.x < limitDragX) {
            mouseDragDelta.x += deltaX;
        }
        if (deltaY < 0 && mouseDragDelta.y > -limitDragY) {
            mouseDragDelta.y += deltaY;
        }
        else if (deltaY > 0 && mouseDragDelta.y < limitDragY) {
            mouseDragDelta.y += deltaY;
        }
        mouseDragPoint = e.getPoint();
        repaint();
    }

    /**
     * Ignored; does nothing.
     *
     * @param e Ignored.
     */
    @Override
    public void mouseMoved(MouseEvent e) {
    }

    /**
     * Associates the given JPopupMenu with this ImagePanel instance. The popup menu will
     * only pop if the current image is not null.
     *
     * @param menu The new JPopupMenu to use. Can be null to disable popup menu.
     */
    public void setPopupMenu(JPopupMenu menu) {
        this.popupMenu = menu;

        // Unregister any old menu:
        setComponentPopupMenu(null);
        imageIconLabel.setComponentPopupMenu(null);

        // If our image is not null, register this menu:
        if (dBuffer != null || imageIcon != null) {
            setComponentPopupMenu(popupMenu);
            imageIconLabel.setComponentPopupMenu(popupMenu);
        }
    }

    @Override
    public void addMouseListener(MouseListener listener) {
        super.addMouseListener(listener);
        imageIconLabel.addMouseListener(listener);
    }

    @Override
    public void removeMouseListener(MouseListener listener) {
        super.removeMouseListener(listener);
        imageIconLabel.removeMouseListener(listener);
    }

}
