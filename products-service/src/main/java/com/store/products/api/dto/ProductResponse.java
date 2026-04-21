package com.store.products.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductResponse {
    private ProductData data;

    public static ProductResponse of(ProductData data) {
        return ProductResponse.builder().data(data).build();
    }
}
