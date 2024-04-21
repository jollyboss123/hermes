package org.jolly;

import java.util.logging.Logger;

public class Peer {
    private static final Logger log = Logger.getLogger(Peer.class.getName());
    private final byte[] buf;
    private final int len;

    private Peer(byte[] buf, int len) {
        this.buf = buf;
        this.len = len;
    }

    public static Peer create(byte[] buf, int len) {
        return new Peer(buf, len);
    }

    public byte[] receive() {
        byte[] msgBuf = new byte[len];
        System.arraycopy(buf, 0, msgBuf, 0, len);
        return msgBuf;
    }
}
