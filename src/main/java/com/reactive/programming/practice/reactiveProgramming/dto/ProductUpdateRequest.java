package com.reactive.programming.practice.reactiveProgramming.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * ProductUpdateRequest - DTO for updating existing products
 * Separate from create request because:
 * 1. All fields are optional (partial updates via PATCH)
 * 2. PUT requests update full objects (all fields required)
 * 3. Clear contract for different HTTP verbs
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateRequest {

    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    @Schema(
            description = "Product name (optional)",
            example = "MacBook Pro M3 Updated",
            minLength = 1,
            maxLength = 255,
            required = false
    )
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Schema(
            description = "Product description (optional)",
            example = "Updated high-performance laptop",
            maxLength = 1000,
            required = false
    )
    private String description;

    @DecimalMin(value = "0.01", inclusive = true, message = "Price must be greater than 0")
    @Schema(
            description = "Product price in USD (optional, must be > 0 if provided)",
            example = "2099.99",
            type = "number",
            format = "decimal",
            minimum = "0.01",
            required = false
    )
    private BigDecimal price;

    @Min(value = 0, message = "Quantity cannot be negative")
    @Schema(
            description = "Product stock quantity (optional, must be >= 0 if provided)",
            example = "30",
            type = "integer",
            minimum = "0",
            required = false
    )
    private Integer quantity;

    @Schema(
            description = "Product category (optional)",
            example = "Electronics",
            required = false
    )
    private String category;

    @Pattern(
            regexp = "ACTIVE|INACTIVE",
            message = "Status must be either ACTIVE or INACTIVE"
    )
    @Schema(
            description = "Product status (optional, must be ACTIVE or INACTIVE if provided)",
            example = "ACTIVE",
            allowableValues = {"ACTIVE", "INACTIVE"},
            required = false
    )
    private String status;
}
