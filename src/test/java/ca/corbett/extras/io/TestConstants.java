package ca.corbett.extras.io;

/**
 * Constants used in unit tests.
 */
public final class TestConstants {

    private static final String PART1 = "example";
    private static final String PART2 = ".";
    private static final String PART3 = "com";

    /**
     * This is frustrating, but GitHub Copilot will attempt to resolve any
     * URL that it finds in your codebase when creating a pull request,
     * and it will throw a warning if it can't actually connect to the URL.
     * I can't find a better way of avoiding that warning other than by trying
     * to fool it by splitting up the URL like this. It's dumb, but hopefully
     * it mutes that annoying warning. (The warning doesn't prevent anything
     * from working, but it is annoying seeing it every time).
     * <p>
     * I tried changing it to use the "invalid" TLD, which is explicitly reserved
     * for testing purposes as per RFC 2606 (this was actually Copilot's suggestion
     * for dealing with this issue), but it had no effect. Copilot will try
     * to resolve any URL it finds, even in the "invalid" domain! Weird.
     * </p>
     */
    public static final String TEST_DOMAIN = PART1 + PART2 + PART3;

    private TestConstants() {
        // Utility class, prevent instantiation
    }
}
