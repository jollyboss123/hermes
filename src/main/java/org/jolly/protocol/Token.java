package org.jolly.protocol;

import java.util.Arrays;
import java.util.Collection;

/**
 * Token interface defines the structure for tokens used in the communication protocol based on RESP 3.
 * This interface includes factory methods for creating various types of tokens such as strings,
 * integers, boolean values, etc., which are used to encode or decode messages between the server and client.
 */
public interface Token {
    Token NULL_STRING = bulkString(null);
    Token RESPONSE_OK = string("OK");
    TokenType getType();
    byte[] getBytes();
    static Token nullString() {
        return NULL_STRING;
    }
    static Token responseOk() {
        return RESPONSE_OK;
    }
    static Token string(String str) {
        return new StringToken(str);
    }
    static Token bulkString(String str) {
        return new BulkStringToken(str);
    }
    static Token integer(int i) {
        return new IntegerToken(i);
    }
    static Token bool(boolean b) {
        return new BooleanToken(b);
    }
    static Token bool(String str) {
        return new BooleanToken(str.equals("t"));
    }
    static Token nulls() {
        return new NullToken("");
    }
    static Token err(String str) {
        return new ErrorToken(str);
    }
    static Token array(Token... tokens) {
        return new ArrayToken(Arrays.asList(tokens));
    }
    static Token array(Collection<Token> tokens) {
        return new ArrayToken(tokens);
    }
}
