package ca.corbett.extras.gradient;

/**
 * Contains the available types of gradients.
 */
public enum GradientType {

    /**
     * Describes a single gradient that progresses linearly from left to right, color 1 to color 2.
     */
    HORIZONTAL_LINEAR("Horizontal linear"),

    /**
     * Describes a single gradient that progresses linearly from top to bottom, color 1 to color 2.
     */
    VERTICAL_LINEAR("Vertical linear"),

    /**
     * Describes a two-part gradient that progresses linearly from top to center, color 1 to
     * color 2, and then center to bottom, color 2 to color 1. The end result is a gradient that
     * looks like a horizontal stripe running along the center of the image.
     */
    HORIZONTAL_STRIPE("Horizontal stripe"),

    /**
     * Describes a two-part gradient that progresses linearly from left to center, color 1 to
     * color 2, and then center to right, color 2 to color 1. The end result is a gradient that
     * looks like a vertical stripe running up the center of the image.
     */
    VERTICAL_STRIPE("Vertical stripe"),

    /**
     * Represents a single gradient that progresses linearly from top left to bottom right,
     * color 1 to color 2.
     */
    DIAGONAL1("Diagonal 1"),

    /**
     * Represents a single gradient that progresses linearly from bottom left to top right,
     * color 1 to color 2.
     */
    DIAGONAL2("Diagonal 2"),

    /**
     * Describes a four-part gradient that progresses linearly from each corner of the image
     * towards the center, color 1 to color 2.
     */
    STAR("Star"),

    /**
     * Describes a circular gradient that progresses radially from the center outward,
     * color 1 to color 2.
     */
    CIRCLE("Circle");

    private final String label;

    GradientType(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    public static GradientType fromLabel(String l) {
        for (GradientType candidate : values()) {
            if (candidate.label.equals(l)) {
                return candidate;
            }
        }
        return null;
    }
}
