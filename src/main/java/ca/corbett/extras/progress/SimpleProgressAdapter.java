package ca.corbett.extras.progress;

/**
 * Simple adapter class for SimpleProgressListener. None of these methods do anything.
 *
 * @author scorbo2
 * @since 2022-05-10
 */
public class SimpleProgressAdapter implements SimpleProgressListener {

  /**
   * Invoked by the worker when an operation has begun or is about to begin.
   *
   * @param totalStepCount The total steps that will occur during this operation.
   */
  @Override
  public void progressBegins(int totalStepCount) {

  }

  /**
   * Invoked by the worker at the completion of each step of this operation.
   * You can cancel the rest of the operation by returning false here.
   *
   * @param currentStep The numeric index of the current step in this operation.
   * @param message An informational message about the current step.
   * @return Return true to continue processing, or false if you wish to cancel the operation.
   */
  @Override
  public boolean progressUpdate(int currentStep, String message) {
    return true;
  }

  /**
   * Invoked by the worker if a recoverable error occurs during the scan operation.
   * The operation will continue.
   *
   * @param errorSource A message describing what caused the error.
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
   * Invoked by the worker if the operation is canceled before completion. No further messages
   * will be invoked unless a new operation is requested.
   */
  @Override
  public void progressCanceled() {

  }

}
