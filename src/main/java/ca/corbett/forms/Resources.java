package ca.corbett.forms;

import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.net.URL;
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

    private static final String PREFIX = "ca/corbett/swing-forms";
    private static final String VALID = PREFIX + "/images/formfield-valid.png";
    private static final String INVALID = PREFIX + "/images/formfield-invalid.png";
    private static final String HELP = PREFIX + "/images/formfield-help.png";
    private static final String BLANK = PREFIX + "/images/formfield-blank.png";

    private final ImageIcon validIcon;
    private final ImageIcon invalidIcon;
    private final ImageIcon helpIcon;
    private final ImageIcon blankIcon;

    private Resources() {
        ClassLoader classLoader = Resources.class.getClassLoader();
        validIcon = loadIcon(VALID, classLoader.getResource(VALID), Color.GREEN);
        invalidIcon = loadIcon(INVALID, classLoader.getResource(INVALID), Color.RED);
        helpIcon = loadIcon(HELP, classLoader.getResource(HELP), Color.YELLOW);
        blankIcon = loadIcon(HELP, classLoader.getResource(BLANK), new Color(0, 0, 0, 0));
    }

    private ImageIcon loadIcon(String path, URL url, Color defaultColor) {
        if (url == null) {
            log.severe("Unable to load resource " + path);
            return createIcon(defaultColor);
        }
        return new ImageIcon(url);
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
     * Cheesy fallback in case our images can't be loaded for some reason.
     */
    private ImageIcon createIcon(Color color) {
        BufferedImage image = new BufferedImage(22, 22, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, 22, 22);
        g.dispose();
        return new ImageIcon(image);
    }

    /**
     * Ignore this - just debugging weird classloader issue in client apps.
     */
    private void debug() {
        System.out.println("=== Classloader Investigation ===");

        // Check what classloader loaded us
        ClassLoader demoAppCL = Resources.class.getClassLoader();
        System.out.println("Resources classloader: " + demoAppCL);
        System.out.println("Resources classloader class: " + demoAppCL.getClass().getName());

        // Check the classloader hierarchy
        ClassLoader current = demoAppCL;
        int level = 0;
        while (current != null) {
            System.out.println("Level " + level + ": " + current + " (" + current.getClass().getName() + ")");
            current = current.getParent();
            level++;
        }

        // Check where Resources class is actually loaded from
        URL classLocation = Resources.class.getProtectionDomain().getCodeSource().getLocation();
        System.out.println("DemoApp loaded from: " + classLocation);

        // Try to find the resource using the same classloader that loaded DemoApp
        URL resourceUrl = demoAppCL.getResource(VALID);
        System.out.println("Resource URL: " + resourceUrl);
    }
}
