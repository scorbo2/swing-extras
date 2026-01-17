package ca.corbett.extras.gradient;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.image.BufferedImage;

/**
 * Contains some static utility methods for dealing with drawing gradients into images.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2022-05-10
 */
public class GradientUtil {

    protected GradientUtil() {
    }

    /**
     * Creates a new RGB image of the given width and height, and then fills it with the given
     * Gradient.
     *
     * @param conf   The Gradient to use to fill the image.
     * @param width  The width of the image to generate.
     * @param height The height of the image to generate.
     * @return A BufferedImage containing the requested gradient.
     */
    public static BufferedImage createGradientImage(Gradient conf, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        fill(conf, graphics, 0, 0, width, height);
        graphics.dispose();
        return image;
    }

    /**
     * Renders a gradient fill into the given Graphics2D object using the given Gradient.
     *
     * @param conf     The Gradient describing the type of gradient to render.
     * @param graphics A Graphics2D object to receive the gradient. Will not call dispose() here.
     * @param x1       The x co-ordinate of the upper left area to fill.
     * @param y1       The y co-ordinate of the upper left area to fill.
     * @param width    The width of the gradient fill.
     * @param height   The height of the gradient fill.
     */
    public static void fill(Gradient conf, Graphics2D graphics, int x1, int y1, int width, int height) {
        // Figure out our extent:
        final int centerX = x1 + (width / 2);
        final int centerY = y1 + (height / 2);
        int x2 = x1 + width;
        int y2 = y1 + height;

        // Do it:
        switch (conf.type()) {
            case HORIZONTAL_LINEAR:
                fillHorizontalGradient(graphics, x1, y1, x2, y2, conf.color1(), conf.color2());
                break;

            case VERTICAL_LINEAR:
                fillVerticalGradient(graphics, x1, y1, x2, y2, conf.color1(), conf.color2());
                break;

            case HORIZONTAL_STRIPE:
                fillVerticalGradient(graphics, x1, y1, x2, centerY, conf.color1(), conf.color2());
                fillVerticalGradient(graphics, x1, centerY, x2, y2, conf.color2(), conf.color1());
                break;

            case VERTICAL_STRIPE:
                fillHorizontalGradient(graphics, x1, y1, centerX, y2, conf.color1(), conf.color2());
                fillHorizontalGradient(graphics, centerX, y1, x2, y2, conf.color2(), conf.color1());
                break;

            case DIAGONAL1:
                fillDiagonal1Gradient(graphics, x1, y1, x2, y2, conf.color1(), conf.color2());
                break;

            case DIAGONAL2:
                fillDiagonal2Gradient(graphics, x1, y1, x2, y2, conf.color1(), conf.color2());
                break;

            case STAR:
                fillDiagonal1Gradient(graphics, x1, y1, centerX, centerY, conf.color1(), conf.color2());
                fillDiagonal1Gradient(graphics, centerX, centerY, x2, y2, conf.color2(), conf.color1());
                fillDiagonal2Gradient(graphics, x1, centerY, centerX, y2, conf.color1(), conf.color2());
                fillDiagonal2Gradient(graphics, centerX, y1, x2, centerY, conf.color2(), conf.color1());
                break;
        }
    }

