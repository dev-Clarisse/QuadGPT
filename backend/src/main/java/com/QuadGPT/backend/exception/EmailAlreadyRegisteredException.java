package com.QuadGPT.backend.exception;

public class EmailAlreadyRegisteredException extends RuntimeException {

    public EmailAlreadyRegisteredException() {
        super("Email already registered");
    }
}
