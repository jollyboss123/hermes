package org.jolly;

import org.jolly.command.SetCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProtoTest {

    @Test
    void parseCommand() {
        String raw = "*3\r\n$3\r\nSET\r\n$5\r\nhello\r\n$5\r\nworld\r\n";
        SetCommand actual = (SetCommand) Proto.parseCommand(raw.getBytes());

        String key = "hello";
        String val = "world";
        assertNotNull(actual);
        assertEquals(key, actual.getKey());
        assertEquals(val, actual.getValue());
    }

    @Test
    void parseCommandWithException() {
        byte[] raw = "*2\r\n$3\r\nSET\r\n$5\r\nhello\r\n".getBytes();
        assertThrows(IllegalArgumentException.class, () -> Proto.parseCommand(raw));
    }
}
