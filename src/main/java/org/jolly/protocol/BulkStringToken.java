package org.jolly.protocol;

public final class BulkStringToken extends AbstractToken<String> {
    BulkStringToken(String value) {
        super(TokenType.BULK_STRING, value);
    }
}
