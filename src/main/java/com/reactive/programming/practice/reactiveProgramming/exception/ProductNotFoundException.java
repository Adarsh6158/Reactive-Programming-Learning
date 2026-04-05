package com.reactive.programming.practice.reactiveProgramming.exception;

/**
 * Custom exception for when a Product is not found in the database.
 * Used throughout the service and controller layers to signal product absence.
 *
 * Key Points for Interviews:
 * - Extends RuntimeException for unchecked exception (no throws declaration needed)
 * - Spring treats unchecked exceptions specially in error handling
 * - Custom exceptions make error handling specific and clear
 * - Important to throw this from service layer and handle in controller
 */
public class ProductNotFoundException extends RuntimeException {

    /**
     * Constructor accepting error message.
     *
     * @param message descriptive error message about the missing product
     */
    public ProductNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor accepting error message and underlying cause.
     * Useful for exception chaining and debugging.
     *
     * @param message descriptive error message
     * @param cause the underlying exception that caused this
     */
    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
