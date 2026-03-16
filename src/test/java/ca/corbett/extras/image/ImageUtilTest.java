package ca.corbett.extras.image;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.MediaTracker;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

public class ImageUtilTest {

    private ImageIcon mockedImageIcon;

    @BeforeEach
    void setUp() {
        mockedImageIcon = Mockito.mock(ImageIcon.class);
    }

    @Test
    public void validateImageIcon_withNullImage_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> ImageUtil.validateImageIcon(null));
    }

    @Test
    public void validateImageIcon_withZeroWidthImage_shouldThrow() {
        // GIVEN an image with zero width:
        when(mockedImageIcon.getIconWidth()).thenReturn(0); // invalid!
        when(mockedImageIcon.getIconHeight()).thenReturn(100); // valid
        when(mockedImageIcon.getImageLoadStatus()).thenReturn(MediaTracker.COMPLETE); // valid

        // WHEN we try to validate it:
        // THEN we should get an IOException:
        assertThrows(IOException.class, () -> ImageUtil.validateImageIcon(mockedImageIcon));
    }

    @Test
    public void validateImageIcon_withZeroHeightImage_shouldThrow() {
        // GIVEN an image with zero height:
        when(mockedImageIcon.getIconWidth()).thenReturn(100); // valid
        when(mockedImageIcon.getIconHeight()).thenReturn(0); // invalid!
        when(mockedImageIcon.getImageLoadStatus()).thenReturn(MediaTracker.COMPLETE); // valid

        // WHEN we try to validate it:
        // THEN we should get an IOException:
        assertThrows(IOException.class, () -> ImageUtil.validateImageIcon(mockedImageIcon));
    }

    @Test
    public void validateImageIcon_withABORTEDImage_shouldPass() {
        // GIVEN a mocked ImageIcon with a getImageLoadStatus of ABORTED:
        when(mockedImageIcon.getIconWidth()).thenReturn(100); // valid
        when(mockedImageIcon.getIconHeight()).thenReturn(100); // valid
        when(mockedImageIcon.getImageLoadStatus()).thenReturn(MediaTracker.ABORTED); // believe it or not, valid

        try {
            // WHEN we try to validate it:
            // THEN it should pass without exception:
            ImageUtil.validateImageIcon(mockedImageIcon);
        }
        catch (IOException ioe) {
            fail("Did not expect an exception but got: " + ioe.getMessage());
        }
    }

    @Test
    public void validateImageIcon_withCOMPLETEDImage_shouldPass() {
        // GIVEN a mocked ImageIcon with a getImageLoadStatus of COMPLETE:
        when(mockedImageIcon.getIconWidth()).thenReturn(100); // valid
        when(mockedImageIcon.getIconHeight()).thenReturn(100); // valid
        when(mockedImageIcon.getImageLoadStatus()).thenReturn(MediaTracker.COMPLETE); // valid

        try {
            // WHEN we try to validate it:
            // THEN it should pass without exception:
            ImageUtil.validateImageIcon(mockedImageIcon);
        }
        catch (IOException ioe) {
            fail("Did not expect an exception but got: " + ioe.getMessage());
        }
    }

    @Test
    public void validateImageIcon_withERROREDImage_shouldThrow() {
        // GIVEN a mocked ImageIcon with a getImageLoadStatus of ERRORED:
        when(mockedImageIcon.getIconWidth()).thenReturn(100); // valid
        when(mockedImageIcon.getIconHeight()).thenReturn(100); // valid
        when(mockedImageIcon.getImageLoadStatus()).thenReturn(MediaTracker.ERRORED); // invalid!

        // WHEN we try to validate it:
        // THEN it should pass without exception:
        assertThrows(IOException.class, () -> ImageUtil.validateImageIcon(mockedImageIcon));
    }

    @Test
    public void savePngImage_withValidImage_shouldSave() {
        // GIVEN a valid image:
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 100, 100);
        g.dispose();

        try {
            // WHEN we try to save it as PNG:
            File tempFile = File.createTempFile("testImage", ".png");
            tempFile.deleteOnExit();
            ImageUtil.savePngImage(image, tempFile);

            // THEN it should save without exception and the file should exist:
            if (!tempFile.exists()) {
                fail("Expected file to be created but it doesn't exist!");
            }

            // AND We should be able to load it back without exception:
            BufferedImage loadedImage = ImageUtil.loadImage(tempFile);

            // Clean up
            image.flush();
            loadedImage.flush();
        }
        catch (IOException ioe) {
            fail("Expected to save and load the image without exception but got: " + ioe.getMessage());
        }
    }

    @Test
    public void loadImage_withNullStream_shouldThrow() {
        // GIVEN a null stream:
        // WHEN we try to load an image from it:
        // THEN we should get an IllegalArgumentException:
        assertThrows(IllegalArgumentException.class, () -> ImageUtil.loadImage((InputStream)null));
    }

    @Test
    public void loadImage_withNullFile_shouldThrow() {
        // GIVEN a null file:
        // WHEN we try to load an image from it:
        // THEN we should get an IllegalArgumentException:
        assertThrows(IllegalArgumentException.class, () -> ImageUtil.loadImage((File)null));
    }

    @Test
    public void loadImage_withNullURL_shouldThrow() {
        // GIVEN a null URL:
        // WHEN we try to load an image from it:
        // THEN we should get an IllegalArgumentException:
        assertThrows(IllegalArgumentException.class, () -> ImageUtil.loadImage((URL)null));
    }

    @Test
    public void generateThumbnailWithTransparency_withNullFile_shouldThrow() {
        // GIVEN a null file:
        // WHEN we try to generate a thumbnail from it:
        // THEN we should get an IllegalArgumentException:
        assertThrows(IllegalArgumentException.class, () -> ImageUtil.generateThumbnailWithTransparency(
                (File)null, 50, 50));
    }

    @Test
    public void savePngImage_withNullImage_shouldThrow() {
        // GIVEN a null image:
        // WHEN we try to save it as PNG:
        // THEN we should get an IllegalArgumentException:
        assertThrows(IllegalArgumentException.class, () -> ImageUtil.savePngImage(
                null, new File("dummy.png")));
    }

    @Test
    public void savePngImage_withNullFile_shouldThrow() {
        // GIVEN a null file:
        // WHEN we try to save an image as PNG to it:
        // THEN we should get an IOException:
        BufferedImage dummyImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        assertThrows(IllegalArgumentException.class, () -> ImageUtil.savePngImage(dummyImage, null));
    }

    @Test
    public void loadImageIcon_withNullFile_shouldThrow() {
        // GIVEN a null file:
        // WHEN we try to load an ImageIcon from it:
        // THEN we should get an IllegalArgumentException:
        assertThrows(IllegalArgumentException.class, () -> ImageUtil.loadImageIcon((File) null));
    }

    @Test
    public void loadImageIcon_withNullURL_shouldThrow() {
        // GIVEN a null URL:
        // WHEN we try to load an ImageIcon from it:
        // THEN we should get an IllegalArgumentException:
        assertThrows(IllegalArgumentException.class, () -> ImageUtil.loadImageIcon((URL) null));
    }

    @Test
    public void generateThumbnailWithTransparency_withNullImage_shouldThrow() {
        // GIVEN a null BufferedImage:
        // WHEN we try to generate a thumbnail from it:
        // THEN we should get an IllegalArgumentException:
        assertThrows(IllegalArgumentException.class,
                () -> ImageUtil.generateThumbnailWithTransparency((BufferedImage) null, 50, 50));
    }

    @Test
    public void generateThumbnailWithTransparency_withNonPositiveWidth_shouldThrow() {
        // GIVEN a valid image but a non-positive width:
        BufferedImage dummyImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);

        // WHEN we try to generate a thumbnail with width=0:
        // THEN we should get an IllegalArgumentException:
        assertThrows(IllegalArgumentException.class,
                () -> ImageUtil.generateThumbnailWithTransparency(dummyImage, 0, 50));
    }

    @Test
    public void generateThumbnailWithTransparency_withNonPositiveHeight_shouldThrow() {
        // GIVEN a valid image but a non-positive height:
        BufferedImage dummyImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);

        // WHEN we try to generate a thumbnail with height=0:
        // THEN we should get an IllegalArgumentException:
        assertThrows(IllegalArgumentException.class,
                () -> ImageUtil.generateThumbnailWithTransparency(dummyImage, 50, 0));
    }

    @Test
    public void getImageDimensions_withNullFile_shouldThrow() {
        // GIVEN a null file:
        // WHEN we try to get image dimensions from it:
        // THEN we should get an IllegalArgumentException:
        assertThrows(IllegalArgumentException.class, () -> ImageUtil.getImageDimensions(null));
    }

    @Test
    public void getAspectRatioDescription_withNullDimension_shouldThrow() {
        // GIVEN a null Dimension:
        // WHEN we try to get an aspect ratio description from it:
        // THEN we should get an IllegalArgumentException:
        assertThrows(IllegalArgumentException.class, () -> ImageUtil.getAspectRatioDescription(null));
    }

    @Test
    public void scaleImageToFitSquareBounds_withNullImage_shouldThrow() {
        // GIVEN a null image:
        // WHEN we try to scale it:
        // THEN we should get an IllegalArgumentException:
        assertThrows(IllegalArgumentException.class,
                () -> ImageUtil.scaleImageToFitSquareBounds(null, 100));
    }

    @Test
    public void scaleImageToFitSquareBounds_withNonPositiveMaxDimension_shouldThrow() {
        // GIVEN a valid image but a non-positive maxDimension:
        BufferedImage dummyImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);

        // WHEN we try to scale it with maxDimension=0:
        // THEN we should get an IllegalArgumentException:
        assertThrows(IllegalArgumentException.class,
                () -> ImageUtil.scaleImageToFitSquareBounds(dummyImage, 0));
    }
}
