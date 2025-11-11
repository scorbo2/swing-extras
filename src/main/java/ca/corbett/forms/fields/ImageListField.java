package ca.corbett.forms.fields;

import ca.corbett.extras.image.ImageListPanel;

import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

/**
 * A FormField implementation that allows the user to select one or more images, and display
 * them in a scrollable form field. The field contains controls to allow the user to add
 * or remove items within the list. Double-clicking an image launches a preview window for it.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.5
 */
public class ImageListField extends FormField implements ChangeListener {

    private static final Logger log = Logger.getLogger(ImageListField.class.getName());

    private final JScrollPane scrollPane;
    private final ImageListPanel imageListPanel;
    private boolean shouldExpand;

    /**
     * Creates a new ImageListField with a default width sufficient to show
     * a single image. Use ImageListField(String, int) to pick a different
     * image size, OR use setShouldExpand(true) to consume as much
     * horizontal width as the containing FormPanel will allow.
     */
    public ImageListField(String label) {
        this(label, 1);
    }

    /**
     * Creates a new ImageListField with a starting width large enough to show
     * the given count of images. Alternatively, you can use setShouldExpand(true)
     * to allow this field to consume as much horizontal width as the containing
     * FormPanel allows.
     *
     * @param label The text for the field label
     * @param initialSize Will start with horizontal space sufficient to show this number of images.
     */
    public ImageListField(String label, int initialSize) {
        this(label, initialSize, ImageListPanel.DEFAULT_THUMB_SIZE);
    }

    /**
     * Creates a new ImageListField with a starting width large enough to show
     * the given count of images, and with the given square thumbnail pixel dimensions.
     * You can also optionally use setShouldExpand(true) to allow this panel to
     * consume as much horizontal space as the containing FormPanel allows.
     *
     * @param label The text for the field label
     * @param initialSize Will start with horizontal space sufficient to show this number of images.
     * @param thumbDimension The square thumbnail dimensions to use for each image.
     */
    public ImageListField(String label, int initialSize, int thumbDimension) {
        fieldLabel.setText(label);
        imageListPanel = new ImageListPanel(null);
        imageListPanel.setThumbnailSize(thumbDimension);
        imageListPanel.addChangeListener(this);
        scrollPane = new JScrollPane(imageListPanel);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(12);
        int panelWidth = thumbDimension * initialSize;
        scrollPane.setPreferredSize(new Dimension(panelWidth, thumbDimension + 20)); // + scrollbar
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        fieldComponent = scrollPane;
        shouldExpand = false; // arbitrary default
    }

    /**
     * Overridden here as we generally don't want to show a validation label on an image list.
     * Will return true only if one or more FieldValidators have been explicitly assigned.
     */
    @Override
    public boolean hasValidationLabel() {
        return !fieldValidators.isEmpty();
    }

    /**
     * Optionally make this FormField expand to fill the entire width of the parent
     * FormPanel. This overrides the "initialSize" that was given to the constructor. Defaults to false.
     */
    public ImageListField setShouldExpand(boolean expand) {
        shouldExpand = expand;
        return this;
    }

    /**
     * Enables or disables the underlying ImageListPanel. When disabled, features like drag
     * and drop to add images, or right click to remove images, are switched off.
     */
    @Override
    public FormField setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        imageListPanel.setReadOnly(!enabled);
        return this;
    }

    /**
     * Optionally restrict how many images this field will allow.
     * By default, this is Integer.MAX_VALUE.
     * The value must be at least 1 (otherwise what is the point).
     * <p>
     * <b>Warning:</b> If you pass a value that is less than the number
     * of images currently held in this field, the images at indexes above the
     * new max limit will be dropped.
     * </p>
     */
    public ImageListField setMaxImageCount(int maxCount) {
        imageListPanel.setMaxListSize(maxCount);
        return this;
    }

    /**
     * Returns the maximum number of images allowed in this field,
     * or Integer.MAX_VALUE if there is no limit.
     */
    public int getMaxImageCount() {
        return imageListPanel.getMaxListSize();
    }

    /**
     * Adjusts the desired thumbnail size for images in this field. Note that you shouldn't
     * bypass this method by doing imageListField.getImageListPanel().setThumbnailSize(),
     * because then this FormField is cut out of the loop and will not resize to the new
     * thumbnail dimensions. This method intercepts the request, adjusts this FormField
     * size, and then forwards the request to the underlying ImageListPanel.
     */
    public ImageListField setThumbnailSize(int size) {
        imageListPanel.setThumbnailSize(size);
        scrollPane.setPreferredSize(new Dimension(scrollPane.getPreferredSize().width, size));
        scrollPane.revalidate();
        scrollPane.repaint();
        return this;
    }

    /**
     * Programmatically adds an image to this image field, assuming the number of images
     * currently contained is less than the configured image limit.
     */
    public ImageListField addImage(BufferedImage image) {
        imageListPanel.addImage(image);
        return this;
    }

    /**
     * Programmatically adds an animated GIF image to this field, assuming the number
     * of images currently contained is less than the configured image limit.
     * Caller must supply a static image which will be scaled to use as the thumbnail.
     * A good choice is usually the first frame of the animation. This image will
     * be scaled as needed to fit the thumbnail panel.
     */
    public ImageListField addImage(BufferedImage thumbnail, ImageIcon imageIcon) {
        imageListPanel.addImage(thumbnail, imageIcon);
        return this;
    }

    /**
     * Returns the count of images currently contained in this field.
     */
    public int getImageCount() {
        return imageListPanel.getImageCount();
    }

    /**
     * Returns EITHER a BufferedImage for static image types like jpeg or png,
     * OR an ImageIcon if the image at the given index is an animated GIF.
     */
    public Object getImageAt(int index) {
        return imageListPanel.getImageAt(index);
    }

    /**
     * Removes the image at the specified index. Does nothing if the specified
     * index is out of range.
     */
    public ImageListField removeImageAt(int index) {
        imageListPanel.removeImage(index);
        return this;
    }

    /**
     * Removes all images from this field.
     */
    public ImageListField clear() {
        imageListPanel.clear();
        return this;
    }

    /**
     * Provides direct access to the underlying ImageListPanel, if needed.
     */
    public ImageListPanel getImageListPanel() {
        return imageListPanel;
    }

    @Override
    public boolean isMultiLine() {
        return true;
    }

    @Override
    public boolean shouldExpand() {
        return shouldExpand;
    }

    /**
     * We receive ChangeEvents from our underlying ImageListPanel as images are added or removed.
     * We can use that to send value changed events to our own listeners.
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        fireValueChangedEvent();
    }
}
