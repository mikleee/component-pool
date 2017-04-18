package com.virtual1.componentpool.exception;

/**
 * Created by misha on 18.04.17.
 */
public class PoolNotFoundException extends RuntimeException {
    public PoolNotFoundException(String message) {
        super(message);
    }
}
