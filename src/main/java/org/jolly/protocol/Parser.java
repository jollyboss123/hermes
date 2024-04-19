package org.jolly.protocol;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author jolly
 */
public class Parser implements Iterator<Token> {
    private static final byte PREFIX_BULK_STRING = '$';
    private static final byte PREFIX_INTEGER = ':';
    private static final byte PREFIX_ERROR = '-';
    private static final byte PREFIX_STRING = '+';
    private static final byte PREFIX_ARRAY = '*';

    private final int maxLength;
    private final Decoder decoder;
    private byte[] nextTokenBuf;

    private Parser(int maxLength, Decoder decoder) {
        this.maxLength = maxLength;
        this.decoder = decoder;
    }

    public static Parser create(int maxLength, Decoder decoder) {
        return new Parser(maxLength, decoder);
    }

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

    @Override
    public Token next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        byte[] buf = nextTokenBuf;
        nextTokenBuf = null;
        return parseToken(buf);
    }

    private Token parseToken(byte[] buf) {
        byte prefix = buf[0];
        switch (prefix) {
            case PREFIX_BULK_STRING -> {
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
            token = new BulkStringToken(new String(new byte[0]));
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

    private static int size(byte[] buf) {
        return Integer.parseInt(new String(buf, 1, buf.length - 1, StandardCharsets.UTF_8));
    }
}
