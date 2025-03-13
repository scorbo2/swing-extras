package ca.corbett.extras.audio;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

/**
 * Provides a worker thread that can be used to capture audio and record it.
 *
 * @author scorbo2
 * @since 2018-01-20
 */
public class RecordThread implements Runnable {

  private final Logger logger = Logger.getLogger(RecordThread.class.getName());

  private final File destinationFile;
  private final AudioFormat audioFormat;
  private final TargetDataLine dataLine;
  private final RecordingListener listener;

  /**
   * To create a new RecordThread, you must specify the destination file where audio
   * will be saved. Output file will be WAV, 16bit, 44.1Khz.
   *
   * @param destFile The file which will receive the audio (wav format).
   * @param listener An optional RecordingListener to receive notification when complete.
   * @throws LineUnavailableException if an audio system error occurs.
   */
  public RecordThread(File destFile, RecordingListener listener) throws LineUnavailableException {
    this.destinationFile = destFile;
    this.listener = listener;
    audioFormat = new AudioFormat(44100, 16, 2, true, true);
    DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);

    // checks if system supports the data line
    if (!AudioSystem.isLineSupported(info)) {
      throw new LineUnavailableException("Line not supported.");
    }
    dataLine = (TargetDataLine)AudioSystem.getLine(info);
    dataLine.open(audioFormat);
  }

  public void stop() {
    dataLine.stop();
    dataLine.close();
    if (listener != null) {
      listener.complete();
    }
  }

  @Override
  public void run() {
    dataLine.start();   // start capturing
    AudioInputStream ais = new AudioInputStream(dataLine);
    try {
      AudioSystem.write(ais, AudioFileFormat.Type.WAVE, destinationFile);
    }
    catch (IOException ioe) {
      logger.log(Level.SEVERE, "Recording error: " + ioe.getMessage(), ioe);
    }
  }

}
