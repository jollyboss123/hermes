package org.jolly.protocol;

import org.jolly.ByteArrayBuilder;

import java.util.Collection;

/**
 * Serializer class provides static methods to encode different types of Token
 * into a byte array format according to a specified protocol. Each token type
 * is prefixed with a specific byte that indicates the type of the token, followed
 * by the token's data and a delimiter.
 */
public class Serializer {
    private static final byte ARRAY = '*';
    private static final byte ERROR = '-';
    private static final byte INTEGER = ':';
    private static final byte STRING = '+';
    private static final byte BULK_STRING = '$';
    private static final byte NULL = '_';
    private static final byte BOOLEAN = '#';
    private static final byte[] DELIMITER = new byte[]{'\r', '\n'};

    private Serializer() {
        // Private constructor to prevent instantiation
    }

    /**
     * Encodes a token into a byte array.
     * @param msg The token to encode.
     * @return A byte array representing the encoded token.
     */
    public static byte[] encodeToken(Token msg) {
        ByteArrayBuilder builder = new ByteArrayBuilder();
        encodeToken(builder, msg);
        // trim the result byte[]
        byte[] buf = new byte[builder.size()];
        System.arraycopy(builder.getBackingArray(), 0, buf, 0, builder.size()); // trim
        return buf;
    }

    /**
     * Recursively encodes a token and appends the encoded data to a ByteArrayBuilder.
     * This method handles different types of tokens and formats them according to their specific encoding rules.
     * @param builder The ByteArrayBuilder to append encoded bytes to.
     * @param msg The token to encode.
     */
    private static void encodeToken(ByteArrayBuilder builder, Token msg) {
        switch (msg) {
            case ArrayToken at -> {
                Collection<Token> array = at.getValue();
                if (array != null) {
                    builder.append(ARRAY).append(array.size()).append(DELIMITER);
                    for (Token token : array) {
                        encodeToken(builder, token);
                    }
                } else {
                    builder.append(ARRAY).append(0).append(DELIMITER);
                }
            }
            case StringToken st -> {
                String str = st.getValue();
                builder.append(STRING).append(str).append(DELIMITER);
            }
            case BulkStringToken bst -> {
                String str = bst.getValue();
                if (str != null) {
                    builder.append(BULK_STRING).append(str.length()).append(DELIMITER).append(str).append(DELIMITER);
                } else {
                    builder.append(BULK_STRING).append(-1);
                }
            }
            case IntegerToken it -> {
                Integer i = it.getValue();
                builder.append(INTEGER).append(i).append(DELIMITER);
            }
            case ErrorToken et -> {
                String err = et.getValue();
                builder.append(ERROR).append(err).append(DELIMITER);
            }
            case NullToken ignored -> {
                builder.append(NULL).append(DELIMITER);
            }
            case BooleanToken bt -> {
                Boolean bool = bt.getValue();
                if (Boolean.TRUE.equals(bool)) {
                    builder.append(BOOLEAN).append('t').append(DELIMITER);
                } else {
                    builder.append(BOOLEAN).append('f').append(DELIMITER);
                }
            }
            case UnknownToken ignored -> {
                throw new IllegalArgumentException(msg.toString());
            }
            case null, default -> {
                throw new IllegalArgumentException("cannot serialize null token");
            }
        }
    }
}
