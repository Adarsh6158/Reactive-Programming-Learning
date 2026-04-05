package com.reactive.programming.practice.reactiveProgramming.repository;

import com.reactive.programming.practice.reactiveProgramming.model.Product;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive Repository interface for Product persistence operations.
 * ReactiveMongoRepository provides reactive CRUD operations.
 *
 * Key Points for Interviews:
 * - Extends ReactiveMongoRepository which provides reactive methods:
 *   * Mono<T> save(T entity) - returns single result wrapped in Mono
 *   * Mono<T> findById(ID id) - returns single result or empty
 *   * Flux<T> findAll() - returns stream of results
 *   * Mono<Void> deleteById(ID id) - deletion returns Mono<Void>
 * - @Query allows custom MongoDB query definitions
 * - Spring Data generates implementation at runtime using reflection
 * - Works seamlessly with Spring Data MongoDB for reactive operations
 * - First parameter is Entity type, second is ID type
 */
@Repository
public interface ProductRepository extends ReactiveMongoRepository<Product, String> {

    /**
     * Find products by category name.
     * Returns a Flux (stream) of products matching the category.
     *
     * Reactive Query: Will emit all matching products as stream.
     * Non-blocking: Query executes asynchronously without blocking thread.
     *
     * @param category the category name to search for
     * @return Flux containing all products in the category
     */
    Flux<Product> findByCategory(String category);

    /**
     * Find products by status.
     * Useful for filtering products by their availability or state.
     *
     * @param status the product status (e.g., ACTIVE, DISCONTINUED)
     * @return Flux containing all products with the given status
     */
    Flux<Product> findByStatus(String status);

    /**
     * Find products with price greater than specified amount.
     * Custom MongoDB query using @Query annotation.
     * The '?0' is a placeholder for the first method parameter.
     *
     * Interview Note: Demonstrates custom query against reactive repository.
     *
     * @param price the minimum price threshold
     * @return Flux containing all products with price greater than specified
     */
    @Query("{ 'price' : { $gt: ?0 } }")
    Flux<Product> findProductsWithPriceGreaterThan(java.math.BigDecimal price);

    /**
     * Find products by name pattern (contains).
     * Uses MongoDB regex query and the $regex operator.
     *
     * @param namePattern the pattern to search for in product names
     * @return Flux containing products matching the name pattern
     */
    @Query("{ 'name' : { $regex: ?0, $options: 'i' } }")
    Flux<Product> findByNamePattern(String namePattern);

    /**
     * Find single product by exact name match.
     * Returns Mono since name uniqueness is expected.
     *
     * @param name the exact product name
     * @return Mono containing the product if found, empty Mono if not
     */
    Mono<Product> findByName(String name);

    /**
     * Check if a product with given name exists.
     * Returns a Mono<Boolean> - reactive way to check existence.
     *
     * Interview Note: Demonstrates existence check in reactive way.
     *
     * @param name the product name
     * @return Mono<Boolean> - true if exists, false otherwise
     */
    Mono<Boolean> existsByName(String name);

    /**
     * Count products in a specific category.
     * Returns Mono<Long> with the count.
     *
     * @param category the category to count
     * @return Mono<Long> containing the count
     */
    Mono<Long> countByCategory(String category);
}
