package ca.corbett.extras.io;

import ca.corbett.extras.progress.SimpleProgressListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileScannerThreadTest {

    @TempDir
    private File tempDir;

    @BeforeEach
    public void setup() throws Exception {
        FileSystemUtil.writeStringToFile("", new File(tempDir, "topfile1.txt"));

        File subdir1 = new File(tempDir, "subdir1");
        File subdir2 = new File(tempDir, "subdir2");
        subdir1.mkdirs();
        subdir2.mkdirs();
        FileSystemUtil.writeStringToFile("", new File(subdir1, "file1.txt"));
        FileSystemUtil.writeStringToFile("", new File(subdir1, "file2.log"));
        FileSystemUtil.writeStringToFile("", new File(subdir2, "file3.txt"));
        FileSystemUtil.writeStringToFile("", new File(subdir2, "file4.log"));

        File subsubdir1 = new File(subdir1, "subsubdir1");
        subsubdir1.mkdirs();
        FileSystemUtil.writeStringToFile("", new File(subsubdir1, "file5.txt"));
    }

    @Test
    public void run_withInvalidRootDir_shouldAbort() {
        // GIVEN a FileScannerThread with an invalid root directory:
        FileScannerThread thread = new FileScannerThread(null);
        SimpleProgressListener listener = Mockito.mock(SimpleProgressListener.class);
        Mockito.when(listener.progressUpdate(Mockito.anyInt(), Mockito.anyString())).thenReturn(true);
        thread.addProgressListener(listener);

        // WHEN we simulate running the thread:
        thread.run();

        // THEN our listener should have been notified of the error:
        Mockito.verify(listener).progressError(Mockito.any(String.class), Mockito.any(String.class));

        // AND the thread should have no results:
        assertEquals(0, thread.getResults().size());

        // AND our listener should have received a progressComplete notification exactly once:
        Mockito.verify(listener, Mockito.times(1)).progressComplete();
    }

    @Test
    public void run_withAllDefaultSettings_shouldReturnEverything() {
        // GIVEN a FileScannerThread with no explicit settings other than rootDir:
        FileScannerThread thread = new FileScannerThread(tempDir);
        SimpleProgressListener listener = Mockito.mock(SimpleProgressListener.class);
        Mockito.when(listener.progressUpdate(Mockito.anyInt(), Mockito.anyString())).thenReturn(true);
        thread.addProgressListener(listener);

        // WHEN we simulate running the thread:
        thread.run();

        // THEN our listener should have been notified of the progress beginning with the correct total:
        Mockito.verify(listener).progressBegins(6);

        // AND the thread should have returned all files:
        assertEquals(6, thread.getResults().size());

        // AND our listener should have received a progressComplete notification exactly once:
        Mockito.verify(listener, Mockito.times(1)).progressComplete();
    }

    @Test
    public void run_withRecursiveFalse_shouldReturnOnlyTopLevelFiles() {
        // GIVEN a FileScannerThread with recursive set to false:
        FileScannerThread thread = new FileScannerThread(tempDir).setRecursive(false);
        SimpleProgressListener listener = Mockito.mock(SimpleProgressListener.class);
        Mockito.when(listener.progressUpdate(Mockito.anyInt(), Mockito.anyString())).thenReturn(true);
        thread.addProgressListener(listener);

        // WHEN we simulate running the thread:
        thread.run();

        // THEN our listener should have been notified of the progress beginning with the correct total:
        //   (actually receives this twice - once for the indeterminate step, then again with the count):
        Mockito.verify(listener, Mockito.times(2)).progressBegins(1);

        // AND the thread should have returned our single top-level file:
        assertEquals(1, thread.getResults().size());

        // AND our listener should have received a progressComplete notification exactly once:
        Mockito.verify(listener, Mockito.times(1)).progressComplete();
    }

    @Test
    public void run_withExtensionFilter_shouldReturnOnlyMatchingFiles() {
        // GIVEN a FileScannerThread with an extension filter:
        FileScannerThread thread = new FileScannerThread(tempDir).addExtensionsToMatch(List.of("txt"));
        SimpleProgressListener listener = Mockito.mock(SimpleProgressListener.class);
        Mockito.when(listener.progressUpdate(Mockito.anyInt(), Mockito.anyString())).thenReturn(true);
        thread.addProgressListener(listener);

        // WHEN we simulate running the thread:
        thread.run();

        // THEN our listener should have been notified of the progress beginning with the correct total:
        Mockito.verify(listener).progressBegins(6);

        // AND the thread should have returned only the .txt files:
        assertEquals(4, thread.getResults().size());
    }

    @Test
    public void run_withExtensionFilterAndInvertedSearch_shouldReturnOnlyNonMatchingFiles() {
        // GIVEN a FileScannerThread with an extension filter and inverted search:
        FileScannerThread thread = new FileScannerThread(tempDir)
                .addExtensionsToMatch(List.of("txt"))
                .setInvertSearch(true);
        SimpleProgressListener listener = Mockito.mock(SimpleProgressListener.class);
        Mockito.when(listener.progressUpdate(Mockito.anyInt(), Mockito.anyString())).thenReturn(true);
        thread.addProgressListener(listener);

        // WHEN we simulate running the thread:
        thread.run();

        // THEN our listener should have been notified of the progress beginning with the correct total:
        Mockito.verify(listener).progressBegins(6);

        // AND the thread should have returned only the non-.txt files:
        assertEquals(2, thread.getResults().size());
    }

    @Test
    public void run_invertedSearchWithNoExtensions_shouldReturnEverything() {
        // GIVEN a FileScannerThread with inverted search but no extensions to match:
        FileScannerThread thread = new FileScannerThread(tempDir)
                .setInvertSearch(true);
        SimpleProgressListener listener = Mockito.mock(SimpleProgressListener.class);
        Mockito.when(listener.progressUpdate(Mockito.anyInt(), Mockito.anyString())).thenReturn(true);
        thread.addProgressListener(listener);

        // WHEN we simulate running the thread:
        thread.run();

        // THEN our listener should have been notified of the progress beginning with the correct total:
        Mockito.verify(listener).progressBegins(6);

        // AND the thread should have returned all files (since we're inverting an empty filter):
        assertEquals(6, thread.getResults().size());
    }

    @Test
    public void run_withMultipleExtensions_shouldReturnMatchingFiles() {
        // GIVEN a FileScannerThread with multiple extensions to match:
        FileScannerThread thread = new FileScannerThread(tempDir)
                .addExtensionsToMatch(List.of("txt", "log"));
        SimpleProgressListener listener = Mockito.mock(SimpleProgressListener.class);
        Mockito.when(listener.progressUpdate(Mockito.anyInt(), Mockito.anyString())).thenReturn(true);
        thread.addProgressListener(listener);

        // WHEN we simulate running the thread:
        thread.run();

        // THEN our listener should have been notified of the progress beginning with the correct total:
        Mockito.verify(listener).progressBegins(6);

        // AND the thread should have returned all .txt and .log files:
        assertEquals(6, thread.getResults().size());
    }

    @Test
    public void run_withNoMatchingExtensions_shouldFilterAll() {
        // GIVEN a FileScannerThread with an extension filter that matches nothing:
        FileScannerThread thread = new FileScannerThread(tempDir)
                .addExtensionsToMatch(List.of("pdf"));
        SimpleProgressListener listener = Mockito.mock(SimpleProgressListener.class);
        Mockito.when(listener.progressUpdate(Mockito.anyInt(), Mockito.anyString())).thenReturn(true);
        thread.addProgressListener(listener);

        // WHEN we simulate running the thread:
        thread.run();

        // THEN our listener should have been notified of the progress beginning with the correct total:
        Mockito.verify(listener).progressBegins(6);

        // AND the thread should have returned no files (since none match the filter):
        assertEquals(0, thread.getResults().size());
    }

    @Test
    public void run_withCallerCancellation_shouldCancelSearch() {
        // GIVEN a FileScannerThread with a listener that cancels the search:
        FileScannerThread thread = new FileScannerThread(tempDir);
        SimpleProgressListener listener = Mockito.mock(SimpleProgressListener.class);
        Mockito.when(listener.progressUpdate(Mockito.anyInt(), Mockito.anyString())).thenReturn(false);
        thread.addProgressListener(listener);

        // WHEN we simulate running the thread:
        thread.run();

        // THEN our listener should have been notified of the progress beginning with the correct total:
        Mockito.verify(listener).progressBegins(6);

        // AND the thread should only receive one result, since the search was canceled
        //     immediately after finding the first file:
        assertEquals(1, thread.getResults().size());

        // AND our listener should have received a progressCanceled notification exactly once:
        Mockito.verify(listener, Mockito.times(1)).progressCanceled();

        // AND our listener should NOT have received a progressComplete notification:
        Mockito.verify(listener, Mockito.never()).progressComplete();
    }

    @Test
    public void addExtensionsToMatch_withMixedCaseExtensions_shouldNormalizeThem() {
        // GIVEN a FileScannerThread with mixed-case extensions to match:
        FileScannerThread thread = new FileScannerThread(tempDir)
                .addExtensionsToMatch(List.of("TxT", "LoG", "txt", "TXT"));

        // WHEN we ask for the list of extensions:
        List<String> extensions = thread.getExtensionsToMatch();

        // THEN they should have been normalized and deduplicated:
        assertEquals(2, extensions.size());
        assertTrue(extensions.contains(".txt"));
        assertTrue(extensions.contains(".log"));
    }

    @Test
    public void addExtensionsToMatch_invokedMultipleTimes_shouldAddTogether() {
        // GIVEN a FileScannerThread with multiple calls to setExtensionsToMatch:
        FileScannerThread thread = new FileScannerThread(tempDir)
                .addExtensionsToMatch(List.of("txt"))
                .addExtensionsToMatch(List.of("log"))
                .addExtensionsToMatch(List.of("pdf"));

        // WHEN we ask for the list of extensions:
        List<String> extensions = thread.getExtensionsToMatch();

        // THEN they should have been combined together into one list:
        assertEquals(3, extensions.size());
        assertTrue(extensions.contains(".txt"));
        assertTrue(extensions.contains(".log"));
        assertTrue(extensions.contains(".pdf"));
    }

    @Test
    public void clearExtensionsToMatch_shouldRemoveAllExtensions() {
        // GIVEN a FileScannerThread with some extensions to match:
        FileScannerThread thread = new FileScannerThread(tempDir)
                .addExtensionsToMatch(List.of("txt", "log"));

        // WHEN we clear the extensions:
        thread.clearExtensionsToMatch();

        // THEN the list of extensions should be empty:
        List<String> extensions = thread.getExtensionsToMatch();
        assertEquals(0, extensions.size());
    }
}
