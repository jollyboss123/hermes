package org.jolly.protocol;

/**
 * @author jolly
 */
public final class ErrorToken extends AbstractToken<String> {
    ErrorToken(String value) {
        super(TokenType.ERROR, value);
    }
}
