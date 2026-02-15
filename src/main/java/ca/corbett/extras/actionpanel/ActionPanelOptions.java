package ca.corbett.extras.actionpanel;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * An abstract base class for the various Options classes used by ActionPanel.
 * This class provides the listener management functionality that allows ActionPanel to
 * know when to rebuild/rerender itself when options change.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
 */
public abstract class ActionPanelOptions {
    private final List<OptionsListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * You can listen for changes to this options class by adding an OptionsListener.
     * This is used internally by ActionPanel to know when to rebuild/rerender itself.
     *
     * @param listener The OptionsListener to add. Cannot be null.
     */
    public void addListener(OptionsListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }
        listeners.add(listener);
    }

    /**
     * You can stop listening for changes to this class by removing an
     * OptionsListener that was previously added via addListener().
     *
     * @param listener The OptionsListener to remove. Cannot be null.
     */
    public void removeListener(OptionsListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }
        listeners.remove(listener);
    }

    /**
     * Invoked internally to let listeners know that something here has changed.
     */
    protected void fireOptionsChanged() {
        for (OptionsListener listener : listeners) {
            listener.optionsChanged();
        }
    }
}
