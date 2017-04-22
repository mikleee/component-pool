package com.virtual1.componentpool;

import com.virtual1.componentpool.exception.NotSupportedOperationException;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Mikhail Tkachenko
 */
public class ComponentPool<K, V> {
    private final static int RUN_STATE_CODE = 1;
    private final static int DESTROYED_STATE_CODE = 2;

    private final static Logger LOGGER = Logger.getLogger(ComponentPool.class);
    private final Map<K, ComponentWrapper<V>> registry = new HashMap<>();

    private final String name;
    private long timeToLive;
    private final PoolCleaner cleaner;

    private int state;

    ComponentPool(String name) {
        this.name = name;
        this.timeToLive = PoolConfig.getTimeToLive();
        this.cleaner = new PoolCleaner(this);
        this.state = RUN_STATE_CODE;
    }

    public synchronized V get(K key) {
        checkState();
        ComponentWrapper<V> wrapper = registry.get(key);
        if (wrapper != null) {
            wrapper.updateLastAccess();
            return wrapper.getData();
        } else {
            return null;
        }
    }


    public synchronized V putIfAbsent(K key, V value) {
        checkState();
        ComponentWrapper<V> wrapper = registry.get(key);
        if (wrapper == null || (value != null && !value.equals(wrapper.getData()))) {
            LOGGER.trace(String.format("pool '%s': put object under key '%s'", name, key));
            wrapper = new ComponentWrapper<>(value);
            registry.put(key, wrapper);
        } else {
            wrapper.updateLastAccess();
        }
        return wrapper.getData();
    }

    public synchronized void remove(K key) {
        checkState();
        registry.remove(key);
    }

    public String getName() {
        return name;
    }

    void destroy() {
        cleaner.interrupt();
        registry.clear();
        state = DESTROYED_STATE_CODE;
    }

    synchronized void cleanExpiredElements() {
        try {
            checkState();
        } catch (NotSupportedOperationException e) {
            return;
        }

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
    }

    private void checkState() {
        if (state == DESTROYED_STATE_CODE) {
            throw new NotSupportedOperationException("The pool " + name + " has already been destroyed");
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
