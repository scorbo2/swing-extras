package ca.corbett.extras;

import ca.corbett.extras.gradient.Gradient;
import ca.corbett.extras.gradient.GradientUtil;
import ca.corbett.extras.properties.Properties;

import javax.swing.JDesktopPane;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A customized JDesktopPane that allows setting a customized background
 * instead of the default one provided by Java. The background can be set as
 * a solid color or as a color gradient. An optional logo image can also
 * be displayed with configurable positioning and transparency.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class CustomizableDesktopPane extends JDesktopPane {

    /**
     * Provides a way to choose where the logo image should be displayed, if at all.
     */
    public enum LogoPlacement {
        TOP_LEFT("Top left"),
        TOP_RIGHT("Top right"),
        CENTER("Center"),
        BOTTOM_LEFT("Bottom left"),
        BOTTOM_RIGHT("Bottom right"),
        OFF("No logo image");

        private final String label;

        LogoPlacement(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }

        public static LogoPlacement fromLabel(String l) {
            for (LogoPlacement candidate : values()) {
                if (candidate.label.equals(l)) {
                    return candidate;
                }
            }
            return null;
        }
    }

    private static final Logger logger = Logger.getLogger(CustomizableDesktopPane.class.getName());
    private LogoPlacement logoPlacement;
    private Gradient gradientConfig;
    private Color bgColor;
    private final BufferedImage logoImage;
    private float logoImageAlpha;

    /**
     * Creates a new CustomizableDesktopPane with the specified initial GradientConfig and no logo image.
     *
     * @param gradient The GradientConfig to use initially for drawing the background.
     */
    public CustomizableDesktopPane(Gradient gradient) {
        this(null, LogoPlacement.OFF, 0f, gradient);
    }

    /**
     * Creates a new CustomizableDesktopPane with the specified initial solid background color
     * and no logo image.
     */
    public CustomizableDesktopPane(Color bgColor) {
        this(null, LogoPlacement.OFF, 0f, bgColor);
    }

    /**
     * Creates a new CustomizableDesktopPane with the specified logo image and placement, and
     * the specified initial GradientConfig. The GradientConfig can be modified later
     * via setGradientConfig(), but the logo image cannot be changed once set.
     *
     * @param image     The logo image to render in the background.
     * @param placement Where on the desktop the logo image should be rendered.
     * @param alpha     The transparency value to use for the logo image (0=invisible, 1=opaque).
     * @param gradient  The GradientConfig to use initially for drawing the background.
     */
    public CustomizableDesktopPane(BufferedImage image, LogoPlacement placement, float alpha, Gradient gradient) {
        super();

        this.logoImage = image;
        this.logoPlacement = placement;
        this.logoImageAlpha = alpha;
        this.gradientConfig = gradient;
        this.bgColor = null;

        logger.log(Level.FINE, "CustomizableDesktopPane created: logo:{0}, {1}, {2}, {3}",
                   new Object[]{logoImage == null ? "no" : "yes",
                           logoPlacement.name(),
                           logoImageAlpha,
                           gradientConfig.type().name()});
    }

    /**
     * Creates a new CustomizableDesktopPane with the specified logo image and placement, and
     * the specified initial solid background color.
     */
    public CustomizableDesktopPane(BufferedImage image, LogoPlacement placement, float alpha, Color bgColor) {
        super();

        this.logoImage = image;
        this.logoPlacement = placement;
        this.logoImageAlpha = alpha;
        this.gradientConfig = null;
        this.bgColor = bgColor;

        logger.log(Level.FINE, "CustomizableDesktopPane created: logo:{0}, {1}, {2}, {3}",
                   new Object[]{logoImage == null ? "no" : "yes",
                           logoPlacement.name(),
                           logoImageAlpha,
                           Properties.encodeColor(bgColor)});
    }

    /**
     * Adjusts the transparency of the logo image, if a logo image is being shown.
     *
     * @param alpha The transparency value for the logo image (0=invisible, 1=opaque).
     */
    public void setLogoImageTransparency(float alpha) {
        logoImageAlpha = alpha;
        logger.log(Level.FINE, "CustomizableDesktopPane: logo alpha set to {0}", alpha);
        redraw();
    }

    /**
     * Sets the GradientConfig to use for drawing the desktop background.
     *
     * @param conf The new GradientConfig to use.
     */
    public void setGradientConfig(Gradient conf) {
        gradientConfig = conf;
        bgColor = null;
        logger.fine("CustomizableDesktopPane: new GradientConfig set");
        redraw();
    }

    /**
     * Returns the currently set gradient config for the background, or null if the
     * background is currently set to a solid color.
     */
    public Gradient getGradientConfig() {
        return gradientConfig;
    }

    /**
     * Sets the solid color to use for drawing the desktop background.
     */
    public void setBgSolidColor(Color bgColor) {
        gradientConfig = null;
        this.bgColor = bgColor;
        logger.fine("CustomizableDesktopPane: new solid background color set");
        redraw();
    }

    /**
     * Returns the currently set solid background color, or null if the
     * background is currently set to a gradient.
     */
    public Color getBgSolidColor() {
        return bgColor;
    }

    /**
     * Sets the LogoPlacement for use with the logo image.
     *
     * @param placement The new position for the logo image.
     */
    public void setLogoImagePlacement(LogoPlacement placement) {
        logoPlacement = placement;
        logger.log(Level.FINE, "CustomizableDesktopPane: logo image placement set to {0}", placement.label);
        redraw();
    }

    /**
     * Overridden to allow rendering of our custom desktop background.
     *
     * @param g The Graphics object to use for rendering.
     */
    @Override
    public void paintComponent(Graphics g) {
        if (gradientConfig != null) {
            GradientUtil.fill(gradientConfig, (Graphics2D)g, 0, 0, getWidth(), getHeight());
        }
        else {
            g.setColor(bgColor);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        if (logoImage != null && logoPlacement != LogoPlacement.OFF) {
            int x = 0;
            int y = 0;
            int margin = logoImage.getWidth() / 10;
            y = switch (logoPlacement) {
                case TOP_LEFT -> {
                    x = margin;
                    yield margin;
                }
                case TOP_RIGHT -> {
                    x = getWidth() - logoImage.getWidth() - margin;
                    yield margin;
                }
                case BOTTOM_LEFT -> {
                    x = margin;
                    yield getHeight() - logoImage.getHeight() - margin;
                }
                case BOTTOM_RIGHT -> {
                    x = getWidth() - logoImage.getWidth() - margin;
                    yield getHeight() - logoImage.getHeight() - margin;
                }
                case CENTER -> {
                    x = (getWidth() / 2) - (logoImage.getWidth() / 2);
                    yield (getHeight() / 2) - (logoImage.getHeight() / 2);
                }
                default -> y;
            };
            Composite composite = ((Graphics2D)g).getComposite();
            ((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, logoImageAlpha));
            g.drawImage(logoImage, x, y, null);
            ((Graphics2D)g).setComposite(composite);
        }
    }

    /**
     * Invoked internally when some change is made that requires an immediate redraw.
     */
    private void redraw() {
        invalidate();
        revalidate();
        repaint();
    }
}
