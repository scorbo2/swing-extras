package ca.corbett.extras.progress;

import ca.corbett.extras.image.ImagePanel;
import ca.corbett.extras.image.ImagePanelConfig;
import ca.corbett.extras.image.LogoGenerator;
import ca.corbett.extras.image.LogoProperty;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

/**
 * Normally the SplashScreen that comes with AWT is good enough for an application. But if the
 * app has some costly process to go through before the UI is shown, it is useful to be able to
 * show a progress bar with the splash screen. This gives the user some idea of how startup is going,
 * and reassures them that the application is actually starting up and isn't frozen.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 1.6 (2022-05-10)
 */
public final class SplashProgressWindow extends JWindow {

    private static final int PROGRESS_BAR_HEIGHT = 20;

    private final int splashWidth;
    private final int splashHeight;
    private final BufferedImage splashImage;
    private final Color fgColor;
    private final Color bgColor;
    private JProgressBar progressBar;

    /**
     * If you don't have an application logo image at the ready, use this constructor to
     * dynamically generate one on the fly (using LogoGenerator). You must supply enough
     * information about the app to make this possible.
     *
     * @param appName The name of the application.
     * @param config  The LogoConfig instance containing cosmetic parameters for the logo image.
     */
    public SplashProgressWindow(String appName, LogoProperty config) {
        this(null, appName, config);
    }

    /**
     * Very generally, the SplashProgressWindow will never have a parent frame, as it
     * is intended to be used as a standalone splash screen on application startup.
     * But, for testing purposes (or perhaps for some custom scenario), this constructor
     * is offered as a way to set a parent frame for it.
     *
     * @param owner   The owning Frame (can be null for no owner).
     * @param appName The name of the application (used as logo text)
     * @param config  The LogoConfig instance containing cosmetic parameters for the logo image.
     */
    public SplashProgressWindow(Frame owner, String appName, LogoProperty config) {
        super(owner);
        this.splashWidth = config.getLogoWidth();
        this.splashHeight = config.getLogoHeight();
        this.fgColor = config.getTextColor();
        this.bgColor = config.getBgColor();
        this.splashImage = LogoGenerator.generateImage(appName, config);
        initializeWindow(owner);
    }

    /**
     * If you do have an application logo image at the ready, use this constructor to display
     * a splash screen based on the given image. You must still supply a background and foreground
     * color so the progress bar can be styled to match the logo image.
     *
     * @param fgColor     The foreground color for the progress bar.
     * @param bgColor     The background color for the progress bar.
     * @param splashImage The application logo image. Window dimensions will be based on this image's
     *                    dimensions.
     */
    public SplashProgressWindow(Color fgColor, Color bgColor, BufferedImage splashImage) {
        this(null, fgColor, bgColor, splashImage);
    }

    /**
     * Very generally, the SplashProgressWindow will never have a parent frame, as it
     * is intended to be used as a standalone splash screen on application startup.
     * But, for testing purposes (or perhaps for some custom scenario), this constructor
     * is offered as a way to set a parent frame for it.
     *
     * @param fgColor     The foreground color for the progress bar.
     * @param bgColor     The background color for the progress bar.
     * @param splashImage The application logo image. Window dimensions will be based on this image's
     *                    dimensions.
     */
    public SplashProgressWindow(Frame owner, Color fgColor, Color bgColor, BufferedImage splashImage) {
        super(owner);
        this.splashImage = splashImage;
        this.fgColor = fgColor;
        this.bgColor = bgColor;
        this.splashWidth = splashImage.getWidth();
        this.splashHeight = splashImage.getHeight();
        initializeWindow(owner);
    }

    /**
     * Maybe you don't have a lengthy operation to perform on startup, but you still want to use
     * the SplashProgressWindow to make it look like you're doing something. This method will
     * show the window and start a dummy worker thread to simulate some activity, based on the
     * desired number of progress steps and the given delay between steps. When the "work" completes,
     * the splash window will be disposed automatically.
     * <p>
     * This method returns immediately,
     * while the "work" is still in progress. If you want to be notified when the splash window
     * has finished, you can either do the math based on steps*stepDelay or instead invoke
     * showFakeProgress(int,int,AbstractAction).</p>
     *
     * @param steps     How many steps of progress to fake.
     * @param stepDelay The desired delay, in milliseconds, between each work step.
     */
    public void showFakeProgress(int steps, int stepDelay) {
        showFakeProgress(steps, stepDelay, null);
    }

    /**
     * Executes the given worker thread and listens to it for progress updates.
     * Auto-wires the progress bar on this window and updates it as the work progresses.
     * When the work completes, disposes this window. Progress errors are simply ignored here.
     * You can add your own MultiProgressAdapter to the worker before passing it in here
     * if you wish to respond to errors.
     *
     * @param worker Any SimpleProgressWorker implementation.
     */
    public void runWorker(SimpleProgressWorker worker) {
        worker.addProgressListener(new BuiltInProgressListener(this));
        setVisible(true);
        new Thread(worker).start();
    }

