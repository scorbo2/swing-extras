package ca.corbett.extras.progress;

import ca.corbett.extras.StringFormatter;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.PanelField;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Replacement for ProgressMonitor, which is a bit limiting.
 * Specifically, this dialog can show "major" and "minor" progress
 * bars simultaneously, and increment them each as a long-running thread
 * progresses. This allows you to show more detailed progress, particularly for
 * executing worker threads involving a list of long-running work items. For example,
 * iterating over a list of directories and performing some time-intensive operation within
 * each directory. In this hypothetical example, it would be helpful to the user to show
 * a "major" progress bar showing the progress through the list of directories (for example,
 * "directory 2 of 12"), while also showing a "minor" progress bar showing the progress
 * within the current directory ("file 17 of 111").
 * <p>
 * <b>USAGE:</b></p>
 * <p>
 * Create an instance of MultiProgressDialog and pass it into a worker thread (or have the
 * worker thread create one). The worker thread should invoke setMajorProgressBounds() to
 * set the min and max values of the major progress bar. The worker thread can then show the
 * dialog and begin work. As soon as the bounds of the minor progress bar are known, the worker
 * thread can invoke setMinorProgressBounds(). Periodically, the worker thread should invoke
 * setMajorProgress() and setMinorProgress() to let the user know what's going on, and also
 * check on the isCanceled() method to learn if the user has clicked the cancel button.
 * When the worker thread is finished, it can dispose the dialog.
 * </p>
 * <p>
 * MultiProgressDialog instances can be re-used, but be sure to invoke resetProgress()
 * in between each usage to reset the progress bars (you will also then need to invoke
 * setMajorProgressBounds() and setMinorProgressBounds() as soon as they are known, as above).
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2022-04-15
 */
public final class MultiProgressDialog extends JDialog {

    /**
     * This will yield: "[%currentStep of %totalSteps] %message"
     * <p>
     * You can go back to the pre-2.7 format with "%m (%s of %t)", which will
     * yield "%message (%currentStep of %totalSteps)".
     * </p>
     * <p>
     * It's probably best to keep the "x of y" part on the left side of the string.
     * The string length of "%message" can vary widely and rapidly as progress
     * continues, which can make it hard to read the step counts if they're bouncing
     * around at the end of the string.
     * </p>
     * <p>
     * But rather than hard-coding a "fix" for that problem, it is now configurable!
     * </p>
     */
    public static final String DEFAULT_PROGRESS_FORMAT = "[%s of %t] %m";

    private static final int LABEL_LENGTH_CUTOFF = 50;
    private LabelField majorProgressLabel;
    private LabelField minorProgressLabel;
    private JProgressBar majorProgressBar;
    private JProgressBar minorProgressBar;
    private long initialShowDelayMS;
    private boolean isCanceled;
    private String formatString = DEFAULT_PROGRESS_FORMAT;

    /**
     * Creates a new, blank MultiProgressDialog with the specified owner window
     * and window title. Closing the progress dialog will act the same
     * as though the cancel button had been clicked. Call isCanceled()
     * at any time to see if the operation should continue or not (that is,
     * if the user has clicked the cancel button on the progress dialog).
     *
     * @param owner The Window that will own this MultiProgressDialog
     * @param title The window title
     */
    public MultiProgressDialog(Window owner, String title) {
        super(owner, title);
        initComponents();
        resetProgress();
        setLocationRelativeTo(owner);
        initialShowDelayMS = 0;
    }

    /**
     * Resets to a blank, default state. Also resets the isCanceled flag.
     */
    public void resetProgress() {
        majorProgressLabel.setText("");
        minorProgressLabel.setText("");
        majorProgressBar.setValue(0);
        minorProgressBar.setValue(0);
        setMajorProgressBounds(0, 100);
        setMinorProgressBounds(0, 100);
        isCanceled = false;
    }

    /**
     * Returns the optional delay time, in milliseconds, between the time when the work
     * begins and the time when the progress dialog is made visible. The default value
     * is 0, meaning the dialog will appear as soon as the work begins.
     */
    public long getInitialShowDelayMS() {
        return initialShowDelayMS;
    }

    /**
     * Sets an optional delay time, in milliseconds, between the time when the work starts
     * and the time when the dialog will make itself visible. The default value is 0, which
     * means the dialog will appear as soon as the work begins. But you can set this to
     * avoid showing a dialog for very short-running tasks. For example, setting this value
     * to 2000 will prevent the dialog from showing if the work to be executed completes in
     * less than 2 seconds.
     */
    public void setInitialShowDelayMS(long initialShowDelayMS) {
        this.initialShowDelayMS = initialShowDelayMS;
    }

