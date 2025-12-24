package ca.corbett.extras.io;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * This utility class can be used to quickly "guess" if a file is likely a text file
 * based on the presence of non-printable characters in a sample of its content.
 * It uses a simple heuristic that counts the number of non-printable characters
 * in the first N bytes of the file, and if the ratio of non-printable characters
 * exceeds a certain threshold, it classifies the file as binary.
 * <p>
 * This detector works well for single-byte encodings (ASCII, UTF-8, ISO-8859-1, etc.)
 * but will classify UTF-16 and UTF-32 encoded files as binary due to their embedded
 * null bytes. For more comprehensive encoding detection, consider using a library like
 * Apache Tika or ICU4J.
 * </p>
 * <p>
 * The detection algorithm:
 * <ul>
 *   <li>Reads a sample of bytes from the beginning of the file</li>
 *   <li>Immediately rejects files containing null bytes (0x00)</li>
 *   <li>Counts non-printable control characters (excluding common whitespace)</li>
 *   <li>Classifies as text if non-printable ratio is below threshold</li>
 * </ul>
 * </p>
 *
 * @author claude.ai
 * @since swing-extras 2.6
 */
public class TextFileDetector {

    private static final int DEFAULT_SAMPLE_SIZE = 8192; // 8KB
    private static final double DEFAULT_THRESHOLD = 0.02; // 2%

    /**
     * Detects if a file is likely a text file using default settings.
     * <p>
     * Note: This method is optimized for single-byte encodings (ASCII, UTF-8, etc.)
     * and will classify UTF-16/UTF-32 files as binary.
     * </p>
     *
     * @param file the file to check
     * @return true if the file appears to be a text file
     * @throws IOException if an I/O error occurs
     */
    public static boolean isTextFile(File file) throws IOException {
        return isTextFile(file, DEFAULT_SAMPLE_SIZE, DEFAULT_THRESHOLD);
    }

    /**
     * Detects if a file is likely a text file with configurable parameters.
     * <p>
     * Note: This method is optimized for single-byte encodings (ASCII, UTF-8, etc.)
     * and will classify UTF-16/UTF-32 files as binary.
     * </p>
     *
     * @param file the file to check
     * @param sampleSize number of bytes to read for analysis
     * @param nonPrintableThreshold maximum ratio of non-printable characters (0.0 to 1.0)
     * @return true if the file appears to be a text file
     * @throws IOException if an I/O error occurs
     */
    public static boolean isTextFile(File file, int sampleSize, double nonPrintableThreshold)
            throws IOException {

        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("File does not exist or is not a regular file");
        }

        byte[] buffer = new byte[sampleSize];
        int bytesRead;

        try (BufferedInputStream is = new BufferedInputStream(new FileInputStream(file))) {
            bytesRead = is.read(buffer, 0, sampleSize);

            if (bytesRead == -1) {
                return true; // Empty file is considered a text file
            }
        }

        // Skip UTF-8 BOM if present
        int startIndex = 0;
        if (bytesRead >= 3 &&
                (buffer[0] & 0xFF) == 0xEF &&
                (buffer[1] & 0xFF) == 0xBB &&
                (buffer[2] & 0xFF) == 0xBF) {
            startIndex = 3;
        }
        // Skip UTF-16 BE BOM - this will fail due to null bytes, but we skip it anyway
        else if (bytesRead >= 2 &&
                (buffer[0] & 0xFF) == 0xFE &&
                (buffer[1] & 0xFF) == 0xFF) {
            startIndex = 2;
        }
        // Skip UTF-16 LE BOM - this will fail due to null bytes, but we skip it anyway
        else if (bytesRead >= 2 &&
                (buffer[0] & 0xFF) == 0xFF &&
                (buffer[1] & 0xFF) == 0xFE) {
            startIndex = 2;
        }

        // Check for null bytes - strong indicator of binary content
        for (int i = startIndex; i < bytesRead; i++) {
            if (buffer[i] == 0) {
                return false;
            }
        }

        // Count non-printable characters
        int nonPrintableCount = 0;
        for (int i = startIndex; i < bytesRead; i++) {
            byte b = buffer[i];

            // Check for control characters (0-31), excluding whitespace characters
            if (b >= 0 && b < 32) {
                // Allow common text control characters
                if (b != '\n' && b != '\r' && b != '\t' && b != '\f') {
                    nonPrintableCount++;
                }
            }
            // For bytes in the range 128-255, we're more lenient as they could be
            // valid UTF-8 continuation bytes or extended ASCII
            // Very high ratios of these would still fail the threshold check
        }

        // Calculate the ratio and compare to threshold
        double nonPrintableRatio = (double) nonPrintableCount / (bytesRead - startIndex);
        return nonPrintableRatio <= nonPrintableThreshold;
    }

    /**
     * Builder for configurable text file detection.
     */
    public static class Builder {
        private int sampleSize = DEFAULT_SAMPLE_SIZE;
        private double threshold = DEFAULT_THRESHOLD;

        public Builder sampleSize(int sampleSize) {
            if (sampleSize <= 0) {
                throw new IllegalArgumentException("Sample size must be positive");
            }
            this.sampleSize = sampleSize;
            return this;
        }

        public Builder threshold(double threshold) {
            if (threshold < 0.0 || threshold > 1.0) {
                throw new IllegalArgumentException("Threshold must be between 0.0 and 1.0");
            }
            this.threshold = threshold;
            return this;
        }

        public boolean detect(File file) throws IOException {
            return isTextFile(file, sampleSize, threshold);
        }
    }

    // Example usage
//    public static void main(String[] args) throws IOException {
//        File testFile = new File("example.txt");
//
//        // Simple usage with defaults
//        boolean isText = TextFileDetector.isTextFile(testFile);
//        System.out.println("Is text file (default): " + isText);
//
//        // Custom parameters
//        boolean isTextCustom = TextFileDetector.isTextFile(testFile, 4096, 0.01);
//        System.out.println("Is text file (custom): " + isTextCustom);
//
//        // Using builder
//        boolean isTextBuilder = new TextFileDetector.Builder()
//                .sampleSize(16384)
//                .threshold(0.03)
//                .detect(testFile);
//        System.out.println("Is text file (builder): " + isTextBuilder);
//    }
}