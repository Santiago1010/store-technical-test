package com.store.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateInventoryRequest {

    @NotNull(message = "productId is required")
    private UUID productId;

    @NotNull(message = "available is required")
    @Min(value = 0, message = "available must be at least 0")
    private Integer available;
}