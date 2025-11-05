package ca.corbett.updates;

/**
 * Implementations of this interface can be optionally supplied to UpdateManager
 * to be invoked before the application is restarted. Application restarts are needed
 * as new extension jars are downloaded, installed, or uninstalled. The application may
 * need to do some cleanup before the application exits - close database connections,
 * save all work, etc. This hook provides a way to be notified before a restart.
 * <p>
 * <b>NOTE:</b> all registered shutdown hooks are triggered ONLY on application
 * restart, not when the application exits normally. This is not a replacement
 * for your normal cleanup mechanism!
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.5
 */
public interface ShutdownHook {

    /**
     * The application is about to restart. Perform whatever cleanup your
     * application requires here, before the restart happens.
     * <p>
     * <B>NOTE:</B> Don't start a worker thread to handle your cleanup!
     * As soon as the last shutdown hook returns, System.exit() is invoked.
     * Make sure your buffers are flushed, your changes are saved, and
     * your connections are closed!
     * </p>
     */
    void applicationWillRestart();
}
