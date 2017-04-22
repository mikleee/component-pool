package com.virtual1.componentpool;

import org.apache.log4j.Logger;

/**
 * @author Mikhail Tkachenko
 */
class PoolCleaner extends Thread {
    private final static Logger LOGGER = Logger.getLogger(PoolCleaner.class);

    private final ComponentPool pool;

     PoolCleaner(ComponentPool pool) {
        super("pool-cleaner-" + pool.getName());
        this.pool = pool;
        start();
    }

    @Override
    public void run() {
        LOGGER.debug(getName() + " has been started");
        while (!isInterrupted()) {
            System.out.println(isInterrupted());
            try {
                Thread.sleep(PoolConfig.getCleanSchedule());
                LOGGER.debug("Schedule pool cleaner for pool " + pool.getName());
                pool.cleanExpiredElements();
            } catch (InterruptedException ignore) {
                LOGGER.debug(getName() + " has been interuprted");
                interrupt();
            }
        }
        LOGGER.debug(getName() + " has been stopped");
    }


}
