package org.jolly;

import org.jolly.protocol.Token;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * KV class represents a key-value store with thread-safe read-write operations.
 * It uses a ConcurrentHashMap for efficient concurrent access.
 */
public class KV {
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<String, Token> data;

    private KV() {
        this.data = new ConcurrentHashMap<>();
    }

    /**
     * Static factory method to create a new instance of KV.
     * @return A new instance of KV.
     */
    public static KV create() {
        return new KV();
    }

    /**
     * Sets a key-value pair in the store.
     * @param key The key token.
     * @param val The value token.
     */
    public void set(Token key, Token val) {
        lock.writeLock().lock();
        try {
            data.put(new String(key.getBytes(), StandardCharsets.UTF_8), val);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Retrieves a value from the store based on the key.
     * @param key The key token.
     * @return The corresponding value token, or null if not found.
     */
    public Token get(Token key) {
        lock.readLock().lock();
        try {
            return data.get(new String(key.getBytes(), StandardCharsets.UTF_8));
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Returns the number of key-value pairs in the store.
     * @return The size of the store.
     */
    public int size() {
        return data.size();
    }
}
