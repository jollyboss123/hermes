package org.jolly.protocol;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * The Parser class implements the Iterator interface for tokens, providing a mechanism
 * to sequentially read tokens from a byte stream as defined by a custom protocol.
 * Each token type is prefixed with a specific byte that indicates its type.
 */
public class Parser implements Iterator<Token> {
    private static final byte PREFIX_BULK_STRING = '$';
    private static final byte PREFIX_INTEGER = ':';
    private static final byte PREFIX_ERROR = '-';
    private static final byte PREFIX_STRING = '+';
    private static final byte PREFIX_ARRAY = '*';
    private static final byte PREFIX_NULL = '_';
    private static final byte PREFIX_BOOLEAN = '#';

    // Maximum length for tokens, used as a security or format measure
    private final int maxLength;
    private final Decoder decoder;
    // Buffer to hold the next token data read from the stream.
    private byte[] nextTokenBuf;

    private Parser(int maxLength, Decoder decoder) {
        this.maxLength = maxLength;
        this.decoder = decoder;
    }

    /**
     * Static factory method to create a new Parser instance.
     * @param maxLength The maximum allowable length for any token.
     * @param decoder The decoder to interpret byte data into tokens.
     * @return A new instance of Parser configured with the provided parameters.
     */
    public static Parser create(int maxLength, Decoder decoder) {
        return new Parser(maxLength, decoder);
    }

    /**
     * Checks if there are more tokens to read.
     * @return true if there is at least one more token to read, false otherwise.
     */
    @Override
    public boolean hasNext() {
        if (nextTokenBuf != null) {
            return true;
        }
        byte[] buf = decoder.readLine();
        if (buf.length == 0) {
            return false;
        }
        nextTokenBuf = buf;
        return true;
    }

    /**
     * Reads the next token from the byte stream.
     * @return The next token.
     */
    @Override
    public Token next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        byte[] buf = nextTokenBuf;
        nextTokenBuf = null;
        return parseToken(buf);
    }

    /**
     * Parses a token from a byte buffer based on the prefix indicating the token type.
     * @param buf The byte buffer containing the token data.
     * @return The parsed token.
     */
    private Token parseToken(byte[] buf) {
        byte prefix = buf[0];
        switch (prefix) {
            case PREFIX_NULL -> {
                return Token.nulls();
            }
            case PREFIX_BOOLEAN -> {
                return parseBooleanToken(buf[1]);
            }
            case PREFIX_BULK_STRING -> {
                int size = size(buf);
                if (size == -1) {
                    return Token.nullString();
                }
                return parseBulkStringToken(decoder.readLine(), size(buf));
            }
            case PREFIX_ARRAY -> {
                return parseArrayToken(size(buf));
            }
            case PREFIX_INTEGER -> {
                return parseIntegerToken(buf);
            }
            case PREFIX_ERROR -> {
                return parseErrorToken(buf);
            }
            case PREFIX_STRING -> {
                return parseStringToken(buf);
            }
            default -> {
                return new UnknownToken(new String(buf));
            }
        }
    }

    private Token parseBulkStringToken(byte[] buf, int size) {
        BulkStringToken token;
        if (size > 0 && size < maxLength) {
            token = new BulkStringToken(new String(buf, 0, size, StandardCharsets.UTF_8));
        } else {
            token = new BulkStringToken(new String(new byte[] {}));
        }
        return token;
    }

    private Token parseArrayToken(int size) {
        List<Token> tokens = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            tokens.add(parseToken(decoder.readLine()));
        }

        return Token.array(tokens);
    }

    private Token parseIntegerToken(byte[] buf) {
        IntegerToken token;
        token = new IntegerToken(Integer.parseInt(new String(buf, 1, buf.length - 1, StandardCharsets.UTF_8)));

        return token;
    }

    private Token parseErrorToken(byte[] buf) {
        Token token;
        token = Token.err(new String(buf, 1, buf.length - 1, StandardCharsets.UTF_8));

        return token;
    }

    private Token parseStringToken(byte[] buf) {
        Token token;
        token = Token.string(new String(buf, 1, buf.length - 1, StandardCharsets.UTF_8));

        return token;
    }

    private Token parseBooleanToken(byte b) {
        return Token.bool(b == 't');
    }

    private static int size(byte[] buf) {
        return Integer.parseInt(new String(buf, 1, buf.length - 1, StandardCharsets.UTF_8));
    }
}
