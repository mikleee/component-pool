package com.virtual1.componentpool;

import java.io.Serializable;

/**
 * @author Mikhail Tkachenko
 */
class PoolValue<T extends Serializable> implements Serializable {
    private long lastAccess;
    private T data;

    PoolValue(T data) {
        this.data = data;
        updateLastAccess();
    }

    long getLastAccess() {
        return lastAccess;
    }

    void updateLastAccess() {
        lastAccess = System.currentTimeMillis();
    }

    T getData() {
        return data;
    }

    void setData(T data) {
        this.data = data;
    }
}
