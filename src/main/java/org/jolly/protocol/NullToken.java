package org.jolly.protocol;

public final class NullToken extends AbstractToken<Object> {
    NullToken(Object value) {
        super(TokenType.NULL, value);
    }
}
