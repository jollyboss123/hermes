package org.jolly.command;

import org.jolly.protocol.Token;

import java.util.List;
import java.util.logging.Logger;

public final class SetCommand implements Command {
    private static final Logger log = Logger.getLogger(SetCommand.class.getName());
    private String key;
    private String value;

    @Override
    public int size() {
        return 3;
    }

    @Override
    public CommandType getType() {
        return CommandType.SET;
    }

    @Override
    public void execute(List<Token> tokens) {
        if (tokens.size() != this.size()) {
            throw new IllegalArgumentException("wrong number of parameters, expecting: " + this.size() + ", got: " + tokens.size());
        }
        this.key = tokens.get(1).toString();
        this.value = tokens.get(2).toString();
        log.info(() -> "command: %s key: %s value %s".formatted(this.getType(), this.key, this.value));
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
