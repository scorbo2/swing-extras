package ca.corbett.extras.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Watches a single file on disk for external changes (modify, delete, or create)
 * and invokes a caller-supplied callback when a change is detected.
 * <p>
 * Uses Java NIO {@link WatchService} to monitor the directory that contains
 * the watched file.  Only events for the exact target file are forwarded;
 * changes to other files in the same directory are silently ignored.
 * </p>
 * <p>
 * A debounce mechanism coalesces rapid bursts of events (such as those produced
 * by editors that truncate-then-rewrite, or by IDEs that perform atomic saves
 * via a temp-file rename) into a single callback invocation.
 * </p>
 * <p>
 * The watcher runs on dedicated daemon threads and never blocks the Swing EDT.
 * The supplied callback is invoked directly on the debouncer thread; callers
 * that need to update Swing components should wrap their callback with
 * {@link javax.swing.SwingUtilities#invokeLater}.
 * </p>
 * <p>
 * Self-triggered events (events caused by the application saving the file
 * itself) can be suppressed by calling {@link #ignoreSelfTriggeredChanges()}
 * immediately <em>before</em> initiating the write operation, so that the
 * suppression window is guaranteed to be in effect before the WatchService
 * event arrives.
 * </p>
 * <p>
 * <b>A note about OVERFLOW events:</b> If the native event queue gets too
 * full, we may receive an OVERFLOW event here, which means "something changed
 * in the directory, but we don't know exactly what". This code takes a
 * conservative approach and will treat this as a change to the file under
 * watch, even though some other file in the same directory may have triggered it.
 * This means that we may occasionally fire a false positive (callback invoked
 * when the watched file didn't actually change). The alternative would be
 * to ignore it, which would risk a false negative (failure to report an actual change).
 * Neither option is great, but the conservative approach is the safer one.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 3.0.0 (absorbed from CryptText)
 */
public class FileWatcher {

    private static final Logger log = Logger.getLogger(FileWatcher.class.getName());

    /** Debounce window in milliseconds – events arriving within this window are coalesced. */
    static final long DEBOUNCE_DELAY_MS = 300;

    /** How long (ms) to suppress events after a self-initiated save. */
    private static final long SUPPRESS_DURATION_MS = 1000;

    private final File watchedFile;
    private final Runnable onChange;

    private WatchService watchService;
    private ScheduledExecutorService debouncer;
    private final AtomicReference<ScheduledFuture<?>> pendingEvent = new AtomicReference<>();
    private volatile Thread watchThread;
    private volatile boolean running;
    private volatile long suppressUntil = 0;

    /**
     * Creates a new FileWatcher that will watch the given file.
     * Call {@link #start()} to begin watching.
     * <p>
     * <b>IMPORTANT REMINDER:</b> Your callback will be invoked on a background thread!
     * If you need to update any UI component, you must marshal back to the Swing EDT using
     * {@link javax.swing.SwingUtilities#invokeLater}. Failure to do so may result
     * in subtle and hard-to-debug concurrency issues in your UI. Java Swing is not thread safe!
     * </p>
     *
     * @param file     The file to watch. Must not be null and must have a parent directory.
     * @param onChange The callback invoked (on the debouncer thread) when a relevant change
     *                 is detected on disk. Must not be null.
     * @throws IllegalArgumentException if {@code file} or {@code onChange} is null,
     *                                  or if {@code file} has no parent directory.
     */
    public FileWatcher(File file, Runnable onChange) {
        if (file == null) {
            throw new IllegalArgumentException("file cannot be null");
        }
        if (file.getParentFile() == null) {
            throw new IllegalArgumentException("file must have a parent directory");
        }
        if (onChange == null) {
            throw new IllegalArgumentException("onChange cannot be null");
        }
        this.watchedFile = file.getAbsoluteFile();
        this.onChange = onChange;
    }

    /**
     * Starts watching the file. Does nothing if the watcher is already running.
     *
     * @throws IOException if the underlying {@link WatchService} cannot be created or
     *                     if the parent directory cannot be registered.
     */
    public void start() throws IOException {
        if (running) {
            return;
        }
        try {
            watchService = FileSystems.getDefault().newWatchService();
            debouncer = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "FileWatcher-debouncer-" + watchedFile.getName());
                t.setDaemon(true);
                return t;
            });
            Path dir = watchedFile.getParentFile().toPath();
            dir.register(watchService,
                         StandardWatchEventKinds.ENTRY_MODIFY,
                         StandardWatchEventKinds.ENTRY_DELETE,
                         StandardWatchEventKinds.ENTRY_CREATE);
            running = true;
            watchThread = new Thread(this::watchLoop, "FileWatcher-" + watchedFile.getName());
            watchThread.setDaemon(true);
            watchThread.start();
            log.fine("FileWatcher started for: " + watchedFile.getAbsolutePath());
        }
        catch (IOException e) {
            stop(); // clean up any partially-initialized resources
            throw e;
        }
    }

    /**
     * Returns true if this watcher is currently active.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Stops watching the file and releases all associated resources.
     * Safe to call even if the watcher was never started.
     */
    public void stop() {
        running = false;
        ScheduledFuture<?> pending = pendingEvent.getAndSet(null);
        if (pending != null) {
            pending.cancel(false);
        }
        if (watchService != null) {
            try {
                watchService.close();
            }
            catch (IOException e) {
                log.log(Level.WARNING, "Error closing WatchService for: " + watchedFile.getAbsolutePath(), e);
            }
        }
        if (debouncer != null) {
            debouncer.shutdownNow();
        }
        if (watchThread != null) {
            watchThread.interrupt();
        }
        log.fine("FileWatcher stopped for: " + watchedFile.getAbsolutePath());
    }

    /**
     * Suppresses the next change event(s) for a short window after a self-initiated write.
     * Call this immediately <em>before</em> initiating the write operation, so that the
     * suppression window is guaranteed to be in effect before the WatchService event arrives.
     * Any already-pending debounced event is also cancelled.
     */
    public void ignoreSelfTriggeredChanges() {
        suppressUntil = System.currentTimeMillis() + SUPPRESS_DURATION_MS;
        ScheduledFuture<?> pending = pendingEvent.getAndSet(null);
        if (pending != null) {
            pending.cancel(false);
        }
    }

    // -------------------------------------------------------------------------
    // Internal implementation
    // -------------------------------------------------------------------------

    private void watchLoop() {
        try {
            while (running) {
                WatchKey key;
                try {
                    key = watchService.take();
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                catch (ClosedWatchServiceException e) {
                    break;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        // *something* just changed in the directory under watch,
                        // but unfortunately we don't know what. It might have been
                        // some other file, or it might have been the one we're watching.
                        // Our options are:
                        //   1) ignore it, and possibly miss a relevant change
                        //   2) treat it as a change to our file, even though it might not have been.
                        // Neither option is great, but the safest option is 2,
                        // so we will schedule a callback just in case.
                        scheduleDebounced();
                        continue; // OVERFLOW events have no context, so let's avoid an NPE in the code below.
                    }

                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> pathEvent = (WatchEvent<Path>)event;
                    Path changedPath = ((Path)key.watchable()).resolve(pathEvent.context());

                    if (changedPath.toAbsolutePath().equals(watchedFile.toPath().toAbsolutePath())) {
                        scheduleDebounced();
                    }
                }

                if (!key.reset()) {
                    break;
                }
            }
        }
        finally {
            cleanupAfterWatchLoopExit();
        }
    }

    private void cleanupAfterWatchLoopExit() {
        running = false;

        ScheduledFuture<?> pending = pendingEvent.getAndSet(null);
        if (pending != null) {
            pending.cancel(false);
        }

        debouncer.shutdownNow();

        try {
            watchService.close();
        }
        catch (IOException | ClosedWatchServiceException ignored) {
            // Already closed or no further cleanup possible.
        }
    }

    private void scheduleDebounced() {
        if (!running) {
            return;
        }
        ScheduledFuture<?> old = pendingEvent.getAndSet(null);
        if (old != null) {
            old.cancel(false);
        }
        try {
            ScheduledFuture<?> next = debouncer.schedule(() -> {
                if (running && System.currentTimeMillis() >= suppressUntil) {
                    onChange.run();
                }
            }, DEBOUNCE_DELAY_MS, TimeUnit.MILLISECONDS);
            pendingEvent.set(next);
        }
        catch (RejectedExecutionException ignored) {
            // Executor was shut down concurrently; nothing to schedule.
        }
    }
}
