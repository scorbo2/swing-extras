package ca.corbett.extras.progress;

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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

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

    private static final int LABEL_LENGTH_CUTOFF = 50;
    private LabelField majorProgressLabel;
    private LabelField minorProgressLabel;
    private JProgressBar majorProgressBar;
    private JProgressBar minorProgressBar;
    private boolean isCanceled;

    /**
     * Creates a new, blank MultiProgressDialog with the specified owner frame
     * and window title. Closing the progress dialog will act the same
     * as though the cancel button had been clicked. Call isCanceled()
     * at any time to see if the operation should continue or not (that is,
     * if the user has clicked the cancel button on the progress dialog).
     *
     * @param owner The JFrame that will own this MultiProgressDialog
     * @param title The window title
     */
    public MultiProgressDialog(JFrame owner, String title) {
        super(owner, title);
        initComponents();
        resetProgress();
        setLocationRelativeTo(owner);
    }

    /**
     * Creates a new, blank MultiProgressDialog with the specified owner dialog
     * and window title. Closing the progress dialog will act the same
     * as though the cancel button had been clicked. Call isCanceled()
     * at any time to see if the operation should continue or not (that is,
     * if the user has clicked the cancel button on the progress dialog).
     *
     * @param owner The JDialog that will own this MultiProgressDialog
     * @param title The window title
     */
    public MultiProgressDialog(JDialog owner, String title) {
        super(owner, title);
        initComponents();
        resetProgress();
        setLocationRelativeTo(owner);
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
        final MultiProgressDialog progressDialog = this;
        minorProgressLabel.setVisible(true);
        minorProgressBar.setVisible(true);
        setSize(new Dimension(500, 210));
        final MultiProgressListener listener = new MultiProgressAdapter() {
            private int totalMajorSteps;
            private int totalMinorSteps;

            @Override
            public void progressBegins(int totalMajorSteps) {
                this.totalMajorSteps = totalMajorSteps;
                SwingUtilities.invokeLater(() -> {
                    progressDialog.setMajorProgressBounds(0, totalMajorSteps);
                    progressDialog.setVisible(true);
                });
            }

            @Override
            public boolean majorProgressUpdate(int majorStep, int totalMinorSteps, String message) {
                this.totalMinorSteps = totalMinorSteps;
                SwingUtilities.invokeLater(() -> {
                    progressDialog.setMinorProgressBounds(0, totalMinorSteps);
                    progressDialog.setMajorProgress(majorStep + 1,
                                                    message + " (" + (majorStep + 1) + " of " + totalMajorSteps + ")");
                    progressDialog.setMinorProgress(0, "");
                });
                return !isCanceled;
            }

            @Override
            public boolean minorProgressUpdate(int majorStep, int minorStep, String message) {
                SwingUtilities.invokeLater(() -> {
                    progressDialog.setMinorProgress(minorStep + 1,
                                                    message + " (" + (minorStep + 1) + " of " + totalMinorSteps + ")");
                });
                return !isCanceled;
            }

            @Override
            public void progressComplete() {
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
                SwingUtilities.invokeLater(() -> {
                    if (disposeWhenComplete) {
                        progressDialog.dispose();
                    }
                    else {
                        progressDialog.setVisible(false);
                    }
                });
            }

        };

        worker.addProgressListener(listener);
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
        final MultiProgressDialog progressDialog = this;
        minorProgressLabel.setVisible(false);
        minorProgressBar.setVisible(false);
        setSize(new Dimension(500, 160));
        final SimpleProgressListener listener = new SimpleProgressAdapter() {
            private int totalSteps;

            @Override
            public void progressBegins(int totalSteps) {
                this.totalSteps = totalSteps;
                SwingUtilities.invokeLater(() -> {
                    progressDialog.setMajorProgressBounds(0, totalSteps);
                    progressDialog.setVisible(true);
                });
            }

            @Override
            public boolean progressUpdate(int currentStep, String message) {
                SwingUtilities.invokeLater(() -> {
                    progressDialog.setMajorProgress(currentStep + 1,
                                                    message + " (" + (currentStep + 1) + " of " + totalSteps + ")");
                    progressDialog.setMinorProgress(0, "");
                });
                return !isCanceled;
            }

            @Override
            public void progressComplete() {
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
                SwingUtilities.invokeLater(() -> {
                    if (disposeWhenComplete) {
                        progressDialog.dispose();
                    }
                    else {
                        progressDialog.setVisible(false);
                    }
                });
            }

        };

        worker.addProgressListener(listener);
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
     * Invoked internally to lay out the form.
     */
    private void initComponents() {
        setMinimumSize(new Dimension(500, 160));
        setSize(new Dimension(500, 210));
        setResizable(false);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                isCanceled = true;
            }

        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ESCAPE: {
                        isCanceled = true;
                    }
                    break;
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
        //majorProgressBar.setForeground(Color.BLUE);
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

        final JButton button = new JButton("Cancel");
        button.setPreferredSize(new Dimension(90, 23));
        button.setFocusable(false);
        buttonPanel.add(button);

        add(buttonPanel, BorderLayout.SOUTH);

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                button.setEnabled(false);
                isCanceled = true;
            }

        });

    }

}
