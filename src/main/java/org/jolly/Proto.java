package org.jolly;

import org.jolly.command.Command;
import org.jolly.command.SetCommand;
import org.jolly.protocol.ArrayToken;
import org.jolly.protocol.Decoder;
import org.jolly.protocol.Token;
import org.jolly.protocol.TokenType;

import java.util.List;

public class Proto {
    private Proto() {}

    public static Command parseCommand(KV kv, byte[] buf) {
        Decoder decoder = Decoder.create(buf);
        Token decoded = decoder.decode();

        if (decoded.getType().equals(TokenType.ARRAY)) {
            List<Token> tokens = (List<Token>) ((ArrayToken) decoded).getValue();
            switch (tokens.getFirst().toString()) {
                case "SET" -> {
                    Command cmd = new SetCommand();
                    cmd.execute(kv, tokens);
                    return cmd;
                }
                default -> throw new UnsupportedOperationException(tokens.getFirst().toString());
            }
        }

        return null;
    }
}
