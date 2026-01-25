package ca.corbett.extras.progress;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiProgressDialogTest {

    @Test
    public void truncString_withShortMessage_shouldDoNothing() {
        // GIVEN a message that is shorter than the truncation limit:
        String shortMessage = "This is a short message.";

        // WHEN we truncate with no explicit truncation mode:
        MultiProgressDialog dialog = new MultiProgressDialog(null, "test");
        String truncated = dialog.truncString(shortMessage);

        // THEN the message should remain unchanged:
        assertEquals(shortMessage, truncated);

        // WHEN we switch to TruncationMode.START:
        dialog.setTruncationMode(MultiProgressDialog.TruncationMode.START);
        truncated = dialog.truncString(shortMessage);

        // THEN the message should remain unchanged:
        assertEquals(shortMessage, truncated);
    }

    @Test
    public void truncString_withLongMessageAndDefaultSettings_shouldTruncateEnd() {
        // GIVEN a message that exceeds the truncation limit:
        String longMessage = "This is a very long message that is intended to exceed the truncation limit set in the MultiProgressDialog class for testing purposes.";

        // WHEN we truncate with no explicit truncation mode:
        MultiProgressDialog dialog = new MultiProgressDialog(null, "test");
        String truncated = dialog.truncString(longMessage);

        // THEN the message should be truncated at the end:
        String expected = longMessage.substring(0, MultiProgressDialog.LABEL_LENGTH_CUTOFF - 3) + "...";
        assertEquals(expected, truncated);
        assertTrue(truncated.length() <= MultiProgressDialog.LABEL_LENGTH_CUTOFF);
    }

    @Test
    public void truncString_withLongMessageAndStartMode_shouldTruncateStart() {
        // GIVEN a message that exceeds the truncation limit:
        String longMessage = "This is a very long message that is intended to exceed the truncation limit set in the MultiProgressDialog class for testing purposes.";

        // WHEN we set truncation mode to START and truncate:
        MultiProgressDialog dialog = new MultiProgressDialog(null, "test");
        dialog.setTruncationMode(MultiProgressDialog.TruncationMode.START);
        String truncated = dialog.truncString(longMessage);

        // THEN the message should be truncated at the start:
        String expected = "..." + longMessage.substring(longMessage.length() - MultiProgressDialog.LABEL_LENGTH_CUTOFF + 3);
        assertEquals(expected, truncated);
        assertTrue(truncated.length() <= MultiProgressDialog.LABEL_LENGTH_CUTOFF);
    }

    @Test
    public void truncString_withMessageExactly50Chars_shouldNotTruncate() {
        // GIVEN a message that is exactly 50 characters long (our length cutoff limit):
        String exactLengthMessage = "12345678901234567890123456789012345678901234567890"; // 50 characters

        // WHEN we truncate it in any truncation mode:
        MultiProgressDialog dialog = new MultiProgressDialog(null, "test");
        String truncatedDefault = dialog.truncString(exactLengthMessage);
        dialog.setTruncationMode(MultiProgressDialog.TruncationMode.START);
        String truncatedStart = dialog.truncString(exactLengthMessage);

        // THEN the message should NOT have been truncated in either case:
        assertEquals(exactLengthMessage, truncatedDefault);
        assertEquals(exactLengthMessage, truncatedStart);
    }

    @Test
    public void truncString_withMessageExactly51Chars_shouldTruncate() {
        // GIVEN a message that is a single character longer than the cutoff:
        String overLengthMessage = "123456789012345678901234567890123456789012345678901"; // 51 characters

        // WHEN we truncate it in any truncation mode:
        MultiProgressDialog dialog = new MultiProgressDialog(null, "test");
        String truncatedDefault = dialog.truncString(overLengthMessage);
        dialog.setTruncationMode(MultiProgressDialog.TruncationMode.START);
        String truncatedStart = dialog.truncString(overLengthMessage);

        // THEN it should have been truncated correctly in both cases:
        String expectedDefault = overLengthMessage.substring(0, MultiProgressDialog.LABEL_LENGTH_CUTOFF - 3) + "...";
        String expectedStart = "..." + overLengthMessage.substring(overLengthMessage.length() - MultiProgressDialog.LABEL_LENGTH_CUTOFF + 3);
        assertEquals(expectedDefault, truncatedDefault);
        assertEquals(expectedStart, truncatedStart);

        // AND the resulting message should be exactly LABEL_LENGTH_CUTOFF characters long:
        assertEquals(MultiProgressDialog.LABEL_LENGTH_CUTOFF, truncatedDefault.length());
        assertEquals(MultiProgressDialog.LABEL_LENGTH_CUTOFF, truncatedStart.length());
    }

    @Test
    public void formatMessage_withDefaultFormat_shouldFormatCorrectly() {
        // GIVEN a message and progress values:
        String message = "Processing item";
        int current = 5;
        int total = 10;

        // WHEN we format the message with default settings:
        MultiProgressDialog dialog = new MultiProgressDialog(null, "test");
        String formatted = dialog.formatMessage(message, current, total);

        // THEN the formatted message should include progress:
        String expected = "[5 of 10] Processing item";
        assertEquals(expected, formatted);
    }

    @Test
    public void formatMessage_withLegacyFormat_shouldFormatOldStyle() {
        // GIVEN a message and progress values:
        String message = "Processing item";
        int current = 3;
        int total = 8;

        // WHEN we format the message with legacy format:
        MultiProgressDialog dialog = new MultiProgressDialog(null, "test");
        dialog.setFormatString(MultiProgressDialog.LEGACY_PROGRESS_FORMAT);
        String formatted = dialog.formatMessage(message, current, total);

        // THEN the formatted message should use legacy style:
        String expected = "Processing item (3 of 8)";
        assertEquals(expected, formatted);
    }

    @Test
    public void formatMessage_withCustomStyle_shouldFollowCustomStyle() {
        // GIVEN a message and progress values:
        String message = "Loading data";
        int current = 7;
        int total = 14;

        // WHEN we format the message with a custom format:
        MultiProgressDialog dialog = new MultiProgressDialog(null, "test");
        dialog.setFormatString("Progress: %s/%t - %m - what %% is that?");
        String formatted = dialog.formatMessage(message, current, total);

        // THEN the formatted message should follow the custom style:
        String expected = "Progress: 7/14 - Loading data - what % is that?";
        assertEquals(expected, formatted);
    }

    @Test
    public void formatMessage_withLongLogMessageAndDefaultTruncationMode_shouldTruncateEnd() {
        // GIVEN a long log message that exceeds the truncation limit:
        String longLogMessage = "This is a very long log message that is intended to exceed the truncation limit set in the MultiProgressDialog class for testing purposes.";

        // WHEN we format the message with default truncation mode:
        MultiProgressDialog dialog = new MultiProgressDialog(null, "test");
        String formatted = dialog.formatMessage(longLogMessage, 1, 1);

        // THEN the formatted message should be truncated at the end, WITHOUT affecting the step count information:
        int expectedLength = MultiProgressDialog.LABEL_LENGTH_CUTOFF - "[1 of 1] ".length() - 3;
        String expected = "[1 of 1] " + longLogMessage.substring(0, expectedLength) + "...";
        assertEquals(expected, formatted);
        assertTrue(formatted.length() <= MultiProgressDialog.LABEL_LENGTH_CUTOFF);
    }

    @Test
    public void formatMessage_withLongLogMessageAndStartTruncationMode_shouldTruncateStart() {
        // GIVEN a long log message that exceeds the truncation limit:
        String longLogMessage = "This is a very long log message that is intended to exceed the truncation limit set in the MultiProgressDialog class for testing purposes.";

        // WHEN we set truncation mode to START and format the message:
        MultiProgressDialog dialog = new MultiProgressDialog(null, "test");
        dialog.setTruncationMode(MultiProgressDialog.TruncationMode.START);
        String formatted = dialog.formatMessage(longLogMessage, 1, 1);

        // THEN the formatted message should be truncated at the start, WITHOUT affecting the step count information:
        int expectedLength = MultiProgressDialog.LABEL_LENGTH_CUTOFF - "[1 of 1] ".length() - 3;
        String expected = "[1 of 1] " + "..." + longLogMessage.substring(longLogMessage.length() - expectedLength);
        assertEquals(expected, formatted);
        assertTrue(formatted.length() <= MultiProgressDialog.LABEL_LENGTH_CUTOFF);
    }

    @Test
    public void formatMessage_withLegacyFormatAndLongLogMessage_shouldTruncateCorrectly() {
        // GIVEN a long log message that exceeds the truncation limit:
        String longLogMessage = "This is a very long log message that is intended to exceed the truncation limit set in the MultiProgressDialog class for testing purposes.";

        // WHEN we set legacy format and format the message:
        MultiProgressDialog dialog = new MultiProgressDialog(null, "test");
        dialog.setFormatString(MultiProgressDialog.LEGACY_PROGRESS_FORMAT);
        String formatted = dialog.formatMessage(longLogMessage, 2, 5);

        // THEN the formatted message should be truncated at the end, WITHOUT affecting the step count information:
        int expectedLength = MultiProgressDialog.LABEL_LENGTH_CUTOFF - " (2 of 5)".length() - 3;
        String expected = longLogMessage.substring(0, expectedLength) + "... (2 of 5)";
        assertEquals(expected, formatted);
        assertTrue(formatted.length() <= MultiProgressDialog.LABEL_LENGTH_CUTOFF);
    }

    @Test
    public void formatMessage_withLegacyFormatAndLongLogMessageAndStartTruncation_shouldTruncateStart() {
        // GIVEN a long log message that exceeds the truncation limit:
        String longLogMessage = "This is a very long log message that is intended to exceed the truncation limit set in the MultiProgressDialog class for testing purposes.";

        // WHEN we set legacy format, set truncation mode to START, and format the message:
        MultiProgressDialog dialog = new MultiProgressDialog(null, "test");
        dialog.setFormatString(MultiProgressDialog.LEGACY_PROGRESS_FORMAT);
        dialog.setTruncationMode(MultiProgressDialog.TruncationMode.START);
        String formatted = dialog.formatMessage(longLogMessage, 2, 5);

        // THEN the formatted message should be truncated at the start, WITHOUT affecting the step count information:
        int expectedLength = MultiProgressDialog.LABEL_LENGTH_CUTOFF - " (2 of 5)".length() - 3;
        String expected = "..." + longLogMessage.substring(longLogMessage.length() - expectedLength) + " (2 of 5)";
        assertEquals(expected, formatted);
        assertTrue(formatted.length() <= MultiProgressDialog.LABEL_LENGTH_CUTOFF);
    }

    @Test
    public void formatMessage_withLongMessageAndMultiDigitStepCounts_shouldTruncateCorrectly() {
        // GIVEN a long message that exceeds the truncation limit:
        String longMessage = "This is a very long message that is intended to exceed the truncation limit set in the MultiProgressDialog class for testing purposes.";

        // WHEN we format the message with multi-digit step counts:
        MultiProgressDialog dialog = new MultiProgressDialog(null, "test");
        String formatted = dialog.formatMessage(longMessage, 123, 456);

        // THEN the formatted message should be truncated correctly without cutting off step counts:
        int expectedLength = MultiProgressDialog.LABEL_LENGTH_CUTOFF - "[123 of 456] ".length() - 3;
        String expected = "[123 of 456] " + longMessage.substring(0, expectedLength) + "...";
        assertEquals(expected, formatted);
        assertTrue(formatted.length() <= MultiProgressDialog.LABEL_LENGTH_CUTOFF);
    }
}
