package com.store.inventory.web;

import com.store.inventory.dto.jsonapi.JsonApiError;
import com.store.inventory.dto.jsonapi.JsonApiErrorResponse;
import com.store.inventory.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<JsonApiErrorResponse> handleProductNotFound(ProductNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, "Not Found", e.getMessage());
    }

    @ExceptionHandler(InventoryNotFoundException.class)
    public ResponseEntity<JsonApiErrorResponse> handleInventoryNotFound(InventoryNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, "Not Found", e.getMessage());
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<JsonApiErrorResponse> handleInsufficientStock(InsufficientStockException e) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", e.getMessage());
    }

    @ExceptionHandler(OptimisticLockConflictException.class)
    public ResponseEntity<JsonApiErrorResponse> handleOptimisticLock(OptimisticLockConflictException e) {
        return error(HttpStatus.CONFLICT, "Conflict", e.getMessage());
    }

    @ExceptionHandler(ProductsServiceUnavailableException.class)
    public ResponseEntity<JsonApiErrorResponse> handleServiceUnavailable(ProductsServiceUnavailableException e) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<JsonApiErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .reduce((a, b) -> a + "; " + b)
            .orElse("Validation failed");
        return error(HttpStatus.BAD_REQUEST, "Bad Request", detail);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<JsonApiErrorResponse> handleMissingHeader(MissingRequestHeaderException e) {
        return error(HttpStatus.BAD_REQUEST, "Bad Request", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JsonApiErrorResponse> handleGeneric(Exception e) {
        log.error("Unexpected error: {}", e.getMessage(), e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred");
    }

    private ResponseEntity<JsonApiErrorResponse> error(HttpStatus status, String title, String detail) {
        return ResponseEntity.status(status)
            .body(JsonApiErrorResponse.builder()
                .errors(List.of(JsonApiError.builder()
                    .status(String.valueOf(status.value()))
                    .title(title)
                    .detail(detail)
                    .correlationId(MDC.get("correlationId"))
                    .build()))
                .build());
    }
}
