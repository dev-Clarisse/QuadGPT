package com.QuadGPT.backend.exception;

// The token is still valid but its account no longer exists.
public class UnknownUserException extends RuntimeException {

    public UnknownUserException() {
        super("Unknown user");
    }
}
