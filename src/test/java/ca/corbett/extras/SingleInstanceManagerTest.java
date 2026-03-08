package ca.corbett.extras;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SingleInstanceManagerTest {

    @BeforeAll
    static void setup() {
        // Ensure each test starts with a fresh instance
        SingleInstanceManager.getInstance().release();

        // Don't show error dialogs in unit tests!
        SingleInstanceManager.getInstance().setShowErrorDialogOnArgSendFailure(false);
    }

    @AfterEach
    void tearDown() {
        // restore default provider and release any socket
        SingleInstanceManager.setServerSocketProvider(null);
        SingleInstanceManager.getInstance().release();
    }

    @Test
    void tryAcquireLock_withPreboundServerSocket_receivesArgs() throws Exception {
        // Let OS assign an available port:
        try (ServerSocket prebound = new ServerSocket(0)) {
            int port = prebound.getLocalPort();

            // Inject provider that returns our pre-bound ServerSocket for the requested port
            SingleInstanceManager.setServerSocketProvider(requestedPort -> {
                if (requestedPort != port) {
                    throw new IOException("unexpected port: " + requestedPort);
                }
                return prebound;
            });

            CountDownLatch latch = new CountDownLatch(1);
            List<String> received = Collections.synchronizedList(new ArrayList<>());

            boolean primary = SingleInstanceManager.getInstance().tryAcquireLock(args -> {
                received.addAll(args);
                latch.countDown();
            }, port);

            assertTrue(primary, "Should become primary when provider supplies a ServerSocket");
            assertTrue(SingleInstanceManager.getInstance().isListening());
            assertEquals(port, SingleInstanceManager.getInstance().getListeningPort());

            String[] toSend = new String[]{"one", "two"};
            SingleInstanceManager.getInstance().sendArgsToRunningInstance(toSend);

            boolean got = latch.await(2, TimeUnit.SECONDS);
            assertTrue(got, "Listener should be invoked");
            assertEquals(Arrays.asList(toSend), received);
        }
    }

    @Test
    void tryAcquireLock_whenProviderThrows_returnsFalse() {
        SingleInstanceManager.setServerSocketProvider(p -> { throw new IOException("simulated in use"); });
        boolean isPrimaryInstance = SingleInstanceManager.getInstance().tryAcquireLock(null, 12345);
        assertFalse(isPrimaryInstance);
    }

    @Test
    void tryAcquireLock_withDefaultPort_shouldLockDefaultPort() throws Exception {
        // When we use the single-argument version of tryAcquireLock, it should
        // use the default port defined in SingleInstanceManager.
        // But! It is not a safe assumption that the default port is available on the test system,
        // so we will pre-bind a ServerSocket to an OS-assigned port, and have our provider
        // return that socket when the default port is requested.
        try (ServerSocket prebound = new ServerSocket(0)) {
            SingleInstanceManager.setServerSocketProvider(requestedPort -> {
                // Verify it's requesting the default port constant
                if (requestedPort != SingleInstanceManager.DEFAULT_PORT) {
                    throw new IOException("unexpected port: " + requestedPort);
                }
                // But return a socket on an available port
                return prebound;
            });

            boolean isPrimaryInstance = SingleInstanceManager.getInstance().tryAcquireLock(null);
            assertTrue(isPrimaryInstance);
        }
    }

    @Test
    void tryAcquireLock_withInvalidPort_shouldReject() {
        int[] invalidPorts = new int[]{-1, 0, 80, 65536};
        int exceptionsCaught = 0;
        for (int port : invalidPorts) {
            try {
                SingleInstanceManager.getInstance().tryAcquireLock(null, port);
            }
            catch (IllegalArgumentException e) {
                exceptionsCaught++;
                assertTrue(e.getMessage().contains("Port must be between 1024 and 65535"));
            }
        }
        assertEquals(invalidPorts.length, exceptionsCaught, "Should catch expected exceptions for all invalid ports");
    }

    @Test
    void release_whenNotPrimary_doesNothing() {
        // Should not throw any exceptions
        SingleInstanceManager.getInstance().release();
        assertFalse(SingleInstanceManager.getInstance().isListening());
    }

    @Test
    void release_shouldAllowReacquire() throws Exception {
        // Grab an OS-assigned port and then release it,
        // so that we know it's available for our test:
        int port;
        try (ServerSocket temp = new ServerSocket(0)) {
            port = temp.getLocalPort();
        }

        boolean isPrimary = SingleInstanceManager.getInstance().tryAcquireLock(null, port);
        assertTrue(isPrimary, "Should become primary instance on port " + port);

        SingleInstanceManager.getInstance().release();
        assertFalse(SingleInstanceManager.getInstance().isListening());

        // There's an extremely intermittent failure in this test (observed on Linux).
        // It seems that very rarely, the OS doesn't immediately release the port after closing the socket,
        // causing the reacquire attempt to fail. I can't make it fail reliably, so to try to work around this,
        // we will retry a few times before failing the test.
        boolean isPrimaryAgain = false;
        int maxAttempts = 10;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            if (SingleInstanceManager.getInstance().tryAcquireLock(null, port)) {
                isPrimaryAgain = true;
                break;
            }
            Thread.sleep(50); // small backoff
        }

        // If we still failed after retries, then it might be a legitimate failure:
        assertTrue(isPrimaryAgain, "Should be able to reacquire lock on port " + port + " after release");
    }

    @Test
    void sendArgsToRunningInstance_whenNotListening_doesNothing() {
        // Should not throw any exceptions
        SingleInstanceManager.getInstance().sendArgsToRunningInstance(new String[]{"arg1", "arg2"});
    }

    @Test
    void sendArgsToRunningInstance_withNoArgs_sendsEmpty() throws Exception {
        // Grab an OS-assigned port and then release it,
        // so that we know it's available for our test:
        int port;
        try (ServerSocket temp = new ServerSocket(0)) {
            port = temp.getLocalPort();
        }

        CountDownLatch latch = new CountDownLatch(1);
        List<String> received = Collections.synchronizedList(new ArrayList<>());

        boolean primary = SingleInstanceManager.getInstance().tryAcquireLock(args -> {
            received.addAll(args);
            latch.countDown();
        }, port);

        assertTrue(primary, "Should become primary instance");

        SingleInstanceManager.getInstance().sendArgsToRunningInstance(new String[]{});

        boolean got = latch.await(2, TimeUnit.SECONDS);
        assertTrue(got, "Listener should be invoked");
        assertEquals(Collections.emptyList(), received);
    }
}
