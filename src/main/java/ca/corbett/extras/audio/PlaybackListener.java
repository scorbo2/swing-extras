package ca.corbett.extras.audio;

/**
 * Provides a way to listen for events from a PlaybackThread.
 * <P>
 *     <B>IMPORTANT:</B> The callbacks in this listener will be invoked from the worker thread!
 *     If you need to update a Swing UI component as a result of one of these callbacks, you need
 *     to marshal that call to the Swing Event Dispatching Thread, like this:
 * </P>
 * <pre>
 * &#64;Override
 * boolean updateProgress(long curMillis, long totalMillis) {
 *     // Most operations are fine to do here on the worker thread.
 *     // For example, computing a user-friendly string describing current progress:
 *     String text = buildProgressTextLabel(curMillis, totalMillis);
 *
 *     // BUT! Now we need to display it in a Swing UI component:
 *     SwingUtilities.invokeLater(() -> { // marshal to EDT
 *        myStatusLabel.setText(text);
 *     });
 * }
 * </pre>
 * <p>
 *     Failure to do this may result in deadlocks or other threading issues, as Swing
 *     itself is not thread-safe.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2018-01-10
 */
public interface PlaybackListener {

    /**
     * Invoked when playback starts. Also invoked if playback is resumed on a thread that was
     * previously paused or stopped.
     */
    void started();

    /**
     * Invoked when playback stops, either due to programmatic stop or by hitting the end of the
     * audio stream.
     *
     * @param stopReason Contains information about why the clip stopped playing.
     */
    void stopped(PlaybackThread.StopReason stopReason);

    /**
     * Invoked about once per second while playback is in progress. The current position, in
     * milliseconds, and the total length of the clip, in milliseconds, is provided.
     * You can return true here to continue playback, or false to tell the
     * PlaybackThread to stop playing.
     *
     * @param curMillis   The current play offset, in milliseconds.
     * @param totalMillis The total runtime of the current audio stream, in milliseconds.
     * @return True to continue playing, false to stop.
     */
    boolean updateProgress(long curMillis, long totalMillis);
}
