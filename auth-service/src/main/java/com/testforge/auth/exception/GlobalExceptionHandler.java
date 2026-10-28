package com.testforge.auth.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> fieldErrors = new TreeMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(
                    error.getField(),
                    error.getDefaultMessage() == null
                            ? "Invalid value"
                            : error.getDefaultMessage()
            );
        }

        return build(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                "Please correct the invalid fields",
                request,
                fieldErrors
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        return build(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                "Request parameters are invalid",
                request,
                Map.of()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableRequest(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        return build(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "Request body is missing or contains invalid JSON",
                request,
                Map.of()
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(
            ResponseStatusException ex,
            HttpServletRequest request) {

        HttpStatus status = HttpStatus.valueOf(
                ex.getStatusCode().value()
        );

        return build(
                status,
                status.getReasonPhrase(),
                ex.getReason() == null
                        ? status.getReasonPhrase()
                        : ex.getReason(),
                request,
                Map.of()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        log.warn("Database constraint violation on {}",
                request.getRequestURI());

        return build(
                HttpStatus.CONFLICT,
                "Conflict",
                "The request conflicts with existing data",
                request,
                Map.of()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
            Exception ex,
            HttpServletRequest request) {

        log.error(
                "Unexpected error processing {} {}",
                request.getMethod(),
                request.getRequestURI(),
                ex
        );

        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                request,
                Map.of()
        );
    }

    private ResponseEntity<ApiErrorResponse> build(
            HttpStatus status,
            String error,
            String message,
            HttpServletRequest request,
            Map<String, String> fieldErrors) {

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                error,
                message,
                request.getRequestURI(),
                fieldErrors
        );

        return ResponseEntity.status(status).body(response);
    }
}