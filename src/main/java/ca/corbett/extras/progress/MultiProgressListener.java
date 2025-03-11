package ca.corbett.extras.progress;

/**
 * Serves as a progress listener for some work task that involves multiple major steps,
 * each with some variable number of minor work steps. Listeners can use this in conjunction
 * with MultiProgressDialog to show a major and minor progress bar as the work progresses.
 *
 * @author scorbo2
 * @since 2022-05-10
 */
public interface MultiProgressListener {

  /**
   * Invoked by the worker when an operation has begun or is about to begin. The number of
   * major work steps is provided here. At the beginning of each major work step,
   * majorProgressUpdate will be invoked with the number of minor steps needed for this
   * major step.
   *
   * @param totalMajorSteps The count of major work steps that will occur during this operation.
   */
  public void progressBegins(int totalMajorSteps);

  /**
   * Invoked by the worker when a major work step has begun or is about to begin.
   * The number of minor work steps required for this major step is provided.
   *
   * @param majorStep The index of the major step that is beginning.
   * @param totalMinorSteps The count of minor work steps required for this major step to complete.
   * @param message An optional message describing this major step.
   * @return Return true to continue processing, or false if you wish to cancel the operation.
   */
  public boolean majorProgressUpdate(int majorStep, int totalMinorSteps, String message);

  /**
   * Invoked by the worker at the completion of each minor step of this operation.
   * You can cancel the rest of the operation by returning false here.
   *
   * @param majorStep The index of the major step in progress.
   * @param minorStep The index of the minor step that just completed.
   * @param message An optional message about the current minor step.
   * @return Return true to continue processing, or false if you wish to cancel the operation.
   */
  public boolean minorProgressUpdate(int majorStep, int minorStep, String message);

  /**
   * Invoked by the worker if a recoverable error occurs during the scan operation.
   * The operation will continue.
   *
   * @param errorSource A message describing what caused the error.
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
   * Invoked by the worker if the operation was canceled before completion. No further messages
   * will be invoked unless a new operation is requested.
   */
  public void progressCanceled();

}
