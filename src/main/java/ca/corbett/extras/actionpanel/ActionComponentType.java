package ca.corbett.extras.actionpanel;

/**
 * Represents options for how actions are displayed in an ActionPanel.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
 */
public enum ActionComponentType {
    LABELS("Show actions as clickable labels"),
    BUTTONS("Show actions as buttons");

    private final String label;

    ActionComponentType(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
