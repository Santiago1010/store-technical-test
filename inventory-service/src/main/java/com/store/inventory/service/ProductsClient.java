package com.store.inventory.service;

import com.store.inventory.exception.ProductNotFoundException;
import com.store.inventory.exception.ProductsServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductsClient {

    private final WebClient productsWebClient;

    @CircuitBreaker(name = "productsService", fallbackMethod = "circuitBreakerFallback")
    @Retry(name = "productsService")
    public void validateProductExists(UUID productId) {
        String correlationId = MDC.get("correlationId");
        try {
            productsWebClient.get()
                .uri("/api/v1/products/{id}", productId)
                .header("X-Correlation-Id", correlationId != null ? correlationId : "")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    if (response.statusCode().value() == 404) {
                        return response.createException()
                            .map(e -> new ProductNotFoundException(productId));
                    }
                    return response.createException();
                })
                .toBodilessEntity()
                .block();
        } catch (ProductNotFoundException e) {
            throw e;
        } catch (WebClientResponseException e) {
            log.warn("Products service error: status={}, correlationId={}", e.getStatusCode(), correlationId);
            throw new ProductsServiceUnavailableException("Products service returned error: " + e.getStatusCode());
        } catch (Exception e) {
            log.warn("Products service call failed: {}, correlationId={}", e.getMessage(), correlationId);
            throw new ProductsServiceUnavailableException("Products service call failed: " + e.getMessage());
        }
    }

    public void circuitBreakerFallback(UUID productId, Throwable t) {
        if (t instanceof ProductNotFoundException) {
            throw (ProductNotFoundException) t;
        }
        log.error("Circuit breaker open for productsService: {}", t.getMessage());
        throw new ProductsServiceUnavailableException("Products service is unavailable (circuit open)");
    }
}
