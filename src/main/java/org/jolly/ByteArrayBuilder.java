package org.jolly;

import java.nio.charset.StandardCharsets;

public class ByteArrayBuilder {
    public static final int INITIAL_CAPACITY = 13;
    private byte[] backingArray;
    private int size;

    public ByteArrayBuilder() {
        backingArray = new byte[INITIAL_CAPACITY];
    }

    public ByteArrayBuilder append(byte b) {
        if (size == backingArray.length) {
            extendCapacity();
        }
        backingArray[size] = b;
        size++;
        return this;
    }

    public ByteArrayBuilder append(int i) {
        return append(String.valueOf(i));
    }

    public ByteArrayBuilder append(String str) {
        return append(str.getBytes(StandardCharsets.UTF_8));
    }

    public ByteArrayBuilder append(byte[] buf) {
        if (buf.length > (backingArray.length - size)) {
            extendCapacity();
        }
        for (byte b : buf) {
            append(b);
        }
        return this;
    }

    public byte[] getBackingArray() {
        return backingArray;
    }

    public int size() {
        return size;
    }

    private void extendCapacity() {
        int cap = backingArray.length * 2;
        byte[] arr = new byte[cap];
        System.arraycopy(backingArray, 0, arr, 0, backingArray.length);
        backingArray = arr;
    }
}
