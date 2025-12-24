package ca.corbett.extras.logging;


import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for Stopwatch class.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class StopwatchTest {

    public StopwatchTest() {
    }

    @Test
    public void testStartStop() {
        assertFalse(Stopwatch.isRunning("test"));
        assertEquals(0, Stopwatch.getTimerCount());
        Stopwatch.start("test");
        assertTrue(Stopwatch.isRunning("test"));
        assertEquals(1, Stopwatch.getTimerCount());
        Stopwatch.stop("test");
        assertFalse(Stopwatch.isRunning("test"));
        assertEquals(0, Stopwatch.getTimerCount());
    }

    @Test
    public void testStopAll() {
        assertEquals(0, Stopwatch.stopAll());
        Stopwatch.start("timer1");
        Stopwatch.start("timer2");
        Stopwatch.start("timer3");
        assertEquals(3, Stopwatch.getTimerCount());
        assertEquals(3, Stopwatch.stopAll());
        assertEquals(0, Stopwatch.getTimerCount());
    }

    @Test
    public void testReport() {
        Stopwatch.start("something");

        // Do something to get some time to elapse:
        int blah = 1;
        for (int i = 0; i < 1000000; i++) {
            blah = i * 2 + 3 + (int)(i / 4f);
        }
        assertNotEquals(0, blah);

        long time = Stopwatch.stop("something");
        assertNotEquals(0, time);
        assertEquals(time, Stopwatch.report("something"));
        String report = Stopwatch.reportFormatted("something");
        assertNotNull(report);
        assertNotEquals(0, report.length());
    }

    @Test
    public void testFormatTimeValue() {
        assertEquals("0ms", Stopwatch.formatTimeValue(0));
        assertEquals("1s", Stopwatch.formatTimeValue(1000));
        assertEquals("1m0s", Stopwatch.formatTimeValue(60001));
        assertEquals("1m0s", Stopwatch.formatTimeValue(60000));
        assertEquals("59s", Stopwatch.formatTimeValue(59999));
        assertEquals("1h0m0s", Stopwatch.formatTimeValue(3600000));
        assertEquals("59m59s", Stopwatch.formatTimeValue(3599999));
        assertEquals("27h45m44s", Stopwatch.formatTimeValue(99944422));
    }

    @Test
    public void testConcurrentStartStopSameId() throws Exception {
        // ensure clean state
        Stopwatch.stopAll();

        final String id = "concurrentSameId";
        final int threads = 20;
        final int iterations = 100;

        ExecutorService svc = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<Void>> futures = new ArrayList<>();

        for (int t = 0; t < threads; t++) {
            Callable<Void> task = () -> {
                startLatch.await();
                for (int i = 0; i < iterations; i++) {
                    Stopwatch.start(id);
                    try {
                        Thread.sleep(1); // small pause to allow overlapping start/stop
                    }
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    long elapsed = Stopwatch.stop(id);
                    if (elapsed < 0) {
                        throw new AssertionError("elapsed time negative");
                    }
                }
                return null;
            };
            futures.add(svc.submit(task));
        }

        // release all threads at once
        startLatch.countDown();

        // propagate any exceptions thrown in worker threads
        for (Future<Void> f : futures) {
            f.get();
        }

        svc.shutdown();

        // all timers should be stopped
        assertEquals(0, Stopwatch.getTimerCount());
    }

    @Test
    public void testConcurrentStartManyIds() throws Exception {
        // ensure clean state
        Stopwatch.stopAll();

        final int total = 200;
        final int poolSize = 50;

        ExecutorService svc = Executors.newFixedThreadPool(poolSize);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<String>> futures = new ArrayList<>();

        // submit tasks that will start unique ids when released
        for (int i = 0; i < total; i++) {
            final String id = "many-" + i + "-" + System.nanoTime();
            Callable<String> task = () -> {
                startLatch.await();
                Stopwatch.start(id);
                return id;
            };
            futures.add(svc.submit(task));
        }

        // release and wait for all starts to complete
        startLatch.countDown();
        for (Future<String> f : futures) {
            f.get();
        }

        // all timers should be present
        assertEquals(total, Stopwatch.getTimerCount());

        // stop all and verify count
        int stopped = Stopwatch.stopAll();
        assertEquals(total, stopped);
        assertEquals(0, Stopwatch.getTimerCount());

        svc.shutdownNow();
    }

}
