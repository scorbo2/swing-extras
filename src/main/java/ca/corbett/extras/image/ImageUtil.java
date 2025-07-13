package ca.corbett.extras.image;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.swing.ImageIcon;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Locale;

/**
 * Contains generic methods for dealing with images and image data.
 *
 * @author steve
 * @since 2012-09-01
 */
public final class ImageUtil {

    /**
     * JPEG compression quality to use. *
     */
    private static final float COMPRESSION_QUALITY = 0.95f;

    /**
     * Internal handle on the ImageWriter to use. *
     */
    private static ImageWriter imageWriter = null;

    /**
     * Internal handle on the (currently non editable) ImageWriteParam to use. *
     */
    private static ImageWriteParam imageWriteParam = null;

    /**
     * Utility classes have no public constructor. *
     */
    private ImageUtil() {
    }

    /**
     * Internal method to create our internal image utilities.
     */
    private static void createImageWriter() {
        Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpg");
        if (iter.hasNext()) {
            imageWriter = iter.next();
        }

        imageWriteParam = new JPEGImageWriteParam(Locale.getDefault());
        imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        imageWriteParam.setCompressionQuality(COMPRESSION_QUALITY);
    }

    /**
     * Loads and returns an ImageIcon from the specified file.
     * Generally, you should use loadImage() instead, as that returns a BufferedImage
     * that can be manipulated in memory and then written out using saveImage(), but
     * certain types of images (namely animated GIFs) are better in ImageIcon format,
     * as that allows proper rendering of them, for example in an ImagePanel.
     *
     * @param file The File from which to load the image icon.
     * @return an ImageIcon instance.
     */
    public static ImageIcon loadImageIcon(final File file) {
        return new ImageIcon(file.getAbsolutePath());
    }

    /**
     * Loads and returns an ImageIcon from the specified URL.
     * Generally, you should use loadImage() instead, as that returns a BufferedImage
     * that can be manipulated in memory and then written out using saveImage(), but
     * certain types of images (namely animated GIFs) are better in ImageIcon format,
     * as that allows proper rendering of them, for example in an ImagePanel.
     *
     * @param url The URL from which to load the image icon.
     * @return an ImageIcon instance.
     */
    public static ImageIcon loadImageIcon(final URL url) {
        return new ImageIcon(url);
    }

    /**
     * Loads a BufferedImage from the specified URL.
     *
     * @param url The URL of the image to load.
     * @return The BufferedImage contained in that URL.
     * @throws IOException If image not found.
     */
    public static BufferedImage loadImage(final URL url) throws IOException {
        return ImageIO.read(url);
    }

    /**
     * Loads a BufferedImage from the specified file.
     *
     * @param file The file in question. Can be any format supported by javax.imageio.ImageIO.
     * @return A BufferedImage containing the image data.
     * @throws IOException on read/write error.
     */
    public static BufferedImage loadImage(final File file) throws IOException {
        return ImageIO.read(file);
    }

    /**
     * Loads a BufferedImage from the specified input stream.
     *
     * @param inStream The input stream in question. Must contain an image in a format supported by javax.imageio.ImageIo.
     * @return A BufferedImage containing the image data.
     * @throws IOException on read/write error.
     */
    public static BufferedImage loadImage(final InputStream inStream) throws IOException {
        return ImageIO.read(inStream);
    }

    /**
     * Attempts to load the given resource for the given class.
     *
     * @param loadingClass The class to be used for loading the resource.
     * @param resourceName The fully qualified name of the resource to load. Must be a supported image format.
     * @return The image.
     * @throws IOException if something goes wrong.
     */
    public static BufferedImage loadFromResource(Class<?> loadingClass, String resourceName) throws IOException {
        try (InputStream inStream = loadingClass.getResourceAsStream(resourceName)) {
            return loadImage(inStream);
        }
    }

