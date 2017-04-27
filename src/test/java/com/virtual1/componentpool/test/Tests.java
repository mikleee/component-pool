package com.virtual1.componentpool.test;

import com.virtual1.componentpool.ComponentPool;
import com.virtual1.componentpool.ComponentPoolRegistry;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author Mikhail Tkachenko
 */
public class Tests {
    private final static Logger LOGGER = Logger.getLogger(Tests.class);
    private static int threadsCount = 0;

    @BeforeClass
    public static void doFirst() {
        threadsCount = Thread.activeCount();
        LOGGER.info(threadsCount + " active threads");
    }

    @AfterClass
    public static void doFinally() throws InterruptedException {
        Thread.sleep(1000);

        threadsCount = Thread.activeCount();
        LOGGER.info(threadsCount + " active threads");
        Assert.assertEquals(Tests.threadsCount, threadsCount);
    }


    @Test
    public void test_registerAndDestroy_1() {
        ComponentPool<String, String> pool = ComponentPoolRegistry.getOrCreate("test_registerAndDestroy_1");
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
        ComponentPool<String, String> pool = ComponentPoolRegistry.getOrCreate(key);
        ComponentPool<String, String> pool1 = ComponentPoolRegistry.getOrCreate(key);
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

    @Test
    public void test_asyncMess() throws InterruptedException {
        String poolKey = "test_asyncMess";
        final ComponentPool<Character, String> pool = ComponentPoolRegistry.getOrCreate(poolKey);

        final String keySource = "qwertyuiopasdfghjklzxcvbnm";
        final List<Thread> threads = new ArrayList<>();


        int count = 1000;
        while (count-- > 0) {
            threads.add(new Thread(new Runnable() {
                public void run() {
                    traceWaitingThreads(threads);
                    Character key = keySource.charAt((int) Math.round(Math.random() * (keySource.length() - 1)));
                    pool.putIfAbsent(key, key());
                }
            }, "put-" + count));
            threads.add(new Thread(new Runnable() {
                public void run() {
                    traceWaitingThreads(threads);
                    Character key = keySource.charAt((int) Math.round(Math.random() * (keySource.length() - 1)));
                    pool.remove(key);
                }
            }, "remove-" + count));
            threads.add(new Thread(new Runnable() {
                public void run() {
                    traceWaitingThreads(threads);
                    Character key = keySource.charAt((int) Math.round(Math.random() * (keySource.length() - 1)));
                    pool.get(key);
                }
            }, "remove-" + count));
        }
        Collections.shuffle(threads);

        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        ComponentPoolRegistry.destroy(poolKey);
    }

    private String key() {
        return UUID.randomUUID().toString();
    }

    private void traceWaitingThreads(List<Thread> threads) {
        int result = 0;
        for (Thread thread : threads) {
            Thread.State state = thread.getState();
            switch (state) {
                case BLOCKED:
                    result++;
            }
        }
        if (result > 0) {
            LOGGER.info("Blocked ones: " + result);
        }
    }

}
