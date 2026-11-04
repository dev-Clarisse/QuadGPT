package com.QuadGPT.backend.exception;

// A document, user or conversation that does not exist (or that the user may not know about).
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource) {
        super(resource + " not found");
    }
}
