package org.jolly;

import org.jolly.command.GetCommand;
import org.jolly.command.SetCommand;
import org.jolly.protocol.ArrayToken;
import org.jolly.protocol.Serializer;
import org.jolly.protocol.Token;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A runnable server thread to handle individual client connections in a separate thread.
 * It manages the lifecycle of a client connection, processing commands and responses.
 */
public class ServerThread implements Runnable {
    private static final Logger log = Logger.getLogger(ServerThread.class.getName());
    private final KV kv;
    private final Socket socket;
    private final Map<Peer, Boolean> peers;

    /**
     * Private constructor for creating an instance of ServerThread.
     *
     * @param socket The client socket connected to the server.
     * @param kv The key-value store to which this server has access.
     * @param peers A map tracking all active peers connected to the server.
     */
    private ServerThread(Socket socket, KV kv, Map<Peer, Boolean> peers) {
        this.socket = socket;
        this.kv = kv;
        this.peers = peers;
    }

    /**
     * Static factory method to create instances of ServerThread.
     *
     * @param socket The client socket.
     * @param kv The key-value store.
     * @param peers The map tracking all peers.
     * @return A new instance of ServerThread.
     */
    public static ServerThread create(Socket socket, KV kv, Map<Peer, Boolean> peers) {
        return new ServerThread(socket, kv, peers);
    }

    /**
     * Manages reading data from the socket, processing it,
     * and handling client requests and responses.
     */
    @Override
    public void run() {
        try (Socket clientSocket = this.socket;
             InputStream in = new BufferedInputStream(clientSocket.getInputStream());
             OutputStream out = new BufferedOutputStream(clientSocket.getOutputStream());) {

            byte[] buf = new byte[1024];
            int n;
            while ((n = in.read(buf)) != -1) {
                handleConn(out, buf, n);
            }

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        log.info("client disconnected");
    }

    /**
     * Handles the connection for received data. Creates a peer per connection and removes once
     * complete.
     *
     * @param out The OutputStream to send responses to the client.
     * @param buf The buffer containing data from the client.
     * @param len The length of valid data in the buffer.
     */
    private void handleConn(OutputStream out, byte[] buf, int len) throws IOException {
        Peer peer = Peer.create(kv, out, buf, len);
        peers.put(peer, true);
        log.info(() -> "peer connected: %s".formatted(peer));

        handleMessage(peer.receive());
        peers.remove(peer);
    }

    /**
     * Handles messages based on the type of command received.
     *
     * @param msg The message received from a peer.
     */
    private void handleMessage(Message msg) throws IOException {
        log.info(() -> "handling and serializing: " + msg.getCmd().getType().toString());
        switch (msg.getCmd()) {
            case SetCommand ignored -> {
                msg.getPeer().send(Serializer.encodeToken(Token.RESPONSE_OK));
            }
            case GetCommand gc -> {
                log.info(() -> "command: get key: %s value: %s".formatted(gc.getKey().toString(), gc.getValue().toString()));
                msg.getPeer().send(Serializer.encodeToken(gc.getValue()));
            }
            default ->
                    throw new IllegalStateException("Unexpected value: " + msg.getCmd());
        }
    }
}