    /**
     * Attempts to load the given resource as an image, and also scales it to the given dimensions.
     * The image scale will be done with transparency. Scaling is skipped if the image dimensions
     * match the given dimensions.
     *
     * @param loadingClass The class to be used for loading the resource.
     * @param resourceName The fully qualified name of the resource to load. Must be a supported image format.
     * @param width        The desired width of the image.
     * @param height       The desired height of the image.
     * @return The requested image.
     * @throws IOException If something goes wrong.
     */
    public static BufferedImage loadFromResource(Class<?> loadingClass, String resourceName, int width, int height)
            throws IOException {
        BufferedImage image;
        try (InputStream inStream = loadingClass.getResourceAsStream(resourceName)) {
            image = loadImage(inStream);
            if (image.getWidth() != width || image.getHeight() != height) {
                image = generateThumbnailWithTransparency(image, width, height);
            }
        }

        return image;
    }

    /**
     * Saves the specified BufferedImage to the specified file. The default save format is
     * jpeg at 95% quality. Use the other saveImage() methods to specify a different save format
     * or compression quality.
     *
     * @param image The BufferedImage to save
     * @param file  The File to which to save.
     * @throws IOException on file access error.
     */
    public static void saveImage(final BufferedImage image, final File file) throws IOException {
        if (imageWriter == null) {
            createImageWriter();
        }

        saveImage(image, file, imageWriter, imageWriteParam);
    }

    /**
     * Saves the specified BufferedImage to the specified file in jpeg format with the specified
     * compression quality.
     *
     * @param image              The BufferedImage to save.
     * @param file               The File to which to save.
     * @param compressionQuality The jpeg compression quality value to use.
     * @throws IOException On file access error.
     */
    public static void saveImage(final BufferedImage image, final File file, float compressionQuality)
            throws IOException {
        if (imageWriter == null) {
            createImageWriter();
        }

        ImageWriteParam writeParam = new JPEGImageWriteParam(Locale.getDefault());
        writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        writeParam.setCompressionQuality(compressionQuality);
        saveImage(image, file, imageWriter, writeParam);
    }

    /**
     * Saves the specified BufferedImage to the specified file in jpeg format with the specified
     * ImageWriteParam. You can use this to specify your own image write parameters. If you only
     * want to set the jpeg compression quality, use saveImage(BufferedImage,File,float) instead.
     *
     * @param image      The BufferedImage to save.
     * @param file       The File to which to save.
     * @param writeParam The ImageWriteParam to use.
     * @throws IOException On file access error.
     */
    public static void saveImage(final BufferedImage image, final File file, ImageWriteParam writeParam)
            throws IOException {
        if (imageWriter == null) {
            createImageWriter();
        }

        saveImage(image, file, imageWriter, writeParam);
    }

    /**
     * Saves the specified BufferedImage using the specified ImageWriter and ImageWriteParam.
     * This allows you to save in some format other than jpeg. For example:
     * ImageIO.getImageWritersByFormatName("png")
     *
     * @param image      The BufferedImage to save.
     * @param file       The File to which to save.
     * @param writer     The ImageWriter to use.
     * @param writeParam The ImageWriteParam to use.
     * @throws IOException On file access error.
     */
    public static void saveImage(final BufferedImage image, final File file, ImageWriter writer, ImageWriteParam writeParam)
            throws IOException {
        try (FileImageOutputStream os = new FileImageOutputStream(file)) {
            writer.setOutput(os);
            writer.write(null, new IIOImage(image, null, null), writeParam);
        }
    }

    /**
     * Converts the given image to a byte array, which can then be serialized.
     * Default format is jpeg at 95% compression quality. Use the overloaded serializeImage()
     * methods to specify a different format or compression quality.
     *
     * @param image The BufferedImage to serialize
     * @return a byte array representing the image in question.
     * @throws IOException if an error occurs during image serialization.
     */
    public static byte[] serializeImage(final BufferedImage image) throws IOException {
        if (imageWriter == null) {
            createImageWriter();
        }

        return serializeImage(image, imageWriter, imageWriteParam);

    }

