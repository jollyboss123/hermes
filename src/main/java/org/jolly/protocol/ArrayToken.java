package org.jolly.protocol;

import java.util.Collection;
import java.util.Iterator;

public final class ArrayToken extends AbstractToken<Collection<Token>> implements Iterable<Token> {
    public ArrayToken(Collection<Token> value) {
        super(TokenType.ARRAY, value);
    }

    public int size() {
        return getValue().size();
    }

    @Override
    public Iterator<Token> iterator() {
        return getValue().iterator();
    }
}
