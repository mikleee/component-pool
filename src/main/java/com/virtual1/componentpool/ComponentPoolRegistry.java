package com.virtual1.componentpool;

import com.virtual1.componentpool.exception.NonUniquePoolNameException;
import com.virtual1.componentpool.exception.PoolNotFoundException;

import java.util.*;

/**
 * Created by misha on 18.04.17.
 */
public class ComponentPoolRegistry {
    private final static Map<String, ComponentPool> registry = new HashMap<>();


    public static Set<ComponentPool> getRegisteredPools() {
        return new HashSet<>(registry.values());
    }

    public static <K, V> ComponentPool<K, V> register(String name, Long liveTime) {
        if (registry.containsKey(name)) {
            throw new NonUniquePoolNameException("Pool with name " + name + " has been already registered");
        }

        ComponentPool<K, V> pool = new ComponentPool<>(name, liveTime);
        registry.put(name, pool);
        return pool;
    }

    public static <K, V> ComponentPool<K, V> register(String name) {
        return register(name, null);
    }

    public static <K, V> ComponentPool<K, V> get(String name) {
        if (!registry.containsKey(name)) {
            throw new PoolNotFoundException("Pool with name " + name + " hasn't been registered");
        }
        return (ComponentPool<K, V>) registry.get(name);
    }

}
