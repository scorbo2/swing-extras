package ca.corbett.extras.audio;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * A utility class that can be used to interact with audio files. This is an abstraction over
 * the javax.sound utility classes, but also adds some additional features.
 *
 * @author scorbo2
 * @since 2018-01-03
 */
public class AudioUtil {

  private static final Logger logger = Logger.getLogger(AudioUtil.class.getName());

  /**
   * Utility classes do not have public constructors. *
   */
  private AudioUtil() {
  }

  /**
   * Loads and plays the given audio file. If you specify a PlaybackListener, it will receive
   * events during playback and when playback ends. A PlaybackThread instance is returned
   * which can be used to control playback. Playback starts immediately, but this method
   * does not block - it returns immediately. Your code can continue processing as normal,
   * and playback will end naturally when the end of the audio stream is reached. You can
   * also use the facilities within PlaybackThread to control playback.
   *
   * @param audioFile A file containing audio. Must be in a format supported by javax.sound.
   * @param listener An optional PlaybackListener to receive playback events. Can be null.
   * @return A PlaybackThread instance.
   * @throws javax.sound.sampled.UnsupportedAudioFileException On unsupported audio.
   * @throws java.io.IOException On general i/o error.
   * @throws javax.sound.sampled.LineUnavailableException On audio system error.
   */
  public static PlaybackThread play(File audioFile, PlaybackListener listener)
          throws UnsupportedAudioFileException, IOException, LineUnavailableException {
    AudioInputStream audioStream = getAudioInputStream(parseAudioFile(audioFile));
    PlaybackThread thread = new PlaybackThread(audioStream, 0, 0, listener);
    new Thread(thread).start();
    return thread;
  }

  /**
   * Loads and plays the given audio stream. If you specify a PlaybackListener, it will receive
   * events during playback and when playback ends. A PlaybackThread instance is returned
   * which can be used to control playback. Playback starts immediately, but this method
   * does not block - it returns immediately. Your code can continue processing as normal,
   * and playback will end naturally when the end of the audio stream is reached. You can
   * also use the facilities within PlaybackThread to control playback.
   *
   * @param inStream A stream containing audio. Must be in a format supported by javax.sound.
   * @param listener An optional PlaybackListener to receive playback events. Can be null.
   * @return A PlaybackThread instance.
   * @throws javax.sound.sampled.UnsupportedAudioFileException On unsupported audio.
   * @throws java.io.IOException On general i/o error.
   * @throws javax.sound.sampled.LineUnavailableException On audio system error.
   */
  public static PlaybackThread play(BufferedInputStream inStream, PlaybackListener listener)
          throws UnsupportedAudioFileException, IOException, LineUnavailableException {
    AudioInputStream audioStream = getAudioInputStream(parseAudioStream(inStream));
    PlaybackThread thread = new PlaybackThread(audioStream, 0, 0, listener);
    new Thread(thread).start();
    return thread;
  }

  /**
   * Loads and plays the given audio data. If you specify a PlaybackListener, it will receive
   * events during playback and when playback ends. A PlaybackThread instance is returned
   * which can be used to control playback. Playback starts immediately, but this method
   * does not block - it returns immediately. Your code can continue processing as normal,
   * and playback will end naturally when the end of the audio stream is reached. You can
   * also use the facilities within PlaybackThread to control playback.
   *
   * @param audioData An int array containing audio data (from one of the parseAudio methods).
   * @param listener An optional PlaybackListener to receive playback events. Can be null.
   * @return A PlaybackThread instance.
   * @throws javax.sound.sampled.UnsupportedAudioFileException On unsupported audio.
   * @throws java.io.IOException On general i/o error.
   * @throws javax.sound.sampled.LineUnavailableException On audio system error.
   */
  public static PlaybackThread play(int[][] audioData, PlaybackListener listener)
          throws UnsupportedAudioFileException, IOException, LineUnavailableException {
    AudioInputStream audioStream = getAudioInputStream(audioData);
    PlaybackThread thread = new PlaybackThread(audioStream, 0, 0, listener);
    new Thread(thread).start();
    return thread;
  }

