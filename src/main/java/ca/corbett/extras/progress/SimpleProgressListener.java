package ca.corbett.extras.progress;

/**
 * Serves as a progress listener for some generic work task. This is intended to be used
 * with a simple progress bar such as ProgressMonitor. For more complex tasks that involve
 * major and minor work steps with multiple progress bars needed, use MultiProgressListener
 * instead.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2022-05-10
 */
public interface SimpleProgressListener {

    /**
     * Invoked by the worker when an operation has begun or is about to begin.
     *
     * @param totalStepCount The total steps that will occur during this operation.
     */
    public void progressBegins(int totalStepCount);

    /**
     * Invoked by the worker at the completion of each step of this operation.
     * You can cancel the rest of the operation by returning false here.
     *
     * @param currentStep The numeric index of the current step in this operation.
     * @param message     An informational message about the current step.
     * @return Return true to continue processing, or false if you wish to cancel the operation.
     */
    public boolean progressUpdate(int currentStep, String message);

    /**
     * Invoked by the worker if a recoverable error occurs during the scan operation.
     * The operation will continue.
     *
     * @param errorSource  A message describing what caused the error.
     * @param errorDetails Additional details about the error.
     * @return Return true to continue processing, or false if you wish to cancel the operation.
     */
    public boolean progressError(String errorSource, String errorDetails);

    /**
     * Invoked by the worker when the operation completes. No further messages
     * will be invoked unless a new operation is requested.
     */
    public void progressComplete();

    /**
     * Invoked by the worker if the operation is canceled before completion. No further messages
     * will be invoked unless a new operation is requested.
     */
    public void progressCanceled();

}
