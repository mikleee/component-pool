package com.virtual1.componentpool.test;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mikhail Tkachenko
 */
public class TestsAsync {
    private final static Logger LOGGER = Logger.getLogger(TestsAsync.class);


    @Test
    public void testAsync() throws InterruptedException, InvocationTargetException, IllegalAccessException {
        Method beforeClass = null;
        Method afterClass = null;
        List<Method> tests = new ArrayList<>();

        Method[] methods = Tests.class.getDeclaredMethods();

        for (Method method : methods) {
            if (method.isAnnotationPresent(BeforeClass.class)) {
                beforeClass = method;
            } else if (method.isAnnotationPresent(AfterClass.class)) {
                afterClass = method;
            } else if (method.isAnnotationPresent(Test.class)) {
                tests.add(method);
            }
        }

        final Tests inst = new Tests();

        if (beforeClass != null) {
            beforeClass.invoke(inst);
        }

        for (final Method test : tests) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        test.invoke(inst);
                    } catch (Exception ignore) {
                    }
                }
            });
            thread.start();
            thread.join();
        }

        if (afterClass != null) {
            afterClass.invoke(inst);
        }
    }

}
