package ca.corbett.extras;

import ca.corbett.extras.image.ImageUtil;

import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A generic utility class for managing access to resources packaged within a jar file.
 * You can optionally extend this class to provide static convenience methods for
 * specific resources used by your application. For example:
 * <pre>
 *     public class MyAppResourceLoader extends ResourceLoader {
 *         public static final String LOGO_IMG
 *                  = "com/mycompany/myapp/images/logo.png";
 *         public static final String RELEASE_NOTES
 *                  = "com/mycompany/myapp/docs/release-notes.txt";
 *
 *         public static BufferedImage getLogo() {
 *             return getImage(LOGO_IMG);
 *         }
 *
 *         public static String getReleaseNotes() {
 *             return getTextResource(RELEASE_NOTES);
 *         }
 *
 *         // And so on for your other resources...
 *     }
 * </pre>
 * <p>
 * You can optionally use setPrefix() to make it easier to load multiple resources from a common path.
 * For example:
 * </p>
 * <pre>
 *     ResourceLoader.setPrefix("com/mycompany/myapp/");
 *     BufferedImage logo = ResourceLoader.getImage("images/logo.png");
 *     String helpText = ResourceLoader.getTextResource("docs/help.txt");
 *     // And so on...
 * </pre>
 *
 * <h2>Application extensions</h2>
 * <p>
 * Application extensions can use this class to load resources from the application's jar file, but
 * they cannot use this to load resources from their own extension jar files. Extension developers
 * need to implement the loadJarResources() method in their AppExtension subclass and use
 * that class's class loader to load resources from the extension jar. Refer to the javadocs
 * in AppExtension and ExtensionManager for more details, or refer to the
 * <A href="https://www.corbett.ca/swing-extras-book/">swing-extras book</A> for a complete example.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.7
 */
public class ResourceLoader {

    private static final Logger log = Logger.getLogger(ResourceLoader.class.getName());
    protected static String prefix = "";

    protected ResourceLoader() {
    }

    /**
     * You can optionally set a prefix that will be prepended to all resource paths.
     * That way, your calling code can do getImage("icon.png") instead of
     * getImage("my/app/prefix/images/icon.png").
     * <p>
     * The default prefix is "", meaning all calls must specify the full resource path.
     * </p>
     *
     * @param prefix A resource path prefix common to all your resources, e.g. "/my/app/prefix/"
     */
    public static void setPrefix(String prefix) {
        ResourceLoader.prefix = prefix == null ? "" : prefix;
    }

    /**
     * Returns the current resource path prefix.
     */
    public static String getPrefix() {
        return prefix;
    }

    /**
     * Attempts to read the given text resource from the jar and return it as a List of lines.
     * You can use setPrefix() to avoid having to specify a full path each time.
     * Otherwise, the given path is expected to be the full resource path within the jar.
     * Will return null if the resource cannot be found.
     * If you request a resource that is not text, the result will be garbage.
     */
    public static List<String> getTextResourceAsLines(String resourcePath) {
        if (resourcePath == null) {
            log.warning("getTextResourceAsLines(null) invoked.");
        }
        String path = prefix + (resourcePath == null ? "" : resourcePath);
        URL url = ResourceLoader.class.getClassLoader().getResource(path);
        if (url == null) {
            log.severe("Unable to load text resource from path: " + path);
            return null;
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        }
        catch (IOException ioe) {
            log.log(Level.SEVERE, "Caught IOException while loading text resource: " + ioe.getMessage(), ioe);
            return null;
        }
    }

    /**
     * Attempts to read the given text resource from the jar and return it as a String.
     * You can use setPrefix() to avoid having to specify a full path each time.
     * Otherwise, the given path is expected to be the full resource path within the jar.
     * Will return null if the resource cannot be found.
     * If you request a resource that is not text, the result will be garbage.
     * <p>
     * Note: This method joins lines using the system line separator, regardless
     * of what line endings are present in the original resource. If you'd rather
     * process the lines separately, use getTextResourceAsLines() instead.
     * </P>
     */
    public static String getTextResource(String resourcePath) {
        List<String> lines = getTextResourceAsLines(resourcePath);
        if (lines == null) {
            return null;
        }
        return String.join(System.lineSeparator(), lines);
    }

