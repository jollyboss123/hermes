package org.jolly.protocol;

/**
 * @author jolly
 */
public abstract class AbstractToken<T> implements Token {
    private final TokenType type;
    private final T value;

    protected AbstractToken(TokenType type, T value) {
        if (type == null) {
            throw new IllegalArgumentException("non null value: type required");
        }
        this.type = type;
        this.value = value;
    }

    @Override
    public TokenType getType() {
        return type;
    }

    public T getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractToken<?> that)) {
            return false;
        }
        return type.equals(that.getType()) && value.equals(that.getValue());
    }

    @Override
    public String toString() {
        return type + "=>" + value;
    }
}
