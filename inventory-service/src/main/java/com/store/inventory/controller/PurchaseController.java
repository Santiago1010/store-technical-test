package com.store.inventory.controller;

import com.store.inventory.dto.PurchaseRequest;
import com.store.inventory.dto.jsonapi.JsonApiResponse;
import com.store.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
@Tag(name = "Purchases", description = "Purchase operations with idempotency")
public class PurchaseController {

    private final InventoryService inventoryService;

    @PostMapping
    @Operation(summary = "Process a purchase and deduct stock")
    public ResponseEntity<JsonApiResponse> purchase(
            @RequestHeader("Idempotency-Key") UUID idempotencyKey,
            @Valid @RequestBody PurchaseRequest request) {
        return ResponseEntity.ok(inventoryService.purchase(idempotencyKey, request));
    }
}
