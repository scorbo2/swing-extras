package ca.corbett.extras.audio;

import ca.corbett.extras.MessageUtil;
import ca.corbett.extras.image.ImagePanel;
import ca.corbett.extras.image.ImagePanelConfig;
import ca.corbett.extras.image.ImageUtil;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Presents a JPanel that encapsulates an audio clip, and can visually present a graphical
 * waveform of that clip, along with controls for playback and editing. This component relies
 * heavily on AudioUtil as well as ImagePanel. Audio controls can optionally be displayed,
 * to allow user input for playback, recording, and editing functions. If the controls
 * are hidden, these functions are only available programmatically.
 *
 * @author scorbo2
 * @since 2018-01-08
 */
public final class AudioWaveformPanel extends JPanel {

    private final static Logger logger = Logger.getLogger(AudioWaveformPanel.class.getName());
    private MessageUtil messageUtil;

    public enum ControlPanelPosition {
        TOP_LEFT, TOP_CENTER, TOP_RIGHT,
        SIDE_EDGES,
        BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT,
        HIDDEN
    }

    public enum ControlPanelSize {
        XSMALL, SMALL, NORMAL, LARGE, XLARGE
    }

    public enum PanelState {
        IDLE, PLAYING, RECORDING
    }

    private boolean allowRecording;
    private boolean allowEditing;

    private int[][] audioData;
    private int[][] clipboardData;
    private PlaybackThread playbackThread;
    private float playbackPosition; // 0f==start, 1f==end
    private final PlaybackListener playbackListener;

    private float markPosition;
    private float selectionStart;
    private float selectionEnd;
    private boolean mouseDragging;

    private final ImagePanel imagePanel;
    private final ImagePanelConfig imagePanelProperties;
    private BufferedImage waveformImage;

    private WaveformConfig waveformPreferences;

    private JPanel controlPanelMain;
    private JPanel controlPanelExtra;
    private ControlPanelPosition controlPanelPosition;
    private ControlPanelSize controlPanelSize;

    private RecordThread recordThread;
    private File scratchFile;

    private final List<AudioPanelListener> panelListeners;
    private PanelState panelState;

