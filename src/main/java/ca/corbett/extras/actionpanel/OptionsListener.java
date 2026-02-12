package ca.corbett.extras.actionpanel;

/**
 * Used by our various options classes to notify listeners that an option
 * has changed and that the UI should be updated.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
 */
@FunctionalInterface
public interface OptionsListener {
    void optionsChanged();
}
