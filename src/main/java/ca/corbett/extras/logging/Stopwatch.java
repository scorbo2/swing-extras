package ca.corbett.extras.logging;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides static utility methods for tracking how long operations take, for logging
 * and diagnostic purposes.
 * <h2>Starting timers</h2>
 * <p>
 * You can have as many timers running simultaneously as you wish, memory permitting.
 * Each time you invoke Stopwatch.start() with a unique timer string id, a new timer
 * is added. If you invoke Stopwatch.start() with the id of a timer that is already in
 * progress, that timer is restarted and its previous value is lost.
 * </p>
 * <h2>Monitoring timers in progress</h2>
 * <p>
 * You can report on the status of a running timer without stopping it by invoking
 * the report() or reportFormatted() method. You can check to see if a timer is
 * currently running using the isRunning() method, and you can see how many timers
 * are currently running via the getTimerCount() method.
 * </p>
 * <h2>Stopping a timer</h2>
 * <p>
 * Calling Stopwatch.stop() will stop a timer and mark how much time it counted
 * while it was running. At any point after stopping a timer, you can invoke report()
 * or reportFormatted() to get this information. A stopped timer can be restarted
 * by passing its id to the start() method.
 * </p>
 * <h2>Formatting timing information</h2>
 * <p>
 * The raw timer results can be retrieved by report(), which simply returns the number
 * of milliseconds for which the timer in question ran. If you want a more human-readable
 * version of this information, you can invoke reportFormatted() and get a friendlier
 * string version, along the lines of "1h24m32s". Note that fractional second values
 * are ignored by reportFormatted(). If higher precision is required, use the report()
 * method instead.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2023-03-17
 */
public abstract class Stopwatch {

    /**
     * Tracks timers that are currently running. This maps a String identifier
     * to the System.currentTimeMillis() value when the timer was started.
     * When a timer is stopped, its id is moved from this map to the RESULTS map,
     * and the value is changed from the start time to the elapsed time.
     */
    private static final Map<String, Long> TIMERS = new ConcurrentHashMap<>();

    /**
     * Tracks timing results for timers that have been stopped. This maps a String
     * identifier to a number of milliseconds that the timer was running.
     */
    private static final Map<String, Long> RESULTS = new ConcurrentHashMap<>();

    /**
     * Starts a timer with the given identifier. If a timer with that identifier is
     * already running, it is restarted.
     *
     * @param id Any String which uniquely identifies this timer. Null values are ignored.
     */
    public static void start(String id) {
        if (id != null) {
            TIMERS.put(id, System.currentTimeMillis());
            RESULTS.remove(id);
        }
    }

    /**
     * Stops the timer with the given identifier, and notes the count of milliseconds
     * for which that timer was running. The elapsed time in milliseconds is returned
     * from this method, but can also be queried via the report methods. If the given
     * id does not reference a currently running timer, this method does nothing
     * and will return 0.
     *
     * @param id The unique String identifier of the timer in question.
     * @return The number of milliseconds for which the timer in question was running.
     */
    public static long stop(String id) {
        long elapsedTime = 0;
        // atomically remove the start time (if any)
        Long startTime = TIMERS.remove(id);
        if (startTime != null) {
            elapsedTime = System.currentTimeMillis() - startTime;
            RESULTS.put(id, elapsedTime);
        }
        return elapsedTime;
    }

    /**
     * Stops all timers, and returns a count of how many timers were affected.
     *
     * @return How many timers were stopped by this call.
     */
    public static int stopAll() {
        List<String> timerIds = new ArrayList<>(TIMERS.keySet());
        int timerCount = 0;
        for (String id : timerIds) {
            stop(id);
            timerCount++;
        }
        return timerCount;
    }

    /**
     * Returns a count of how many timers are currently running.
     *
     * @return How many timers are currently running.
     */
    public static int getTimerCount() {
        return TIMERS.size();
    }

    /**
     * Indicates whether a timer with the given id is currently running.
     *
     * @param id The identifier of the timer in question.
     * @return true if the named timer exists and is running.
     */
    public static boolean isRunning(String id) {
        return TIMERS.containsKey(id);
    }

    /**
     * Reports on the timer with the given identifier. If the identifier refers to a timer
     * that is currently running, then the return value is the number of milliseconds for
     * which the timer in question has been running. The timer will not be stopped as a result
     * of this call. If the identifier refers to a timer that has been stopped, then the
     * result is the number of milliseconds for which that timer ran before it was stopped.
     * If the given identifier does not match any known timer, then this method does nothing
     * and will return 0.
     *
     * @param id The identifier of the timer in question.
     * @return A count of milliseconds for the given timer, as described above.
     */
    public static long report(String id) {
        long elapsedTime = 0;

        Long startTime = TIMERS.get(id);
        if (startTime != null) {
            elapsedTime = System.currentTimeMillis() - startTime;
        }
        else {
            Long res = RESULTS.get(id);
            if (res != null) {
                elapsedTime = res;
            }
        }

        return elapsedTime;
    }

    /**
     * Reports on the timer with the given identifier. This is similar to the report
     * method except that the return value here is a formatted, human-readable
     * string describing the elapsed time. For example: "1m24s" instead of 84000.
     *
     * @param id The identifier of the timer in question.
     * @return A human-readable string representing the elapsed time.
     */
    public static String reportFormatted(String id) {
        return formatTimeValue(report(id));
    }

    /**
     * Takes a count of milliseconds and returns it in a human-readable format along the
     * lines of "1h24m32s". Extremely short values (less than one second) are returned
     * in the format "44ms".
     *
     * @param timeValue An arbitrary millisecond count.
     * @return A formatted String that describes how long the given timeValue is.
     */
    public static String formatTimeValue(long timeValue) {

        // special case for very fast values:
        if (timeValue < 1000) {
            return timeValue + "ms";
        }

        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        while (timeValue >= 3600000) {
            timeValue -= 3600000;
            hours++;
        }
        while (timeValue >= 60000) {
            timeValue -= 60000;
            minutes++;
        }
        while (timeValue >= 1000) {
            timeValue -= 1000;
            seconds++;
        }

        String hoursStr = "";
        String minutesStr = "";
        String secondsStr = "";
        String remainderStr = "";
        if (hours > 0) {
            hoursStr = hours + "h";
        }
        if (minutes > 0 || hours > 0) {
            minutesStr = minutes + "m";
        }
        if (seconds > 0 || minutes > 0 || hours > 0) {
            secondsStr = seconds + "";
        }
        if (secondsStr.isEmpty()) {
            secondsStr = "0";
        }

    /*
    Do we care about fractional second values? Eh, maybe not.
    float remainder = timeValue / 1000;
    int remainderDigits = (int)(remainder * 100);
    if (remainderDigits != 0) {
      remainderStr = "." + String.format("%2d", remainderDigits);
    }
     */
        return hoursStr + minutesStr + secondsStr + remainderStr + "s";
    }

}
