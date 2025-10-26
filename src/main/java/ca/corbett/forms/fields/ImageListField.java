package ca.corbett.forms.fields;

import ca.corbett.extras.image.ImageListPanel;
import ca.corbett.extras.image.ImageUtil;

import javax.swing.JScrollPane;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * A FormField implementation that allows the user to select one or more images, and display
 * them in a scrollable form field. The field contains controls to allow the user to add
 * or remove items within the list. Double-clicking an image launches a preview window for it.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.5
 */
public class ImageListField extends FormField {

    private static final Logger log = Logger.getLogger(ImageListField.class.getName());

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
     * You can also optionall use setShouldExpand(true) to allow this panel to
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
        JScrollPane scrollPane = new JScrollPane(imageListPanel);
        int panelWidth = thumbDimension * initialSize;
        scrollPane.setPreferredSize(new Dimension(panelWidth, thumbDimension + 20)); // + scrollbar
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        fieldComponent = scrollPane;
        shouldExpand = false; // arbitrary default
        enableDragAndDrop();
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
     * FormPanel. Defaults to false.
     */
    public ImageListField setShouldExpand(boolean expand) {
        shouldExpand = expand;
        return this;
    }

    public void addImage(BufferedImage image) {
        imageListPanel.addImage(image);
    }

    public int getImageCount() {
        return imageListPanel.getImageCount();
    }

    public void removeImageAt(int index) {
        imageListPanel.removeImage(index);
    }

    public void clear() {
        imageListPanel.clear();
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
     * Enables drag-and-drop of image files from the filesystem onto this panel.
     */
    public void enableDragAndDrop() {
        DropTarget dropTarget = new DropTarget(imageListPanel, new DropTargetAdapter() {
            @Override
            public void dragOver(DropTargetDragEvent dtde) {
                if (isImageFileDrag(dtde)) {
                    dtde.acceptDrag(DnDConstants.ACTION_COPY);
                }
                else {
                    dtde.rejectDrag();
                }
            }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                dtde.acceptDrop(DnDConstants.ACTION_COPY);

                Transferable transferable = dtde.getTransferable();
                if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    try {
                        @SuppressWarnings("unchecked")
                        List<File> files = (List<File>)transferable.getTransferData(DataFlavor.javaFileListFlavor);

                        for (File file : files) {
                            if (isImageFile(file)) {
                                try {
                                    BufferedImage image = ImageUtil.loadImage(file);
                                    if (image != null) {
                                        addImage(image);
                                    }
                                }
                                catch (IOException ioe) {
                                    log.warning("ImageListField: ignoring non-image: " + file.getAbsolutePath());
                                }
                            }
                        }

                        imageListPanel.revalidate();
                        imageListPanel.repaint();
                        dtde.dropComplete(true);
                    }
                    catch (UnsupportedFlavorException | IOException e) {
                        log.warning("ImageListField: drag-and-drop supports images only.");
                    }
                }
                else {
                    dtde.dropComplete(false);
                }
            }

            private boolean isImageFileDrag(DropTargetDragEvent dtde) {
                return dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor);// We'll validate actual files on drop
            }

            private boolean isImageFile(File file) {
                String name = file.getName().toLowerCase();
                return name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                        name.endsWith(".png") || name.endsWith(".gif") ||
                        name.endsWith(".bmp");
            }
        });

        imageListPanel.setDropTarget(dropTarget);
    }
}
