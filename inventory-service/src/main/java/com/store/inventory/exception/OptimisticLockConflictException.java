package com.store.inventory.exception;

public class OptimisticLockConflictException extends RuntimeException {
    public OptimisticLockConflictException() {
        super("Could not acquire lock after maximum retries");
    }
}
