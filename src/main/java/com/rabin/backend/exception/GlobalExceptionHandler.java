package com.rabin.backend.exception;

import com.rabin.backend.dto.GenericApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for all REST controllers
 * Provides proper error messages and HTTP status codes
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle validation errors (e.g., @Valid annotations)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericApiResponse<Map<String, String>>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("❌ Validation error: {}", errors);
        return ResponseEntity.badRequest()
                .body(GenericApiResponse.error(400, "Validation failed: " + errors.toString()));
    }

    /**
     * Handle illegal argument exceptions (e.g., invalid input)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GenericApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("❌ Illegal argument: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(GenericApiResponse.error(400, ex.getMessage()));
    }

    /**
     * Handle illegal state exceptions (e.g., business logic violations)
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<GenericApiResponse<Void>> handleIllegalState(IllegalStateException ex) {
        log.warn("❌ Illegal state: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(GenericApiResponse.error(409, ex.getMessage()));
    }

    /**
     * Handle authentication exceptions (e.g., invalid credentials)
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<GenericApiResponse<Void>> handleAuthentication(AuthenticationException ex) {
        log.warn("❌ Authentication error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(GenericApiResponse.error(401, ex.getMessage() != null ? ex.getMessage() : "Authentication failed"));
    }

    /**
     * Handle bad credentials (e.g., wrong password)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<GenericApiResponse<Void>> handleBadCredentialsException(BadCredentialsException ex) {
        log.warn("❌ Bad credentials for authentication attempt");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(GenericApiResponse.error(401, "Invalid email or password"));
    }

    /**
     * Handle access denied exceptions (e.g., insufficient permissions)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<GenericApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("❌ Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(GenericApiResponse.error(403, "You don't have permission to access this resource"));
    }

    /**
     * Handle resource not found exceptions
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<GenericApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("❌ Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(GenericApiResponse.error(404, ex.getMessage()));
    }

    /**
     * Handle invalid credentials exception
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<GenericApiResponse<Void>> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        log.warn("❌ Invalid credentials: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(GenericApiResponse.error(401, ex.getMessage()));
    }

    /**
     * Handle invalid token exception
     */
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<GenericApiResponse<Void>> handleInvalidTokenException(InvalidTokenException ex) {
        log.warn("❌ Invalid token: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(GenericApiResponse.error(401, ex.getMessage()));
    }

    /**
     * Handle user not found exception
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<GenericApiResponse<Void>> handleUserNotFoundException(UserNotFoundException ex) {
        log.warn("❌ User not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(GenericApiResponse.error(404, ex.getMessage()));
    }

    /**
     * Handle file upload size exceeded
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<GenericApiResponse<Void>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        log.warn("❌ File upload size exceeded");
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(GenericApiResponse.error(413, "File size exceeds maximum limit of 5MB"));
    }

    /**
     * Handle generic runtime exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<GenericApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        log.error("❌ Runtime exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GenericApiResponse.error(500, ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred"));
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("❌ Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GenericApiResponse.error(500, "An unexpected error occurred: " + ex.getMessage()));
    }
}
