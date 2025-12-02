package ca.corbett.extras.crypt;

import ca.corbett.extras.io.FileSystemUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SignatureUtilTest {

    @Test
    public void signFile_givenValidData_shouldSign() throws Exception {
        // GIVEN some file to sign and a key to sign it with:
        File dataFile = File.createTempFile("SignatureUtilTest", ".txt");
        dataFile.deleteOnExit();
        FileSystemUtil.writeStringToFile("Hello", dataFile);
        KeyPair keyPair = SignatureUtil.generateKeyPair();

        // WHEN we sign it:
        File signatureFile = File.createTempFile("SignatureUtilTest", ".txt");
        signatureFile.deleteOnExit();
        byte[] signatureData = SignatureUtil.signFile(dataFile, keyPair.getPrivate());
        SignatureUtil.saveSignature(signatureData, signatureFile);

        // THEN we should see a signature file:
        assertTrue(signatureFile.exists());
        assertTrue(signatureFile.length() > 0);

        // AND we should be able to parse it out and get the exact same signature:
        byte[] actualData = SignatureUtil.loadSignature(signatureFile);
        assertArrayEquals(signatureData, actualData);
    }

    @Test
    public void verifyFile_withOriginalFile_shouldVerify() throws Exception {
        // GIVEN some file to sign and a key to sign it with:
        File dataFile = File.createTempFile("SignatureUtilTest", ".txt");
        dataFile.deleteOnExit();
        FileSystemUtil.writeStringToFile("Hello", dataFile);
        KeyPair keyPair = SignatureUtil.generateKeyPair();
        File signatureFile = File.createTempFile("SignatureUtilTest", ".txt");
        signatureFile.deleteOnExit();
        byte[] signatureData = SignatureUtil.signFile(dataFile, keyPair.getPrivate());
        SignatureUtil.saveSignature(signatureData, signatureFile);

        // WHEN we try to verify the signature:
        boolean result = SignatureUtil.verifyFile(dataFile, signatureData, keyPair.getPublic());

        // THEN we should see it validated:
        assertTrue(result);
    }

    @Test
    public void verifyFile_withModifiedFile_shouldNotVerify() throws Exception {
        // GIVEN some file to sign and a key to sign it with:
        File dataFile = File.createTempFile("SignatureUtilTest", ".txt");
        dataFile.deleteOnExit();
        FileSystemUtil.writeStringToFile("Hello", dataFile);
        KeyPair keyPair = SignatureUtil.generateKeyPair();
        File signatureFile = File.createTempFile("SignatureUtilTest", ".txt");
        signatureFile.deleteOnExit();
        byte[] signatureData = SignatureUtil.signFile(dataFile, keyPair.getPrivate());
        SignatureUtil.saveSignature(signatureData, signatureFile);

        // WHEN we modify the file after signing it:
        FileSystemUtil.writeStringToFile("Hackerman modified your file!", dataFile);
        boolean verified = SignatureUtil.verifyFile(dataFile, signatureData, keyPair.getPublic());

        // THEN it should fail to verify:
        assertFalse(verified);
    }

    @Test
    public void saveKeyPair_givenValidKeyPair_shouldSave() throws Exception {
        // GIVEN a valid key pair:
        KeyPair keyPair = SignatureUtil.generateKeyPair();

        // WHEN we save it:
        File privateKeyFile = File.createTempFile("SignatureUtilTest", ".key");
        File publicKeyFile = File.createTempFile("SignatureUtilTest", ".key");
        SignatureUtil.saveKeyPair(keyPair, privateKeyFile, publicKeyFile);

        // THEN the files should be generated:
        assertTrue(privateKeyFile.exists());
        assertTrue(publicKeyFile.exists());
        assertTrue(privateKeyFile.length() > 0);
        assertTrue(publicKeyFile.length() > 0);

        // cleanup:
        privateKeyFile.delete();
        publicKeyFile.delete();
    }

    @Test
    public void loadPublicKey_givenValidPublicKey_shouldSave() throws Exception {
        // GIVEN a valid public key:
        KeyPair keyPair = SignatureUtil.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        File keyFile = File.createTempFile("SignatureUtilTest", ".key");
        keyFile.deleteOnExit();
        SignatureUtil.savePublicKey(publicKey, keyFile);

        // WHEN we try to load it:
        PublicKey actual = SignatureUtil.loadPublicKey(keyFile);

        // THEN we should get a valid key with no exceptions:
        assertNotNull(actual);
        assertEquals(publicKey, actual);
    }

    @Test
    public void loadPrivateKey_givenValidPublicKey_shouldSave() throws Exception {
        // GIVEN a valid public key:
        KeyPair keyPair = SignatureUtil.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        File keyFile = File.createTempFile("SignatureUtilTest", ".key");
        keyFile.deleteOnExit();
        SignatureUtil.savePrivateKey(privateKey, keyFile);

        // WHEN we try to load it:
        PrivateKey actual = SignatureUtil.loadPrivateKey(keyFile);

        // THEN we should get a valid key with no exceptions:
        assertNotNull(actual);
        assertEquals(privateKey, actual);
    }
}