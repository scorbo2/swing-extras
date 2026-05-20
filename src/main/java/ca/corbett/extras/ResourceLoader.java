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
 * Application extensions can use this class to load resources from the application's jar file, by
 * using the methods that do NOT accept a class loader argument. These methods use this class's class loader,
 * which will load resources from the application's jar file. For example:
 * </p>
 * <pre>
 * // Loads application resources:
 * BufferedImage logo = ResourceLoader.getImage("images/app-logo.png");
 * String helpText = ResourceLoader.getTextResource("docs/app-help.txt");
 * // And so on...
 * </pre>
 * <p>
 * If an extension wishes to use this class to load resources from ITS OWN jar file, then
 * it must supply its own class loader to the overloads that accept a class loader argument.
 * For example:
 * </p>
 * <pre>
 * // Loads extension resources:
 * BufferedImage logo = ResourceLoader.getImage("images/extension-logo.png",
 *                                              extensionClassLoader);
 * </pre>
 * <p>
 * Remember that application extensions only have access to their own class loader
 * either in the constructor of the main extension class, or in the loadJarResources() method!
 * After the extension is fully instantiated, its class loader is closed.
 * Refer to the swing-extras documentation for more details on application extensions and class loaders.
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
     * Uses this class's class loader to locate the resource.
     * You can use setPrefix() to avoid having to specify a full path each time.
     * Otherwise, the given path is expected to be the full resource path within the jar.
     * Will return null if the resource cannot be found.
     * If you request a resource that is not text, the result will be garbage.
     */
    public static List<String> getTextResourceAsLines(String resourcePath) {
        return getTextResourceAsLines(resourcePath, ResourceLoader.class.getClassLoader());
    }

    /**
     * Attempts to read the given text resource from the jar and return it as a List of lines.
     * Uses the specified class loader to locate the resource.
     * You can use setPrefix() to avoid having to specify a full path each time.
     * Otherwise, the given path is expected to be the full resource path within the jar.
     * Will return null if the resource cannot be found.
     * If you request a resource that is not text, the result will be garbage.
     *
     * @param resourcePath the path to the resource within the jar
     * @param classLoader  the class loader to use for locating the resource
     * @return a list of lines from the resource, or null if the resource cannot be found
     */
    public static List<String> getTextResourceAsLines(String resourcePath, ClassLoader classLoader) {
        if (resourcePath == null) {
            log.warning("getTextResourceAsLines(null) invoked.");
        }
        if (classLoader == null) {
            log.warning("getTextResourceAsLines(resourcePath, null) invoked. Falling back to default class loader.");
            classLoader = ResourceLoader.class.getClassLoader();
        }
        String path = prefix + (resourcePath == null ? "" : resourcePath);
        URL url = classLoader.getResource(path);
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
     * Uses this class's class loader to locate the resource.
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
        return getTextResource(resourcePath, ResourceLoader.class.getClassLoader());
    }

    /**
     * Attempts to read the given text resource from the jar and return it as a String.
     * Uses the specified class loader to locate the resource.
     * You can use setPrefix() to avoid having to specify a full path each time.
     * Otherwise, the given path is expected to be the full resource path within the jar.
     * Will return null if the resource cannot be found.
     * If you request a resource that is not text, the result will be garbage.
     * <p>
     * Note: This method joins lines using the system line separator, regardless
     * of what line endings are present in the original resource. If you'd rather
     * process the lines separately, use getTextResourceAsLines() instead.
     * </P>
     *
     * @param resourcePath the path to the resource within the jar
     * @param classLoader  the class loader to use for locating the resource
     * @return the text resource as a String, or null if the resource cannot be found
     */
    public static String getTextResource(String resourcePath, ClassLoader classLoader) {
        List<String> lines = getTextResourceAsLines(resourcePath, classLoader);
        if (lines == null) {
            return null;
        }
        return String.join(System.lineSeparator(), lines);
    }

    /**
     * Attempts to extract the given jar resource to the specified output file.
     * Uses this class's class loader to locate the resource.
     * You can use setPrefix() to avoid having to specify a full path each time.
     * Otherwise, the given path is expected to be the full resource path within the jar.
     * Returns true if successful, false otherwise.
     */
    public static boolean extractResourceToFile(String resourcePath, File outFile) {
        return extractResourceToFile(resourcePath, outFile, ResourceLoader.class.getClassLoader());
    }

    /**
     * Attempts to extract the given jar resource to the specified output file.
     * Uses the specified class loader to locate the resource.
     * You can use setPrefix() to avoid having to specify a full path each time.
     * Otherwise, the given path is expected to be the full resource path within the jar.
     * Returns true if successful, false otherwise.
     *
     * @param resourcePath the path to the resource within the jar
     * @param outFile      the file to write the resource to
     * @param classLoader  the class loader to use for locating the resource
     * @return true if extraction was successful, false otherwise
     */
    public static boolean extractResourceToFile(String resourcePath, File outFile, ClassLoader classLoader) {
        if (resourcePath == null) {
            log.warning("extractResourceToFile(null, outFile) invoked.");
        }
        if (classLoader == null) {
            log.warning("extractResourceToFile(resourcePath, outFile, null) invoked. " +
                                "Falling back to default class loader.");
            classLoader = ResourceLoader.class.getClassLoader();
        }
        String path = prefix + (resourcePath == null ? "" : resourcePath);
        URL url = classLoader.getResource(path);
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
     * Uses this class's class loader to locate the resource.
     * You can use setPrefix() to avoid having to specify a full path each time.
     * Otherwise, the given path is expected to be the full resource path within the jar.
     * Will return null if the resource cannot be found.
     */
    public static BufferedImage getImage(String imagePath) {
        return getImage(imagePath, ResourceLoader.class.getClassLoader());
    }

    /**
     * Loads the given image resource as a BufferedImage and returns it at its natural size.
     * Uses the specified class loader to locate the resource.
     * You can use setPrefix() to avoid having to specify a full path each time.
     * Otherwise, the given path is expected to be the full resource path within the jar.
     * Will return null if the resource cannot be found.
     *
     * @param imagePath   the path to the image resource within the jar
     * @param classLoader the class loader to use for locating the resource
     * @return the image as a BufferedImage, or null if the resource cannot be found
     */
    public static BufferedImage getImage(String imagePath, ClassLoader classLoader) {
        if (imagePath == null) {
            log.warning("getImage(null) invoked.");
        }
        if (classLoader == null) {
            log.warning("getImage(imagePath, null) invoked. Falling back to default class loader.");
            classLoader = ResourceLoader.class.getClassLoader();
        }
        String path = prefix + (imagePath == null ? "" : imagePath);
        URL url = classLoader.getResource(path);
        return loadImage(url, 0);
    }

    /**
     * Loads the named image resource, scales it to the specified size, and returns it as an ImageIcon.
     * Uses this class's class loader to locate the resource.
     * You can use setPrefix() to avoid having to specify a full path each time.
     * Otherwise, the given path is expected to be the full resource path within the jar.
     * Will return null if the resource cannot be found.
     */
    public static ImageIcon getIcon(String imagePath, int iconSize) {
        return getIcon(imagePath, iconSize, ResourceLoader.class.getClassLoader());
    }

    /**
     * Loads the named image resource, scales it to the specified size, and returns it as an ImageIcon.
     * Uses the specified class loader to locate the resource.
     * You can use setPrefix() to avoid having to specify a full path each time.
     * Otherwise, the given path is expected to be the full resource path within the jar.
     * Will return null if the resource cannot be found.
     *
     * @param imagePath   the path to the image resource within the jar
     * @param iconSize    the desired size of the icon
     * @param classLoader the class loader to use for locating the resource
     * @return the image as an ImageIcon, or null if the resource cannot be found
     */
    public static ImageIcon getIcon(String imagePath, int iconSize, ClassLoader classLoader) {
        if (imagePath == null) {
            log.warning("getIcon(null) invoked.");
        }
        if (classLoader == null) {
            log.warning("getIcon(imagePath, iconSize, null) invoked. Falling back to default class loader.");
            classLoader = ResourceLoader.class.getClassLoader();
        }
        String path = prefix + (imagePath == null ? "" : imagePath);
        URL url = classLoader.getResource(path);
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
