package org.jolly;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

public class Peer {
    private static final Logger log = Logger.getLogger(Peer.class.getName());
    private final byte[] buf;
    private final int len;
    private final OutputStream out;

    private Peer(byte[] buf, int len, OutputStream out) {
        this.buf = buf;
        this.len = len;
        this.out = out;
    }

    public static Peer create(OutputStream out, byte[] buf, int len) {
        return new Peer(buf, len, out);
    }

    public byte[] receive() {
        byte[] msgBuf = new byte[len];
        System.arraycopy(buf, 0, msgBuf, 0, len);
        return msgBuf;
    }

    public void send(byte[] msg) throws IOException {
        out.write(msg);
        out.flush();
    }
}