    /**
     * Draws an unfilled rectangle into the given Graphics2D object using the given Gradient.
     *
     * @param conf     The Gradient describing the gradient to use.
     * @param graphics The Graphics2D object to use. Won't dispose() it here.
     * @param x1       The x value of the left edge of the rectangle.
     * @param y1       The y value of the top edge of the rectangle.
     * @param width    The pixel width of the rectangle.
     * @param height   The pixel height of the rectangle.
     */
    public static void drawRect(Gradient conf, Graphics2D graphics, int x1, int y1, int width, int height) {
        // Figure out our extent:
        final int centerX = x1 + (width / 2);
        final int centerY = y1 + (height / 2);
        int x2 = x1 + width;
        int y2 = y1 + height;

        // Do it:
        // (unfortunately have to inline these because we don't want to draw full rects
        //  for all the ones that are split into 2 or 4 calls):
        switch (conf.type()) {
            case HORIZONTAL_LINEAR:
                graphics.setPaint(new GradientPaint(x1, y1, conf.color1(), x2, y1, conf.color2()));
                graphics.drawRect(x1, y1, width, height);
                break;

            case VERTICAL_LINEAR:
                graphics.setPaint(new GradientPaint(x1, y1, conf.color1(), x1, y2, conf.color2()));
                graphics.drawRect(x1, y1, width, height);
                break;

            case HORIZONTAL_STRIPE:
                graphics.setPaint(new GradientPaint(x1, y1, conf.color1(), x1, centerY, conf.color2()));
                graphics.drawLine(x1, y1, x1, centerY);
                graphics.drawLine(x2, y1, x2, centerY);
                graphics.drawLine(x1, y1, x2, y1);
                graphics.setPaint(new GradientPaint(x1, centerY, conf.color2(), x1, y2, conf.color1()));
                graphics.drawLine(x1, centerY, x1, y2);
                graphics.drawLine(x2, centerY, x2, y2);
                graphics.drawLine(x1, y2, x2, y2);
                break;

            case VERTICAL_STRIPE:
                graphics.setPaint(new GradientPaint(x1, y1, conf.color1(), centerX, y1, conf.color2()));
                graphics.drawLine(x1, y1, centerX, y1);
                graphics.drawLine(x1, y2, centerX, y2);
                graphics.drawLine(x1, y1, x1, y2);
                graphics.setPaint(new GradientPaint(centerX, y1, conf.color2(), x2, y1, conf.color1()));
                graphics.drawLine(centerX, y1, x2, y1);
                graphics.drawLine(centerX, y2, x2, y2);
                graphics.drawLine(x2, y1, x2, y2);
                break;

            case DIAGONAL1:
                graphics.setPaint(new GradientPaint(x1, y1, conf.color1(), x2, y2, conf.color2()));
                graphics.drawRect(x1, y1, width, height);
                break;

            case DIAGONAL2:
                graphics.setPaint(new GradientPaint(x1, y2, conf.color1(), x2, y1, conf.color2()));
                graphics.drawRect(x1, y1, width, height);
                break;

            case STAR:
                graphics.setPaint(new GradientPaint(x1, y1, conf.color1(), centerX, centerY, conf.color2()));
                graphics.drawLine(x1, y1, centerX, y1);
                graphics.drawLine(x1, y1, x1, centerY);
                graphics.setPaint(new GradientPaint(centerX, centerY, conf.color2(), x2, y2, conf.color1()));
                graphics.drawLine(centerX, y2, x2, y2);
                graphics.drawLine(x2, centerY, x2, y2);
                graphics.setPaint(new GradientPaint(x1, y2, conf.color1(), centerX, centerY, conf.color2()));
                graphics.drawLine(x1, centerY, x1, y2);
                graphics.drawLine(x1, y2, centerX, y2);
                graphics.setPaint(new GradientPaint(centerX, centerY, conf.color2(), x2, y1, conf.color1()));
                graphics.drawLine(centerX, y1, x2, y1);
                graphics.drawLine(x2, y1, x2, centerY);
                break;
        }
    }

    /**
     * Draws the given String into the given Graphics2D object using the given Gradient.
     * The boundaries for the gradient will be determined automatically based on the text extents.
     * If this is unacceptable, use the other drawString() method to set them manually.
     *
     * @param conf     The Gradient describing the gradient to use.
     * @param graphics The Graphics2D object to use. Won't call dispose() here.
     * @param textX    The x value of the left of the text.
     * @param textY    The y value of the text baseline.
     * @param string   The string to render.
     */
    public static void drawString(Gradient conf, Graphics2D graphics, int textX, int textY, String string) {
        // Kludge alert: stringWidth() and getHeight() are often shy of the actual number,
        // so we need to adjust our gradient bounding box accordingly:
        int textWidth = graphics.getFontMetrics().stringWidth(string);
        int textHeight = (int)graphics.getFontMetrics().getLineMetrics(string, graphics).getHeight();
        int widthFudgeFactor = (int)(textWidth * 0.1);
        int heightFudgeFactor = (int)(textHeight * 0.5);
        int gradientX1 = textX - (widthFudgeFactor / 2);
        int gradientY1 = textY - textHeight; // - (heightFudgeFactor / 2);
        int gradientX2 = textX + textWidth + (widthFudgeFactor / 2);
        int gradientY2 = textY + heightFudgeFactor;

        drawString(conf, graphics, gradientX1, gradientY1, gradientX2, gradientY2, textX, textY, string);
    }

