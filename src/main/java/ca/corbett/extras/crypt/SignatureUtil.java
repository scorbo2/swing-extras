package ca.corbett.extras.crypt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This utility class provides convenient wrappers for creating public/private key pairs,
 * using a private key to sign a file, and using a public key to verify a file signature.
 * <p>
 * <b>How do I sign a file?</b> - You generate a key pair, save the public/private keys,
 * and then use the private key to sign the file.
 * </p>
 * <pre>
 * KeyPair keyPair = SignatureUtil.generateKeyPair();
 * SignatureUtil.saveKeyPair(keyPair, privateKeyFile, publicKeyFile); // keep the private key safe!
 *
 * // Generate a signature and save it to "signatureFile":
 * SignatureUtil.signFile(dataFile, keyPair.getPrivate(), signatureFile);
 * </pre>
 * <p>
 * You can bundle signatureFile and your publicKeyFile together with the dataFile that you wish
 * to transmit. The receiver can use the public key to verify the signature and confirm that
 * dataFile has not been modified since it was signed:
 * </p>
 * <pre>
 * PublicKey publicKey = SignatureUtil.loadPublicKey(publicKeyFile);
 * boolean isValid = SignatureUtil.verifyFile(dataFile, signatureFile, publicKey);
 * if (! isValid) {
 *     throw new Exception("The signature is wrong! The file has been modified!");
 * }
 * </pre>
 * <p>
 * <b>Note:</b> there is a generateKeyPair method in this class as a convenience.
 * It is perfectly compatible with the following manual generation approach:
 * </p>
 * <pre>
 * # Generate key pair
 * ssh-keygen -t rsa -b 2048 -m PEM -f mykey -N ""
 *
 * # Convert private key to PKCS#8
 * openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in mykey -out mykey.pkcs8
 *
 * # Convert public key to X.509 PEM
 * ssh-keygen -f mykey -e -m PEM | openssl rsa -pubin -RSAPublicKey_in -pubout -out mykey_pub.pem
 * </pre>
 * <p>
 * Java is very particular about key formats, and it doesn't seem to like the usual openssh format.
 * So, we use PKCS#8 in PEM format (this is what you get out of the box with the java.security classes).
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a> with help from claude.ai
 * @since swing-extras 2.5
 */
public class SignatureUtil {

    private static final Logger log = Logger.getLogger(SignatureUtil.class.getName());

    private static final String KEY_ALGORITHM = "RSA"; // Could also use EC...
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA"; // Could also use SHA256withECDSA...
    private static final int KEY_SIZE = 2048;

    private static final Map<String, String> HEADERS = Map.of(
            "PKCS1StartPublic", "-----BEGIN RSA PUBLIC KEY-----",
            "PKCS1EndPublic", "-----END RSA PUBLIC KEY-----",
            "PKCS1StartPrivate", "-----BEGIN RSA PRIVATE KEY-----",
            "PKCS1EndPrivate", "-----END RSA PRIVATE KEY-----",
            "PKCS8StartPublic", "-----BEGIN PUBLIC KEY-----",
            "PKCS8EndPublic", "-----END PUBLIC KEY-----",
            "PKCS8StartPrivate", "-----BEGIN PRIVATE KEY-----",
            "PKCS8EndPrivate", "-----END PRIVATE KEY-----",
            "SignatureStart", "-----START SIGNATURE-----",
            "SignatureEnd", "-----END SIGNATURE-----"
    );

    private SignatureUtil() {
    }

    /**
     * Signs the given file using the given PrivateKey.
     *
     * @param file       The file to be signed. Must exist.
     * @param privateKey The PrivateKey to use for signing.
     * @return The raw byte array of the
     */
    public static byte[] signFile(File file, PrivateKey privateKey)
            throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        if (file == null || !file.exists() || !file.canRead()) {
            throw new IOException("SignatureUtil.signFile: input file does not exist or can't be read.");
        }
        log.info("SignatureUtil: generating signature for file " + file.getAbsolutePath());

        // Read file contents
        byte[] fileData = Files.readAllBytes(file.toPath());

        // Create signature:
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(privateKey);
        signature.update(fileData);

