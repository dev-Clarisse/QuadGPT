package com.QuadGPT.backend.exception;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

/**
 * Turns exceptions into an HTTP status with a plain-text message, which the frontend
 * displays as is. Services only throw the exceptions of this package and never deal
 * with HTTP: the status of each one is decided here. Security errors (401/403) are not handled here: Spring Security
 * answers them before or after the controllers.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ===== Application exceptions =====

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleNotFound(ResourceNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler(DocumentAccessDeniedException.class)
    public ResponseEntity<String> handleDocumentAccessDenied(DocumentAccessDeniedException exception) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(exception.getMessage());
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<String> handleInvalidFile(InvalidFileException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(exception.getMessage());
    }

    @ExceptionHandler(UnknownUserException.class)
    public ResponseEntity<String> handleUnknownUser(UnknownUserException exception) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(exception.getMessage());
    }

    @ExceptionHandler(DuplicateDocumentException.class)
    public ResponseEntity<String> handleDuplicateDocument(
            DuplicateDocumentException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(exception.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<String> handleInvalidCredentials(
            InvalidCredentialsException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(exception.getMessage());
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<String> handleEmailAlreadyRegistered(
            EmailAlreadyRegisteredException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(exception.getMessage());
    }

    // ===== Framework exceptions =====

    // Last line of defence against concurrent inserts (same email, same file hash).
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolation(
            DataIntegrityViolationException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body("This resource already exists");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining(", "));
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(message);
    }

    // Malformed JSON, or an unknown value such as a department that does not exist.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleUnreadableBody(HttpMessageNotReadableException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Invalid request body");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Invalid value for parameter '" + exception.getName() + "'");
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MissingServletRequestPartException.class})
    public ResponseEntity<String> handleMissingParameter(Exception exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(exception.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleFileTooLarge(MaxUploadSizeExceededException exception) {
        return ResponseEntity
                .status(HttpStatus.CONTENT_TOO_LARGE)
                .body("The file is too large (20 MB maximum)");
    }

    // Ollama (chat or embeddings) did not answer: the problem is not the client's request.
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<String> handleAiServiceDown(RestClientException exception) {
        log.error("AI service call failed", exception);
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("The AI service is unavailable, please try again later");
    }
}
