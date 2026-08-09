package com.nightflow.orderservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleOrderNotFound(OrderNotFoundException ex) {
        return body(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return body(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage(), Map.of());
    }

    /**
     * A compensated payment is a business outcome, not a server fault: answer
     * 409 and hand back the state the order was left in, so the caller can tell
     * "your tickets were released, try again" (FAILED) apart from "we could not
     * clean up, do not retry blindly" (COMPENSATION_FAILED).
     */
    @ExceptionHandler(OrderSagaFailedException.class)
    public ResponseEntity<Map<String, Object>> handleSagaFailed(OrderSagaFailedException ex) {
        Map<String, Object> details = new HashMap<>();
        details.put("orderNumber", ex.getOrder().getOrderNumber());
        details.put("orderStatus", ex.getOrder().getStatus());
        details.put("failureReason", ex.getOrder().getFailureReason());
        return body(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), details);
    }

    /** payOrder on an order that is not PENDING, and similar state guards. */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        return body(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), Map.of());
    }

    private ResponseEntity<Map<String, Object>> body(HttpStatus status,
                                                     String error,
                                                     String message,
                                                     Map<String, Object> details) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("timestamp", LocalDateTime.now());
        payload.put("status", status.value());
        payload.put("error", error);
        payload.put("message", message);
        payload.putAll(details);
        return new ResponseEntity<>(payload, status);
    }
}
