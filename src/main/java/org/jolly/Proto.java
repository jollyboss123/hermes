package org.jolly;

import org.jolly.command.Command;
import org.jolly.command.GetCommand;
import org.jolly.command.SetCommand;
import org.jolly.protocol.ArrayToken;
import org.jolly.protocol.Decoder;
import org.jolly.protocol.Token;
import org.jolly.protocol.TokenType;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Proto class represents the communication protocol and contains methods
 * for parsing commands from byte arrays.
 */
public class Proto {
    private static final Logger log = Logger.getLogger(Proto.class.getName());
    private Proto() {}

    /**
     * Parses a command from a byte array using the provided key-value store.
     * @param kv The key-value store to operate on.
     * @param buf The byte array containing the command.
     * @return An optional Command object parsed from the byte array.
     */
    public static Optional<Command> parseCommand(KV kv, byte[] buf) {
        Decoder decoder = Decoder.create(buf);
        Token decoded = decoder.decode();
        log.info(() -> "decoded: " + decoded.toString());

        if (decoded.getType().equals(TokenType.ARRAY)) {
            List<Token> tokens = (List<Token>) ((ArrayToken) decoded).getValue();
            switch (tokens.getFirst().toString()) {
                case "SET" -> {
                    Command cmd = new SetCommand();
                    cmd.execute(kv, tokens);
                    return Optional.of(cmd);
                }
                case "GET" -> {
                    Command cmd = new GetCommand();
                    cmd.execute(kv, tokens);
                    return Optional.of(cmd);
                }
                default -> throw new UnsupportedOperationException(tokens.getFirst().toString());
            }
        }

        return Optional.empty();
    }
}
