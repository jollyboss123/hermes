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
                final int finalN = n;
                byte[] copy = Arrays.copyOf(buf, n);
                executor.submit(() -> {
                    try {
                        handleConn(out, copy, finalN);
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                });
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
        Peer peer = Peer.create(buf, len);
        peers.put(peer, true);
        log.info(() -> "peer connected: %s".formatted(peer));

        byte[] rawMsg = peer.receive();

        handleRawMessage(out, rawMsg);
    }

    private void handleRawMessage(OutputStream out, byte[] rawMsg) throws IOException {
        log.info(() -> new String(rawMsg, StandardCharsets.UTF_8));
        Command cmd = Proto.parseCommand(kv, rawMsg)
                .orElseThrow();
        switch (cmd) {
            case SetCommand ignored -> {
                out.write(Serializer.encodeToken(Token.RESPONSE_OK));
                out.flush();
            }
            case GetCommand gc -> {
                Token val = gc.getValue()
                        .orElseThrow(() -> new RuntimeException("key not found"));
                log.info(() -> "command: get key: %s value: %s".formatted(gc.getKey().toString(), val.toString()));
                out.write(Serializer.encodeToken(new ArrayToken(List.of(gc.getKey(), val))));
                out.flush();
            }
            default ->
                throw new IllegalStateException("Unexpected value: " + cmd);
        }
    }
}

