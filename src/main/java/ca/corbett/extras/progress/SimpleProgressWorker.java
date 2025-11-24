package ca.corbett.extras.progress;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents some generic worker thread that performs a simple task, and can be subscribed
 * to for progress events. Intended for use with a simple progress bar such as ProgressMonitor
 * or with SplashProgressWindow. For complex tasks with multiple progress bars, use
 * MultiProgressWorker instead.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2022-05-10
 */
public abstract class SimpleProgressWorker implements Runnable {

    private final List<SimpleProgressListener> listeners = new ArrayList<>();

    /**
     * Listeners are notified in the order they were added. Normally, this isn't an issue.
     * But if you need your listener invoked before any other, you can use this method
     * to put your listener first in the list.
     */
    public void addPriorityProgressListener(SimpleProgressListener listener) {
        if (listeners.isEmpty()) {
            addProgressListener(listener);
            return;
        }
        listeners.add(0, listener);
    }

    public void addProgressListener(SimpleProgressListener listener) {
        listeners.add(listener);
    }

    public void removeProgressListener(SimpleProgressListener listener) {
        listeners.remove(listener);
    }

    public void clearProgressListeners() {
        listeners.clear();
    }

    protected void fireProgressBegins(int totalMajorSteps) {
        for (SimpleProgressListener listener : new ArrayList<>(listeners)) {
            listener.progressBegins(totalMajorSteps);
        }
    }

    protected boolean fireProgressUpdate(int currentStep, String message) {
        boolean shouldContinue = true;
        for (SimpleProgressListener listener : new ArrayList<>(listeners)) {
            shouldContinue = shouldContinue && listener.progressUpdate(currentStep, message);
        }
        return shouldContinue;
    }

    protected boolean fireProgressError(String errorSource, String errorDetails) {
        boolean shouldContinue = true;
        for (SimpleProgressListener listener : new ArrayList<>(listeners)) {
            shouldContinue = shouldContinue && listener.progressError(errorSource, errorDetails);
        }
        return shouldContinue;

    }

    protected void fireProgressComplete() {
        for (SimpleProgressListener listener : new ArrayList<>(listeners)) {
            listener.progressComplete();
        }
    }

    public void fireProgressCanceled() {
        for (SimpleProgressListener listener : new ArrayList<>(listeners)) {
            listener.progressCanceled();
        }
    }

}