  /**
   * Loads and plays the given audio file, starting from the given offset and ending at the given
   * limit (all times in milliseconds). If you specify a PlaybackListener, it will receive
   * events during playback and when playback ends. A PlaybackThread instance is returned
   * which can be used to control playback. Playback starts immediately, but this method
   * does not block - it returns immediately. Your code can continue processing as normal,
   * and playback will end naturally when the end of the audio stream is reached. You can
   * also use the facilities within PlaybackThread to control playback.
   *
   * @param audioFile A file containing audio. Must be in a format supported by javax.sound.
   * @param offset An offset, in milliseconds, where to start playing. 0 means play from start.
   * @param limit An offset, in milliseconds, where to stop playing. 0 means play to end of stream.
   * @param listener An optional PlaybackListener to receive playback events. Can be null.
   * @return A PlaybackThread instance.
   * @throws javax.sound.sampled.UnsupportedAudioFileException On unsupported audio.
   * @throws java.io.IOException On general i/o error.
   * @throws javax.sound.sampled.LineUnavailableException On audio system error.
   */
  public static PlaybackThread play(File audioFile, long offset, long limit,
                                    PlaybackListener listener) throws UnsupportedAudioFileException,
          IOException, LineUnavailableException {
    AudioInputStream audioStream = getAudioInputStream(parseAudioFile(audioFile));
    PlaybackThread thread = new PlaybackThread(audioStream, offset, limit, listener);
    new Thread(thread).start();
    return thread;
  }

  /**
   * Loads and plays the given audio stream, starting from the given offset and ending at the given
   * limit (all times in milliseconds). If you specify a PlaybackListener, it will receive
   * events during playback and when playback ends. A PlaybackThread instance is returned
   * which can be used to control playback. Playback starts immediately, but this method
   * does not block - it returns immediately. Your code can continue processing as normal,
   * and playback will end naturally when the end of the audio stream is reached. You can
   * also use the facilities within PlaybackThread to control playback.
   *
   * @param inStream A stream containing audio. Must be in a format supported by javax.sound.
   * @param offset An offset, in milliseconds, where to start playing. 0 means play from start.
   * @param limit An offset, in milliseconds, where to stop playing. 0 means play to end of stream.
   * @param listener An optional PlaybackListener to receive playback events. Can be null.
   * @return A PlaybackThread instance.
   * @throws javax.sound.sampled.UnsupportedAudioFileException On unsupported audio.
   * @throws java.io.IOException On general i/o error.
   * @throws javax.sound.sampled.LineUnavailableException On audio system error.
   */
  public static PlaybackThread play(BufferedInputStream inStream, long offset, long limit,
                                    PlaybackListener listener)
          throws UnsupportedAudioFileException, IOException, LineUnavailableException {
    AudioInputStream audioStream = getAudioInputStream(parseAudioStream(inStream));
    PlaybackThread thread = new PlaybackThread(audioStream, offset, limit, listener);
    new Thread(thread).start();
    return thread;
  }

  /**
   * Loads and plays the given audio data, starting from the given offset and ending at the given
   * limit (all times in milliseconds). If you specify a PlaybackListener, it will receive
   * events during playback and when playback ends. A PlaybackThread instance is returned
   * which can be used to control playback. Playback starts immediately, but this method
   * does not block - it returns immediately. Your code can continue processing as normal,
   * and playback will end naturally when the end of the audio stream is reached. You can
   * also use the facilities within PlaybackThread to control playback.
   *
   * @param audioData An int array containing audio data (from one of the parseAudio methods).
   * @param offset An offset, in milliseconds, where to start playing. 0 means play from start.
   * @param limit An offset, in milliseconds, where to stop playing. 0 means play to end of stream.
   * @param listener An optional PlaybackListener to receive playback events. Can be null.
   * @return A PlaybackThread instance.
   * @throws javax.sound.sampled.UnsupportedAudioFileException On unsupported audio.
   * @throws java.io.IOException On general i/o error.
   * @throws javax.sound.sampled.LineUnavailableException On audio system error.
   */
  public static PlaybackThread play(int[][] audioData, long offset, long limit,
                                    PlaybackListener listener)
          throws UnsupportedAudioFileException, IOException, LineUnavailableException {
    AudioInputStream audioStream = getAudioInputStream(audioData);
    PlaybackThread thread = new PlaybackThread(audioStream, offset, limit, listener);
    new Thread(thread).start();
    return thread;
  }

