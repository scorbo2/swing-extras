package ca.corbett.updates;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.security.PublicKey;

/**
 * Invoked as needed by UpdateManager, to turn async download operations into effectively synchronous
 * operations in a UI, with a progress bar. The idea is that callers should just be able to
 * invoke UpdateManager.retrieveX, where X is whatever type of resource they want to download, and
 * UpdateManager will create and show this modal dialog to do the async work and error handling.
 * The internal callbacks here will then supply the finished product back to UpdateManager
 * <p>
 *     TODO fill in this javadoc carefully, it needs to be clear what this class is for.
 *     TODO maybe conceal the UpdateManagerListener stuff in an inner class so it's not exposed in this class.
 *          The whole idea is to hide the inner workings after all
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.5
 */
public class UpdateManagerDialog extends JDialog implements UpdateManagerListener {

    private final UpdateManager updateManager;
    private boolean wasOkayed;

    public UpdateManagerDialog(Window owner, UpdateManager updateManager, String title) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        setSize(new Dimension(500, 500));
        setResizable(false);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        this.updateManager = updateManager;
        setLayout(new BorderLayout());
        add(buildContentPanel(), BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);
    }

    public boolean wasOkayed() {
        return wasOkayed;
    }

    private JPanel buildContentPanel() {
        return new JPanel();
    }

    private void buttonHandler(boolean okay) {
        wasOkayed = okay;
        // TODO if not okay, cancel anything in progress
        dispose();
    }

    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton button = new JButton("OK");
        button.setPreferredSize(new Dimension(90, 24));
        button.addActionListener(e -> buttonHandler(true));
        panel.add(button);
        button = new JButton("Cancel");
        button.setPreferredSize(new Dimension(90, 24));
        button.addActionListener(e -> buttonHandler(false));
        panel.add(button);
        return panel;
    }

    @Override
    public void versionManifestDownloaded(UpdateManager manager, URL sourceUrl, VersionManifest versionManifest) {

    }

    @Override
    public void publicKeyDownloaded(UpdateManager manager, URL sourceUrl, PublicKey publicKey) {

    }

    @Override
    public void screenshotDownloaded(UpdateManager manager, URL sourceUrl, BufferedImage screenshot) {

    }

    @Override
    public void jarFileDownloaded(UpdateManager manager, URL sourceUrl, File jarFile) {

    }

    @Override
    public void signatureFileDownloaded(UpdateManager manager, URL sourceUrl, File signatureFile) {

    }

    @Override
    public void downloadFailed(UpdateManager manager, URL requestedUrl, String errorMessage) {

    }
}
