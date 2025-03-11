package ca.corbett.extras.logging;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

/**
 * A singleton LogConsole window that works with LogConsoleHandler to display
 * log messages within an application (viewable via the AboutPanel or by
 * showing it programmatically). Works transparently with any application, provided the logging
 * configuration is set up for it: you must specify "ca.corbett.extras.logging.LogConsoleHandler"
 * in the "handlers" list in your logging.properties in order for LogConsole to receive
 * log messages. Alternatively, you can skip logging.properties and java.util.logging
 * altogether and log directly to LogConsole via the append() method.
 * <p>
 * Somewhere in your startup code, you can set an ImageIcon and a title
 * for the window (default is no icon and "Log console" title):
 * </p>
 * <blockquote>
 * LogConsole.getInstance().setIconImage(Toolkit.getDefaultToolkit().createImage("/some/image.png"));<br>
 * LogConsole.getInstance().setTitle(Version.NAME + " log console");
 * </blockquote>
 * <p>
 * You also have the option of specifying custom styling for your application's log
 * messages, based either on the log.Level of each log message, or by looking for some
 * string token within each log message. See LogConsoleTheme and LogConsoleStyle for
 * details on setting up custom log styling.
 * </p>
 * <p>
 * Out of the box, LogConsole supports three themes: default (black text on white background),
 * matrix (green text on black background), and paper (black text on grey background). These
 * are provided as conveniences, or you can add your own theme by creating and configuring
 * an instance of this class.
 * </p>
 *
 * @author scorbo2
 * @since 2023-03-17
 */
public final class LogConsole extends JFrame implements ChangeListener {

  /**
   * How many log messages to store in memory, or 0 for no limit.
   */
  public static int LOG_LIMIT = 0;

  private final List<LogConsoleListener> listeners = new ArrayList<>();

  private final List<String> logHistory = new ArrayList<>(1000);
  private final List<Level> logLevelHistory = new ArrayList<>(1000);

  private final List<String> logQueue = new ArrayList<>(1000);
  private final List<Level> logLevelQueue = new ArrayList<>(1000);

  private final Map<String, LogConsoleTheme> registeredThemes = new HashMap<>();
  private LogConsoleTheme currentTheme;

  private static LogConsole instance;

  private final JTextPane textPane;
  private DefaultComboBoxModel comboBoxModel;
  private JComboBox comboBox;
  private JSpinner fontSizeSpinner;

  private final ItemListener itemListener = new ItemListener() {
    @Override
    public void itemStateChanged(ItemEvent e) {
      switchTheme((String)comboBox.getSelectedItem());
    }

  };

  private LogConsole() {
    super("Log console");
    textPane = new JTextPane();
    textPane.setEditable(false);
    setSize(new Dimension(500, 400));
    setMinimumSize(new Dimension(200, 100));
    setResizable(true);
    setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    setLayout(new BorderLayout());
    JScrollPane scrollPane = new JScrollPane(textPane);
    add(scrollPane, BorderLayout.CENTER);
    add(buildButtonPanel(), BorderLayout.NORTH);

    registerTheme(LogConsoleTheme.DEFAULT_STYLE_NAME, LogConsoleTheme.createDefaultStyledTheme(), false);
    registerTheme("Matrix", LogConsoleTheme.createMatrixStyledTheme(), false);
    registerTheme("Paper", LogConsoleTheme.createPaperStyledTheme(), false);

    switchTheme(LogConsoleTheme.DEFAULT_STYLE_NAME);
  }

  public void addLogConsoleListener(LogConsoleListener listener) {
    listeners.add(listener);
  }

  public void removeLogConsoleListener(LogConsoleListener listener) {
    listeners.remove(listener);
  }

  /**
   * Returns an alphabetized list of theme names currently registered with this LogConsole.
   *
   * @return A list of registered theme names.
   */
  public SortedSet<String> getRegisteredThemeNames() {
    SortedSet<String> list = new TreeSet<>();
    list.addAll(registeredThemes.keySet());
    return list;
  }

  /**
   * Returns the name of the effective LogConsoleTheme.
   *
   * @return The name of the current theme.
   */
  public String getCurrentThemeName() {
    String themeName = null;
    for (String key : registeredThemes.keySet()) {
      if (registeredThemes.get(key) == currentTheme) {
        themeName = key;
        break;
      }
    }
    return themeName;
  }