  /**
   * Attempts to parse the given audio file to return the raw PCM audio samples,
   * returned as a multi-dimensional int array. The outer array is by channel (one for
   * monaural, two for stereo, more for 5.1 or whatever). The inner array is for the
   * actual audio samples themselves.
   *
   * @param file The File containing the audio data. Must be in a format supported by javax.sound.
   * @return A multi-dimensional array as described above.
   * @throws javax.sound.sampled.UnsupportedAudioFileException On unsupported audio.
   * @throws java.io.IOException On general I/O error.
   */
  public static int[][] parseAudioFile(File file)
          throws UnsupportedAudioFileException, IOException {
    return parseAudioStream(new BufferedInputStream(new FileInputStream(file)));
  }

  /**
   * Attempts to parse the given audio stream to return the raw PCM audio samples,
   * returned as a multi-dimensional int array. The outer array is by channel (one for
   * monoraul, two for stereo, more for 5.1 or whatever). The inner array is for the
   * actual audio samples themselves. The given stream will be closed before return,
   * unless some exception is thrown.
   *
   * @param inStream An InputStream containing the audio data. Must be in a supported format.
   * @return A multi-dimensional int array as described above.
   * @throws UnsupportedAudioFileException On unsupported audio.
   * @throws IOException On general I/O error.
   */
  public static int[][] parseAudioStream(BufferedInputStream inStream)
          throws UnsupportedAudioFileException, IOException {
    // Open the stream and read the raw data as one big byte array:
    AudioInputStream inputStream = AudioSystem.getAudioInputStream(inStream);
    int frameLength = (int)inputStream.getFrameLength();
    int frameSize = (int)inputStream.getFormat().getFrameSize();

    // Odd NegativeArraySize exception can be thrown below, see AREC-10:
    if (frameLength < 0 || frameSize < 0 || (frameLength * frameSize) < 0) {
      throw new IOException("Empty or corrupt Audio stream.");
    }

    byte[] byteArray = new byte[frameLength * frameSize];
    int result = inputStream.read(byteArray);

    // Now split that byte array into channels and proper samples (assuming 16 bit samples here):
    int numChannels = inputStream.getFormat().getChannels();
    int[][] audioData = new int[numChannels][frameLength];
    int sampleIndex = 0;
    for (int t = 0; t < result;) {
      for (int channel = 0; channel < numChannels; channel++) {
        int lowByte = (int)byteArray[t++];
        int highByte = (int)byteArray[t++];
        int sample = (highByte << 8) | (lowByte & 0x00ff);
        audioData[channel][sampleIndex] = sample;
      }
      sampleIndex++;
    }

    inputStream.close();
    return audioData;
  }

