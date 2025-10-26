package ca.corbett.extras.image;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * A scrollable panel that can show a list of images, each represented by a thumbnail
 * and an optional label to describe the image. The user can optionally add new images
 * to the list, remove them from the list, or double-click an image to view it in a
 * popup window.
 * <p>
 * <b>NOTE:</b> this component will grow horizontally without ever wrapping!
 * The intention is that it will be added to a JScrollPane to prevent the component
 * from taking up an unreasonable amount of space. It contains mouse listeners that
 * will allow clicking and dragging on any image panel to scroll left/right within the list,
 * if the ImageListPanel is added to a JScrollPane container.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.5
 */
public class ImageListPanel extends JPanel {

    private static final Logger log = Logger.getLogger(ImageListPanel.class.getName());

    public static final int DEFAULT_THUMB_SIZE = 100;
    public static final int MINIMUM_THUMB_SIZE = 25;
    public static final int MAXIMUM_THUMB_SIZE = 500;

    private final JFrame ownerFrame;
    private int thumbSize;

    private int startX;
    private boolean isReadOnly;
    private int maxListSize;

    /**
     * Creates a new, empty ImageListPanel.
     */
    public ImageListPanel(JFrame ownerFrame) {
        this.ownerFrame = ownerFrame;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        thumbSize = DEFAULT_THUMB_SIZE;
        isReadOnly = false;
        maxListSize = Integer.MAX_VALUE;
        enableDragAndDrop();
    }

    /**
     * Determines whether image addition and removal operations are permitted,
     * and also whether drag and drop from the file system is allowed.
     */
    public void setReadOnly(boolean readOnly) {
        if (isReadOnly == readOnly) {
            return; // already set.
        }

        isReadOnly = readOnly;
        if (isReadOnly) {
            setDropTarget(null);
        }
        else {
            enableDragAndDrop();
        }
    }

    /**
     * Returns whether this panel allows image addition and removal operations,
     * and also whether drag and drop from the file system is allowed.
     */
    public boolean isReadOnly() {
        return isReadOnly;
    }

    /**
     * Returns the configured maximum image count that this panel will allow.
     * Addition attempts beyond this count are discarded.
     */
    public int getMaxListSize() {
        return maxListSize;
    }

    /**
     * Puts a limit on the number of images that this panel will allow.
     * By default, this is Integer.MAX_VALUE.
     * <p>
     * <b>WARNING:</b> Passing a value that is less than the number
     * of images currently held in this panel will cause all images
     * at indexes higher than this count to be dropped.
     * </p>
     */
    public void setMaxListSize(int max) {
        if (max < 1) {
            max = 1; // reject stupid values
        }
        maxListSize = max;

        // Nuke any images beyond our new max:
        while (getImageCount() > maxListSize) {
            removeImage(maxListSize);
        }
    }

    /**
     * Sets a new pixel size for our image thumbnails. Note that our thumbnails will
     * always be scaled proportionally to fit a square bounding area, so the given
     * pixel value represents both the desired width and the height of each thumbnail.
     * This method will force a reload of the panel to force all current images
     * to redisplay at the new thumb size.
     */
    public void setThumbnailSize(int newSize) {
        // Ignore no-op requests:
        if (thumbSize == newSize) {
            return;
        }

        // Keep it within a reasonable range:
        thumbSize = Math.max(MINIMUM_THUMB_SIZE, newSize);
        thumbSize = Math.min(MAXIMUM_THUMB_SIZE, thumbSize);

        // If we have no images, we're done:
        if (getComponentCount() == 0) {
            return;
        }

        // Re-do our layout by removing all old image panels and creating new ones at the right size:
        List<BufferedImage> images = new ArrayList<>(getComponentCount());
        for (int i = 0; i < getComponentCount(); i++) {
            images.add(getImageAt(i));
        }
        removeAll();
        for (BufferedImage image : images) {
            addImage(image);
        }
        revalidate();
        repaint();
    }

