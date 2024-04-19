package org.jolly.protocol;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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
        List<Token> tokens = new ArrayList<>();
        decoder.decode(tokens);

        assertFalse(tokens.isEmpty());
        List<Token> expected = new ArrayList<>();
        Token token = new BulkStringToken("hello");
        Token token2 = new BulkStringToken("world");
        expected.add(new ArrayToken(List.of(token, token2)));
        assertEquals(expected, tokens);
    }

}