    /**
     * Creates a new AudioWaveformPanel with no audio clip, and with default properties.
     * Use setWaveformProperties to set cosmetic properties for the waveform display, or
     * the other setters to set general behavioural properties.
     */
    public AudioWaveformPanel() {
        // Create and configure our image panel properties.
        // These settings are strictly internal and cannot be overridden by clients:
        imagePanelProperties = ImagePanelConfig.createDefaultProperties();
        imagePanelProperties.setDisplayMode(ImagePanelConfig.DisplayMode.STRETCH);
        imagePanelProperties.setEnableMouseDragging(false);
        imagePanelProperties.setEnableZoomOnMouseClick(false);
        imagePanelProperties.setEnableZoomOnMouseWheel(false);
        imagePanel = new ImagePanel((BufferedImage)null, imagePanelProperties);
        imagePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleImagePanelClick(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mouseDragging = false;
            }

        });
        imagePanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                handleImagePanelDrag(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mouseDragging = false;
            }

        });

        // Create and configure our waveform properties.
        // These settings can be modified by clients, as they're mostly cosmetic.
        waveformPreferences = new WaveformConfig();

        // Misc:
        panelState = PanelState.IDLE;
        allowRecording = true;
        allowEditing = true;
        controlPanelPosition = ControlPanelPosition.SIDE_EDGES;
        controlPanelSize = ControlPanelSize.NORMAL;
        playbackPosition = 0f;
        markPosition = 0f;
        selectionStart = 0f;
        selectionEnd = 0f;
        playbackListener = new PlaybackListener() {
            @Override
            public void started() {
            }

            @Override
            public void stopped(PlaybackThread.StopReason stopReason) {
                setPlaybackPosition(0);
                panelState = PanelState.IDLE;
                fireStateChangedEvent();
                fireAudioStoppedEvent(stopReason);
            }

            @Override
            public boolean updateProgress(long curMillis, long totalMillis) {
                setPlaybackPosition((float)curMillis / (float)totalMillis);
                return true;
            }

        };
        panelListeners = new ArrayList<>();

        // Lay out the UI:
        initComponents();
    }

    /**
     * Returns a copy of the current WaveformConfig, which describe cosmetic attributes
     * for the audio waveform display. A copy of the properties object is returned to prevent
     * client modification. To change the properties, use setWaveformPreferences.
     *
     * @return A copy of the current WaveformProperties object.
     */
    public WaveformConfig getWaveformPreferences() {
        return WaveformConfig.clonePreferences(waveformPreferences);
    }

    /**
     * Sets the WaveformConfig, which control cosmetic attributes of the audio waveform
     * display. A copy of the given object is made to prevent client modification after
     * this method is invoked.
     *
     * @param prefs The WaveformConfig to use. If null, default prefs will be used.
     */
    public void setWaveformPreferences(WaveformConfig prefs) {
        waveformPreferences = WaveformConfig.clonePreferences(prefs);
    }

    /**
     * Registers an AudioPanelListener to receive events from this panel.
     *
     * @param listener The new listener to register.
     */
    public void addAudioPanelListener(AudioPanelListener listener) {
        panelListeners.add(listener);
    }

    /**
     * Unregisters a listener from this panel.
     *
     * @param listener The listener to unregister.
     */
    public void removeAudioPanelListener(AudioPanelListener listener) {
        panelListeners.remove(listener);
    }

    /**
     * Returns the raw audio data contained by this panel.
     *
     * @return Raw audio data, suitable for manipulation by AudioUtil methods.
     */
    public int[][] getAudioData() {
        return audioData;
    }

    /**
     * Sets the audio clip to represent in this panel using the given File, which must be in
     * a format supported by the javax.sound package. Any previous clip in this panel is discarded.
     *
     * @param file A File containing an audio clip. Must be in a format supported by javax.sound.
     * @throws UnsupportedAudioFileException If the file format is not supported.
     * @throws IOException                   on general I/O error.
     */
    public void setAudioClip(File file) throws UnsupportedAudioFileException, IOException {
        if (panelState != PanelState.IDLE) {
            stop();
        }
        audioData = AudioUtil.parseAudioFile(file);
        waveformImage = AudioUtil.generateWaveform(audioData, waveformPreferences);
        markPosition = 0f;
        selectionStart = 0f;
        selectionEnd = 0f;
        setPlaybackPosition(0f);
        fireAudioLoadedEvent();
    }

    /**
     * Sets the audio clip to represent in this panel using the given input stream.
     * Any previous clip in this panel is discarded.
     *
     * @param stream A stream containing an audio clip. Must be in a format supported by javax.sound.
     * @throws UnsupportedAudioFileException If the file format is not supported.
     * @throws IOException                   on general I/O error.
     */
    public void setAudioClip(BufferedInputStream stream)
            throws UnsupportedAudioFileException, IOException {
        if (panelState != PanelState.IDLE) {
            stop();
        }
        audioData = AudioUtil.parseAudioStream(stream);
        waveformImage = AudioUtil.generateWaveform(audioData, waveformPreferences);
        markPosition = 0f;
        selectionStart = 0f;
        selectionEnd = 0f;
        setPlaybackPosition(0f);
        fireAudioLoadedEvent();
    }

    /**
     * Sets the audio clip to represent in this panel using the given audio data array.
     * Any previous clip in this panel is discarded.
     *
     * @param data The array of audio data.
     * @throws UnsupportedAudioFileException If the file format is not supported.
     * @throws IOException                   on general I/O error.
     */
    public void setAudioClip(int[][] data) throws UnsupportedAudioFileException, IOException {
        if (panelState != PanelState.IDLE) {
            stop();
        }
        audioData = data;
        waveformImage = AudioUtil.generateWaveform(audioData, waveformPreferences);
        markPosition = 0f;
        selectionStart = 0f;
        selectionEnd = 0f;
        setPlaybackPosition(0f);
        fireAudioLoadedEvent();
    }

    /**
     * Plays the current audio clip, if any, or does nothing if there isn't one.
     * Does nothing if audio is currently playing or recording.
     * This can be invoked programmatically, and is also invoked by the user from the play button,
     * if the control panel is visible and the user clicks it.
     */
    public void play() {

        // Ignore this call if our state is anything other than IDLE:
        if (panelState != PanelState.IDLE) {
            return;
        }

        // Do nothing if no audio is loaded:
        if (audioData == null) {
            return;
        }

        // Set starting offset if set:
        long startOffset = 0;
        long limitOffset = 0;
        if (markPosition > 0f) {
            startOffset = (long)(markPosition * (audioData[0].length / 44.1f)); // WARNING assuming bit rate
        }
        else if (selectionStart > 0f) {
            startOffset = (long)(selectionStart * (audioData[0].length / 44.1f));
            limitOffset = (long)(selectionEnd * (audioData[0].length / 44.1f));
        }

        try {
            panelState = PanelState.PLAYING;
            fireStateChangedEvent();
            playbackThread = AudioUtil.play(audioData, startOffset, limitOffset, playbackListener);
        }
        catch (IOException | LineUnavailableException | UnsupportedAudioFileException exc) {
            getMessageUtil().error("Playback error", "Problem playing audio: " + exc.getMessage(), exc);
            playbackThread = null;
            panelState = PanelState.IDLE;
            fireStateChangedEvent();

        }
    }

    /**
     * Stops playing, if playback was in progress, or does nothing if it wasn't.
     * This can be invoked programmatically, and is also invoked from the stop button,
     * if the control panel is visible and the user clicks it.
     */
    public void stop() {
        switch (panelState) {
            case PLAYING:
                playbackThread.stop();
                playbackThread = null;
                break;

            case RECORDING:
                recordThread.stop();
                recordThread = null;
        }

        panelState = PanelState.IDLE;
        fireStateChangedEvent();
    }

    /**
     * Starts recording a new audio clip. Will prompt the user if an existing clip would
     * be overwritten. If playback was in progress, it is stopped.
     */
    public void record() {
        // Ignore this request if we're currently playing audio:
        if (panelState == PanelState.PLAYING) {
            return;
        }

        // Treat this as a "stop" if we're currently recording:
        if (panelState == PanelState.RECORDING) {
            stop();
            return;
        }

        // Otherwise, start recording:
        try {
            panelState = PanelState.RECORDING;
            fireStateChangedEvent();
            redrawWaveform();

            scratchFile = File.createTempFile("audio_", "scratch.wav");
            recordThread = new RecordThread(scratchFile, new RecordingListener() {
                @Override
                public void complete() {
                    panelState = PanelState.IDLE;
                    fireStateChangedEvent();
                    try {
                        setAudioClip(scratchFile);
                        fireRecordingCompleteEvent();
                    }
                    catch (IOException | UnsupportedAudioFileException e) {
                        logger.log(Level.SEVERE, "Recording complete, but with error: " + e.getMessage(), e);
                    }
                }

            });
        }
        catch (LineUnavailableException | IOException lue) {
            logger.log(Level.SEVERE, "Recording thread exception: " + lue.getMessage(), lue);
            panelState = PanelState.IDLE;
            fireStateChangedEvent();
        }
        new Thread(recordThread).start();
    }

    /**
     * Invoked internally from doCut and doCopy to copy a range of audio data
     * to the internal clipboard. This will replace whatever was in the clipboard previously.
     */
    private void copyRangeToClipboard(int startIndex, int endIndex) {
        int dataLength = endIndex - startIndex;
        clipboardData = new int[audioData.length][dataLength];
        for (int channelI = 0; channelI < audioData.length; channelI++) {
            System.arraycopy(audioData[channelI], startIndex, clipboardData[channelI], 0, dataLength);
        }
    }

    /**
     * Cuts the selected portion of the audio clip, and places it on the clipboard.
     * Does nothing if there is no selection, or if audio is currently playing or recording.
     */
    public void doCut() {
        if (panelState != PanelState.IDLE || audioData == null) {
            return;
        }

        if (selectionStart < 0 && selectionEnd <= 0) {
            return;
        }

        // Translate the selection start/end into array indeces, based on the length of our audio data:
        int startIndex = Math.max(0, (int)(selectionStart * audioData[0].length));
        int endIndex = Math.min(audioData[0].length - 1, (int)(selectionEnd * audioData[0].length));
        int dataLength = endIndex - startIndex;

        // Create or overwrite the clipboard data:
        copyRangeToClipboard(startIndex, endIndex);

        // Now remove the data from the audio clip:
        for (int channelI = 0; channelI < audioData.length; channelI++) {
            int[] tempArr = new int[audioData[channelI].length - dataLength];
            System.arraycopy(audioData[channelI], 0, tempArr, 0, startIndex);
            for (int i = endIndex; i < audioData[channelI].length; i++) {
                tempArr[i - dataLength] = audioData[channelI][i];
            }
            audioData[channelI] = tempArr;
        }

        // Regenerate the waveform image with the new audio data and rerender it:
        try {
            setAudioClip(audioData);
        }
        catch (UnsupportedAudioFileException | IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        redrawWaveform();
    }

    /**
     * Copies the selected portion of the audio clip, and places it on the clipboard.
     * Does nothing if there is no selection, or if audio is currently playing or recording.
     */
    public void doCopy() {
        if (panelState != PanelState.IDLE || audioData == null) {
            return;
        }

        if (selectionStart <= 0 && selectionEnd <= 0) {
            return;
        }

        // Translate the selection start/end into array indeces, based on the length of our audio data:
        int startIndex = Math.max(0, (int)(selectionStart * audioData[0].length));
        int endIndex = Math.min(audioData[0].length - 1, (int)(selectionEnd * audioData[0].length));
        int dataLength = endIndex - startIndex;

        // Create or overwrite the clipboard data:
        copyRangeToClipboard(startIndex, endIndex);

    }

    /**
     * Pastes the contents of the clipboard into the current mark position.
     * Does nothing if there is no audio data, or if audio is currently playing or recording.
     * If the mark position is not set, the paste happens at the start of the clip.
     */
    public void doPaste() {
        if (panelState != PanelState.IDLE || audioData == null || clipboardData == null) {
            return;
        }

        // Find out the length of the data in the clipboard, and resize our audio array:
        int markIndex = (int)(audioData[0].length * markPosition);
        int dataLength = clipboardData[0].length;
        for (int channelI = 0; channelI < audioData.length; channelI++) {
            int[] tempArr = new int[audioData[channelI].length + dataLength];
            System.arraycopy(audioData[channelI], 0, tempArr, 0, markIndex);
            System.arraycopy(clipboardData[channelI], 0, tempArr, markIndex, dataLength);
            int size = audioData[channelI].length - markIndex;
            System.arraycopy(audioData[channelI], markIndex, tempArr, markIndex + dataLength, size);
            audioData[channelI] = tempArr;
        }

        // Regenerate the waveform image with the new audio data and rerender it:
        try {
            setAudioClip(audioData);
        }
        catch (UnsupportedAudioFileException | IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        redrawWaveform();
    }

    /**
     * Returns the current state of this panel.
     * <ul>
     * <li><b>IDLE</b> - neither playing nor recording.
     * <li><b>PLAYING</b> - currently playing audio.
     * <li><b>RECORDING</b> - currently recording audio.
     * </ul>
     *
     * @return One of the PanelState enum values as described above.
     */
    public PanelState getPanelState() {
        return panelState;
    }

    /**
     * Reports whether the "record" button is visible - if true, user can click it
     * to record a new audio clip, which will overwrite the current one if any.
     *
     * @return Whether recording is allowed.
     */
    public boolean isRecordingAllowed() {
        return allowRecording;
    }

    /**
     * Sets whether the "record" button is visible - if true, user can click it
     * to record a new audio clip, which will overwrite the current one if any.
     *
     * @param allowed Whether recording should be allowed.
     */
    public void setRecordingAllowed(boolean allowed) {
        allowRecording = allowed;

        // Now we need to force the control panel to regenerate itself.
        // An easy way to do this is to fake changing the control panel position.
        setControlPanelPosition(controlPanelPosition);
    }

    /**
     * Reports whether the audio editing buttons are visible. If true, user can click the
     * cut, copy, and paste buttons to manipulate the current audio clip.
     *
     * @return Whether audio editing controls are shown.
     */
    public boolean isEditingAllowed() {
        return allowEditing;
    }

    /**
     * Sets whether the audio editing buttons are visible. If true, user can click the
     * cut, copy, and paste buttons to manipulate the current audio clip.
     *
     * @param allowed Whether audio editing controls are shown.
     */
    public void setEditingAllowed(boolean allowed) {
        allowEditing = allowed;

        // Now we need to force the control panel to regenerate itself.
        // An easy way to do this is to fake changing the control panel position.
        setControlPanelPosition(controlPanelPosition);
    }

    /**
     * Reports whether the control panel is visible.
     *
     * @return Whether the control panel is visible.
     */
    public boolean isControlPanelVisible() {
        return controlPanelPosition != ControlPanelPosition.HIDDEN;
    }

    /**
     * Returns the current control panel position.
     *
     * @return The control panel position. See ControlPanelPosition enum in this class.
     */
    public ControlPanelPosition getControlPanelPosition() {
        return controlPanelPosition;
    }

    /**
     * Sets the control panel position using one of the ControlPanelPosition constants:
     * <ul>
     * <li>TOP_LEFT: cpanel across the top, controls on the left.</li>
     * <li>TOP_CENTER: cpanel across the top, controls centered horizontally.</li>
     * <li>TOP_RIGHT: cpanel across the top, controls on the right.</li>
     * <li>SIDE_EDGES: cpanel split to left/right edges; playback on left, edit on right.</li>
     * <li>BOTTOM_LEFT: cpanel across the bottom, controls on the left.</li>
     * <li>BOTTOM_CENTER: cpanel across the bottom, controls centered horizontally.</li>
     * <li>BOTTOM_RIGHT: cpanel across the bottom, controls on the right.</li>
     * <li>HIDDEN: Control panel is not shown. Playback must be controlled programmatically.</li>
     * </ul>
     *
     * @param pos One of the ControlPanelPosition constants as described above.
     */
    public void setControlPanelPosition(ControlPanelPosition pos) {
        if (controlPanelPosition != ControlPanelPosition.HIDDEN) {
            remove(controlPanelMain);
            remove(controlPanelExtra);
        }

        controlPanelPosition = pos;
        buildControlPanels();
        switch (controlPanelPosition) {
            case TOP_LEFT:
            case TOP_CENTER:
            case TOP_RIGHT:
                add(controlPanelMain, BorderLayout.NORTH);
                break;

            case BOTTOM_LEFT:
            case BOTTOM_CENTER:
            case BOTTOM_RIGHT:
                add(controlPanelMain, BorderLayout.SOUTH);
                break;

            case SIDE_EDGES:
                add(controlPanelMain, BorderLayout.WEST);
                add(controlPanelExtra, BorderLayout.EAST);
                break;

            // Note we do nothing if HIDDEN... just don't add it
        }

        revalidate();
        repaint();
    }

    /**
     * Returns the current control panel size, as one of the ControlPanelSize enum values.
     * Note that this will return the last value that was set even if the control panel
     * is not currently visible.
     *
     * @return The current or last set control panel size.
     */
    public ControlPanelSize getControlPanelSize() {
        return controlPanelSize;
    }

    /**
     * Sets the size of the control panel, using one of the ControlPanelSize enum values:
     * <ul>
     * <li>XSMALL</li>
     * <li>SMALL</li>
     * <li>NORMAL</li>
     * <li>LARGE</li>
     * <li>XLARGE</li>
     * <li>
     * </ul>
     * These values are interpolated internally. Note that this has no effect if the control
     * panel is not currently visible.
     *
     * @param size One of the ControlPanelSize constants listed above.
     */
    public void setControlPanelSize(ControlPanelSize size) {
        if (!controlPanelSize.equals(size)) {
            controlPanelSize = size;
            setControlPanelPosition(controlPanelPosition); // force regeneration of control panel
        }
    }

    /**
     * Returns the pixel dimensions of the current audio waveform image, if there is one, or
     * null otherwise.
     *
     * @return A Dimension object representing the size of the waveform image, or null.
     */
    public Dimension getWaveformDimensions() {
        Dimension dim = null;

        if (imagePanel.getImage() != null) {
            dim = new Dimension(imagePanel.getImage().getWidth(), imagePanel.getImage().getHeight());
        }

        return dim;
    }

    /**
     * Invoked internally to lay out all UI components.
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        add(imagePanel, BorderLayout.CENTER);
        buildControlPanels();
        setControlPanelPosition(controlPanelPosition);
    }

    /**
     * Invoked internally to build the control panels. There are two control panels: the "main"
     * control panel, which is used to hold all controls in the horizontal configuration (if
     * control panel position is along the top or bottom edge), and the "extra" control panel,
     * which is only used in the vertical configuration. In the vertical config, the audio controls
     * will be split so that playback/recording controls are shown along the left vertical edge,
     * and cut/copy/paste will be shown along the right vertical edge.
     */
    private void buildControlPanels() {
        controlPanelMain = new JPanel();
        controlPanelMain.setLayout(new GridBagLayout());
        controlPanelExtra = new JPanel();
        controlPanelExtra.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        // Figure out control positioning:
        boolean isVertical = controlPanelPosition == ControlPanelPosition.SIDE_EDGES;
        boolean isHorizontal = !isVertical;
        boolean biasStart = controlPanelPosition == ControlPanelPosition.TOP_LEFT
                || controlPanelPosition == ControlPanelPosition.BOTTOM_LEFT;
        boolean biasCenter = controlPanelPosition == ControlPanelPosition.TOP_CENTER
                || controlPanelPosition == ControlPanelPosition.BOTTOM_CENTER
                || controlPanelPosition == ControlPanelPosition.SIDE_EDGES;
        boolean biasEnd = controlPanelPosition == ControlPanelPosition.TOP_RIGHT
                || controlPanelPosition == ControlPanelPosition.BOTTOM_RIGHT;
        int deltaX = isVertical ? 0 : 1;
        int deltaY = isHorizontal ? 0 : 1;

        // Figure out the sizes of our buttons:
        int btnSize = 22;
        int iconSize = 20;
        switch (controlPanelSize) {
            case XSMALL:
                btnSize = 16;
                iconSize = 14;
                break;
            case SMALL:
                btnSize = 20;
                iconSize = 18;
                break;
            case NORMAL:
                btnSize = 24;
                iconSize = 22;
                break;
            case LARGE:
                btnSize = 30;
                iconSize = 28;
                break;
            case XLARGE:
                btnSize = 40;
                iconSize = 38;
                break;
        }

        JLabel spacer = new JLabel("");
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.VERTICAL;
        constraints.weightx = isVertical ? 0 : (biasStart ? 0 : (biasCenter ? 0.5 : 1.0));
        constraints.weighty = isHorizontal ? 0 : (biasStart ? 0 : (biasCenter ? 0.5 : 1.0));
        controlPanelMain.add(spacer, constraints);
        spacer = new JLabel("");
        controlPanelExtra.add(spacer, constraints);

        String iconResource = "/swing-extras/images/media-playback-start.png";
        JButton button = buildToolBarButton(iconResource, "Play", btnSize, iconSize);
        constraints.gridx += deltaX;
        constraints.gridy += deltaY;
        constraints.insets = new Insets(2, 2, 2, 2);
        constraints.weightx = 0;
        constraints.weighty = 0;
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                play();
            }

        });
        controlPanelMain.add(button, constraints);

        iconResource = "/swing-extras/images/media-playback-stop.png";
        button = buildToolBarButton(iconResource, "Stop", btnSize, iconSize);
        constraints.gridx += deltaX;
        constraints.gridy += deltaY;
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stop();
            }

        });
        controlPanelMain.add(button, constraints);

        iconResource = "/swing-extras/images/media-record.png";
        button = buildToolBarButton(iconResource, "Record", btnSize, iconSize);
        constraints.gridx += deltaX;
        constraints.gridy += deltaY;
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                record();
            }

        });
        if (allowRecording) {
            controlPanelMain.add(button, constraints);
        }

        iconResource = "/swing-extras/images/icon-cut.png";
        button = buildToolBarButton(iconResource, "Cut", btnSize, iconSize);
        constraints.gridx += deltaX;
        constraints.gridy += deltaY;
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doCut();
            }

        });
        if (allowEditing) {
            if (controlPanelPosition == ControlPanelPosition.SIDE_EDGES) {
                controlPanelExtra.add(button, constraints);
            }
            else {
                controlPanelMain.add(button, constraints);
            }
        }

        iconResource = "/swing-extras/images/icon-copy.png";
        button = buildToolBarButton(iconResource, "Copy", btnSize, iconSize);
        constraints.gridx += deltaX;
        constraints.gridy += deltaY;
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doCopy();
            }

        });
        if (allowEditing) {
            if (controlPanelPosition == ControlPanelPosition.SIDE_EDGES) {
                controlPanelExtra.add(button, constraints);
            }
            else {
                controlPanelMain.add(button, constraints);
            }
        }

        iconResource = "/swing-extras/images/icon-paste.png";
        button = buildToolBarButton(iconResource, "Paste", btnSize, iconSize);
        constraints.gridx += deltaX;
        constraints.gridy += deltaY;
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doPaste();
            }

        });
        if (allowEditing) {
            if (controlPanelPosition == ControlPanelPosition.SIDE_EDGES) {
                controlPanelExtra.add(button, constraints);
            }
            else {
                controlPanelMain.add(button, constraints);
            }
        }

        spacer = new JLabel("");
        constraints.gridx += deltaX;
        constraints.gridy += deltaY;
        constraints.fill = GridBagConstraints.VERTICAL;
        constraints.weightx = isVertical ? 0 : (biasEnd ? 0 : (biasCenter ? 0.5 : 1.0));
        constraints.weighty = isHorizontal ? 0 : (biasEnd ? 0 : (biasCenter ? 0.5 : 1.0));
        constraints.insets = new Insets(0, 0, 0, 0);
        controlPanelMain.add(spacer, constraints);
        spacer = new JLabel("");
        controlPanelExtra.add(spacer, constraints);
    }

    /**
     * Renders a visible vertical tracking line overtop of the current waveform to indicate
     * the current playback position. If the playback position is 0, this will simply render
     * the waveform itself with no tracking line.
     *
     * @param pos From 0 - 1, indicating the percentage of the image width (eg. 0.5 == middle).
     */
    private void setPlaybackPosition(float pos) {
        // If we have no waveform or audio data, reset to 0 and we're done:
        if (waveformImage == null || audioData == null) {
            playbackPosition = pos;
            return;
        }

        // Keep it in range:
        playbackPosition = (pos < 0f) ? 0 : ((pos > 1f) ? 1f : pos);

        redrawWaveform();
    }

    /**
     * Clears the current waveform image.
     */
    public void clear() {
        if (panelState != PanelState.IDLE) {
            stop();
        }
        audioData = null;
        waveformImage = null;
        redrawWaveform();
    }

    public void regenerateWaveformImage() {
        if (panelState != PanelState.IDLE) {
            stop();
        }
        if (audioData == null) {
            return;
        }

        try {
            waveformImage = AudioUtil.generateWaveform(audioData, waveformPreferences);
        }
        catch (UnsupportedAudioFileException | IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        redrawWaveform();
    }

    /**
     * Re-draws the current waveform, if there is one, or clears the panel if not.
     * If you want to force a recalculation of the waveform image, use regenerateWaveform instead.
     */
    private void redrawWaveform() {
        // If currently recording, show a recording message:
        if (panelState == PanelState.RECORDING) {
            BufferedImage buf = new BufferedImage(imagePanel.getWidth(),
                                                  imagePanel.getHeight(),
                                                  BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = (Graphics2D)buf.createGraphics();
            graphics.setColor(Color.DARK_GRAY);
            graphics.fillRect(0, 0, imagePanel.getWidth(), imagePanel.getHeight());
            graphics.setColor(Color.RED);
            graphics.setFont(new Font("Monospaced", Font.BOLD, 16));
            graphics.drawString("(recording in progress)", 20, 20);
            graphics.dispose();
            imagePanel.setImage(buf);
            return;
        }

        // If we have no waveform image, we're done here:
        if (waveformImage == null || audioData == null) {
            imagePanel.setImage(null);
            return;
        }

        BufferedImage buf = new BufferedImage(imagePanel.getWidth(),
                                              imagePanel.getHeight(),
                                              BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = (Graphics2D)buf.createGraphics();
        graphics.drawImage(waveformImage, 0, 0, buf.getWidth(), buf.getHeight(), null);

        // Add the playback position if not at the extreme edges:
        if (playbackPosition > 0.01f && playbackPosition < 0.99f) {
            int x = (int)(buf.getWidth() * playbackPosition);
            graphics.setColor(Color.RED);
            graphics.setXORMode(Color.GREEN);
            graphics.drawLine(x, 0, x, buf.getHeight());
            graphics.drawLine(x + 1, 0, x + 1, buf.getHeight());
        }

        // Draw the marker position if any:
        if (markPosition > 0f) {
            int x = (int)(imagePanel.getWidth() * markPosition);
            graphics.setColor(Color.BLACK);
            graphics.setXORMode(Color.WHITE);
            graphics.drawLine(x, 0, x, imagePanel.getHeight());
        }

        // Draw the selection, if any:
        if (selectionStart > 0f || selectionEnd > 0f) {
            int x1 = (int)(imagePanel.getWidth() * selectionStart);
            int x2 = (int)(imagePanel.getWidth() * selectionEnd);
            graphics.setColor(Color.RED);
            graphics.setXORMode(Color.GREEN);
            graphics.fillRect(x1, 0, x2 - x1, imagePanel.getHeight());
        }

        graphics.dispose();
        imagePanel.setImage(buf);
    }

    private void handleImagePanelClick(MouseEvent e) {
        // Ignore this click if we're not idle or if we have no audio data:
        if (panelState != PanelState.IDLE || audioData == null) {
            return;
        }

        // Clear previous mark point or selection:
        markPosition = 0f;
        selectionStart = 0f;
        selectionEnd = 0f;

        // If it was a left click, set the new mark point:
        if (e.getButton() == MouseEvent.BUTTON1) {
            markPosition = e.getX() / (float)imagePanel.getWidth();
        }

        // Redraw with these settings:
        redrawWaveform();
    }

    private void handleImagePanelDrag(MouseEvent e) {
        // Ignore this drag if we're not idle or if we have no audio data:
        if (panelState != PanelState.IDLE || audioData == null) {
            return;
        }

        // Clear any mark position:
        markPosition = 0f;

        // Where was this mouse event triggered?
        float mouseX = e.getX() / (float)imagePanel.getWidth();
        mouseX = Math.max(0f, mouseX);
        mouseX = Math.min(1f, mouseX);

        if (!mouseDragging) {
            mouseDragging = true;
            selectionStart = selectionEnd = mouseX;
        }
        else {
            if (mouseX > selectionStart) {
                selectionEnd = mouseX;
            }
            else {
                selectionStart = mouseX;
            }
        }

        redrawWaveform();
    }

    /**
     * Notifies all listeners that an audio clip has been loaded into this panel.
     */
    private void fireAudioLoadedEvent() {
        for (AudioPanelListener listener : panelListeners) {
            listener.audioLoaded(this);
        }
    }

    /**
     * Notifies all listeners that our state has changed.
     */
    private void fireStateChangedEvent() {
        for (AudioPanelListener listener : panelListeners) {
            listener.stateChanged(this, panelState);
        }
    }

    /**
     * Notifies all listeners that a recording has been made within the panel
     * and is available for playback or saving.
     */
    private void fireRecordingCompleteEvent() {
        for (AudioPanelListener listener : panelListeners) {
            listener.recordingComplete(this);
        }
    }

    /**
     * Notifies all listeners that audio has stopped (this is in addition to the
     * stateChanged they will also receive). The distinction here is that we will
     * supply the reason why the audio stopped playing.
     *
     * @param stopReason Why did the audio stop?
     */
    private void fireAudioStoppedEvent(PlaybackThread.StopReason stopReason) {
        for (AudioPanelListener listener : panelListeners) {
            listener.audioStopped(stopReason);
        }
    }

    /**
     * Builds and returns a button with the specified icon loaded from resources, and
     * with the icon and button set to the given pixel size.
     *
     * @param iconResourceName The path of the image icon resource for the button.
     * @param description      The text description of this button.
     * @param btnSize          The desired pixel dimensions of the button icon (assumes square icon).
     * @param iconSize         The desired pixel dimensions of the button icon (assumes square button).
     * @return A JButton that can be added to a toolbar.
     */
    public JButton buildToolBarButton(String iconResourceName,
                                      String description, int btnSize, int iconSize) {
        JButton button = new JButton();
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setPreferredSize(new Dimension(btnSize, btnSize));
        BufferedImage iconImage = loadIconResource(iconResourceName, iconSize, iconSize);
        ImageIcon icon = new ImageIcon(iconImage, description);
        button.setIcon(icon);
        button.setToolTipText(description);

        return button;
    }

    /**
     * Loads and returns an image icon resource, scaling up or down to the given size if needed.
     *
     * @param resourceName The path to the resource file containing the image.
     * @param width        The desired width of the image.
     * @param height       The desired height of the image.
     * @return An image, loaded and scaled, or null if the resource was not found.
     */
    private BufferedImage loadIconResource(String resourceName, int width, int height) {
        BufferedImage image = null;
        try {
            URL url = getClass().getResource(resourceName);
            image = ImageUtil.loadImage(url);

            // If the width or height don't match, scale it up or down as needed:
            if (image.getWidth() != width || image.getHeight() != height) {
                image = ImageUtil.generateThumbnailWithTransparency(image, width, height);
            }
        }
        catch (IOException ioe) {
            getMessageUtil().error("Error loading image: " + resourceName, ioe);
        }

        return image;
    }

    private MessageUtil getMessageUtil() {
        if (messageUtil == null) {
            messageUtil = new MessageUtil(this, logger);
        }
        return messageUtil;
    }

}
