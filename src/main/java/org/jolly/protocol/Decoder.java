package org.jolly.protocol;

import java.util.Arrays;
import java.util.List;

public class Decoder {
    private static final byte CR = 13;
    private static final byte LF = 10;
    private static final int MAX_FRAME_SIZE = 1024 * 1024 * 100;
    private final byte[] buf;
    private int readingIdx;

    private Decoder(byte[] buf) {
        this.buf = buf;
        this.readingIdx = 0;
    }

    public static Decoder create(byte[] buf) {
        return new Decoder(buf);
    }

    public void decode(List<Token> out) {
        Parser parser = Parser.create(MAX_FRAME_SIZE, this);
        Token token = parser.next();
        out.add(token);
    }

    public Token decode() {
        Parser parser = Parser.create(MAX_FRAME_SIZE, this);
        return parser.next();
    }

    public byte[] readLine() {
        return readLine(buf);
    }

    private byte[] readLine(byte[] in) {
        int eol = findEndOfLine(in, readingIdx);
        if (eol == -1) {
            return new byte[0];
        }
        byte[] line = Arrays.copyOfRange(buf, readingIdx, eol);
        readingIdx = eol + 2;
        return line;
    }

    private static int findEndOfLine(byte[] in, int from) {
        for (int i = from; i < in.length; i++) {
            if (in[i] == CR && in[i + 1] == LF) {
                return i;
            }
        }
        return -1;
    }
}
