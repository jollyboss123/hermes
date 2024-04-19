package org.jolly.protocol;

/**
 * @author jolly
 */
public final class UnknownToken extends AbstractToken<String> {
    UnknownToken(String value) {
        super(TokenType.UNKNOWN, value);
    }
}
