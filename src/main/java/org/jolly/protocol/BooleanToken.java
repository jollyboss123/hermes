package org.jolly.protocol;

public final class BooleanToken extends AbstractToken<Boolean> {
    BooleanToken(Boolean value) {
        super(TokenType.BOOLEAN, value);
    }
}
