package com.virtual1.componentpool;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Mikhail Tkachenko
 */
public class ComponentPool<K extends Serializable, V extends Serializable> {
    private final static int RUN_STATE_CODE = 1;
    private final static int DESTROYED_STATE_CODE = 2;

    private final static Logger LOGGER = Logger.getLogger(ComponentPool.class);
    private final Map<K, PoolValue<V>> registry = new HashMap<>();

    private final String name;
    private final PoolCleaner cleaner;
    private long timeToLive;
    private int state;

    ComponentPool(String name, ThreadGroup cleanersThreadGroup) {
        this.name = name;
        this.timeToLive = PoolConfig.getTimeToLive();
        this.cleaner = new PoolCleaner(this, cleanersThreadGroup);
        this.state = RUN_STATE_CODE;
    }

    public synchronized V get(K key) {
        checkState();
        PoolValue<V> wrapper = registry.get(key);
        if (wrapper != null) {
            wrapper.updateLastAccess();
            return wrapper.getData();
        } else {
            return null;
        }
    }


    public synchronized V putIfAbsent(K key, V value) {
        checkState();
        PoolValue<V> wrapper = registry.get(key);
        if (wrapper == null || (value != null && !value.equals(wrapper.getData()))) {
            LOGGER.trace(String.format("pool '%s': put object under key '%s'", name, key));
            wrapper = new PoolValue<>(value);
            registry.put(key, wrapper);
        } else {
            wrapper.updateLastAccess();
        }
        return wrapper.getData();
    }

    public synchronized void remove(K key) {
        checkState();
        if (LOGGER.isTraceEnabled()) {
            if (registry.containsKey(key)) {
                LOGGER.trace(String.format("pool '%s': remove object under key '%s'", name, key));
                registry.remove(key);
            }
        } else {
            registry.remove(key);
        }
    }

    public String getName() {
        return name;
    }

    public boolean isDestroyed() {
        return state == DESTROYED_STATE_CODE;
    }

    public int serializedSize() {
        int result = 0;
        for (Map.Entry<K, PoolValue<V>> entry : registry.entrySet()) {
            result += Utils.serializedSize(entry.getKey());
            result += Utils.serializedSize(entry.getValue());
        }
        return result;
    }

    public int size() {
        return registry.size();
    }

    void destroy() {
        cleaner.interrupt();
        registry.clear();
        state = DESTROYED_STATE_CODE;
    }

    synchronized void cleanExpiredElements() {
        try {
            checkState();
        } catch (UnsupportedOperationException e) {
            return;
        }

        Set<K> toRemove = new HashSet<>();
        long validAfter = System.currentTimeMillis() - timeToLive;
        for (Map.Entry<K, PoolValue<V>> entry : registry.entrySet()) {
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
    }

    private void checkState() {
        if (state == DESTROYED_STATE_CODE) {
            throw new UnsupportedOperationException("The pool " + name + " has already been destroyed");
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(name);
        builder.append(", ").append("size=").append(size());
        builder.append(", ").append("serializedSize=").append(serializedSize());
        return builder.toString();
    }
}
