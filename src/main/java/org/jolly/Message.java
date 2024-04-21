package org.jolly;

import org.jolly.command.Command;

public class Message {
    private final Command cmd;
    private final Peer peer;

    public Message(Command cmd, Peer peer) {
        this.cmd = cmd;
        this.peer = peer;
    }

    public Command getCmd() {
        return cmd;
    }

    public Peer getPeer() {
        return peer;
    }
}
