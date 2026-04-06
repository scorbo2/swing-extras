package ca.corbett.extras;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Fallback handler for any exception that goes uncaught in any thread.
 * This class ensures that the exception gets logged with the full stack trace.
 * By default, uncaught exceptions print a stack trace to the console, but that
 * is not helpful if the application wasn't started via a terminal.
 * <p>
 * Use the static register() method to set this as the default uncaught
 * exception handler for all threads in your application startup code.
 * </p>
 * <p>
 * Note that this handler will only be used if the current Thread
 * and/or ThreadGroup does not have its own uncaught exception handler.
 * This class represents a last-ditch fallback.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.9
 */
public class FallbackExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final Logger log = Logger.getLogger(FallbackExceptionHandler.class.getName());

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.log(Level.SEVERE, "Uncaught exception in thread " + t.getName() + ": " + e.getMessage(), e);
    }

    /**
     * Registers an instance of this handler as the default
     * uncaught exception handler for all threads in the application.
     */
    public static void register() {
        Thread.setDefaultUncaughtExceptionHandler(new FallbackExceptionHandler());
    }
}
