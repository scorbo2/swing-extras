package ca.corbett.extras.progress;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents some generic worker thread that performs a complex task, and can be subscribed
 * to for major and minor progress events. Intended for use with MultiProgressDialog.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2022-05-10
 */
public abstract class MultiProgressWorker implements Runnable {

    private final List<MultiProgressListener> listeners = new ArrayList<>();

    /**
     * Listeners are notified in the order they were added. Normally, this isn't an issue.
     * But if you need your listener invoked before any other, you can use this method
     * to put your listener first in the list.
     */
    public void addPriorityProgressListener(MultiProgressListener listener) {
        if (listeners.isEmpty()) {
            addProgressListener(listener);
            return;
        }
        listeners.add(0, listener);
    }

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
        for (MultiProgressListener listener : new ArrayList<>(listeners)) {
            listener.progressBegins(totalMajorSteps);
        }
    }

    protected boolean fireMajorProgressUpdate(int majorStep, int totalMinorSteps, String message) {
        boolean shouldContinue = true;
        for (MultiProgressListener listener : new ArrayList<>(listeners)) {
            shouldContinue = shouldContinue && listener.majorProgressUpdate(majorStep, totalMinorSteps, message);
        }
        return shouldContinue;
    }

    public boolean fireMinorProgressUpdate(int majorStep, int minorStep, String message) {
        boolean shouldContinue = true;
        for (MultiProgressListener listener : new ArrayList<>(listeners)) {
            shouldContinue = shouldContinue && listener.minorProgressUpdate(majorStep, minorStep, message);
        }
        return shouldContinue;
    }

    public boolean fireProgressError(String errorSource, String errorDetails) {
        boolean shouldContinue = true;
        for (MultiProgressListener listener : new ArrayList<>(listeners)) {
            shouldContinue = shouldContinue && listener.progressError(errorSource, errorDetails);
        }
        return shouldContinue;

    }

    public void fireProgressComplete() {
        for (MultiProgressListener listener : new ArrayList<>(listeners)) {
            listener.progressComplete();
        }
    }

    public void fireProgressCanceled() {
        for (MultiProgressListener listener : new ArrayList<>(listeners)) {
            listener.progressCanceled();
        }
    }

}
