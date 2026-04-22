package com.store.inventory.exception;

public class ProductsServiceUnavailableException extends RuntimeException {
    public ProductsServiceUnavailableException(String message) {
        super(message);
    }
}
