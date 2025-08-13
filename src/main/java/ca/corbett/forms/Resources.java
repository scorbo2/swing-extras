package ca.corbett.forms;

import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.net.URL;

/**
 * Centralizes and manages access to the various resource images that are used in swing-forms.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.4
 */
public final class Resources {

    private static final String PREFIX = "/ca/corbett/swing-forms/";
    private static final String VALID = PREFIX + "/images/formfield-valid.png";
    private static final String INVALID = PREFIX + "/images/formfield-invalid.png";
    private static final String HELP = PREFIX + "/images/formfield-help.png";
    private static final String BLANK = PREFIX + "/images/formfield-blank.png";

    private static final ImageIcon validIcon;
    private static final ImageIcon invalidIcon;
    private static final ImageIcon helpIcon;
    private static final ImageIcon blankIcon;

    static {
        URL url = Resources.class.getResource(VALID);
        validIcon = url == null ? createIcon(Color.GREEN) : new ImageIcon(url);

        url = Resources.class.getResource(INVALID);
        invalidIcon = url == null ? createIcon(Color.RED) : new ImageIcon(url);

        url = Resources.class.getResource(HELP);
        helpIcon = url == null ? createIcon(Color.YELLOW) : new ImageIcon(url);

        url = Resources.class.getResource(BLANK);
        blankIcon = url == null ? createIcon(new Color(0, 0, 0, 0)) : new ImageIcon(url);
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
     * Returns a transparent ImageIcon of the same dimensions as the other formfield icons.
     * This is useful for reserving space where an icon should go.
     */
    public static ImageIcon getBlankIcon() {
        return blankIcon;
    }

    /**
     * Cheesy fallback in case our images can't be loaded for some reason.
     */
    private static ImageIcon createIcon(Color color) {
        BufferedImage image = new BufferedImage(22, 22, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, 22, 22);
        g.dispose();
        return new ImageIcon(image);
    }
}
