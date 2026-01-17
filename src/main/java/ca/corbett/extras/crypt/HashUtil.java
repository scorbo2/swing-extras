package ca.corbett.extras.crypt;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple wrapper class to simplify hashing functionality a little.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2012-08-29 (originally written for ICE and then generalized much later)
 */
public class HashUtil {

    private static final Logger log = Logger.getLogger(HashUtil.class.getName());

    public enum HashType {
        MD2("MD2"),
        MD5("MD5"),
        SHA1("SHA-1"),
        SHA256("SHA-256"),
        SHA384("SHA-384"),
        SHA512("SHA-512");

        private final String label;

        HashType(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    protected final static Map<HashType, MessageDigest> hashMap;

    protected HashUtil() {
    }

    static {
        hashMap = new HashMap<>();
        for (HashType hashType : HashType.values()) {
            // Nest the try/catch inside the loop so that if one
            // fails, the others still have a chance to load:
            try {
                hashMap.put(hashType, MessageDigest.getInstance(hashType.toString()));
            }
            catch (NoSuchAlgorithmException nsae) {
                // The above are all guaranteed to us by the java standard, so this *should* be okay
                log.log(Level.SEVERE, "Digest algorithm not available: " + hashType, nsae);
            }
        }
        for (MessageDigest digest : hashMap.values()) {
            digest.reset();
        }
    }

    /**
     * Returns a digest of the data in the given byte array using the given HashType.
     *
     * @param hashType The HashType to use.
     * @param data     A byte array of any length.
     * @return A hash of the input array using the specified digest algorithm.
     */
    public static byte[] getHash(final HashType hashType, final byte[] data) {
        MessageDigest digest = hashMap.get(hashType);
        return digest.digest(data);
    }

    /**
     * Returns a digest of the data in the given byte array using SHA-1.
     *
     * @param data A byte array of any length.
     * @return A SHA-1 hash of the input array.
     */
    public static byte[] getHash(final byte[] data) {
        return getHash(HashType.SHA1, data);
    }

    /**
     * Returns a digest of the contents of the given file, using the given HashType.
     *
     * @param hashType The HashType to use.
     * @param file     The input file. Must exist and be readable.
     * @return A hash of the file contents, in the specified digest type.
     * @throws IOException If the file does not exist or is not readable.
     */
    public static byte[] getHash(final HashType hashType, final File file) throws IOException {
        if (!file.exists() || !file.canRead() || file.isDirectory()) {
            throw new IOException("File " + file.getName() + " does not exist or cannot be read.");
        }
        try (BufferedInputStream is = new BufferedInputStream(new FileInputStream(file))) {
            MessageDigest digest = hashMap.get(hashType);
            byte[] buffer = new byte[1024 * 1024 * 4]; // 4MB buffer
            int len;
            while ((len = is.read(buffer)) != -1) {
                digest.update(buffer, 0, len);
            }
            return digest.digest();
        }
    }

    /**
     * Returns a digest of the contents of the given file. File must exist and
     * be readable. Uses SHA-1 as the default hash type.
     *
     * @param file The input file. Must exist and be readable.
     * @return A SHA-1 hash of the file contents.
     * @throws IOException If the file does not exist or is not readable.
     */
    public static byte[] getHash(final File file) throws IOException {
        return getHash(HashType.SHA1, file);
    }

    /**
     * Hashes the given byte array using the given HashType and then returns a printable
     * string (hex format) of the output.
     *
     * @param hashType The HashType to use.
     * @param data     A byte array of any length.
     * @return A hex-encoded printable string equivalent to the hash of the input.
     */
    public static String getHashString(final HashType hashType, final byte[] data) {
        return byteArrayToHexString(getHash(hashType, data));
    }

    /**
     * Hashes the given byte array and then returns a printable string
     * (hex format) of the given byte array, using SHA-1 as the hash type.
     *
     * @param data A byte array of any length.
     * @return A hex-encoded printable string equivalent to the hash of the input.
     */
    public static String getHashString(final byte[] data) {
        return byteArrayToHexString(getHash(HashType.SHA1, data));
    }

    /**
     * Returns a printable string (hex format) of the hash of the given file contents,
     * using the given HashType.
     *
     * @param hashType The HashType to use.
     * @param file     The input file in question. Must exist and be readable.
     * @return A hex-encoded printable string of the hash of the file contents.
     * @throws IOException If the file does not exist or is not readable.
     */
    public static String getHashString(final HashType hashType, final File file) throws IOException {
        return byteArrayToHexString(getHash(hashType, file));
    }

    /**
     * Returns a printable string (hex format) of the hash of the given file contents.
     *
     * @param file The input file in question. Must exist and be readable.
     * @return A hex-encoded printable string equivalent of the hash of the file contents.
     * @throws IOException If the file does not exist or is not readable.
     */
    public static String getHashString(final File file) throws IOException {
        return byteArrayToHexString(getHash(HashType.SHA1, file));
    }

    /**
     * Converts a byte array into a hex-encoded printable string.
     * As of swing-extras 2.4, this method simply delegates to the java.util.HexFormat class,
     * which is available as of Java 17.
     *
     * @param data An input array of any length.
     * @return A printable hex-encoded string equivalent.
     */
    public static String byteArrayToHexString(final byte[] data) {
        return HexFormat.of().formatHex(data);
    }
}
