package com.reactive.programming.practice.reactiveProgramming.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product domain model class representing a product in the MongoDB database.
 * This class uses Lombok annotations to reduce boilerplate code.
 *
 * Key Points for Interviews:
 * - @Document marks this as a MongoDB document (collection name defaults to 'product')
 * - @Id marks the primary key field
 * - BigDecimal is used for price (financial calculations need precision)
 * - @Builder provides a builder pattern for object creation (useful for testing)
 * - @Data combines @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
 */
@Document(collection = "products")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product {

    /**
     * Unique identifier for the product.
     * MongoDB automatically generates ObjectId if not provided.
     */
    @Id
    private String id;

    /**
     * Product name - basic product information.
     * Cannot be null for a valid product.
     */
    private String name;

    /**
     * Product description - detailed information about the product.
     */
    private String description;

    /**
     * Product price stored as BigDecimal for precision.
     * Financial operations should never use float/double.
     */
    private BigDecimal price;

    /**
     * Product stock quantity.
     * Useful for inventory management.
     */
    private Integer quantity;

    /**
     * Category of the product for classification.
     */
    private String category;

    /**
     * Status of the product (e.g., ACTIVE, DISCONTINUED, OUT_OF_STOCK).
     */
    private String status;

    /**
     * Timestamp when the product was created.
     * Automatically set to current time, immutable after creation.
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when the product was last updated.
     * Updated whenever the product is modified.
     */
    private LocalDateTime updatedAt;
}
