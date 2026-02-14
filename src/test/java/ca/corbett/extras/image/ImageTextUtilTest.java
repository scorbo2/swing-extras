package ca.corbett.extras.image;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ImageTextUtil class.
 */
public class ImageTextUtilTest {

    /**
     * Test that adjustLineWrapLength correctly calculates adjusted length for a wide image.
     * This test validates the fix for issue #308.
     */
    @Test
    public void testAdjustLineWrapLength_wideImage() {
        // GIVEN: A wide image (aspect ratio 2.0 = 400/200)
        int imageWidth = 400;
        int imageHeight = 200;
        int initialLength = 30;
        
        // WHEN: We adjust the line wrap length
        int adjustedLength = ImageTextUtil.adjustLineWrapLength(initialLength, imageWidth, imageHeight);
        
        // THEN: The adjusted length should be doubled (30 * 2.0 = 60)
        assertEquals(60, adjustedLength);
    }

    /**
     * Test that adjustLineWrapLength correctly calculates adjusted length for a narrow image.
     */
    @Test
    public void testAdjustLineWrapLength_narrowImage() {
        // GIVEN: A narrow (tall) image (aspect ratio 0.5 = 200/400)
        int imageWidth = 200;
        int imageHeight = 400;
        int initialLength = 30;
        
        // WHEN: We adjust the line wrap length
        int adjustedLength = ImageTextUtil.adjustLineWrapLength(initialLength, imageWidth, imageHeight);
        
        // THEN: The adjusted length should be halved (30 * 0.5 = 15)
        assertEquals(15, adjustedLength);
    }

    /**
     * Test that adjustLineWrapLength returns the same value for a square image.
     */
    @Test
    public void testAdjustLineWrapLength_squareImage() {
        // GIVEN: A square image (aspect ratio 1.0 = 300/300)
        int imageWidth = 300;
        int imageHeight = 300;
        int initialLength = 30;
        
        // WHEN: We adjust the line wrap length
        int adjustedLength = ImageTextUtil.adjustLineWrapLength(initialLength, imageWidth, imageHeight);
        
        // THEN: The adjusted length should be the same (30 * 1.0 = 30)
        assertEquals(30, adjustedLength);
    }

    /**
     * Test that adjustLineWrapLength handles various aspect ratios correctly.
     */
    @Test
    public void testAdjustLineWrapLength_variousAspectRatios() {
        // Test aspect ratio 3:1 (very wide)
        assertEquals(60, ImageTextUtil.adjustLineWrapLength(20, 600, 200));
        
        // Test aspect ratio 1:3 (very narrow)
        assertEquals(6, ImageTextUtil.adjustLineWrapLength(20, 200, 600));
        
        // Test aspect ratio 16:9 (widescreen)
        assertEquals(35, ImageTextUtil.adjustLineWrapLength(20, 1600, 900));
        
        // Test aspect ratio 4:3 (standard)
        assertEquals(26, ImageTextUtil.adjustLineWrapLength(20, 800, 600));
    }

    /**
     * Test that adjustLineWrapLength handles edge cases correctly.
     */
    @Test
    public void testAdjustLineWrapLength_edgeCases() {
        // Test with zero height (should return initial length to avoid division by zero)
        assertEquals(30, ImageTextUtil.adjustLineWrapLength(30, 400, 0));
        
        // Test with negative height (should return initial length)
        assertEquals(30, ImageTextUtil.adjustLineWrapLength(30, 400, -100));
    }

    /**
     * Test handleLineWrap with text shorter than lineLength.
     */
    @Test
    public void testHandleLineWrap_shortText() {
        // GIVEN: Short text
        String shortText = "Hello";
        
        // WHEN: We wrap it with a longer line length
        List<String> lines = ImageTextUtil.handleLineWrap(shortText, 20);
        
        // THEN: We should get exactly one line
        assertEquals(1, lines.size());
        assertEquals("Hello", lines.get(0));
    }

    /**
     * Test handleLineWrap with text longer than lineLength.
     */
    @Test
    public void testHandleLineWrap_longText() {
        // GIVEN: Text longer than line length
        String longText = "This is a longer piece of text that needs wrapping";
        
        // WHEN: We wrap it with a short line length
        List<String> lines = ImageTextUtil.handleLineWrap(longText, 20);
        
        // THEN: We should get multiple lines
        assertTrue(lines.size() > 1, "Long text should wrap into multiple lines");
        
        // AND: Each line should be reasonably close to the line length
        for (String line : lines) {
            assertTrue(line.length() <= 30, 
                      "Each line should not be excessively longer than lineLength");
        }
    }

