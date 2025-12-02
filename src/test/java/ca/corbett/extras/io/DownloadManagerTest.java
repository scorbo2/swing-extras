package ca.corbett.extras.io;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    public void downloadFile_withValidLocalFileURL_shouldCopy() throws Exception {
        // GIVEN a download request for a local file that exists:
        DownloadListener mockListener = Mockito.mock(DownloadListener.class);
        File sourceFile = File.createTempFile("swing-extras", ".txt");

        // WHEN we try to download the file:
        manager.downloadFile(sourceFile.toURI().toURL(), mockListener);

        // (cheesy! give it some time to copy)
        Thread.sleep(250);

        // THEN our mock listener should have been notified:
        Mockito.verify(mockListener, Mockito.times(1)).downloadBegins(Mockito.any(), Mockito.any());
        Mockito.verify(mockListener, Mockito.times(1))
               .downloadComplete(Mockito.any(), Mockito.any(), Mockito.any());

        // We should never receive a progress update for a local file copy:
        Mockito.verify(mockListener, Mockito.never())
               .downloadProgress(Mockito.any(), Mockito.any(), Mockito.anyLong(), Mockito.anyLong());

        // Cleanup:
        sourceFile.delete();
    }

    @Test
    public void downloadFile_withInvalidLocalFileURL_shouldCopy() throws Exception {
        // GIVEN a download request for a local file that exists:
        DownloadListener mockListener = Mockito.mock(DownloadListener.class);
        File sourceFile = File.createTempFile("swing-extras", ".txt");
        sourceFile.delete(); // source file doesn't exist!

        // WHEN we try to download the file:
        manager.downloadFile(sourceFile.toURI().toURL(), mockListener);

        // (cheesy! give it some time to copy)
        Thread.sleep(250);

        // THEN our mock listener should have been notified:
        Mockito.verify(mockListener, Mockito.times(1)).downloadBegins(Mockito.any(), Mockito.any());
        Mockito.verify(mockListener, Mockito.times(1))
               .downloadFailed(Mockito.any(), Mockito.any(), Mockito.any());

        // We should never receive a progress update for a local file copy:
        Mockito.verify(mockListener, Mockito.never())
               .downloadProgress(Mockito.any(), Mockito.any(), Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    public void downloadFile_withInvalidURL_shouldFailImmediately() throws Exception {
        // GIVEN a download request in an unsupported format:
        String urlString = "ftp://example.com/blah/blah/doesnotexist.jpg";
        DownloadListener mockListener = Mockito.mock(DownloadListener.class);

        // WHEN we try to download it:
        manager.downloadFile(new URL(urlString), mockListener);

        // (cheesy! give it some time to fail)
        Thread.sleep(250);

        // THEN our mock listener should NOT have been notified of the start (this is a fast failure case):
        Mockito.verify(mockListener, Mockito.never()).downloadBegins(Mockito.any(), Mockito.any());

        // BUT we should still get notification of the failure:
        Mockito.verify(mockListener, Mockito.times(1)).downloadFailed(Mockito.any(), Mockito.any(), Mockito.any());

        // We should never receive a progress update for a local file copy:
        Mockito.verify(mockListener, Mockito.never())
               .downloadProgress(Mockito.any(), Mockito.any(), Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    public void getFileExtension() {
        assertEquals(".txt", DownloadManager.getFileExtension("hello.txt"));
        assertEquals(".jpg", DownloadManager.getFileExtension("hello.txt.jpg"));
        assertEquals("", DownloadManager.getFileExtension("hello"));
        assertEquals("", DownloadManager.getFileExtension(null));
    }

    @Test
    public void getFilenameComponent() {
        assertEquals("hello.txt", DownloadManager.getFilenameComponent("/path/to/hello.txt"));
        assertEquals("", DownloadManager.getFilenameComponent("/slash/at/end/"));
        assertEquals("cowabunga", DownloadManager.getFilenameComponent("/cowabunga"));
    }
}