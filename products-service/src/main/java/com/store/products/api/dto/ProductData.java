package com.store.products.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ProductData {
    private UUID id;
    private String type;
    private ProductAttributes attributes;
}
