package org.jolly.command;

import org.jolly.KV;
import org.jolly.protocol.Token;

import java.util.List;

public interface Command {
    int size();
    CommandType getType();
    void execute(KV kv, List<Token> tokens) throws IllegalArgumentException;
}
