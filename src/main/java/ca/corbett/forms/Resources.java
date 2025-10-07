package ca.corbett.forms;

import ca.corbett.extras.image.ImageUtil;

import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centralizes and manages access to the various resource images that are used in swing-forms.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.4
 */
public final class Resources {

    private static final Logger log = Logger.getLogger(Resources.class.getName());
    private static Resources instance = null;
    private static final int DEFAULT_SIZE = 22;

    private static final String PREFIX = "ca/corbett/swing-forms";
    private static final String VALID = PREFIX + "/images/formfield-valid.png";
    private static final String INVALID = PREFIX + "/images/formfield-invalid.png";
    private static final String HELP = PREFIX + "/images/formfield-help.png";
    private static final String BLANK = PREFIX + "/images/formfield-blank.png";
    private static final String PLUS = PREFIX + "/images/icon-plus.png";
    private static final String MINUS = PREFIX + "/images/icon-minus.png";

    private final ImageIcon validIcon;
    private final ImageIcon invalidIcon;
    private final ImageIcon helpIcon;
    private final ImageIcon blankIcon;
    private final ImageIcon plusIcon;
    private final ImageIcon minusIcon;

    private Resources() {
        ClassLoader classLoader = Resources.class.getClassLoader();
        validIcon = loadIcon(VALID, classLoader.getResource(VALID), Color.GREEN);
        invalidIcon = loadIcon(INVALID, classLoader.getResource(INVALID), Color.RED);
        helpIcon = loadIcon(HELP, classLoader.getResource(HELP), Color.YELLOW);
        blankIcon = loadIcon(HELP, classLoader.getResource(BLANK), new Color(0, 0, 0, 0));
        plusIcon = loadIcon(PLUS, classLoader.getResource(PLUS), Color.GRAY, 20);
        minusIcon = loadIcon(MINUS, classLoader.getResource(MINUS), Color.GRAY, 20);
    }

    private ImageIcon loadIcon(String path, URL url, Color defaultColor) {
        return loadIcon(path, url, defaultColor, 0);
    }

    private ImageIcon loadIcon(String path, URL url, Color defaultColor, int size) {
        if (url == null) {
            log.severe("Unable to load resource " + path);
            return createIcon(defaultColor);
        }
        if (size == 0) { // no scaling requested
            return new ImageIcon(url);
        }
        else {
            try {
                BufferedImage rawImage = ImageUtil.loadImage(url);
                return new ImageIcon(ImageUtil.generateThumbnailWithTransparency(rawImage, size, size));
            }
            catch (IOException ioe) {
                log.log(Level.SEVERE, "Caught IOException while loading resources: " + ioe.getMessage(), ioe);
                return createIcon(defaultColor);
            }
        }
    }

    private static Resources getInstance() {
        if (instance == null) {
            instance = new Resources();
        }
        return instance;
    }

    /**
     * Returns an ImageIcon to represent a "valid" FormField - that is, one that
     * has no validation errors.
     */
    public static ImageIcon getValidIcon() {
        return getInstance().validIcon;
    }

    /**
     * Returns an ImageIcon to represent an "invalid" FormField - that is, one that
     * has at least one validation error.
     */
    public static ImageIcon getInvalidIcon() {
        return getInstance().invalidIcon;
    }

    /**
     * Returns an ImageIcon to represent a FormField that has some help or informational
     * text associated with it.
     */
    public static ImageIcon getHelpIcon() {
        return getInstance().helpIcon;
    }

    /**
     * Returns a transparent ImageIcon of the same dimensions as the other formfield icons.
     * This is useful for reserving space where an icon should go.
     */
    public static ImageIcon getBlankIcon() {
        return getInstance().blankIcon;
    }

    /**
     * Returns an ImageIcon to represent a plus sign (expand, zoom in, etc).
     */
    public static ImageIcon getPlusIcon() {
        return getInstance().plusIcon;
    }

    /**
     * Returns an ImageIcon to represent a minus sign (collapse, zoom out, etc).
     */
    public static ImageIcon getMinusIcon() {
        return getInstance().minusIcon;
    }

    /**
     * Cheesy fallback in case our images can't be loaded for some reason.
     */
    private ImageIcon createIcon(Color color) {
        BufferedImage image = new BufferedImage(DEFAULT_SIZE, DEFAULT_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, DEFAULT_SIZE, DEFAULT_SIZE);
        g.dispose();
        return new ImageIcon(image);
    }
}
