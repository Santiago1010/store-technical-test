package com.store.products.api.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class ProductAttributes {
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private Instant createdAt;
    private Instant updatedAt;
}
