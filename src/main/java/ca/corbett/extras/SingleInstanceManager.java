package ca.corbett.extras;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Allows an application to enforce a single running instance.
 * If a second instance is started, the new instance will send its arguments
 * to the primary instance and then immediately exit. This is great for
 * adding right-click support in your OS's file manager, because then
 * the user can quickly add new files to the already-running instance
 * instead of launching multiple instances of the application.
 * If enabled, it will also bring the main window of the primary instance
 * to the front if you select your application's icon from the system menu,
 * instead of starting a new instance.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a> (with copilot/claude)
 * @since swing-extras 2.6
 */
public class SingleInstanceManager {

    private static final Logger log = Logger.getLogger(SingleInstanceManager.class.getName());
    private MessageUtil messageUtil;

    // Use the initialization-on-demand holder idiom for a lazy, thread-safe singleton
    private SingleInstanceManager() {
    }

    /**
     * The singleton instance holder.
     */
    private static class Holder {
        private static final SingleInstanceManager INSTANCE = new SingleInstanceManager();
    }

    /**
     * Returns the singleton instance of SingleInstanceManager.
     */
    public static SingleInstanceManager getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * The port number should be something that is unlikely to conflict
     * with any other application. Just pick a random high number.
     * Callers can override this default by calling tryAcquireLock with a different port.
     */
    private static final int DEFAULT_PORT = 44787;

    /**
     * Used to signal the end of arguments when sending to the primary instance.
     */
    private static final String ARGUMENT_END_SIGNAL = "-END-";

    private int port = DEFAULT_PORT; // Store the actual port being used
    private ServerSocket serverSocket;
    private ArgsListener argsListener;

    /**
     * Package-private functional provider used for tests to inject ServerSocket creation.
     * Tests in the same package can set a custom provider via setServerSocketProvider(...).
     */
    @FunctionalInterface
    interface ServerSocketProvider {
        ServerSocket create(int port) throws IOException;
    }

    // Default provider - creates a normal ServerSocket
    private static ServerSocketProvider serverSocketProvider = ServerSocket::new;

    // Package-private setter for tests; passing null restores default behavior
    static void setServerSocketProvider(ServerSocketProvider provider) {
        serverSocketProvider = (provider == null) ? ServerSocket::new : provider;
    }

    /**
     * Invoked when startup arguments are received from a new instance.
     */
    public interface ArgsListener {
        void argsReceived(List<String> args);
    }

    /**
     * Attempts to start as primary instance. Returns true if this is the primary
     * instance, false if another instance is already running.
     */
    public boolean tryAcquireLock(ArgsListener listener) {
        return tryAcquireLock(listener, DEFAULT_PORT);
    }

    /**
     * Attempts to start as primary instance using the specified port. Returns true if this is the primary
     * instance, false if another instance is already running.
     *
     * @param listener the listener to be notified when arguments are received from new instances
     * @param port the port number to use (must be between 1024 and 65535, non-privileged ports only)
     * @return true if this is the primary instance, false otherwise
     * @throws IllegalArgumentException if the port is outside the valid range or is a privileged port
     */
    public synchronized boolean tryAcquireLock(ArgsListener listener, int port) {
        if (port < 1024 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1024 and 65535 (non-privileged ports only), got: " + port);
        }

        this.argsListener = listener;
        this.port = port;

        try {
            // Use the injectable provider so tests can control creation/mocking
            serverSocket = serverSocketProvider.create(port);

            // Successfully bound to port - we're the primary instance
            startListening();
            log.info("SingleInstanceManager listening on port " + port);
            return true;
        } catch (IOException e) {
            // Port already in use - another instance is running, or we chose a bad port number
            log.info("This application is already running (port " + port + " is in use).");
            return false;
        }
    }

    /**
     * Sends the given arguments to the running instance.
     * Will log an exception if unable to connect.
     */
    public void sendArgsToRunningInstance(String[] args) {
        try (Socket socket = new Socket("localhost", port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // Send each argument
            for (String arg : args) {
                out.println(arg);
            }
            out.println(ARGUMENT_END_SIGNAL);

        } catch (IOException e) {
            getMessageUtil().error("SingleInstanceManager error",
                                   "Failed to connect to running instance on port " + port + ": " + e.getMessage(),
                                   e);
        }
    }

    /**
     * Start listening for connections from new instances.
     * We use a TCP port for this purpose, which may pose a problem
     * if the user has a firewall that blocks localhost connections.
     * An alternative approach would be to use a local domain socket,
     * but that would require more platform-specific code. We could
     * also use a file lock, but that can be unreliable on some platforms.
     * If the local TCP connection can't be initialized, then the
     * single instance feature will simply not work. Not much
     * we can do about that.
     */
    private void startListening() {
        Thread listenerThread = new Thread(() -> {
            while (!serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    handleClient(clientSocket);
                } catch (IOException e) {
                    if (!serverSocket.isClosed()) {
                        log.log(Level.SEVERE, "Error accepting connection: " + e.getMessage(), e);
                    }
                    else {
                        serverSocket = null;
                        break; // Server socket closed, exit loop
                    }
                }
            }
        }, "SingleInstanceManager-ListenerThread-" + System.currentTimeMillis());
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    /**
     * Handle incoming command-line arguments from new instances.
     */
    private void handleClient(Socket clientSocket) {
        new Thread(() -> {
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()))) {

                List<String> args = new ArrayList<>();
                String line;
                while ((line = in.readLine()) != null) {
                    if (ARGUMENT_END_SIGNAL.equals(line)) {
                        break;
                    }
                    args.add(line);
                }

                if (argsListener != null) {
                    // Notify on EDT for Swing apps
                    // Send even if empty, to bring window to front
                    javax.swing.SwingUtilities.invokeLater(() -> argsListener.argsReceived(args));
                }

            } catch (IOException e) {
                log.log(Level.SEVERE, "Error reading from client: " + e.getMessage(), e);
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    log.log(Level.SEVERE, "Error closing client socket: " + e.getMessage(), e);
                }
            }
        }, "SingleInstanceManager-clientHandler-" + System.currentTimeMillis()).start();
    }

    /**
     * Release the lock on shutdown. This should be invoked by the application's
     * shutdown hook to clean up the server socket. Note that for swing-extras
     * applications that use the UpdateManager, the cleanup code should be registered
     * as a shutdownHook with the UpdateManager so that it runs when
     * the UpdateManager initiates an application restart.
     */
    public void release() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                log.info("SingleInstanceManager on port " + port + " has been shut down.");
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error closing server socket: " + e.getMessage(), e);
        }
    }

    private MessageUtil getMessageUtil() {
        if (messageUtil == null) {
            messageUtil = new MessageUtil(null, log);
        }
        return messageUtil;
    }
}
