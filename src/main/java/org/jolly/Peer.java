package org.jolly;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * @author jolly
 */
public class Peer {
    private static final Logger log = Logger.getLogger(Peer.class.getName());
    private final Socket conn;

    private Peer(Socket conn) {
        this.conn = conn;
    }

    public static Peer create(Socket conn) {
        return new Peer(conn);
    }

    public void readLoop() throws IOException {
        byte[] buf = new byte[1024];
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                log.info(inputLine);
            }
        } catch (IOException e) {
            log.severe(() -> "input buffer error");
            throw e;
        }
    }
}
