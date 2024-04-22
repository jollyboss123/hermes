package org.jolly;

import org.jolly.command.Command;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * Peer class represents a connection endpoint in the system, handling incoming
 * messages and sending responses.
 */
public class Peer {
    private static final Logger log = Logger.getLogger(Peer.class.getName());
    private final byte[] buf;
    private final int len;
    private final OutputStream out;
    private final KV kv;

    private Peer(byte[] buf, int len, OutputStream out, KV kv) {
        this.buf = buf;
        this.len = len;
        this.out = out;
        this.kv = kv;
    }

    /**
     * Static factory method to create a new instance of Peer.
     * @param kv The key-value store reference.
     * @param out The output stream for sending responses.
     * @param buf The received message buffer.
     * @param len The length of the received message.
     * @return A new instance of Peer.
     */
    public static Peer create(KV kv, OutputStream out, byte[] buf, int len) {
        return new Peer(buf, len, out, kv);
    }

    /**
     * Receives and processes a message.
     * @return A Message object containing the parsed command and the peer.
     */
    public Message receive() {
        byte[] msgBuf = new byte[len];
        System.arraycopy(buf, 0, msgBuf, 0, len);
        log.info(() -> "processing: " + new String(msgBuf, StandardCharsets.UTF_8));
        Command cmd = Proto.parseCommand(kv, msgBuf)
                .orElseThrow();
        return new Message(cmd, this);
    }

    /**
     * Sends a response message.
     * @param msg The response message as a byte array.
     */
    public void send(byte[] msg) throws IOException {
        out.write(msg);
        out.flush();
    }
}
