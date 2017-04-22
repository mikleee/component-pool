package com.virtual1.componentpool;

import org.apache.log4j.Logger;

import java.util.*;

/**
 * @author Mikhail Tkachenko
 */
public class ComponentPoolRegistry {
    private final static Logger LOGGER = Logger.getLogger(ComponentPoolRegistry.class);
    private final static Map<ComponentPoolKey, ComponentPool> REGISTRY = new HashMap<>();

    static {
        PoolConfig.init();
    }

    public synchronized static Set<ComponentPool> getRegisteredPools() {
        return new HashSet<>(REGISTRY.values());
    }

    public synchronized static <K, V> ComponentPool<K, V> getOrCreate(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Pool name cant be null");
        }

        ComponentPoolKey key = new ComponentPoolKey(name);
        ComponentPool<K, V> pool = REGISTRY.get(key);
        if (pool == null) {
            LOGGER.info(String.format("Register new pool '%s'", name));
            pool = new ComponentPool<>(name);
            REGISTRY.put(key, pool);
        } else {
            LOGGER.debug(String.format("Pool '%s' has been registered already returning the existing one", name));
        }

        return pool;
    }

    public synchronized static void destroy(String name) {
        ComponentPoolKey key = new ComponentPoolKey(name);
        ComponentPool pool = REGISTRY.get(key);
        if (pool == null) {
            LOGGER.warn(String.format("Pool '%s' cant be destroyed because it doesn't exist", name));
        } else {
            pool.destroy();
            REGISTRY.remove(key);
            LOGGER.info(String.format("Pool '%s' has been destroyed", name));
        }
    }

    public synchronized static void destroy(ComponentPool pool) {
        destroy(pool.getName());
    }


}
