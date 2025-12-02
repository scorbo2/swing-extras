package ca.corbett.extras.audio;

/**
 * Provides a way to listen for events on an AudioWaveformPanel.
 * <p>
 *     <B>NOTE:</B> AudioWaveformPanel will ensure that the callbacks in this
 *     interface are invoked on the Swing Event Dispatch Thread. It's safe to
 *     update UI components in these callbacks.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2018-01-20
 */
public interface AudioPanelListener {

    /**
     * Indicates a state change within the panel.
     * <ul>
     * <li><b>IDLE</b> - the panel is neither playing nor recording.
     * <li><b>PLAYING</b> - the panel is playing audio.
     * <li><b>RECORDING</b> - the panel is recording audio.
     * </ul>
     *
     * @param sourcePanel The AudioWaveformPanel that triggered this event.
     * @param state       The new state of the panel.
     */
    void stateChanged(AudioWaveformPanel sourcePanel, AudioWaveformPanel.PanelState state);

    /**
     * Indicates that a clip has been recorded within the panel and is available for playback or saving.
     *
     * @param sourcePanel The AudioWaveformPanel that triggered this event.
     */
    void recordingComplete(AudioWaveformPanel sourcePanel);

    /**
     * Indicates that an audio clip has been loaded into the panel. This event is also triggered
     * after recording a clip, as the newly recorded clip is loaded into the panel.
     *
     * @param sourcePanel The AudioWaveformPanel that triggered this event.
     */
    void audioLoaded(AudioWaveformPanel sourcePanel);

    /**
     * In addition to stateChanged, this message will also be triggered when audio
     * stops playing, to provide information about why the audio stopped.
     *
     * @param stopReason Indicates whether the audio was interrupted, hit its limit, or exhausted itself.
     */
    void audioStopped(PlaybackThread.StopReason stopReason);
}
