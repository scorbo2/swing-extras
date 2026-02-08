package ca.corbett.extras.image.animation;

/**
 * Controls the total duration of an animation.
 * These preset values can be used with animation components like
 * {@link FadeLayerUI} and {@link BlurLayerUI}.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
 */
public enum AnimationDuration {
    VeryShort(100),
    Short(200),
    Medium(400),
    Long(600),
    VeryLong(1000);

    private final int milliseconds;

    AnimationDuration(int ms) {
        this.milliseconds = ms;
    }

    public int getDurationMS() {
        return milliseconds;
    }

    @Override
    public String toString() {
        return name() + ": " + milliseconds + "ms";
    }

    public static AnimationDuration fromLabel(String label) {
        for (AnimationDuration candidate : values()) {
            if (candidate.toString().equals(label)) {
                return candidate;
            }
        }
        return null;
    }
}
