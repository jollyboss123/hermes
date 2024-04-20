package org.jolly;

import java.util.logging.Logger;

public class JRedis {

    private static final Logger log = Logger.getLogger(JRedis.class.getName());

    public static void main(String[] args) {
        Config cfg = new Config(null);
        Server server = Server.create(cfg);
        server.start();
    }
}