    /**
     * Test that drawText throws IllegalArgumentException when minFontSize is zero.
     */
    @Test
    public void testDrawText_minFontSizeZero() {
        // GIVEN: A BufferedImage
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(100, 100, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        
        // WHEN/THEN: drawText is called with minFontSize = 0, it should throw IllegalArgumentException
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ImageTextUtil.drawText(image, "Test", 30, ImageTextUtil.DEFAULT_FONT,
                    ImageTextUtil.TextAlign.CENTER, ImageTextUtil.DEFAULT_OUTLINE_COLOR,
                    ImageTextUtil.DEFAULT_OUTLINE_WIDTH_FACTOR, ImageTextUtil.DEFAULT_FILL_COLOR, 0.9, 0);
        });
        assertTrue(exception.getMessage().contains("minFontSize must be greater than 0"));
    }

    /**
     * Test that drawText throws IllegalArgumentException when minFontSize is negative.
     */
    @Test
    public void testDrawText_minFontSizeNegative() {
        // GIVEN: A BufferedImage
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(100, 100, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        
        // WHEN/THEN: drawText is called with minFontSize = -1, it should throw IllegalArgumentException
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ImageTextUtil.drawText(image, "Test", 30, ImageTextUtil.DEFAULT_FONT,
                    ImageTextUtil.TextAlign.CENTER, ImageTextUtil.DEFAULT_OUTLINE_COLOR,
                    ImageTextUtil.DEFAULT_OUTLINE_WIDTH_FACTOR, ImageTextUtil.DEFAULT_FILL_COLOR, 0.9, -1);
        });
        assertTrue(exception.getMessage().contains("minFontSize must be greater than 0"));
    }

    /**
     * Test that drawText throws IllegalArgumentException when maxSizeToFitPercent is below 0.1.
     */
    @Test
    public void testDrawText_maxSizeToFitPercentTooLow() {
        // GIVEN: A BufferedImage
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(100, 100, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        
        // WHEN/THEN: drawText is called with maxSizeToFitPercent = 0.05, it should throw IllegalArgumentException
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ImageTextUtil.drawText(image, "Test", 30, ImageTextUtil.DEFAULT_FONT,
                    ImageTextUtil.TextAlign.CENTER, ImageTextUtil.DEFAULT_OUTLINE_COLOR,
                    ImageTextUtil.DEFAULT_OUTLINE_WIDTH_FACTOR, ImageTextUtil.DEFAULT_FILL_COLOR, 0.05, 12);
        });
        assertTrue(exception.getMessage().contains("maxSizeToFitPercent must be between 0.1 and 1.0"));
    }

    /**
     * Test that drawText throws IllegalArgumentException when maxSizeToFitPercent is above 1.0.
     */
    @Test
    public void testDrawText_maxSizeToFitPercentTooHigh() {
        // GIVEN: A BufferedImage
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(100, 100, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        
        // WHEN/THEN: drawText is called with maxSizeToFitPercent = 1.5, it should throw IllegalArgumentException
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ImageTextUtil.drawText(image, "Test", 30, ImageTextUtil.DEFAULT_FONT,
                    ImageTextUtil.TextAlign.CENTER, ImageTextUtil.DEFAULT_OUTLINE_COLOR,
                    ImageTextUtil.DEFAULT_OUTLINE_WIDTH_FACTOR, ImageTextUtil.DEFAULT_FILL_COLOR, 1.5, 12);
        });
        assertTrue(exception.getMessage().contains("maxSizeToFitPercent must be between 0.1 and 1.0"));
    }

    /**
     * Test that drawText accepts valid boundary values for maxSizeToFitPercent.
     */
    @Test
    public void testDrawText_maxSizeToFitPercentBoundaryValues() {
        // GIVEN: A BufferedImage
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(100, 100, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        
        // WHEN/THEN: drawText is called with maxSizeToFitPercent = 0.1, it should not throw
        assertDoesNotThrow(() -> {
            ImageTextUtil.drawText(image, "Test", 30, ImageTextUtil.DEFAULT_FONT,
                    ImageTextUtil.TextAlign.CENTER, ImageTextUtil.DEFAULT_OUTLINE_COLOR,
                    ImageTextUtil.DEFAULT_OUTLINE_WIDTH_FACTOR, ImageTextUtil.DEFAULT_FILL_COLOR, 0.1, 12);
        });
        
        // AND: drawText is called with maxSizeToFitPercent = 1.0, it should not throw
        assertDoesNotThrow(() -> {
            ImageTextUtil.drawText(image, "Test", 30, ImageTextUtil.DEFAULT_FONT,
                    ImageTextUtil.TextAlign.CENTER, ImageTextUtil.DEFAULT_OUTLINE_COLOR,
                    ImageTextUtil.DEFAULT_OUTLINE_WIDTH_FACTOR, ImageTextUtil.DEFAULT_FILL_COLOR, 1.0, 12);
        });
    }

    /**
     * Test that drawText with just maxSizeToFitPercent parameter also validates correctly.
     */
    @Test
    public void testDrawText_withMaxSizeToFitPercentOnly_invalidValue() {
        // GIVEN: A BufferedImage
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(100, 100, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        
        // WHEN/THEN: drawText is called with invalid maxSizeToFitPercent, it should throw IllegalArgumentException
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ImageTextUtil.drawText(image, "Test", 30, ImageTextUtil.DEFAULT_FONT,
                    ImageTextUtil.TextAlign.CENTER, ImageTextUtil.DEFAULT_OUTLINE_COLOR,
                    ImageTextUtil.DEFAULT_OUTLINE_WIDTH_FACTOR, ImageTextUtil.DEFAULT_FILL_COLOR, 1.5);
        });
        assertTrue(exception.getMessage().contains("maxSizeToFitPercent must be between 0.1 and 1.0"));
    }
}
