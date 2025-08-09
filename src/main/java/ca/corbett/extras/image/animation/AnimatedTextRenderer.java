package ca.corbett.extras.image.animation;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Animates a text display, such that a given text string will be slowly "printed" out character
 * by character with configurable styling and with a configurable speed.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a> (with help from Claude!)
 * @since swing-extras 2.3
 */
public class AnimatedTextRenderer {
    // Default styling constants
    public static final Font DEFAULT_FONT = new Font("Monospaced", Font.PLAIN, 48);
    public static final Color DEFAULT_TEXT_COLOR = Color.GREEN;
    public static final Color DEFAULT_BACKGROUND_COLOR = Color.BLACK;

    // Animation state
    protected final BufferedImage buffer;
    protected final Graphics2D bufferGraphics;
    protected String textToRender;
    protected List<String> wrappedLines;
    protected int totalChars;
    protected int currentCharIndex;
    protected double charsPerSecond;
    protected long lastUpdateTime;
    protected double charAccumulator; // For smooth fractional character progression

    // Cursor state
    protected boolean showCursor;
    protected double cursorBlinkRate; // Blinks per second
    protected double cursorAccumulator;

    // Styling
    protected Font font;
    protected Color textColor;
    protected Color backgroundColor;
    protected Color cursorColor;

    // Layout
    protected int padding;
    protected boolean needsReflow;

    /**
     * Creates a new AnimatedTextRenderer with the given dimensions and the given text string.
     * All styling options and speed will use built-in default values.
     */
    public AnimatedTextRenderer(int width, int height, String text) {
        this(width, height, text, 3.0, DEFAULT_FONT, DEFAULT_TEXT_COLOR, DEFAULT_BACKGROUND_COLOR);
    }

    /**
     * Creates a new AnimatedTextRenderer with the given dimensions, text string, and typing speed.
     * All styling options will use built-in default values.
     */
    public AnimatedTextRenderer(int width, int height, String text, double charsPerSecond) {
        this(width, height, text, charsPerSecond, DEFAULT_FONT, DEFAULT_TEXT_COLOR, DEFAULT_BACKGROUND_COLOR);
    }

    /**
     * Creates a new AnimatedTextRenderer with the given dimensions, text string, typing speed,
     * and styling options.
     */
    public AnimatedTextRenderer(int width, int height, String text, double charsPerSecond,
                                Font font, Color textColor, Color backgroundColor) {
        this.buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        this.bufferGraphics = buffer.createGraphics();
        this.textToRender = text;
        this.charsPerSecond = charsPerSecond;
        this.font = font;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
        this.cursorColor = textColor; // Default cursor color same as text
        this.cursorBlinkRate = 0.0; // Eh, I'm not wild about the blinking, default it to off
        this.padding = 10;
        this.needsReflow = true;

        // Initialize animation state
        this.currentCharIndex = 0;
        this.charAccumulator = 0.0;
        this.showCursor = true;
        this.cursorAccumulator = 0.0;
        this.lastUpdateTime = System.currentTimeMillis();

        // Set up graphics context
        setupGraphics();

        // Initial clear
        clearBuffer();
    }

    protected void setupGraphics() {
        bufferGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        bufferGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        bufferGraphics.setFont(font);
        bufferGraphics.setColor(textColor);
    }

    protected void clearBuffer() {
        bufferGraphics.setColor(backgroundColor);
        bufferGraphics.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
        bufferGraphics.setColor(textColor);
    }

    /**
     * Updates the animation state and renders new characters if needed.
     * Call this once per frame from your animation loop. This method checks how
     * long it's been since the last time it was invoked, and will output the appropriate
     * number of characters using the currently configured charsPerSecond.
     */
    public void updateTextAnimation() {
        if (textToRender == null || textToRender.isEmpty()) {
            return;
        }

        // Reflow text if needed (first time or after changes)
        if (needsReflow) {
            reflowText();
            needsReflow = false;
        }

        // Calculate time delta
        long currentTime = System.currentTimeMillis();
        double deltaTime = (currentTime - lastUpdateTime) / 1000.0; // Convert to seconds
        lastUpdateTime = currentTime;

        // Update character accumulator
        charAccumulator += charsPerSecond * deltaTime;

        // Update cursor blink (only if animation is not complete)
        boolean animationComplete = currentCharIndex >= totalChars;
        if (!animationComplete) {
            cursorAccumulator += cursorBlinkRate * deltaTime;
            showCursor = ((int)cursorAccumulator) % 2 == 0; // Toggle cursor visibility
        }

        // Check if we need to reveal more characters or update cursor
        int newCharIndex = Math.min((int)charAccumulator, totalChars);

        if (newCharIndex > currentCharIndex || !animationComplete) {
            // Render new characters and/or cursor
            renderUpToCharacter(newCharIndex);
            currentCharIndex = newCharIndex;
        }
    }

    /**
     * Reflows the text to fit within the buffer bounds
     */
    protected void reflowText() {
        if (textToRender == null) {
            return;
        }

        FontRenderContext frc = bufferGraphics.getFontRenderContext();
        int availableWidth = buffer.getWidth() - (padding * 2);

        this.wrappedLines = wrapText(textToRender, frc, font, availableWidth);
        this.totalChars = textToRender.length();

        // Clear buffer and reset animation
        clearBuffer();
        this.currentCharIndex = 0;
        this.charAccumulator = 0.0;
        this.cursorAccumulator = 0.0;
        this.showCursor = true;
    }

