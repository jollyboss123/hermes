package org.jolly;

import org.jolly.protocol.Token;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class KV {
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<String, Token> data;

    private KV() {
        this.data = new ConcurrentHashMap<>();
    }

    public static KV create() {
        return new KV();
    }

    public void set(Token key, Token val) {
        lock.writeLock().lock();
        try {
            data.put(new String(key.getBytes(), StandardCharsets.UTF_8), val);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Token get(Token key) {
        lock.readLock().lock();
        try {
            return data.get(new String(key.getBytes(), StandardCharsets.UTF_8));
        } finally {
            lock.readLock().unlock();
        }
    }
}
