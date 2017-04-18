package com.virtual1.componentpool;

import org.apache.log4j.Logger;

/**
 * Created by misha on 18.04.17.
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
        while (isAlive()) {
            try {
                Thread.sleep(PoolConfig.getCleanSchedule());
            } catch (InterruptedException ignore) {
            }
            LOGGER.debug("Schedule pool cleaner for pool " + pool.getName());
            pool.cleanExpiredElements();
        }
        LOGGER.debug(getName() + " has been stopped");
    }


}
