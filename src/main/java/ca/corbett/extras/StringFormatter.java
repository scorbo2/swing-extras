package ca.corbett.extras;

/**
 * A utility class for taking a formatString and performing a configurable
 * mapping using a provided mapper function.
 * <p>
 * <b>USAGE:</b> Your format string can include any number of tags
 * starting with a percent sign (%) followed by a single character.
 * The ReplaceMapper that you supply will be called with each character
 * that follows a percent sign, and should return the string to replace
 * the tag with. If the mapper returns null, the tag will be left in place as-is.
 * To include a literal percent sign in the output, use %% in the format string.
 * </p>
 * <p><b>Examples:</b></p>
 * <pre>
 *     // This returns: "Hello, Alice! Today is Monday."
 *     StringFormatter.format("Hello, %n! Today is %d.", ch -> {
 *         switch (ch) {
 *             case 'n': return "Alice";
 *             case 'd': return "Monday";
 *             default: return null;
 *         }
 *     });
 *
 *     // This returns: "Processing: [step 1 of 3] 50% complete"
 *     StringFormatter.format("Processing: [step %s of %t] %p%% complete", ch -> {
 *         switch (ch) {
 *             case 's': return "1";
 *             case 't': return "3";
 *             case 'p': return "50";
 *             default: return null;
 *         }
 *     });
 * </pre>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class StringFormatter {

    /**
     * Using the given format string and mapper, produce a formatted string.
     *
     * @param formatString A format string containing zero or more format tags to be replaced.
     * @param mapper       A mapper which maps format tag characters to their replacement strings.
     * @return The formatted string with all applicable tags replaced.
     */
    public static String format(String formatString, ReplaceMapper mapper) {
        // Null mapper is almost certainly a programmer error:
        if (mapper == null) {
            throw new IllegalArgumentException("ReplaceMapper cannot be null");
        }
        // Garbage in, garbage out:
        if (formatString == null) {
            return null;
        }

        // We'll go character by character through the format string:
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < formatString.length()) {
            char c = formatString.charAt(i);

            // The '%' character is what signals a format tag:
            if (c == '%') {
                // Check if there's a character after %
                if (i + 1 >= formatString.length()) {
                    result.append('%');
                    i++;
                    continue; // just leave it in, it's fine
                }

                // Get the format tag to be replaced:
                char formatChar = formatString.charAt(i + 1);

                // Special case: for a literal % sign, you can use %%:
                if (formatChar == '%') {
                    result.append('%');
                    i += 2; // Skip both '%' characters
                    continue;
                }

                // Ask our mapper for the replacement string:
                String replacement = mapper.replace(formatChar);

                // If the mapper flaked out, we'll just ignore this tag:
                if (replacement == null) {
                    result.append('%');
                    result.append(formatChar);
                    i += 2; // Skip over them
                    continue;
                }

                result.append(replacement);
                i += 2; // Skip both '%' and the format character
            }
            else {
                result.append(c);
                i++;
            }
        }

        return result.toString();
    }

    /**
     * A very simple functional interface for mapping format tags to replacement strings.
     * The assumption is that a format tag will never be more than a single character.
     */
    @FunctionalInterface
    public interface ReplaceMapper {
        String replace(char ch);
    }
}
