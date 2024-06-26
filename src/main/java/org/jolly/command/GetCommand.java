package org.jolly.command;

import org.jolly.KV;
import org.jolly.protocol.Token;

import java.util.List;
import java.util.logging.Logger;

/**
 * GetCommand is a concrete implementation of the Command interface that handles
 * retrieval of values from a key-value store based on a provided key.
 */
public final class GetCommand implements Command {
    private static final Logger log = Logger.getLogger(GetCommand.class.getName());
    private Token key;
    private Token value;

    @Override
    public int size() {
        return 2; // "GET" and one key
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
            log.warning(() -> "key nonexistent : " + key.toString());
            value = Token.nulls();
        }
    }

    public Token getKey() {
        return key;
    }

    public Token getValue() {
        return value;
    }
}
