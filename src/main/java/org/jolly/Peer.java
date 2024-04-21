package org.jolly;

import org.jolly.command.Command;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

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

    public static Peer create(KV kv, OutputStream out, byte[] buf, int len) {
        return new Peer(buf, len, out, kv);
    }

    public Message receive() {
        byte[] msgBuf = new byte[len];
        System.arraycopy(buf, 0, msgBuf, 0, len);
        log.info(() -> "processing: " + new String(msgBuf, StandardCharsets.UTF_8));
        Command cmd = Proto.parseCommand(kv, msgBuf)
                .orElseThrow();
        return new Message(cmd, this);
    }

    public void send(byte[] msg) throws IOException {
        out.write(msg);
        out.flush();
    }
}
