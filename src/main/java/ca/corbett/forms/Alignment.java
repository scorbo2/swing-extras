package ca.corbett.forms;

/**
 * Useful for FormPanel alignment, to decide how to lay out controls within a FormPanel.
 */
public enum Alignment {
    TOP_LEFT("Top left"),
    TOP_CENTER("Top center"),
    TOP_RIGHT("Top right"),
    CENTER_LEFT("Center left"),
    CENTER("Center"),
    CENTER_RIGHT("Center right"),
    BOTTOM_LEFT("Bottom left"),
    BOTTOM_CENTER("Bottom center"),
    BOTTOM_RIGHT("Bottom right");

    private final String label;

    Alignment(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    public boolean isLeftAligned() {
        return this == TOP_LEFT || this == CENTER_LEFT || this == BOTTOM_LEFT;
    }

    public boolean isRightAligned() {
        return this == TOP_RIGHT || this == CENTER_RIGHT || this == BOTTOM_RIGHT;
    }

    public boolean isTopAligned() {
        return this == TOP_LEFT || this == TOP_CENTER || this == TOP_RIGHT;
    }

    public boolean isBottomAligned() {
        return this == BOTTOM_LEFT || this == BOTTOM_CENTER || this == BOTTOM_RIGHT;
    }

    public boolean isCenteredVertically() {
        return this == CENTER_LEFT || this == CENTER || this == CENTER_RIGHT;
    }

    public boolean isCenteredHorizontally() {
        return this == TOP_CENTER || this == CENTER || this == BOTTOM_CENTER;
    }
}