    /**
     * Converts the given image to a byte array, which can then be serialized.
     * The image will be serialized in jpeg format with the specified compression quality.
     * For more control over the output, or to change the output format, use the overloaded
     * serializeImage methods instead.
     *
     * @param image              The BufferedImage to serialize.
     * @param compressionQuality The jpeg compression quality to use.
     * @return A byte array representing the image in question.
     * @throws IOException if an error occurs during serialization.
     */
    public static byte[] serializeImage(final BufferedImage image, final float compressionQuality) throws IOException {
        if (imageWriter == null) {
            createImageWriter();
        }

        ImageWriteParam writeParam = new JPEGImageWriteParam(Locale.getDefault());
        writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        writeParam.setCompressionQuality(compressionQuality);

        return serializeImage(image, imageWriter, writeParam);
    }

    /**
     * Converts the given image to a byte array, which can then be serialized.
     * The image will be serialized in jpeg format with the given ImageWriteParam.
     * Use the ImageWriteParam to control various aspects of the image generation.
     * If you just want to set the jpeg compression level, use serializeImage(BufferedImage, float)
     * instead.
     *
     * @param image      The BufferedImage to serialize.
     * @param writeParam The ImageWriteParam to use when generating the output image.
     * @return A byte array representing the image in question.
     * @throws IOException If an error occurs during serialization.
     */
    public static byte[] serializeImage(final BufferedImage image, final ImageWriteParam writeParam)
            throws IOException {
        if (imageWriter == null) {
            createImageWriter();
        }

        return serializeImage(image, imageWriter, writeParam);
    }

