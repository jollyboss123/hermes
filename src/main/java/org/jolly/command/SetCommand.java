package org.jolly.command;

import org.jolly.KV;
import org.jolly.protocol.Token;

import java.util.List;
import java.util.logging.Logger;

/**
 * SetCommand is a concrete implementation of the Command interface that handles
 * setting values in a key-value store. It stores a key-value pair from the provided tokens.
 */
public final class SetCommand implements Command {
    private static final Logger log = Logger.getLogger(SetCommand.class.getName());
    private Token key;
    private Token value;

    @Override
    public int size() {
        return 3; // "SET", one key, and one value
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
        this.key = tokens.get(1);
        this.value = tokens.get(2);
        log.info(() -> "command: %s key: %s value %s".formatted(this.getType(), this.key, this.value));
        kv.set(key, value);
    }

    public Token getKey() {
        return key;
    }

    public Token getValue() {
        return value;
    }
}
