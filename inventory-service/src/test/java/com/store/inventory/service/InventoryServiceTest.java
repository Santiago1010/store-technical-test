package com.store.inventory.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.inventory.domain.IdempotencyKey;
import com.store.inventory.domain.Inventory;
import com.store.inventory.dto.PurchaseRequest;
import com.store.inventory.exception.*;
import com.store.inventory.repository.IdempotencyKeyRepository;
import com.store.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock InventoryRepository inventoryRepository;
    @Mock IdempotencyKeyRepository idempotencyKeyRepository;
    @Mock ProductsClient productsClient;
    @Spy  ObjectMapper objectMapper;
    @InjectMocks InventoryService service;

    UUID productId;
    UUID idempKey;
    Inventory inventory;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        idempKey  = UUID.randomUUID();
        inventory = Inventory.builder()
            .id(UUID.randomUUID())
            .productId(productId)
            .available(10)
            .build();
    }

    // --- getByProductId ---

    @Test
    void getByProductId_found() {
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));
        var res = service.getByProductId(productId);
        assertThat(res.getData().getType()).isEqualTo("inventory");
    }

    @Test
    void getByProductId_notFound_throws() {
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getByProductId(productId))
            .isInstanceOf(InventoryNotFoundException.class);
    }

    // --- purchase (idempotency) ---

    @Test
    void purchase_cachedIdempotencyKey_returnsCached() throws Exception {
        var cached = IdempotencyKey.builder()
            .idempotencyKey(idempKey)
            .responseBody(objectMapper.writeValueAsString(
                com.store.inventory.dto.jsonapi.JsonApiResponse.builder()
                    .data(com.store.inventory.dto.jsonapi.JsonApiData.builder()
                        .id(inventory.getId().toString()).type("inventory")
                        .attributes(java.util.Map.of("available", 10)).build())
                    .build()))
            .build();
        when(idempotencyKeyRepository.findById(idempKey)).thenReturn(Optional.of(cached));

        var res = service.purchase(idempKey, new PurchaseRequest(productId, 1));
        assertThat(res).isNotNull();
        verifyNoInteractions(productsClient);
    }

    // --- doDeduct (via purchase) ---

    @Test
    void purchase_sufficientStock_deductsAndSaves() {
        when(idempotencyKeyRepository.findById(idempKey)).thenReturn(Optional.empty());
        doNothing().when(productsClient).validateProductExists(productId);
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var res = service.purchase(idempKey, new PurchaseRequest(productId, 3));

        assertThat(res.getData().getAttributes()).containsEntry("available", 7);
        verify(idempotencyKeyRepository).save(any());
    }

    @Test
    void purchase_insufficientStock_throws() {
        when(idempotencyKeyRepository.findById(idempKey)).thenReturn(Optional.empty());
        doNothing().when(productsClient).validateProductExists(productId);
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));

        assertThatThrownBy(() -> service.purchase(idempKey, new PurchaseRequest(productId, 99)))
            .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void purchase_productNotFound_throws() {
        when(idempotencyKeyRepository.findById(idempKey)).thenReturn(Optional.empty());
        doThrow(new ProductNotFoundException(productId))
            .when(productsClient).validateProductExists(productId);

        assertThatThrownBy(() -> service.purchase(idempKey, new PurchaseRequest(productId, 1)))
            .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void purchase_optimisticLockExhausted_throws() {
        when(idempotencyKeyRepository.findById(idempKey)).thenReturn(Optional.empty());
        doNothing().when(productsClient).validateProductExists(productId);
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any())).thenThrow(OptimisticLockingFailureException.class);

        assertThatThrownBy(() -> service.purchase(idempKey, new PurchaseRequest(productId, 1)))
            .isInstanceOf(OptimisticLockConflictException.class);
    }
}