    /**
     * Executes the given Runnable and auto-wires all progress events to this dialog.
     * This is a very easy way to simply implement some runnable that can fire progress
     * events, and pass it to this method to handle the UI aspect automatically.
     * Progress errors are simply ignored here. You can add your own MultiProgressAdapter
     * to the worker before passing it in here if you wish to respond to errors.
     * This method will show both major and minor progress bars.
     * It is intended for complex tasks that have subtasks. Use the overloaded
     * runWorker that accepts a SimpleProgressWorker if you only need a single
     * progress bar.
     *
     * @param worker               Any MultiProgressWorker implementation that can perform some task.
     * @param disposeWhenComplete, if true, will dispose() this dialog when complete. Otherwise,
     *                             hides the dialog.
     */
    public void runWorker(final MultiProgressWorker worker, final boolean disposeWhenComplete) {
        minorProgressLabel.setVisible(true);
        minorProgressBar.setVisible(true);
        setSize(new Dimension(500, 210));
        // Use a priority listener to make sure we get notified first...
        // otherwise, the timer on the dialog may force it visible even if some other handler is showing a popup
        // Our listener will kill the timer, which avoids that problem as long as our listener is invoked first.
        worker.addPriorityProgressListener(new MultiProgressHandler(this, initialShowDelayMS, disposeWhenComplete));
        new Thread(worker).start();
    }

    /**
     * Executes the given Runnable and auto-wires all progress events to this dialog.
     * This is a very easy way to simply implement some runnable that can fire progress
     * events, and pass it to this method to handle the UI aspect automatically.
     * Progress errors are simply ignored here. You can add your own SimpleProgressAdapter
     * to the worker before passing it in here if you wish to respond to errors.
     * This method will hide the "minor" progress bar and only show a single progress bar.
     * It is intended for simple tasks. Use the overloaded runWorker that accepts
     * a MultiProgressWorker to show both major and minor progress.
     *
     * @param worker               Any SimpleProgressWorker implementation that can perform some task.
     * @param disposeWhenComplete, if true, will dispose() this dialog when complete. Otherwise,
     *                             hides the dialog.
     */
    public void runWorker(final SimpleProgressWorker worker, final boolean disposeWhenComplete) {
        minorProgressLabel.setVisible(false);
        minorProgressBar.setVisible(false);
        setSize(new Dimension(500, 160));
        // Use a priority listener to make sure we get notified first...
        // otherwise, the timer on the dialog may force it visible even if some other handler is showing a popup
        // Our listener will kill the timer, which avoids that problem as long as our listener is invoked first.
        worker.addPriorityProgressListener(new SimpleProgressHandler(this, initialShowDelayMS, disposeWhenComplete));
        new Thread(worker).start();
    }

    /**
     * Sets the minimum and maximum bounds of the major progress bar.This will also set the major
     * progress value to "minimum".
     *
     * @param minimum The minimum value of the major progress bar (typically 0).
     * @param maximum The maximum value of the major progress bar.
     */
    public void setMajorProgressBounds(int minimum, int maximum) {
        majorProgressBar.setMinimum(minimum);
        majorProgressBar.setMaximum(maximum);
        setMajorProgress(minimum);
    }

    /**
     * Sets the minimum and maximum bounds of the minor progress bar.This will also set the minor
     * progress value to "minimum".
     *
     * @param minimum The minimum value of the minor progress bar (typically 0).
     * @param maximum The maximum value of the minor progress bar.
     */
    public void setMinorProgressBounds(int minimum, int maximum) {
        minorProgressBar.setMinimum(minimum);
        minorProgressBar.setMaximum(maximum);
        setMinorProgress(minimum);
    }

    /**
     * Sets the progress value for the major progress bar without updating the progress label.
     * See also setMajorProgress(int,String)
     *
     * @param progress The new value for the major progress bar.
     */
    public void setMajorProgress(int progress) {
        setMajorProgress(progress, null);
    }

    /**
     * Sets the progress value for the major progress bar and updates
     * the major progress label with the given message.
     *
     * @param progress The new value for the major progress bar.
     * @param message  The new message to show in the major progress label (ignored if null).
     */
    public void setMajorProgress(int progress, String message) {
        if (message != null) {
            majorProgressLabel.setText(truncString(message));
        }
        majorProgressBar.setValue(progress);
    }

    /**
     * Sets the progress value for the minor progress bar without updating the progress label.
     * See also setMajorProgress(int,String)
     *
     * @param progress The new value for the minor progress bar.
     */
    public void setMinorProgress(int progress) {
        setMinorProgress(progress, null);
    }

    /**
     * Sets the progress value for the minor progress bar and updates
     * the major progress label with the given message.
     *
     * @param progress The new value for the minor progress bar.
     * @param message  The new message to show in the minor progress label (ignored if null).
     */
    public void setMinorProgress(int progress, String message) {
        if (message != null) {
            minorProgressLabel.setText(truncString(message));
        }
        minorProgressBar.setValue(progress);
    }

