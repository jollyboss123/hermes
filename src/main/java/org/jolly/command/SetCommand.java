package org.jolly.command;

import org.jolly.KV;
import org.jolly.protocol.Token;

import java.util.List;
import java.util.logging.Logger;

public final class SetCommand implements Command {
    private static final Logger log = Logger.getLogger(SetCommand.class.getName());
    private byte[] key;
    private byte[] value;

    @Override
    public int size() {
        return 3;
    }

    @Override
    public CommandType getType() {
        return CommandType.SET;
    }

    @Override
    public void execute(KV kv, List<Token> tokens) {
        if (tokens.size() != this.size()) {
            throw new IllegalArgumentException("wrong number of parameters, expecting: " + this.size() + ", got: " + tokens.size());
        }
        this.key = tokens.get(1).getBytes();
        this.value = tokens.get(2).getBytes();
        log.info(() -> "command: %s key: %s value %s".formatted(this.getType(), this.key, this.value));
        kv.set(key, value);
    }

    public byte[] getKey() {
        return key;
    }

    public byte[] getValue() {
        return value;
    }
}
