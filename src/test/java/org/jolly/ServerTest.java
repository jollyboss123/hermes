package org.jolly;

import org.jolly.protocol.Token;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    void testSetAndGetCommand() throws IOException {
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
    void testThread() {
        String setCommand = "*3\r\n$3\r\nSET\r\n$5\r\nhello\r\n$5\r\nworld\r\n";
        String getCommand = "*2\r\n$3\r\nGET\r\n$5\r\nhello\r\n";

        AtomicReference<String> setResponse = new AtomicReference<>();
        AtomicReference<String> getResponse = new AtomicReference<>();

        int threadCount = 2;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            int finalI = i;
            new Thread(() -> {
                try {
                    awaitOnLatch(startLatch);
                    setResponse.set(new String(client.sendCommand(setCommand), StandardCharsets.UTF_8));
                    getResponse.set(new String(client.sendCommand(getCommand), StandardCharsets.UTF_8));
                } catch (IOException e) {
                    exceptions.add(e);
                } finally {
                    log.info(() -> "counting down: " + finalI);
                    endLatch.countDown();
                }
            }).start();
        }

        log.info(() -> "starting threads");
        startLatch.countDown();
        log.info(() -> "main threads waiting for all command threads to finish");
        awaitOnLatch(endLatch);

        assertEquals("+OK\r\n", setResponse.get(), "Expected positive acknowledgment for SET command");
        assertEquals("*2\r\n$5\r\nhello\r\n$5\r\nworld\r\n", getResponse.get(), "Expected fetched value to match the set value");

        Token key = Token.bulkString("hello");
        Token expectedValue = Token.bulkString("world");
        Token actualValue = kv.get(key);
        assertEquals(expectedValue, actualValue, "KV store should return the correct value for 'hello'");
    }

    @Test
    void testVirtualThreads() throws InterruptedException {
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
