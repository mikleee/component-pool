package com.virtual1.componentpool.exception;

/**
 * Created by misha on 18.04.17.
 */
public class NonUniquePoolNameException extends RuntimeException {
    public NonUniquePoolNameException(String message) {
        super(message);
    }
}