    /**
     * Adds a new image to the list. The list displays images in the order
     * that they are added! Re-ordering the list dynamically is not yet possible.
     */
    public void addImage(BufferedImage image) {
        // Ignore garbage input:
        if (image == null) {
            return;
        }

        // Don't allow our list size to be exceeded:
        if (getImageCount() >= maxListSize) {
            return;
        }

        // Scale the image to our thumbnail size if needed:
        BufferedImage thumbnail = image;
        if (image.getWidth() > thumbSize || image.getHeight() > thumbSize) {
            thumbnail = ImageUtil.scaleImageToFitSquareBounds(image, thumbSize, true);
        }

        // Create an ImagePanel to represent this image:
        ImagePanel imagePanel = new ImagePanel(thumbnail, ImagePanelConfig.createSimpleReadOnlyProperties());
        imagePanel.setMinimumSize(new Dimension(thumbSize, thumbSize));
        imagePanel.setPreferredSize(new Dimension(thumbSize, thumbSize)); // TODO add some height for image label
        imagePanel.setExtraAttribute("originalImage", image);

        // Add our double-click handler to show the image in a preview window:
        imagePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startX = e.getXOnScreen();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showImage((BufferedImage)imagePanel.getExtraAttribute("originalImage"));
                }
            }
        });
        imagePanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                JScrollPane scrollPane = findParentScrollPane(imagePanel);
                if (scrollPane != null) {
                    int deltaX = startX - e.getXOnScreen();
                    JViewport viewport = scrollPane.getViewport();
                    Point viewPosition = viewport.getViewPosition();
                    viewPosition.translate(deltaX, 0);

                    // Clamp to valid range
                    viewPosition.x = Math.max(0, viewPosition.x);
                    viewPosition.x = Math.min(imagePanel.getParent().getWidth() - viewport.getWidth(), viewPosition.x);

                    viewport.setViewPosition(viewPosition);
                    startX = e.getXOnScreen();
                }
            }
        });

        add(imagePanel);
    }

    /**
     * Reports how many images are in the current list.
     */
    public int getImageCount() {
        return getComponentCount();
    }

    /**
     * Removes all images.
     */
    public void clear() {
        removeAll();
    }

    /**
     * Returns the image at the given index, or null if the given index is invalid.
     */
    public BufferedImage getImageAt(int index) {
        if (index < 0 || index >= getComponentCount()) {
            return null;
        }

        ImagePanel imagePanel = (ImagePanel)getComponent(index);
        return (BufferedImage)imagePanel.getExtraAttribute("originalImage");
    }

    /**
     * Remove an image by index. If the given index is invalid, this call does nothing.
     */
    public void removeImage(int index) {
        if (index < 0 || index >= getComponentCount()) {
            return;
        }

        remove(index);
    }

    /**
     * Launches a popup window to show the image at full size.
     */
    private void showImage(BufferedImage image) {
        image.flush();
        JDialog dialog = new JDialog(ownerFrame, "Image preview", false);
        dialog.setSize(new Dimension(600, 400));
        dialog.setLocationRelativeTo(ownerFrame);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout());
        dialog.add(new ImagePanel(image, ImagePanelConfig.createSimpleReadOnlyProperties()), BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    /**
     * Traverses up the component hierarchy to find the parent JScrollPane, if any.
     */
    private JScrollPane findParentScrollPane(Component component) {
        Component parent = component.getParent();
        while (parent != null) {
            if (parent instanceof JScrollPane) {
                return (JScrollPane)parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    /**
     * Enables drag-and-drop of image files from the filesystem onto this panel.
     */
    public void enableDragAndDrop() {
        DropTarget dropTarget = new DropTarget(this, new DropTargetAdapter() {
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

                        revalidate();
                        repaint();
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

        setDropTarget(dropTarget);
    }
}
