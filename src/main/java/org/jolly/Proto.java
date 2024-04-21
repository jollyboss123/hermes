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

public class Proto {
    private static final Logger log = Logger.getLogger(Proto.class.getName());
    private Proto() {}

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
