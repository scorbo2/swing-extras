package ca.corbett.extras.audio;

import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;

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
  private static final int UPDATE_MS = 75;

  private final Clip clip;
  private boolean isPlaying;
  private final long offsetms;
  private final long limitms;
  private long lastPlayPositionms;
  private final PlaybackListener listener;

  public PlaybackThread(AudioInputStream audioStream, long offset, long limit,
                        PlaybackListener listener) throws LineUnavailableException, IOException {
    this.listener = listener;
    this.offsetms = offset;
    this.limitms = limit;
    clip = (Clip)AudioSystem.getLine(new Line.Info(Clip.class));

    clip.addLineListener(new LineListener() {
      @Override
      public void update(LineEvent event) {
        if (event.getType() == LineEvent.Type.STOP) {
          isPlaying = false;
        }
      }

    });

    clip.open(audioStream);
  }

  public boolean isPlaying() {
    return isPlaying;
  }

  public void stop() {
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
    if (listener != null) {
      listener.started();
    }

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
          isPlaying = false;
        }
      }

      // Update progress every UPDATE_MS milliseconds:
      long elapsedSinceLastUpdate = System.currentTimeMillis() - lastUpdateTime;
      if (elapsedSinceLastUpdate > UPDATE_MS) {
        lastUpdateTime = System.currentTimeMillis();
        if (listener != null) {
          long curMillis = lastPlayPositionms;
          if (!listener.updateProgress(curMillis, clipTimeMillis)) {
            isPlaying = false;
          }
        }
      }
    }

    // Notify listeners that we're stopped:
    if (listener != null) {
      listener.stopped();
    }

    clip.stop();
    clip.close();
  }

}
