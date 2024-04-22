package org.jolly.protocol;

import java.util.Arrays;
import java.util.List;

/**
 * The Decoder class is designed to decode byte arrays into tokens by reading lines
 * and interpreting each line as a potential token using a {@link Parser}. It manages its
 * internal index to track the current position within the byte array for continuous
 * reading and decoding.
 */
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

    /**
     * Static factory method to create a new Decoder instance.
     * @param buf The byte array that will be decoded by this decoder.
     * @return A new instance of Decoder.
     */
    public static Decoder create(byte[] buf) {
        return new Decoder(buf);
    }

    /**
     * Decodes tokens and adds them to a provided list. This method uses a Parser
     * to interpret the first token found in the buffer.
     * @param out The list where the decoded token will be added.
     */
    public void decode(List<Token> out) {
        Parser parser = Parser.create(MAX_FRAME_SIZE, this);
        Token token = parser.next();
        out.add(token);
    }

    /**
     * Decodes and returns the next token found in the buffer. This method is similar to
     * {@code decode(List<Token>)} but returns a single token instead.
     * @return The next token decoded from the buffer.
     */
    public Token decode() {
        Parser parser = Parser.create(MAX_FRAME_SIZE, this);
        return parser.next();
    }

    /**
     * Reads a line from the buffer starting from the current reading index until it
     * finds a CRLF sequence, which denotes the end of a line.
     * @return The byte array representing the line, or an empty byte array if end of buffer
     *         is reached without finding a newline.
     */
    public byte[] readLine() {
        return readLine(buf);
    }

    /**
     * Helper method to extract a line of bytes from a specified input byte array starting from
     * a given index.
     * @param in The input byte array from which to read the line.
     * @return The byte array for the line, not including the CR+LF characters.
     */
    private byte[] readLine(byte[] in) {
        int eol = findEndOfLine(in, readingIdx);
        if (eol == -1) {
            return new byte[0];
        }
        byte[] line = Arrays.copyOfRange(buf, readingIdx, eol);
        readingIdx = eol + 2;
        return line;
    }

    /**
     * Locates the first end of a line (CRLF) in the byte array starting from a specific index.
     * @param in The byte array to search.
     * @param from The starting index from which to search for the end of line.
     * @return The index of the CR in the CR+LF sequence, or -1 if not found.
     */
    private static int findEndOfLine(byte[] in, int from) {
        for (int i = from; i < in.length; i++) {
            if (in[i] == CR && in[i + 1] == LF) {
                return i;
            }
        }
        return -1;
    }
}
