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

/**
 * Server class to handle incoming client connections and manage these using separate threads.
 * It uses a TCP socket to accept incoming connections and dispatches each connection to a {@link ServerThread}.
 */
public class Server {
    private static final Logger log = Logger.getLogger(Server.class.getName());
    private final Config cfg;
    private final Map<Peer, Boolean> peers;
    private final KV kv;
    private static final int DEFAULT_PORT = 5001;
    private final AtomicBoolean running = new AtomicBoolean(true);

    Server(Config cfg) {
        this(cfg, new ConcurrentHashMap<>(), KV.create());
    }

    /**
     * Main constructor initializing the server with a configuration, peer list, and key-value store.
     *
     * @param cfg Configuration settings for the server.
     * @param peers Map to track all connected clients.
     * @param kv Key-value store instance.
     */
    Server(Config cfg, Map<Peer, Boolean> peers, KV kv) {
        this.cfg = cfg;
        this.peers = peers;
        this.kv = kv;
    }

    /**
     * Static factory method to create a Server instance with default settings or specific configuration.
     *
     * @param cfg Configuration which may or may not include a specific port.
     * @return a new Server instance based on the provided configuration.
     */
    public static Server create(Config cfg) {
        if (cfg.getPort() == null) {
            cfg = new Config(DEFAULT_PORT);
        }
        return new Server(cfg);
    }

    /**
     * Starts the server to accept incoming connections and dispatch them to threads.
     * Utilizes an {@link ExecutorService} to manage threads efficiently.
     */
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

    /**
     * Stops the server by updating the running flag to false.
     */
    public void stop() {
        running.set(false);
    }

    /**
     * For testing purposes, this method allows access to the peers map to verify correct addition/removal of clients.
     *
     * @return Map of connected peers.
     */
    Map<Peer, Boolean> getPeers() {
        return peers;
    }
}

