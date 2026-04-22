package com.store.products.domain.mapper;

import com.store.products.api.dto.ProductAttributes;
import com.store.products.api.dto.ProductData;
import com.store.products.domain.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    private static final String TYPE = "products";

    public ProductData toData(Product product) {
        return ProductData.builder()
            .id(product.getId())
            .type(TYPE)
            .attributes(ProductAttributes.builder()
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build())
            .build();
    }
}
