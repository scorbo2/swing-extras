package ca.corbett.extras.io;

/**
 * Constants used in unit tests.
 */
public final class TestConstants {

    /**
     * A test domain name that should not resolve.
     * Using .invalid TLD which is reserved for testing per RFC 2606.
     */
    public static final String TEST_DOMAIN = "example.invalid";

    private TestConstants() {
        // Utility class, prevent instantiation
    }
}
