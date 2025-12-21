package ca.corbett.extras;

import org.junit.jupiter.api.AfterEach;
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

    @AfterEach
    void tearDown() {
        // restore default provider and release any socket
        SingleInstanceManager.setServerSocketProvider(null);
        SingleInstanceManager.getInstance().release();
    }

    @Test
    void tryAcquireLock_withPreboundServerSocket_receivesArgs() throws Exception {
        ServerSocket prebound = new ServerSocket(55555);
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

        String[] toSend = new String[] { "one", "two" };
        SingleInstanceManager.getInstance().sendArgsToRunningInstance(toSend);

        boolean got = latch.await(2, TimeUnit.SECONDS);
        assertTrue(got, "Listener should be invoked");
        assertEquals(Arrays.asList(toSend), received);

        prebound.close();
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
        int defaultPort = SingleInstanceManager.DEFAULT_PORT;

        ServerSocket prebound = new ServerSocket(defaultPort);
        int port = prebound.getLocalPort();

        // Inject provider that returns our pre-bound ServerSocket for the requested port
        SingleInstanceManager.setServerSocketProvider(requestedPort -> {
            if (requestedPort != port) {
                throw new IOException("unexpected port: " + requestedPort);
            }
            return prebound;
        });

        boolean isPrimaryInstance = SingleInstanceManager.getInstance().tryAcquireLock(null);
        assertTrue(isPrimaryInstance, "Should acquire lock on default port " + defaultPort);
        assertTrue(SingleInstanceManager.getInstance().isListening());
        assertEquals(defaultPort, SingleInstanceManager.getInstance().getListeningPort());
        prebound.close();
    }

    @Test
    void tryAcquireLock_withInvalidPort_shouldReject() throws Exception {
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
}
