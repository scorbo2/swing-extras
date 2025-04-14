package ca.corbett.extras.audio;

/**
 * Provides a way to listen for events from a PlaybackThread.
 *
 * @author scorbo2
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
