package ca.corbett.extras.image.animation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the BlurLayerUI class.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class BlurLayerUITest {

    private BlurLayerUI blurLayerUI;

    @BeforeEach
    void setUp() {
        blurLayerUI = new BlurLayerUI();
    }

    @Test
    public void setOverlayText_singleLine_storedAsIs() {
        blurLayerUI.setOverlayText("Hello World");
        assertEquals("Hello World", blurLayerUI.getOverlayText());
    }

    @Test
    public void setOverlayText_multiLine_storedAsIs() {
        blurLayerUI.setOverlayText("Line 1\nLine 2\nLine 3");
        assertEquals("Line 1\nLine 2\nLine 3", blurLayerUI.getOverlayText());
    }

    @Test
    public void setOverlayText_null_storedAsNull() {
        blurLayerUI.setOverlayText("some text");
        blurLayerUI.setOverlayText(null);
        assertNull(blurLayerUI.getOverlayText());
    }

    @Test
    public void setOverlayText_leadingLineBreaks_storedAsIs() {
        blurLayerUI.setOverlayText("\n\n\nLeading breaks");
        assertEquals("\n\n\nLeading breaks", blurLayerUI.getOverlayText());
    }

    @Test
    public void setOverlayText_trailingLineBreaks_storedAsIs() {
        blurLayerUI.setOverlayText("Trailing breaks\n\n\n");
        assertEquals("Trailing breaks\n\n\n", blurLayerUI.getOverlayText());
    }

    @Test
    public void setOverlayTextSize_validSize_accepted() {
        blurLayerUI.setOverlayTextSize(24);
        assertEquals(24, blurLayerUI.getOverlayTextSize());
    }

    @Test
    public void setOverlayTextSize_tooSmall_throwsException() {
        assertThrows(IllegalArgumentException.class,
                     () -> blurLayerUI.setOverlayTextSize(BlurLayerUI.TEXT_MINIMUM_SIZE - 1));
    }

    @Test
    public void setOverlayTextSize_tooLarge_throwsException() {
        assertThrows(IllegalArgumentException.class,
                     () -> blurLayerUI.setOverlayTextSize(BlurLayerUI.TEXT_MAXIMUM_SIZE + 1));
    }

    @Test
    public void setOverlayTextColor_colorIsStored() {
        blurLayerUI.setOverlayTextColor(Color.RED);
        assertEquals(Color.RED, blurLayerUI.getOverlayTextColor());
    }

    @Test
    public void defaultValues_areCorrect() {
        assertEquals(BlurLayerUI.DEFAULT_TEXT_SIZE, blurLayerUI.getOverlayTextSize());
        assertEquals(BlurLayerUI.DEFAULT_TEXT_COLOR, blurLayerUI.getOverlayTextColor());
        assertEquals(BlurLayerUI.DEFAULT_INTENSITY, blurLayerUI.getBlurIntensity());
        assertNull(blurLayerUI.getOverlayText());
    }
}