  /**
   * Returns the font size currently in use in the LogConsole.
   *
   * @return a font point size.
   */
  public int getFontPointSize() {
    return (Integer)fontSizeSpinner.getValue();
  }

  /**
   * Sets the current font size for the LogConsole.
   *
   * @param size The new font point size.
   */
  public void setFontPointSize(int size) {
    fontSizeSpinner.setValue(size);
    currentTheme.setFontPointSize(size);
    fireFontSizeChangedEvent(size);
  }

  /**
   * Registers the given LogConsoleTheme for use with this LogConsole and then immediately
   * switches to it.
   *
   * @param name The name of the theme to register. Will overwrite if this theme exists.
   * @param theme The new theme.
   */
  public void registerTheme(String name, LogConsoleTheme theme) {
    registerTheme(name, theme, true);
  }

  /**
   * Registers the given LogConsoleTheme, and then optionally switches to it.
   *
   * @param name THe name of the theme to register. Will overwrite if this theme exists.
   * @param theme The new theme.
   * @param switchToImmediately If true, LogConsole will switch to the new theme immediately.
   */
  public void registerTheme(String name, LogConsoleTheme theme, boolean switchToImmediately) {
    // Add the name to the combo box if it wasn't already registered:
    if (comboBoxModel.getIndexOf(name) == -1) {
      comboBoxModel.addElement(name);
    }

    // Add or replace the registered theme by this name:
    registeredThemes.put(name, theme);

    if (switchToImmediately) {
      switchTheme(name);
    }
  }

  /**
   * Switches to the named theme, if that theme is registered with this LogConsole.
   * Does nothing if the given theme name is null or unrecognized.
   *
   * @param themeName The name of the theme in question.
   */
  public void switchTheme(String themeName) {
    if (themeName == null || themeName.trim().isEmpty()) {
      return;
    }
    LogConsoleTheme newTheme = registeredThemes.get(themeName);
    if (newTheme == null) {
      return;
    }

    if (currentTheme != null) {
      currentTheme.removeChangeListener(this);
    }

    currentTheme = newTheme;
    currentTheme.addChangeListener(this);

    comboBox.removeItemListener(itemListener);
    comboBox.setSelectedItem(themeName);
    comboBox.addItemListener(itemListener);

    for (String styleName : currentTheme.getStyleNames()) {
      LogConsoleStyle style = currentTheme.getStyle(styleName);
      setNamedStyle(styleName, style);
    }

    // Re-render with new style:
    styleChanged();

    fireThemeChangedEvent(themeName);

    // Make sure our new theme respects our selected font size:
    currentTheme.setFontPointSize((Integer)fontSizeSpinner.getValue());
  }

  /**
   * Returns the single instance of LogConsole.
   *
   * @return The LogConsole instance.
   */
  public static LogConsole getInstance() {
    if (instance == null) {
      instance = new LogConsole();
    }
    return instance;
  }

  /**
   * Removes all log messages from the console.
   */
  public void clear() {
    logHistory.clear();
    logLevelHistory.clear();
    textPane.setText("");
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);

