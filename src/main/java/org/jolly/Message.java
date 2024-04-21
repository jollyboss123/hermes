package org.jolly;

public class Message {
    private final byte[] data;
    private final Peer peer;

    public Message(byte[] data, Peer peer) {
        this.data = data;
        this.peer = peer;
    }

    public byte[] getData() {
        return data;
    }

    public Peer getPeer() {
        return peer;
    }
}
