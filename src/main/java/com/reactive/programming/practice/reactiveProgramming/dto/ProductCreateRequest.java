package com.reactive.programming.practice.reactiveProgramming.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * ProductCreateRequest - DTO for creating new products
 * 
 * Separate from response DTO because:
 * 1. Request doesn't need id, createdAt, updatedAt (server generates these)
 * 2. Validation rules may differ between request and response
 * 3. Clear contract for API clients
 * 4. Server can ignore extra fields without exposing internals
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreateRequest {

    /**
     * Validation annotations work with OpenAPI to define constraints
     * - @NotBlank: validates not null and not empty string
     * - OpenAPI displays these as requirements in the schema
     */
    @NotBlank(message = "Product name cannot be blank")
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    @Schema(
            description = "Product name (required)",
            example = "MacBook Pro M3",
            minLength = 1,
            maxLength = 255,
            required = true
    )
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Schema(
            description = "Product description (optional)",
            example = "High-performance laptop with M3 processor and 16GB RAM",
            maxLength = 1000,
            required = false
    )
    private String description;

    /**
     * Constraints on BigDecimal prices
     * - @DecimalMin: ensures price is greater than 0 (not less than or equal)
     * - @NotNull: required field
     * - OpenAPI displays this as type=number with minimum constraint
     */
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Price must be greater than 0")
    @Schema(
            description = "Product price in USD (must be > 0)",
            example = "1999.99",
            type = "number",
            format = "decimal",
            minimum = "0.01",
            required = true
    )
    private BigDecimal price;

    /**
     * Constraints on quantity
     * - @Min: ensures quantity >= 0
     * - @NotNull: required field
     * - Prevents negative inventory
     */
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    @Schema(
            description = "Product stock quantity (must be >= 0)",
            example = "25",
            type = "integer",
            minimum = "0",
            required = true
    )
    private Integer quantity;

    @NotBlank(message = "Category cannot be blank")
    @Schema(
            description = "Product category or classification",
            example = "Electronics",
            required = true
    )
    private String category;

    /**
     * Validation with pattern matching
     * - @Pattern: regex validation for specific formats
     * - OpenAPI displays allowed values
     */
    @NotBlank(message = "Status cannot be blank")
    @Pattern(
            regexp = "ACTIVE|INACTIVE",
            message = "Status must be either ACTIVE or INACTIVE"
    )
    @Schema(
            description = "Product status (ACTIVE or INACTIVE)",
            example = "ACTIVE",
            allowableValues = {"ACTIVE", "INACTIVE"},
            required = true
    )
    private String status;

    /**
     * EXAMPLE annotations for Swagger UI
     * When you expand an endpoint in Swagger, it shows real example data
     * Helps API consumers see exactly what to send
     */
    public static class CreateExample {
        public static final ProductCreateRequest example = ProductCreateRequest.builder()
                .name("MacBook Pro M3")
                .description("High-performance laptop with M3 processor")
                .price(BigDecimal.valueOf(1999.99))
                .quantity(25)
                .category("Electronics")
                .status("ACTIVE")
                .build();
    }
}
