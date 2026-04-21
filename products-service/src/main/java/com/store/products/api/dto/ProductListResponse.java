package com.store.products.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductListResponse {
    private List<ProductData> data;
}
