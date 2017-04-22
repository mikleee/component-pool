package com.virtual1.componentpool.test;

import com.virtual1.componentpool.ComponentPool;
import com.virtual1.componentpool.ComponentPoolRegistry;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

/**
 * @author Mikhail Tkachenko
 */
public class Tests {

    @Test
    public void test_registerAndDestroy_1() {
        String key = key();
        ComponentPool<Object, Object> pool = ComponentPoolRegistry.getOrCreate(key);
        Assert.assertEquals(ComponentPoolRegistry.getRegisteredPools().size(), 1);
        ComponentPoolRegistry.destroy(pool);
        Assert.assertEquals(ComponentPoolRegistry.getRegisteredPools().size(), 0);
    }

    @Test
    public void test_registerAndDestroy_2() {
        String key = key();
        ComponentPoolRegistry.getOrCreate(key);
        Assert.assertEquals(ComponentPoolRegistry.getRegisteredPools().size(), 1);
        ComponentPoolRegistry.destroy(key);
        Assert.assertEquals(ComponentPoolRegistry.getRegisteredPools().size(), 0);
    }

    @Test
    public void test_registerAndGet() {
        String key = key();
        ComponentPool<Object, Object> pool = ComponentPoolRegistry.getOrCreate(key);
        ComponentPool<Object, Object> pool1 = ComponentPoolRegistry.getOrCreate(key);
        Assert.assertTrue(pool == pool1);
        ComponentPoolRegistry.destroy(pool);
    }

    @Test
    public void test_putAndGet() {
        String key = key();
        ComponentPool<String, String> pool = ComponentPoolRegistry.getOrCreate(key);
        String value = key();
        String value1 = pool.putIfAbsent("1", value);
        Assert.assertTrue(value == value1);
        String value2 = pool.get("1");
        Assert.assertTrue(value == value2);
        ComponentPoolRegistry.destroy(pool);
    }

    @Test(expected = ClassCastException.class)
    public void test_registerAndGetWithDifferentParametrisation() {
        String key = key();
        ComponentPool<Integer, String> pool = null;
        try {
            pool = ComponentPoolRegistry.getOrCreate(key);
            ComponentPool<Integer, Integer> pool1 = ComponentPoolRegistry.getOrCreate(key);
            String string = pool.putIfAbsent(1, "1");
            Integer integer = pool1.get(1);
        } finally {
            ComponentPoolRegistry.destroy(pool);
        }
    }

    private String key() {
        return UUID.randomUUID().toString();
    }

}
