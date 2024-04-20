package org.jolly.command;

import org.jolly.protocol.Token;

import java.util.List;

public interface Command {
    int size();
    CommandType getType();
    void execute(List<Token> tokens) throws IllegalArgumentException;
}