  /**
   * Writes the given channel/sample array to the specified file. The multi-dimensional array
   * is structured the same as the return from parseAudioFile() - that is, the outer array
   * is by channel, the inner arrays are for actual audio samples. We assume 16 bit samples
   * and a rate of 44.1Khz for the output.
   *
   * @param file The File to which to save the audio data. Will be overwritten if it exists.
   * @param audioData A multi-dimensional array of audio data as outlined above.
   * @throws IOException on general i/o problem.
   */
  public static void saveAudioFile(File file, int[][] audioData) throws IOException {
    AudioInputStream audioStream = getAudioInputStream(audioData);
    AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, file);
    audioStream.close();
  }

  /**
   * Constructs an AudioInputStream based on the parsed audio data. This is mainly used
   * to play a clip that has been parsed by one of the parseAudio methods in this class.
   *
   * @param audioData Audio data as parsed by one of the parseAudio methods in this class.
   * @return An AudioInputStream ready to be read.
   */
  public static AudioInputStream getAudioInputStream(int[][] audioData) {
    // Convert the int array into a single byte array:
    byte[] byteArray = new byte[audioData[0].length * audioData.length * 2];
    int sample = 0;
    for (int i = 0; i < byteArray.length;) {
      for (int channel = 0; channel < audioData.length; channel++) {
        byteArray[i++] = (byte)(audioData[channel][sample] & 0xff);
        byteArray[i++] = (byte)(audioData[channel][sample] >>> 8);
      }
      sample++;
    }

    AudioFormat format = new AudioFormat(44100f, 16, audioData.length, true, false);
    return new AudioInputStream(new ByteArrayInputStream(byteArray), format, audioData[0].length);
  }

  /**
   * Parses the given audio file and returns a BufferedImage containing a graphical waveform
   * representing the contained audio. All WaveformConfig are set to their default
   * values - if this is not acceptable, use generateWaveform(WaveformConfig) instead.
   *
   * @param file An audio file.
   * @return A BufferedImage containing a graphical waveform of the given audio file.
   * @throws javax.sound.sampled.UnsupportedAudioFileException On unsupported audio.
   * @throws java.io.IOException On general i/o error.
   */
  public static BufferedImage generateWaveform(File file)
          throws UnsupportedAudioFileException, IOException {
    return generateWaveform(file, new WaveformConfig());
  }

  /**
   * Parses the given audio stream and returns a BufferedImage containing a graphical waveform
   * representing the contained audio. All WaveformConfig are set to their default
   * values - if this is not acceptable, use generateWaveform(WaveformConfig) instead.
   *
   * @param audioStream An InputStream containing audio data. Must be in a supported format.
   * @return A BufferedImage containing a graphical waveform of the given audio file.
   * @throws javax.sound.sampled.UnsupportedAudioFileException On unsupported audio.
   * @throws java.io.IOException On general i/o error.
   */
  public static BufferedImage generateWaveform(BufferedInputStream audioStream)
          throws UnsupportedAudioFileException, IOException {
    return generateWaveform(audioStream, new WaveformConfig());
  }

  /**
   * Parses the given audio file and returns a BufferedImage containing a graphical
   * waveform from the contained audio. See WaveformConfig for options around
   * controlling what the output looks like.
   *
   * @param file An audio file.
   * @param prefs A WaveformConfig instance containing desired preferences.
   * @return A BufferedImage containing a graphical waveform of the given audio file.
   * @throws javax.sound.sampled.UnsupportedAudioFileException On unsupported audio.
   * @throws java.io.IOException On general i/o error.
   */
  public static BufferedImage generateWaveform(File file, WaveformConfig prefs)
          throws UnsupportedAudioFileException, IOException {
    int[][] audioData = parseAudioFile(file);
    return generateWaveform(audioData, prefs);
  }

  /**
   * Parses the given audio stream and returns a BufferedImage containing a graphical
   * waveform from the contained audio. See WaveformConfig for options around
   * controlling what the output looks like.
   *
   * @param audioStream An InputStream containing audio data. Must be in a supported format.
   * @param prefs A WaveformConfig instance containing desired preferences.
   * @return A BufferedImage containing a graphical waveform of the given audio file.
   * @throws javax.sound.sampled.UnsupportedAudioFileException On unsupported audio.
   * @throws java.io.IOException On general i/o error.
   */
  public static BufferedImage generateWaveform(BufferedInputStream audioStream,
                                               WaveformConfig prefs)
          throws UnsupportedAudioFileException, IOException {
    int[][] audioData = parseAudioStream(audioStream);
    return generateWaveform(audioData, prefs);
  }

  /**
   * Generates a BufferedImage containing a graphical waveform representing the audio
   * in the given multidimensional int array. This is primarily inovked from the other
   * generateWaveform methods in this class, but can be invoked by clients also if they
   * have parsed the audio already.
   *
   * @param audioData The parsed audio data, presumably from one of the parseAudio methods.
   * @param prefs A WaveformConfig instance describing what the waveform should look like.
   * @return A BufferedImage containing a graphical representation of the audio data.
   * @throws javax.sound.sampled.UnsupportedAudioFileException On unsupported audio.
   * @throws java.io.IOException On general i/o error.
   */
  public static BufferedImage generateWaveform(int[][] audioData, WaveformConfig prefs)
          throws UnsupportedAudioFileException, IOException {
    BufferedImage waveform;

    // Make sure our audio channel indexes make sense:
    int topChannelIndex = Math.max(prefs.getTopChannelIndex(), 0);
    int btmChannelIndex = Math.max(prefs.getBottomChannelIndex(), 0);
    topChannelIndex = (topChannelIndex >= audioData.length) ? audioData.length - 1 : topChannelIndex;
    btmChannelIndex = (btmChannelIndex >= audioData.length) ? audioData.length - 1 : btmChannelIndex;

    // Go through the data and find our highest y values:
    int averagedSample1 = 0;
    int averagedSample2 = 0;
    int averagedSampleCount = 0;
    int maxY1 = 0;
    int maxY2 = 0;
    for (int i = 0; i < audioData[0].length; i++) {
      int sample1 = Math.abs(audioData[topChannelIndex][i] / prefs.getYScale());
      int sample2 = Math.abs(audioData[btmChannelIndex][i] / prefs.getYScale());

      averagedSample1 += sample1;
      averagedSample2 += sample2;
      averagedSampleCount++;
      if (averagedSampleCount > prefs.getXScale()) {
        averagedSample1 /= averagedSampleCount;
        averagedSample2 /= averagedSampleCount;

        maxY1 = Math.max(averagedSample1, maxY1);
        maxY2 = Math.max(averagedSample2, maxY2);
        averagedSample1 = 0;
        averagedSample2 = 0;
        averagedSampleCount = 0;
      }
    }

    // We can now create a blank image of the appropriate size based on this scale:
    int xScale = prefs.getXScale();
    int width = audioData[0].length / prefs.getXScale();
    if (width > prefs.getXLimit()) {
      width = prefs.getXLimit();
      xScale = audioData[0].length / prefs.getXLimit();
      logger.log(Level.INFO, "AudioUtil: Adjusted xScale from {0} to {1} to accomodate x limit of {2}.",
                 new Object[]{prefs.getXScale(), xScale, prefs.getXLimit()});
    }
    int height = maxY1 + maxY2;
    height = (height <= 0) ? 100 : height; // height can be zero if there's no audio data.
    int verticalMargin = 0; // (int)((maxY1+maxY2)*0.1); // disabling margin for now
    int centerY = maxY1 + verticalMargin;
    waveform = new BufferedImage(width, height + (verticalMargin * 2), BufferedImage.TYPE_INT_RGB);

    // Flood the blank image with our background colour and get ready to draw on it:
    Graphics2D graphics = waveform.createGraphics();
    graphics.setColor(prefs.getBgColor());
    graphics.fillRect(0, 0, width, height + (verticalMargin * 2));

    // Now generate the waveform:
    int previousSample1 = 0;
    int previousSample2 = 0;
    int x = 0;
    for (int sample = 0; sample < audioData[0].length; sample++) {
      averagedSample1 += Math.abs(audioData[topChannelIndex][sample] / prefs.getYScale());
      averagedSample2 += Math.abs(audioData[btmChannelIndex][sample] / prefs.getYScale());
      averagedSampleCount++;

      if (averagedSampleCount > xScale) {
        averagedSample1 /= averagedSampleCount;
        averagedSample2 /= averagedSampleCount;

        graphics.setColor(prefs.getFillColor());
        graphics.drawLine(x, centerY, x, centerY - averagedSample1);
        graphics.drawLine(x, centerY, x, centerY + averagedSample2);

        if (prefs.isOutlineEnabled()) {
          graphics.setColor(prefs.getOutlineColor());
          for (int lineI = 0; lineI < prefs.getOutlineThickness(); lineI++) {
            graphics.drawLine(x - 1, centerY - previousSample1 - lineI, x, centerY - averagedSample1 - lineI);
            graphics.drawLine(x - 1, centerY + previousSample2 + lineI, x, centerY + averagedSample2 + lineI);
          }
        }

        previousSample1 = averagedSample1;
        previousSample2 = averagedSample2;
        averagedSample1 = 0;
        averagedSample2 = 0;
        averagedSampleCount = 0;
        x++;
      }
    }

    if (prefs.isBaselineEnabled()) {
      int thickness = Math.max(1, prefs.getBaselineThickness() / 2);
      graphics.setColor(prefs.getBaselineColor());
      for (int y = centerY - thickness; y <= centerY + thickness; y++) {
        graphics.drawLine(0, y, width, y);
      }
    }

    graphics.dispose();

    return waveform;
  }

}
