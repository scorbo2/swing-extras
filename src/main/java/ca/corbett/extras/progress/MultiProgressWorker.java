package ca.corbett.extras.progress;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents some generic worker thread that performs a complex task (major steps and minor steps),
 * and can be subscribed to for major and minor progress events. Intended for use with MultiProgressDialog.
 * <p>
 *     <B>USAGE:</B> Generally, you just need to extend this class and implement the run() method from
 *     the Runnable interface. Use the various fire...() methods to keep listeners up to date
 *     as to the status of your operation. Remember to end by invoking EITHER fireProgressComplete()
 *     or fireProgressCanceled()! Failure to fire one of these termination events will result in the
 *     progress dialog remaining open.
 * </p>
 * <p>
 *     <b>It is important to check for cancellation!</b> The fireMajorProgressUpdate() and
 *     fireMinorProgressUpdate() methods return a boolean indicating whether the operation should continue.
 *     If any listener returns false, you should abort your operation as soon as possible and invoke
 *     fireProgressCanceled(). Failure to do so may result in the progress dialog remaining open
 *     indefinitely.
 * </p>
 * <p>
 *     <b>Reporting errors</b> - If a recoverable error occurs during your operation, you can report it
 *     by invoking fireProgressError(). This will notify all listeners of the error. The listeners
 *     can then decide whether to continue or cancel the operation by returning true or false.
 *     If any listener returns false, you should abort your operation and invoke fireProgressCanceled().
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 1.6 (2022-05-10)
 */
public abstract class MultiProgressWorker implements Runnable {

    private final List<MultiProgressListener> listeners = new ArrayList<>();

    /**
     * This is invoked internally by MultiProgressDialog to ensure that its listener fires
     * before any other. This is necessary to avoid problems with initialShowDelay on the dialog.
     */
    void addPriorityProgressListener(MultiProgressListener listener) {
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