    /**
     * Indicates whether the cancel button has been clicked or not.
     *
     * @return True if the cancel button has been clicked.
     */
    public boolean isCanceled() {
        return isCanceled;
    }

    /**
     * Truncates the given string to a size of LABEL_LENGTH_CUTOFF if necessary.
     *
     * @param s The string to truncate.
     * @return The truncated string, or null if input was null.
     */
    private String truncString(String s) {
        if (s == null) {
            return null;
        }
        return (s.length() > LABEL_LENGTH_CUTOFF)
                ? s.substring(0, LABEL_LENGTH_CUTOFF) + "..."
                : s;
    }

    /**
     * Returns the current format string that will be used to display progress messages.
     * See the DEFAULT_PROGRESS_FORMAT constant for details.
     */
    public String getFormatString() {
        return formatString;
    }

    /**
     * Optionally change the format string that will be used to display progress messages.
     * See the DEFAULT_PROGRESS_FORMAT constant for details.
     */
    public void setFormatString(String formatString) {
        this.formatString = formatString;
    }

    /**
     * Invoked internally to lay out the form.
     */
    private void initComponents() {
        setMinimumSize(new Dimension(500, 160));
        setSize(new Dimension(500, 210));
        setResizable(false);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        final JButton cancelButton = new JButton("Cancel");
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                isCanceled = true;
                cancelButton.setEnabled(false);
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    isCanceled = true;
                    cancelButton.setEnabled(false);
                }
            }
        });

        setLayout(new BorderLayout());
        List<FormField> formFields = new ArrayList<>();

        majorProgressLabel = new LabelField("");
        majorProgressLabel.getMargins().setAll(0).setLeft(12).setInternalSpacing(2);
        formFields.add(majorProgressLabel);

        majorProgressBar = new JProgressBar();
        majorProgressBar.setPreferredSize(new Dimension(450, 20));
        majorProgressBar.setStringPainted(true);
        PanelField panelField = new PanelField();
        panelField.getMargins().setAll(0);
        panelField.getPanel().setLayout(new BorderLayout());
        panelField.getPanel().add(majorProgressBar, BorderLayout.CENTER);
        formFields.add(panelField);

        minorProgressLabel = new LabelField("");
        minorProgressLabel.getMargins().setAll(0).setLeft(16).setTop(12).setInternalSpacing(2);
        formFields.add(minorProgressLabel);

        minorProgressBar = new JProgressBar();
        minorProgressBar.setStringPainted(true);
        minorProgressBar.setPreferredSize(new Dimension(450, 20));
        panelField = new PanelField();
        panelField.getMargins().setAll(0);
        panelField.getPanel().setLayout(new BorderLayout());
        panelField.getPanel().add(minorProgressBar, BorderLayout.CENTER);
        formFields.add(panelField);

        FormPanel formPanel = new FormPanel(Alignment.TOP_CENTER);
        formPanel.add(formFields);
        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        cancelButton.setPreferredSize(new Dimension(90, 23));
        cancelButton.setFocusable(false);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelButton.setEnabled(false);
                isCanceled = true;
            }
        });
    }

    /**
     * Used internally to wire up a MultiProgressWorker to the UI elements in this progress
     * dialog, so that we can show current progress.
     */
    private static class MultiProgressHandler extends MultiProgressAdapter {

        private final MultiProgressDialog progressDialog;
        private final long initialShowDelayMS;
        private final boolean disposeWhenComplete;
        private long startTimeMS;
        private int totalMajorSteps;
        private int totalMinorSteps;
        private boolean isFinished;

        public MultiProgressHandler(MultiProgressDialog ownerDialog, long initialShowDelay, boolean disposeWhenComplete) {
            this.progressDialog = ownerDialog;
            this.initialShowDelayMS = initialShowDelay;
            this.disposeWhenComplete = disposeWhenComplete;
        }

        private long getElapsedTime() {
            return System.currentTimeMillis() - startTimeMS;
        }

        private void showDialogIfNeeded() {
            if (progressDialog.isVisible() || isFinished) {
                return;
            }
            if (initialShowDelayMS <= 0 || getElapsedTime() > initialShowDelayMS) {
                progressDialog.setVisible(true);
            }
        }

        @Override
        public void progressBegins(int totalMajorSteps) {
            this.startTimeMS = System.currentTimeMillis();
            this.totalMajorSteps = totalMajorSteps;
            SwingUtilities.invokeLater(() -> {
                progressDialog.setMajorProgressBounds(0, totalMajorSteps);
                showDialogIfNeeded();
            });
            if (initialShowDelayMS > 0) {
                // We'll check this on each progress update, but if there's a very
                // long-running step, we still want to show the progress dialog after our configured delay:
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        SwingUtilities.invokeLater(() -> showDialogIfNeeded());
                    }
                }, initialShowDelayMS);
            }
        }

        @Override
        public boolean majorProgressUpdate(int majorStep, int totalMinorSteps, String message) {
            this.totalMinorSteps = totalMinorSteps;
            String formatString = progressDialog.getFormatString();
            final String formatted = StringFormatter.format(formatString, key -> switch (key) {
                case 'm' -> message;
                case 's' -> Integer.toString(majorStep + 1);
                case 't' -> Integer.toString(totalMajorSteps);
                default -> null;
            });
            SwingUtilities.invokeLater(() -> {
                showDialogIfNeeded();
                progressDialog.setMinorProgressBounds(0, totalMinorSteps);
                progressDialog.setMajorProgress(majorStep + 1, formatted);
                progressDialog.setMinorProgress(0, "");
            });
            return !progressDialog.isCanceled();
        }

        @Override
        public boolean minorProgressUpdate(int majorStep, int minorStep, String message) {
            String formatString = progressDialog.getFormatString();
            final String formatted = StringFormatter.format(formatString, key -> switch (key) {
                case 'm' -> message;
                case 's' -> Integer.toString(minorStep + 1);
                case 't' -> Integer.toString(totalMinorSteps);
                default -> null;
            });
            SwingUtilities.invokeLater(() -> {
                showDialogIfNeeded();
                progressDialog.setMinorProgress(minorStep + 1, formatted);
            });
            return !progressDialog.isCanceled();
        }

        @Override
        public void progressComplete() {
            isFinished = true;
            SwingUtilities.invokeLater(() -> {
                if (disposeWhenComplete) {
                    progressDialog.dispose();
                }
                else {
                    progressDialog.setVisible(false);
                }
            });
        }

        @Override
        public void progressCanceled() {
            isFinished = true;
            SwingUtilities.invokeLater(() -> {
                if (disposeWhenComplete) {
                    progressDialog.dispose();
                }
                else {
                    progressDialog.setVisible(false);
                }
            });
        }
    }

    /**
     * Used internally to wire up a SimpleProgressWorker to the UI elements in this progress
     * dialog, so we can show current progress.
     */
    private static class SimpleProgressHandler extends SimpleProgressAdapter {
        private final MultiProgressDialog progressDialog;
        private final long initialShowDelayMS;
        private final boolean disposeWhenComplete;
        private long startTimeMS;
        private int totalSteps;
        private boolean isFinished;

        public SimpleProgressHandler(MultiProgressDialog ownerDialog, long initialShowDelay, boolean disposeWhenComplete) {
            this.progressDialog = ownerDialog;
            this.initialShowDelayMS = initialShowDelay;
            this.disposeWhenComplete = disposeWhenComplete;
        }

        private long getElapsedTime() {
            return System.currentTimeMillis() - startTimeMS;
        }

        private void showDialogIfNeeded() {
            if (progressDialog.isVisible() || isFinished) {
                return;
            }
            if (initialShowDelayMS <= 0 || getElapsedTime() > initialShowDelayMS) {
                progressDialog.setVisible(true);
            }
        }

        @Override
        public void progressBegins(int totalSteps) {
            this.totalSteps = totalSteps;
            this.startTimeMS = System.currentTimeMillis();
            SwingUtilities.invokeLater(() -> {
                progressDialog.setMajorProgressBounds(0, totalSteps);
                showDialogIfNeeded();
            });
            if (initialShowDelayMS > 0) {
                // We'll check this on each progress update, but if there's a very
                // long-running step, we still want to show the progress dialog after our configured delay:
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        SwingUtilities.invokeLater(() -> showDialogIfNeeded());
                    }
                }, initialShowDelayMS);
            }
        }

        @Override
        public boolean progressUpdate(int currentStep, String message) {
            String formatString = progressDialog.getFormatString();
            final String formatted = StringFormatter.format(formatString, key -> switch (key) {
                case 'm' -> message;
                case 's' -> Integer.toString(currentStep + 1);
                case 't' -> Integer.toString(totalSteps);
                default -> null;
            });
            SwingUtilities.invokeLater(() -> {
                showDialogIfNeeded();
                progressDialog.setMajorProgress(currentStep + 1, formatted);
                progressDialog.setMinorProgress(0, "");
            });
            return !progressDialog.isCanceled();
        }

        @Override
        public void progressComplete() {
            isFinished = true;
            SwingUtilities.invokeLater(() -> {
                if (disposeWhenComplete) {
                    progressDialog.dispose();
                }
                else {
                    progressDialog.setVisible(false);
                }
            });
        }

        @Override
        public void progressCanceled() {
            isFinished = true;
            SwingUtilities.invokeLater(() -> {
                if (disposeWhenComplete) {
                    progressDialog.dispose();
                }
                else {
                    progressDialog.setVisible(false);
                }
            });
        }
    }
}
