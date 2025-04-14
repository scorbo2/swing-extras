package ca.corbett.extras.logging;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for Stopwatch class.
 *
 * @author scorbo2
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

}
