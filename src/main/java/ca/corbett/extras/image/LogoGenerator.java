package ca.corbett.extras.image;

import ca.corbett.extras.gradient.GradientUtil;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Utility class to generate small, text-based images that can serve as a
 * very quick logo type of thing. This was moved into sc-util from the
 * standalone LogoGenerator app as it makes more sense to live here.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public final class LogoGenerator {

    /**
     * Private constructor to avoid instantiation.
     */
    private LogoGenerator() {
    }

    /**
     * Generates an image using the given text and LogoConfig instance.
     *
     * @param text   The text to render.
     * @param preset The LogoConfig instance containing config settings to use.
     * @return A BufferedImage containing the rendered final product.
     */
    public static BufferedImage generateImage(String text, LogoConfig preset) {
        int width = preset.getLogoWidth();
        int height = preset.getLogoHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();

        if (preset.getBgColorType() == LogoConfig.ColorType.SOLID) {
            graphics.setColor(preset.getBgColor());
            graphics.fillRect(0, 0, width, height);
        }
        else {
            GradientUtil.fill(preset.getBgGradient(), graphics, 0, 0, width, height);
        }

        if (preset.hasBorder()) {
            if (preset.getBorderColorType() == LogoConfig.ColorType.SOLID) {
                graphics.setColor(preset.getBorderColor());
                for (int offset = 0; offset < preset.getBorderWidth(); offset++) {
                    graphics.drawRect(offset, offset, width - (offset * 2) - 1, height - (offset * 2) - 1);
                }
            }
            else {
                for (int offset = 0; offset < preset.getBorderWidth(); offset++) {
                    GradientUtil.drawRect(preset.getBorderGradient(), graphics, offset, offset,
                                          width - (offset * 2) - 1, height - (offset * 2) - 1);
                }
            }
        }

        int fontPointSize = preset.getFont().getSize();
        Font font = preset.getFont().deriveFont((float)fontPointSize);
        graphics.setFont(font);
        int textWidth = (int)graphics.getFontMetrics().stringWidth(text);
        int textHeight = (int)graphics.getFontMetrics().getLineMetrics(text, graphics).getHeight();

        // Compute size automatically if requested:
        if (preset.isAutoSize()) {
            int fontSize = 150;
            int fontDecrement = 1;

            // It's possible we were given very large dimensions.
            // If so, increase our starting font size and our search decrement accordingly.
            // Note: we could just start with a font size of a million and work our way down, but
            // in the interest of performance it's better if we don't have to do that.
            int tempWidth = width;
            while (tempWidth > 1500) {
                fontSize += 100;
                fontDecrement++;
                tempWidth -= 1000;
            }

            // Now search for a size that fits, assuming that we're starting with a size that's
            // too large, and decrementing it down until we find the largest size that fits.
            boolean fits = false;
            while (!fits) {
                font = new Font(font.getName(), font.getStyle(), fontSize);
                graphics.setFont(font);
                if (graphics.getFontMetrics().stringWidth(text) < (width * 0.9)
                        && graphics.getFontMetrics().getLineMetrics(text, graphics).getHeight() < (height * 0.9)) {
                    fits = true;
                }
                else {
                    fontSize -= fontDecrement;
                }
            }
            textWidth = graphics.getFontMetrics().stringWidth(text);
            textHeight = (int)graphics.getFontMetrics().getLineMetrics(text, graphics).getHeight();//.getAscent();
            //textHeight += (int)graphics.getFontMetrics().getLineMetrics(text, graphics).getDescent();
        }

        // It's possible the text is just too wide to fit in these dimensions. Not much
        // we can do here.
        if (textWidth > width) {
            Logger.getLogger(LogoGenerator.class.getName()).info("WARN: Text too large for image; overflowing.");
        }

        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                  RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        // Kludge alert: text height calculations seems wrong, have to divide by 4 to center vertically:
        // Kludge alert: and sometimes even that doesn't work... hence "yTweak"
        int textX = (width - textWidth) / 2;
        int textY = (height / 2) + (int)(textHeight / 4) + preset.getYTweak();
        if (preset.getTextColorType() == LogoConfig.ColorType.SOLID) {
            graphics.setColor(preset.getTextColor());
            graphics.drawString(text, textX, textY);
        }
        else {
            // You can set the gradient bounds to be larger than the text bounds... say, for example,
            // the entire size of the image. But if the image is very large, this washes out the
            // gradient and defeats the purpose. If you don't explicitly give bounds for the gradient,
            // then GradientUtil will calculate the text bounds and use that instead, which is
            // probably fine for LogoGenerator purposes.
            GradientUtil.drawString(preset.getTextGradient(), graphics, /*0, 0, width, height,*/ textX, textY, text);
        }
        graphics.dispose();

        return image;
    }

    /**
     * Generates an image using the given text and LogoConfig instance, and then saves it
     * to the given File. Output format is currently limited to jpeg regardless of
     * what file name/extension you pass in here.
     *
     * @param text       The text to render.
     * @param preset     The LogoConfig instance containing config settings to use.
     * @param outputFile The file to which the image will be saved. Must be writable.
     * @throws IOException if something goes wrong.
     */
    public static void generateAndSaveImage(String text, LogoConfig preset, File outputFile) throws IOException {
        BufferedImage image = LogoGenerator.generateImage(text, preset);

        ImageWriter imageWriter = null;
        Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpg");
        if (iter.hasNext()) {
            imageWriter = iter.next();
        }

        if (imageWriter == null) {
            throw new IOException("Can't find ImageWriter for jpg images.");
        }

        // kludge alert... note these were hard coded in ice
        ImageWriteParam imageWriteParam = new JPEGImageWriteParam(Locale.getDefault());
        imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        imageWriteParam.setCompressionQuality(0.98f);

        FileImageOutputStream os = new FileImageOutputStream(outputFile);
        imageWriter.setOutput(os);
        IIOImage outImage = new IIOImage(image, null, null);
        imageWriter.write(null, outImage, imageWriteParam);
    }

}
