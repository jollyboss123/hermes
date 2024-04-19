package org.jolly.protocol;

/**
 * @author jolly
 */
public final class StringToken extends AbstractToken<String> {
    StringToken(String value) {
        super(TokenType.STRING, value);
    }
}
