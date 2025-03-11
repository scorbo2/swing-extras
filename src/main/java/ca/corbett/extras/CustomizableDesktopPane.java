package ca.corbett.extras;

import ca.corbett.extras.gradient.GradientConfig;
import ca.corbett.extras.gradient.GradientUtil;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDesktopPane;
import javax.swing.SwingUtilities;

/**
 * A customized JDesktopPane that allows setting a customized background
 * instead of the default one provided by Java.
 *
 * @author scorbett
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

        private LogoPlacement(String label) {
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

    };

    private static final Logger logger = Logger.getLogger(CustomizableDesktopPane.class.getName());
    private LogoPlacement logoPlacement;
    private GradientConfig gradientConfig;
    private final BufferedImage logoImage;
    private float logoImageAlpha;

    /**
     * Creates a new CustomizableDesktopPane with the specified initial GradientConfig and no
     * logo image. The GradientConfig can be modified later via setGradientConfig().
     *
     * @param gradient The GradientConfig to use initially for drawing the background.
     */
    public CustomizableDesktopPane(GradientConfig gradient) {
        this(null, LogoPlacement.OFF, 0f, gradient);
    }

    /**
     * Creates a new CustomizableDesktopPane with the specified logo image and placement, and
     * the specified initial GradientConfig. The GradientConfig can be modified later
     * via setGradientConfig(), but the logo image cannot be changed once set.
     *
     * @param image The logo image to render in the background.
     * @param placement Where on the desktop the logo image should be rendered.
     * @param alpha The transparency value to use for the logo image (0=invisible, 1=opaque).
     * @param gradient The GradientConfig to use initially for drawing the background.
     */
    public CustomizableDesktopPane(BufferedImage image, LogoPlacement placement, float alpha, GradientConfig gradient) {
        super();

        this.logoImage = image;
        this.logoPlacement = placement;
        this.logoImageAlpha = alpha;
        this.gradientConfig = new GradientConfig(gradient);

        logger.log(Level.FINE, "CustomizableDesktopPane created: logo:{0}, {1}, {2}, {3}",
                new Object[]{logoImage == null ? "no" : "yes",
                        logoPlacement.name(),
                        logoImageAlpha,
                        gradientConfig.getGradientType().name()});
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
    public void setGradientConfig(GradientConfig conf) {
        gradientConfig = new GradientConfig(conf);
        logger.fine("CustomizableDesktopPane: new GradientConfig set");
        redraw();
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
        GradientUtil.fill(gradientConfig, (Graphics2D)g, 0, 0, getWidth(), getHeight());

        if (logoImage != null && logoPlacement != LogoPlacement.OFF) {
            int x = 0;
            int y = 0;
            int margin = logoImage.getWidth() / 10;
            switch (logoPlacement) {
                case TOP_LEFT:
                    x = margin;
                    y = margin;
                    break;

                case TOP_RIGHT:
                    x = getWidth() - logoImage.getWidth() - margin;
                    y = margin;
                    break;

                case BOTTOM_LEFT:
                    x = margin;
                    y = getHeight() - logoImage.getHeight() - margin;
                    break;

                case BOTTOM_RIGHT:
                    x = getWidth() - logoImage.getWidth() - margin;
                    y = getHeight() - logoImage.getHeight() - margin;
                    break;

                case CENTER:
                    x = (getWidth() / 2) - (logoImage.getWidth() / 2);
                    y = (getHeight() / 2) - (logoImage.getHeight() / 2);
                    break;
            }
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
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                invalidate();
                revalidate();
                repaint();
            }

        });
    }

}
