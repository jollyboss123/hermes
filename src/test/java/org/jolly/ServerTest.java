package org.jolly;

import org.jolly.protocol.Token;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class ServerTest {
    private static final Logger log = Logger.getLogger(ServerTest.class.getName());

    private Server server;
    private MockClient client;
    private KV kv;

    @BeforeEach
    void setup() throws IOException {
        Config cfg = new Config(5002);
        kv = KV.create();
        server = new Server(cfg, new ConcurrentHashMap<>(), kv);

        Thread serverThread = new Thread(() -> server.start());
        serverThread.start();

        client = new MockClient("localhost", 5002);
    }

    @AfterEach
    void cleanup() throws IOException {
        client.close();
        server.stop();
    }

    @Test
    void singleRequest() throws IOException {
        String setCommand = "*3\r\n$3\r\nSET\r\n$5\r\nhello\r\n$5\r\nworld\r\n";
        String getCommand = "*2\r\n$3\r\nGET\r\n$5\r\nhello\r\n";

        String setResponse = new String(client.sendCommand(setCommand), StandardCharsets.UTF_8);
        assertEquals("+OK\r\n", setResponse, "Expected positive acknowledgment for SET command");

        String getResponse = new String(client.sendCommand(getCommand), StandardCharsets.UTF_8);
        assertEquals("*2\r\n$5\r\nhello\r\n$5\r\nworld\r\n", getResponse, "Expected fetched value to match the set value");

        Token key = Token.bulkString("hello");
        Token expectedValue = Token.bulkString("world");
        Token actualValue = kv.get(key);
        assertEquals(expectedValue, actualValue, "KV store should return the correct value for 'hello'");
    }

    @Test
    void multipleRequests() throws IOException {
        String setCommand = "*3\r\n$3\r\nSET\r\n$7\r\nhello_%d\r\n$7\r\nworld_%d\r\n";
        String getCommand = "*2\r\n$3\r\nGET\r\n$7\r\nhello_%d\r\n";

        for (int i = 0; i < 10; i++) {
            String setResponse = new String(client.sendCommand(setCommand.formatted(i, i)), StandardCharsets.UTF_8);
            assertEquals("+OK\r\n", setResponse, "Expected positive acknowledgment for SET command");

            String getResponse = new String(client.sendCommand(getCommand.formatted(i)), StandardCharsets.UTF_8);
            assertEquals("*2\r\n$7\r\nhello_%d\r\n$7\r\nworld_%d\r\n".formatted(i, i), getResponse, "Expected fetched value to match the set value");
        }

        assertEquals(10, kv.size());
    }

    @Test
    void sameKeyDiffVal() throws IOException {
        String setCommand = "*3\r\n$3\r\nSET\r\n$7\r\nhello_%d\r\n$7\r\nworld_%d\r\n";
        String getCommand = "*2\r\n$3\r\nGET\r\n$7\r\nhello_%d\r\n";

        for (int i = 0; i < 2; i++) {
            String setResponse = new String(client.sendCommand(setCommand.formatted(0, i)), StandardCharsets.UTF_8);
            assertEquals("+OK\r\n", setResponse, "Expected positive acknowledgment for SET command");

            String getResponse = new String(client.sendCommand(getCommand.formatted(0)), StandardCharsets.UTF_8);
            assertEquals("*2\r\n$7\r\nhello_%d\r\n$7\r\nworld_%d\r\n".formatted(0, i), getResponse, "Expected fetched value to match the set value");
        }

        assertEquals(1, kv.size());
    }

    @Test
    void multipleClientRequest() {
        int clients = 10;
        String setCommand = "*3\r\n$3\r\nSET\r\n$7\r\nhello_%d\r\n$7\r\nworld_%d\r\n";
        String getCommand = "*2\r\n$3\r\nGET\r\n$7\r\nhello_%d\r\n";

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(clients);
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());
        for (int i = 0; i < clients; i++) {
            int j = i;
            new Thread(() -> {
                try {
                    awaitOnLatch(startLatch);
                    MockClient c = new MockClient("localhost", 5002);

                    String setResponse = new String(c.sendCommand(setCommand.formatted(j, j)), StandardCharsets.UTF_8);
                    assertEquals("+OK\r\n", setResponse, "Expected positive acknowledgment for SET command");

                    String getResponse = new String(c.sendCommand(getCommand.formatted(j)), StandardCharsets.UTF_8);
                    assertEquals("*2\r\n$7\r\nhello_%d\r\n$7\r\nworld_%d\r\n".formatted(j, j), getResponse, "Expected fetched value to match the set value");
                } catch (IOException e) {
                    exceptions.add(e);
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }

        log.info("starting threads");
        startLatch.countDown();
        log.info("main threads waiting for all get and set threads to finish");
        awaitOnLatch(endLatch);

        assertEquals(10, kv.size());
    }

    @Disabled("pending handling of exceptions into error tokens")
    @Test
    void keyDoesNotExist() throws IOException {
        String setCommand = "*3\r\n$3\r\nSET\r\n$5\r\nhello\r\n$5\r\nworld\r\n";
        String getCommand = "*2\r\n$3\r\nGET\r\n$7\r\nhello_0\r\n";

        String setResponse = new String(client.sendCommand(setCommand), StandardCharsets.UTF_8);
        assertEquals("+OK\r\n", setResponse, "Expected positive acknowledgment for SET command");

        assertThrows(NoSuchElementException.class, () -> client.sendCommand(getCommand), "Expected no value for this key");
    }

    @Disabled("pending handling concurrent requests")
    @Test
    void concurrentRequestsBySameClient() {
        String setCommand = "*3\r\n$3\r\nSET\r\n$7\r\nhello_%d\r\n$7\r\nworld_%d\r\n";
        String getCommand = "*2\r\n$3\r\nGET\r\n$7\r\nhello_%d\r\n";

        int threadCount = 3;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            int j = i;
            new Thread(() -> {
                try {
                    awaitOnLatch(startLatch);
                    String setResponse = new String(client.sendCommand(setCommand.formatted(j, j)), StandardCharsets.UTF_8);
                    assertEquals("+OK\r\n", setResponse, "Expected positive acknowledgment for SET command");

                    String getResponse = new String(client.sendCommand(getCommand.formatted(j)), StandardCharsets.UTF_8);
                    assertEquals("*2\r\n$7\r\nhello_%d\r\n$7\r\nworld_%d\r\n".formatted(j, j), getResponse, "Expected fetched value to match the set value");
                } catch (IOException e) {
                    exceptions.add(e);
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }

        log.info(() -> "starting threads");
        startLatch.countDown();
        log.info(() -> "main threads waiting for all command threads to finish");
        awaitOnLatch(endLatch);

        assertEquals(8, kv.size());
    }

    @Disabled("pending handling concurrent requests")
    @Test
    void concurrentRequestBySameClient_virtualThreads() throws InterruptedException {
        String setCommand = "*3\r\n$3\r\nSET\r\n$5\r\nhello\r\n$5\r\nworld\r\n";
        String getCommand = "*2\r\n$3\r\nGET\r\n$5\r\nhello\r\n";

        AtomicReference<String> setResponse = new AtomicReference<>();
        AtomicReference<String> getResponse = new AtomicReference<>();
        List<Thread> setCommandThreads = IntStream.range(0, 1)
                .mapToObj(idx -> Thread.ofVirtual()
                        .name("set-thread-", idx)
                        .unstarted(() -> {
                            try {
                                setResponse.set(new String(client.sendCommand(setCommand)));
                                getResponse.set(new String(client.sendCommand(getCommand)));
                            } catch (IOException e) {
                                throw new IllegalStateException(e);
                            }
                        })).toList();

        setCommandThreads.forEach(Thread::start);
        for (int i = 0; i < 1; i++) {
            setCommandThreads.get(i).join();
        }

        assertEquals("+OK\r\n", setResponse.get(), "Expected positive acknowledgment for SET command");
        assertEquals("*2\r\n$5\r\nhello\r\n$5\r\nworld\r\n", getResponse.get(), "Expected fetched value to match the set value");

        Token key = Token.bulkString("hello");
        Token expectedValue = Token.bulkString("world");
        Token actualValue = kv.get(key);
        assertEquals(expectedValue, actualValue, "KV store should return the correct value for 'hello'");
    }

    private void awaitOnLatch(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }
}
