package org.jolly.protocol;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    @Test
    void parseArray() {
        String raw = "*2\r\n$5\r\nhello\r\n$5\r\nworld\r\n";
        Decoder decoder = Decoder.create(raw.getBytes());
        Token actual = decoder.decode();

        Token token = new BulkStringToken("hello");
        Token token2 = new BulkStringToken("world");
        assertEquals(new ArrayToken(List.of(token, token2)), actual);
    }

    @Test
    void parseEmptyArray() {
        String raw = "*0\r\n";
        Decoder decoder = Decoder.create(raw.getBytes());
        Token actual = decoder.decode();

        assertEquals(new ArrayToken(Collections.emptyList()), actual);
    }

    @Test
    void parseInteger() {
        String raw = "*3\r\n:1\r\n:2\r\n:3\r\n";
        Decoder decoder = Decoder.create(raw.getBytes());
        Token actual = decoder.decode();

        Token token = new IntegerToken(1);
        Token token2 = new IntegerToken(2);
        Token token3 = new IntegerToken(3);
        assertEquals(new ArrayToken(List.of(token, token2, token3)), actual);
    }

    @Test
    void parseMixed() {
        String raw = "*6\r\n:1\r\n:2\r\n:3\r\n$5\r\nhello\r\n+Hello\r\n-World\r\n";
        Decoder decoder = Decoder.create(raw.getBytes());
        Token actual = decoder.decode();

        Token token = new IntegerToken(1);
        Token token2 = new IntegerToken(2);
        Token token3 = new IntegerToken(3);
        Token token4 = new BulkStringToken("hello");
        Token token5 = new StringToken("Hello");
        Token token6 = new ErrorToken("World");
        assertEquals(new ArrayToken(List.of(token, token2, token3, token4, token5, token6)), actual);
    }

    @Test
    void parseNestedArray() {
        String raw = "*2\r\n*3\r\n:1\r\n:2\r\n:3\r\n*2\r\n+Hello\r\n-World\r\n";
        Decoder decoder = Decoder.create(raw.getBytes());
        Token actual = decoder.decode();

        Token token = new IntegerToken(1);
        Token token2 = new IntegerToken(2);
        Token token3 = new IntegerToken(3);
        Token token4 = new StringToken("Hello");
        Token token5 = new ErrorToken("World");
        Token array = new ArrayToken(List.of(token, token2, token3));
        Token array2 = new ArrayToken(List.of(token4, token5));
        assertEquals(new ArrayToken(List.of(array, array2)), actual);
    }

    @Test
    void parseNullBulkString() {
        String raw = "*3\r\n$5\r\nhello\r\n$-1\r\n$5\r\nworld\r\n";
        Decoder decoder = Decoder.create(raw.getBytes());
        Token actual = decoder.decode();

        Token token = new BulkStringToken("hello");
        Token token2 = Token.nullString();
        Token token3 = new BulkStringToken("world");
        assertEquals(new ArrayToken(List.of(token, token2, token3)), actual);
    }

    @Test
    void parseNulls() {
        String raw = "*3\r\n$5\r\nhello\r\n_\r\n$5\r\nworld\r\n";
        Decoder decoder = Decoder.create(raw.getBytes());
        Token actual = decoder.decode();

        Token token = new BulkStringToken("hello");
        Token token2 = Token.nulls();
        Token token3 = new BulkStringToken("world");
        assertEquals(new ArrayToken(List.of(token, token2, token3)), actual);
    }

    @Test
    void parseBoolean() {
        String raw = "*3\r\n$5\r\nhello\r\n#f\r\n$5\r\nworld\r\n";
        Decoder decoder = Decoder.create(raw.getBytes());
        Token actual = decoder.decode();

        Token token = new BulkStringToken("hello");
        Token token2 = new BooleanToken(false);
        Token token3 = new BulkStringToken("world");
        assertEquals(new ArrayToken(List.of(token, token2, token3)), actual);
    }
}
