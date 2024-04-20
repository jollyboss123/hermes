package org.jolly;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class KV {
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<String, byte[]> data;

    private KV() {
        this.data = new HashMap<>();
    }

    public static KV create() {
        return new KV();
    }

    public void set(byte[] key, byte[] val) {
        lock.writeLock().lock();
        try {
            data.put(new String(key), val);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public byte[] get(byte[] key) {
        lock.readLock().lock();
        try {
            return data.get(new String(key));
        } finally {
            lock.readLock().unlock();
        }
    }
}
