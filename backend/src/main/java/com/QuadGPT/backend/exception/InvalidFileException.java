package com.QuadGPT.backend.exception;

// The uploaded file is empty or is not a readable PDF.
public class InvalidFileException extends RuntimeException {

    public InvalidFileException(String message) {
        super(message);
    }

    public InvalidFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
