package ca.corbett.forms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.ImageIcon;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class SwingFormsResourcesTest {

    private final List<String> validResourceNames = List.of(
            SwingFormsResources.VALID,
            SwingFormsResources.INVALID,
            SwingFormsResources.HELP,
            SwingFormsResources.BLANK,
            SwingFormsResources.LOCKED,
            SwingFormsResources.UNLOCKED,
            SwingFormsResources.COPY,
            SwingFormsResources.HIDDEN,
            SwingFormsResources.REVEALED,
            SwingFormsResources.PLUS,
            SwingFormsResources.MINUS,
            SwingFormsResources.ADD,
            SwingFormsResources.REMOVE,
            SwingFormsResources.REMOVE_ALL,
            SwingFormsResources.EDIT,
            SwingFormsResources.MOVE_LEFT,
            SwingFormsResources.MOVE_RIGHT,
            SwingFormsResources.MOVE_UP,
            SwingFormsResources.MOVE_DOWN,
            SwingFormsResources.MOVE_ALL_LEFT,
            SwingFormsResources.MOVE_ALL_RIGHT
    );

    @BeforeEach
    public void setup() {
        // Clear the icon cache before each test:
        SwingFormsResources.clearCache();
    }

    @Test
    public void internalLoad_withInvalidResourceName_shouldReturnNull() {
        // Given an invalid resource name, when we try to load it, then it should return null:
        assertNull(SwingFormsResources.internalLoad("invalid/resource/name.png", 0));
        assertNull(SwingFormsResources.internalLoad("", 0));
        assertNull(SwingFormsResources.internalLoad(null, 0));

        // Our cache should be empty:
        assertEquals(0, SwingFormsResources.Instance.INSTANCE.iconCache.size());
    }

    @Test
    public void internalLoad_withValidResourceNameAndNoScaling_shouldReturnAtNaturalSize() {
        // GIVEN valid resource names:
        for (String resourceName : validResourceNames) {
            // WHEN we load the resource with no scaling:
            ImageIcon icon = SwingFormsResources.internalLoad(resourceName, SwingFormsResources.NO_RESIZE);

            // THEN the image should not be null:
            assertNotNull(icon);

            // AND the image should be at its original size (48x48):
            assertEquals(SwingFormsResources.NATIVE_SIZE, icon.getIconWidth());
            assertEquals(SwingFormsResources.NATIVE_SIZE, icon.getIconHeight());
        }

        // Our cache should have been populated with each entry:
        assertEquals(validResourceNames.size(), SwingFormsResources.Instance.INSTANCE.iconCache.size());
    }

    @Test
    public void internalLoad_withValidResourceNameAndWithScaling_shouldReturnAtRequestedSize() {
        // GIVEN valid resource names:
        final int requestedSize = 24;
        for (String resourceName : validResourceNames) {
            // WHEN we load the resource with requested scaling:
            ImageIcon icon = SwingFormsResources.internalLoad(resourceName, requestedSize);

            // THEN the image should not be null:
            assertNotNull(icon);

            // AND the image should be at the requested size:
            assertEquals(requestedSize, icon.getIconWidth());
            assertEquals(requestedSize, icon.getIconHeight());
        }

        // Our cache should have been populated with each entry:
        assertEquals(validResourceNames.size(), SwingFormsResources.Instance.INSTANCE.iconCache.size());
    }

    @Test
    public void internalLoad_withNegativeScaleSize_shouldReturnAtNaturalSize() {
        // GIVEN any valid resource name:
        final String resourceName = SwingFormsResources.VALID;

        // WHEN we ask for it at a negative size:
        ImageIcon icon = SwingFormsResources.internalLoad(resourceName, -10);

        // THEN we should get it at its natural size instead:
        assertNotNull(icon);
        assertEquals(SwingFormsResources.NATIVE_SIZE, icon.getIconWidth());
        assertEquals(SwingFormsResources.NATIVE_SIZE, icon.getIconHeight());
    }

    @Test
    public void internalLoad_withUnreasonableScaleSize_shouldCap() {
        // GIVEN any valid resource name:
        final String resourceName = SwingFormsResources.VALID;

        // WHEN we ask for it at some unreasonable size:
        ImageIcon icon = SwingFormsResources.internalLoad(resourceName, 1000);

        // THEN we should get it at the maximum allowed size:
        assertNotNull(icon);
        assertEquals(SwingFormsResources.MAX_ICON_SIZE, icon.getIconWidth());
        assertEquals(SwingFormsResources.MAX_ICON_SIZE, icon.getIconHeight());
    }
}
