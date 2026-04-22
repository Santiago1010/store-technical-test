package com.store.inventory.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(int requested, int available) {
        super("Insufficient stock: requested " + requested + ", available " + available);
    }
}
