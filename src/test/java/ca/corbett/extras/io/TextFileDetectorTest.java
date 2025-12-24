package ca.corbett.extras.io;

import ca.corbett.extras.logging.Stopwatch;
import org.junit.jupiter.api.Test;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class TextFileDetectorTest {

    @Test
    public void detectTextFile_withValidTextFile_shouldDetectAsText() throws Exception {
        // GIVEN a valid text file
        File tempFile = File.createTempFile("detectTextFile", ".txt");
        tempFile.deleteOnExit();
        FileSystemUtil.writeStringToFile("Hello there.", tempFile);

        // WHEN we try to detect if it's a text file
        boolean isText = TextFileDetector.isTextFile(tempFile);

        // THEN we should see that yes, it is a text file:
        assertTrue(isText);
    }

    @Test
    public void detectTextFile_withEmptyFile_shouldDetectAsText() throws Exception {
        // GIVEN an empty file
        File tempFile = File.createTempFile("detectTextFile", ".txt");
        tempFile.deleteOnExit();

        // WHEN we try to detect if it's a text file
        boolean isText = TextFileDetector.isTextFile(tempFile);

        // THEN we should see that yes, empty files are technically text files:
        assertTrue(isText);
    }

    @Test
    public void detectTextFile_givenNonExistentFile_shouldThrow() throws Exception {
        // GIVEN a file that does not exist
        File nonExistentFile = new File("this_file_does_not_exist_1234567890.txt");

        // WHEN we try to detect if it's a text file
        // THEN we should see that an exception is thrown
        try {
            TextFileDetector.isTextFile(nonExistentFile);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
            return;
        }

        fail("Expected IllegalArgumentException to be thrown");
    }

    @Test
    public void detectTextFile_withBinaryFile_shouldDetectAsBinary() throws Exception {
        // GIVEN a binary file (we'll create a temp file with some binary content)
        File tempFile = File.createTempFile("detectTextFile", ".bin");
        tempFile.deleteOnExit();
        byte[] binaryData = new byte[] {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
                                        0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F};
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempFile))) {
            outputStream.write(binaryData);
        }

        // WHEN we try to detect if it's a text file
        boolean isText = TextFileDetector.isTextFile(tempFile);

        // THEN we should see that it is detected as a binary file:
        assertFalse(isText);
    }

    @Test
    public void detectTextFile_withNullBytes_shouldInstantlyFail() throws Exception {
        // GIVEN a file with at least one null byte:
        File tempFile = File.createTempFile("detectTextFile", ".bin");
        tempFile.deleteOnExit();
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempFile))) {
            outputStream.write(new byte[] {0x00});
        }

        // WHEN we try to detect if it's a text file, even with a generous error threshold:
        boolean isText = TextFileDetector.isTextFile(tempFile, 1024, 0.5);

        // THEN we should see that it instantly failed because it saw a null byte:
        //      (our threshold of 50% is irrelevant in this case)
        assertFalse(isText);
    }

    @Test
    public void detectTextFile_withSubThresholdBinaryChars_shouldDetectAsText() throws Exception {
        // GIVEN a file with a few binary characters, but below the threshold:
        File tempFile = File.createTempFile("detectTextFile", ".txt");
        tempFile.deleteOnExit();
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempFile))) {
            // Write mostly text, with a few binary chars
            outputStream.write("Hello there!".getBytes());
            outputStream.write(new byte[] {0x01, 0x02}); // 2 binary chars
            outputStream.write(" How are you?".getBytes());
        }

        // WHEN we try to detect if it's a text file, with a threshold that allows these few binary chars:
        boolean isText = TextFileDetector.isTextFile(tempFile, 1024, 0.1); // 10% threshold

        // THEN we should see that it is detected as a text file:
        assertTrue(isText);
    }

    @Test
    public void detectTextFile_withOverThresholdBinaryChars_shouldDetectAsBinary() throws Exception {
        // GIVEN a file with enough binary characters to exceed the threshold:
        File tempFile = File.createTempFile("detectTextFile", ".bin");
        tempFile.deleteOnExit();
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempFile))) {
            // Write some text, then enough binary chars to exceed threshold
            outputStream.write("Hello there!".getBytes());
            outputStream.write(new byte[] {0x01, 0x02, 0x03, 0x04, 0x05, 0x06}); // 6 binary chars
            outputStream.write(" How are you?".getBytes());
        }

        // WHEN we try to detect if it's a text file, with a threshold that these binary chars exceed:
        boolean isText = TextFileDetector.isTextFile(tempFile, 1024, 0.05); // 5% threshold

        // THEN we should see that it is detected as a binary file:
        assertFalse(isText);
    }

    @Test
    public void detectTextFile_withUtf8Bom_shouldDetectAsText() throws Exception {
        // GIVEN a file that starts with a UTF-8 BOM followed by text:
        File tempFile = File.createTempFile("detectTextFile", ".txt");
        tempFile.deleteOnExit();
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempFile))) {
            // Write UTF-8 BOM
            outputStream.write(new byte[] {(byte)0xEF, (byte)0xBB, (byte)0xBF});
            // Write some text
            outputStream.write("Hello there!".getBytes());
        }

        // WHEN we try to detect if it's a text file:
        boolean isText = TextFileDetector.isTextFile(tempFile);

        // THEN we should see that it is detected as a text file:
        assertTrue(isText);
    }

    @Test
    public void detectTextFile_withUtf16Bom_instantFail() throws Exception {
        // GIVEN a file that starts with a UTF-16 BE BOM followed by text:
        File tempFile = File.createTempFile("detectTextFile", ".txt");
        tempFile.deleteOnExit();
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempFile))) {
            // Write UTF-16 BE BOM
            outputStream.write(new byte[] {(byte)0xFE, (byte)0xFF});
            // Write some text (in UTF-16 BE encoding)
            outputStream.write("H".getBytes("UTF-16BE"));
            outputStream.write("e".getBytes("UTF-16BE"));
            outputStream.write("l".getBytes("UTF-16BE"));
            outputStream.write("l".getBytes("UTF-16BE"));
            outputStream.write("o".getBytes("UTF-16BE"));
        }

        // WHEN we try to detect if it's a text file:
        boolean isText = TextFileDetector.isTextFile(tempFile);

        // THEN it will fail immediately because the data contains null bytes:
        assertFalse(isText);
    }

    @Test
    public void detectTextFile_withUtf16LeBom_instantFail() throws Exception {
        // GIVEN a file that starts with a UTF-16 LE BOM followed by text:
        File tempFile = File.createTempFile("detectTextFile", ".txt");
        tempFile.deleteOnExit();
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempFile))) {
            // Write UTF-16 LE BOM
            outputStream.write(new byte[] {(byte)0xFF, (byte)0xFE});
            // Write some text (in UTF-16 LE encoding)
            outputStream.write("H".getBytes("UTF-16LE"));
            outputStream.write("e".getBytes("UTF-16LE"));
            outputStream.write("l".getBytes("UTF-16LE"));
            outputStream.write("l".getBytes("UTF-16LE"));
            outputStream.write("o".getBytes("UTF-16LE"));
        }

        // WHEN we try to detect if it's a text file:
        boolean isText = TextFileDetector.isTextFile(tempFile);

        // THEN it will fail immediately because the data contains null bytes:
        assertFalse(isText);
    }

    @Test
    public void detectTextFile_withVeryLargeTextFile_shouldBePerformant() throws Exception {
        // GIVEN an unreasonably large text file:
        final int lineCount = 100_000;
        File tempFile = File.createTempFile("detectTextFile", ".txt");
        tempFile.deleteOnExit();
        List<String> lines = new ArrayList<>(lineCount);
        for (int i = 0; i < lineCount; i++) {
            lines.add("This is line number " + i + " in a very large text file.");
        }
        FileSystemUtil.writeLinesToFile(lines, tempFile);

        // WHEN we run it through the detector with a stopwatch:
        Stopwatch.start("detectTextFile_largeFile");
        boolean isText = TextFileDetector.isTextFile(tempFile, 512*1024, 0.01); // 0.5MB sample, 1% threshold
        long elapsedMillis = Stopwatch.stop("detectTextFile_largeFile");

        // THEN we should see it didn't take an unreasonable amount of time:
        //      (only takes 7ms on my laptop to read up to the first 1MB of data, but we'll allow 1 second for 0.5MB)
        assertTrue(elapsedMillis < 1000, "Text file detection took too long: " + elapsedMillis + " ms");

        // AND we should see that it is detected as a text file:
        assertTrue(isText);
    }
}