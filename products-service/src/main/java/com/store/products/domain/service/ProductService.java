package com.store.products.domain.service;

import com.store.products.api.dto.*;
import com.store.products.domain.entity.Product;
import com.store.products.domain.mapper.ProductMapper;
import com.store.products.domain.repository.ProductRepository;
import com.store.products.exception.ProductNotFoundException;
import com.store.products.exception.SkuAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public ProductListResponse findAll() {
        List<ProductData> data = productRepository.findAll()
            .stream()
            .map(productMapper::toData)
            .toList();
        return ProductListResponse.builder().data(data).build();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(UUID id) {
        Product product = getOrThrow(id);
        return ProductResponse.of(productMapper.toData(product));
    }

    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new SkuAlreadyExistsException(request.getSku());
        }
        Product product = Product.builder()
            .sku(request.getSku())
            .name(request.getName())
            .description(request.getDescription())
            .price(request.getPrice())
            .build();
        Product saved = productRepository.save(product);
        log.info("event=product_created productId={} sku={}", saved.getId(), saved.getSku());
        return ProductResponse.of(productMapper.toData(saved));
    }

    @Transactional
    public ProductResponse update(UUID id, UpdateProductRequest request) {
        Product product = getOrThrow(id);
        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        Product saved = productRepository.save(product);
        log.info("event=product_updated productId={}", saved.getId());
        return ProductResponse.of(productMapper.toData(saved));
    }

    @Transactional
    public void delete(UUID id) {
        Product product = getOrThrow(id);
        productRepository.delete(product);
        log.info("event=product_deleted productId={}", id);
    }

    private Product getOrThrow(UUID id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
