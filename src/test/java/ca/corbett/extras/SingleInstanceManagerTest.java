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
}
