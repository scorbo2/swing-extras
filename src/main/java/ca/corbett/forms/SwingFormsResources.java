package ca.corbett.forms;

import ca.corbett.extras.ResourceLoader;
import ca.corbett.extras.image.ImageUtil;

import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Centralizes and manages access to the various resource images that are used in swing-forms.
 * All icons are stored internally at 48x48, but can be requested at any size.
 * This class maintains a cache of icons at their native size to avoid unnecessary loads.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.4
 */
public final class SwingFormsResources extends ResourceLoader {

    private static final Logger log = Logger.getLogger(SwingFormsResources.class.getName());

    /**
     * The singleton instance holder.
     */
    static class Instance {
        static final SwingFormsResources INSTANCE = new SwingFormsResources();
    }

    /**
     * This flag indicates that no resizing should be performed on an icon.
     * Used with the static loadIcon() method from ResourceLoader.
     */
    static final int NO_RESIZE = 0;

    /**
     * The native size (width and height) of all swing-forms icons.
     */
    public static final int NATIVE_SIZE = 48;

    /**
     * This is just here as a safeguard, and will likely never be used outside of unit tests.
     */
    static final int MAX_ICON_SIZE = 256;

    /**
     * All swing-forms icon resources share this resource prefix.
     */
    private static final String PREFIX = "ca/corbett/swing-forms/images/";

    // Icons intended for use with FormFields:
    static final String VALID = "formfield-valid.png";
    static final String INVALID = "formfield-invalid.png";
    static final String HELP = "formfield-help.png";
    static final String BLANK = "formfield-blank.png";
    static final String LOCKED = "formfield-locked.png"; // currently unused
    static final String UNLOCKED = "formfield-unlocked.png"; // currently unused
    static final String COPY = "formfield-copy.png";
    static final String HIDDEN = "formfield-hidden.png";
    static final String REVEALED = "formfield-revealed.png";

    // Icons intended for general use:
    static final String PLUS = "icon-plus.png";
    static final String MINUS = "icon-minus.png";
    static final String ADD = "icon-add.png";
    static final String REMOVE = "icon-remove.png";
    static final String REMOVE_ALL = "icon-remove-all.png";
    static final String EDIT = "icon-edit.png";
    static final String MOVE_LEFT = "icon-move-left.png";
    static final String MOVE_RIGHT = "icon-move-right.png";
    static final String MOVE_UP = "icon-move-up.png";
    static final String MOVE_DOWN = "icon-move-down.png";
    static final String MOVE_ALL_LEFT = "icon-move-all-left.png";
    static final String MOVE_ALL_RIGHT = "icon-move-all-right.png";

    // Our cache will be lazy-loaded as icons are requested:
    // (If callers go through the parent class methods, they bypass our cache)
    // (not much we can do about that, as we can't override static methods)
    // (but these icons are generally tiny, so the overhead is minimal)
    final Map<String, ImageIcon> iconCache = new ConcurrentHashMap<>();

    private SwingFormsResources() {
    }

    /**
     * Returns an ImageIcon to represent a "valid" FormField - that is, one that
     * has no validation errors.
     */
    public static ImageIcon getValidIcon(int size) {
        return internalLoad(VALID, size);
    }

    /**
     * Returns an ImageIcon to represent an "invalid" FormField - that is, one that
     * has at least one validation error.
     */
    public static ImageIcon getInvalidIcon(int size) {
        return internalLoad(INVALID, size);
    }

    /**
     * Returns an ImageIcon to represent a FormField that has some help or informational
     * text associated with it.
     */
    public static ImageIcon getHelpIcon(int size) {
        return internalLoad(HELP, size);
    }

    /**
     * Returns a transparent ImageIcon, useful for reserving space where an icon should go.
     */
    public static ImageIcon getBlankIcon(int size) {
        return internalLoad(BLANK, size);
    }

    /**
     * Returns an ImageIcon to represent a lock.
     */
    public static ImageIcon getLockedIcon(int size) {
        return internalLoad(LOCKED, size);
    }

    /**
     * Returns an ImageIcon to represent an open lock.
     */
    public static ImageIcon getUnlockedIcon(int size) {
        return internalLoad(UNLOCKED, size);
    }

    /**
     * Returns an ImageIcon to represent a copy operation.
     */
    public static ImageIcon getCopyIcon(int size) {
        return internalLoad(COPY, size);
    }

    /**
     * Returns an ImageIcon to represent something that is hidden.
     */
    public static ImageIcon getHiddenIcon(int size) {
        return internalLoad(HIDDEN, size);
    }

    /**
     * Returns an ImageIcon to represent something that is revealed.
     */
    public static ImageIcon getRevealedIcon(int size) {
        return internalLoad(REVEALED, size);
    }

