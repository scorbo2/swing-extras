package ca.corbett.extras.demo.panels;

import ca.corbett.extras.MessageUtil;
import ca.corbett.extras.audio.AudioPanelListener;
import ca.corbett.extras.audio.AudioUtil;
import ca.corbett.extras.audio.AudioWaveformPanel;
import ca.corbett.extras.audio.WaveformConfig;
import ca.corbett.extras.audio.WaveformConfigPanel;
import ca.corbett.extras.audio.WaveformPanelConfigPanel;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a way to test out the AudioUtils, and more specifically, the AudioWaveformPanel.
 * Apologies: this code predates swing-forms and is therefore a little ugly. One day I'll
 * sit down and rewrite it to get rid of all the GridBagLayout stuff.
 *
 * @author scorbo2
 * @since 2018-01-04
 */
public final class AudioDemoPanel extends PanelBuilder implements AudioPanelListener {
    private MessageUtil messageUtil;
    private final JFileChooser fileChooser;
    private JComboBox audioSourceCombo;
    private JLabel audioFileLabel;
    private JTextField audioFileTextField;
    private JButton audioFileButton;
    private File exampleAudioFile;
    private File selectedAudioFile;
    private File recordedAudioFile;
    private final WaveformConfig wavePrefs;
    private WaveformConfigPanel wavePrefsPanel;
    private WaveformPanelConfigPanel audioPanelPrefsPanel;
    private final JLabel waveformLabel;
    private JPanel panel;

    private static final int DEFAULT_XSCALE = 512;
    private static final int DEFAULT_YSCALE = 64;

    private AudioWaveformPanel waveformPanel;

