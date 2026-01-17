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
 * Replacement for Java's ProgressMonitor class, which is a bit limiting.
 * Specifically, this dialog can show "major" and "minor" progress
 * bars simultaneously, and increment them both, as a long-running worker thread
 * progresses. This allows you to show more detailed progress, particularly for
 * executing worker threads involving a list of long-running work items. For example,
 * iterating over a list of directories (major steps), and performing some time-intensive
 * operation within each directory (minor steps). In this hypothetical example, it
 * would be helpful to the user to show a "major" progress bar showing the progress
 * through the list of directories ("directory 2 of 12"), while also showing
 * a "minor" progress bar showing the progress within the current directory ("file 17 of 111").
 * <p>
 * <b>USAGE:</b> Start by extending one of the worker classes: either MultiProgressWorker
 * (for major and minor progress) or SimpleProgressWorker (for just a single progress bar).
 * Implement the run() method to perform your long-running task. As your task progresses,
 * invoke the various fire...() methods to notify listeners of progress. Remember to
 * invoke either fireProgressComplete() or fireProgressCanceled() at the end of your
 * run() method to let listeners know that the work is done. Failure to invoke one of
 * these termination events will result in the progress dialog remaining open indefinitely.
 * </p>
 * <p>
 * When you have your worker class ready, create an instance of MultiProgressDialog,
 * and invoke its runWorker() method, passing in an instance of your worker class.
 * If you wish to set other options (documented below), do so before invoking runWorker().
 * </p>
 * <p>
 * MultiProgressDialog instances can be re-used, but be sure to invoke resetProgress()
 * in between each usage to reset the progress bars.
 * </p>
 * <h2>Other options</h2>
 * <ul>
 *     <li>You can set an initial show delay on the dialog, so that it will only appear
 *     if the work takes longer than a certain amount of time. This is useful to avoid
 *     flashing a progress dialog for very short operations. Use the setInitialShowDelayMS()
 *     method to set this delay time in milliseconds. The default is 0, meaning the dialog
 *     will appear as soon as the work begins.</li>
 *     <li>You can customize the format of the progress messages shown in the major and minor
 *     progress labels. See the DEFAULT_PROGRESS_FORMAT constant for details.
 *     Use the setFormatString() method to change the format string.</li>
 * </ul>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 1.6 (2022-04-15)
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
     * <p>
     *     The available tags are:
     * </p>
     * <ul>
     *     <li>%m - The progress message sent from the worker thread</li>
     *     <li>%s - The current step number (1-based)</li>
     *     <li>%t - The total number of steps</li>
     * </ul>
     * <p>
     *     You can add whatever punctuation, spacing, or other text you like around the tags,
     *     though be aware there is a length limit on the progress label, and the worker-supplied
     *     message can be quite long.
     * </p>
     * <p>
     *     Use the setFormatString() method to change the format string.
     * </p>
     */
    public static final String DEFAULT_PROGRESS_FORMAT = "[%s of %t] %m";

    /**
     * If you really don't like the new 2.7 default format, you can use this
     * legacy format string to get the old behavior.
     */
    public static final String LEGACY_PROGRESS_FORMAT = "%m (%s of %t)";

    /**
     * Long progress messages are truncated at a fixed length to avoid rendering
     * problems with very large JLabels on the dialog. By default, we truncate them
     * after a certain length (TruncationMode.END). You can opt to truncate
     * from the start instead by using TruncationMode.START.
     */
    public enum TruncationMode {
        START,
        END
    }

    static final int LABEL_LENGTH_CUTOFF = 50;
    private LabelField majorProgressLabel;
    private LabelField minorProgressLabel;
    private JProgressBar majorProgressBar;
    private JProgressBar minorProgressBar;
    private long initialShowDelayMS;
    private boolean isCanceled;
    private TruncationMode truncationMode;
    private volatile String formatString = DEFAULT_PROGRESS_FORMAT;

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
        truncationMode = TruncationMode.END; // default
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
        // We will use a "priority listener" to make sure we get notified first...
        // This feature of our worker classes is package-protected, so only we can do this.
        // Without this, the "initialShowDelay" timer on the dialog may force it visible after completion, even if some
        // other listener is trying showing a popup (for example, to report on the results of the operation).
        // Our priority listener will kill the timer, which avoids that problem.
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
            majorProgressLabel.setText(truncString(message, LABEL_LENGTH_CUTOFF));
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
            minorProgressLabel.setText(truncString(message, LABEL_LENGTH_CUTOFF));
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
     * Shorthand for truncString(s, LABEL_LENGTH_CUTOFF)
     *
     * @param s The string to truncate.
     * @return The truncated string, or null if input was null. Length of LABEL_LENGTH_CUTOFF or less, including "...".
     */
    String truncString(String s) {
        return truncString(s, LABEL_LENGTH_CUTOFF);
    }

    /**
     * Truncates the given string to the given length limit, if necessary.
     * We add a "..." to indicate truncation. This is accounted for in the
     * length of the returned string. That is, the length of the returned
     * string will never exceed the given limit, even if we add "..." to it.
     *
     * @param s The string to truncate.
     * @param limit The maximum length of the string. Cannot exceed LABEL_LENGTH_CUTOFF.
     * @return The truncated string, or null if input was null. Length of "limit" or less, including the "...".
     */
    String truncString(String s, int limit) {
        // Our resulting string, including our ellipsis, must fit within the limit:
        final String overageMarker = "...";

        // But make sure we don't end up with a negative limit:
        if (limit < 0) {
            limit = 0;
        }

        // Also, ensure that our given limit never exceeds our built-in max:
        if (limit > LABEL_LENGTH_CUTOFF) {
            limit = LABEL_LENGTH_CUTOFF;
        }

        // You give me null, you get null:
        if (s == null) {
            return null;
        }

        // If our message isn't long enough to warrant this, then it's fine as-is:
        if (s.length() <= limit) {
            return s;
        }

        // Adjust for the ellipsis that we will add:
        // (i.e. the TOTAL length of the returned string must be <= limit)
        limit = limit - overageMarker.length();

        // Trim the correct end of the string:
        return switch (truncationMode) {
            case START -> "..." + s.substring(s.length() - limit);
            case END -> s.substring(0, limit) + "...";
        };
    }

    /**
     * Uses the current formatString to format the given progress message.
     * The given msg may be truncated if it exceeds the length limit.
     * We add a "..." to the progress message if truncation occurs.
     * This will never result in the step counters being truncated,
     * only the log message itself.
     *
     * @param msg         The progress message, which contains formatting tags to be replaced.
     * @param currentStep The current numeric step (1-based).
     * @param totalSteps  The total number of steps.
     * @return A formatted progress message, of length LABEL_LENGTH_CUTOFF or less.
     */
    String formatMessage(String msg, int currentStep, int totalSteps) {
        final String formatString = getFormatString();

        // The logic in here is a bit more complicated than it seems like it needs to be,
        // but we have to be careful when truncating messages, so that we don't
        // truncate %s (currentStep) and %t (totalSteps) in the format string.
        // Ideally, we ONLY want to truncate the %m part of the message.
        //
        // Example: "[1 of 10] This is a very long message that might need truncation"
        // Should never become: "... a very long message that might need truncation" (50 chars)
        // But rather: "[1 of 10] ...ng message that might need truncation" (50 chars)
        //
        // And likewise when using TruncationMode.END with the legacy format:
        // Example: "This is a very long message that might need truncation (1 of 10)"
        // Should never become: "This is a very long message that might need tru..." (50 chars)
        // But rather: "This is a very long message that migh... (1 of 10)" (50 chars)

        // Start by substituting in the step numbers, so that we can measure the length:
        String intermediateMsg = StringFormatter.format(formatString, key -> switch (key) {
            case 's' -> Integer.toString(currentStep);
            case 't' -> Integer.toString(totalSteps);
            default -> null;
        });
        int intermediateLength = intermediateMsg.length() - 2; // subtract 2 for %m, which we will replace later
        int totalLength = intermediateLength + msg.length(); // how long will our result be?

        // Now figure out if we're going to exceed the length limit:
        int overage = totalLength - LABEL_LENGTH_CUTOFF;
        if (overage > 0) {
            // Our message is too long, so we need to truncate it:
            msg = truncString(msg, msg.length() - overage);
        }
        final String finalMessage = msg;

        // Now we can replace %m with our possibly-truncated message:
        return StringFormatter.format(intermediateMsg, key -> key == 'm' ? finalMessage : null);
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
        this.formatString = (formatString != null) ? formatString : DEFAULT_PROGRESS_FORMAT;
    }

    /**
     * Returns the current truncation mode for long progress messages.
     */
    public TruncationMode getTruncationMode() {
        return truncationMode;
    }

    /**
     * The default behavior is to truncate long progress messages at the end.
     * You can change this to truncate at the start instead.
     * Example: "some unreasonably long message" can either become
     * "some unreasonably long mes..." with TruncationMode.END, or it can
     * become "...nably long message" with TruncationMode.START.
     */
    public void setTruncationMode(TruncationMode truncationMode) {
        this.truncationMode = (truncationMode != null) ? truncationMode : TruncationMode.END;
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
            String formatted = progressDialog.formatMessage(message, majorStep + 1, totalMajorSteps);
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
            String formatted = progressDialog.formatMessage(message, minorStep + 1, totalMinorSteps);
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
            String formatted = progressDialog.formatMessage(message, currentStep + 1, totalSteps);
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