    // If becoming visible, dequeue all queued-up log messages and write them out:
    if (visible) {
      for (int i = 0; i < logQueue.size(); i++) {
        append(logQueue.get(i), logLevelQueue.get(i));
      }
      logQueue.clear();
      logLevelQueue.clear();
    }
  }

  /**
   * Appends a log message to this console with the given log level. Intended to be
   * invoked by LogConsoleHandler automatically when log messages are received, but can
   * also be invoked manually if you are not using the usual java.util.logging mechanism.
   *
   * @param msg The message to log.
   * @param level The log level.
   */
  public void append(String msg, Level level) {
    // If we're visible, process it now:
    if (isVisible()) {
      logHistory.add(msg);
      logLevelHistory.add(level);

      LogConsoleStyle consoleStyle = currentTheme.getMatchingStyle(msg, level);
      String styleName = currentTheme.getStyleName(consoleStyle);
      Style textPaneStyle = textPane.getStyle(styleName);
      if (textPaneStyle == null) {
        textPaneStyle = textPane.getStyle(LogConsoleTheme.DEFAULT_STYLE_NAME);
      }

      try {
        textPane.getDocument().insertString(textPane.getDocument().getLength(), msg, textPaneStyle);
        textPane.setCaretPosition(textPane.getDocument().getLength());
      }
      catch (BadLocationException ble) {

      }

      // Cleanup if required:
      if (LOG_LIMIT > 0) {
        while (logHistory.size() > LOG_LIMIT) {
          logHistory.remove(0);
          logLevelHistory.remove(0);
        }
      }
    }

    // Otherwise, queue it up for later (saves processing time if LogConsole not visible):
    else {
      logQueue.add(msg);
      logLevelQueue.add(level);

      // Cleanup if required:
      if (LOG_LIMIT > 0) {
        while (logQueue.size() > LOG_LIMIT) {
          logQueue.remove(0);
          logLevelQueue.remove(0);
        }
      }
    }
  }

  /**
   * Invoked internally to set font style properties for the named style.
   * The named style will be created if it does not yet exist.
   *
   * @param styleName The name of the style to create or update.
   * @param consoleStyle The LogConsoleStyle instance to use for styling.
   */
  private void setNamedStyle(String styleName, LogConsoleStyle consoleStyle) {
    Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
    Style style = textPane.getStyle(styleName);
    if (style == null) {
      style = textPane.addStyle(styleName, def);
    }
    StyleConstants.setFontFamily(style, consoleStyle.getFontFamilyName());
    StyleConstants.setFontSize(style, consoleStyle.getFontPointSize());
    StyleConstants.setForeground(style, consoleStyle.getFontColor());
    if (consoleStyle.getFontBgColor() != null) {
      StyleConstants.setBackground(style, consoleStyle.getFontBgColor());
    }
    StyleConstants.setBold(style, consoleStyle.isBold());
    StyleConstants.setItalic(style, consoleStyle.isItalic());
    StyleConstants.setUnderline(style, consoleStyle.isUnderline());
  }

  /**
   * Removes all content and re-appends it to pick up any style changes (fonts, colours, etc).
   */
  private void styleChanged() {
    // Setting the background on a JTextPane is not supported on
    // some look and feel implementations, most notably Nimbus.
    // Therefore, this will look like ass on those l&fs.
    textPane.setBackground(currentTheme.getDefaultBgColor());
    textPane.setText("");

    // Copy the lists to iterate over because append() will add to the master lists.
    List<String> logCopy = new ArrayList<>();
    List<Level> levelCopy = new ArrayList<>();
    logCopy.addAll(logHistory);
    levelCopy.addAll(logLevelHistory);
    logHistory.clear();
    logLevelHistory.clear();
    for (int i = 0; i < logCopy.size(); i++) {
      append(logCopy.get(i), levelCopy.get(i));
    }

    // TODO update font size spinner
  }

  /**
   * Invoked internally to build the control panel.
   */
  private JPanel buildButtonPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new FlowLayout(FlowLayout.LEFT));

    String[] options = new String[]{LogConsoleTheme.DEFAULT_STYLE_NAME, "Matrix", "Paper"};
    comboBoxModel = new DefaultComboBoxModel(options);
    comboBox = new JComboBox(comboBoxModel);
    comboBox.setEditable(false);
    comboBox.addItemListener(itemListener);
    panel.add(comboBox);

    JButton button = new JButton("Copy all to clipboard");
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(textPane.getText()), null);
      }

    });
    panel.add(button);

    button = new JButton("Clear");
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        clear();
      }

    });
    panel.add(button);

    JLabel label = new JLabel("  Font size:");
    panel.add(label);
    fontSizeSpinner = new JSpinner(new SpinnerNumberModel(12, 8, 48, 1));
    panel.add(fontSizeSpinner);
    fontSizeSpinner.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        currentTheme.setFontPointSize((Integer)fontSizeSpinner.getValue());
        fireFontSizeChangedEvent((Integer)fontSizeSpinner.getValue());
      }

    });

    return panel;
  }

  @Override
  public void stateChanged(ChangeEvent e) {
    for (String styleName : currentTheme.getStyleNames()) {
      LogConsoleStyle style = currentTheme.getStyle(styleName);
      setNamedStyle(styleName, style);
    }

    // Re-render with new style:
    styleChanged();
  }

  private void fireThemeChangedEvent(String newTheme) {
    for (LogConsoleListener listener : listeners) {
      listener.logConsoleThemeChanged(newTheme);
    }
  }

  private void fireFontSizeChangedEvent(int newFontSize) {
    for (LogConsoleListener listener : listeners) {
      listener.logConsoleFontSizeChanged(newFontSize);
    }
  }

}