    public AudioDemoPanel() {
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        wavePrefs = new WaveformConfig();
        wavePrefs.setXScale(DEFAULT_XSCALE);
        wavePrefs.setYScale(DEFAULT_YSCALE);

        waveformLabel = new JLabel("(not yet generated)");

        try {
            InputStream inStream = getClass().getResourceAsStream("/swing-extras/audio/motorcycle-startup.wav");
            BufferedInputStream buf = new BufferedInputStream(inStream);
            exampleAudioFile = File.createTempFile("audio_", "_example.wav");
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(exampleAudioFile));
            buf.transferTo(out);
            buf.close();
            out.close();
        }
        catch (IOException ioe) {
            Logger.getLogger(AudioDemoPanel.class.getName()).log(Level.SEVERE, "Unable to load example audio file.", ioe);
        }
    }

    @Override
    public String getTitle() {
        return "Audio";
    }

    @Override
    public JPanel build() {
        panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        JLabel spacer = new JLabel("");
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0.5;
        panel.add(spacer, constraints);

        spacer = new JLabel("");
        constraints.gridx = 3;
        panel.add(spacer, constraints);

        waveformPanel = new AudioWaveformPanel();
        waveformPanel.addAudioPanelListener(this);
        waveformPanel.setWaveformPreferences(wavePrefs);

        String s = "Audio panel preferences";
        audioPanelPrefsPanel = new WaveformPanelConfigPanel(s, waveformPanel);
        constraints.gridx = 1;
        constraints.gridwidth = 1;
        constraints.gridy++;
        constraints.insets = new Insets(12, 2, 2, 2);
        constraints.weightx = 0;
        constraints.fill = GridBagConstraints.BOTH;
        panel.add(audioPanelPrefsPanel, constraints);

        wavePrefsPanel = new WaveformConfigPanel(wavePrefs);
        constraints.gridx = 2;
        constraints.gridheight = 2;
        panel.add(wavePrefsPanel, constraints);

        JPanel controlPanel = buildControlPanel(panel);
        constraints.gridx = 1;
        constraints.gridy++;
        constraints.gridheight = 1;
        constraints.insets = new Insets(2, 2, 2, 2);
        panel.add(controlPanel, constraints);

        constraints.gridy++;
        waveformPanel.setPreferredSize(new Dimension(100, 120));
        waveformPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(20, 10, 4, 10);
        constraints.gridwidth = 2;
        panel.add(waveformPanel, constraints);

        constraints.gridx = 1;
        constraints.gridy++;
        constraints.insets = new Insets(4, 4, 4, 4);
        constraints.anchor = GridBagConstraints.WEST;
        panel.add(waveformLabel, constraints);

        spacer = new JLabel("");
        constraints.gridx = 0;
        constraints.gridwidth = 5;
        constraints.gridy = 99;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weighty = 1;
        panel.add(spacer, constraints);

        return panel;
    }

    public void generateWaveform() {
        waveformPanel.clear();
        try {
            if (audioSourceCombo.getSelectedIndex() == 0) {
                waveformPanel.setWaveformPreferences(wavePrefs);
                waveformPanel.setAudioClip(exampleAudioFile);
            }

            else if (audioSourceCombo.getSelectedIndex() == 1) {
                if (selectedAudioFile == null || !selectedAudioFile.exists() || !selectedAudioFile.canRead()) {
                    return;
                }

                waveformPanel.setWaveformPreferences(wavePrefs);
                waveformPanel.setAudioClip(selectedAudioFile);
            }

            else {
                if (recordedAudioFile == null || !recordedAudioFile.exists() || ! recordedAudioFile.canRead()) {
                    return;
                }

                waveformPanel.setWaveformPreferences(wavePrefs);
                waveformPanel.setAudioClip(recordedAudioFile);
            }

            Dimension dim = waveformPanel.getWaveformDimensions();
            if (dim != null) {
                waveformLabel.setText("Waveform image size: "
                        + ((int)dim.getWidth()) + "x" + ((int)dim.getHeight()) + " pixels");
            }
            else {
                waveformLabel.setText("");
            }
        }
        catch (IOException | UnsupportedAudioFileException e) {
            getMessageUtil().error("Load Error", "Error loading audio data: " + e.getMessage(), e);
        }
    }

    private JPanel buildControlPanel(final JPanel ownerPanel) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        JLabel label = new JLabel("Audio source:");
        Font labelFont = new Font("SansSerif", 0, 12);
        label.setFont(labelFont);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.insets = new Insets(2, 2, 2, 2);
        constraints.anchor = GridBagConstraints.EAST;
        panel.add(label, constraints);

        String[] items = new String[]{"Motorcycle startup", "Choose a file", "Manual recording"};
        audioSourceCombo = new JComboBox(items);
        audioSourceCombo.setEditable(false);
        audioSourceCombo.setSelectedIndex(0);
        audioSourceCombo.setFont(labelFont);
        constraints.gridx = 1;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.WEST;
        audioSourceCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                audioFileLabel.setEnabled(audioSourceCombo.getSelectedIndex() == 1);
                audioFileTextField.setEnabled(audioSourceCombo.getSelectedIndex() == 1);
                audioFileButton.setEnabled(audioSourceCombo.getSelectedIndex() == 1);
                generateWaveform();
            }

        });
        panel.add(audioSourceCombo, constraints);

        audioFileLabel = new JLabel("Audio file:");
        audioFileLabel.setFont(labelFont);
        audioFileLabel.setEnabled(false);
        constraints.gridx = 0;
        constraints.gridwidth = 1;
        constraints.gridy++;
        constraints.anchor = GridBagConstraints.EAST;
        panel.add(audioFileLabel, constraints);

        audioFileTextField = new JTextField();
        audioFileTextField.setEditable(false);
        audioFileTextField.setEnabled(false);
        audioFileTextField.setColumns(14);
        audioFileTextField.setText("");
        constraints.gridx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        panel.add(audioFileTextField, constraints);

        audioFileButton = new JButton("Choose");
        audioFileButton.setPreferredSize(new Dimension(90, 20));
        audioFileButton.setEnabled(false);
        audioFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = fileChooser.showOpenDialog(ownerPanel);
                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedAudioFile = fileChooser.getSelectedFile();
                    audioFileTextField.setText(selectedAudioFile.getName());
                    generateWaveform();
                }
            }

        });
        constraints.gridx = 2;
        panel.add(audioFileButton, constraints);

        JButton button = new JButton("Generate waveform image");
        button.setFont(labelFont);
        button.setPreferredSize(new Dimension(280, 20));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateWaveform();
            }

        });
        constraints.gridx = 0;
        constraints.gridy++;
        constraints.gridwidth = 3;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.insets = new Insets(10, 4, 4, 4);
        panel.add(button, constraints);

        return panel;
    }

    private MessageUtil getMessageUtil() {
        if (messageUtil == null) {
            messageUtil = new MessageUtil(panel, Logger.getLogger(AudioDemoPanel.class.getName()));
        }
        return messageUtil;
    }

    @Override
    public void stateChanged(AudioWaveformPanel sourcePanel, AudioWaveformPanel.PanelState state) {
    }

    @Override
    public void recordingComplete(AudioWaveformPanel sourcePanel) {
        if (waveformPanel.getAudioData() == null) {
            getMessageUtil().warning("No audio data was captured - is there a mic connected?");
            return;
        }
        recordedAudioFile = null;
        try {
            File tempFile = File.createTempFile("audio_", "_recording.wav");
            AudioUtil.saveAudioFile(tempFile, waveformPanel.getAudioData());
            recordedAudioFile = tempFile;
            audioSourceCombo.setSelectedIndex(2);
            System.out.println("Write recorded audio to "+tempFile.getAbsolutePath());
        }
        catch (IOException ioe) {
            getMessageUtil().warning("Problem generating recording: "+ioe.getMessage());
        }
    }

    @Override
    public void audioLoaded(AudioWaveformPanel sourcePanel) {
    }
}
