package org.jolly;

import org.jolly.command.Command;

/**
 * Message class represents a processed message containing a {@link Command} and a {@link Peer}.
 * This is an immutable class.
 */
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
