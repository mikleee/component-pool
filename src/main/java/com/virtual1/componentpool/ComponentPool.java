package com.virtual1.componentpool;

import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by misha on 18.04.17.
 */
public class ComponentPool<K, V> {
    private final static Logger LOGGER = Logger.getLogger(PoolCleaner.class);
    private final ConcurrentHashMap<K, ComponentWrapper<V>> registry = new ConcurrentHashMap<>();

    private final String name;
    private final long timeToLive;
    private final PoolCleaner cleaner;

    private AtomicBoolean isLocked = new AtomicBoolean(false);

    ComponentPool(String name, Long timeToLive) {
        this.name = name;
        this.timeToLive = timeToLive == null ? PoolConfig.getTimeToLive() : timeToLive;
        this.cleaner = new PoolCleaner(this);
    }

    public synchronized V get(K key) {
        try {
            waitForUnlock();
            lock();
            ComponentWrapper<V> wrapper = registry.get(key);
            if (wrapper != null) {
                wrapper.updateLastAccess();
                return wrapper.getData();
            } else {
                return null;
            }
        } finally {
            unlock();
        }
    }

    public synchronized V putIfAbsent(K key, V value) {
        try {
            waitForUnlock();
            lock();
            ComponentWrapper<V> wrapper = registry.get(key);
            if (wrapper == null) {
                wrapper = new ComponentWrapper<>(value);
                registry.put(key, wrapper);
            } else {
                wrapper.updateLastAccess();
            }
            return wrapper.getData();
        } finally {
            unlock();
        }
    }

    public synchronized void remove(K key) {
        try {
            waitForUnlock();
            lock();
            registry.remove(key);
        } finally {
            unlock();
        }
    }

    public String getName() {
        return name;
    }

    public void destroy() {
        try {
            waitForUnlock();
            lock();
            cleaner.interrupt();
            registry.clear();
        } finally {
            unlock();
        }
    }

    synchronized void cleanExpiredElements() {
        try {
            waitForUnlock();
            lock();
            Set<K> toRemove = new HashSet<>();
            long validAfter = System.currentTimeMillis() - timeToLive;
            for (Map.Entry<K, ComponentWrapper<V>> entry : registry.entrySet()) {
                long exceedTime = validAfter - entry.getValue().getLastAccess();
                if (exceedTime >= 0) {
                    K key = entry.getKey();
                    LOGGER.trace("Component under key " + key + " in the " + name + " pool has been expired, exceed time: " + exceedTime + " ms");
                    toRemove.add(key);
                }
            }

            for (K k : toRemove) {
                registry.remove(k);
            }
        } finally {
            unlock();
        }
    }

    private void lock() {
        isLocked.set(true);
    }

    private void unlock() {
        isLocked.set(false);
    }

    private void waitForUnlock() {
        while (isLocked.get()) {
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ComponentPool<?, ?> that = (ComponentPool<?, ?>) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
