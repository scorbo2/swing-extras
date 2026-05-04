package ca.corbett.extras.io;

import ca.corbett.extras.progress.SimpleProgressListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DirectoryScannerThreadTest {

    @TempDir
    private File tempDir;

    @BeforeEach
    public void setup() throws Exception {
        FileSystemUtil.writeStringToFile("", new File(tempDir, "topfile1.txt"));

        File subdir1 = new File(tempDir, "subdir1");
        File subdir2 = new File(tempDir, "subdir2");
        subdir1.mkdirs();
        subdir2.mkdirs();

        File subsubdir1 = new File(subdir1, "subsubdir1");
        File subsubdir2 = new File(subdir1, "subsubdir2");
        subsubdir1.mkdirs();
        subsubdir2.mkdirs();

        File subsubsubdir1 = new File(subsubdir1, "subsubsubdir1");
        subsubsubdir1.mkdirs();
    }

    @Test
    public void run_withInvalidRootDir_shouldAbort() {
        // GIVEN a DirectoryScannerThread with an invalid root directory:
        DirectoryScannerThread thread = new DirectoryScannerThread(null);
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
        // GIVEN a DirectoryScannerThread with no explicit settings other than rootDir:
        DirectoryScannerThread thread = new DirectoryScannerThread(tempDir);
        SimpleProgressListener listener = Mockito.mock(SimpleProgressListener.class);
        Mockito.when(listener.progressUpdate(Mockito.anyInt(), Mockito.anyString())).thenReturn(true);
        thread.addProgressListener(listener);

        // WHEN we simulate running the thread:
        thread.run();

        // THEN our listener should have been notified of the progress beginning with the correct total:
        Mockito.verify(listener).progressBegins(5);

        // AND the thread should have returned all directories:
        assertEquals(5, thread.getResults().size());

        // AND our listener should have received a progressComplete notification exactly once:
        Mockito.verify(listener, Mockito.times(1)).progressComplete();
    }

    @Test
    public void run_withRecursiveFalse_shouldReturnOnlyTopLevelDirectories() {
        // GIVEN a DirectoryScannerThread with recursive set to false:
        DirectoryScannerThread thread = new DirectoryScannerThread(tempDir).setRecursive(false);
        SimpleProgressListener listener = Mockito.mock(SimpleProgressListener.class);
        Mockito.when(listener.progressUpdate(Mockito.anyInt(), Mockito.anyString())).thenReturn(true);
        thread.addProgressListener(listener);

        // WHEN we simulate running the thread:
        thread.run();

        // THEN our listener should have been notified of the progress beginning with the correct total:
        Mockito.verify(listener).progressBegins(2);

        // AND the thread should have returned our two top-level dirs:
        assertEquals(2, thread.getResults().size());

        // AND our listener should have received a progressComplete notification exactly once:
        Mockito.verify(listener, Mockito.times(1)).progressComplete();
    }
}
