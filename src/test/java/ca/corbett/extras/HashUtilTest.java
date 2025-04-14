package ca.corbett.extras;

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
 * @author scorbo2
 * @since 2017-11-06
 */
public class HashUtilTest {

    private final String[] inputs = {
            "Hello",
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890",
            ""
    };

    private final String[] results = {
            "f7ff9e8b7bb2e09b70935a5d785e0cc5d9d0abf0",
            "bb814cf15cf9478829e6b85205824b0f1fd8ca08",
            "da39a3ee5e6b4b0d3255bfef95601890afd80709"
    };


    public HashUtilTest() {
    }


    @Test
    public void testGetHashString_withInputStrings_shouldHash() {
        System.out.println("HashUtilTest: getHashString(String)");
        for (int i = 0; i < inputs.length; i++) {
            String result = HashUtil.getHashString(inputs[i].getBytes());
            assertEquals(results[i], result);
        }
    }


    @Test
    public void testGetHashString_withInputFile_shouldHash() {
        System.out.println("HashUtilTest: getHashString(File)");
        for (int i = 0; i < inputs.length; i++) {
            try {
                File file = File.createTempFile("HashUtilTest", ".tmp");
                file.deleteOnExit();
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                writer.write(inputs[i]);
                writer.flush();
                writer.close();
                String result = HashUtil.getHashString(file);
                assertEquals(results[i], result);
            }
            catch (IOException ioe) {
                fail(ioe.getMessage());
            }
        }
    }
}
