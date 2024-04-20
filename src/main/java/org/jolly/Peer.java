package org.jolly;

import com.softwaremill.jox.Channel;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

public class Peer {
    private static final Logger log = Logger.getLogger(Peer.class.getName());
    private final Socket conn;
    private final Channel<byte[]> msgCh;

    private Peer(Socket conn, Channel<byte[]> msgCh) {
        this.conn = conn;
        this.msgCh = msgCh;
    }

    public static Peer create(Socket conn, Channel<byte[]> msgCh) {
        return new Peer(conn, msgCh);
    }

    public void readLoop() throws IOException {
        byte[] buf = new byte[1024];
        try (InputStream in = new BufferedInputStream(conn.getInputStream())) {
            int n = in.read(buf);
            byte[] msgBuf = new byte[n];
            System.arraycopy(buf, 0, msgBuf, 0, n);
//            log.info(() -> new String(buf, 0, n));
//            log.info(() -> String.valueOf(new String(buf, 0, n).length()));
            msgCh.send(msgBuf);
        } catch (InterruptedException e) {
            log.warning(() -> "message channel interrupted");
            Thread.currentThread().interrupt();
        }
    }
}
