package ca.corbett.extras.image;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains utility method for drawing stylized text onto a BufferedImage.
 * Absorbed into sc-util as per UTIL-143.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2023-11-11
 */
public class ImageTextUtil {

    private static final Logger logger = Logger.getLogger(ImageTextUtil.class.getName());

    public enum TextAlign {
        TOP_LEFT("Top left"),
        TOP_CENTER("Top center"),
        TOP_RIGHT("Top right"),
        CENTER_LEFT("Center left"),
        CENTER("Dead center"),
        CENTER_RIGHT("Center right"),
        BOTTOM_LEFT("Bottom left"),
        BOTTOM_CENTER("Bottom center"),
        BOTTOM_RIGHT("Bottom right");

        private final String label;

        TextAlign(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }

        public static TextAlign fromLabel(String label) {
            for (TextAlign align : values()) {
                if (align.label.equals(label)) {
                    return align;
                }
            }
            return null;
        }

    }

    public static final int DEFAULT_LINE_LENGTH = 30;
    public static final Font DEFAULT_FONT = new Font("SansSerif", Font.BOLD, 12);
    public static final Color DEFAULT_FILL_COLOR = Color.WHITE;
    public static final Color DEFAULT_OUTLINE_COLOR = Color.BLACK;
    public static final float DEFAULT_OUTLINE_WIDTH_FACTOR = 8f;

    /**
     * Protected constructor to allow subclassing for application-specific utility methods
     * while preventing direct instantiation of this utility class.
     */
    protected ImageTextUtil() {
    }

    /**
     * Shorthand for drawing text on an image with all default settings. The text will be
     * centered in the image horizontally and vertically and will be line wrapped as needed.
     *
     * @param image The image on which to draw.
     * @param text  The text to be rendered.
     */
    public static void drawText(BufferedImage image, String text) {
        drawText(image,
                 text,
                 DEFAULT_LINE_LENGTH,
                 DEFAULT_FONT,
                 TextAlign.CENTER,
                 DEFAULT_OUTLINE_COLOR,
                 DEFAULT_OUTLINE_WIDTH_FACTOR,
                 DEFAULT_FILL_COLOR,
                 null,
                 new Rectangle(image.getWidth(), image.getHeight()));
    }

    /**
     * Shorthand for drawing text on an image with the given font styling properties.
     * The text will be centered within the image vertically and horizontally and will
     * be line wrapped as needed.
     *
     * @param image        The BufferedImage on which the text will be drawn.
     * @param text         The text to be drawn. Can be arbitrarily long; linewrapping will be applied.
     * @param font         The font to use.
     * @param outlineColor The Color to use for outlining the text (if necessary).
     * @param fillColor    The color to use to fill the text.
     */
    public static void drawText(BufferedImage image, String text, Font font, Color outlineColor, Color fillColor) {
        drawText(image,
                 text,
                 DEFAULT_LINE_LENGTH,
                 font,
                 TextAlign.CENTER,
                 outlineColor,
                 DEFAULT_OUTLINE_WIDTH_FACTOR,
                 fillColor,
                 null,
                 new Rectangle(image.getWidth(), image.getHeight()));
    }

    /**
     * Shorthand for drawing text on an image with the given font styling properties and alignment.
     * The text will be line wrapped as needed.
     *
     * @param image        The BufferedImage on which the text will be drawn.
     * @param text         The text to be drawn. Can be arbitrarily long; linewrapping will be applied.
     * @param font         The font to use.
     * @param outlineColor The Color to use for outlining the text (if necessary).
     * @param fillColor    The color to use to fill the text.
     * @param align        The desired alignment for the text within the image.
     */
    public static void drawText(BufferedImage image, String text, Font font, Color outlineColor,
                                Color fillColor, TextAlign align) {
        drawText(image,
                 text,
                 DEFAULT_LINE_LENGTH,
                 font,
                 align,
                 outlineColor,
                 DEFAULT_OUTLINE_WIDTH_FACTOR,
                 fillColor,
                 null,
                 new Rectangle(image.getWidth(), image.getHeight()));
    }

