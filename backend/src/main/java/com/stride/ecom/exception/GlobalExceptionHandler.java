package com.stride.ecom.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * GlobalExceptionHandler
 * Catches all exceptions and returns structured JSON error responses.
 *
 * STRIDE: Information Disclosure
 * Mitigation: Returns generic error messages — never exposes stack traces,
 *             SQL errors, or internal paths to the client.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Validation errors (e.g., blank email, short password)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status",  400,
                "errors",  errors
        ));
    }

    // Access denied — user tried to access admin route
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status",  403,
                "error",   "Access Denied — insufficient permissions"
        ));
    }

    // Generic runtime errors
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status",  400,
                "error",   ex.getMessage()
        ));
    }

    // All other errors — generic message (never expose internals)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status",  500,
                "error",   "An internal error occurred"
                // Note: ex.getMessage() is NOT returned — STRIDE: Information Disclosure mitigation
        ));
    }
}
