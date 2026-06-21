package ca.corbett.extras.io;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileWatcherTest {

    @TempDir
    File tempDir;

    private FileWatcher watcher;

    @AfterEach
    void tearDown() {
        if (watcher != null) {
            watcher.stop();
        }
    }

    // ---- constructor validation ----

    @Test
    void constructor_withNullFile_shouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                     () -> new FileWatcher(null, () -> {
                     }));
    }

    @Test
    void constructor_withNullCallback_shouldThrowException() throws IOException {
        File file = File.createTempFile("fwt", ".txt", tempDir);
        assertThrows(IllegalArgumentException.class,
                     () -> new FileWatcher(file, null));
    }

    @Test
    void constructor_withFileHavingNoParent_shouldThrowException() {
        // A File with no parent path component has getParentFile() == null:
        File noParent = new File("orphan.txt");
        assertThrows(IllegalArgumentException.class,
                     () -> new FileWatcher(noParent, () -> {
                     }));
    }

    // ---- change detection ----

    @Test
    void start_thenModifyWatchedFile_shouldInvokeCallback() throws Exception {
        // GIVEN a watched file and a latch to wait for the callback:
        File file = File.createTempFile("fwt", ".txt", tempDir);
        CountDownLatch latch = new CountDownLatch(1);

        watcher = new FileWatcher(file, latch::countDown);
        watcher.start();

        // WHEN the watched file is modified:
        Files.writeString(file.toPath(), "hello world");

        // THEN the callback should be invoked within a reasonable timeout:
        boolean called = latch.await(3, TimeUnit.SECONDS);
        assertTrue(called, "Callback should have been invoked after file modification");
    }

    @Test
    void start_thenDeleteWatchedFile_shouldInvokeCallback() throws Exception {
        // GIVEN a watched file:
        File file = File.createTempFile("fwt", ".txt", tempDir);
        CountDownLatch latch = new CountDownLatch(1);

        watcher = new FileWatcher(file, latch::countDown);
        watcher.start();

        // WHEN the watched file is deleted:
        Files.delete(file.toPath());

        // THEN the callback should be invoked:
        boolean called = latch.await(3, TimeUnit.SECONDS);
        assertTrue(called, "Callback should have been invoked after file deletion");
    }

    @Test
    void start_thenModifyUnrelatedFile_shouldNotInvokeCallback() throws Exception {
        // GIVEN a watched file and an unrelated file in the same directory:
        File watchedFile = File.createTempFile("fwt-watched", ".txt", tempDir);
        File otherFile = File.createTempFile("fwt-other", ".txt", tempDir);
        AtomicInteger callCount = new AtomicInteger(0);

        watcher = new FileWatcher(watchedFile, callCount::incrementAndGet);
        watcher.start();

        // WHEN only the unrelated file is modified:
        Files.writeString(otherFile.toPath(), "some change");

        // THEN the callback should NOT be invoked (wait beyond the debounce window):
        Thread.sleep(FileWatcher.DEBOUNCE_DELAY_MS + 500);
        assertEquals(0, callCount.get(), "Callback should not fire for unrelated file changes");
    }

    @Test
    void start_withRapidMultipleModifications_shouldCoalesceIntoSingleCallback() throws Exception {
        // GIVEN a watched file:
        File file = File.createTempFile("fwt", ".txt", tempDir);
        AtomicInteger callCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);

        watcher = new FileWatcher(file, () -> {
            callCount.incrementAndGet();
            latch.countDown();
        });
        watcher.start();

        // WHEN the file is modified multiple times in rapid succession (within the debounce window):
        for (int i = 0; i < 5; i++) {
            Files.writeString(file.toPath(), "content " + i);
            Thread.sleep(30); // faster than DEBOUNCE_DELAY_MS
        }

        // THEN the callback should be invoked only once (debounced):
        boolean called = latch.await(3, TimeUnit.SECONDS);
        assertTrue(called, "Callback should have been invoked at least once");
        assertEquals(1, callCount.get(), "Rapid modifications should be coalesced into a single callback");
    }

    // ---- suppression ----

    @Test
    void ignoreSelfTriggeredChanges_shouldSuppressSubsequentCallback() throws Exception {
        // GIVEN a watched file:
        File file = File.createTempFile("fwt", ".txt", tempDir);
        AtomicInteger callCount = new AtomicInteger(0);

        watcher = new FileWatcher(file, callCount::incrementAndGet);
        watcher.start();

        // WHEN we suppress self-triggered changes and then modify the file:
        watcher.ignoreSelfTriggeredChanges();
        Files.writeString(file.toPath(), "self-written content");

        // THEN the callback should NOT be invoked within the suppression window:
        Thread.sleep(FileWatcher.DEBOUNCE_DELAY_MS + 500);
        assertEquals(0, callCount.get(), "Self-triggered change should be suppressed");
    }

    @Test
    void ignoreSelfTriggeredChanges_afterSuppressionWindowExpires_shouldAllowNextCallback() throws Exception {
        // GIVEN a watched file:
        File file = File.createTempFile("fwt", ".txt", tempDir);
        AtomicInteger callCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);

        watcher = new FileWatcher(file, () -> {
            callCount.incrementAndGet();
            latch.countDown();
        });
        watcher.start();

        // WHEN we suppress, wait past the window, then modify:
        watcher.ignoreSelfTriggeredChanges(200);
        Thread.sleep(400);
        Files.writeString(file.toPath(), "external content");

        // THEN the callback SHOULD be invoked now that the window has expired:
        boolean called = latch.await(3, TimeUnit.SECONDS);
        assertTrue(called, "Callback should fire after suppression window has expired");
        assertEquals(1, callCount.get());
    }

    @Test
    void ignoreSelfTriggeredChanges_withInvalidSuppressionWindow_shouldThrowException() throws Exception {
        File file = File.createTempFile("fwt", ".txt", tempDir);
        watcher = new FileWatcher(file, () -> {
        });

        assertThrows(IllegalArgumentException.class, () -> watcher.ignoreSelfTriggeredChanges(0));
        assertThrows(IllegalArgumentException.class, () -> watcher.ignoreSelfTriggeredChanges(-1));
    }

    // ---- stop ----

    @Test
    void stop_shouldPreventFurtherCallbacks() throws Exception {
        // GIVEN a running watcher:
        File file = File.createTempFile("fwt", ".txt", tempDir);
        AtomicInteger callCount = new AtomicInteger(0);

        watcher = new FileWatcher(file, callCount::incrementAndGet);
        watcher.start();

        // WHEN the watcher is stopped and then the file is modified:
        watcher.stop();
        Files.writeString(file.toPath(), "post-stop content");

        // THEN no callback should be fired:
        Thread.sleep(FileWatcher.DEBOUNCE_DELAY_MS + 500);
        assertEquals(0, callCount.get(), "Callback should not fire after watcher is stopped");
    }

    @Test
    void stop_calledBeforeStart_shouldNotThrow() {
        // GIVEN a watcher that was never started:
        File file = new File(tempDir, "not-created.txt");
        watcher = new FileWatcher(file, () -> {
        });

        // WHEN stop() is called, THEN no exception should be thrown:
        watcher.stop();
    }
}