    /**
     * Converts the given image to a byte array, which can then be serialized.
     * Use the ImageWriter and ImageWriteParam parameters to control the output format
     * of the generated image if you don't like the default jpeg. For example:
     * ImageIO.getImageWritersByFormatName("png")
     *
     * @param image      The BufferedImage to save.
     * @param writer     The ImageWriter to use.
     * @param writeParam The ImageWriteParam to use.
     * @return A byte array representing the image in question.
     * @throws IOException If an error occurs during serialization.
     */
    public static byte[] serializeImage(final BufferedImage image, final ImageWriter writer, final ImageWriteParam writeParam)
            throws IOException {
        byte[] arr;
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            writer.setOutput(new MemoryCacheImageOutputStream(os));
            writer.write(null, new IIOImage(image, null, null), writeParam);
            arr = os.toByteArray();
        }
        return arr;
    }

    /**
     * Converts the given byte array (created via serializeImage) back into a BufferedImage.
     *
     * @param arr A byte array generated by one of the serializeImage methods.
     * @return A BufferedImage representing the image data, or null if the input array was null.
     * @throws IOException on access error.
     */
    public static BufferedImage deserializeImage(final byte[] arr) throws IOException {
        if (arr == null) {
            return null;
        }
        ByteArrayInputStream is = new ByteArrayInputStream(arr);
        return ImageIO.read(is);
    }

    /**
     * Generates an image thumbnail for the given image file using the given dimensions.
     * Note that the image will be resized proportionally so that it fits inside the max of the
     * specified width and height. For example, an 800x600 image will be shrunk to 150x112.
     *
     * @param image  The image file in question. Can be any format supported by javax.imageio.ImageIO.
     * @param width  The desired max width of the thumbnail.
     * @param height The desired max height of the thumbnail.
     * @return A BufferedImage containing the thumbnail.
     * @throws IOException on input/output error.
     */
    public static BufferedImage generateThumbnail(final File image,
                                                  final int width,
                                                  final int height) throws IOException {
        BufferedImage sourceImage = ImageIO.read(image);
        return generateThumbnail(sourceImage, width, height);
    }

    /**
     * Generates an image thumbnail for the given image using the given dimensions.
     * The image will be resized proportionally to fit into the specified width and height.
     *
     * @param sourceImage The image in question
     * @param width       The desired max width of the thumbnail
     * @param height      The desired max height of the thumbnail
     * @return A BufferedImage representing the thumbnail
     */
    public static BufferedImage generateThumbnail(final BufferedImage sourceImage,
                                                  final int width,
                                                  final int height) {
        int srcWidth = sourceImage.getWidth();
        int srcHeight = sourceImage.getHeight();
        float scaleFactor = (srcWidth > srcHeight)
                ? (float)width / srcWidth
                : (float)height / srcHeight;
        int newWidth = (int)(srcWidth * scaleFactor);
        int newHeight = (int)(srcHeight * scaleFactor);

        // safeguard... this can happen with really wonky input image dimensions (eg. 9600x80).
        //   And yeah, I found that one out the hard way by generating really wide waveform images.
        newWidth = Math.max(newWidth, 1);
        newHeight = Math.max(newHeight, 1);

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight,
                                                       BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resizedImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                  RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                                  RenderingHints.VALUE_RENDER_QUALITY);
        graphics.drawImage(sourceImage, 0, 0, newWidth, newHeight, null);
        graphics.dispose();
        return resizedImage;
    }

    /**
     * Generates an image thumbnail for the given image file using the given dimensions.
     * The resulting BufferedImage will be rendered with an alpha channel to preserve transparency.
     * Note that the image will be resized proportionally so that it fits inside the max of the
     * specified width and height. For example, an 800x600 image will be shrunk to 150x112.
     *
     * @param image  The image file in question. Can be any format supported by javax.imageio.ImageIO.
     * @param width  The desired max width of the thumbnail.
     * @param height The desired max height of the thumbnail.
     * @return A BufferedImage containing the thumbnail.
     * @throws IOException on input/output error.
     */
    public static BufferedImage generateThumbnailWithTransparency(final File image,
                                                                  final int width,
                                                                  final int height) throws IOException {
        BufferedImage sourceImage = ImageIO.read(image);
        return generateThumbnailWithTransparency(sourceImage, width, height);
    }

    /**
     * Generates an image thumbnail for the given image using the given dimensions.
     * The resulting BufferedImage will be rendered with an alpha channel to preserve transparency.
     * The image will be resized proportionally to fit into the specified width and height.
     *
     * @param sourceImage The image in question
     * @param width       The desired max width of the thumbnail
     * @param height      The desired max height of the thumbnail
     * @return A BufferedImage representing the thumbnail
     */
    public static BufferedImage generateThumbnailWithTransparency(final BufferedImage sourceImage,
                                                                  final int width,
                                                                  final int height) {
        int srcWidth = sourceImage.getWidth();
        int srcHeight = sourceImage.getHeight();
        float scaleFactor = (srcWidth > srcHeight)
                ? (float)width / srcWidth
                : (float)height / srcHeight;
        int newWidth = (int)(srcWidth * scaleFactor);
        int newHeight = (int)(srcHeight * scaleFactor);

        // safeguard... this can happen with really wonky input image dimensions (eg. 9600x80).
        //   And yeah, I found that one out the hard way by generating really wide waveform images.
        newWidth = Math.max(newWidth, 1);
        newHeight = Math.max(newHeight, 1);

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight,
                                                       BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = resizedImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                  RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                                  RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                                  RenderingHints.VALUE_RENDER_QUALITY);
        graphics.drawImage(sourceImage, 0, 0, newWidth, newHeight, null);
        graphics.dispose();
        return resizedImage;
    }

    /**
     * Scales an image up or down proportionally until it fits inside the square bounding area
     * specified by maxDimension. The image is scaled based on its largest dimension.
     * For example, a landscape image will be scaled so that its with matches maxDimension.
     * A portrait image will be scaled so that its height matches maxDimension.
     * A square image will be scaled until both width and height equals maxDimension.
     *
     * @param image        The image to scale.
     * @param maxDimension The desired largest dimension of the scaled image.
     * @return The scaled image.
     */
    public static BufferedImage scaleImageToFitSquareBounds(BufferedImage image, int maxDimension) {
        int originalWidth = image.getWidth();
        int originalHeight = image.getHeight();

        // Calculate scaling factor based on the larger dimension
        double scaleFactor;
        if (originalWidth > originalHeight) {
            // Landscape - scale based on width
            scaleFactor = (double)maxDimension / originalWidth;
        }
        else {
            // Portrait (or square) - scale based on height
            scaleFactor = (double)maxDimension / originalHeight;
        }

        // Calculate new dimensions
        int newWidth = (int)Math.round(originalWidth * scaleFactor);
        int newHeight = (int)Math.round(originalHeight * scaleFactor);

        // Create the scaled image
        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaledImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(image, 0, 0, newWidth, newHeight, null);
        g.dispose();

        return scaledImage;
    }
}
