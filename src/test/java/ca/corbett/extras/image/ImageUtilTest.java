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
        try {
            // GIVEN a null image:
            // WHEN we try to validate it:
            // THEN we should get an IOException:
            ImageUtil.validateImageIcon(null);
            fail("Expected exception but didn't get one!");
        }
        catch (IOException ignored) {
        }
    }

    @Test
    public void validateImageIcon_withZeroWidthImage_shouldThrow() {
        // GIVEN an image with zero width:
        when(mockedImageIcon.getIconWidth()).thenReturn(0); // invalid!
        when(mockedImageIcon.getIconHeight()).thenReturn(100); // valid
        when(mockedImageIcon.getImageLoadStatus()).thenReturn(MediaTracker.COMPLETE); // valid

        // WHEN we try to validate it:
        try {
            // THEN we should get an IOException:
            ImageUtil.validateImageIcon(mockedImageIcon);
            fail("Expected exception but didn't get one!");
        }
        catch (IOException ignored) {
        }
    }

    @Test
    public void validateImageIcon_withZeroHeightImage_shouldThrow() {
        // GIVEN an image with zero height:
        when(mockedImageIcon.getIconWidth()).thenReturn(100); // valid
        when(mockedImageIcon.getIconHeight()).thenReturn(0); // invalid!
        when(mockedImageIcon.getImageLoadStatus()).thenReturn(MediaTracker.COMPLETE); // valid

        // WHEN we try to validate it:
        try {
            // THEN we should get an IOException:
            ImageUtil.validateImageIcon(mockedImageIcon);
            fail("Expected exception but didn't get one!");
        }
        catch (IOException ignored) {
        }
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
        catch (IOException ignored) {
            fail("Did not expect an exception but got one!");
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
        catch (IOException ignored) {
            fail("Did not expect an exception but got one!");
        }
    }

    @Test
    public void validateImageIcon_withERROREDImage_shouldThrow() {
        // GIVEN a mocked ImageIcon with a getImageLoadStatus of ERRORED:
        when(mockedImageIcon.getIconWidth()).thenReturn(100); // valid
        when(mockedImageIcon.getIconHeight()).thenReturn(100); // valid
        when(mockedImageIcon.getImageLoadStatus()).thenReturn(MediaTracker.ERRORED); // invalid!

        // WHEN we try to validate it:
        try {
            // THEN we should get an IOException:
            ImageUtil.validateImageIcon(mockedImageIcon);
            fail("Expected exception but didn't get one!");
        }
        catch (IOException ignored) {
        }
    }

    @Test
    public void savePngImage_withValidImage_shouldSave() throws Exception {
        // GIVEN a valid image:
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 100, 100);
        g.dispose();

        // WHEN we try to save it as PNG:
        File tempFile = File.createTempFile("testImage", ".png");
        tempFile.deleteOnExit();
        ImageUtil.savePngImage(image, tempFile);

        // THEN it should save without exception and the file should exist:
        if (!tempFile.exists()) {
            fail("Expected file to be created but it doesn't exist!");
        }

        // AND We should be able to load it back:
        BufferedImage loadedImage = ImageUtil.loadImage(tempFile);
        if (loadedImage == null) {
            fail("Expected to load the saved image but got null!");
        }

        // Clean up
        image.flush();
        loadedImage.flush();
    }
}