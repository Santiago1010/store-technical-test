package com.store.products.domain.service;

import com.store.products.api.dto.*;
import com.store.products.domain.entity.Product;
import com.store.products.domain.mapper.ProductMapper;
import com.store.products.domain.repository.ProductRepository;
import com.store.products.exception.ProductNotFoundException;
import com.store.products.exception.SkuAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @Mock ProductMapper productMapper;
    @InjectMocks ProductService service;

    UUID id;
    Product product;
    ProductData productData;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        product = Product.builder().id(id).sku("SKU-1").name("Widget")
            .price(BigDecimal.TEN).build();
        productData = ProductData.builder().id(id.toString())
            .attributes(ProductAttributes.builder().sku("SKU-1").name("Widget")
                .price(BigDecimal.TEN).build()).build();
    }

    @Test
    void findAll_returnsList() {
        when(productRepository.findAll()).thenReturn(List.of(product));
        when(productMapper.toData(product)).thenReturn(productData);
        assertThat(service.findAll().getData()).hasSize(1);
    }

    @Test
    void findById_found() {
        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productMapper.toData(product)).thenReturn(productData);
        assertThat(service.findById(id).getData().getId()).isEqualTo(id.toString());
    }

    @Test
    void findById_notFound_throws() {
        when(productRepository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(id))
            .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void create_duplicateSku_throws() {
        when(productRepository.existsBySku("SKU-1")).thenReturn(true);
        var req = new CreateProductRequest("SKU-1", "Widget", null, BigDecimal.TEN);
        assertThatThrownBy(() -> service.create(req))
            .isInstanceOf(SkuAlreadyExistsException.class);
    }

    @Test
    void create_success() {
        when(productRepository.existsBySku("SKU-1")).thenReturn(false);
        when(productRepository.save(any())).thenReturn(product);
        when(productMapper.toData(product)).thenReturn(productData);
        var req = new CreateProductRequest("SKU-1", "Widget", null, BigDecimal.TEN);
        assertThat(service.create(req).getData()).isNotNull();
    }

    @Test
    void update_notFound_throws() {
        when(productRepository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.update(id, new UpdateProductRequest("New", null, null)))
            .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void delete_notFound_throws() {
        when(productRepository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.delete(id))
            .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void delete_success() {
        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        service.delete(id);
        verify(productRepository).delete(product);
    }
}