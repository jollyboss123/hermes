package org.jolly.protocol;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author jolly
 */
class ParserTest {

    @Test
    void parseArray() {
        String raw = "*2\r\n$5\r\nhello\r\n$5\r\nworld\r\n";
        Decoder decoder = Decoder.create(raw.getBytes());
        List<Token> actual = new ArrayList<>();
        decoder.decode(actual);

        assertFalse(actual.isEmpty());
        List<Token> expected = new ArrayList<>();
        Token token = new BulkStringToken("hello");
        Token token2 = new BulkStringToken("world");
        expected.add(new ArrayToken(List.of(token, token2)));
        assertEquals(expected, actual);
    }

    @Test
    void parseEmptyArray() {
        String raw = "*0\r\n";
        Decoder decoder = Decoder.create(raw.getBytes());
        List<Token> actual = new ArrayList<>();
        decoder.decode(actual);

        assertFalse(actual.isEmpty());
        List<Token> expected = new ArrayList<>();
        expected.add(new ArrayToken(Collections.emptyList()));
        assertEquals(expected, actual);
    }

    @Test
    void parseInteger() {
        String raw = "*3\r\n:1\r\n:2\r\n:3\r\n";
        Decoder decoder = Decoder.create(raw.getBytes());
        List<Token> actual = new ArrayList<>();
        decoder.decode(actual);

        assertFalse(actual.isEmpty());
        List<Token> expected = new ArrayList<>();
        Token token = new IntegerToken(1);
        Token token2 = new IntegerToken(2);
        Token token3 = new IntegerToken(3);
        expected.add(new ArrayToken(List.of(token, token2, token3)));
        assertEquals(expected, actual);
    }

    @Test
    void parseMixed() {
        String raw = "*6\r\n:1\r\n:2\r\n:3\r\n$5\r\nhello\r\n+Hello\r\n-World\r\n";
        Decoder decoder = Decoder.create(raw.getBytes());
        List<Token> actual = new ArrayList<>();
        decoder.decode(actual);

        assertFalse(actual.isEmpty());
        List<Token> expected = new ArrayList<>();
        Token token = new IntegerToken(1);
        Token token2 = new IntegerToken(2);
        Token token3 = new IntegerToken(3);
        Token token4 = new BulkStringToken("hello");
        Token token5 = new StringToken("Hello");
        Token token6 = new ErrorToken("World");
        expected.add(new ArrayToken(List.of(token, token2, token3, token4, token5, token6)));
        assertEquals(expected, actual);
    }

    @Test
    void parseNestedArray() {
        String raw = "*2\r\n*3\r\n:1\r\n:2\r\n:3\r\n*2\r\n+Hello\r\n-World\r\n";
        Decoder decoder = Decoder.create(raw.getBytes());
        List<Token> actual = new ArrayList<>();
        decoder.decode(actual);

        assertFalse(actual.isEmpty());
        List<Token> expected = new ArrayList<>();
        Token token = new IntegerToken(1);
        Token token2 = new IntegerToken(2);
        Token token3 = new IntegerToken(3);
        Token token4 = new StringToken("Hello");
        Token token5 = new ErrorToken("World");
        Token array = new ArrayToken(List.of(token, token2, token3));
        Token array2 = new ArrayToken(List.of(token4, token5));
        expected.add(new ArrayToken(List.of(array, array2)));
        assertEquals(expected, actual);
    }
}
