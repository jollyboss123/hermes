package org.jolly;

import java.util.logging.Logger;

public class Pegasus {

    private static final Logger log = Logger.getLogger(Pegasus.class.getName());

    public static void main(String[] args) {
        Config cfg = new Config(null);
        Server server = Server.create(cfg);
        server.start();
    }
}
