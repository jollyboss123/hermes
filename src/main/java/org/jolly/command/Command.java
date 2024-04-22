package org.jolly.command;

import org.jolly.KV;
import org.jolly.protocol.Token;

import java.util.List;

/**
 * Command is an interface defining the contract for classes that will execute
 * operations on a key-value store.
 */
public interface Command {
    /**
     * Returns the number of tokens expected by the command.
     * @return the number of expected tokens
     */
    int size();

    /**
     * Returns the type of the command.
     * @return the command type
     */
    CommandType getType();

    /**
     * Executes the command using the provided key-value store and list of tokens.
     * @param kv The key-value store to operate on.
     * @param tokens The tokens parsed from the command input.
     * @throws IllegalArgumentException if the number of tokens does not match what is expected.
     */
    void execute(KV kv, List<Token> tokens) throws IllegalArgumentException;
}
