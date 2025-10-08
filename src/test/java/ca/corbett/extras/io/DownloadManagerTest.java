package ca.corbett.extras.io;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DownloadManagerTest {

    private static DownloadManager manager;

    @BeforeAll
    public static void setup() {
        manager = new DownloadManager();
    }

    @AfterAll
    public static void cleanup() {
        manager.close();
    }

    /**
     * <B>NOTE: THIS IS NOT A UNIT TEST!</B>
     * <p>
     * Executing this test will actually download a file from the internet,
     * which is why this test is disabled by default. It's here only for
     * troubleshooting the DownloadManager class.
     * </p>
     */
    @Test
    @Disabled
    public void downloadFile_withValidURL_shouldDownload() throws Exception {
        // GIVEN a download request for a valid file on some web server:
        DownloadListener mockListener = Mockito.mock(DownloadListener.class);
        File tmpFile = File.createTempFile("swing-extras", ".gif");
        tmpFile.delete();
        final String urlString = "https://www.corbett.ca/corbett.gif";

        try {
            // WHEN we try to download the file:
            manager.downloadFile(new URL(urlString), tmpFile, mockListener);

            // (Cheesy! give it some time to finish)
            Thread.sleep(5000);

            // THEN we should see that the file downloaded:
            assertTrue(tmpFile.exists());
            assertTrue(tmpFile.length() > 0);

            // AND our mock listener should have been notified:
            Mockito.verify(mockListener, Mockito.times(1)).downloadBegins(Mockito.any(), Mockito.any());
            Mockito.verify(mockListener, Mockito.times(1))
                   .downloadComplete(Mockito.any(), Mockito.any(), Mockito.any());

            // If the file is smaller than our buffer size, it might not have had the chance
            // to send a progress notification. Hence the "atLeast(0)", which admittedly looks odd:
            Mockito.verify(mockListener, Mockito.atLeast(0))
                   .downloadProgress(Mockito.any(), Mockito.any(), Mockito.anyLong(), Mockito.anyLong());

        }

        finally {
            // Clean up the temp directory when we're done:
            tmpFile.delete();
        }
    }

    /**
     * <B>NOTE: THIS IS NOT A UNIT TEST!</B>
     * <p>
     * Executing this test will actually try to download a file from the internet,
     * which is why this test is disabled by default. It's here only for
     * troubleshooting the DownloadManager class.
     * </p>
     */
    @Test
    @Disabled
    public void downloadFile_withInvalidURL_shouldFail() throws Exception {
        // GIVEN a download request for a file that does not exist
        DownloadListener mockListener = Mockito.mock(DownloadListener.class);
        File tmpFile = File.createTempFile("swing-extras", ".gif");
        tmpFile.delete();
        final String urlString = "https://www.corbett.ca/YabbaDabbaDonkeyDoodleDoo.gif";

        // WHEN we try to download the file:
        manager.downloadFile(new URL(urlString), tmpFile, mockListener);

        // (Cheesy! give it some time to finish)
        Thread.sleep(5000);

        // THEN we should see that the file did not download:
        assertFalse(tmpFile.exists());

        // AND our mock listener should have been notified:
        Mockito.verify(mockListener, Mockito.times(1)).downloadBegins(Mockito.any(), Mockito.any());
        Mockito.verify(mockListener, Mockito.times(1))
               .downloadFailed(Mockito.any(), Mockito.any(), Mockito.any());

        // We should never receive a progress update for a 404 not found:
        Mockito.verify(mockListener, Mockito.never())
               .downloadProgress(Mockito.any(), Mockito.any(), Mockito.anyLong(), Mockito.anyLong());
    }
}