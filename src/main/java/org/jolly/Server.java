package org.jolly;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class Server {
    private static final Logger log = Logger.getLogger(Server.class.getName());
    private final Config cfg;
    private final Map<Peer, Boolean> peers;
    private final KV kv;
    private static final int DEFAULT_PORT = 5001;
    private final AtomicBoolean running = new AtomicBoolean(true);

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
            ExecutorService executor = Executors.newCachedThreadPool()) {

            while (running.get()) {
                log.info(() -> "waiting for new client connection");
                Socket socket = serverSocket.accept();
                log.info(() -> "client connected");
                executor.submit(ServerThread.create(socket, kv, peers));
            }

        } catch (IOException e) {
            log.severe(() -> "failed to start server ip: %s".formatted(e.getMessage()));
            System.exit(1);
        }
    }

    public void stop() {
        running.set(false);
    }

    // just for testing purposes to check if peer is removed correctly
    Map<Peer, Boolean> getPeers() {
        return peers;
    }
}

