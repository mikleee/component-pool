package com.virtual1.componentpool.test;

import com.virtual1.componentpool.ComponentPool;
import com.virtual1.componentpool.ComponentPoolRegistry;
import org.junit.*;

import java.util.UUID;

/**
 * @author Mikhail Tkachenko
 */
public class Tests {
    private static int threadsCount = 0;

    @BeforeClass
    public static void doFirst() {
        threadsCount = Thread.activeCount();
        System.out.println(threadsCount + " active threads");
    }

    @AfterClass
    public static void doFinally() {
        int threadsCount = Thread.activeCount();
        System.out.println(threadsCount + " active threads");
        Assert.assertEquals(Tests.threadsCount, threadsCount);
    }


    @Test
    public void test_registerAndDestroy_1() {
        ComponentPool<Object, Object> pool = ComponentPoolRegistry.getOrCreate("test_registerAndDestroy_1");
        Assert.assertEquals(ComponentPoolRegistry.getRegisteredPools().size(), 1);
        ComponentPoolRegistry.destroy(pool);
        Assert.assertEquals(ComponentPoolRegistry.getRegisteredPools().size(), 0);
    }

    @Test
    public void test_registerAndDestroy_2() {
        String key = "test_registerAndDestroy_2";
        ComponentPoolRegistry.getOrCreate(key);
        Assert.assertEquals(ComponentPoolRegistry.getRegisteredPools().size(), 1);
        ComponentPoolRegistry.destroy(key);
        Assert.assertEquals(ComponentPoolRegistry.getRegisteredPools().size(), 0);
    }

    @Test
    public void test_registerAndGet() {
        String key = "test_registerAndGet";
        ComponentPool<Object, Object> pool = ComponentPoolRegistry.getOrCreate(key);
        ComponentPool<Object, Object> pool1 = ComponentPoolRegistry.getOrCreate(key);
        Assert.assertTrue(pool == pool1);
        ComponentPoolRegistry.destroy(pool);
    }

    @Test
    public void test_putAndGet() {
        String key = "test_putAndGet";
        ComponentPool<String, String> pool = ComponentPoolRegistry.getOrCreate(key);
        String value = key();
        String value1 = pool.putIfAbsent("1", value);
        Assert.assertTrue(value == value1);
        String value2 = pool.get("1");
        Assert.assertTrue(value == value2);
        String absent = pool.get("2");
        Assert.assertNull(absent);
        ComponentPoolRegistry.destroy(pool);
    }

    @Test(expected = ClassCastException.class)
    public void test_registerAndGetWithDifferentParametrisation() {
        String key = "test_registerAndGetWithDifferentParametrisation";
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

    @Test(expected = UnsupportedOperationException.class)
    public void test_accessToDestroyedPool() {
        String key = "test_accessToDestroyedPool";
        ComponentPool<Integer, String> pool = ComponentPoolRegistry.getOrCreate(key);
        pool.putIfAbsent(1, "1");
        ComponentPoolRegistry.destroy(pool);
        pool.get(1);
    }

    private String key() {
        return UUID.randomUUID().toString();
    }

}
