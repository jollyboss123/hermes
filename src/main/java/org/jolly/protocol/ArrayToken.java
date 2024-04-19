package org.jolly.protocol;

import java.util.Collection;

/**
 * @author jolly
 */
public final class ArrayToken extends AbstractToken<Collection<Token>> {
    ArrayToken(Collection<Token> value) {
        super(TokenType.ARRAY, value);
    }

    public int size() {
        return getValue().size();
    }
}