    /**
     * Returns an ImageIcon to represent a plus sign (expand, zoom in, etc.).
     */
    public static ImageIcon getPlusIcon(int size) {
        return internalLoad(PLUS, size);
    }

    /**
     * Returns an ImageIcon to represent a minus sign (collapse, zoom out, etc.).
     */
    public static ImageIcon getMinusIcon(int size) {
        return internalLoad(MINUS, size);
    }

    /**
     * Returns an ImageIcon to represent adding an item.
     */
    public static ImageIcon getAddIcon(int size) {
        return internalLoad(ADD, size);
    }

    /**
     * Returns an ImageIcon to represent removing an item.
     */
    public static ImageIcon getRemoveIcon(int size) {
        return internalLoad(REMOVE, size);
    }

    /**
     * Returns an ImageIcon to represent removing all items.
     */
    public static ImageIcon getRemoveAllIcon(int size) {
        return internalLoad(REMOVE_ALL, size);
    }

    /**
     * Returns an ImageIcon to represent editing an item.
     */
    public static ImageIcon getEditIcon(int size) {
        return internalLoad(EDIT, size);
    }

    /**
     * Returns an ImageIcon to represent moving an item to the left.
     */
    public static ImageIcon getMoveLeftIcon(int size) {
        return internalLoad(MOVE_LEFT, size);
    }

    /**
     * Returns an ImageIcon to represent moving an item to the right.
     */
    public static ImageIcon getMoveRightIcon(int size) {
        return internalLoad(MOVE_RIGHT, size);
    }

    /**
     * Returns an ImageIcon to represent moving an item up.
     */
    public static ImageIcon getMoveUpIcon(int size) {
        return internalLoad(MOVE_UP, size);
    }

    /**
     * Returns an ImageIcon to represent moving an item down.
     */
    public static ImageIcon getMoveDownIcon(int size) {
        return internalLoad(MOVE_DOWN, size);
    }

    /**
     * Returns an ImageIcon to represent moving all items to the left.
     */
    public static ImageIcon getMoveAllLeftIcon(int size) {
        return internalLoad(MOVE_ALL_LEFT, size);
    }

    /**
     * Returns an ImageIcon to represent moving all items to the right.
     */
    public static ImageIcon getMoveAllRightIcon(int size) {
        return internalLoad(MOVE_ALL_RIGHT, size);
    }

    /**
     * All icons are all stored at 48x48 internally, but can be requested at any size.
     * This method will load one and cache it at native size, then return a resized
     * version as requested.
     *
     * @param resourceName Any of the icon name constants.
     * @param size The requested size. Use NO_RESIZE or a negative value to get the native size of 48x48.
     * @return An ImageIcon instance.
     */
    static ImageIcon internalLoad(String resourceName, int size) {
        if (resourceName == null) {
            log.severe("SwingFormsResources: internalLoad() called with null resourceName");
            return null;
        }

        // Try to grab it from cache:
        ImageIcon icon = Instance.INSTANCE.iconCache.get(resourceName);

        // If not in cache, load it now once and add to cache:
        if (icon == null) {
            icon = getIcon(PREFIX + resourceName, NO_RESIZE); // Load at native size (48x48)
            if (icon != null) {
                Instance.INSTANCE.iconCache.put(resourceName, icon);
            }
        }

        // If it wasn't in cache, and it couldn't be loaded, we have a problem:
        // (this *should* never happen, but perhaps our jar was packaged incorrectly)
        if (icon == null) {
            log.severe("SwingFormsResources: Could not load icon resource: " + resourceName);
            return null;
        }

        // Resize if needed:
        if (size > 0 && size != NATIVE_SIZE) {
            // Put some kind of cap on it to avoid stupid issues:
            if (size > MAX_ICON_SIZE) {
                // *should* never happen, but let's be safe:
                log.warning("SwingFormsResources: Requested icon size too large (" + size + "), capping at 256");
                size = MAX_ICON_SIZE;
            }

            // We create all our icons from BufferedImages, so it should be safe to cast,
            // but let's be cautious:
            if (!(icon.getImage() instanceof BufferedImage)) {
                log.severe("SwingFormsResources: Icon image is not a BufferedImage, cannot scale: " + resourceName);
                return icon; // return unscaled version
            }

            // Now scale it:
            BufferedImage unscaled = (BufferedImage)icon.getImage();
            BufferedImage scaled = ImageUtil.generateThumbnailWithTransparency(unscaled, size, size);
            // Should we cache the scaled icon also, so we don't have to rescale each time?
            // Eh, we don't know how often this will be called with different sizes.
            icon = new ImageIcon(scaled);
        }

        return icon;
    }

    /**
     * Only for testing - clears the internal icon cache.
     */
    static void clearCache() {
        Instance.INSTANCE.iconCache.clear();
    }
}
