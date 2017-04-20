package com.virtual1.componentpool;

/**
 * Created by misha on 18.04.17.
 */
class ComponentWrapper<T> {
    private long lastAccess;
    private T data;

    ComponentWrapper(T data) {
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


}