    /**
     * Outputs the given lines of text using the given styling parameters.
     * Text will be line wrapped and scaled as needed to fit in the image.
     *
     * @param image              The BufferedImage on which the text will be drawn.
     * @param text               The text to be drawn. Can be arbitrarily long; linewrapping will be applied.
     * @param lineLength         The desired length of each line. Will be adjusted based on image dimensions.
     * @param font               The font to use.
     * @param align              A TextAlign value for placing the text within our rectangle.
     * @param outlineColor       The Color to use for outlining the text (if necessary).
     * @param outlineWidthFactor fontPointSize/outlineWidthFactor determines thickness of text border.
     * @param fillColor          The color to use to fill the text.
     */
    public static void drawText(BufferedImage image, String text, int lineLength, Font font,
                                TextAlign align, Color outlineColor, float outlineWidthFactor, Color fillColor) {
        drawText(image,
                 text,
                 lineLength,
                 font,
                 align,
                 outlineColor,
                 outlineWidthFactor,
                 fillColor,
                 null,
                 new Rectangle(image.getWidth(), image.getHeight()));
    }

    /**
     * Outputs the given lines of text using the given styling parameters and the
     * given placement rectangle. Text will be line wrapped and scaled as needed to fit
     * in the given Rectangle.
     *
     * @param image              The BufferedImage on which the text will be drawn.
     * @param text               The text to be drawn. Can be arbitrarily long; linewrapping will be applied.
     * @param lineLength         The desired length of each line. Will be adjusted based on image dimensions.
     * @param font               The font to use.
     * @param align              A TextAlign value for placing the text within our rectangle.
     * @param outlineColor       The Color to use for outlining the text (if necessary).
     * @param outlineWidthFactor fontPointSize/outlineWidthFactor determines thickness of text border.
     * @param fillColor          The color to use to fill the text.
     * @param fillTexture        An image to use to fill text (if fillColor is null, otherwise ignored).
     * @param rect               The Rectangle in image coordinates in which the text will be drawn.
     */
    public static void drawText(BufferedImage image, String text, int lineLength, Font font,
                                TextAlign align, Color outlineColor, float outlineWidthFactor,
                                Color fillColor, BufferedImage fillTexture, Rectangle rect) {

        // put a 1 pixel margin on all edges, just to avoid wonkiness later
        int boundLeft = (int)rect.getX() + 1;
        int boundTop = (int)rect.getY() + 1;
        int boundRight = boundLeft + (int)rect.getWidth() - 2;
        int boundBottom = boundTop + (int)rect.getHeight() - 2;
        int boundWidth = (int)rect.getWidth() - 2;
        int boundHeight = (int)rect.getHeight() - 2;

        // Adjust line wrap limit based on the image aspect ratio.
        // Wide images can have more characters per line, narrow images have less space for text.
        int linewrapLength = (int)(lineLength * ((float)image.getWidth() / image.getHeight()));
        if (linewrapLength != lineLength) {
            logger.log(Level.FINE,
                       "drawText: adjusting linewrap limit from {0} to {1} based on image dimensions.",
                       new Object[]{lineLength, linewrapLength});
        }

        // Now handle line wrapping as needed:
        List<String> lines = handleLineWrap(text, linewrapLength);

        Graphics2D g = image.createGraphics();

        // Figure out the smallest font size that will cause the longest line of text to fit
        // comfortably within our given Rectangle:
        int fontPointSize = computeFontSize(font, lines, g, boundLeft, boundTop, boundRight, boundBottom);
        font = font.deriveFont((float)fontPointSize);
        g.setFont(font);

        int textY = 0; // 0 means compute, other values mean increment because we're on a subsequent line
        for (String line : lines) {
            int textWidth = g.getFontMetrics().stringWidth(line);
            int textHeight = (int)(g.getFontMetrics().getLineMetrics(line, g).getHeight());
            int textX;
            switch (align) {
                case TOP_LEFT:
                case CENTER_LEFT:
                case BOTTOM_LEFT:
                    textX = boundLeft;
                    break;
                case TOP_RIGHT:
                case CENTER_RIGHT:
                case BOTTOM_RIGHT:
                    textX = boundRight - textWidth; //  - (int)(textWidth * 0.1); // 0.1 = kludge to fix bad centering
                    break;
                default:
                    textX = boundLeft + (boundWidth - textWidth) / 2;//  - (int)(textWidth * 0.1); // 0.1 = kludge to fix bad centering
            }
            if (textY == 0) {
                int paragraphHeight = textHeight * lines.size();
                switch (align) {
                    case TOP_LEFT:
                    case TOP_CENTER:
                    case TOP_RIGHT:
                        textY = boundTop;
                        break;
                    case BOTTOM_LEFT:
                    case BOTTOM_CENTER:
                    case BOTTOM_RIGHT:
                        textY = boundBottom - paragraphHeight; //  - (int)(textHeight * 0.2); // 0.2 = kludge as above
                        break;
                    default:
                        textY = boundTop + ((boundHeight - paragraphHeight) / 2); //  - (int)(textHeight * 0.2); // 0.2 = kludge as above
                }
            }
            else {
                textY += textHeight;
            }

            // Bounds check:
            if (outlineWidthFactor < 1) {
                outlineWidthFactor = 1;
            }

            // Render the text into a temporary image and then blit that image onto ours with transparency.
            // This is slightly easier than rendering the text directly onto our image for placement reasons.
            int tmpImgWidth = (int)(textWidth * 1.2);
            if (tmpImgWidth <= 0) { // can happen with certain input strings
                tmpImgWidth = 1;
            }
            BufferedImage textImage = new BufferedImage(tmpImgWidth, textHeight * 2, BufferedImage.TYPE_INT_ARGB);
            Graphics2D textGraphics = textImage.createGraphics();
            textGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                          RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            textGraphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                                          RenderingHints.VALUE_RENDER_QUALITY);
            textGraphics.setColor(new Color(0, 0, 0, 0)); // transparent background
            textGraphics.fillRect(0, 0, (int)(textWidth * 1.2), textHeight * 2);
            textGraphics.setColor(outlineColor);
            textGraphics.setStroke(
                    new BasicStroke(fontPointSize / outlineWidthFactor, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            textGraphics.setFont(font);
            GlyphVector vector = font.createGlyphVector(textGraphics.getFontRenderContext(), line);
            Shape textShape = vector.getOutline();

            // Move slightly to the right before drawing to avoid clipping the text on the left edge.
            // Also note that draw(Shape) seems to set its 0,0 origin to the bottom left of the Shape
            // to be drawn, rather than the top left, so we have to move to the bottom of where the
            // text will go.
            //textGraphics.translate((int)(narrowestLine * 0.2), textHeight);
            textGraphics.translate(0, textHeight);
            if (outlineWidthFactor > 1) {
                textGraphics.draw(textShape);
            }

            if (fillColor != null) {
                textGraphics.setColor(fillColor);
            }
            else {
                textGraphics.setPaint(
                        new TexturePaint(fillTexture,
                                         new Rectangle(0, 0, fillTexture.getWidth(), fillTexture.getHeight())));
            }
            textGraphics.fill(textShape);
            textGraphics.dispose();
            g.drawImage(textImage, textX, textY, null);
        }

        g.dispose();
    }

    /**
     * Invoked internally to break up a single long line into multiple short lines, if necessary.
     * The given lineLength is used as a guide to determine how long a line must be before
     * it is split, but the actual split point may vary, as we try to only split a line on
     * a space character to avoid wrapping a line in the middle of a word.
     * If the given text is shorter than the given lineLength, then the returned
     * array will contain just that one string.
     *
     * @param text       The text to be split up (if needed).
     * @param lineLength The length at which the given text will be split.
     * @return A List containing one or more lines generated from the line wrapping.
     */
    protected static List<String> handleLineWrap(String text, int lineLength) {
        List<String> lines = new ArrayList<>();
        return handleLineWrap(lines, text, lineLength);
    }

    /**
     * Invoked internally to recurse and handle line wrapping as needed.
     * See handleLineWrap(String,int)
     *
     * @param lines      A List of Strings which will be added to as needed.
     * @param text       The remaining text to be split.
     * @param lineLength The length at which the line will be line-wrapped.
     * @return The modified input List.
     */
    protected static List<String> handleLineWrap(List<String> lines, String text, int lineLength) {
        if (text.length() > lineLength) {
            // Try to break on a space character:
            int splitIndex = lineLength;
            for (int index = lineLength; index >= 0; index--) {
                if (text.charAt(index) == ' ') {
                    splitIndex = index;
                    break;
                }
            }
            lines.add(text.substring(0, splitIndex));
            return handleLineWrap(lines, text.substring(splitIndex + 1), lineLength);
        }
        else {
            lines.add(text);
        }
        return lines;
    }

    /**
     * Invoked internally to determine the smallest font point size that will allow the given
     * text to fit comfortably inside the given pixel boundary.
     * A minimum font point size of 12 is used to prevent text from getting too long to see.
     * Very very long lines will therefore overflow if line wrapping has not been performed.
     *
     * @param font   The font to use for calculation purposes.
     * @param text   The block of text in question.
     * @param g      A Graphics object which will be used to retrieve font metrics.
     * @param left   The left edge of the text zone.
     * @param top    The top edge of the text zone.
     * @param right  The right edge of the text zone.
     * @param bottom The bottom edge of the text zone.
     * @return A font point size appropriate for the given text in the given boundary.
     */
    protected static int computeFontSize(Font font, List<String> text, Graphics2D g, int left, int top, int right, int bottom) {
        int fontPointSize = 150; // huge default, we'll shrink it down to fit
        int boundWidth = right - left;
        int boundHeight = bottom - top;
        int fontDecrement = 1;

        // It's possible we were given very large boundary dimensions.
        // If so, increase our starting font size and our search decrement accordingly.
        // Note: we could just start with a font size of a million and work our way down, but
        // in the interest of performance it's better if we don't have to do that.
        int tempWidth = boundWidth;
        while (tempWidth > 1500) {
            fontPointSize += 100;
            fontDecrement++;
            tempWidth -= 1000;
        }

        // Now search for a size that fits, assuming that we're starting with a size that's
        // too large, and decrementing it down until we find the largest size that fits.
        String longestLine = findLongestLine(text);
        boolean fits = false;
        while (!fits) {
            g.setFont(font.deriveFont((float)fontPointSize));
            int stringWidth = g.getFontMetrics().stringWidth(longestLine);
            int stringHeight = (int)(g.getFontMetrics().getLineMetrics(longestLine, g).getHeight());
            int paragraphHeight = stringHeight * text.size();
            if (stringWidth < (boundWidth * 0.9)
                    && paragraphHeight < (boundHeight * 0.9)) { // TODO height limit should be configurable
                fits = true;
            }
            else {
                fontPointSize -= fontDecrement;
                if (fontPointSize <= 12) { // TODO this minimum should probably also be configurable
                    logger.info("computeFontSize: text too small! Applying minimum point size of 12.");
                    break; // don't go so small you can't see it
                }
            }
        }

        logger.log(Level.FINE, "computeFontSize: computed font point size of {0} for text \"{1}\"",
                   new Object[]{fontPointSize, text});
        return fontPointSize;
    }

    protected static String findLongestLine(List<String> lines) {
        // Lines may have different character lengths. This is a problem because if each line
        // is sized independently, it will result in lines having different font sizes, because
        // each line is scaled to fit the available width within the given Rectangle.
        // So, we'll base the font size on the character length of the longest line of text,
        // so that each line of text will have the same font size. This makes for better,
        // more consistent-looking text output.
        String longestLine = lines.get(0);
        for (int i = 1; i < lines.size(); i++) {
            if (longestLine.length() < lines.get(i).length()) {
                longestLine = lines.get(i);
            }
        }
        return longestLine;
    }
}
