package ca.corbett.extras.image;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.ImageIcon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the ImagePanel class.
 */
class ImagePanelTest {

    private ImagePanel panel;

    @BeforeEach
    void setUp() {
        panel = new ImagePanel();
    }

    /**
     * Verifies that a MouseListener added to an ImagePanel receives only ONE
     * notification per mouse click, not two.
     * <p>
     * This test addresses issue #307 where animated GIFs caused double mouse events
     * because MouseListeners were being added to both the panel and the internal
     * image label.
     */
    @Test
    void testMouseListenerReceivesSingleNotificationPerClick() {
        // GIVEN an ImagePanel with an ImageIcon (simulating an animated GIF scenario)
        BufferedImage testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageIcon icon = new ImageIcon(testImage);
        panel.setImageIcon(icon);

        // AND a MouseListener that counts click events
        AtomicInteger clickCount = new AtomicInteger(0);
        MouseListener countingListener = new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                clickCount.incrementAndGet();
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        };
        panel.addMouseListener(countingListener);

        // WHEN a mouse click event is dispatched to the panel
        MouseEvent clickEvent = new MouseEvent(panel, MouseEvent.MOUSE_CLICKED,
                System.currentTimeMillis(), 0, 50, 50, 1, false);
        panel.dispatchEvent(clickEvent);

        // THEN the listener should receive exactly ONE notification
        assertEquals(1, clickCount.get(),
                "MouseListener should receive exactly one notification per click, not two");
    }

    /**
     * Verifies that the dispose method can be called without errors.
     */
    @Test
    void testDisposeDoesNotThrow() {
        BufferedImage testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageIcon icon = new ImageIcon(testImage);
        panel.setImageIcon(icon);

        // Should not throw any exception
        panel.dispose();
    }

    /**
     * Verifies that mouse listeners can be added and removed properly.
     */
    @Test
    void testAddAndRemoveMouseListener() {
        AtomicInteger clickCount = new AtomicInteger(0);
        MouseListener listener = new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                clickCount.incrementAndGet();
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        };

        panel.addMouseListener(listener);

        // Fire an event
        MouseEvent clickEvent = new MouseEvent(panel, MouseEvent.MOUSE_CLICKED,
                System.currentTimeMillis(), 0, 50, 50, 1, false);
        panel.dispatchEvent(clickEvent);
        assertEquals(1, clickCount.get());

        // Remove the listener
        panel.removeMouseListener(listener);

        // Fire another event - count should not increase
        clickEvent = new MouseEvent(panel, MouseEvent.MOUSE_CLICKED,
                System.currentTimeMillis(), 0, 50, 50, 1, false);
        panel.dispatchEvent(clickEvent);
        assertEquals(1, clickCount.get(), "Removed listener should not receive events");
    }
}