    /**
     * Maybe you don't have a lengthy operation to perform on startup, but you still want to use
     * the SplashProgressWindow to make it look like you're doing something. This method will
     * show the window and start a dummy worker thread to simulate some activity, based on the
     * desired number of progress steps and the given delay between steps. When the "work" completes,
     * the splash window will be disposed automatically.
     * <p>
     * The AbstractAction that you supply
     * will be triggered when the "work" is complete.</p>
     *
     * @param steps     How many steps of progress to fake.
     * @param stepDelay The desired delay, in milliseconds, between each work step.
     * @param action    An AbstractAction to be fired when the fake work is done.
     */
    public void showFakeProgress(int steps, int stepDelay, AbstractAction action) {
        runWorker(new FakeWorker(steps, stepDelay, action, this));
    }

    /**
     * Configures the progress bar for the given number of work steps. You must call this
     * before showing the window.
     *
     * @param totalStepCount The total number of steps for the progress bar.
     */
    public void initializeProgressBar(int totalStepCount) {
        progressBar.setModel(new DefaultBoundedRangeModel(0, 0, 0, totalStepCount));
    }

    /**
     * Updates the progress bar to the given step number. Make sure you invoke
     * initializeProgressBar() first.
     *
     * @param value The current progress value.
     */
    public void setProgress(int value) {
        progressBar.setValue(value);
    }

    private void initializeWindow(Frame owner) {
        setLayout(new BorderLayout());
        setSize(new Dimension(splashWidth, splashHeight));
        if (owner == null) {
            Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
            int windowHeight = splashHeight + PROGRESS_BAR_HEIGHT;
            setBounds(center.x - splashWidth / 2, center.y - windowHeight / 2, splashWidth, windowHeight);
        }
        else {
            setLocationRelativeTo(owner);
        }

        ImagePanelConfig ipc = ImagePanelConfig.createSimpleReadOnlyProperties();
        ipc.setDisplayMode(ImagePanelConfig.DisplayMode.NONE);
        ImagePanel imagePanel = new ImagePanel(splashImage, ipc);
        imagePanel.setPreferredSize(new Dimension(splashWidth, splashHeight));
        add(imagePanel, BorderLayout.CENTER);

        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(1, PROGRESS_BAR_HEIGHT));
        progressBar.setStringPainted(true);
        progressBar.setBackground(bgColor);
        progressBar.setForeground(fgColor);
        add(progressBar, BorderLayout.SOUTH);
    }

    /**
     * A simple worker thread to perform some "fake" work to simulate slow progress.
     */
    private static class FakeWorker extends SimpleProgressWorker {
        final int steps;
        final int stepDelay;
        final Action action;
        final SplashProgressWindow thisWindow;

        /**
         * Creates a new {@code FakeWorker} that simulates progress over a fixed number of steps.
         *
         * @param steps      the total number of progress steps to simulate
         * @param stepDelay  the delay in milliseconds to wait between each simulated step
         * @param action     an optional action to invoke when all steps have completed; may be {@code null}
         * @param window     the {@link SplashProgressWindow} associated with this worker
         */
        public FakeWorker(int steps, int stepDelay, Action action, SplashProgressWindow window) {
            this.steps = steps;
            this.stepDelay = stepDelay;
            this.action = action;
            this.thisWindow = window;
        }

        @Override
        public void run() {
            fireProgressBegins(steps);
            for (int i = 0; i < steps; i++) {
                fireProgressUpdate(i, "");
                try {
                    Thread.sleep(stepDelay);
                }
                catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
            fireProgressComplete();
            if (action != null) {
                action.actionPerformed(new ActionEvent(thisWindow, 0, "Complete"));
            }
        }
    }

    /**
     * Used internally to monitor progress and update our progress bar.
     * Will dispose the given SplashProgressWindow when completed or canceled.
     */
    private static class BuiltInProgressListener extends SimpleProgressAdapter {

        private final SplashProgressWindow thisWindow;

        public BuiltInProgressListener(SplashProgressWindow window) {
            thisWindow = window;
        }

        @Override
        public void progressBegins(int stepCount) {
            SwingUtilities.invokeLater(() -> thisWindow.initializeProgressBar(stepCount));
        }

        @Override
        public boolean progressUpdate(int currentStep, String message) {
            SwingUtilities.invokeLater(() -> thisWindow.setProgress(currentStep + 1));
            return true;
        }

        @Override
        public void progressComplete() {
            SwingUtilities.invokeLater(thisWindow::dispose);
        }

        @Override
        public void progressCanceled() {
            SwingUtilities.invokeLater(thisWindow::dispose);
        }
    }
}
