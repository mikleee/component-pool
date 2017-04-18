package com.virtual1.componentpool.test;

import com.virtual1.componentpool.ComponentPool;
import com.virtual1.componentpool.ComponentPoolRegistry;
import com.virtual1.componentpool.exception.NonUniquePoolNameException;
import com.virtual1.componentpool.exception.PoolNotFoundException;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by misha on 18.04.17.
 */
public class Tests {

    @Test(expected = PoolNotFoundException.class)
    public void test1_unregistered() {
        ComponentPoolRegistry.get("sraka");
    }


    @Test(expected = NonUniquePoolNameException.class)
    public void test1_duplicateName() {
        ComponentPoolRegistry.register("duplicate");
        ComponentPoolRegistry.register("duplicate");
    }

    @Test
    public void test1_getPool() {
        ComponentPool<Object, Object> myPool = ComponentPoolRegistry.register("myPool");
        ComponentPool<Object, Object> myPool1 = ComponentPoolRegistry.get("myPool");
        Assert.assertTrue(myPool1 == myPool);
    }

    @Test
    public void test1_registerSeveral() {
        String[] names = {"one", "two", "three"};
        for (String name : names) {
            ComponentPoolRegistry.register(name);
        }

        Set<ComponentPool> pools = ComponentPoolRegistry.getRegisteredPools();
        Assert.assertEquals(pools.size(), 3);


        Set<String> poolNames = new HashSet<>();
        for (ComponentPool pool : pools) {
            poolNames.add(pool.getName());
        }

        for (String name : names) {
            Assert.assertTrue(poolNames.contains(name));
            ComponentPoolRegistry.register(name);
        }
    }

    @Test
    public void test1_expiry() throws InterruptedException {
        final ComponentPool<String, String> myPool = ComponentPoolRegistry.register("expiry", 3500L);

        String[][] values = {{"1", "one"}, {"2", "two"}, {"3", "three"}};
        for (final String[] value : values) {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    myPool.putIfAbsent(value[0], value[1]);
                }
            });
            thread.start();
        }
        Thread.sleep(100);

        for (final String[] value : values) {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    String s = myPool.get(value[0]);
                    Assert.assertEquals(s, value[1]);
                }
            });
            thread.start();
        }
        Thread.sleep(100);

        for (String[] value : values) {
            String s = myPool.get(value[0]);
            Assert.assertEquals(s, value[1]);
        }

        Thread.sleep(5 * 1000);

        for (String[] value : values) {
            String s = myPool.get(value[0]);
            Assert.assertNull(s);
        }
    }

    @Test
    public void test1_Random() throws InterruptedException {
        ComponentPoolRegistry.register("random1", 5000L);
        ComponentPoolRegistry.register("random2", 4000L);
        ComponentPoolRegistry.register("random3", 1000L);

        String[][] values = {{"1", "one"}, {"2", "two"}, {"3", "three"}, {"4", "four"}};

        int i = 0;
        while (i++ < 10) {
            for (final ComponentPool pool : ComponentPoolRegistry.getRegisteredPools()) {
                for (final String[] value : values) {
                        Thread thread = new Thread(new Runnable() {
                            public void run() {
                                pool.putIfAbsent(value[0], value[1]);
                            }
                        });
                        thread.start();
                        thread.join();

                }
            }

            Thread.sleep(500L);
        }

        Thread.sleep(3000L);
    }


}
