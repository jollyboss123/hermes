package org.jolly;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author jolly
 */
public class Main {

    private static final Logger log = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        Config cfg = new Config(null);
        Server server = Server.create(cfg);
        server.start();
    }
}
