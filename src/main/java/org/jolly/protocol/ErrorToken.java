package org.jolly.protocol;

public final class ErrorToken extends AbstractToken<String> {
    ErrorToken(String value) {
        super(TokenType.ERROR, value);
    }
}