    /**
     * Renders text up to the specified character index
     */
    protected void renderUpToCharacter(int charIndex) {
        if (wrappedLines == null || wrappedLines.isEmpty()) {
            return;
        }

        // Clear buffer
        clearBuffer();

        FontMetrics fm = bufferGraphics.getFontMetrics();
        int lineHeight = fm.getHeight();
        int y = padding + fm.getAscent();

        int charsProcessed = 0;
        CursorPosition cursorPos = null;

        for (String line : wrappedLines) {
            if (charsProcessed >= charIndex) {
                // We've reached the end of revealed text, cursor goes here
                if (cursorPos == null && charsProcessed < totalChars) {
                    cursorPos = new CursorPosition(padding, y, lineHeight);
                }
                break;
            }

            // Determine how many characters of this line to draw
            int charsToDrawFromLine = Math.min(line.length(), charIndex - charsProcessed);
            String textToDraw = line.substring(0, charsToDrawFromLine);

            // Draw the text
            bufferGraphics.drawString(textToDraw, padding, y);

            // Check if cursor should be positioned at end of this partial line
            if (charsProcessed + charsToDrawFromLine == charIndex && charIndex < totalChars) {
                int textWidth = fm.stringWidth(textToDraw);
                cursorPos = new CursorPosition(padding + textWidth, y, lineHeight);
            }

            charsProcessed += line.length();
            y += lineHeight;

            // Check if we've exceeded the bottom boundary
            if (y > buffer.getHeight() - padding) {
                break;
            }
        }

        // Draw cursor if animation is not complete and cursor should be visible
        if (cursorPos != null && showCursor && charIndex < totalChars) {
            drawCursor(cursorPos);
        }
    }

    /**
     * Draws the block cursor at the specified position
     */
    protected void drawCursor(CursorPosition pos) {
        FontMetrics fm = bufferGraphics.getFontMetrics();
        int cursorWidth = fm.charWidth('M'); // Use 'M' width for cursor size
        int cursorHeight = fm.getHeight();

        // Save current color
        Color originalColor = bufferGraphics.getColor();

        // Draw cursor background
        bufferGraphics.setColor(cursorColor);
        bufferGraphics.fillRect(pos.x, pos.y - fm.getAscent(), cursorWidth, cursorHeight);

        // Restore original color
        bufferGraphics.setColor(originalColor);
    }

    /**
     * Helper class to store cursor position
     */
    protected static class CursorPosition {
        final int x, y, lineHeight;

        CursorPosition(int x, int y, int lineHeight) {
            this.x = x;
            this.y = y;
            this.lineHeight = lineHeight;
        }
    }

    /**
     * Wraps text to fit within the specified width
     */
    protected List<String> wrapText(String text, FontRenderContext frc, Font font, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split("\\s+");

        if (words.length == 0) {
            return lines;
        }

        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;

            // Check if adding this word would exceed the width
            TextLayout layout = new TextLayout(testLine, font, frc);
            if (layout.getBounds().getWidth() > maxWidth && currentLine.length() != 0) {
                // Start a new line
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            }
            else {
                currentLine = new StringBuilder(testLine);
            }
        }

        // Add the last line if it has content
        if (currentLine.length() != 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    /**
     * Returns the current buffer image
     */
    public BufferedImage getBuffer() {
        return buffer;
    }

    /**
     * Changes the text to animate (restarts animation)
     */
    public void setText(String text) {
        this.textToRender = text;
        this.needsReflow = true;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * Changes the animation speed
     */
    public void setCharsPerSecond(double charsPerSecond) {
        this.charsPerSecond = charsPerSecond;
    }

    /**
     * Changes the font (triggers reflow)
     */
    public void setFont(Font font) {
        this.font = font;
        this.needsReflow = true;
        bufferGraphics.setFont(font);
    }

    /**
     * Changes text color
     */
    public void setTextColor(Color textColor) {
        this.textColor = textColor;
        this.cursorColor = textColor; // Update cursor color to match
        // Re-render current text with new color
        if (currentCharIndex > 0) {
            renderUpToCharacter(currentCharIndex);
        }
    }

    /**
     * Sets cursor color independently of text color
     */
    public void setCursorColor(Color cursorColor) {
        this.cursorColor = cursorColor;
        // Re-render if cursor might be visible
        if (currentCharIndex < totalChars) {
            renderUpToCharacter(currentCharIndex);
        }
    }

    /**
     * Sets cursor blink rate (blinks per second)
     */
    public void setCursorBlinkRate(double blinksPerSecond) {
        this.cursorBlinkRate = blinksPerSecond;
    }

    /**
     * Changes background color
     */
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        // Re-render current text with new background
        if (currentCharIndex > 0) {
            renderUpToCharacter(currentCharIndex);
        }
    }

    /**
     * Sets padding around text
     */
    public void setPadding(int padding) {
        this.padding = padding;
        this.needsReflow = true;
    }

    /**
     * Checks if animation is complete
     */
    public boolean isAnimationComplete() {
        return currentCharIndex >= totalChars;
    }

    /**
     * Immediately shows all text (skips animation)
     */
    public void showAllText() {
        if (wrappedLines != null) {
            renderUpToCharacter(totalChars);
            currentCharIndex = totalChars;
            charAccumulator = totalChars;
        }
    }

    /**
     * Resets animation to beginning
     */
    public void resetAnimation() {
        this.currentCharIndex = 0;
        this.charAccumulator = 0.0;
        this.cursorAccumulator = 0.0;
        this.showCursor = true;
        this.lastUpdateTime = System.currentTimeMillis();
        clearBuffer();
    }

    /**
     * Gets current progress as a percentage (0.0 to 1.0)
     */
    public double getProgress() {
        return totalChars > 0 ? (double)currentCharIndex / totalChars : 0.0;
    }

    /**
     * Cleans up resources
     */
    public void dispose() {
        if (bufferGraphics != null) {
            bufferGraphics.dispose();
        }
    }
}
