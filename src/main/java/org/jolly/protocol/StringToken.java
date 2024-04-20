package org.jolly.protocol;

public final class StringToken extends AbstractToken<String> {
    StringToken(String value) {
        super(TokenType.STRING, value);
    }
}