    /**
     * Attempts to extract the given jar resource to the specified output file.
     * You can use setPrefix() to avoid having to specify a full path each time.
     * Otherwise, the given path is expected to be the full resource path within the jar.
     * Returns true if successful, false otherwise.
     */
    public static boolean extractResourceToFile(String resourcePath, File outFile) {
        if (resourcePath == null) {
            log.warning("extractResourceToFile(null, outFile) invoked.");
        }
        String path = prefix + (resourcePath == null ? "" : resourcePath);
        URL url = ResourceLoader.class.getClassLoader().getResource(path);
        if (url == null) {
            log.severe("Unable to load resource from path: " + path);
            return false;
        }
        if (outFile == null) {
            log.severe("Output file is null, cannot extract resource: " + path);
            return false;
        }
        try (BufferedInputStream in = new BufferedInputStream(url.openStream());
             FileOutputStream out = new FileOutputStream(outFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            return true;
        }
        catch (IOException ioe) {
            log.log(Level.SEVERE, "Caught IOException while extracting resource: " + ioe.getMessage(), ioe);
            return false;
        }
    }

    /**
     * Loads the given image resource as a BufferedImage and returns it at its natural size.
     * You can use setPrefix() to avoid having to specify a full path each time.
     * Otherwise, the given path is expected to be the full resource path within the jar.
     * Will return null if the resource cannot be found.
     */
    public static BufferedImage getImage(String imagePath) {
        if (imagePath == null) {
            log.warning("getImage(null) invoked.");
        }
        String path = prefix + (imagePath == null ? "" : imagePath);
        URL url = ResourceLoader.class.getClassLoader().getResource(path);
        return loadImage(url, 0);
    }

    /**
     * Loads the named image resource, scales it to the specified size, and returns it as an ImageIcon.
     * You can use setPrefix() to avoid having to specify a full path each time.
     * Otherwise, the given path is expected to be the full resource path within the jar.
     * Will return null if the resource cannot be found.
     */
    public static ImageIcon getIcon(String imagePath, int iconSize) {
        if (imagePath == null) {
            log.warning("getIcon(null) invoked.");
        }
        String path = prefix + (imagePath == null ? "" : imagePath);
        URL url = ResourceLoader.class.getClassLoader().getResource(path);
        return loadIcon(url, iconSize);
    }

    /**
     * Loads the given image resource as an ImageIcon and returns it at its natural size.
     * Will return null if the resource cannot be found, or if the given URL is null.
     */
    protected static ImageIcon loadIcon(URL url) {
        return loadIcon(url, 0);
    }

    /**
     * Loads the named image resource, scales it to the specified size, and returns it as an ImageIcon.
     * Will return null if the resource cannot be found, or if the given URL is null.
     */
    protected static ImageIcon loadIcon(URL url, int size) {
        if (url == null) {
            log.warning("loadIcon(null) invoked.");
        }
        BufferedImage image = (url == null) ? null : loadImage(url, size);
        return (image == null) ? null : new ImageIcon(image);
    }

    /**
     * Loads and optionally scales the given image resource. Pass 0 for size to load at natural size.
     * Will return null if the resource cannot be found, or if the given URL is null.
     */
    protected static BufferedImage loadImage(URL url, int size) {
        if (url == null) {
            log.severe("Unable to load resource from null URL.");
            return null;
        }
        try {
            BufferedImage rawImage = ImageUtil.loadImage(url);
            if (size <= 0) {
                return rawImage; // no scaling requested
            }
            return ImageUtil.generateThumbnailWithTransparency(rawImage, size, size);
        }
        catch (IOException ioe) {
            log.log(Level.SEVERE, "Caught IOException while loading resources: " + ioe.getMessage(), ioe);
            return null;
        }
    }
}
