package com.reactive.programming.practice.reactiveProgramming.service;

import com.reactive.programming.practice.reactiveProgramming.exception.ProductNotFoundException;
import com.reactive.programming.practice.reactiveProgramming.model.Product;
import com.reactive.programming.practice.reactiveProgramming.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

/**
 * ProductService - Business logic layer for Product operations.
 * This service handles all product-related operations and acts as bridge
 * between repository and controller layers.
 *
 * Key Points for Interviews:
 * - @Service marks this as a business logic/service component
 * - @RequiredArgsConstructor: Lombok generates constructor using final fields
 *   This is the modern way to do dependency injection (field injection is discouraged)
 * - All methods return Mono or Flux for reactive, non-blocking operations
 * - Service adds business logic: validation, timestamps, error handling
 * - @Slf4j provides logger instance for logging (from Lombok)
 *
 * Design Pattern: Separation of Concerns
 * - Controller: handles HTTP requests/responses
 * - Service: implements business logic
 * - Repository: handles data access
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Retrieve all products.
     * Returns a Flux (stream) of all products in the database.
     *
     * Reactive Pattern:
     * - Non-blocking: doesn't wait for all products to load
     * - Streaming: products are emitted as they're fetched
     * - Backpressure: client controls consumption speed
     *
     * @return Flux<Product> stream of all products
     */
    public Flux<Product> getAllProducts() {
        log.info("Fetching all products");
        return productRepository.findAll()
                .doOnNext(product -> log.debug("Retrieved product: {}", product.getId()))
                .doOnError(error -> log.error("Error fetching products", error))
                .doOnComplete(() -> log.info("Completed fetching all products"));
    }

    /**
     * Find product by ID.
     * Returns Mono - single product (or empty if not found).
     *
     * Error Handling: Uses onErrorMap to convert empty result to custom exception.
     * This makes errors explicit and testable.
     *
     * @param id the product ID to find
     * @return Mono<Product> containing the product if found
     * @throws ProductNotFoundException if product doesn't exist
     */
    public Mono<Product> getProductById(String id) {
        log.debug("Finding product by id: {}", id);

        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(
                        String.format("Product with ID: %s not found", id))))
                .doOnNext(product -> log.debug("Found product: {}", product.getName()))
                .doOnError(error -> log.error("Error finding product", error));
    }

    /**
     * Create new product.
     * Validates input and sets audit fields (createdAt, updatedAt).
     *
     * Business Logic:
     * - Sets creation timestamp
     * - Sets initial status if not provided
     * - Validates product data
     *
     * @param product the product to create
     * @return Mono<Product> containing saved product with generated ID
     * @throws IllegalArgumentException if product data is invalid
     */
    public Mono<Product> createProduct(Product product) {
        log.info("Creating new product: {}", product.getName());

        // Validation logic
        if (product.getName() == null || product.getName().isBlank()) {
            return Mono.error(new IllegalArgumentException("Product name cannot be blank"));
        }
        if (product.getPrice() == null || product.getPrice().signum() <= 0) {
            return Mono.error(new IllegalArgumentException("Product price must be greater than zero"));
        }

        // Set audit fields
        product.setId(null); // MongoDB generates ID
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        product.setStatus(product.getStatus() != null ? product.getStatus() : "ACTIVE");

        return productRepository.save(product)
                .doOnSuccess(saved -> log.info("Product created successfully with ID: {}", saved.getId()))
                .doOnError(error -> log.error("Error creating product", error));
    }

    /**
     * Update existing product.
     * Retrieves product, applies updates, and saves back.
     *
     * Reactive Chain Pattern:
     * - First find the product (Mono)
     * - If found, update it and save (Mono)
     * - If not found, return error
     *
     * This demonstrates proper reactive composition.
     *
     * @param id the product ID to update
     * @param productDetails the new product data
     * @return Mono<Product> containing updated product
     */
    public Mono<Product> updateProduct(String id, Product productDetails) {
        log.info("Updating product with ID: {}", id);

        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(
                        String.format("Product with ID: %s not found", id))))
                .flatMap(existingProduct -> {
                    // Update only provided fields
                    if (productDetails.getName() != null) {
                        existingProduct.setName(productDetails.getName());
                    }
                    if (productDetails.getDescription() != null) {
                        existingProduct.setDescription(productDetails.getDescription());
                    }
                    if (productDetails.getPrice() != null) {
                        existingProduct.setPrice(productDetails.getPrice());
                    }
                    if (productDetails.getQuantity() != null) {
                        existingProduct.setQuantity(productDetails.getQuantity());
                    }
                    if (productDetails.getCategory() != null) {
                        existingProduct.setCategory(productDetails.getCategory());
                    }
                    if (productDetails.getStatus() != null) {
                        existingProduct.setStatus(productDetails.getStatus());
                    }

                    existingProduct.setUpdatedAt(LocalDateTime.now());

                    return productRepository.save(existingProduct);
                })
                .doOnSuccess(updated -> log.info("Product updated successfully: {}", id))
                .doOnError(error -> log.error("Error updating product", error));
    }

    /**
     * Delete product by ID.
     * Returns Mono<Void> - completion signal.
     *
     * Interview Point: Mono<Void> is used for operations with no return value.
     * It signals either successful completion or error.
     *
     * @param id the product ID to delete
     * @return Mono<Void> signal of completion or error
     */
    public Mono<Void> deleteProduct(String id) {
        log.info("Deleting product with ID: {}", id);

        return productRepository.existsById(id)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new ProductNotFoundException(
                                String.format("Product with ID: %s not found", id)));
                    }
                    return productRepository.deleteById(id);
                })
                .doOnSuccess(v -> log.info("Product deleted successfully: {}", id))
                .doOnError(error -> log.error("Error deleting product", error));
    }

    /**
     * Find products by category.
     * Demonstrates Flux for multiple results.
     *
     * @param category the category to filter by
     * @return Flux<Product> stream of products in category
     */
    public Flux<Product> getProductsByCategory(String category) {
        log.info("Fetching products for category: {}", category);

        return productRepository.findByCategory(category)
                .doOnNext(product -> log.debug("Product in category: {}", product.getName()))
                .switchIfEmpty(Flux.error(new ProductNotFoundException(
                        String.format("No products found in category: %s", category))));
    }

    /**
     * Find products by status.
     * Another example of Flux returning multiple items.
     *
     * @param status the product status to filter by
     * @return Flux<Product> stream of products with status
     */
    public Flux<Product> getProductsByStatus(String status) {
        log.info("Fetching products with status: {}", status);

        return productRepository.findByStatus(status)
                .doOnNext(product -> log.debug("Product with status: {}", product.getName()));
    }

    /**
     * Count products in a category.
     * Returns Mono<Long>
     *
     * @param category the category name
     * @return Mono<Long> count of products
     */
    public Mono<Long> countProductsByCategory(String category) {
        log.debug("Counting products in category: {}", category);

        return productRepository.countByCategory(category);
    }

    /**
     * Search products by name pattern.
     * Demonstrates custom query via repository.
     *
     * @param namePattern the pattern to search for
     * @return Flux<Product> matching products
     */
    public Flux<Product> searchProductsByName(String namePattern) {
        log.info("Searching products with pattern: {}", namePattern);

        return productRepository.findByNamePattern(namePattern)
                .doOnNext(product -> log.debug("Found matching product: {}", product.getName()));
    }
}
