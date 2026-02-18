package ca.corbett.extras;

import javax.swing.JScrollPane;
import java.awt.Component;

/**
 * Contains static convenience methods related to scroll panes.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8 (refactored from older code)
 */
public class ScrollUtil {

    private ScrollUtil() {
    }

    /**
     * A static convenience method to generate a JScrollPane with more sensible default values.
     * Seriously, why is the default behavior to scroll 1 pixel at a time when you mouse wheel?
     *
     * @param component Any Component that needs scrolling.
     * @return A JScrollPane that won't take a million years to scroll through.
     */
    public static JScrollPane buildScrollPane(Component component) {
        return buildScrollPane(component, 24);
    }

    /**
     * A static convenience method to generate a JScrollPane with more sensible default values.
     * Seriously, why is the default behavior to scroll 1 pixel at a time when you mouse wheel?
     *
     * @param component     Any Component that needs scrolling.
     * @param unitIncrement How much to scroll by (I believe this is a pixel value).
     * @return A JScrollPane that won't take a million years to scroll through.
     */
    public static JScrollPane buildScrollPane(Component component, int unitIncrement) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.getVerticalScrollBar().setUnitIncrement(unitIncrement);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(unitIncrement);
        return scrollPane;
    }
}
