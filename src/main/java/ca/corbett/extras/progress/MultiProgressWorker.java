package ca.corbett.extras.progress;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents some generic worker thread that performs a complex task, and can be subscribed
 * to for major and minor progress events. Intended for use with MultiProgressDialog.
 *
 * @author scorbo2
 * @since 2022-05-10
 */
public abstract class MultiProgressWorker implements Runnable {

  private final List<MultiProgressListener> listeners = new ArrayList<>();

  public void addProgressListener(MultiProgressListener listener) {
    listeners.add(listener);
  }

  public void removeProgressListener(MultiProgressListener listener) {
    listeners.remove(listener);
  }

  public void clearProgressListeners() {
    listeners.clear();
  }

  protected void fireProgressBegins(int totalMajorSteps) {
    for (MultiProgressListener listener : listeners) {
      listener.progressBegins(totalMajorSteps);
    }
  }

  protected boolean fireMajorProgressUpdate(int majorStep, int totalMinorSteps, String message) {
    boolean shouldContinue = true;
    for (MultiProgressListener listener : listeners) {
      shouldContinue = shouldContinue && listener.majorProgressUpdate(majorStep, totalMinorSteps, message);
    }
    return shouldContinue;
  }

  public boolean fireMinorProgressUpdate(int majorStep, int minorStep, String message) {
    boolean shouldContinue = true;
    for (MultiProgressListener listener : listeners) {
      shouldContinue = shouldContinue && listener.minorProgressUpdate(majorStep, minorStep, message);
    }
    return shouldContinue;
  }

  public boolean fireProgressError(String errorSource, String errorDetails) {
    boolean shouldContinue = true;
    for (MultiProgressListener listener : listeners) {
      shouldContinue = shouldContinue && listener.progressError(errorSource, errorDetails);
    }
    return shouldContinue;

  }

  public void fireProgressComplete() {
    for (MultiProgressListener listener : listeners) {
      listener.progressComplete();
    }
  }

  public void fireProgressCanceled() {
    for (MultiProgressListener listener : listeners) {
      listener.progressCanceled();
    }
  }

}
