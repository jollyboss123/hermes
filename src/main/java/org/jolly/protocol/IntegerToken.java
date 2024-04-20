package org.jolly.protocol;

public final class IntegerToken extends AbstractToken<Integer> {
    IntegerToken(Integer value) {
        super(TokenType.INTEGER, value);
    }
}
