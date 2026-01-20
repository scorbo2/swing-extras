package ca.corbett.extras.image;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the ImagePanel class.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class ImagePanelTest {

    private ImagePanel imagePanel;
    private BufferedImage testImage;

    @BeforeEach
    void setUp() {
        // Create a simple test image
        testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        imagePanel = new ImagePanel(testImage);
    }

    @Test
    public void dispose_clearsExtraAttributes() {
        // GIVEN an ImagePanel with some extra attributes:
        imagePanel.setExtraAttribute("key1", "value1");
        imagePanel.setExtraAttribute("key2", "value2");
        assertNotNull(imagePanel.getExtraAttribute("key1"));
        assertNotNull(imagePanel.getExtraAttribute("key2"));

        // WHEN we call dispose:
        imagePanel.dispose();

        // THEN the extra attributes should be cleared:
        assertNull(imagePanel.getExtraAttribute("key1"));
        assertNull(imagePanel.getExtraAttribute("key2"));
    }

    @Test
    public void dispose_clearsImage() {
        // GIVEN an ImagePanel with an image:
        assertNotNull(imagePanel.getImage());

        // WHEN we call dispose:
        imagePanel.dispose();

        // THEN the image should be null:
        assertNull(imagePanel.getImage());
    }

    @Test
    public void dispose_clearsImageIcon() {
        // GIVEN an ImagePanel with an ImageIcon:
        ImageIcon icon = new ImageIcon(testImage);
        imagePanel.setImageIcon(icon);
        assertNotNull(imagePanel.getImageIcon());

        // WHEN we call dispose:
        imagePanel.dispose();

        // THEN the imageIcon should be null:
        assertNull(imagePanel.getImageIcon());
    }

    @Test
    public void dispose_clearsPopupMenu() {
        // GIVEN an ImagePanel with a popup menu:
        JPopupMenu popupMenu = new JPopupMenu();
        imagePanel.setPopupMenu(popupMenu);

        // WHEN we call dispose:
        imagePanel.dispose();

        // THEN the popup menu should be cleared:
        // (We verify by checking that component popup menu is null)
        assertNull(imagePanel.getComponentPopupMenu());
    }

    @Test
    public void dispose_removesMouseListeners() {
        // GIVEN an ImagePanel with mouse listeners:
        int initialMouseListenerCount = imagePanel.getMouseListeners().length;
        int initialMouseWheelListenerCount = imagePanel.getMouseWheelListeners().length;
        int initialMouseMotionListenerCount = imagePanel.getMouseMotionListeners().length;

        // Verify that listeners were added during construction
        assertTrue(initialMouseListenerCount > 0, "Expected mouse listeners to be present");
        assertTrue(initialMouseWheelListenerCount > 0, "Expected mouse wheel listeners to be present");
        assertTrue(initialMouseMotionListenerCount > 0, "Expected mouse motion listeners to be present");

        // WHEN we call dispose:
        imagePanel.dispose();

        // THEN all mouse listeners should be removed:
        assertEquals(0, imagePanel.getMouseListeners().length);
        assertEquals(0, imagePanel.getMouseWheelListeners().length);
        assertEquals(0, imagePanel.getMouseMotionListeners().length);
    }

    @Test
    public void dispose_removesComponentListeners() {
        // GIVEN an ImagePanel with component listeners:
        int initialComponentListenerCount = imagePanel.getComponentListeners().length;

        // Verify that listeners were added during construction
        assertTrue(initialComponentListenerCount > 0, "Expected component listeners to be present");

        // WHEN we call dispose:
        imagePanel.dispose();

        // THEN all component listeners should be removed:
        assertEquals(0, imagePanel.getComponentListeners().length);
    }

    @Test
    public void dispose_isIdempotent() {
        // GIVEN an ImagePanel with resources:
        imagePanel.setExtraAttribute("key", "value");
        assertNotNull(imagePanel.getExtraAttribute("key"));

        // WHEN we call dispose multiple times:
        imagePanel.dispose();
        imagePanel.dispose();
        imagePanel.dispose();

        // THEN there should be no errors and resources should remain cleared:
        assertNull(imagePanel.getExtraAttribute("key"));
        assertNull(imagePanel.getImage());
        assertEquals(0, imagePanel.getMouseListeners().length);
    }

    @Test
    public void dispose_withNoImage() {
        // GIVEN an ImagePanel with no image:
        imagePanel = new ImagePanel();
        assertNull(imagePanel.getImage());

        // WHEN we call dispose:
        imagePanel.dispose();

        // THEN there should be no errors:
        assertNull(imagePanel.getImage());
        assertEquals(0, imagePanel.getMouseListeners().length);
    }
}
