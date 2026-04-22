package com.store.inventory.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.inventory.domain.IdempotencyKey;
import com.store.inventory.domain.Inventory;
import com.store.inventory.dto.PurchaseRequest;
import com.store.inventory.dto.jsonapi.JsonApiData;
import com.store.inventory.dto.jsonapi.JsonApiResponse;
import com.store.inventory.exception.InsufficientStockException;
import com.store.inventory.exception.InventoryNotFoundException;
import com.store.inventory.exception.OptimisticLockConflictException;
import com.store.inventory.repository.IdempotencyKeyRepository;
import com.store.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private static final int MAX_OPTIMISTIC_RETRIES = 3;

    private final InventoryRepository inventoryRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final ProductsClient productsClient;
    private final ObjectMapper objectMapper;

    public JsonApiResponse getByProductId(UUID productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new InventoryNotFoundException(productId));
        return toJsonApiResponse(inventory);
    }

    public JsonApiResponse purchase(UUID idempotencyKey, PurchaseRequest request) {
        return idempotencyKeyRepository.findById(idempotencyKey)
            .map(cached -> deserializeCachedResponse(cached.getResponseBody()))
            .orElseGet(() -> processPurchase(idempotencyKey, request));
    }

    private JsonApiResponse processPurchase(UUID idempotencyKey, PurchaseRequest request) {
        productsClient.validateProductExists(request.getProductId());

        Inventory inventory = deductWithRetry(request);

        JsonApiResponse response = toJsonApiResponse(inventory);
        saveIdempotencyKey(idempotencyKey, response, 200);

        log.info("inventory_changed event: productId={}, quantityDeducted={}, remaining={}, correlationId={}",
            request.getProductId(), request.getQuantity(), inventory.getAvailable(), MDC.get("correlationId"));

        return response;
    }

    private Inventory deductWithRetry(PurchaseRequest request) {
        int attempts = 0;
        while (attempts < MAX_OPTIMISTIC_RETRIES) {
            try {
                return doDeduct(request);
            } catch (OptimisticLockingFailureException e) {
                attempts++;
                log.warn("Optimistic lock conflict attempt {}/{} for productId={}",
                    attempts, MAX_OPTIMISTIC_RETRIES, request.getProductId());
                if (attempts >= MAX_OPTIMISTIC_RETRIES) {
                    throw new OptimisticLockConflictException();
                }
            }
        }
        throw new OptimisticLockConflictException();
    }

    @Transactional
    protected Inventory doDeduct(PurchaseRequest request) {
        Inventory inventory = inventoryRepository.findByProductId(request.getProductId())
            .orElseThrow(() -> new InventoryNotFoundException(request.getProductId()));

        if (inventory.getAvailable() < request.getQuantity()) {
            throw new InsufficientStockException(request.getQuantity(), inventory.getAvailable());
        }

        inventory.setAvailable(inventory.getAvailable() - request.getQuantity());
        return inventoryRepository.save(inventory);
    }

    private void saveIdempotencyKey(UUID key, JsonApiResponse response, int status) {
        try {
            String body = objectMapper.writeValueAsString(response);
            idempotencyKeyRepository.save(IdempotencyKey.builder()
                .idempotencyKey(key)
                .responseBody(body)
                .httpStatus(status)
                .createdAt(LocalDateTime.now())
                .build());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize idempotency response: {}", e.getMessage());
        }
    }

    private JsonApiResponse deserializeCachedResponse(String body) {
        try {
            return objectMapper.readValue(body, JsonApiResponse.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize cached idempotency response: {}", e.getMessage());
            throw new RuntimeException("Cached response deserialization failed");
        }
    }

    private JsonApiResponse toJsonApiResponse(Inventory inventory) {
        return JsonApiResponse.builder()
            .data(JsonApiData.builder()
                .id(inventory.getId().toString())
                .type("inventory")
                .attributes(Map.of(
                    "productId", inventory.getProductId(),
                    "available", inventory.getAvailable()
                ))
                .build())
            .build();
    }
}
