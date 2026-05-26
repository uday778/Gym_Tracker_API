package com.gymtracker.exception;

import com.gymtracker.dto.response.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ── 400 Validation (@Valid failures) ─────────────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value",
                        (first, second) -> first  // keep first error per field
                ));

        return ResponseEntity.badRequest().body(
                ApiErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(400)
                        .errorCode("VALIDATION_FAILED")
                        .message("Request validation failed. Check fieldErrors for details.")
                        .fieldErrors(fieldErrors)
                        .build());
    }

    // ── 400 Type mismatch (e.g. string passed for enum/Long param) ───────────
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String msg = String.format("Parameter '%s' has invalid value: '%s'", ex.getName(), ex.getValue());
        return ResponseEntity.badRequest().body(error(400, "INVALID_PARAMETER", msg));
    }

    // ── 401 Bad credentials ───────────────────────────────────────────────────
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(401).body(error(401, "INVALID_CREDENTIALS", "Invalid email or password"));
    }

    // ── 403 Access denied ─────────────────────────────────────────────────────
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(403).body(error(403, "FORBIDDEN", "You do not have permission to access this resource"));
    }

    // ── 404 Not found ─────────────────────────────────────────────────────────
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(404).body(error(404, "RESOURCE_NOT_FOUND", ex.getMessage()));
    }

    // ── 409 Conflict ──────────────────────────────────────────────────────────
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(UserAlreadyExistsException ex) {
        return ResponseEntity.status(409).body(error(409, "USER_ALREADY_EXISTS", ex.getMessage()));
    }

    // ── 422 Invalid business state ────────────────────────────────────────────
    @ExceptionHandler(InvalidSubscriptionStateException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidState(InvalidSubscriptionStateException ex) {
        return ResponseEntity.status(422).body(error(422, "INVALID_SUBSCRIPTION_STATE", ex.getMessage()));
    }

    // ── 500 Catch-all — never leak internals ──────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAll(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error(500, "INTERNAL_ERROR", "An unexpected error occurred. Please try again later."));
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private ApiErrorResponse error(int status, String code, String message) {
        return ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status)
                .errorCode(code)
                .message(message)
                .build();
    }
}
