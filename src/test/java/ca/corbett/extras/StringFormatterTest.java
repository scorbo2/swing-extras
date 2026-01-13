package ca.corbett.extras;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

class StringFormatterTest {

    private StringFormatter.ReplaceMapper testMapper = key -> switch (key) {
        case 'h' -> "Hello";
        case 'w' -> "World";
        default -> null;
    };

    @Test
    public void format_withNullFormatString_shouldReturnNull() {
        assertNull(StringFormatter.format(null, testMapper)); // null formatString
    }

    @Test
    public void format_withNullMapper_shouldThrow() {
        try {
            StringFormatter.format("Hello", null);
            fail("Expected IllegalArgumentException but didn't get one!");
        }
        catch (IllegalArgumentException ignored) {
            // Expected exception
        }
    }

    @Test
    public void format_withNoFormatTags_shouldReturnInput() {
        assertEquals("No tags here", StringFormatter.format("No tags here", testMapper));
    }

    @Test
    public void format_withEmptyString_shouldReturnInput() {
        assertEquals("", StringFormatter.format("", testMapper));
    }

    @Test
    public void format_withUnknownTags_shouldIgnoreTags() {
        assertEquals("Unknown: %x %y", StringFormatter.format("Unknown: %x %y", testMapper));
    }

    @Test
    public void format_withLiteralPercent_shouldHandleCorrectly() {
        assertEquals("100% sure", StringFormatter.format("100%% sure", testMapper));
    }

    @Test
    public void format_withActualTags_shouldReplaceCorrectly() {
        assertEquals("Hello, World!", StringFormatter.format("%h, %w!", testMapper));
    }

    @Test
    public void format_withMultipleTags_shouldReplaceAllCorrectly() {
        assertEquals("Hello World! Hello World!", StringFormatter.format("%h %w! %h %w!", testMapper));
    }
}
