package com.store.products.common.handler;

import com.store.products.common.dto.ApiError;
import com.store.products.common.filter.CorrelationIdFilter;
import com.store.products.exception.ProductNotFoundException;
import com.store.products.exception.SkuAlreadyExistsException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ProductNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }

    @ExceptionHandler(SkuAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleConflict(SkuAlreadyExistsException ex) {
        return error(HttpStatus.CONFLICT, "Conflict", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .reduce("", (a, b) -> a.isEmpty() ? b : a + "; " + b);
        return error(HttpStatus.UNPROCESSABLE_ENTITY, "Validation Error", detail);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Unexpected error");
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String title, String detail) {
        ApiError body = ApiError.builder()
            .errors(List.of(ApiError.ErrorObject.builder()
                .status(String.valueOf(status.value()))
                .title(title)
                .detail(detail)
                .correlationId(MDC.get(CorrelationIdFilter.MDC_KEY))
                .build()))
            .build();
        return ResponseEntity.status(status).body(body);
    }
}
