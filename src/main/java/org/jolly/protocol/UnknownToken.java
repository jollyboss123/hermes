package org.jolly.protocol;

public final class UnknownToken extends AbstractToken<String> {
    UnknownToken(String value) {
        super(TokenType.UNKNOWN, value);
    }
}
