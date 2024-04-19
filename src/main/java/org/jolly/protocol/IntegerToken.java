package org.jolly.protocol;

/**
 * @author jolly
 */
public final class IntegerToken extends AbstractToken<Integer> {
    IntegerToken(Integer value) {
        super(TokenType.INTEGER, value);
    }
}