    /**
     * Draws the given String into the given Graphics2D object using the given Gradient.
     * The gradient bounds will not be inferred from the text! This is because it's quite often
     * nice to be able to set the gradient bounds to a larger area than just the text (else the
     * gradient is less effective). For example, if your image is 500x500 and your text is only
     * 300x40, you might want to gradient bounds to be 0,0 to 499,499 so you can draw the
     * text right in the middle and have it better match the desired gradient of the image.
     * If you don't care about the gradient bounds, you can invoke
     * drawString(Gradient,Graphics2D,int,int) instead and they will be auto-determined
     * based on the size of the text string to draw.
     * <p>
     * Note that this allows you to make multiple calls to this method with the same full-sized
     * gradient bounds to write out multiple lines of text one after the other with the same
     * gradient applying to all of them, as opposed to each line of text having its own
     * gradient box calculated according to the text extents of that one line. So, you could
     * do a gradual vertical gradient that spans multiple lines by putting all the lines
     * within one large rectangle (the gradient bounding area).
     * </p>
     *
     * @param conf       The Gradient describing the gradient to use.
     * @param graphics   The Graphics2D object to use. Won't call dispose() here.
     * @param gradientX1 The left edge of the text gradient area.
     * @param gradientY1 The top edge of the text gradient area.
     * @param gradientX2 The right edge of the text gradient area.
     * @param gradientY2 The bottom edge of the text gradient area.
     * @param textX      The left edge of the text location.
     * @param textY      The bottom edge of the text location.
     * @param string     The String to render.
     */
    public static void drawString(Gradient conf, Graphics2D graphics, int gradientX1, int gradientY1, int gradientX2, int gradientY2, int textX, int textY, String string) {
        // Figure out our extent:
        int width = gradientX2 - gradientX1;
        int height = gradientY2 - gradientY1;
        final int centerX = gradientX1 + (width / 2);
        final int centerY = gradientY1 + (height / 2);
        Shape oldClip = graphics.getClip();

        // Do it:
        // (unfortunately have to inline these because we don't want to draw full strings
        //  for all the ones that are split into 2 or 4 calls):
        switch (conf.type()) {
            case HORIZONTAL_LINEAR:
                graphics.setPaint(new GradientPaint(gradientX1, gradientY1, conf.color1(), gradientX2, gradientY1,
                                                    conf.color2()));
                graphics.drawString(string, textX, textY);
                break;

            case VERTICAL_LINEAR:
                graphics.setPaint(new GradientPaint(gradientX1, gradientY1, conf.color1(), gradientX1, gradientY2,
                                                    conf.color2()));
                graphics.drawString(string, textX, textY);
                break;

            case HORIZONTAL_STRIPE:
                graphics.setPaint(new GradientPaint(gradientX1, gradientY1, conf.color1(), gradientX1, centerY,
                                                    conf.color2()));
                graphics.setClip(gradientX1, gradientY1, width, height / 2);
                graphics.drawString(string, textX, textY);
                graphics.setPaint(new GradientPaint(gradientX1, centerY, conf.color2(), gradientX1, gradientY2,
                                                    conf.color1()));
                graphics.setClip(gradientX1, centerY, width, height / 2);
                graphics.drawString(string, textX, textY);
                break;

            case VERTICAL_STRIPE:
                graphics.setPaint(new GradientPaint(gradientX1, gradientY1, conf.color1(), centerX, gradientY1,
                                                    conf.color2()));
                graphics.setClip(gradientX1, gradientY1, width / 2, height);
                graphics.drawString(string, textX, textY);
                graphics.setPaint(new GradientPaint(centerX, gradientY1, conf.color2(), gradientX2, gradientY1,
                                                    conf.color1()));
                graphics.setClip(centerX, gradientY1, width / 2, height);
                graphics.drawString(string, textX, textY);
                break;

            case DIAGONAL1:
                graphics.setPaint(new GradientPaint(gradientX1, gradientY1, conf.color1(), gradientX2, gradientY2,
                                                    conf.color2()));
                graphics.drawString(string, textX, textY);
                break;

            case DIAGONAL2:
                graphics.setPaint(new GradientPaint(gradientX1, gradientY2, conf.color1(), gradientX2, gradientY1,
                                                    conf.color2()));
                graphics.drawString(string, textX, textY);
                break;

            case STAR:
                graphics.setPaint(new GradientPaint(gradientX1, gradientY1, conf.color1(), centerX, centerY,
                                                    conf.color2()));
                graphics.setClip(gradientX1, gradientY1, width / 2, height / 2);
                graphics.drawString(string, textX, textY);
                graphics.setPaint(new GradientPaint(centerX, centerY, conf.color2(), gradientX2, gradientY2,
                                                    conf.color1()));
                graphics.setClip(centerX, centerY, width / 2, height / 2);
                graphics.drawString(string, textX, textY);
                graphics.setPaint(new GradientPaint(gradientX1, gradientY2, conf.color1(), centerX, centerY,
                                                    conf.color2()));
                graphics.setClip(gradientX1, centerY, width / 2, height / 2);
                graphics.drawString(string, textX, textY);
                graphics.setPaint(new GradientPaint(centerX, centerY, conf.color2(), gradientX2, gradientY1,
                                                    conf.color1()));
                graphics.setClip(centerX, gradientY1, width / 2, height / 2);
                graphics.drawString(string, textX, textY);
                break;
        }

        graphics.setClip(oldClip);
    }

    protected static void fillHorizontalGradient(Graphics2D graphics, int x1, int y1, int x2, int y2, Color color1, Color color2) {
        graphics.setPaint(new GradientPaint(x1, y1, color1, x2, y1, color2));
        graphics.fillRect(x1, y1, x2 - x1, y2 - y1);
    }

    protected static void fillVerticalGradient(Graphics2D graphics, int x1, int y1, int x2, int y2, Color color1, Color color2) {
        graphics.setPaint(new GradientPaint(x1, y1, color1, x1, y2, color2));
        graphics.fillRect(x1, y1, x2 - x1, y2 - y1);
    }

    protected static void fillDiagonal1Gradient(Graphics2D graphics, int x1, int y1, int x2, int y2, Color color1, Color color2) {
        graphics.setPaint(new GradientPaint(x1, y1, color1, x2, y2, color2));
        graphics.fillRect(x1, y1, x2 - x1, y2 - y1);
    }

    protected static void fillDiagonal2Gradient(Graphics2D graphics, int x1, int y1, int x2, int y2, Color color1, Color color2) {
        graphics.setPaint(new GradientPaint(x1, y2, color1, x2, y1, color2));
        graphics.fillRect(x1, y1, x2 - x1, y2 - y1);
    }
}
