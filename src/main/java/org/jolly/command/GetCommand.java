package org.jolly.command;

import org.jolly.KV;
import org.jolly.protocol.Decoder;
import org.jolly.protocol.Token;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public final class GetCommand implements Command {
    private static final Logger log = Logger.getLogger(GetCommand.class.getName());
    private Token key;
    private Optional<Token> value;

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
        this.value = Optional.of(kv.get(key));
    }

    public Token getKey() {
        return key;
    }

    public Optional<Token> getValue() {
        return value;
    }
}
