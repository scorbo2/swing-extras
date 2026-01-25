package ca.corbett.updates;

import java.util.Comparator;

/**
 * Compares String versions in a sane and predictable way. String sorting of numeric values is inherently
 * unreliable, and winds up with wonkiness like "11.0" being sorted ahead of "2.0". This Comparator
 * makes use of convertVersionToSafeCompareString() in this class to compare version strings in a more
 * predictable way.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.5
 */
public class VersionStringComparator implements Comparator<String> {

    private static final VersionStringComparator comparator = new VersionStringComparator();

    /**
     * Given a version string in the format "x.y.z", return a String that is safe for
     * use in string comparison operations. This involves removing the dots and then zero-padding
     * each component number to three digits. So, "1.2" returns "001002000", and "1.21.5" returns "001021005".
     * <p>
     * The intention is to avoid weird sorting errors like "11.0" being sorted before "2.1",
     * which is what would happen without this method.
     * </p>
     * <p>
     * <b>Versions are normalized to 3 segments</b>. Missing segments are treated as 0.
     * Non-numeric characters are stripped from each segment (e.g., the "v" in "v1.0" is removed).
     * So, "v1-SNAPSHOT" would return "001000000", because we implicitly read that as "1.0.0".
     * Extra version numbers beyond Major.Minor.Patch are simply ignored. So, "1.0.0.1.0"
     * would evaluate to "001000000".
     * </p>
     */
    public static String convertVersionToSafeCompareString(String version) {
        if (version == null || version.isBlank()) {
            return "000000000"; // you give me null, you get 0.0.0
        }

        String[] parts = version.split("\\.");
        StringBuilder sb = new StringBuilder();

        // Process up to 3 version segments
        for (int i = 0; i < 3; i++) {
            int number = 0;
            if (i < parts.length) {
                // Strip non-numeric characters and parse
                String cleaned = parts[i].replaceAll("[^0-9]", "");
                if (!cleaned.isEmpty()) {
                    try {
                        number = Integer.parseInt(cleaned);
                    }
                    catch (NumberFormatException ignored) {
                    }
                }
            }
            sb.append(String.format("%03d", number));
        }

        return sb.toString();
    }

    /**
     * Convenience method to quickly compare a candidate version and report if it is
     * at least (equal to or newer than) the given target version.
     */
    public static boolean isAtLeast(String candidateVersion, String targetVersion) {
        return comparator.compare(candidateVersion, targetVersion) >= 0;
    }

    /**
     * Convenience method to quickly compare a candidate version and report if it is
     * at most (equal to or older than) the given target version.
     */
    public static boolean isAtMost(String candidateVersion, String targetVersion) {
        return comparator.compare(candidateVersion, targetVersion) <= 0;
    }

    /**
     * Convenience method to quickly compare a candidate version and report if it is
     * exactly equal to the given target version.
     */
    public static boolean isExactly(String candidateVersion, String targetVersion) {
        return comparator.compare(candidateVersion, targetVersion) == 0;
    }

    /**
     * Convenience method to quickly compare a candidate version and report if it is
     * newer (higher version number) than the given target version.
     */
    public static boolean isNewerThan(String candidateVersion, String targetVersion) {
        return comparator.compare(candidateVersion, targetVersion) > 0;
    }

    /**
     * Convenience method to quickly compare a candidate version and report if it is
     * older (lower version number) than the given target version.
     */
    public static boolean isOlderThan(String candidateVersion, String targetVersion) {
        return comparator.compare(candidateVersion, targetVersion) < 0;
    }

    @Override
    public int compare(String o1, String o2) {
        return convertVersionToSafeCompareString(o1).compareTo(convertVersionToSafeCompareString(o2));
    }
}
