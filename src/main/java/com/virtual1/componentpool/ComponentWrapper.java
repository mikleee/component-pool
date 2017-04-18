package com.virtual1.componentpool;

/**
 * Created by misha on 18.04.17.
 */
class ComponentWrapper<T> {
    private long created = System.currentTimeMillis();
    private T data;

    ComponentWrapper(T data) {
        this.data = data;
    }

    long getCreated() {
        return created;
    }

    T getData() {
        return data;
    }


}
