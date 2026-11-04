package com.QuadGPT.backend.exception;

// The document exists but is not shared with the user's department.
public class DocumentAccessDeniedException extends RuntimeException {

    public DocumentAccessDeniedException() {
        super("You cannot access this document");
    }
}
