package ca.corbett.extras.image.animation;

/**
 * Controls the speed of animation frames, in terms of the delay between frames.
 * These preset values can be used with animation components like
 * {@link FadeLayerUI} and {@link BlurLayerUI}.
 * <p>
 * The toString() method provides both the delay in milliseconds and the roughly
 * equivalent FPS.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
 */
public enum AnimationSpeed {
    VerySlow(100),
    Slow(60),
    Medium(40),
    Fast(25),
    VeryFast(16);

    private final int timerDelay;

    AnimationSpeed(int delay) {
        this.timerDelay = delay;
    }

    public int getDelayMS() {
        return timerDelay;
    }

    @Override
    public String toString() {
        return name() + ": " + timerDelay + "ms = " + (1000 / timerDelay) + "FPS";
    }

    public static AnimationSpeed fromLabel(String label) {
        for (AnimationSpeed candidate : values()) {
            if (candidate.toString().equals(label)) {
                return candidate;
            }
        }
        return null;
    }
}
