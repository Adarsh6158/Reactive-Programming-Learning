package com.reactive.programming.practice.reactiveProgramming.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ProductResponseDTO - Data Transfer Object for API responses
 * 
 * DTOs are used in APIs instead of domain entities to:
 * 1. Separate API contracts from database entities
 * 2. Control what fields are exposed to clients
 * 3. Enable API versioning independently
 * 4. Provide clear documentation for API consumers
 * 5. Avoid accidental exposure of internal fields
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDTO {

    /**
     * @Schema annotation documents fields in OpenAPI
     * - name: property name in JSON
     * - description: what this field represents
     * - example: concrete example value for UI
     * - required: whether field is mandatory
     */
    @Schema(
            name = "id",
            description = "Unique product identifier (MongoDB ObjectId)",
            example = "507f1f77bcf86cd799439011",
            required = true
    )
    private String id;

    @Schema(
            name = "name",
            description = "Product display name",
            example = "MacBook Pro M3",
            required = true,
            minLength = 1,
            maxLength = 255
    )
    private String name;

    @Schema(
            name = "description",
            description = "Detailed product description",
            example = "High-performance laptop with M3 processor",
            required = false,
            maxLength = 1000
    )
    private String description;

    @Schema(
            name = "price",
            description = "Product price in USD (must be > 0)",
            example = "1999.99",
            type = "number",
            format = "decimal",
            required = true
    )
    private BigDecimal price;

    @Schema(
            name = "quantity",
            description = "Available stock quantity (must be >= 0)",
            example = "25",
            type = "integer",
            minimum = "0",
            required = true
    )
    private Integer quantity;

    @Schema(
            name = "category",
            description = "Product category/classification",
            example = "Electronics",
            required = true,
            enumAsRef = false
    )
    private String category;

    @Schema(
            name = "status",
            description = "Product status (ACTIVE or INACTIVE)",
            example = "ACTIVE",
            required = true,
            allowableValues = {"ACTIVE", "INACTIVE"}
    )
    private String status;

    @Schema(
            name = "createdAt",
            description = "Timestamp when product was created (ISO-8601 format)",
            example = "2026-04-05T17:03:54.593",
            type = "string",
            format = "date-time",
            required = true
    )
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @Schema(
            name = "updatedAt",
            description = "Timestamp when product was last modified (ISO-8601 format)",
            example = "2026-04-05T17:04:25.064",
            type = "string",
            format = "date-time",
            required = true
    )
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
}
