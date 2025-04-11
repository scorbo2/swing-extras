package ca.corbett.extras.logging;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * A logging handler implementation specifically to support the LogConsole within
 * applications that use sc-util.
 * <p>
 * Your logging.properties should reference this class in the "handlers" directive:
 * </p>
 * <blockquote>
 * handlers=java.util.logging.FileHandler,ca.corbett.extras.logging.LogConsoleHandler
 * </blockquote>
 * The default logging level is ALL, but you can change this:
 * <blockquote>
 * ca.corbett.extras.logging.LogConsoleHandler.level=SEVERE
 * </blockquote>
 * This handler will use the SimpleFormatter, which you have presumably configured
 * in your logging.properties already.
 * <p>
 * Once configuration changes are in place, your application code can use Logger
 * methods to log as you normally would, and they will show up in the LogConsole.
 * </p>
 *
 * @author scorbo2
 * @since 2023-03-17
 */
public class LogConsoleHandler extends Handler {

    public LogConsoleHandler() {
        setFormatter(new SimpleFormatter());
    }

    @Override
    public void publish(LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }
        LogConsole.getInstance().append(getFormatter().format(record), record.getLevel());
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }

}
