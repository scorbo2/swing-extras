package ca.corbett.extras.audio;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides a worker thread that can be used to play audio clips. You can instantiate
 * this directly, but it's easier to go through the various play() wrapper functions
 * in AudioUtil to generate and start the thread for you.
 *
 * @author scorbo2
 * @since 2018-01-10
 */
public class PlaybackThread implements Runnable {

  // Update progress interval, in milliseconds. Lower is more frequent, but also more costly.
  protected static final int UPDATE_MS = 75;

  public enum StopReason {
    /**
     * The audio stopped because the clip ran to the very end.
     **/
    AUDIO_EXHAUSTED,

    /**
     * The audio stopped because it was interrupted by a call to stop() or because a listener signalled us to stop.
     **/
    INTERRUPTED,

    /**
     * The audio stopped because a limit was configured when we started, and we hit that limit.
     **/
    LIMIT_REACHED
  }

  protected final Clip clip;
  protected volatile boolean isPlaying;
  protected final long offsetms;
  protected final long limitms;
  protected long lastPlayPositionms;
  protected final List<PlaybackListener> listeners;
  protected volatile StopReason stopReason;

  public PlaybackThread(AudioInputStream audioStream, long offset, long limit,
                        PlaybackListener listener) throws LineUnavailableException, IOException {
    this.listeners = new ArrayList<>();
    if (listener != null) {
      listeners.add(listener);
    }
    this.offsetms = offset;
    this.limitms = limit;
    clip = (Clip)AudioSystem.getLine(new Line.Info(Clip.class));

    clip.addLineListener(event -> {
      if (event.getType() == LineEvent.Type.STOP) {
        isPlaying = false;
      }
    });

    clip.open(audioStream);
  }

  public boolean isPlaying() {
    return isPlaying;
  }

  public void stop() {
    stopReason = StopReason.INTERRUPTED;
    isPlaying = false;
  }

  /**
   * Returns the current offset, in milliseconds, of the playback.
   * If the playback is stopped, this value will represent the last playback position.
   *
   * @return The current offset, in ms, of the playback.
   */
  public long getCurrentOffset() {
    return lastPlayPositionms;
  }

  @Override
  public void run() {
    stopReason = null;

    // If given a starting offset, set it now before calling clip.start()
    clip.setMicrosecondPosition(offsetms * 1000);

    // Start the clip and wait for it to spin up:
    clip.start();
    int timeout = 10;
    while (!clip.isActive() || !clip.isRunning()) {
      try {
        Thread.sleep(50);
      }
      catch (InterruptedException ignored) {
      }
      timeout--;
      if (timeout <= 0) {
        break; // don't let it just loop forever if something went wrong
      }
    }
    isPlaying = true;

    // Notify listeners that we're starting up:
    fireStartedEvent();

    long startTime = System.currentTimeMillis();
    long lastUpdateTime = startTime - UPDATE_MS; // Ensure immediate update
    long clipTimeMillis = (long)(clip.getMicrosecondLength() / 1000f);
    while (isPlaying) {

      // Kludge alert: getMicrosecondPosition seems pretty unreliable, especially for the
      // first second or two of playback. So, we'll instead rely on tracking how many
      // milliseconds have elapsed since this loop started. This should be reasonably
      // close if not exact.
      //lastPlayPositionms = (long)(clip.getMicrosecondPosition() / 1000f);
      lastPlayPositionms = offsetms + (System.currentTimeMillis() - startTime);

      // If a limit is set, don't play past the limit:
      if (limitms > 0) {
        if (lastPlayPositionms >= limitms) {
          stopReason = StopReason.LIMIT_REACHED;
          isPlaying = false;
        }
      }

      // Update progress every UPDATE_MS milliseconds:
      long elapsedSinceLastUpdate = System.currentTimeMillis() - lastUpdateTime;
      if (elapsedSinceLastUpdate > UPDATE_MS) {
        if (!fireProgressEvent(lastPlayPositionms, clipTimeMillis)) {
          stopReason = StopReason.INTERRUPTED;
          isPlaying = false;
        }
        lastUpdateTime = System.currentTimeMillis();
      }
    }

    // If no stop reason was set above, then we must
    // have just hit the end of the clip:
    if (stopReason == null) {
      stopReason = StopReason.AUDIO_EXHAUSTED;
    }

    // Notify listeners that we're stopped:
    fireStopEvent(stopReason);

    clip.stop();
    clip.close();
  }

  public void addPlaybackListener(PlaybackListener listener) {
    if (listener != null) {
      listeners.add(listener);
    }
  }

  public void removePlaybackListener(PlaybackListener listener) {
    if (listener != null) {
      listeners.remove(listener);
    }
  }

  protected void fireStartedEvent() {
    for (PlaybackListener listener : listeners) {
      listener.started();
    }
  }

  protected boolean fireProgressEvent(long current, long total) {
    boolean shouldContinue = true;
    for (PlaybackListener listener : listeners) {
      shouldContinue = shouldContinue && listener.updateProgress(current, total);
    }
    return shouldContinue;
  }

  protected void fireStopEvent(StopReason stopReason) {
    for (PlaybackListener listener : listeners) {
      listener.stopped(stopReason);
    }
  }
}
