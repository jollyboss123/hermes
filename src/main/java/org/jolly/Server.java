package org.jolly;

import org.jolly.command.Command;
import org.jolly.command.GetCommand;
import org.jolly.command.SetCommand;
import org.jolly.protocol.ArrayToken;
import org.jolly.protocol.Serializer;
import org.jolly.protocol.Token;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Server {
    private static final Logger log = Logger.getLogger(Server.class.getName());
    private final Config cfg;
    private final Map<Peer, Boolean> peers;
    private final KV kv;
    private static final int DEFAULT_PORT = 5001;

    protected Server(Config cfg) {
        this(cfg, new ConcurrentHashMap<>(), KV.create());
    }

    protected Server(Config cfg, Map<Peer, Boolean> peers, KV kv) {
        this.cfg = cfg;
        this.peers = peers;
        this.kv = kv;
    }

    public static Server create(Config cfg) {
        if (cfg.getPort() == null) {
            cfg = new Config(DEFAULT_PORT);
        }
        return new Server(cfg);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(cfg.getPort());
             Socket clientSocket = serverSocket.accept();
             InputStream in = new BufferedInputStream(clientSocket.getInputStream());
             OutputStream out = new BufferedOutputStream(clientSocket.getOutputStream());
             ExecutorService executor = Executors.newCachedThreadPool()) {

            log.info(() -> "starting server ip: %s port: %d".formatted(serverSocket.getInetAddress().getHostAddress(), serverSocket.getLocalPort()));

            byte[] buf = new byte[1024];
            int n;
            while ((n = in.read(buf)) != -1) {
                handleConn(out, buf, n);
//                final int finalN = n;
//                byte[] copy = Arrays.copyOf(buf, n);
//                executor.submit(() -> {
//                    try {
//                        handleConn(out, copy, finalN);
//                    } catch (IOException e) {
//                        throw new IllegalStateException(e);
//                    }
//                });
            }

            log.info(() -> "server stopped");
        } catch (IOException e) {
            log.severe(() -> "failed to start server ip: %s".formatted(e.getMessage()));
            System.exit(1);
        }
    }

    public void stop() {
        // cleanup
    }

    private void handleConn(OutputStream out, byte[] buf, int len) throws IOException {
        Peer peer = Peer.create(kv, out, buf, len);
        peers.put(peer, true);
        log.info(() -> "peer connected: %s".formatted(peer));

        handleMessage(peer.receive());
    }

    private void handleMessage(Message msg) throws IOException {
        log.info(() -> "handling and serializing: " + msg.getCmd().getType().toString());
        switch (msg.getCmd()) {
            case SetCommand ignored -> {
                msg.getPeer().send(Serializer.encodeToken(Token.RESPONSE_OK));
            }
            case GetCommand gc -> {
                log.info(() -> "command: get key: %s value: %s".formatted(gc.getKey().toString(), gc.getValue().toString()));
                msg.getPeer().send(Serializer.encodeToken(new ArrayToken(List.of(gc.getKey(), gc.getValue()))));
            }
            default ->
                throw new IllegalStateException("Unexpected value: " + msg.getCmd());
        }
    }
}