        return signature.sign();
    }

    /**
     * Signs the given file and saves the resulting signature to the given output file.
     *
     * @param file          The file to sign. Must exist.
     * @param privateKey    The PrivateKey to use for signing.
     * @param signatureFile The file to which we'll encode and save the resulting signature data.
     */
    public static void signFile(File file, PrivateKey privateKey, File signatureFile)
            throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        byte[] signatureData = signFile(file, privateKey);
        saveSignature(signatureData, signatureFile);
    }

    /**
     * Verify a file signature using the given public key.
     *
     * @param file           The file to be verified. Must exist.
     * @param signatureBytes The raw signature data.
     * @param publicKey      The PublicKey to use for verification.
     * @return true if signature is valid, false otherwise
     */
    public static boolean verifyFile(File file, byte[] signatureBytes, PublicKey publicKey)
            throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        if (file == null || !file.exists() || !file.canRead()) {
            throw new IOException("SignatureUtil.verifyFile: input file does not exist or can't be read.");
        }
        log.info("SignatureUtil: verifying signature for file " + file.getAbsolutePath());

        // Read file contents
        byte[] fileData = Files.readAllBytes(file.toPath());

        // Verify signature
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(publicKey);
        signature.update(fileData);

        return signature.verify(signatureBytes);
    }

    public static boolean verifyFile(File file, File signatureFile, PublicKey publicKey)
            throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        if (signatureFile == null || !signatureFile.exists() || !signatureFile.canRead()) {
            throw new IOException("SignatureUtil.verifyFile: signature file does not exist or can't be read.");
        }
        return verifyFile(file, loadSignature(signatureFile), publicKey);
    }

    /**
     * Loads a signature from a PEM file
     */
    public static byte[] loadSignature(File signatureFile) throws IOException {
        log.info("SignatureUtil: loading signature from file " + signatureFile.getAbsolutePath());
        String rawData = new String(Files.readAllBytes(signatureFile.toPath()));

        // Remove PEM headers and newlines
        for (String value : HEADERS.values()) {
            rawData = rawData.replace(value, "");
        }
        rawData = rawData.replaceAll("\\s", "");

        // Base64 decode and return:
        return Base64.getDecoder().decode(rawData);
    }

    /**
     * Save a signature to a PEM file
     */
    public static void saveSignature(byte[] signatureData, File signatureFile) throws IOException {
        log.info("SignatureUtil: saving signature to file " + signatureFile.getAbsolutePath());

        // Save private key in PEM format:
        String privateKeyPEM = HEADERS.get("SignatureStart")
                + System.lineSeparator()
                + Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(signatureData)
                + System.lineSeparator()
                + HEADERS.get("SignatureEnd")
                + System.lineSeparator();
        Files.writeString(signatureFile.toPath(), privateKeyPEM);
    }


    /**
     * Generates and returns a KeyPair using default settings of RSA and size 2048.
     */
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        log.info("SignatureUtil: generating new key pair (" + KEY_ALGORITHM + ", " + KEY_SIZE + ")");
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        keyGen.initialize(KEY_SIZE);
        return keyGen.generateKeyPair();
    }

    /**
     * Saves the given KeyPair in PEM format to the given private and public files.
     *
     * @param keyPair        The KeyPair to be saved.
     * @param privateKeyFile The destination file for the private key (must be writable).
     * @param publicKeyFile  The destination file for the public key (must be writable).
     * @throws IOException If either file can't be saved.
     */
    public static void saveKeyPair(KeyPair keyPair, File privateKeyFile, File publicKeyFile) throws IOException {
        savePrivateKey(keyPair.getPrivate(), privateKeyFile);
        savePublicKey(keyPair.getPublic(), publicKeyFile);
    }

    /**
     * Load a private key from a PEM file (PKCS#8 format)
     */
    public static PrivateKey loadPrivateKey(File privateKeyFile)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        log.info("SignatureUtil: loading private key from file " + privateKeyFile.getAbsolutePath());
        String key = new String(Files.readAllBytes(privateKeyFile.toPath()));

        // Remove PEM headers and newlines
        for (String value : HEADERS.values()) {
            key = key.replace(value, "");
        }
        key = key.replaceAll("\\s", "");

        // Base64 decode
        byte[] encoded = Base64.getDecoder().decode(key);

        // Generate private key
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        KeyFactory kf = KeyFactory.getInstance(KEY_ALGORITHM);
        return kf.generatePrivate(keySpec);
    }

    /**
     * Save a private key to a PEM file (PKCS#8 format)
     */
    public static void savePrivateKey(PrivateKey privateKey, File privateKeyFile) throws IOException {
        log.info("SignatureUtil: saving private key to file " + privateKeyFile.getAbsolutePath());
        byte[] privateKeyBytes = privateKey.getEncoded(); // PKCS#8

        // Save private key in PEM format:
        String privateKeyPEM = HEADERS.get("PKCS8StartPrivate")
                + System.lineSeparator()
                + Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(privateKeyBytes)
                + System.lineSeparator()
                + HEADERS.get("PKCS8EndPrivate")
                + System.lineSeparator();
        Files.writeString(privateKeyFile.toPath(), privateKeyPEM);
    }

    /**
     * Load a public key from a PEM file (X.509 format)
     */
    public static PublicKey loadPublicKey(File publicKeyFile)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        log.info("SignatureUtil: loading public key from file " + publicKeyFile.getAbsolutePath());
        String key = new String(Files.readAllBytes(publicKeyFile.toPath()));

        // Remove PEM headers and newlines
        for (String value : HEADERS.values()) {
            key = key.replace(value, "");
        }
        key = key.replaceAll("\\s", "");

        // Base64 decode
        byte[] encoded = Base64.getDecoder().decode(key);

        // Generate public key
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        KeyFactory kf = KeyFactory.getInstance(KEY_ALGORITHM);
        return kf.generatePublic(keySpec);
    }

    /**
     * Saves a public key to a PEM file (X.509 format)
     */
    public static void savePublicKey(PublicKey publicKey, File publicKeyFile) throws IOException {
        log.info("SignatureUtil: saving public key to file " + publicKeyFile.getAbsolutePath());
        byte[] publicKeyBytes = publicKey.getEncoded();   // X.509

        // Save public key in PEM format
        String publicKeyPEM = HEADERS.get("PKCS8StartPublic")
                + System.lineSeparator()
                + Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(publicKeyBytes)
                + System.lineSeparator()
                + HEADERS.get("PKCS8EndPublic")
                + System.lineSeparator();
        Files.writeString(publicKeyFile.toPath(), publicKeyPEM);
    }
}
