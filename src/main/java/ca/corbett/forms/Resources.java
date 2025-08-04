package ca.corbett.forms;

import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.net.URL;

/**
 * Centralizes and manages access to the various resource images that are used in swing-forms.
 *
 * @author scorbo2
 * @since swing-extras 2.4
 */
public final class Resources {

    private static final String PREFIX = "/ca/corbett/swing-forms/";
    private static final String VALID = PREFIX + "/images/formfield-valid.png";
    private static final String INVALID = PREFIX + "/images/formfield-invalid.png";
    private static final String HELP = PREFIX + "/images/formfield-help.png";

    private static final ImageIcon validIcon;
    private static final ImageIcon invalidIcon;
    private static final ImageIcon helpIcon;

    static {
        URL url = Resources.class.getResource(VALID);
        validIcon = url == null ? createIcon(Color.GREEN) : new ImageIcon(url);

        url = Resources.class.getResource(INVALID);
        invalidIcon = url == null ? createIcon(Color.RED) : new ImageIcon(url);

        url = Resources.class.getResource(HELP);
        helpIcon = url == null ? createIcon(Color.YELLOW) : new ImageIcon(url);
    }

    /**
     * Returns an ImageIcon to represent a "valid" FormField - that is, one that
     * has no validation errors.
     */
    public static ImageIcon getValidIcon() {
        return validIcon;
    }

    /**
     * Returns an ImageIcon to represent an "invalid" FormField - that is, one that
     * has at least one validation error.
     */
    public static ImageIcon getInvalidIcon() {
        return invalidIcon;
    }

    /**
     * Returns an ImageIcon to represent a FormField that has some help or informational
     * text associated with it.
     */
    public static ImageIcon getHelpIcon() {
        return helpIcon;
    }

    /**
     * Cheesy fallback in case our images can't be loaded for some reason.
     */
    private static ImageIcon createIcon(Color color) {
        BufferedImage image = new BufferedImage(22, 22, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, 22, 22);
        g.dispose();
        return new ImageIcon(image);
    }
}
