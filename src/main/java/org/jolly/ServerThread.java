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

public class ServerThread implements Runnable {
    private static final Logger log = Logger.getLogger(ServerThread.class.getName());
    private final KV kv;
    private final Socket socket;
    private final Map<Peer, Boolean> peers;

    private ServerThread(Socket socket, KV kv, Map<Peer, Boolean> peers) {
        this.socket = socket;
        this.kv = kv;
        this.peers = peers;
    }

    public static ServerThread create(Socket socket, KV kv, Map<Peer, Boolean> peers) {
        return new ServerThread(socket, kv, peers);
    }

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

    private void handleConn(OutputStream out, byte[] buf, int len) throws IOException {
        Peer peer = Peer.create(kv, out, buf, len);
        peers.put(peer, true);
        log.info(() -> "peer connected: %s".formatted(peer));

        handleMessage(peer.receive());
        peers.remove(peer);
    }

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
