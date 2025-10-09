package ca.corbett.extras.crypt;

import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


/**
 * Unit tests for HashUtil class.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2017-11-06
 */
public class HashUtilTest {

    private final String[] inputs = {
            "Hello",
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890",
            ""
    };

    private final String[] hashStrings = {
            "f7ff9e8b7bb2e09b70935a5d785e0cc5d9d0abf0",
            "bb814cf15cf9478829e6b85205824b0f1fd8ca08",
            "da39a3ee5e6b4b0d3255bfef95601890afd80709"
    };

    private final String[] hexEncoded = {
            "48656c6c6f",
            "4142434445464748494a4b4c4d4e4f505152535455565758595a6162636465666768696a6b6c6d6e6f707172737475767778797a31323334353637383930",
            ""
    };


    public HashUtilTest() {
    }


    @Test
    public void testGetHashString_withInputStrings_shouldHash() {
        for (int i = 0; i < inputs.length; i++) {
            String result = HashUtil.getHashString(inputs[i].getBytes());
            assertEquals(hashStrings[i], result);
        }
    }


    @Test
    public void testGetHashString_withInputFile_shouldHash() {
        for (int i = 0; i < inputs.length; i++) {
            try {
                File file = File.createTempFile("HashUtilTest", ".tmp");
                file.deleteOnExit();
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                writer.write(inputs[i]);
                writer.flush();
                writer.close();
                String result = HashUtil.getHashString(file);
                assertEquals(hashStrings[i], result);
            }
            catch (IOException ioe) {
                fail(ioe.getMessage());
            }
        }
    }

    @Test
    public void byteArrayToHexString_withValidInput_shouldEncode() {
        for (int i = 0; i < inputs.length; i++) {
            assertEquals(hexEncoded[i], HashUtil.byteArrayToHexString(inputs[i].getBytes()));
        }
    }
}
