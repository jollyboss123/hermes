package org.jolly.command;

import org.jolly.KV;
import org.jolly.protocol.Token;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

public final class GetCommand implements Command {
    private static final Logger log = Logger.getLogger(GetCommand.class.getName());
    private Token key;
    private Token value;

    @Override
    public int size() {
        return 2;
    }

    @Override
    public CommandType getType() {
        return CommandType.GET;
    }

    @Override
    public void execute(KV kv, List<Token> tokens) throws IllegalArgumentException {
        if (tokens.size() != this.size()) {
            throw new IllegalArgumentException("wrong number of parameters, expecting: " + this.size() + ", got: " + tokens.size());
        }
        this.key = tokens.get(1);
        this.value = kv.get(key);
        if (value == null) {
            throw new NoSuchElementException("key not found: %s".formatted(key.toString()));
        }
    }

    public Token getKey() {
        return key;
    }

    public Token getValue() {
        return value;
    }
}
