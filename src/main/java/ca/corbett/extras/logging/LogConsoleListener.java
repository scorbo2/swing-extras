package ca.corbett.extras.logging;

/**
 * Can be used to listen for theme or style changes in a LogConsole.
 *
 * @author scorbett
 * @since 2023-03-17
 */
public abstract class LogConsoleListener {

  public abstract void logConsoleThemeChanged(String newThemeName);

  public abstract void logConsoleFontSizeChanged(int fontPointSize);

}
