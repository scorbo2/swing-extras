/**
 * Contains the LogConsole and associated classes - useful for showing log information
 * for your Swing application at runtime.
 * <h2>LogConsole</H2>
 * LogConsole is a singleton frame that contains a styleable text pane showing log
 * messages for your application. You must specify "ca.corbett.extras.logging.LogConsoleHandler"
 * in the "handlers" list in your logging.properties in order for LogConsole to receive
 * log messages. A SimpleFormatter is used by default, so it will use either the default
 * SimpleFormatter configuration, or whatever format you have specified for
 * java.util.logging.SimpleFormatter.format in your logging.properties.
 * <h2>Themes and Styles</h2>
 * The advantage of LogConsole instead of just tailing the log file on disk is that
 * the log output can be styled in interesting ways:
 * <ul>
 * <li><b>Log level styling</b> - you can specify a different text color or font properties
 * based on the Level of each log message. For example, errors can show up in bold
 * red text, while everything else shows up in plain black text.
 * <li><b>Custom log message styling</b> - you can set up a LogConsoleStyle that looks for
 * specific string tokens within log messages, and set a special style for them. For example,
 * if your application generates logging related to Foo, you can set a LogConsoleStyle to
 * watch for messages containing "Foo" and have them appear in blue text. You can use this
 * to visually aid in spotting interesting log messages as your application does what it does.
 * </ul>
 * Note that log styling only applies within the LogConsole - none of this styling will
 * be written to your log file, or to the console.
 * <h2>Launching the LogConsole</h2>
 * You can programmatically call LogConsole.getInstance().setVisible(true) in your code,
 * or you can use the "showLogConsole" property in AboutInfo to provide a way of launching
 * the LogConsole from the AboutPanel, wherever that appears in your application.
 * <h2>Using LogConsole without java.util.logging</h2>
 * Even if you aren't using logging.properties and the usual java.util.logging facilities,
 * you can still use LogConsole manually by invoking its append() method, which
 * is the same method that the logging facilities would use to add a new log message.
 * This allows you to use LogConsole even if you have no logging setup at all. In this case,
 * you don't even need to set up the LogConsoleHandler as it will not be needed.
 */
package ca.corbett.extras.logging;
