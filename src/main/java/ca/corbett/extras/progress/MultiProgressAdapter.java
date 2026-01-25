package ca.corbett.extras.progress;

/**
 * An empty adapter class for MultiProgressListener. None of the methods here do anything.
 * <P>
 *     <B>IMPORTANT:</B> The callbacks in this adapter will be invoked from the worker thread!
 *     If you need to update a Swing UI component as a result of one of these callbacks, you need
 *     to marshal that call to the Swing Event Dispatching Thread, like this:
 * </P>
 * <pre>
 * &#64;Override
 * public boolean minorProgressUpdate(int majorStep, int minorStep, String message) {
 *     // Most operations are fine to do here on the worker thread.
 *     // For example, computing a user-friendly string describing current progress:
 *     String text = buildProgressTextLabel(majorStep, minorStep, message);
 *
 *     // BUT! Now we need to display it in a Swing UI component:
 *     SwingUtilities.invokeLater(() -> { // marshal to EDT
 *        myStatusLabel.setText(text);
 *     });
 * }
 * </pre>
 * <p>
 *     Failure to do this may result in deadlocks or other threading issues, as Swing
 *     itself is not thread-safe.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 1.6 (2022-05-10)
 */
public class MultiProgressAdapter implements MultiProgressListener {

    /**
     * Invoked by the worker when an operation has begun or is about to begin. The number of
     * major work steps is provided here. At the beginning of each major work step,
     * majorProgressUpdate will be invoked with the number of minor steps needed for that
     * major step.
     *
     * @param totalMajorSteps The count of major work steps that will occur during this operation.
     */
    @Override
    public void progressBegins(int totalMajorSteps) {
    }

    /**
     * Invoked by the worker when a major work step has begun or is about to begin.
     * The number of minor work steps required for this major step is provided.
     *
     * @param majorStep       The index of the major step that is beginning.
     * @param totalMinorSteps The count of minor work steps required for this major step to complete.
     * @param message         An optional message describing this major step.
     * @return Return true to continue processing, or false if you wish to cancel the operation.
     */
    @Override
    public boolean majorProgressUpdate(int majorStep, int totalMinorSteps, String message) {
        return true;
    }

    /**
     * Invoked by the worker at the completion of each minor step of this operation.
     * You can cancel the rest of the operation by returning false here.
     *
     * @param majorStep The index of the major step in progress.
     * @param minorStep The index of the minor step that just completed.
     * @param message   An optional message about the current minor step.
     * @return Return true to continue processing, or false if you wish to cancel the operation.
     */
    @Override
    public boolean minorProgressUpdate(int majorStep, int minorStep, String message) {
        return true;
    }

    /**
     * Invoked by the worker if a recoverable error occurs during the scan operation.
     * The operation will continue.
     *
     * @param errorSource  A message describing what caused the error.
     * @param errorDetails Additional details about the error.
     * @return Return true to continue processing, or false if you wish to cancel the operation.
     */
    @Override
    public boolean progressError(String errorSource, String errorDetails) {
        return true;
    }

    /**
     * Invoked by the worker when the operation completes. No further messages
     * will be invoked unless a new operation is requested.
     */
    @Override
    public void progressComplete() {
    }

    /**
     * Invoked by the worker if the operation was canceled before completion. No further messages
     * will be invoked unless a new operation is requested.
     */
    @Override
    public void progressCanceled() {
    }
}
