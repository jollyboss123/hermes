package org.jolly;

import org.jolly.command.SetCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProtoTest {
    private KV kv;

    @BeforeEach
    void setUp() {
        kv = KV.create();
    }

    @Test
    void parseCommand() {
        String raw = "*3\r\n$3\r\nSET\r\n$5\r\nhello\r\n$5\r\nworld\r\n";
        SetCommand actual = (SetCommand) Proto.parseCommand(kv, raw.getBytes()).orElseThrow();

        byte[] key = "hello".getBytes();
        byte[] val = "world".getBytes();
        assertNotNull(actual);
//        assertArrayEquals(key, actual.getKey());
//        assertArrayEquals(val, actual.getValue());
//        assertEquals(new String(val), new String(kv.get(key)));
    }

    @Test
    void parseCommandWithException() {
        byte[] raw = "*2\r\n$3\r\nSET\r\n$5\r\nhello\r\n".getBytes();
        assertThrows(IllegalArgumentException.class, () -> Proto.parseCommand(kv, raw));
    }
}
